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
public class TagDataHelperImpl implements TagDataHelper {

	private DataSource dataSource;
	
	private static final Logger meLogger = Logger.getLogger(TagDataHelperImpl.class);

	public TagDataHelperImpl() {
		meLogger.debug("TagDataHelperImpl object instantiated.");
	}
	
	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}
	
	public void CreateTag(long userId, Tag newTag, long newId) throws WallaException
	{
		String sql = "INSERT INTO [Tag] ([TagId],[Name],[Description],[SystemOwned],[SqlExpression],[LastUpdated],[RecordVersion],[UserId]) "
				+ "VALUES (?,?,?,?,?,GetDate(),?,?)";
		
		Connection conn = null;
		PreparedStatement ps = null;
		PreparedStatement bs = null;
		//Statement sQuery = null;
		//ResultSet resultset = null;

		try {			
			int controlCount = 0;
			int returnCount = 0;
			long newTagId = newId;
			int[] responseCounts = null;
			
			meLogger.debug("CreateTag() begins. UserId:" + userId + " TagName:" + newTag.getName());
			
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			//Insert main tag record.
			ps = conn.prepareStatement(sql);
			ps.setLong(1, newTagId);
			ps.setString(2, newTag.getName());
			ps.setString(3, newTag.getDesc());
			ps.setBoolean(4, false);
			ps.setString(5, "");

			ps.setInt(6, 1);
			ps.setLong(7, userId);
			
			//Execute insert statement.
			returnCount = ps.executeUpdate();
			
			//Validate new record was successful.
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Insert statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException(this.getClass().getName(), "CreateTag", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 				
			}

			meLogger.debug("CreateTag() ends. UserId:" + userId + " TagName:" + newTag.getName());
			
			conn.commit();
				
		} catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected SQLException in CreateTag", sqlEx);
			throw new WallaException(sqlEx,0);
		} catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			
			meLogger.error("Unexpected Exception in CreateTag", ex);
			throw new WallaException(ex, 0);
		}
		finally {
	        if (ps != null) try { ps.close(); } catch (SQLException logOrIgnore) {}
	        if (bs != null) try { bs.close(); } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { conn.close(); } catch (SQLException logOrIgnore) {}
		}
	}
	
	public void UpdateTag(long userId, Tag existingTag) throws WallaException
	{
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {			
			int returnCount = 0;
			String updateVersionSql = null;
			
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			updateVersionSql = "UPDATE [dbo].[Tag] SET [Name] = ?, [Description] = ?, [LastUpdated] = GetDate(), [RecordVersion] = [RecordVersion] + 1 WHERE [TagId] = ? AND [RecordVersion] = ?";

			ps = conn.prepareStatement(updateVersionSql);
			ps.setString(1, existingTag.getName());
			ps.setString(2, existingTag.getDesc());
			ps.setLong(3, existingTag.getId());
			ps.setInt(4, existingTag.getVersion());
			
			//Execute update and check response.
			returnCount = ps.executeUpdate();
			ps.close();
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Update statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException("TagDataHelperImpl", "UpdateTag", error, HttpStatus.CONFLICT.value()); 
			}
			
			conn.commit();
		}
		catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected Exception in UpdateTag", sqlEx);
			throw new WallaException(sqlEx, 0);
		} catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected Exception in UpdateTag", ex);
			throw new WallaException(ex, 0);
		}
		finally {
	        if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	public void DeleteTag(long userId, long tagId, int version, String tagName) throws WallaException
	{
		Connection conn = null;
		PreparedStatement ps = null;
		Statement us = null;
		
		try {
			int returnCount = 0;		
			
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			String deleteSql = "DELETE FROM [Tag] WHERE [TagId]= ? AND [RecordVersion] = ? AND [UserId] = ? AND [Name] = ?"; 
			ps = conn.prepareStatement(deleteSql);
			ps.setLong(1, tagId);
			ps.setInt(2, version);
			ps.setLong(3, userId);
			ps.setString(4, tagName);

			//Execute update and check response.
			returnCount = ps.executeUpdate();
			ps.close();
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Delete statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException("TagDataHelperImpl", "DeleteTag", error, HttpStatus.CONFLICT.value()); 
			}

			String updateSql = "UPDATE [User] SET [TagLastDeleted] = GetDate() WHERE [UserId] = " + userId;
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
			meLogger.error("Unexpected SQLException in DeleteTag", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected Exception in DeleteTag", ex);
			throw new WallaException(ex, 0);
		}
		finally {
	        if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (us != null) try { if (!us.isClosed()) {us.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	public void DeleteTagReferences(long tagId) throws WallaException
	{
		Connection conn = null;
		Statement us = null;
		
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);

			String deleteImagesSql = "DELETE FROM [TagImage] WHERE [TagId]=" + tagId; 
			String deleteGalleryTagsSql = "DELETE FROM [GalleryTag] WHERE [TagId]=" + tagId; 
			us = conn.createStatement();
			us.addBatch(deleteImagesSql);
			us.addBatch(deleteGalleryTagsSql);
			us.executeBatch();
			us.close();

			conn.commit();
		}
		catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected SQLException in UpdateTagTimestamp", sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
		} 
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected Exception in UpdateTagTimestamp", ex);
			throw new WallaException(ex, HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally {
	        if (us != null) try { if (!us.isClosed()) {us.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
		
	}
	
	public Tag GetTagMeta(long userId, String tagName) throws WallaException
	{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		Tag tag = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT [TagId],[Name],[Description],[LastUpdated],[RecordVersion] FROM [dbo].[Tag] WHERE [UserId] = ? AND [Name]= ?";
			ps = conn.prepareStatement(selectSql);
			//ps.setFetchDirection(ResultSet.TYPE_FORWARD_ONLY);
			ps.setLong(1, userId);
			ps.setString(2,tagName);

			resultset = ps.executeQuery();

			if (!resultset.next())
			{
				return null;
			}
			
			tag = new Tag();
			tag.setId(resultset.getLong(1));
			tag.setName(resultset.getString(2));
			tag.setDesc(resultset.getString(3));
			
			GregorianCalendar oldGreg = new GregorianCalendar();
			oldGreg.setTime(resultset.getTimestamp(4));
			XMLGregorianCalendar xmlOldGreg = DatatypeFactory.newInstance().newXMLGregorianCalendar(oldGreg);
			
			tag.setLastChanged(xmlOldGreg);
			tag.setVersion(resultset.getInt(5));
			
			return tag;
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetTagMeta", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetTagMeta", ex);
			throw new WallaException(ex, 0);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	public ImageList GetTagImageListMeta(long userId, String tagName) throws WallaException
	{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		ImageList tagImageList = null;
		
		try {
			conn = dataSource.getConnection();

			String selectSql = "SELECT [TagId],[Name],[Description],[ImageCount],[LastUpdated],[RecordVersion] FROM [dbo].[Tag] WHERE [UserId] = ? AND [Name]= ?";
			ps = conn.prepareStatement(selectSql);

			ps.setLong(1, userId);
			ps.setString(2,tagName);

			resultset = ps.executeQuery();

			if (!resultset.next())
			{
				resultset.close();
				String error = "Select statement didn't return any records.";
				meLogger.error(error);
				throw new WallaException("TagDataHelperImpl", "GetTagImageListMeta", error, 0); 
			}
			
			tagImageList = new ImageList();
			tagImageList.setId(resultset.getLong(1));
			tagImageList.setType("Tag");
			tagImageList.setName(resultset.getString(2));
			tagImageList.setDesc(resultset.getString(3));
			tagImageList.setTotalImageCount(resultset.getInt(4));
			
			GregorianCalendar oldGreg = new GregorianCalendar();
			oldGreg.setTime(resultset.getTimestamp(5));
			XMLGregorianCalendar xmlOldGreg = DatatypeFactory.newInstance().newXMLGregorianCalendar(oldGreg);
			
			tagImageList.setLastChanged(xmlOldGreg);
			tagImageList.setVersion(resultset.getInt(6));
			
			resultset.close();
			return tagImageList;
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetTagImageListMeta", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetTagImageListMeta", ex);
			throw new WallaException(ex, 0);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	public Date LastTagListUpdate(long userId) throws WallaException
	{
		Connection conn = null;
		Statement sQuery = null;
		ResultSet resultset = null;
		java.util.Date utilDate = null;
		
		try {			
			conn = dataSource.getConnection();
			
			String selectSql = "SELECT MAX(UpdateDate) FROM (SELECT T.[LastUpdated] AS [UpdateDate] FROM "
					+ "[Tag] T WHERE T.[UserId] = " + userId + " UNION SELECT [TagLastDeleted] AS [UpdateDate] "
					+ "FROM [User] U WHERE U.[UserId] = " + userId + ") TagDates";
			
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
			meLogger.error("Unexpected SQLException in LastTagListUpdate", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in LastTagListUpdate", ex);
			throw new WallaException(ex, 0);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (sQuery != null) try { if (!sQuery.isClosed()) {sQuery.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}

	//TODO Add search params + change image status
	public int GetTotalImageCount(long tagId) throws WallaException
	{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT COUNT(*) "
								+ "FROM TagImage ti INNER JOIN Image i ON ti.ImageId = i.ImageId INNER JOIN ImageMeta im ON i.ImageId = im.ImageId "
								+ "WHERE ti.[TagId] = ? AND i.Status = 3";
			
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, tagId);
			
			resultset = ps.executeQuery();
			if (resultset.next())
			{
				return resultset.getInt(1);
			}
			else
			{
				String error = "Select statement didn't return any records, in GetTotalImageCount.";
				meLogger.error(error);
				throw new WallaException("TagDataHelperImpl", "GetTotalImageCount", error, 0); 
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
	public void GetTagImages(long userId, long machineId, int imageCursor, int imageCount, ImageList tagImageList) throws WallaException
	{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		GregorianCalendar oldGreg = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT [Rank],[ImageId],[Name],[Description],[UploadDate],[TakenDateMeta],"
					+ " CASE WHEN [MachineId] = ? THEN [LocalPath] ELSE NULL END AS [LocalPath], [CategoryId] "
					+ " FROM(   SELECT RANK() OVER (ORDER BY i.[Name], i.[ImageId]) as [Rank], i.[ImageId],i.[Name],i.[Description], "
					+ " i.[MachineId], i.[LocalPath],im.[UploadDate],im.[TakenDateMeta], i.[CategoryId]"
					+ " FROM TagImage ti INNER JOIN Image i ON ti.ImageId = i.ImageId INNER JOIN ImageMeta im ON i.ImageId = im.ImageId"
					+ " WHERE ti.[TagId] = ? AND i.Status = 3 ) AS RR"
					+ " WHERE RR.[Rank] > ? AND RR.[Rank] <= ? ORDER BY [Name]";
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, machineId);
			ps.setLong(2, tagImageList.getId());
			ps.setInt(3, imageCursor);
			ps.setInt(4, imageCursor + imageCount);
			//ps.setString(5, "[Name]"); //Sort
			
			resultset = ps.executeQuery();
			oldGreg = new GregorianCalendar();
			tagImageList.setImages(new ImageList.Images());

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
				newImageRef.setCategoryId(resultset.getLong(8));
				tagImageList.getImages().getImageRef().add(newImageRef);
			}
			resultset.close();
			
			if (tagImageList.getImages().getImageRef().size() == 0)
			{
				meLogger.info("Select statement didn't return any records.");
				//throw new WallaException("TagDataHelperImpl", "GetTagImages", error, 0); 
			}
			
			tagImageList.setImageCount(tagImageList.getImages().getImageRef().size());
			tagImageList.setImageCursor(imageCursor);
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetTagImages", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetTagImages", ex);
			throw new WallaException(ex, 0);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
	
	public TagList GetUserTagList(long userId) throws WallaException
	{
		Connection conn = null;
		Statement sQuery = null;
		ResultSet resultset = null;
		TagList tagList = null;
		
		try {			
			conn = dataSource.getConnection();
			
			String selectSql = "SELECT t.[TagId], t.[Name], t.[Description], [ImageCount] FROM "
					+ "Tag t WHERE t.[UserId] = " + userId + " ORDER BY t.[Name]";
			sQuery = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			resultset = sQuery.executeQuery(selectSql);

			while (resultset.next())
			{
				if (tagList == null)
				{
					tagList = new TagList();
				}
				
				TagList.TagRef newTagRef = new TagList.TagRef(); 
				newTagRef.setId(resultset.getLong(1));
				newTagRef.setName(resultset.getString(2));
				newTagRef.setDesc(resultset.getString(3));
				newTagRef.setCount(resultset.getInt(4));				
				tagList.getTagRef().add(newTagRef);
			}
			
			resultset.close();
			return tagList;
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetUserTagList", sqlEx);
			throw new WallaException(sqlEx,0);
		} 
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetUserTagList", ex);
			throw new WallaException(ex, 0);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (sQuery != null) try { if (!sQuery.isClosed()) {sQuery.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}

	public void UpdateTagTimeAndCount(long userId, long tagId) throws WallaException
	{
		Connection conn = null;
		Statement us = null;
		
		try {
			int returnCount = 0;		
			
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);

			String updateSql = "UPDATE [Tag] SET [LastUpdated] = GetDate(), "
					+ "[ImageCount] = (SELECT COUNT(1) FROM [TagImage] TI INNER JOIN [Image] I ON TI.[ImageId] = I.[ImageId] "
					+ "WHERE TI.[TagId] = [Tag].[TagId] AND I.[Status]=3) "
					+ "WHERE [Tag].TagId = " + tagId + " AND [UserId] = " + userId;
			
			us = conn.createStatement();
			returnCount = us.executeUpdate(updateSql);
			us.close();
			
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Update timestamp statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException("TagDataHelperImpl", "UpdateTagTimestamp", error, HttpStatus.CONFLICT.value()); 
			}
			
			conn.commit();
		}
		catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected SQLException in UpdateTagTimestamp", sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
		} 
		catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected Exception in UpdateTagTimestamp", ex);
			throw new WallaException(ex, HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally {
	        if (us != null) try { if (!us.isClosed()) {us.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}	

	public void AddRemoveTagImages(long userId, long tagId, ImageMoveList moveList, boolean add) throws WallaException
	{
		Connection conn = null;
		PreparedStatement bs = null;

		try {			
			int controlCount = 0;
			int returnCount = 0;
			int[] responseCounts = null;
			
			meLogger.debug("AddRemoveTagImages() begins. UserId:" + userId + " TagId:" + tagId);
			
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			//Add new images in the tag if there are any
			if (moveList.getImageRef() != null)
			{
				if (moveList.getImageRef().size() > 0)
				{
					controlCount = 0;
					returnCount = 0;
					
					String sql = null;
					
					if (add)
					{
						sql = "EXECUTE TagImageInsert ?, ?";
					}
					else
					{
						sql = "DELETE FROM [dbo].[TagImage] WHERE [TagId] = ? AND [ImageId] = ?";
					}
				    bs = conn.prepareStatement(sql);			   
				    
					for (int i = 0; i < moveList.getImageRef().size(); i++)
					{
						long imageId = moveList.getImageRef().get(i);
						bs.setLong(1, tagId);
						bs.setLong(2, imageId);
						bs.addBatch();
						
						controlCount++;
					}
					
					//Perform updates.
					responseCounts = bs.executeBatch();
					if (!add)
					{
						for (int i = 0; i < responseCounts.length; i++)
						{
							returnCount = returnCount + responseCounts[i];
						}
						
						//Check for unexpected row update count in the database
						if (returnCount != controlCount)
						{
							conn.rollback();
							String error = "Row count update didn't match with number of Tag Images to be updated.";
							meLogger.error(error);
							throw new WallaException("TagDataHelperImpl", "AddRemoveTagImages", error, HttpStatus.CONFLICT.value()); 
						}
					}
				}
			}
			
			meLogger.debug("AddRemoveTagImages() ends. UserId:" + userId + " TagId:" + tagId);
			
			conn.commit();
				
		} catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error("Unexpected SQLException in AddRemoveTagImages", sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
		} catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			
			meLogger.error("Unexpected Exception in AddRemoveTagImages", ex);
			throw new WallaException(ex, HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally {
	        if (bs != null) try { bs.close(); } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { conn.close(); } catch (SQLException logOrIgnore) {}
		}
	}

	public long[] GetGalleriesLinkedToTag(long userId, long tagId) throws WallaException
	{
		Connection conn = null;
		Statement sQuery = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();
			
			String selectSql = "SELECT DISTINCT G.GalleryId FROM Gallery G INNER JOIN GalleryTag GT ON "
					+ "G.GalleryId = GT.GalleryId WHERE G.UserId = " + userId + " AND GT.TagId = " + tagId;
			sQuery = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			resultset = sQuery.executeQuery(selectSql);

			int size = 0;
			try {
				resultset.last();
			    size = resultset.getRow();
			    resultset.beforeFirst();
			}
			catch(Exception ex) {}
			
			long[] galleryIds = new long[size];
			for (int i = 0; i < size; i++)
			{
				resultset.next();
				galleryIds[i] = resultset.getLong(1);
			}
			
			resultset.close();
			return galleryIds;			
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetGalleriesLinkedToTag", sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
		} 
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetGalleriesLinkedToTag", ex);
			throw new WallaException(ex, HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (sQuery != null) try { if (!sQuery.isClosed()) {sQuery.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
	}
}


//TODO delete.
/*
public int xxxGetPlatformImageCount(long platformId) throws WallaException
{
	Connection conn = null;
	Statement sQuery = null;
	ResultSet resultset = null;
	
	try {			
		conn = dataSource.getConnection();
		
		String selectSql = "SELECT [ListNumber] FROM [Platform] WHERE [PlatformId]=" + platformId;
		
		sQuery = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		resultset = sQuery.executeQuery(selectSql);
		if (resultset.next())
		{
			return resultset.getInt(1);
		}
		else
		{
			String error = "GetPlatformImageCount didn't return an image count.  PlatformId: " + platformId;
			meLogger.error(error);
			throw new WallaException("TagDataHelperImpl", "GetPlatformImageCount", error, 0);
		}
	}
	catch (SQLException sqlEx) {
		meLogger.error("Unexpected SQLException in GetPlatformImageCount", sqlEx);
		throw new WallaException(sqlEx,0);
	} 
	catch (Exception ex) {
		meLogger.error("Unexpected Exception in GetPlatformImageCount", ex);
		throw new WallaException(ex, 0);
	}
	finally {
		if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
        if (sQuery != null) try { if (!sQuery.isClosed()) {sQuery.close();} } catch (SQLException logOrIgnore) {}
        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	}
}
*/
