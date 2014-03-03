package walla.datatypes.java;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import walla.datatypes.auto.*;

public class CustomSessionState {

	private String userName;
	private long userId;
	private UploadStatusList uploadStatusList;
	private boolean initUploadList = false;
	private long userAppId = 100001;
	private int platformId = 100;
	private int appId = 100;
	
	//private List<UploadStatusList.ImageUploadRef> uploadImageList;

	public String getUserName()
	{
		return this.userName;
	}
	
	public void setUserName(String value)
	{
		this.userName = value;
	}
	
	public long getUserId()
	{
		return this.userId;
	}
	
	public void setUserId(long value)
	{
		this.userId = value;
	}
	
	public UploadStatusList getUploadStatusList()
	{
		if (uploadStatusList == null)
			{ uploadStatusList = new UploadStatusList(); }
		
		return this.uploadStatusList;
	}
	
	public boolean getInitUploadList()
	{
		return this.initUploadList;
	}
	
	public void setInitUploadList(boolean value)
	{
		initUploadList = value;
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
	/*
    public List<UploadStatusList.ImageUploadRef> getImageUploadRefList() {
        if (uploadImageList == null) {
        	uploadImageList = new ArrayList<UploadStatusList.ImageUploadRef>();
        }
        return this.uploadImageList;
    }
	*/
	
    /*
    
    public class ImageUploadRef {

        private long imageId;
        private int imageStatus;
        private Date lastUpdated = null;
        private String errorMessage;

        public long getImageId() {
            return imageId;
        }

        public void setImageId(long value) {
            this.imageId = value;
        }
        
        public int getImageStatus() {
            return imageStatus;
        }

        public void setImageStatus(int value) {
            this.imageStatus = value;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String value) {
            this.errorMessage = value;
        }
        
        public Date getLastUpdated()
        {
        	return this.lastUpdated;
        }
        
        public void setLastUpdated(Date value)
        {
        	this.lastUpdated = value;
        }
    }
    */
    
}
