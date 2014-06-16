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
public class UtilityDataHelperImpl implements UtilityDataHelper{

	private DataSource dataSource;
	
	private static final Logger meLogger = Logger.getLogger(UtilityDataHelperImpl.class);
	
	public List<Platform> GetPlatformList() throws WallaException
	{
		Connection conn = null;
		Statement sQuery = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT [PlatformId],[ShortName],[OperatingSystem],[MachineType],[Supported],[MajorVersion],[MinorVersion] FROM [Platform]";
			
			sQuery = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			resultset = sQuery.executeQuery(selectSql);
			
			List<Platform> platformList = new ArrayList<Platform>();
			
			while (resultset.next())
			{
				Platform platform = new Platform();
				platform.setPlatformId(resultset.getInt(1));
				platform.setShortName(resultset.getString(2));
				platform.setOperatingSystem(resultset.getString(3));
				platform.setMachineType(resultset.getString(4));
				platform.setSupported(resultset.getBoolean(5));
				platform.setMajorVersion(resultset.getInt(6));
				platform.setMinorVersion(resultset.getInt(7));
				platformList.add(platform);
			}
			resultset.close();
			
			if (platformList.size() < 1)
			{
				String error = "GetPlatformObjects didn't return any records";
				meLogger.error(error);
				throw new WallaException("UtilityDataHelperImpl", "GetPlatformObjects", error, 0);
			}
			
			return platformList;
			
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetPlatformObjects", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetPlatformObjects", ex);
			throw new WallaException(ex, 0);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (sQuery != null) try { if (!sQuery.isClosed()) {sQuery.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}

	public List<App> GetAppList() throws WallaException
	{
		Connection conn = null;
		Statement sQuery = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT [AppId],[Name],[WSKey],[MajorVersion],[MinorVersion],[Status],[DefaultFetchSize],[DefaultThumbCacheMB],[DefaultMainCopyCacheMB],[DefaultGalleryType] FROM [App]";
			
			sQuery = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			resultset = sQuery.executeQuery(selectSql);
			
			List<App> appList = new ArrayList<App>();
			
			while (resultset.next())
			{
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
				app.setDefaultGalleryType(resultset.getInt(10));
				appList.add(app);
			}
			resultset.close();
			
			if (appList.size() < 1)
			{
				String error = "GetAppList didn't return any records";
				meLogger.error(error);
				throw new WallaException("UtilityDataHelperImpl", "GetAppList", error, 0);
			}
			
			return appList;
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetAppList", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetAppList", ex);
			throw new WallaException(ex, 0);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (sQuery != null) try { if (!sQuery.isClosed()) {sQuery.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	public List<Style> GetStyleList() throws WallaException
	{
		Connection conn = null;
		Statement sQuery = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT [StyleId],[Name],[Description],[CssFolder],[LastUpdated]  FROM [dbo].[GalleryStyle]";
			
			sQuery = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			resultset = sQuery.executeQuery(selectSql);
			
			List<Style> styleList = new ArrayList<Style>();
			
			while (resultset.next())
			{
				Style style = new Style();
				style.setStyleId(resultset.getInt(1));
				style.setName(resultset.getString(2));
				style.setDesc(resultset.getString(3));
				style.setCssFolder(resultset.getString(4));
				style.setLastUpdated(new java.util.Date(resultset.getTimestamp(5).getTime()));
				styleList.add(style);
			}
			resultset.close();
			
			if (styleList.size() < 1)
			{
				String error = "GetStyleList didn't return any records";
				meLogger.error(error);
				throw new WallaException("UtilityDataHelperImpl", "GetStyleList", error, 0);
			}
			
			return styleList;
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetStyleList", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetStyleList", ex);
			throw new WallaException(ex, 0);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (sQuery != null) try { if (!sQuery.isClosed()) {sQuery.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	public List<Presentation> GetPresentationList() throws WallaException
	{
		Connection conn = null;
		Statement sQuery = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT [PresentationId],[Name],[Description],[JspName],[CssExtension],[MaxSections],[MaxImagesInSection],[LastUpdated] FROM [dbo].[GalleryPresentation]";

			sQuery = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			resultset = sQuery.executeQuery(selectSql);
			
			List<Presentation> presentationList = new ArrayList<Presentation>();
			
			while (resultset.next())
			{
				Presentation presentation = new Presentation();
				presentation.setPresentationId(resultset.getInt(1));
				presentation.setName(resultset.getString(2));
				presentation.setDesc(resultset.getString(3));
				presentation.setJspName(resultset.getString(4));
				presentation.setCssExtension(resultset.getString(5));
				presentation.setMaxSections(resultset.getInt(6));
				presentation.setMaxImagesInSection(resultset.getInt(7));
				presentation.setLastUpdated(new java.util.Date(resultset.getTimestamp(8).getTime()));
				
				presentationList.add(presentation);
			}
			resultset.close();
			
			if (presentationList.size() < 1)
			{
				String error = "GetPresentationList didn't return any records";
				meLogger.error(error);
				throw new WallaException("UtilityDataHelperImpl", "GetPresentationList", error, 0);
			}
			
			return presentationList;
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetPresentationList", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetPresentationList", ex);
			throw new WallaException(ex, 0);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (sQuery != null) try { if (!sQuery.isClosed()) {sQuery.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	public long GetNewId(String idType) throws WallaException
	{
		Connection conn = null;
		CallableStatement idSproc = null;
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(true);
			
			String sprocSql = "EXEC [dbo].[GetId] ?, ?";
			
		    idSproc = conn.prepareCall(sprocSql);
		    idSproc.setString(1, idType);
		    idSproc.registerOutParameter(2, Types.INTEGER);
		    idSproc.execute();
		    
		    long newTagId = idSproc.getLong(2);
		    if (newTagId > 0)
		    {
		    	return newTagId;
		    }
		    else
		    {
		    	String error = "GETID sproc didn't return a positive number";
				meLogger.error(error);
				throw new WallaException("UtilityDataHelperImpl", "GetNewId", error, 0);
		    }
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetNewId", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetNewId", ex);
			throw new WallaException(ex, 0);
		}
		finally {
	        if (idSproc != null) try { idSproc.close(); } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { conn.close(); } catch (SQLException logOrIgnore) {}
		}
	}

	public int GetInt(String sql) throws WallaException
	{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();

			ps = conn.prepareStatement(sql);

			resultset = ps.executeQuery();
			if (resultset.next())
			{
				return resultset.getInt(1);
			}
			else
			{
				String error = "Select statement didn't return any records, in GetInt.";
				meLogger.error(error);
				throw new WallaException("UtilityDataHelperImpl", "GetInt", error, 0); 
			}
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetInt", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetInt", ex);
			throw new WallaException(ex, 0);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	public long GetLong(String sql) throws WallaException
	{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();

			ps = conn.prepareStatement(sql);

			resultset = ps.executeQuery();
			if (resultset.next())
			{
				return resultset.getLong(1);
			}
			else
			{
				return -1;
			}
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetLong", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetLong", ex);
			throw new WallaException(ex, 0);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	
	public String GetString(String sql) throws WallaException
	{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();

			ps = conn.prepareStatement(sql);

			resultset = ps.executeQuery();
			if (resultset.next())
			{
				return resultset.getString(1);
			}
			else
			{
				String error = "Select statement didn't return any records, in GetInt.";
				meLogger.error(error);
				throw new WallaException("UtilityDataHelperImpl", "GetString", error, 0); 
			}
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetString", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetString", ex);
			throw new WallaException(ex, 0);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	public UtilityDataHelperImpl() {
		meLogger.debug("UtilityDataHelperImpl object instantiated.");
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
}
