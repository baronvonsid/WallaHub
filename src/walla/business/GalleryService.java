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

@Service("GalleryService")
public class GalleryService {

	private GalleryDataHelperImpl galleryDataHelper;
	private UtilityDataHelperImpl utilityDataHelper;
	private CachedData cachedData;
	
	private static final Logger meLogger = Logger.getLogger(GalleryService.class);

	//*************************************************************************************************************
	//***********************************  Web server synchronous methods *****************************************
	//*************************************************************************************************************
	
	public int CreateUpdateGallery(long userId, Gallery newGallery, String galleryName)
	{
		try {
			meLogger.debug("CreateUpdateGallery() begins. UserId:" + userId + " Gallery name:" + galleryName);
			
			//TODO Check User is logged in with Write permission
			//HttpStatus.UNAUTHORIZED.value()
			
			Gallery existingGallery = galleryDataHelper.GetGalleryMeta(userId, galleryName);

			if (existingGallery == null)
			{
				if (!newGallery.getName().equals(galleryName))
				{
					String error = "Create Gallery failed, names don't match.";
					meLogger.error(error);
					throw new WallaException("GalleryService", "CreateUpdateGallery", error, HttpStatus.CONFLICT.value()); 
				}
				
				long newGalleryId = utilityDataHelper.GetNewId("GalleryId");
				galleryDataHelper.CreateGallery(userId, newGallery, newGalleryId, UserTools.GetComplexUrl());
				
				//TODO switch to messaging.
				RefreshGalleryImages(userId, newGalleryId);
				
				return HttpStatus.CREATED.value();
			}
			else
			{
				if (existingGallery.getVersion().intValue() != newGallery.getVersion().intValue())
				{
					String error = "Update Gallery failed, record versions don't match.";
					meLogger.error(error);
					throw new WallaException("GalleryService", "CreateUpdateGallery", error, HttpStatus.CONFLICT.value()); 
				}
				
				galleryDataHelper.UpdateGallery(userId, newGallery);
				
				meLogger.debug("CreateUpdateGallery has completed. UserId:" + userId);
				
				//TODO switch to messaging.
				RefreshGalleryImages(userId, newGallery.getId());
				
				if (!existingGallery.getName().equals(galleryName))
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
			meLogger.error("Unexpected error when trying to process CreateUpdateGallery");
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces CreateUpdateGallery", ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
	}

	public int DeleteGallery(long userId, Gallery gallery, String galleryName)
	{
		try {
			meLogger.debug("DeleteTag() begins. UserId:" + userId + " TagName:" + galleryName);
			
			if (!gallery.getName().equals(galleryName))
			{
				String error = "DeleteGallery failed, names don't match.";
				meLogger.error(error);
				throw new WallaException("GalleryService", "DeleteGallery", error, HttpStatus.CONFLICT.value()); 
			}
			
			galleryDataHelper.DeleteGallery(userId, gallery.getId(), gallery.getVersion(), galleryName);
			
			meLogger.debug("DeleteGallery has completed. UserId:" + userId);
			
			return HttpStatus.OK.value();
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process DeleteGallery");
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces DeleteGallery", ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
	}
	
	public Gallery GetGalleryMeta(long userId, String galleryName, CustomResponse customResponse)
	{
		try {
			//Check user can access gallery list
			//HttpStatus.UNAUTHORIZED.value()
			
			meLogger.debug("GetGalleryMeta() begins. UserId:" + userId + " GalleryName:" + galleryName);
			
			//Get gallery list for response.
			Gallery gallery = galleryDataHelper.GetGalleryMeta(userId, galleryName);
			if (gallery == null)
			{
				String error = "GetGalleryMeta didn't return a valid Gallery object";
				meLogger.error(error);
				throw new WallaException("GalleryService", "GetGalleryMeta", error, HttpStatus.NOT_FOUND.value()); 
			}
			
			meLogger.debug("GetGalleryMeta has completed. UserId:" + userId);
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			return gallery;
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process GetGalleryMeta", wallaEx);
			customResponse.setResponseCode(wallaEx.getCustomStatus());
			return null;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to process GetGalleryMeta",ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}

	public GalleryList GetGalleryListForUser(long userId, Date clientVersionTimestamp, CustomResponse customResponse)
	{
		try {
			//Check user can access tag list
			//HttpStatus.UNAUTHORIZED.value()
			
			meLogger.debug("GetGalleryListForUser() begins. UserId:" + userId);
			
			GalleryList galleryList = null;
			Date lastUpdate = galleryDataHelper.LastGalleryListUpdate(userId);
			
			//lastUpdate.setTime(1000 * (lastUpdate.getTime() / 1000));
			
			//Check if tag list changed
			if (clientVersionTimestamp != null)
			{
				if (!lastUpdate.after(clientVersionTimestamp) || lastUpdate.equals(clientVersionTimestamp))
				{
					meLogger.debug("No gallery list generated because server timestamp (" + lastUpdate.toString() + ") is not later than client timestamp (" + clientVersionTimestamp.toString() + ")");
					customResponse.setResponseCode(HttpStatus.NOT_MODIFIED.value());
					return null;
				}
			}
			
			//Get tag list for response.
			galleryList = galleryDataHelper.GetUserGalleryList(userId);
			
			if (galleryList!= null)
			{
				GregorianCalendar gregory = new GregorianCalendar();
				gregory.setTime(lastUpdate);
				XMLGregorianCalendar xmlOldGreg = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregory);
				
				galleryList.setLastChanged(xmlOldGreg);
			}
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			
			meLogger.debug("GetGalleryListForUser has completed. UserId:" + userId);
			return galleryList;
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process GetGalleryListForUser");
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces GetGalleryListForUser", ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}

	//*************************************************************************************************************
	//*************************************  Messaging initiated methods ******************************************
	//*************************************************************************************************************

	public void RefreshGalleryImages(long userId, long galleryId)
	{
		try
		{
			galleryDataHelper.RegenerateGalleryImages(userId, galleryId);
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces RefreshGalleryImages", ex);
		}
	}
	
	public void setGalleryDataHelper(GalleryDataHelperImpl galleryDataHelper)
	{
		this.galleryDataHelper = galleryDataHelper;
	}
	
	public void setCachedData(CachedData cachedData)
	{
		this.cachedData = cachedData;
	}
	
	public void setUtilityDataHelper(UtilityDataHelperImpl utilityDataHelper)
	{
		this.utilityDataHelper = utilityDataHelper;
	}
}
