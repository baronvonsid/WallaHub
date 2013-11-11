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
			platforms = utilityDataHelper.GetPlatformObjects();
			
			Calendar calNow = Calendar.getInstance();
			cacheUpdateTime.setTime(calNow.getTimeInMillis());
			
			meLogger.debug("Cache has now been refreshed.  New timestamp:" + cacheUpdateTime.toGMTString());
		}
	}
	
	public Platform GetPlatform(int platformId) throws WallaException
	{
		try
		{
			meLogger.debug("GetPlatform has been started.  PlatformId:" + platformId);
			
			CheckAndUpdateCache();
			
			//find platform object and return.
			for (Iterator<Platform> platformIterater = platforms.iterator(); platformIterater.hasNext();)
			{
				Platform currentPlatform = (Platform)platformIterater.next();
				if (currentPlatform.getPlatformId() == platformId)
				{
					return currentPlatform;
				}
			}
			
			//Platform not found so rise an exception
			String error = "Platform is not valid.  PlatformId:" + platformId;
			meLogger.error(error);
			throw new WallaException(this.getClass().getName(), "GetPlatform", error, 0);
		}
		catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected Exception in GetPlatform", ex);
			throw new WallaException(ex, 0);
		}
	}
	
	public void setUtilityDataHelper(UtilityDataHelperImpl utilityDataHelper)
	{
		this.utilityDataHelper = utilityDataHelper;
	}
	
}
