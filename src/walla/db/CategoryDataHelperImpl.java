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
import java.util.Calendar;
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
public class CategoryDataHelperImpl implements CategoryDataHelper {

	private DataSource dataSource;
	
	private static final Logger meLogger = Logger.getLogger(CategoryDataHelperImpl.class);

	public CategoryDataHelperImpl() {
		meLogger.debug("CategoryDataHelperImpl object instantiated.");
	}
	
	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}
	
	public void CreateCategory(long userId, Category newCategory, long categoryId) throws WallaException
	{
		String sql = "INSERT INTO [dbo].[Category] ([CategoryId],[ParentId],[Name],[Description],"
					+ "[ImageCount],[LastUpdated],[RecordVersion],[Active],[SystemLocked],[UserId]) "
					+ "VALUES (?,?,?,?,0,dbo.GetDateNoMS(),1,1,?,?)";
		
		Connection conn = null;
		PreparedStatement ps = null;

		try {
			int returnCount = 0;
			
			meLogger.debug("CreateCategory() begins. UserId:" + userId + " CategoryName:" + newCategory.getName());
			
			if (!CheckCategoryExists(userId, newCategory.getParentId()))
			{
				String error = "Parent Category is not valid.  ParentId: " + newCategory.getParentId();
				meLogger.error(error);
				throw new WallaException(this.getClass().getName(), "CreateCategory", error, HttpStatus.BAD_REQUEST.value()); 	
			}
			
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			//Insert main tag record.
			ps = conn.prepareStatement(sql);
			ps.setLong(1, categoryId);
			ps.setLong(2, newCategory.getParentId());
			ps.setString(3, newCategory.getName());
			ps.setString(4, newCategory.getDesc());
			ps.setBoolean(5, newCategory.isSystemOwned());
			ps.setLong(6, userId);
			
			//Execute insert statement.
			returnCount = ps.executeUpdate();
			
			//Validate new record was successful.
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Insert statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException(this.getClass().getName(), "CreateCategory", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 				
			}

			meLogger.debug("CreateCategory() ends. UserId:" + userId + " CategoryName:" + newCategory.getName());
			
			conn.commit();
				
		} catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected SQLException in CreateCategory", sqlEx);
			throw new WallaException(sqlEx,0);
		} catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			
			meLogger.error("Unexpected Exception in CreateCategory", ex);
			throw new WallaException(ex, 0);
		}
		finally {
	        if (ps != null) try { ps.close(); } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { conn.close(); } catch (SQLException logOrIgnore) {}
		}
	}
	
	public void UpdateCategory(long userId, Category existingCategory) throws WallaException
	{
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {			
			int returnCount = 0;
			String updateVersionSql = null;
			
			if (!CheckCategoryExists(userId, existingCategory.getParentId()))
			{
				String error = "Parent Category is not valid.  ParentId: " + existingCategory.getParentId();
				meLogger.error(error);
				throw new WallaException(this.getClass().getName(), "UpdateCategory", error, HttpStatus.BAD_REQUEST.value()); 	
			}
			
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			//Process an update to the main record.
			updateVersionSql = "UPDATE [dbo].[Category] SET [ParentId] = ?,[Name] = ?,[Description] = ?,[LastUpdated] = dbo.GetDateNoMS(),[RecordVersion] = [RecordVersion] + 1 WHERE [CategoryId] = ? AND [RecordVersion] = ?";

			ps = conn.prepareStatement(updateVersionSql);
			ps.setLong(1, existingCategory.getParentId());
			ps.setString(2, existingCategory.getName());
			ps.setString(3, existingCategory.getDesc());
			ps.setLong(4, existingCategory.getId());
			ps.setInt(5, existingCategory.getVersion());
			
			//Execute update and check response.
			returnCount = ps.executeUpdate();
			ps.close();
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Update statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException(this.getClass().getName(), "UpdateCategory", error, HttpStatus.CONFLICT.value()); 
			}

			conn.commit();
		}
		catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected Exception in UpdateCategory", sqlEx);
			throw new WallaException(sqlEx, 0);
		} catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected Exception in UpdateCategory", ex);
			throw new WallaException(ex, 0);
		}
		finally {
	        if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	public void MarkCategoryAsDeleted(long userId, long[] categoryIds, Category existingCategory) throws WallaException
	{
		/*
		 * Category gets marked with Active of 0.
		 * HubProcess then instructed to clear up images, views and tags.
		 */
		
		Connection conn = null;
		Statement ds = null;
		
		try {

			int returnCount[] = null;		
			int updateCount = 1;
			int actualCount = 0;
			
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			String updateActiveSql = "UPDATE [dbo].[Category] SET [Active] = 0, [LastUpdated] = dbo.GetDateNoMS(), [RecordVersion] = [RecordVersion] + 1 WHERE [UserId] = " + userId + " AND [CategoryId] = ";
			ds = conn.createStatement();
			
			for (int i = 0; i < categoryIds.length; i++)
			{
				if (existingCategory.getId() == categoryIds[i])
				{
					//Extra check to ensure timing issue was not encountered.
					ds.addBatch(updateActiveSql + categoryIds[i] + " AND [RecordVersion] = " + existingCategory.getVersion());
				}
				else
				{
					ds.addBatch(updateActiveSql + categoryIds[i]);
				}
				updateCount++;
			}
			
			String updateUserSql = "UPDATE [User] SET [CategoryLastDeleted] = dbo.GetDateNoMS() WHERE [UserId] = " + userId;
			ds.addBatch(updateUserSql);
			
			//Execute statement and ignore counts.
			returnCount = ds.executeBatch();
			ds.close();
			
			for (int i = 0; i < returnCount.length; i++)
			{
				actualCount = actualCount + returnCount[i];
			}

			if (actualCount != updateCount)
			{
				conn.rollback();
				String error = "Update statements didn't return success count which correlates to the updates requested.";
				meLogger.error(error);
				throw new WallaException("CategoryDataHelperImpl", "DeleteCategory", error, HttpStatus.CONFLICT.value()); 
			}
			
			conn.commit();
		}
		catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected SQLException in DeleteCategory", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected Exception in DeleteCategory", ex);
			throw new WallaException(ex, 0);
		}
		finally {
	        if (ds != null) try { if (!ds.isClosed()) {ds.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	public Category GetCategoryMeta(long userId, long categoryId) throws WallaException
	{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		Category category = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT [CategoryId],[ParentId],[Name],[Description],[LastUpdated],[RecordVersion],[SystemOwned] FROM [dbo].[Category] WHERE [UserId] = ? AND [CategoryId]= ?";
			
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, userId);
			ps.setLong(2,categoryId);

			resultset = ps.executeQuery();
			if (!resultset.next())
			{
				return null;
			}
			
			category = new Category();
			category.setId(resultset.getLong(1));
			category.setParentId(resultset.getLong(2));
			category.setName(resultset.getString(3));
			category.setDesc(resultset.getString(4));
			category.setSystemOwned(resultset.getBoolean(5));

			GregorianCalendar oldGreg = new GregorianCalendar();
			oldGreg.setTime(resultset.getTimestamp(5));
			XMLGregorianCalendar xmlOldGreg = DatatypeFactory.newInstance().newXMLGregorianCalendar(oldGreg);
			
			category.setLastChanged(xmlOldGreg);
			category.setVersion(resultset.getInt(6));
			
			return category;
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetCategoryMeta", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetCategoryMeta", ex);
			throw new WallaException(ex, 0);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	public ImageList GetCategoryImageListMeta(long userId, long categoryId) throws WallaException
	{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		ImageList categoryImageList = null;
		
		try {
			conn = dataSource.getConnection();

			String selectSql = "SELECT [CategoryId],[Name],[Description],[ImageCount],[LastUpdated],[RecordVersion],[SystemOwned] FROM [dbo].[Category] WHERE [UserId] = ? AND [CategoryId]= ?";
			ps = conn.prepareStatement(selectSql);

			ps.setLong(1, userId);
			ps.setLong(2,categoryId);

			resultset = ps.executeQuery();

			if (!resultset.next())
			{
				resultset.close();
				String error = "Select statement didn't return any records.";
				meLogger.error(error);
				throw new WallaException("CategoryDataHelperImpl", "GetCategoryImageListMeta", error, 0); 
			}
			
			categoryImageList = new ImageList();
			categoryImageList.setId(resultset.getLong(1));
			categoryImageList.setName(resultset.getString(2));
			categoryImageList.setDesc(resultset.getString(3));
			categoryImageList.setTotalImageCount(resultset.getInt(4));
			
			GregorianCalendar oldGreg = new GregorianCalendar();
			oldGreg.setTime(resultset.getTimestamp(5));
			XMLGregorianCalendar xmlOldGreg = DatatypeFactory.newInstance().newXMLGregorianCalendar(oldGreg);
			
			categoryImageList.setLastChanged(xmlOldGreg);
			categoryImageList.setVersion(resultset.getInt(6));
			categoryImageList.setSystemOwned(resultset.getBoolean(7));
			
			resultset.close();
			return categoryImageList;
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetCategoryImageListMeta", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetCategoryImageListMeta", ex);
			throw new WallaException(ex, 0);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	public Date LastCategoryListUpdate(long userId) throws WallaException
	{
		Connection conn = null;
		Statement sQuery = null;
		ResultSet resultset = null;
		java.util.Date utilDate = null;
		
		try {			
			conn = dataSource.getConnection();
			
			String selectSql = "SELECT MAX(UpdateDate) FROM (SELECT C.[LastUpdated] AS [UpdateDate] FROM [Category] "
					+ "C WHERE C.[Active] = 1 AND C.[UserId] = " + userId + " UNION SELECT [CategoryLastDeleted] "
					+ "AS [UpdateDate] FROM [User] U WHERE U.[UserId] = " + userId + ") CategoryDates";

			sQuery = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			resultset = sQuery.executeQuery(selectSql);
			if (resultset.next())
			{
				utilDate = new java.util.Date(resultset.getTimestamp(1).getTime());
			}
			
			resultset.close();
		
			return utilDate;
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in LastCategoryListUpdate", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in LastCategoryListUpdate", ex);
			throw new WallaException(ex, 0);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (sQuery != null) try { if (!sQuery.isClosed()) {sQuery.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}

	//TODO Add search params
	public int GetTotalImageCount(long userId, long categoryId) throws WallaException
	{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT COUNT(*) "
								+ "FROM [Image]"
								+ "WHERE [UserId] = ? AND [CategoryId] = ? AND Status = 3";
			
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, userId);
			ps.setLong(2, categoryId);
			
			resultset = ps.executeQuery();
			if (resultset.next())
			{
				return resultset.getInt(1);
			}
			else
			{
				String error = "Select statement didn't return any records, in GetTotalImageCount.";
				meLogger.error(error);
				throw new WallaException("CategoryDataHelperImpl", "GetTotalImageCount", error, 0); 
			}
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetTotalImageCount", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetTotalImageCount", ex);
			throw new WallaException(ex, 0);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	// TODO Add search facility
	public void GetCategoryImages(long userId, int imageCursor, int imageCount, ImageList categoryImageList) throws WallaException
	{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		GregorianCalendar oldGreg = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT [Rank],[ImageId],[Name],[Description],[UploadDate],[TakenDateMeta],"
					+ " [RecordVersion], [CategoryId], [ISO], [Aperture], [ShutterSpeed], [Size] "
					+ " FROM(   SELECT RANK() OVER (ORDER BY i.[Name], i.[ImageId]) as [Rank], i.[ImageId],i.[Name],i.[Description], "
					+ " im.[UploadDate],COALESCE(im.[TakenDateMeta], im.[TakenDateFile]) AS TakenDateMeta,i.[RecordVersion], i.[CategoryId],"
					+ " im.[Size], im.[Aperture],im.[ShutterSpeed],im.[ISO]"
					+ " FROM Image i INNER JOIN ImageMeta im ON i.ImageId = im.ImageId"
					+ " WHERE i.[CategoryId] = ? AND i.Status = 3 ) AS RR"
					+ " WHERE RR.[Rank] > ? AND RR.[Rank] <= ? ORDER BY [Name]";
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, categoryImageList.getId());
			ps.setInt(2, imageCursor);
			ps.setInt(3, imageCursor + imageCount);
			
			resultset = ps.executeQuery();
			oldGreg = new GregorianCalendar();
			categoryImageList.setImages(new ImageList.Images());

			while (resultset.next())
			{
				ImageList.Images.ImageRef newImageRef = new ImageList.Images.ImageRef(); 
				newImageRef.setId(resultset.getLong(2));
				newImageRef.setName(resultset.getString(3));
				newImageRef.setDesc(resultset.getString(4));
				
				oldGreg.setTime(resultset.getTimestamp(5));
				XMLGregorianCalendar xmlOldGregUpload = DatatypeFactory.newInstance().newXMLGregorianCalendar(oldGreg);
				newImageRef.setUploadDate(xmlOldGregUpload);
				
				oldGreg.setTime(resultset.getTimestamp(6));
				XMLGregorianCalendar xmlOldGregTaken = DatatypeFactory.newInstance().newXMLGregorianCalendar(oldGreg);
				newImageRef.setTakenDate(xmlOldGregTaken);
				
				newImageRef.setMetaVersion(resultset.getInt(7));
				newImageRef.setCategoryId(resultset.getLong(8));

		        SimpleDateFormat monthDayYearformatter = new SimpleDateFormat("dd MMM yyyy");
		        monthDayYearformatter.format((java.util.Date) resultset.getTimestamp(6));
		        
				String shotSummary = ((resultset.getInt(9) == 0) ? "" : "ISO" + resultset.getInt(9) + " ");
				shotSummary = shotSummary + ((resultset.getString(10) == null) ? "" : resultset.getString(10) + " ");
				shotSummary = shotSummary + ((resultset.getString(11) == null) ? "" : resultset.getString(11));
				newImageRef.setShotSummary(shotSummary);
				
				String fileSummary = UserTools.ConvertBytesToMB(resultset.getLong(12)) + " - ";
				fileSummary = fileSummary + (monthDayYearformatter.format((java.util.Date) resultset.getTimestamp(6)));
				newImageRef.setFileSummary(fileSummary);
				
				categoryImageList.getImages().getImageRef().add(newImageRef);
			}
			resultset.close();
			
			if (categoryImageList.getImages().getImageRef().size() == 0)
			{
				meLogger.info("Select statement didn't return any records.");
				//throw new WallaException("CategoryDataHelperImpl", "GetCategoryImages", error, 0); 
			}
			
			categoryImageList.setImageCount(categoryImageList.getImages().getImageRef().size());
			categoryImageList.setImageCursor(imageCursor);
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetCategoryImages", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetCategoryImages", ex);
			throw new WallaException(ex, 0);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	public CategoryList GetUserCategoryList(long userId) throws WallaException
	{
		Connection conn = null;
		Statement sQuery = null;
		ResultSet resultset = null;
		CategoryList categoryList = null;
		
		try {			
			conn = dataSource.getConnection();
			
			//TODO add logic so categories with inactive parents are excluded.			
			String selectSql = "SELECT C.[CategoryId],C.[ParentId],C.[Name],C.[Description],C.[ImageCount],C.[SystemOwned] "
					+ "FROM [dbo].[Category] C WHERE [Active] = 1 AND [UserId] = " + userId;
			sQuery = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			resultset = sQuery.executeQuery(selectSql);

			while (resultset.next())
			{
				if (categoryList == null)
				{
					categoryList = new CategoryList();
				}
				
				CategoryList.CategoryRef newCategoryRef = new CategoryList.CategoryRef();
				newCategoryRef.setId(resultset.getLong(1));
				newCategoryRef.setParentId(resultset.getLong(2));
				newCategoryRef.setName(resultset.getString(3));
				newCategoryRef.setDesc(resultset.getString(4));
				newCategoryRef.setCount(resultset.getInt(5));	
				newCategoryRef.setSystemOwned(resultset.getBoolean(6));
				
				categoryList.getCategoryRef().add(newCategoryRef);
			}
			
			resultset.close();
			return categoryList;
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetUserCategoryList", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetUserCategoryList", ex);
			throw new WallaException(ex, 0);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (sQuery != null) try { if (!sQuery.isClosed()) {sQuery.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}

	private boolean CheckCategoryExists(long userId, long categoryId) throws WallaException
	{
		Connection conn = null;
		Statement sQuery = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();
			
			String selectSql = "SELECT 1 FROM Category WHERE [CategoryId] = " + categoryId + " AND [UserId] = " + userId + " AND [Active] = 1";
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
			meLogger.error("Unexpected SQLException in CheckCategoryExists", sqlEx);
			throw new WallaException(sqlEx,0);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (sQuery != null) try { if (!sQuery.isClosed()) {sQuery.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	public long[] GetGalleryReferencingCategory(long userId, long[] categoryIds) throws WallaException
	{
		Connection conn = null;
		Statement statement = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();

			if (categoryIds.length == 0)
				return new long[0];
			
			//Includes deactivated categories.
			String selectSql  = "SELECT DISTINCT [GalleryId] FROM GallerysLinkedToCategories WHERE UserId="
					+ userId + " AND CategoryId IN (";
			
			for (int i = 0; i < categoryIds.length; i++)
			{
				selectSql = selectSql + ((i == 0) ? categoryIds[i] : "," + categoryIds[i]);
			}
			selectSql = selectSql + ")";
			
			statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			
			resultset = statement.executeQuery(selectSql);

			int size = 0;
			try {
				resultset.last();
			    size = resultset.getRow();
			    resultset.beforeFirst();
			}
			catch(Exception ex) {}

			long[] returnGalleryIds = new long[size];
			for (int i = 0; i < size; i++)
			{
				resultset.next();
				returnGalleryIds[i] = resultset.getLong(1);
			}
			
			return returnGalleryIds;
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetGalleryReferencingCategory", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetGalleryReferencingCategory", ex);
			throw new WallaException(ex, 0);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (statement != null) try { if (!statement.isClosed()) {statement.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	public void UpdateCategoryTimeAndCount(long userId, long[] categoryIds) throws WallaException
	{
		Connection conn = null;
		Statement ds = null;
		
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			//Process an update to the main record.
			String updateVersionSql = "UPDATE [dbo].[Category] SET [LastUpdated] = dbo.GetDateNoMS(), "
					+ "[ImageCount] = (SELECT COUNT(1) FROM Image I WHERE I.[CategoryId] = [Category].[CategoryId] AND I.[STATUS] = 3) "
					+ "WHERE [UserId] = " + userId + " AND [CategoryId] = ";
			ds = conn.createStatement();
			
			for (int i = 0; i < categoryIds.length; i++)
			{
				ds.addBatch(updateVersionSql + categoryIds[i]);
			}
			
			//Execute statement and ignore counts.
			ds.executeBatch();
			ds.close();
			conn.commit();
		}
		catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected SQLException in UpdateCategoryTimestamp", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected Exception in UpdateCategoryTimestamp", ex);
			throw new WallaException(ex, 0);
		}
		finally {
			if (ds != null) try { if (!ds.isClosed()) {ds.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	public long[] GetCategoryHierachy(long userId, long categoryId, boolean up) throws WallaException
	{
		Connection conn = null;
		Statement statement = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();

			String executeSql = null;
			if (up)
			{
				executeSql = "SELECT CategoryId FROM [dbo].[CategoryListUp](" + userId + "," + categoryId + ")";
			}
			else
			{
				executeSql = "SELECT CategoryId FROM [dbo].[CategoryListDown](" + userId + "," + categoryId + ")";
			}
			statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			
			resultset = statement.executeQuery(executeSql);

			int size = 0;
			try {
				resultset.last();
			    size = resultset.getRow();
			    resultset.beforeFirst();
			}
			catch(Exception ex) {}

			if (size == 0)
			{
				String error = "Function GetCategoryHierachy didn't return any records.";
				meLogger.error(error);
				throw new WallaException("CategoryDataHelperImpl", "GetCategoryHierachy", error, 0); 
			}
			
			long[] returnCategoryId = new long[size];
			for (int i = 0; i < size; i++)
			{
				resultset.next();
				returnCategoryId[i] = resultset.getLong(1);
			}
			
			return returnCategoryId;
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetCategoryHierachy", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetCategoryHierachy", ex);
			throw new WallaException(ex, 0);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (statement != null) try { if (!statement.isClosed()) {statement.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}	

	public long[] GetCategoryIdFromImageMoveList(long userId, ImageMoveList moveList) throws WallaException
	{
		Connection conn = null;
		Statement statement = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT DISTINCT [CategoryId] FROM [Image] WHERE [UserId] = " + userId + " AND [ImageId] IN (";

			//Add new images in the tag if there are any
			if (moveList.getImageRef() != null)
			{
				if (moveList.getImageRef().size() > 0)
				{
					for (int i = 0; i < moveList.getImageRef().size(); i++)
					{
						long imageId = moveList.getImageRef().get(i);
						selectSql = selectSql + imageId + (moveList.getImageRef().size() == i+1 ? ")" : ",");
					}

					statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					
					resultset = statement.executeQuery(selectSql);

					int size = 0;
					try {
						resultset.last();
					    size = resultset.getRow();
					    resultset.beforeFirst();
					}
					catch(Exception ex) {}

					if (size == 0)
					{
						String error = "Select didn't return any records.";
						meLogger.error(error);
						throw new WallaException("CategoryDataHelperImpl", "GetCategoryIdFromImageMoveList", error, 0); 
					}
					
					long[] returnCategoryId = new long[size];
					for (int i = 0; i < size; i++)
					{
						resultset.next();
						returnCategoryId[i] = resultset.getLong(1);
					}
					
					return returnCategoryId;
				}
			}

			return null;
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetCategoryIdFromImageMoveList", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetCategoryIdFromImageMoveList", ex);
			throw new WallaException(ex, 0);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (statement != null) try { if (!statement.isClosed()) {statement.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	public void MoveImagesToNewCategory(long userId, long categoryId, ImageMoveList moveList) throws WallaException
	{
		Connection conn = null;
		Statement statement = null;
		
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			//Process an update to the main record.
			String updateSql = "UPDATE [dbo].[Image] SET [LastUpdated] = dbo.GetDateNoMS(), [RecordVersion] = [RecordVersion] + 1,"
					+ " [CategoryId] = " + categoryId + " WHERE [UserId] = " + userId + " AND [ImageId] IN (";

			if (moveList.getImageRef() != null)
			{
				if (moveList.getImageRef().size() > 0)
				{
					for (int i = 0; i < moveList.getImageRef().size(); i++)
					{
						long imageId = moveList.getImageRef().get(i);
						updateSql = updateSql + imageId + (moveList.getImageRef().size() == i+1 ? ")" : ",");
					}

					statement = conn.createStatement();
					
					int rowCount = statement.executeUpdate(updateSql);
					statement.close();
					
					if (rowCount != moveList.getImageRef().size())
					{
						conn.rollback();
						String error = "Updating images with a new category returned a different result to the expectation, no changes made.";
						meLogger.error(error);
						throw new WallaException("CategoryDataHelperImpl", "MoveImagesToNewCategory", error, 0); 
					}
					conn.commit();
				}
			}
		}
		catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected SQLException in MoveImagesToNewCategory", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected Exception in MoveImagesToNewCategory", ex);
			throw new WallaException(ex, 0);
		}
		finally {
			if (statement != null) try { if (!statement.isClosed()) {statement.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	
}
