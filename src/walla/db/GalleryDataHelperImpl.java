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
public class GalleryDataHelperImpl implements GalleryDataHelper {

	private DataSource dataSource;
	
	private static final Logger meLogger = Logger.getLogger(GalleryDataHelperImpl.class);

	public GalleryDataHelperImpl() {
		meLogger.debug("GalleryDataHelperImpl object instantiated.");
	}
	
	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}
	
	public void CreateGallery(long userId, Gallery newGallery, long newGalleryId, String urlComplex) throws WallaException
	{
		String sql = "INSERT INTO [dbo].[Gallery] ([GalleryId],[Name],[Description],[UrlComplex],"
				+ "[AccessType],[Password],[SelectionType],[GroupingType],[StyleId],[PresentationId],"
				+ "[TotalImageCount],[LastUpdated],[RecordVersion],[UserId])"
				+ "VALUES (?,?,?,?,?,?,?,-1,GetDate(),?,?)";
		
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			int returnCount = 0;
			
			meLogger.debug("CreateGallery() begins. UserId:" + userId + " GalleryName:" + newGallery.getName());
			
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			//Insert main gallery record.
			ps = conn.prepareStatement(sql);
			ps.setLong(1, newGalleryId);
			ps.setString(2, newGallery.getName());
			ps.setString(3, newGallery.getDesc());
			ps.setString(4, urlComplex);
			ps.setInt(5, newGallery.getAccessType());
			ps.setString(6, newGallery.getPassword());
			ps.setInt(7, newGallery.getSelectionType());
			ps.setInt(8, newGallery.getGroupingType());
			ps.setInt(9, newGallery.getStyleId());
			ps.setInt(10, newGallery.getPresentationId());
			ps.setInt(8, 1);
			ps.setLong(9, userId);
			
			//Execute insert statement.
			returnCount = ps.executeUpdate();
			
			//Validate new record was successful.
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Insert statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException(this.getClass().getName(), "CreateGallery", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 				
			}
			
			UpdateGallerySubElements(conn, newGallery, newGalleryId);
			
			meLogger.debug("CreateGallery() ends. UserId:" + userId + " GalleryName:" + newGallery.getName());
			
			conn.commit();
			
			RegenerateGalleryImages(userId, newGalleryId);
				
		} catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected SQLException in CreateGallery", sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
		} catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected Exception in CreateGallery", ex);
			throw new WallaException(ex, HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally {
	        if (ps != null) try { ps.close(); } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { conn.close(); } catch (SQLException logOrIgnore) {}
		}
	}
	
	private void UpdateGallerySubElements(Connection conn, Gallery gallery, long galleryId) throws WallaException, SQLException
	{
		PreparedStatement is = null;
		try
		{			
			int[] responseCounts;
			int returnCount = 0;
			int controlCount = 0;
			
			if (gallery.getUsers() != null)
			{
				if (gallery.getUsers().getUserRef().size() > 0)
				{
					String insertSql = "INSERT INTO [dbo].[GalleryUser] ([GalleryId],[EmailAddress]) VALUES (?,?)";
					is = conn.prepareStatement(insertSql);			   
					
					//Construct update SQL statements
					for (Iterator<Gallery.Users.UserRef> imageIterater = gallery.getUsers().getUserRef().iterator(); imageIterater.hasNext();)
					{
						Gallery.Users.UserRef currentUserRef = (Gallery.Users.UserRef)imageIterater.next();
						
						is.setLong(1, galleryId); 
						is.setString(2, currentUserRef.getEmailAddress());

						is.addBatch();
						controlCount++;
					}
					
					//Perform updates.
					responseCounts = is.executeBatch();
					for (int i = 0; i < responseCounts.length; i++)
					{
						returnCount = returnCount + responseCounts[i];
					}
					
					is.close();
				}
			}
			
			if (gallery.getCategories() != null)
			{
				if (gallery.getCategories().getCategoryRef().size() > 0)
				{
					String insertSql = "INSERT INTO [dbo].[GalleryCategory] ([GalleryId],[CategoryId],[Recursive]) VALUES (?,?,?)";
					is = conn.prepareStatement(insertSql);			   

					for (Iterator<Gallery.Categories.CategoryRef> imageIterater = gallery.getCategories().getCategoryRef().iterator(); imageIterater.hasNext();)
					{
						Gallery.Categories.CategoryRef currentCategoryRef = (Gallery.Categories.CategoryRef)imageIterater.next();
						
						is.setLong(1, galleryId); 
						is.setLong(2, currentCategoryRef.getCategoryId());
						is.setBoolean(3, currentCategoryRef.isRecursive());

						is.addBatch();
						controlCount++;
					}
					
					//Perform updates.
					responseCounts = is.executeBatch();
					for (int i = 0; i < responseCounts.length; i++)
					{
						returnCount = returnCount + responseCounts[i];
					}
					
					is.close();
				}
			}
			
			if (gallery.getSorts() != null)
			{
				if (gallery.getSorts().getSortRef().size() > 0)
				{
					String insertSql = "INSERT INTO [dbo].[GallerySort] ([GalleryId],[FieldName],[Ascending]) VALUES (?,?,?)";
					is = conn.prepareStatement(insertSql);			   

					for (Iterator<Gallery.Sorts.SortRef> imageIterater = gallery.getSorts().getSortRef().iterator(); imageIterater.hasNext();)
					{
						Gallery.Sorts.SortRef currentSortRef = (Gallery.Sorts.SortRef)imageIterater.next();
						
						is.setLong(1, galleryId); 
						is.setString(2, currentSortRef.getFieldname());
						is.setBoolean(3, currentSortRef.isAscending());

						is.addBatch();
						controlCount++;
					}
					
					//Perform updates.
					responseCounts = is.executeBatch();
					for (int i = 0; i < responseCounts.length; i++)
					{
						returnCount = returnCount + responseCounts[i];
					}
					
					is.close();
				}
			}
			
			if (gallery.getTags() != null)
			{
				if (gallery.getTags().getTagRef().size() > 0)
				{
					String insertSql = "INSERT INTO [dbo].[GalleryTag] ([GalleryId],[TagId],[Exclude]) VALUES (?,?,?)";
					is = conn.prepareStatement(insertSql);			   
					
					//Construct update SQL statements
					for (Iterator<Gallery.Tags.TagRef> imageIterater = gallery.getTags().getTagRef().iterator(); imageIterater.hasNext();)
					{
						Gallery.Tags.TagRef currentTagRef = (Gallery.Tags.TagRef)imageIterater.next();
						
						is.setLong(1, galleryId); 
						is.setLong(2, currentTagRef.getTagId());
						is.setBoolean(3, currentTagRef.isExclude());

						is.addBatch();
						controlCount++;
					}
					
					//Perform updates.
					responseCounts = is.executeBatch();
					for (int i = 0; i < responseCounts.length; i++)
					{
						returnCount = returnCount + responseCounts[i];
					}
					
					is.close();
				}
			}
			
			//Check if any updates have been processed.
			if (controlCount > 0)
			{
				//Check for unexpected row update count in the database
				if (returnCount != controlCount)
				{
					conn.rollback();
					String error = "Row count updates didn't total the number gallery inserts requested for sub elements.";
					meLogger.error(error);
					throw new WallaException("GalleryDataHelperImpl", "UpdateGallerySubElements", error, HttpStatus.CONFLICT.value()); 
				}
			}
		}
		finally
		{
			if (is != null) 
			try { if (is.isClosed() == false) {is.close();} } 
			catch (SQLException logOrIgnore) {}
		}
	}
	
	public void UpdateGallery(long userId, Gallery existingGallery) throws WallaException
	{
		Connection conn = null;
		PreparedStatement ps = null;
		Statement ds = null;
		int returnCount = 0;
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			//Process an update to the main record.
			String updateVersionSql = "UPDATE [dbo].[Gallery] SET [Name] = ?, [Description] = ?, [AccessType] = ?, [Password] = ?,"
					+ "[SelectionType] = ?, [GroupingType] = ?,[StyleId] = ?, [TotalImageCount] = -1,[PresentationId] = ?, [LastUpdated] = GetDate(),"
					+ "[RecordVersion] = [RecordVersion] + 1 WHERE [UserId] = ? AND [GalleryId] = ? AND [RecordVersion] = ?";

			ps = conn.prepareStatement(updateVersionSql);
			ps.setString(1, existingGallery.getName());
			ps.setString(2, existingGallery.getDesc());
			ps.setInt(3, existingGallery.getAccessType());
			ps.setString(4, existingGallery.getPassword());
			ps.setInt(5, existingGallery.getSelectionType());
			ps.setInt(6, existingGallery.getGroupingType());
			ps.setInt(7, existingGallery.getStyleId());
			ps.setInt(8, existingGallery.getPresentationId());
			
			ps.setLong(9, userId);
			ps.setLong(10, existingGallery.getId());
			ps.setLong(11, existingGallery.getVersion());
			
			//Execute update and check response.
			returnCount = ps.executeUpdate();
			ps.close();
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Update statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException("GalleryDataHelperImpl", "UpdateGallery", error, HttpStatus.CONFLICT.value()); 
			}

			
			DeleteGallerySubElements(conn, existingGallery.getId());
			/*
			ds = conn.createStatement();
			ds.addBatch("DELETE FROM [dbo].[GalleryUser] WHERE [GalleryId] = " + existingGallery.getId());
			ds.addBatch("DELETE FROM [dbo].[GalleryCategory] WHERE [GalleryId] = " + existingGallery.getId());
			ds.addBatch("DELETE FROM [dbo].[GallerySort] WHERE [GalleryId] = " + existingGallery.getId());
			ds.addBatch("DELETE FROM [dbo].[GalleryTag] WHERE [GalleryId] = " + existingGallery.getId());
			
			//Execute statement and ignore counts.
			ds.executeBatch();
			ds.close();
			*/
			
			UpdateGallerySubElements(conn, existingGallery, existingGallery.getId());
			
			conn.commit();
			
			RegenerateGalleryImages(userId, existingGallery.getId());
		}
		catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected Exception in UpdateGallery", sqlEx);
			throw new WallaException(sqlEx, 0);
		} catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected Exception in UpdateGallery", ex);
			throw new WallaException(ex, 0);
		}
		finally {
	        if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (ds != null) try { if (!ds.isClosed()) {ds.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	private void DeleteGallerySubElements(Connection conn, long galleryId) throws SQLException
	{
		Statement ds = null;
		
		try
		{
			ds = conn.createStatement();
			ds.addBatch("DELETE FROM [dbo].[GalleryUser] WHERE [GalleryId] = " + galleryId);
			ds.addBatch("DELETE FROM [dbo].[GalleryCategory] WHERE [GalleryId] = " + galleryId);
			ds.addBatch("DELETE FROM [dbo].[GallerySort] WHERE [GalleryId] = " + galleryId);
			ds.addBatch("DELETE FROM [dbo].[GalleryTag] WHERE [GalleryId] = " + galleryId);
			
			//Execute statement and ignore counts.
			ds.executeBatch();
			ds.close();
		}
		finally
		{
			if (ds != null) 
			try { if (ds.isClosed() == false) {ds.close();} } 
			catch (SQLException logOrIgnore) {}
		}
	}
	
	public void DeleteGallery(long userId, long galleryId, int version, String galleryName) throws WallaException
	{
		Connection conn = null;
		PreparedStatement ps = null;
		Statement us = null;
		
		try {
			int returnCount = 0;
			
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			String deleteSql = "DELETE FROM [Gallery] WHERE [GalleryId]= ? AND [RecordVersion] = ? AND [UserId] = ? AND [Name] = ?"; 
			ps = conn.prepareStatement(deleteSql);
			ps.setLong(1, galleryId);
			ps.setInt(2, version);
			ps.setLong(3, userId);
			ps.setString(4, galleryName);

			//Execute update and check response.
			returnCount = ps.executeUpdate();
			ps.close();
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Delete statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException("GalleryDataHelperImpl", "DeleteGallery", error, HttpStatus.CONFLICT.value()); 
			}

			DeleteGallerySubElements(conn, galleryId);
			
			//TODO Delete GalleryImages.
			
			String updateSql = "UPDATE [User] SET [GalleryLastDeleted] = GetDate() WHERE [UserId] = " + userId;
			us = conn.createStatement();
			returnCount = us.executeUpdate(updateSql);
			us.close();
			
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Update timestamp statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException("GalleryDataHelperImpl", "DeleteGallery", error, HttpStatus.CONFLICT.value()); 
			}
			
			conn.commit();
		}
		catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected SQLException in DeleteGallery", sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
		} 
		catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected Exception in DeleteGallery", ex);
			throw new WallaException(ex, 0);
		}
		finally {
	        if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (us != null) try { if (!us.isClosed()) {us.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	public Date LastGalleryListUpdate(long userId) throws WallaException
	{
		Connection conn = null;
		Statement sQuery = null;
		ResultSet resultset = null;
		java.util.Date utilDate = null;
		
		try {			
			conn = dataSource.getConnection();
			
			String selectSql = "SELECT MAX(UpdateDate) FROM (SELECT G.[LastUpdated] AS [UpdateDate] FROM "
					+ "[Gallery] G WHERE G.[UserId] = " + userId + " UNION SELECT [GalleryLastDeleted] AS [UpdateDate] "
					+ "FROM [User] U WHERE U.[UserId] = " + userId + ") GalleryDates";
			
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
			meLogger.error("Unexpected SQLException in LastGalleryListUpdate", sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
		} 
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in LastGalleryListUpdate", ex);
			throw new WallaException(ex, HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (sQuery != null) try { if (!sQuery.isClosed()) {sQuery.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}

	public Gallery GetGalleryMeta(long userId, String galleryName) throws WallaException
	{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		Gallery gallery = null;
		
		try {			
			conn = dataSource.getConnection();
			
			String selectSql = "SELECT [GalleryId],[Name],[Description],[UrlComplex],[AccessType],"
				+ "[Password],[SelectionType],[GroupingType],[StyleId],[PresentationId],[TotalImageCount],"
				+ "[LastUpdated],[RecordVersion] FROM [dbo].[Gallery]"
				+ " WHERE [UserId] = ? AND [Name]= ?";
			
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, userId);
			ps.setString(2,galleryName);

			resultset = ps.executeQuery();

			if (!resultset.next())
			{
				return null;
			}
			
			gallery = new Gallery();
			gallery.setId(resultset.getLong(1));
			gallery.setName(resultset.getString(2));
			gallery.setDesc(resultset.getString(3));
			gallery.setUrlComplex(resultset.getString(4));
			gallery.setAccessType(resultset.getInt(5));
			gallery.setPassword(resultset.getString(6));
			gallery.setSelectionType(resultset.getInt(7));
			gallery.setGroupingType(resultset.getInt(8));
			gallery.setStyleId(resultset.getInt(9));
			gallery.setPresentationId(resultset.getInt(10));
			gallery.setTotalImageCount(resultset.getInt(11));
			GregorianCalendar oldGreg = new GregorianCalendar();
			oldGreg.setTime(resultset.getTimestamp(12));
			XMLGregorianCalendar xmlOldGreg = DatatypeFactory.newInstance().newXMLGregorianCalendar(oldGreg);
			gallery.setLastChanged(xmlOldGreg);
			gallery.setVersion(resultset.getInt(13));
			
			GetGallerySubElements(userId, conn, gallery);
			
			return gallery;
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetGalleryMeta", sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
		} 
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetGalleryMeta", ex);
			throw new WallaException(ex, HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	private void GetGallerySubElements(long userId, Connection conn, Gallery gallery) throws WallaException, SQLException
	{
		PreparedStatement ps = null;
		ResultSet resultset = null;
		
		try
		{			
			//Users
			String selectSql = "SELECT [EmailAddress] FROM [dbo].[GalleryUser] WHERE [GalleryId]= ?";
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, gallery.getId());
			resultset = ps.executeQuery();

			gallery.setUsers(new Gallery.Users());
			while (resultset.next())
			{
				Gallery.Users.UserRef user = new Gallery.Users.UserRef();
				user.setEmailAddress(resultset.getString(1));
				gallery.getUsers().getUserRef().add(user);
			}
			resultset.close();
			ps.close();
			
			//Category
			selectSql = "SELECT [CategoryId],[Recursive] FROM [dbo].[GalleryCategory] WHERE [GalleryId]= ?";
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, gallery.getId());
			resultset = ps.executeQuery();

			gallery.setCategories(new Gallery.Categories());
			while (resultset.next())
			{
				Gallery.Categories.CategoryRef category = new Gallery.Categories.CategoryRef();
				category.setCategoryId(resultset.getLong(1));
				category.setRecursive(resultset.getBoolean(2));
				gallery.getCategories().getCategoryRef().add(category);
			}
			resultset.close();
			ps.close();
			
			//Sorts
			selectSql = "SELECT [FieldName],[Ascending] FROM [dbo].[GallerySort] WHERE [GalleryId]= ?";
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, gallery.getId());
			resultset = ps.executeQuery();

			gallery.setSorts(new Gallery.Sorts());
			while (resultset.next())
			{
				Gallery.Sorts.SortRef sort = new Gallery.Sorts.SortRef();
				sort.setFieldname(resultset.getString(1));
				sort.setAscending(resultset.getBoolean(2));
				gallery.getSorts().getSortRef().add(sort);
			}
			resultset.close();
			ps.close();
			
			//Tags
			selectSql = "SELECT [TagId],[Exclude] FROM [dbo].[GalleryTag] WHERE [GalleryId]= ?";
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, gallery.getId());
			resultset = ps.executeQuery();

			gallery.setTags(new Gallery.Tags());
			while (resultset.next())
			{
				Gallery.Tags.TagRef tag = new Gallery.Tags.TagRef();
				tag.setTagId(resultset.getLong(1));
				tag.setExclude(resultset.getBoolean(2));
				gallery.getTags().getTagRef().add(tag);
			}
			resultset.close();
			ps.close();
			
			//Tags
			if (gallery.getGroupingType() > 0)
			{
				if (gallery.getGroupingType() == 1)
				{
					selectSql = "SELECT GS.[SectionId],GS.[ImageCount],C.[Name],C.[Description] FROM [dbo].[GallerySection] GS "
							+ "INNER JOIN [Category] C ON GS.[SectionId] = C.[CategoryId] "
							+ "WHERE GS.[GalleryId]= ?";
				}
				else
				{
					selectSql = "SELECT GS.[SectionId],GS.[ImageCount],T.[Name],T.[Description] FROM [dbo].[GallerySection] GS "
							+ "INNER JOIN [Tag] T ON GS.[SectionId] = T.[TagId] "
							+ "WHERE GS.[GalleryId]= ?";
				}
				
				ps = conn.prepareStatement(selectSql);
				ps.setLong(1, gallery.getId());
				resultset = ps.executeQuery();
	
				gallery.setSections(new Gallery.Sections());
				while (resultset.next())
				{
					Gallery.Sections.SectionRef section = new Gallery.Sections.SectionRef();
					section.setSectionId(resultset.getLong(1));
					section.setImageCount(resultset.getInt(2));
					section.setName(resultset.getString(3));
					section.setDesc(resultset.getString(4));
					gallery.getSections().getSectionRef().add(section);
				}
				resultset.close();
				ps.close();
			}
		}
		finally
		{
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
		}
	}

	public GalleryList GetUserGalleryList(long userId) throws WallaException
	{
		Connection conn = null;
		Statement sQuery = null;
		ResultSet resultset = null;
		GalleryList galleryList = null;
		
		try {			
			conn = dataSource.getConnection();
			
			String selectSql = "SELECT G.[GalleryId], G.[Name], G.[Description], G.[UrlComplex], G.[TotalImageCount] FROM "
					+ "Gallery G WHERE G.[UserId] = " + userId;
			sQuery = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			resultset = sQuery.executeQuery(selectSql);

			while (resultset.next())
			{
				if (galleryList == null)
				{
					galleryList = new GalleryList();
				}
				
				GalleryList.GalleryRef newGalleryRef = new GalleryList.GalleryRef(); 
				newGalleryRef.setId(resultset.getLong(1));
				newGalleryRef.setName(resultset.getString(2));
				newGalleryRef.setDesc(resultset.getString(3));
				newGalleryRef.setUrlComplex(resultset.getString(4));
				newGalleryRef.setCount(resultset.getInt(5));
				galleryList.getGalleryRef().add(newGalleryRef);
			}
			
			resultset.close();
			return galleryList;
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetUserGalleryList", sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
		} 
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetUserGalleryList", ex);
			throw new WallaException(ex, HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (sQuery != null) try { if (!sQuery.isClosed()) {sQuery.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	/*
	//TODO Add search params + change image status
	public int GetTotalImageCount(long galleryId) throws WallaException
	{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT COUNT(*) "
								+ "FROM GalleryImage gi INNER JOIN Image i ON gi.ImageId = i.ImageId "
								+ "WHERE t=gi.[GalleryId] = ? AND i.Status = 1";
			
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, galleryId);
			
			resultset = ps.executeQuery();
			if (resultset.next())
			{
				return resultset.getInt(1);
			}
			else
			{
				String error = "Select statement didn't return any records, in GetTotalImageCount.";
				meLogger.error(error);
				throw new WallaException("GalleryDataHelperImpl", "GetTotalImageCount", error, 0); 
			}
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetTotalImageCount", sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
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
	*/
	
	// TODO Add search,sort facility
	public void GetGalleryImages(long userId, long machineId, int imageCursor, int imageCount, ImageList galleryImageList) throws WallaException
	{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		GregorianCalendar oldGreg = null;
		String selectSql = null;
		
		try {			
			conn = dataSource.getConnection();

			if (galleryImageList.getSectionId() >= 0)
			{
				//With Section Filter.
				selectSql = "SELECT [Rank],[ImageId],[Name],[Description],[UploadDate],[TakenDateMeta],"
						+ " CASE WHEN [MachineId] = ? THEN [LocalPath] ELSE NULL END AS [LocalPath] "
						+ " FROM(   SELECT RANK() OVER (ORDER BY i.[Name], i.[ImageId]) as [Rank], i.[ImageId],i.[Name],i.[Description], "
						+ " i.[MachineId], i.[LocalPath],im.[UploadDate],im.[TakenDateMeta]"
						+ " FROM GalleryImage gi INNER JOIN Image i ON gi.ImageId = i.ImageId "
						+ " INNER JOIN ImageMeta im ON i.ImageId = im.ImageId"
						+ " WHERE gi.[GalleryId] = ? AND i.Status = 3 AND gi.[SectionId] = ?) AS RR"
						+ " WHERE RR.[Rank] > ? AND RR.[Rank] <= ? ORDER BY [Name]";
				
				ps = conn.prepareStatement(selectSql);
				ps.setLong(1, machineId);
				ps.setLong(2, galleryImageList.getId());
				ps.setLong(3, galleryImageList.getSectionId());
				ps.setInt(4, imageCursor);
				ps.setInt(5, imageCursor + imageCount);
			}
			else
			{
				selectSql = "SELECT [Rank],[ImageId],[Name],[Description],[UploadDate],[TakenDateMeta],"
						+ " CASE WHEN [MachineId] = ? THEN [LocalPath] ELSE NULL END AS [LocalPath] "
						+ " FROM(   SELECT RANK() OVER (ORDER BY i.[Name], i.[ImageId]) as [Rank], i.[ImageId],i.[Name],i.[Description], "
						+ " i.[MachineId], i.[LocalPath],im.[UploadDate],im.[TakenDateMeta]"
						+ " FROM GalleryImage gi INNER JOIN Image i ON gi.ImageId = i.ImageId INNER JOIN ImageMeta im ON i.ImageId = im.ImageId"
						+ " WHERE gi.[GalleryId] = ? AND i.Status = 3 ) AS RR"
						+ " WHERE RR.[Rank] > ? AND RR.[Rank] <= ? ORDER BY [Name]";
				
				ps = conn.prepareStatement(selectSql);
				ps.setLong(1, machineId);
				ps.setLong(2, galleryImageList.getId());
				ps.setInt(3, imageCursor);
				ps.setInt(4, imageCursor + imageCount);
			}
			

			//ps.setString(5, "[Name]"); //Sort
			
			resultset = ps.executeQuery();
			oldGreg = new GregorianCalendar();
			galleryImageList.setImages(new ImageList.Images());

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
				
				newImageRef.setLocalPath(resultset.getString(7));
				
				galleryImageList.getImages().getImageRef().add(newImageRef);
			}
			resultset.close();
			
			if (galleryImageList.getImages().getImageRef().size() == 0)
			{
				meLogger.info("Select statement didn't return any records.");
				//throw new WallaException("GalleryDataHelperImpl", "GetGalleryImages", error, 0); 
			}
			
			galleryImageList.setImageCursor(imageCursor);
			galleryImageList.setImageCount(galleryImageList.getImages().getImageRef().size());
			
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetGalleryImages", sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
		} 
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetGalleryImages", ex);
			throw new WallaException(ex, HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	public ImageList GetGalleryImageListMeta(long userId, String galleryName, long sectionId) throws WallaException
	{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		ImageList galleryImageList = null;
		String selectSql = null;
		
		try {
			conn = dataSource.getConnection();

			if (sectionId == -1)
			{
				selectSql = "SELECT G.[GalleryId],G.[Name],G.[Description],-1,G.[TotalImageCount],G.[LastUpdated],G.[RecordVersion] "
						+ "FROM [dbo].[Gallery] G WHERE G.[UserId] = ? AND G.[Name]= ?";
				ps = conn.prepareStatement(selectSql);
				ps.setLong(1, userId);
				ps.setString(2,galleryName);
			}
			else
			{
				selectSql = "SELECT G.[GalleryId],G.[Name],G.[Description],GS.[ImageCount],G.[TotalImageCount],G.[LastUpdated],G.[RecordVersion] "
						+ "FROM [dbo].[Gallery] G INNER JOIN GallerySection GS ON G.[GalleryId] = GS.[GalleryId] "
						+ "WHERE G.[UserId] = ? AND G.[Name]= ? AND GS.[SectionId] = ?";
				ps = conn.prepareStatement(selectSql);
				ps.setLong(1, userId);
				ps.setString(2,galleryName);
				ps.setLong(3, sectionId);
			}

			resultset = ps.executeQuery();

			if (!resultset.next())
			{
				resultset.close();
				String error = "Select statement didn't return any records.";
				meLogger.error(error);
				throw new WallaException("GalleryDataHelperImpl", "GetGalleryImageListMeta", error, 0); 
			}
			
			galleryImageList = new ImageList();
			galleryImageList.setId(resultset.getLong(1));
			galleryImageList.setType("Gallery");
			galleryImageList.setName(resultset.getString(2));
			galleryImageList.setDesc(resultset.getString(3));
			galleryImageList.setSectionId(sectionId);
			galleryImageList.setSectionImageCount(resultset.getInt(4));
			galleryImageList.setTotalImageCount(resultset.getInt(5));

			GregorianCalendar oldGreg = new GregorianCalendar();
			oldGreg.setTime(resultset.getTimestamp(6));
			XMLGregorianCalendar xmlOldGreg = DatatypeFactory.newInstance().newXMLGregorianCalendar(oldGreg);
			
			galleryImageList.setLastChanged(xmlOldGreg);
			galleryImageList.setVersion(resultset.getInt(7));
			
			resultset.close();
			return galleryImageList;
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetGalleryImageListMeta", sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
		} 
		catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetGalleryImageListMeta", ex);
			throw new WallaException(ex, HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	public void RegenerateGalleryImages(long userId, long galleryId) throws WallaException
	{
		Connection conn = null;
		CallableStatement idSproc = null;
		try {
			conn = dataSource.getConnection();

			String sprocSql = "EXEC [dbo].[GenerateGalleryImages] ?, ?";
			
		    idSproc = conn.prepareCall(sprocSql);
		    idSproc.setLong(1, userId);
		    idSproc.setLong(2, galleryId);
		    idSproc.execute();
		    
		    conn.commit();
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in RegenerateGalleryImages", sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
		} 
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in RegenerateGalleryImages", ex);
			throw new WallaException(ex, HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally {
	        if (idSproc != null) try { idSproc.close(); } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { conn.close(); } catch (SQLException logOrIgnore) {}
		}
	}

}


