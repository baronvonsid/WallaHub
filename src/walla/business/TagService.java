package walla.business;

import java.util.*;
import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.db.*;
import walla.utils.*;
import javax.sql.DataSource;
import javax.xml.datatype.*;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Qualifier;

@Service("TagService")
public class TagService {

	private TagDataHelperImpl tagDataHelper;
	private UtilityDataHelperImpl utilityDataHelper;
	private CachedData cachedData;
	private GalleryService galleryService;
	
	private static final Logger meLogger = Logger.getLogger(TagService.class);

	//*************************************************************************************************************
	//***********************************  Web server synchronous methods *****************************************
	//*************************************************************************************************************
	
	public int CreateUpdateTag(long userId, Tag newtag, String tagName)
	{
		try {
			
			meLogger.debug("CreateUpdateTag() begins. UserId:" + userId + " TagName:" + tagName);
			
			//TODO Check User is logged in with Write permission
			//HttpStatus.UNAUTHORIZED.value()
			
			Tag existingTag = tagDataHelper.GetTagMeta(userId, tagName);

			if (existingTag == null)
			{
				if (!newtag.getName().equals(tagName))
				{
					String error = "Create Tag failed, names don't match.";
					meLogger.error(error);
					throw new WallaException("TagService", "CreateUpdateTag", error, HttpStatus.CONFLICT.value()); 
				}
				
				long newTagId = utilityDataHelper.GetNewId("TagId");
				tagDataHelper.CreateTag(userId, newtag, newTagId);
				return HttpStatus.CREATED.value();
			}
			else
			{
				if (existingTag.getVersion() != newtag.getVersion())
				{
					String error = "Update Tag failed, record versions don't match.";
					meLogger.error(error);
					throw new WallaException("TagService", "CreateUpdateTag", error, HttpStatus.CONFLICT.value()); 
				}
				
				tagDataHelper.UpdateTag(userId, newtag);
				
				meLogger.debug("CreateUpdateTag() has completed. UserId:" + userId);
				
				if (!existingTag.getName().equals(tagName))
				{
					return HttpStatus.MOVED_PERMANENTLY.value();
				}
				else
				{
					return HttpStatus.OK.value();
				}
			}
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process CreateUpdateTag");
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces CreateUpdateTag", ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
	}

	public int DeleteTag(long userId, Tag tag, String tagName)
	{
		try {
			meLogger.debug("DeleteTag() begins. UserId:" + userId + " TagName:" + tagName);
			
			if (!tag.getName().equals(tagName))
			{
				String error = "Delete Tag failed, names don't match.";
				meLogger.error(error);
				throw new WallaException("TagService", "DeleteTag", error, HttpStatus.CONFLICT.value()); 
			}
			
			tagDataHelper.DeleteTag(userId, tag.getId(), tag.getVersion(), tagName);
			
			meLogger.debug("DeleteTag() has completed. UserId:" + userId);
			
			//TODO decouple method
			TagRippleDelete(userId, tag.getId());
			
			return HttpStatus.OK.value();
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process DeleteTag");
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces DeleteTag", ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
	}
	
	public Tag GetTagMeta(long userId, String tagName, CustomResponse customResponse)
	{
		try {
			//Check user can access tag list
			//HttpStatus.UNAUTHORIZED.value()
			
			meLogger.debug("GetTagMeta() begins. UserId:" + userId + " TagName:" + tagName);
			
			//Get tag list for response.
			Tag tag = tagDataHelper.GetTagMeta(userId, tagName);
			if (tag == null)
			{
				String error = "GetTagMeta didn't return a valid Tag object";
				meLogger.error(error);
				throw new WallaException("TagService", "GetTagMeta", error, 0); 
			}
			
			meLogger.debug("GetTagMeta has completed. UserId:" + userId);
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			return tag;
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process GetTagMeta", wallaEx);
			customResponse.setResponseCode(wallaEx.getCustomStatus());
			return null;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to process GetTagMeta",ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}
	
	public TagList GetTagListForUser(long userId, Date clientVersionTimestamp, CustomResponse customResponse)
	{
		try {
			//Check user can access tag list
			//HttpStatus.UNAUTHORIZED.value()
			
			meLogger.debug("GetTagListForUser() begins. UserId:" + userId);
			
			TagList tagList = null;
			Date lastUpdate = tagDataHelper.LastTagListUpdate(userId);
			
			//lastUpdate.setTime(1000 * (lastUpdate.getTime() / 1000));
			
			//Check if tag list changed
			if (clientVersionTimestamp != null)
			{
				if (!lastUpdate.after(clientVersionTimestamp) || lastUpdate.equals(clientVersionTimestamp))
				{
					meLogger.debug("No tag list generated because server timestamp (" + lastUpdate.toString() + ") is not later than client timestamp (" + clientVersionTimestamp.toString() + ")");
					customResponse.setResponseCode(HttpStatus.NOT_MODIFIED.value());
					return null;
				}
			}
			
			//Get tag list for response.
			tagList = tagDataHelper.GetUserTagList(userId);
			
			if (tagList != null)
			{
				GregorianCalendar gregory = new GregorianCalendar();
				gregory.setTime(lastUpdate);
				XMLGregorianCalendar xmlOldGreg = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregory);
				
				tagList.setLastChanged(xmlOldGreg);
			}
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			
			meLogger.debug("GetTagListForUser has completed. UserId:" + userId);
			return tagList;
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process GetTagListForUser");
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces GetTagListForUser", ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}

	public int AddRemoveTagImages(long userId, String tagName, ImageMoveList moveList, boolean add)
	{
		try {
			meLogger.debug("AddRemoveTagImages() begins. UserId:" + userId + " TagName:" + tagName);
			
			Tag tag = tagDataHelper.GetTagMeta(userId, tagName);
			if (tag == null)
			{
				String error = "AddRemoveTagImages didn't return a valid Tag object";
				meLogger.error(error);
				throw new WallaException("TagService", "AddRemoveTagImages", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 
			}
			
			tagDataHelper.AddRemoveTagImages(userId, tag.getId(), moveList, add);
			
			meLogger.debug("AddRemoveTagImages() has completed. UserId:" + userId);
			
			//TODO decouple method
			TagRippleUpdate(userId, tag.getId());
			
			return HttpStatus.OK.value();
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process AddRemoveTagImages");
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces AddRemoveTagImages", ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
		
	}
	
	public long CreateOrFindUserAppTag(long userId, int platformId, String machineName)
	{
		try
		{
			Platform platform = cachedData.GetPlatform(platformId, "", "", 0, 0);
			String tagName = platform.getShortName() + " " + machineName;
			if (tagName.length() > 30)
				tagName = tagName.substring(0,30);
			
			String sql = "SELECT [TagId] FROM [Tag] WHERE [SystemOwned] = 1 AND [Name] = '" + tagName + "' AND [UserId] = " + userId;
			long tagId = utilityDataHelper.GetLong(sql);
			if (tagId > 1)
			{
				return tagId;
			}
			else
			{
				Tag newTag = new Tag();
				long newTagId = utilityDataHelper.GetNewId("TagId");
				newTag.setName(tagName);
				newTag.setDesc("Auto generated tag for fotos uploaded from " + machineName + " - " + platform.getShortName());
				newTag.setSystemOwned(true);
				tagDataHelper.CreateTag(userId, newTag, newTagId);
				
				return newTagId;
			}
		}
		catch (WallaException wallaEx) {
			meLogger.error(wallaEx);
			return 0;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return 0;
		}
	}
	
	//*************************************************************************************************************
	//*************************************  Messaging initiated methods ******************************************
	//*************************************************************************************************************
	
	public void ReGenDynamicTags(long userId)
	{
		try
		{
			//Returns an array of affected tags.
			long tags[] = tagDataHelper.ReGenDynamicTags(userId);
			
			for (int ii = 0; ii < tags.length; ii++)
			{
				long[] galleryIds = tagDataHelper.GetGalleriesLinkedToTag(userId, tags[ii]);
				for (int i = 0; i < galleryIds.length; i++)
				{
					//TODO decouple
					galleryService.RefreshGalleryImages(userId, galleryIds[i]);
				}
			}
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process ReGenDynamicTags");
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces ReGenDynamicTags", ex);
		}
	}
	
	//Must aggregate up requests
 	public void TagRippleUpdate(long userId, long tagId)
	{
		try
		{
			//Update tag updated dates
			tagDataHelper.UpdateTagTimeAndCount(userId, tagId);
			
			//Find any views which reference this tag and update last updated.
			long[] galleryIds = tagDataHelper.GetGalleriesLinkedToTag(userId, tagId);
			for (int i = 0; i < galleryIds.length; i++)
			{
				//TODO decouple
				galleryService.RefreshGalleryImages(userId, galleryIds[i]);
			}
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process TagRippleUpdates");
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces TagRippleUpdates", ex);
		}
	}
 	
 	public void TagRippleDelete(long userId, long tagId)
 	{
		try
		{
			//Find any views which reference this tag and update last updated.
			long[] galleryIds = tagDataHelper.GetGalleriesLinkedToTag(userId, tagId);
			
			//Update tag updated dates
			tagDataHelper.DeleteTagReferences(tagId);
			
			for (int i = 0; i < galleryIds.length; i++)
			{
				//TODO decouple
				galleryService.RefreshGalleryImages(userId, galleryIds[i]);
			}
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process TagDeleteTagImages");
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces TagDeleteTagImages", ex);
		}
 	}
	
	public void setTagDataHelper(TagDataHelperImpl tagDataHelper)
	{
		this.tagDataHelper = tagDataHelper;
	}
	
	public void setCachedData(CachedData cachedData)
	{
		this.cachedData = cachedData;
	}
	
	public void setUtilityDataHelper(UtilityDataHelperImpl utilityDataHelper)
	{
		this.utilityDataHelper = utilityDataHelper;
	}
	
	public void setGalleryService(GalleryService galleryService)
	{
		this.galleryService = galleryService;
	}
}
