package walla.db;

import javax.sql.DataSource;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.utils.*;

import org.springframework.http.HttpStatus;

@Repository
public class AccountDataHelperImpl implements AccountDataHelper {

	private DataSource dataSource;
	
	private static final Logger meLogger = Logger.getLogger(AccountDataHelperImpl.class);

	public AccountDataHelperImpl() {
		meLogger.debug("AccountDataHelperImpl object instantiated.");
	}
	
	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}
	
	public long CreateAccount(Account newAccount) throws WallaException
	{
		Connection conn = null;
		CallableStatement createSproc = null;

		try {			
			//Execute SetupNewUser 'Stanley', 'Stanley Prem', 'stanley@fotowalla.com', 'Stan-a-rillo', 1

			conn = dataSource.getConnection();
			conn.setAutoCommit(true);
			
			String sprocSql = "EXEC [dbo].[SetupNewUser] ?, ?, ?, ?, ?, ?";
				
			createSproc = conn.prepareCall(sprocSql);
			createSproc.setString(1, newAccount.getProfileName());
			createSproc.setString(2, newAccount.getDesc());
			createSproc.setString(3, newAccount.getEmail());
			createSproc.setString(4, newAccount.getPassword());
			createSproc.setInt(5, newAccount.getAccountType());
			createSproc.registerOutParameter(6, Types.INTEGER);
			createSproc.execute();
			    
		    long newUserId = createSproc.getLong(6);
		    if (newUserId < 1)
		    {
		    	String error = "SetupNewUser sproc didn't return a valid user number";
				meLogger.error(error);
				throw new WallaException(this.getClass().getName(), "CreateAccount", error, HttpStatus.INTERNAL_SERVER_ERROR.value());
		    }

			meLogger.debug("CreateAccount() completes OK. UserId:" + newUserId);
			return newUserId;
		} 
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in CreateAccount", sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
		} catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in CreateAccount", ex);
			throw new WallaException(ex, HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally {
	        if (createSproc != null) try { createSproc.close(); } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { conn.close(); } catch (SQLException logOrIgnore) {}
		}
	}

	public void UpdateAccount(long userId, Account account) throws WallaException
	{
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {			
			int returnCount = 0;
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			String updateSql = "UPDATE [dbo].[User] SET [Description] = ?, [Email] = ?,[Password] = ?,"
					+ "[RecordVersion] = [RecordVersion] + 1 WHERE [UserId] = ? AND [RecordVersion] = ?";
			
			ps = conn.prepareStatement(updateSql);
			ps.setString(1, account.getDesc());
			ps.setString(2, account.getEmail());
			ps.setString(2, account.getPassword());
			ps.setLong(3, userId);
			ps.setInt(4, account.getVersion());
			
			//Execute update and check response.
			returnCount = ps.executeUpdate();
			ps.close();
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Update statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException("AccountDataHelperImpl", "UpdateAccount", error, HttpStatus.CONFLICT.value()); 
			}
			
			conn.commit();
		} 
		catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected SQLException in UpdateAccount", sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
		} catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected Exception in UpdateAccount", ex);
			throw new WallaException(ex, HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally {
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { conn.close(); } catch (SQLException logOrIgnore) {}
		}
	}
	
	public boolean CheckProfileNameIsUnique(String profileName) throws WallaException
	{
		Connection conn = null;
		Statement sQuery = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();
			
			String selectSql = "SELECT 1 FROM [User] WHERE UPPER([ProfileName]) = UPPER('" + profileName + "')";
			sQuery = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			resultset = sQuery.executeQuery(selectSql);
			if (resultset.next())
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in CheckProfileNameIsUnique", sqlEx);
			throw new WallaException(sqlEx,0);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (sQuery != null) try { if (!sQuery.isClosed()) {sQuery.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	public Account GetAccount(long userId) throws WallaException
	{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		Account account = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT [ProfileName],[Description],[Email],[Status],[AccountTypeName],[RecordVersion],[OpenDate],[CloseDate],[StorageGBLimit],"
								+ "[StorageGBCurrent],[TotalImages],[MonthlyUploadCap],[UploadCount30Days] FROM [dbo].[AccountSummary] "
								+ "WHERE [UserId] = ?";
							
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, userId);

			resultset = ps.executeQuery();

			if (!resultset.next())
			{
				return null;
			}
			
			account = new Account();
			account.setId(userId);
			account.setProfileName(resultset.getString(1));
			account.setDesc(resultset.getString(2));
			account.setEmail(resultset.getString(3));
			account.setStatus(resultset.getInt(4));
			account.setAccountTypeName(resultset.getString(5));
			account.setVersion(resultset.getInt(6));
			
			GregorianCalendar oldGreg = new GregorianCalendar();
			oldGreg.setTime(resultset.getTimestamp(7));
			XMLGregorianCalendar xmlOldGreg = DatatypeFactory.newInstance().newXMLGregorianCalendar(oldGreg);
			account.setOpenDate(xmlOldGreg);
			
			oldGreg.setTime(resultset.getTimestamp(8));
			xmlOldGreg = DatatypeFactory.newInstance().newXMLGregorianCalendar(oldGreg);
			account.setCloseDate(xmlOldGreg);
			
			account.setStorageGBLimit(resultset.getDouble(9));
			account.setStorageGBCurrent(resultset.getDouble(10));
			account.setTotalImages(resultset.getInt(11));
			account.setMonthlyUploadCap(resultset.getInt(12));
			account.setUploadCount30Days(resultset.getInt(13));

			return account;
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetAccount", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetAccount", ex);
			throw new WallaException(ex, 0);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	public void CreateUserApp(long userId, UserApp userApp) throws WallaException
	{
		String sql = "INSERT INTO [dbo].[UserApp]([UserAppId],[PlatformId],[AppId],[MachineName],[LastUsed],[Blocked],[TagId],[CategoryId],[GalleryId],"
					+ "[FetchSize],[ThumbCacheMB],[MainCopyCacheMB],[MainCopyFolder],[AutoUpload],[AutoUploadFolder],[RecordVersion],[UserId])"
					+ "VALUES(?,?,?,?,GetDateNoMS(),0,?,?,?,?,?,?,?,?,?,1,?)";

		Connection conn = null;
		PreparedStatement ps = null;
		try {			
			int returnCount = 0;

			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			//Insert main tag record.
			ps = conn.prepareStatement(sql);
			ps.setLong(1, userApp.getId());
			ps.setInt(2, userApp.getPlatformId());
			ps.setInt(3, userApp.getAppId());
			ps.setString(4, userApp.getMachineName());
			ps.setLong(5, userApp.getTagId());
			ps.setLong(6, userApp.getCategoryId());
			ps.setLong(7, userApp.getGalleryId());
			ps.setInt(8, userApp.getFetchSize());
			ps.setInt(9, userApp.getThumbCacheSizeMB());
			ps.setInt(10, userApp.getMainCopyCacheSizeMB());
			ps.setString(11, userApp.getMainCopyFolder());
			ps.setBoolean(12, userApp.isAutoUpload());
			ps.setString(13, userApp.getAutoUploadFolder());
			ps.setLong(14, userId);
			
			//Execute insert statement.
			returnCount = ps.executeUpdate();
			
			//Validate new record was successful.
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Insert statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException(this.getClass().getName(), "CreateUserApp", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 				
			}
			
			conn.commit();
				
		} catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected SQLException in CreateUserApp", sqlEx);
			throw new WallaException(sqlEx, HttpStatus.INTERNAL_SERVER_ERROR.value());
		} catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			
			meLogger.error("Unexpected Exception in CreateUserApp", ex);
			throw new WallaException(ex, HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally {
	        if (ps != null) try { ps.close(); } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { conn.close(); } catch (SQLException logOrIgnore) {}
		}
	}
	
	public void UpdateUserApp(long userId, UserApp userApp) throws WallaException
	{
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {			
			int returnCount = 0;
			String updateVersionSql = null;
			
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			updateVersionSql = "UPDATE [dbo].[UserApp] SET [MachineName] = ?,[LastUsed] = dbo.GetDateNoMS()"
								+ ",[TagId] = ?,[CategoryId] = ?,[GalleryId] = ?,[FetchSize] = ?,[ThumbCacheMB] = ?"
								+ ",[MainCopyCacheMB] = ?,[MainCopyFolder] = ?,[AutoUpload] = ?,[AutoUploadFolder] = ?,"
								+ "[RecordVersion] = [RecordVersion] + 1 WHERE [UserId] = ? AND [UserAppId] = ? AND [RecordVersion] = ?";

			ps = conn.prepareStatement(updateVersionSql);
			ps.setString(1, userApp.getMachineName());
			ps.setLong(2, userApp.getTagId());
			ps.setLong(3, userApp.getCategoryId());
			ps.setLong(4, userApp.getGalleryId());
			ps.setInt(5, userApp.getFetchSize());
			ps.setInt(6, userApp.getThumbCacheSizeMB());
			ps.setInt(7, userApp.getMainCopyCacheSizeMB());
			ps.setString(8, userApp.getMainCopyFolder());
			ps.setBoolean(9, userApp.isAutoUpload());
			ps.setString(10, userApp.getAutoUploadFolder());
			
			ps.setLong(11, userId);
			ps.setLong(12, userApp.getId());
			ps.setInt(13, userApp.getVersion());
			
			//Execute update and check response.
			returnCount = ps.executeUpdate();
			ps.close();
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Update statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException("AccountDataHelperImpl", "UpdateUserApp", error, HttpStatus.CONFLICT.value()); 
			}
			
			conn.commit();
		}
		catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected Exception in UpdateUserApp", sqlEx);
			throw new WallaException(sqlEx, HttpStatus.INTERNAL_SERVER_ERROR.value());
		} catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected Exception in UpdateUserApp", ex);
			throw new WallaException(ex, HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally {
	        if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	public UserApp GetUserApp(long userId, long userAppId) throws WallaException
	{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		UserApp userApp = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT [PlatformId],[MachineName],[LastUsed],[Blocked]"
								+ ",[TagId],[CategoryId],[GalleryId],[RecordVersion],[FetchSize]"
								+ ",[ThumbCacheMB],[MainCopyCacheMB],[MainCopyFolder],[AutoUpload],[AutoUploadFolder]"
								+ "FROM [dbo].[UserApp] WHERE [UserId] = ? AND [UserAppId] = ?";
							
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, userId);
			ps.setLong(2, userAppId);

			resultset = ps.executeQuery();

			if (!resultset.next())
			{
				return null;
			}
			
			if (resultset.getBoolean(4))
			{
		    	String error = "User app has been explicitly blocked, request cannot continue.";
				meLogger.error(error);
				throw new WallaException(this.getClass().getName(), "GetUserApp", error, HttpStatus.FORBIDDEN.value());
			}
			
			userApp = new UserApp();
			userApp.setId(userAppId);
			userApp.setPlatformId(resultset.getInt(1));
			userApp.setMachineName(resultset.getString(2));
			
			GregorianCalendar oldGreg = new GregorianCalendar();
			oldGreg.setTime(resultset.getTimestamp(3));
			XMLGregorianCalendar xmlOldGreg = DatatypeFactory.newInstance().newXMLGregorianCalendar(oldGreg);
			userApp.setLastUsed(xmlOldGreg);
			
			userApp.setTagId(resultset.getLong(5));
			userApp.setCategoryId(resultset.getLong(6));
			userApp.setGalleryId(resultset.getLong(7));
			userApp.setVersion(resultset.getInt(8));
			userApp.setFetchSize(resultset.getInt(9));
			userApp.setThumbCacheSizeMB(resultset.getInt(10));
			userApp.setMainCopyCacheSizeMB(resultset.getInt(11));
			userApp.setMainCopyFolder(resultset.getString(12));
			userApp.setAutoUpload(resultset.getBoolean(13));
			userApp.setAutoUploadFolder(resultset.getString(14));

			return userApp;
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetUserApp", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetUserApp", ex);
			throw new WallaException(ex, 0);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	public App GetApp(int appId, String key) throws WallaException
	{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT [AppId],[Name],[WSKey],[MajorVersion],[MinorVersion],[Status],[DefaultFetchSize],[DefaultThumbCacheMB],[DefaultMainCopyCacheMB]"
								+ " FROM [dbo].[App] WHERE [AppId] = ? OR [WSKey] = ?";
							
			ps = conn.prepareStatement(selectSql);
			ps.setInt(1, appId);
			ps.setString(2, key);

			resultset = ps.executeQuery();
			if (!resultset.next())
			{
				return null;
			}
			
			App app = new App();
			
			app.setAppId(resultset.getInt(1));
			app.setName(resultset.getString(2));
			app.setWSKey(resultset.getString(3));
			app.setMajorVersion(resultset.getInt(4));
			app.setMinorVersion(resultset.getInt(5));
			app.setStatus(resultset.getInt(6));
			app.setDefaultFetchSize(resultset.getInt(7));
			app.setDefaultThumbCacheMB(resultset.getInt(8));
			app.setDefaultMainCopyCacheMB(resultset.getInt(9));

			return app;
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetApp", sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
		} 
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetApp", ex);
			throw new WallaException(ex,HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
		
}
