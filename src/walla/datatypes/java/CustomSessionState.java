package walla.datatypes.java;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import walla.datatypes.auto.*;

public class CustomSessionState {

	private String profileName = "";
	private long userId = -1;
	private ArrayList<Long> uploadReceivedImageIds = null;
	private long userAppId = -1;
	private int platformId = -1;
	private int appId = -1;
	private boolean isHuman = false;
	private boolean isAuthenticated = false;
	private Gallery galleryPreview = null;
	private String galleryTempKey = "";
	
	
	private ArrayList<String> customSessionIds = null;
	private String nonceKey = "";
	private int failedLogonCount = 0;
	private Date failedLogonLast = null;
	private String remoteAddress = "";
	
	public String getProfileName()
	{
		return this.profileName;
	}
	
	public void setProfileName(String value)
	{
		this.profileName = value;
	}
	
	public long getUserId()
	{
		return this.userId;
	}
	
	public void setUserId(long value)
	{
		this.userId = value;
	}
	
    public List<Long> getUploadFilesReceived() {
        if (uploadReceivedImageIds == null) {
        	uploadReceivedImageIds = new ArrayList<Long>();
        }
        return this.uploadReceivedImageIds;
    }
	    
	public long getUserAppId()
	{
		return this.userAppId;
	}
	
	public void setUserAppId(long value)
	{
		this.userAppId = value;
	}
	
	public int getPlatformId()
	{
		return this.platformId;
	}
	
	public void setPlatformId(int value)
	{
		this.platformId = value;
	}
	
	public int getAppId()
	{
		return this.appId;
	}
	
	public void setAppId(int appId)
	{
		this.appId = appId;
	}
	
	public boolean isHuman()
	{
		return this.isHuman;
	}
	
	public void setHuman(boolean value)
	{
		this.isHuman = value;
	}
	
	public boolean isAuthenticated()
	{
		return this.isAuthenticated;
	}
	
	public void setAuthenticated(boolean isAuthenticated)
	{
		this.isAuthenticated = isAuthenticated;
	}
	
	public Gallery getGalleryPreview()
	{
		return this.galleryPreview;
	}
	
	public void setGalleryPreview(Gallery galleryPreview)
	{
		this.galleryPreview = galleryPreview;
	}
	
	public String getGalleryTempKey()
	{
		return this.galleryTempKey;
	}
	
	public void setGalleryTempKey(String value)
	{
		this.galleryTempKey = value;
	}
	
    public List<String> getCustomSessionIds() {
        if (customSessionIds == null) {
        	customSessionIds = new ArrayList<String>();
        }
        return this.customSessionIds;
    }
    
	public String getNonceKey()
	{
		return this.nonceKey;
	}
	
	public void setNonceKey(String value)
	{
		this.nonceKey = value;
	}
	
	public int getFailedLogonCount()
	{
		return this.failedLogonCount;
	}
	
	public void setFailedLogonCount(int value)
	{
		this.failedLogonCount = value;
	}

	public Date getFailedLogonLast()
	{
		return this.failedLogonLast;
	}
	
	public void setFailedLogonLast(Date value)
	{
		this.failedLogonLast = value;
	}
	
	public String getRemoteAddress()
	{
		return this.remoteAddress;
	}
	
	public void setRemoteAddress(String value)
	{
		this.remoteAddress = value;
	}
	
	
}
