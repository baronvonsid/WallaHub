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
		long startMS = System.currentTimeMillis();
		try {
			Gallery existingGallery = galleryDataHelper.GetGalleryMeta(userId, galleryName);
			if (existingGallery == null)
			{
				if (!newGallery.getName().equals(galleryName))
				{
					meLogger.warn("Create Gallery failed, names don't match.");
					return HttpStatus.CONFLICT.value(); 
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
					meLogger.warn("Update Gallery failed, ids and versions weren't supplied.");
					return HttpStatus.CONFLICT.value(); 
				}
				
				if (existingGallery.getId().longValue() != newGallery.getId().longValue())
				{
					meLogger.warn("Update Gallery failed, ids don't match.");
					return HttpStatus.CONFLICT.value(); 
				}
				
				if (existingGallery.getVersion().intValue() != newGallery.getVersion().intValue())
				{
					meLogger.warn("Update Gallery failed, record versions don't match.");
					return HttpStatus.CONFLICT.value(); 
				}
				
				galleryDataHelper.UpdateGallery(userId, newGallery);

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
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
		finally {UserTools.LogMethod("CreateUpdateGallery", meLogger, startMS, String.valueOf(userId) + " " + galleryName);}
	}

	public int DeleteGallery(long userId, Gallery gallery, String galleryName)
	{
		long startMS = System.currentTimeMillis();
		try {

			if (!gallery.getName().equals(galleryName))
			{
				meLogger.warn("DeleteGallery failed, names don't match.");
				return HttpStatus.CONFLICT.value(); 
			}
			
			galleryDataHelper.DeleteGallery(userId, gallery.getId(), gallery.getVersion(), galleryName);

			return HttpStatus.OK.value();
		}
		catch (WallaException wallaEx) {
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
		finally {UserTools.LogMethod("DeleteGallery", meLogger, startMS, String.valueOf(userId) + " " + galleryName);}
	}
	
	public long GetUserForGallery(String userName, String galleryName, String urlComplex)
	{
		long startMS = System.currentTimeMillis();
		try
		{
			if (galleryName.length() > 30 || urlComplex.length() > 36 || userName.length() > 30)
			{
				String message = "GetUserForGallery was passed an invalid argument. UserName: " + userName + " GalleryName:" + galleryName + " UrlComplex:" + urlComplex;
				meLogger.warn(message);
				return -1;
			}

			return galleryDataHelper.GetGalleryUserId(userName, galleryName, urlComplex);
		}
		catch (WallaException wallaEx) {
			return -1;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return -1;
		}
		finally {UserTools.LogMethod("GetUserForGallery", meLogger, startMS, userName + " " + galleryName);}
	}
	
	public void ResetGallerySectionForPreview(Gallery gallery)
	{
		if (gallery.getSections() != null)
		{
			if (gallery.getSections().getSectionRef().size() > 0)
			{
				for (int i = 0; i < gallery.getSections().getSectionRef().size(); i++)
				{
					Gallery.Sections.SectionRef current = gallery.getSections().getSectionRef().get(i);
					current.setId((long)(i+1));
				}
				
				//TODO sort these bad boys by sequence. 
			}
		}
	}
	
	public Gallery GetGalleryMeta(long userId, String galleryName, CustomResponse customResponse)
	{
		long startMS = System.currentTimeMillis();
		try {
			//Get gallery list for response.
			Gallery gallery = galleryDataHelper.GetGalleryMeta(userId, galleryName);
			if (gallery == null)
			{
				meLogger.warn("GetGalleryMeta didn't return a valid Gallery object");
				customResponse.setResponseCode(HttpStatus.NOT_FOUND.value());
				return null;
			}

			customResponse.setResponseCode(HttpStatus.OK.value());
			return gallery;
		}
		catch (WallaException wallaEx) {
			customResponse.setResponseCode(wallaEx.getCustomStatus());
			return null;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		finally {UserTools.LogMethod("GetGalleryMeta", meLogger, startMS, String.valueOf(userId) + " " + galleryName);}
	}

	public GalleryList GetGalleryListForUser(long userId, Date clientVersionTimestamp, CustomResponse customResponse)
	{
		long startMS = System.currentTimeMillis();
		try {
			GalleryList galleryList = null;
			Date lastUpdate = galleryDataHelper.LastGalleryListUpdate(userId);
			if (lastUpdate == null)
			{
				meLogger.warn("Last updated date for gallery could not be retrieved.");
				customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return null;
			}
			
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
			
			return galleryList;
		}
		catch (WallaException wallaEx) {
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		finally {UserTools.LogMethod("GetGalleryListForUser", meLogger, startMS, String.valueOf(userId));}
	}

	public long GetDefaultGallery(long userId, int appId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
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
			throw wallaEx;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			throw ex;
		}
		finally { UserTools.LogMethod("GetDefaultGallery", meLogger, startMS, String.valueOf(userId) + " " + String.valueOf(appId)); }
	}
	
	public GalleryOptions GetGalleryOptions(long userId, Date clientVersionTimestamp, CustomResponse customResponse)
	{
		long startMS = System.currentTimeMillis();
		try {
			Date latestDate = new Date();
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.YEAR, -10);
			latestDate.setTime(cal.getTimeInMillis());
			
			//Get Presentation and Style objects from memory.
			List<Presentation> presentations = cachedData.GetPresentationList();
			if (presentations == null)
			{
				meLogger.debug("No gallery options list generated because available presentations could not be retrieved.");
				customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return null;
			}
			
			List<Style> style = cachedData.GetStyleList();
			if (style == null)
			{
				meLogger.debug("No gallery options list generated because available styles could not be retrieved.");
				customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return null;
			}
			
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
				ref.setMaxImagesInSection(current.getMaxImagesInSection());
				
				ref.setOptionGalleryName(current.getOptionGalleryName());
				ref.setOptionGalleryDesc(current.getOptionGalleryDesc());
				ref.setOptionImageName(current.getOptionImageName());
				ref.setOptionImageDesc(current.getOptionImageDesc());
				ref.setOptionGroupingDesc(current.getOptionGroupingDesc());
				
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
				if (meLogger.isDebugEnabled()) {meLogger.debug("No gallery options list generated because client list is up to date.");}
				customResponse.setResponseCode(HttpStatus.NOT_MODIFIED.value());
				return null;
			}
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		finally {UserTools.LogMethod("GetGalleryOptions", meLogger, startMS, "");}
	}

	public Gallery GetGallerySections(long userId, Gallery requestGallery, CustomResponse customResponse)
	{
		long startMS = System.currentTimeMillis();
		try {
			long newTempGalleryId = utilityDataHelper.GetNewId("TempGalleryId");
			
			Gallery gallery = galleryDataHelper.GetGallerySections(userId, requestGallery, newTempGalleryId);

			customResponse.setResponseCode(HttpStatus.OK.value());
			return gallery;
		}
		catch (WallaException wallaEx) {
			customResponse.setResponseCode(wallaEx.getCustomStatus());
			return null;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		finally {UserTools.LogMethod("GetGallerySections", meLogger, startMS, String.valueOf(userId));}
	}
	
	public Presentation GetPresentation(int presentationId) throws WallaException
	{
		return cachedData.GetPresentation(presentationId);
	}
	
	public Style GetStyle(int styleId) throws WallaException
	{
		return cachedData.GetStyle(styleId);
	}
	//*************************************************************************************************************
	//*************************************  Messaging initiated methods ******************************************
	//*************************************************************************************************************

	public void RefreshGalleryImages(long userId, long galleryId)
	{
		long startMS = System.currentTimeMillis();
		try
		{
			galleryDataHelper.RegenerateGalleryImages(userId, galleryId);
		}
		catch (WallaException wallaEx) {
			meLogger.error("RefreshGalleryImages failed with an error");
		}
		catch (Exception ex) {
			meLogger.error("RefreshGalleryImages failed with an error", ex);
		}
		finally {UserTools.LogMethod("RefreshGalleryImages", meLogger, startMS, String.valueOf(userId) + " " + String.valueOf(galleryId));}
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
