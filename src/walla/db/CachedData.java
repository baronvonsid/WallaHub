package walla.db;

import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.utils.UserTools;
import walla.utils.WallaException;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

@Repository
public class CachedData {

	private Date cacheUpdateTime = new Date();
	private List<Platform> platforms = null;
	private List<App> apps = null;
	private List<Style> styles = null;
	private List<Presentation> presentations = null;
	private UtilityDataHelperImpl utilityDataHelper;
	private static final Logger meLogger = Logger.getLogger(CachedData.class);
	
	public CachedData() {
		//Date cacheUpdateTime = new Date();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		cacheUpdateTime.setTime(cal.getTimeInMillis());
		
		if (meLogger.isDebugEnabled()) { meLogger.debug("CachedData object instantiated with the timestamp:" + cacheUpdateTime.toGMTString()); }
	}

	private synchronized void CheckAndUpdateCache() throws WallaException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, -1);
	
			if (cacheUpdateTime.before(cal.getTime()))
			{
				//Cache is out of date, so retrieve the latest.
				platforms = utilityDataHelper.GetPlatformList();
				apps = utilityDataHelper.GetAppList();
				styles = utilityDataHelper.GetStyleList();
				presentations = utilityDataHelper.GetPresentationList();
				Calendar calNow = Calendar.getInstance();
				cacheUpdateTime.setTime(calNow.getTimeInMillis());
				
				if (meLogger.isDebugEnabled()) { meLogger.debug("Cache has now been refreshed.  New timestamp:" + cacheUpdateTime.toGMTString()); }
			}
		}
		finally { UserTools.LogMethod("CheckAndUpdateCache", meLogger, startMS, ""); }
	}
	
	public Platform GetPlatform(int platformId, String OSType, String machineType, int majorVersion, int minorVersion) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			CheckAndUpdateCache();
			
			//find platform object and return.
			for (Iterator<Platform> platformIterater = platforms.iterator(); platformIterater.hasNext();)
			{
				Platform currentPlatform = (Platform)platformIterater.next();
				
				if (platformId != 0)
				{
					//Specific lookup required.
					if (currentPlatform.getPlatformId() == platformId && currentPlatform.getSupported() == true)
					{
						return currentPlatform;
					}
				}
				else
				{
					//Try to find based on logic.  Needs to be improved when additional apps loaded.
					if (machineType.equals(currentPlatform.getMachineType()) && majorVersion == currentPlatform.getMajorVersion() && minorVersion == currentPlatform.getMinorVersion() && currentPlatform.getSupported() == true)
					{
						return currentPlatform;
					}
				}
			}
			
			return null;
		}
		finally { UserTools.LogMethod("GetPlatform", meLogger, startMS, "PlatformId:" + platformId + " OSType:" + OSType + " Machine:" + machineType + " Version:" + majorVersion + "." + minorVersion); }
	}
	
	public App GetApp(int appId, String key) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			CheckAndUpdateCache();
			
			//find platform object and return.
			for (Iterator<App> appIterater = apps.iterator(); appIterater.hasNext();)
			{
				App currentApp = (App)appIterater.next();

				if (appId != 0)
				{
					if (currentApp.getAppId() == appId)
					{
						return currentApp;
					}
				}
				else
				{
					if (currentApp.getWSKey().equals(key))
					{
						return currentApp;
					}
				}
			}
			
			return null;
		}
		finally { UserTools.LogMethod("GetApp", meLogger, startMS, "AppId:" + appId + " Key:" + key); }
	}
	
	public List<Style> GetStyleList() throws WallaException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			CheckAndUpdateCache();
			return styles;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetStyleList", ex);
			throw new WallaException(ex, 0);
		}
		finally { UserTools.LogMethod("GetStyleList", meLogger, startMS, ""); }
	}
	
	public Style GetStyle(int styleId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			CheckAndUpdateCache();
			
			for (Iterator<Style> iterater = styles.iterator(); iterater.hasNext();)
			{
				Style current = (Style)iterater.next();

				if (current.getStyleId() == styleId)
				{
					return current;
				}
			}
			
			//Presentation not found so raise an exception
			String error = "Style is not valid.  StyleId:" + styleId;
			meLogger.error(error);
			throw new WallaException("CachedData", "GetStyle", error, HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally { UserTools.LogMethod("GetApp", meLogger, startMS, ""); }
	}
	
	public List<Presentation> GetPresentationList() throws WallaException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			CheckAndUpdateCache();
			return presentations;
		}
		finally { UserTools.LogMethod("GetPresentationList", meLogger, startMS, ""); }
	}
	
	public Presentation GetPresentation(int presentationId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			CheckAndUpdateCache();
			
			for (Iterator<Presentation> iterater = presentations.iterator(); iterater.hasNext();)
			{
				Presentation current = (Presentation)iterater.next();

				if (current.getPresentationId() == presentationId)
				{
					return current;
				}
			}
			
			//Presentation not found so raise an exception
			String error = "Presentation is not valid.  PresentationId:" + presentationId;
			meLogger.error(error);
			throw new WallaException("CachedData", "GetPresentation", error, HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally { UserTools.LogMethod("GetPresentation", meLogger, startMS, String.valueOf(presentationId)); }
	}
	
	public void setUtilityDataHelper(UtilityDataHelperImpl utilityDataHelper)
	{
		this.utilityDataHelper = utilityDataHelper;
	}
	
}
