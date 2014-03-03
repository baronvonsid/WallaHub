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

@Service("AccountService")
public class AccountService {

	private AccountDataHelperImpl accountDataHelper;
	private UtilityDataHelperImpl utilityDataHelper;
	private TagService tagService;
	private CategoryService categoryService;
	private GalleryService galleryService;
	
	private CachedData cachedData;
	
	private static final Logger meLogger = Logger.getLogger(AccountService.class);

	//*************************************************************************************************************
	//***********************************  Web server synchronous methods *****************************************
	//*************************************************************************************************************
	
	/* Account setup sequence and status

		1 - initial details received
		2 - email confirmed 
		3 - billing confirmed
		4 - billing issue
		5 - live
		6 - shutdown pending
		7 - closed
	 */
	
	//Create Account (Brief details) + Email.
	//Collect bank details and personal contact information (if paid account)
	//Send out confirmation email.
	//Receive confirmation email. (Link clicked)
	//Account open
	//Account close requested
	//Account close completed

	public int CreateUpdateAccount(long userId, Account account)
	{
		try 
		{
			if (userId == 0)
			{
				meLogger.debug("CreateUpdateAccount() begins.  New Account.  Email: " + account.getEmail());
				
				//Create new account
				if (!UserTools.ValidEmailAddress(account.getEmail()))
				{
					String error = "Account create failed, email doesn't fit a standard form.  Email:" + account.getEmail();
					meLogger.error(error);
					return HttpStatus.BAD_REQUEST.value();
				}
				
				if (!UserTools.CheckPasswordStrength(account.getPassword()))
				{
					String error = "Account create failed, password does not meet minimum complexity rules." + account.getPassword();
					meLogger.error(error);
					return HttpStatus.BAD_REQUEST.value();
				}
				
				if (!accountDataHelper.CheckProfileNameIsUnique(account.getProfileName()))
				{
					String error = "Profile name is already in use.  " + account.getProfileName();
					meLogger.error(error);
					return HttpStatus.BAD_REQUEST.value();
				}
	
				long newUserId = accountDataHelper.CreateAccount(account);
				account.setId(newUserId);
				
				//TODO decouple.
				SendEmailConfirm(newUserId);
				
				meLogger.debug("CreateUpdateAccount() has created a new account.  Email: " + account.getEmail() + " UserId:" + newUserId);
				return HttpStatus.CREATED.value();
			}
			else
			{
				meLogger.debug("CreateUpdateAccount() begins.  Existing Account.  UserId: " + userId);
				
				//Create new account
				if (userId != account.getId())
				{
					String error = "Account update failed, user ids don't match.  UserId: " + userId;
					meLogger.error(error);
					return HttpStatus.BAD_REQUEST.value();
				}
				
				if (!UserTools.CheckPasswordStrength(account.getPassword()))
				{
					String error = "Account update failed, password does not meet minimum complexity rules." + account.getPassword();
					meLogger.error(error);
					return HttpStatus.BAD_REQUEST.value();
				}
	
				accountDataHelper.UpdateAccount(userId, account);

				meLogger.debug("CreateUpdateAccount() has updated the account.  UserId:" + userId);
				return HttpStatus.OK.value();
			}
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process CreateUpdateAccount");
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to process CreateUpdateAccount", ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
	}

	public Account GetAccount(long userId, CustomResponse customResponse)
	{
		try {
			//Check user can access tag list
			//HttpStatus.UNAUTHORIZED.value()
			
			meLogger.debug("GetAccount() begins. UserId:" + userId);
			
			Account account = accountDataHelper.GetAccount(userId);
			if (account == null)
			{
				String error = "GetAccount didn't return a valid Account object";
				meLogger.error(error);
				throw new WallaException("AccountService", "GetAccount", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 
			}
			
			meLogger.debug("GetAccount has completed. UserId:" + userId);
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			return account;
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process GetAccount", wallaEx);
			customResponse.setResponseCode(wallaEx.getCustomStatus());
			return null;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to process GetAccount",ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}
	
	public int AckEmailConfirm(long userId, String queryString)
	{
		//TODO Check query string validity
		
		//Decode 
		return 0;
	}

	public long CreateUserApp(long userId, int appId, int platformId, UserApp proposedUserApp, CustomResponse customResponse)
	{
		try {
			//Check user can access tag list
			//HttpStatus.UNAUTHORIZED.value()
			
			meLogger.debug("CreateUserApp() begins. UserId:" + userId);
			
			UserApp newUserApp = new UserApp();
			long userAppId = utilityDataHelper.GetNewId("UserAppId");
			
			App app = accountDataHelper.GetApp(appId, "");
			newUserApp.setId(userAppId);
			newUserApp.setFetchSize(app.getDefaultFetchSize());
			newUserApp.setThumbCacheSizeMB(app.getDefaultThumbCacheMB());
			newUserApp.setMainCopyCacheSizeMB(app.getDefaultMainCopyCacheMB());
			newUserApp.setAutoUpload(false);
			newUserApp.setAutoUploadFolder("");
			
			//Create or find new userapp tag (system owned).
			newUserApp.setTagId(tagService.CreateOrFindUserAppTag(userId, platformId, proposedUserApp.getMachineName()));
			
			//Create new auto upload category. 
			newUserApp.setCategoryId(categoryService.CreateOrFindUserAppCategory(userId, platformId, newUserApp.getMachineName()));
			
			//Get default gallery.
			newUserApp.setGalleryId(galleryService.GetDefaultGallery(appId));
			
			if (!proposedUserApp.getMachineName().isEmpty())
				newUserApp.setMachineName(proposedUserApp.getMachineName());
			
			if (proposedUserApp.isAutoUpload())
				newUserApp.setAutoUpload(true);
			
			if (!proposedUserApp.getAutoUploadFolder().isEmpty())
				newUserApp.setAutoUploadFolder(proposedUserApp.getAutoUploadFolder());
			
			if (proposedUserApp.getMainCopyCacheSizeMB() != 0)
				newUserApp.setMainCopyCacheSizeMB(proposedUserApp.getMainCopyCacheSizeMB());

			if (proposedUserApp.getThumbCacheSizeMB() != 0)
				newUserApp.setThumbCacheSizeMB(proposedUserApp.getThumbCacheSizeMB());
			
			if (!proposedUserApp.getMainCopyFolder().isEmpty())
				newUserApp.setMainCopyFolder(proposedUserApp.getMainCopyFolder());
			
			accountDataHelper.CreateUserApp(userId, newUserApp);

			meLogger.debug("CreateUserApp has completed. UserId:" + userId + " UserAppId:" + userAppId);
			
			customResponse.setResponseCode(HttpStatus.CREATED.value());
			return userAppId;
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process CreateUserApp", wallaEx);
			customResponse.setResponseCode(wallaEx.getCustomStatus());
			return 0;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to process CreateUserApp",ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return 0;
		}
	}

	public void UpdateUserApp(long userId, int appId, int platformId, UserApp updatedUserApp, CustomResponse customResponse)
	{
		try {
			//Check user can access tag list
			//HttpStatus.UNAUTHORIZED.value()
			
			meLogger.debug("UpdateUserApp() begins. UserId:" + userId + " UserAppId:" + updatedUserApp.getId());

			UserApp userApp = accountDataHelper.GetUserApp(userId, updatedUserApp.getId());
			if (userApp == null)
			{
				String error = "UpdateUserApp didn't return a valid UserApp object";
				throw new WallaException("AccountService", "UpdateUserApp", error, HttpStatus.NOT_FOUND.value()); 
			}
			
			if (platformId != userApp.getPlatformId())
			{
				String error = "Account update failed, platforms do not match.  PlatformId:" + platformId;
				throw new WallaException("AccountService", "UpdateUserApp", error, HttpStatus.BAD_REQUEST.value()); 
			}
			
			if (appId != userApp.getPlatformId())
			{
				String error = "Account update failed, apps do not match.  PlatformId:" + platformId;
				throw new WallaException("AccountService", "UpdateUserApp", error, HttpStatus.BAD_REQUEST.value()); 
			}
			
			//Ensure correct platformId and appId is used
			updatedUserApp.setPlatformId(platformId);
			updatedUserApp.setAppId(appId);
			
			if (!userApp.getMachineName().equalsIgnoreCase(updatedUserApp.getMachineName()))
			{
				//Create or find new userapp tag (system owned).
				updatedUserApp.setTagId(tagService.CreateOrFindUserAppTag(userId, platformId, userApp.getMachineName()));
				
				//Create new auto upload category. 
				updatedUserApp.setCategoryId(categoryService.CreateOrFindUserAppCategory(userId, platformId, userApp.getMachineName()));
			}
			
			accountDataHelper.UpdateUserApp(userId, updatedUserApp);

			meLogger.debug("RegisterUserApp has completed. UserId:" + userId + " UserAppId:" + updatedUserApp.getId());
			
			customResponse.setResponseCode(HttpStatus.OK.value());
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process UpdateUserApp", wallaEx);
			customResponse.setResponseCode(wallaEx.getCustomStatus());
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to process UpdateUserApp",ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
	
	public UserApp GetUserApp(long userId, int appId, int platformId, long userAppId, CustomResponse customResponse)
	{
		try {
			//Check user can access tag list
			//HttpStatus.UNAUTHORIZED.value()
			
			meLogger.debug("GetUserApp() begins. UserId:" + userId);
			
			UserApp userApp = accountDataHelper.GetUserApp(userId, userAppId);
			if (userApp == null)
			{
				String error = "GetUserApp didn't return a valid UserApp object";
				meLogger.error(error);
				throw new WallaException("AccountService", "GetUserApp", error, HttpStatus.NOT_FOUND.value()); 
			}
			
			//Check the userapp is still relevent for the platform.
			if (userApp.getPlatformId() != platformId)
			{
				//Register new userapp, the platform has changed.  This could either be an upgrade, name change or copying config.
				//Use existing app as a starting point.
				meLogger.debug("Platforms don't match, create a new platform. UserId:" + userId + " PlatformId:" + platformId);
				
				UserApp newUserApp = new UserApp();
				newUserApp.setAutoUpload(userApp.isAutoUpload());
				newUserApp.setAutoUploadFolder(userApp.getAutoUploadFolder());
				newUserApp.setThumbCacheSizeMB(userApp.getThumbCacheSizeMB());
				
				long newUserAppId = CreateUserApp(userId, appId, platformId, newUserApp, customResponse);
				
				userApp = accountDataHelper.GetUserApp(userId, newUserAppId);
				if (userApp == null)
				{
					String error = "GetUserApp didn't return a valid UserApp object";
					meLogger.error(error);
					throw new WallaException("AccountService", "GetUserApp", error, HttpStatus.NOT_FOUND.value()); 
				}
			}
			
			meLogger.debug("GetUserApp has completed. UserId:" + userId);
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			return userApp;
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process GetUserApp", wallaEx);
			customResponse.setResponseCode(wallaEx.getCustomStatus());
			return null;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to process GetUserApp",ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}
	
	public boolean CheckProfileNameIsUnique(String profileName) throws WallaException
	{
		try {
			meLogger.debug("CheckProfileNameIsUnique() is being run. Profile name:" + profileName);

			return accountDataHelper.CheckProfileNameIsUnique(profileName);
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process CheckProfileNameIsUnique", wallaEx);
			throw wallaEx;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to process GetUserApp",ex);
			throw ex;
		}
	}
	
	public int GetPlatformId(String OSType, String machineType, String majorVersion, String minorVersion, CustomResponse customResponse)
	{
		return 0;
		
		
		
	}
	
	
	/*
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
	*/
	
	//*************************************************************************************************************
	//*************************************  Messaging initiated methods ******************************************
	//*************************************************************************************************************
	
	public void SendEmailConfirm(long userId) 
	{
		try
		{

			
		}
		//catch (WallaException wallaEx) {
	//		meLogger.error("Unexpected error when trying to process SendEmailConfirmation");
		//}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces SendEmailConfirmation", ex);
		}
	}
	
	public void setAccountDataHelper(AccountDataHelperImpl accountDataHelper)
	{
		this.accountDataHelper = accountDataHelper;
	}
	
	public void setTagService(TagService tagService)
	{
		this.tagService = tagService;
	}
	
	public void setCachedData(CachedData cachedData)
	{
		this.cachedData = cachedData;
	}
	
	public void setCategoryService(CategoryService categoryService)
	{
		this.categoryService = categoryService;
	}
	
	public void setGalleryService(GalleryService galleryService)
	{
		this.galleryService = galleryService;
	}
	
	public void setUtilityDataHelper(UtilityDataHelperImpl utilityDataHelper)
	{
		this.utilityDataHelper = utilityDataHelper;
	}
}
