package walla.db;

import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.utils.WallaException;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
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
		
		meLogger.debug("CachedData object instantiated with the timestamp:" + cacheUpdateTime.toGMTString());
	}

	private synchronized void CheckAndUpdateCache() throws WallaException
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
			
			meLogger.debug("Cache has now been refreshed.  New timestamp:" + cacheUpdateTime.toGMTString());
		}
	}
	
	public Platform GetPlatform(int platformId, String OSType, String machineType, int majorVersion, int minorVersion) throws WallaException
	{
		try
		{
			meLogger.debug("GetPlatform has been started.  PlatformId:" + platformId + " OSType:" + OSType + " Machine:" + machineType + " Version:" + majorVersion + "." + minorVersion);
			
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
			
			//Platform not found so rise an exception
			String error = "Platform is not valid.  PlatformId:" + platformId + " OSType:" + OSType + " Machine:" + machineType + " Version:" + majorVersion + "." + minorVersion;
			meLogger.error(error);
			throw new WallaException(this.getClass().getName(), "GetPlatform", error, 0);
		}
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetPlatform", ex);
			throw new WallaException(ex, 0);
		}
	}
	
	public App GetApp(int appId, String key) throws WallaException
	{
		try
		{
			meLogger.debug("GetApp has been started.  AppId:" + appId);
			
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
			
			//App not found so raise an exception
			String error = "App is not valid.  AppId:" + appId;
			meLogger.error(error);
			throw new WallaException(this.getClass().getName(), "GetApp", error, 0);
		}
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetApp", ex);
			throw new WallaException(ex, 0);
		}
	}
	
	public List<Style> GetStyleList() throws WallaException
	{
		try
		{
			meLogger.debug("GetStyleList has been started");
			
			CheckAndUpdateCache();
			
			meLogger.debug("GetStyleList has been completed");
			
			return styles;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetStyleList", ex);
			throw new WallaException(ex, 0);
		}
	}
	
	public Style GetStyle(int styleId) throws WallaException
	{
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
			throw new WallaException(this.getClass().getName(), "GetStyle", error, 0);
		}
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetStyle", ex);
			throw new WallaException(ex, 0);
		}
	}
	
	public List<Presentation> GetPresentationList() throws WallaException
	{
		try
		{
			meLogger.debug("GetPresentationList has been started");
			
			CheckAndUpdateCache();
			
			meLogger.debug("GetPresentationList has been completed");
			
			return presentations;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetPresentationList", ex);
			throw new WallaException(ex, 0);
		}
	}
	
	public Presentation GetPresentation(int presentationId) throws WallaException
	{
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
			throw new WallaException(this.getClass().getName(), "GetPresentation", error, 0);
		}
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetPresentation", ex);
			throw new WallaException(ex, 0);
		}
	}
	
	public void setUtilityDataHelper(UtilityDataHelperImpl utilityDataHelper)
	{
		this.utilityDataHelper = utilityDataHelper;
	}
	
}
