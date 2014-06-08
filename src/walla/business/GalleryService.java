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
import java.util.GregorianCalendar;
import java.util.Iterator;
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
				galleryDataHelper.CreateGallery(userId, newGallery, newGalleryId, UserTools.GetComplexString());
				
				//TODO switch to messaging.
				RefreshGalleryImages(userId, newGalleryId);
				
				return HttpStatus.CREATED.value();
			}
			else
			{
				if (newGallery.getId() == null || newGallery.getVersion() == null)
				{
					String error = "Update Gallery failed, ids and versions weren't supplied.";
					meLogger.error(error);
					throw new WallaException("GalleryService", "CreateUpdateGallery", error, HttpStatus.CONFLICT.value()); 
				}
				
				if (existingGallery.getId().longValue() != newGallery.getId().longValue())
				{
					String error = "Update Gallery failed, ids don't match.";
					meLogger.error(error);
					throw new WallaException("GalleryService", "CreateUpdateGallery", error, HttpStatus.CONFLICT.value()); 
				}
				
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
			meLogger.error("Unexpected error when trying to process CreateUpdateGallery", ex);
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

	public long GetDefaultGallery(long userId, int appId)
	{
		try
		{
			App app = cachedData.GetApp(appId, "");
			
			String sql = "SELECT [GalleryId] FROM [Gallery] WHERE [SystemOwned] = 1 "
					+ "AND [GalleryType] = " + app.getDefaultGalleryType() + " AND [UserId] = " + userId;
			
			long galleryId = utilityDataHelper.GetLong(sql);
			if (galleryId > 0)
			{
				return galleryId;
			}
			
			return 0;
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
	
	public GalleryOptions GetGalleryOptions(long userId, Date clientVersionTimestamp, CustomResponse customResponse)
	{
		try {
			Date latestDate = new Date();
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.YEAR, -10);
			latestDate.setTime(cal.getTimeInMillis());
			
			
			meLogger.debug("GetGalleryOptions() begins. UserId:" + userId);
			
			//Get Presentation and Style objects from memory.
			List<Presentation> presentations = cachedData.GetPresentationList();
			List<Style> style = cachedData.GetStyleList();

			
			GalleryOptions options = new GalleryOptions();
			options.setPresentation(new GalleryOptions.Presentation());
			
			for (Iterator<Presentation> iterater = presentations.iterator(); iterater.hasNext();)
			{
				Presentation current = (Presentation)iterater.next();

				GalleryOptions.Presentation.PresentationRef ref = new GalleryOptions.Presentation.PresentationRef();
				ref.setPresentationId(current.getPresentationId());
				ref.setName(current.getName());
				ref.setDescription(current.getDesc());
				ref.setJspName(current.getJspName());
				ref.setCssExtension(current.getCssExtension());
				ref.setMaxSections(current.getMaxSections());
				
				if (current.getLastUpdated().after(latestDate) || current.getLastUpdated().equals(latestDate))
				{
					latestDate = current.getLastUpdated();
				}
				
				options.getPresentation().getPresentationRef().add(ref);
			}

			
			options.setStyle(new GalleryOptions.Style());
			for (Iterator<Style> iterater = style.iterator(); iterater.hasNext();)
			{
				Style current = (Style)iterater.next();

				GalleryOptions.Style.StyleRef ref = new GalleryOptions.Style.StyleRef();
				ref.setStyleId(current.getStyleId());
				ref.setName(current.getName());
				ref.setDescription(current.getDesc());
				ref.setCssFolder(current.getCssFolder());
				
				options.getStyle().getStyleRef().add(ref);
				
				if (current.getLastUpdated().after(latestDate) || current.getLastUpdated().equals(latestDate))
				{
					latestDate = current.getLastUpdated();
				}
			}
			
			meLogger.debug("GetGalleryOptions has completed. UserId:" + userId);
			
			if (clientVersionTimestamp == null || latestDate.after(clientVersionTimestamp))
			{
				GregorianCalendar oldGreg = new GregorianCalendar();
				oldGreg.setTime(latestDate);
				XMLGregorianCalendar xmlOldGreg = DatatypeFactory.newInstance().newXMLGregorianCalendar(oldGreg);
				options.setLastChanged(xmlOldGreg);
				
				customResponse.setResponseCode(HttpStatus.OK.value());
				return options;
			}
			else
			{
				meLogger.debug("No gallery options list generated because client list is up to date.");
				customResponse.setResponseCode(HttpStatus.NOT_MODIFIED.value());
				return null;
			}
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to process GetGalleryOptions",ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}

	public Gallery GetGallerySections(long userId, Gallery requestGallery, CustomResponse customResponse)
	{
		try {
			meLogger.debug("GetGallerySections() begins. UserId:" + userId);
			
			long newTempGalleryId = utilityDataHelper.GetNewId("TempGalleryId");
			
			Gallery gallery = galleryDataHelper.GetGallerySections(userId, requestGallery, newTempGalleryId);
			
			meLogger.debug("GetGallerySections has completed. UserId:" + userId);
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			return gallery;
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process GetGallerySections", wallaEx);
			customResponse.setResponseCode(wallaEx.getCustomStatus());
			return null;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to process GetGallerySections",ex);
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
