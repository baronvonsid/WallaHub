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

		Main
		1 - initial details setup
		2 - live (email and banking done)
		3 - shutdown pending
		4 - closed
		
		Email
		0 - email not sent
		1 - email sent 
		2 - email not confirmed in timely manner
		3 - email confirmed
		
		Banking
		0 - not setup
		1 - details received
		2 - validated
		3 - details need to be re-setup
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
				
				if (!accountDataHelper.ProfileNameIsUnique(account.getProfileName()))
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
				
				//Check name is the same
				//if (userId != account.getId())
				//{
				//	String error = "Account update failed, user ids don't match.  UserId: " + userId;
				//	meLogger.error(error);
				//	return HttpStatus.BAD_REQUEST.value();
				//}
				
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
				throw new WallaException("AccountService", "GetAccount", error, HttpStatus.BAD_REQUEST.value()); 
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
	
	public int AckEmailConfirm(String userName, String requestValidationString)
	{
		//TODO - expire email validation string
		try
		{
			String sql = "SELECT [UserId] FROM [dbo].[User] WHERE [ProfileName] = '" + userName + "'";
			long userId = utilityDataHelper.GetLong(sql);
			
			sql = "SELECT [ValidationString] FROM [dbo].[User] WHERE [EmailStatus] = 1 AND [UserId] = " + userId;
			String serverValidationString = utilityDataHelper.GetString(sql);
			if (serverValidationString.equals(requestValidationString))
			{

				
				accountDataHelper.UpdateEmailStatus(userId, 3, "");;
				//Check if banking is all done and if so, mark the account as Live.
				/*
				sql = "SELECT [BankingStatus] FROM [User] WHERE UserId = " + userId;
				int bankingStatus = utilityDataHelper.GetInt(sql);
				if (bankingStatus == 2)
				{
					accountDataHelper.UpdateMainStatus(userId, 2);
				}
				*/
				return HttpStatus.OK.value();
			}
			else
			{
				meLogger.error("AckEmailConfirm - Validation string didn't match for account: " + userId);
				return HttpStatus.BAD_REQUEST.value();
			}
		}
		catch (WallaException wallaEx)
		{
			meLogger.error(wallaEx);
			return HttpStatus.BAD_REQUEST.value();
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
	}

	public long CreateUserApp(long userId, int appId, int platformId, UserApp proposedUserApp, CustomResponse customResponse)
	{
		try {
			meLogger.debug("CreateUserApp() begins. UserId:" + userId);
			
			if (proposedUserApp.getMachineName() == null || proposedUserApp.getMachineName().isEmpty())
			{
				String error = "CreateUserApp didn't receive a machine name, this is mandatory.";
				throw new WallaException("AccountService", "CreateUserApp", error, HttpStatus.BAD_REQUEST.value()); 
			}
			
			long userAppId = accountDataHelper.FindExistingUserApp(userId, appId, platformId, proposedUserApp.getMachineName());
			if (userAppId > 0)
			{
				customResponse.setResponseCode(HttpStatus.CREATED.value());
				return userAppId;
			}
			
			UserApp newUserApp = new UserApp();
			userAppId = utilityDataHelper.GetNewId("UserAppId");

			App app = cachedData.GetApp(appId, "");
			newUserApp.setId(userAppId);
			newUserApp.setAppId(appId);
			newUserApp.setPlatformId(platformId);
			newUserApp.setFetchSize(app.getDefaultFetchSize());
			newUserApp.setThumbCacheSizeMB(app.getDefaultThumbCacheMB());
			newUserApp.setMainCopyCacheSizeMB(app.getDefaultMainCopyCacheMB());
			newUserApp.setAutoUpload(false);
			newUserApp.setAutoUploadFolder("");
			newUserApp.setMainCopyFolder("");
			newUserApp.setMachineName(proposedUserApp.getMachineName());

			//Create or find new userapp tag (system owned).
			newUserApp.setTagId(tagService.CreateOrFindUserAppTag(userId, platformId, proposedUserApp.getMachineName()));
			
			//Create new auto upload category. 
			newUserApp.setUserAppCategoryId(categoryService.CreateOrFindUserAppCategory(userId, platformId, newUserApp.getMachineName()));
			
			//Default user category
			newUserApp.setUserDefaultCategoryId(categoryService.FindDefaultUserCategory(userId));
			
			//Get default gallery.
			newUserApp.setGalleryId(galleryService.GetDefaultGallery(userId, appId));
			
			if (proposedUserApp.isAutoUpload())
				newUserApp.setAutoUpload(true);
			
			if (proposedUserApp.getAutoUploadFolder() != null && !proposedUserApp.getAutoUploadFolder().isEmpty())
				newUserApp.setAutoUploadFolder(proposedUserApp.getAutoUploadFolder());
			
			if (proposedUserApp.getMainCopyFolder() != null && !proposedUserApp.getMainCopyFolder().isEmpty())
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
			
			if (appId != updatedUserApp.getAppId())
			{
				String error = "Account update failed, apps do not match.  AppId:" + appId;
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
				updatedUserApp.setUserAppCategoryId(categoryService.CreateOrFindUserAppCategory(userId, platformId, userApp.getMachineName()));
			}
			
			accountDataHelper.UpdateUserApp(userId, updatedUserApp);

			meLogger.debug("UpdateUserApp has completed. UserId:" + userId + " UserAppId:" + updatedUserApp.getId());
			
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
				newUserApp.setMainCopyFolder(userApp.getMainCopyFolder());
				newUserApp.setMainCopyCacheSizeMB(userApp.getMainCopyCacheSizeMB());
				
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

			return accountDataHelper.ProfileNameIsUnique(profileName);
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
	
	public int GetPlatformId(String OS, String machine, int major, int minor, CustomResponse customResponse)
	{
		try
		{
			Platform platform = cachedData.GetPlatform(0, OS, machine, major, minor);
			customResponse.setResponseCode(HttpStatus.OK.value());
			return platform.getPlatformId();
		}
		catch (WallaException wallaEx)
		{
			meLogger.error(wallaEx);
			customResponse.setResponseCode(HttpStatus.NOT_ACCEPTABLE.value());
			return 0;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return 0;
		}
	}
	
	public int VerifyApp(String wsKey, CustomResponse customResponse)
	{
		//Check for key existing in Walla	
		//If not, then send back not found message
		//If exists - but retired, then send back not acceptable message
		//Else send back OK.
		try
		{
			App app = cachedData.GetApp(0, wsKey);
			if (app.getStatus() != 1)
			{
				customResponse.setResponseCode(HttpStatus.NOT_ACCEPTABLE.value());
				return 0;
			}
			customResponse.setResponseCode(HttpStatus.OK.value());
			return app.getAppId();
		}
		catch (WallaException wallaEx)
		{
			meLogger.error(wallaEx);
			customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
			return 0;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return 0;
		}
	}

	public long LogonCheck(String userName)
	{
		try
		{
			//TODO - Do it!
			String sql = "SELECT [UserId] FROM [dbo].[User] WHERE [ProfileName] = '" + userName + "'";
			long userId = utilityDataHelper.GetLong(sql);
			return userId;
		}
		catch (WallaException wallaEx)
		{
			meLogger.error(wallaEx);
			return -1;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return -1;
		}
	}

	
	//*************************************************************************************************************
	//*************************************  Messaging initiated methods ******************************************
	//*************************************************************************************************************
	
	public void SendEmailConfirm(long userId) 
	{
		try
		{
			String validationString = UserTools.GetComplexString();
			accountDataHelper.UpdateEmailStatus(userId, 1, validationString.substring(0,30));
			
			//TODO actually send email.
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process SendEmailConfirmation");
		}
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
