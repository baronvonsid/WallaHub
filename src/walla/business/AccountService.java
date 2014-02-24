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

	public int CreateAccount(Account newAccount)
	{
		try 
		{
			meLogger.debug("CreateAccount() begins.  Email: " + newAccount.getEmail());

			if (!UserTools.ValidEmailAddress(newAccount.getEmail()))
			{
				String error = "Account create failed, email doesn't fit a standard form.  Email:" + newAccount.getEmail();
				meLogger.error(error);
				return HttpStatus.BAD_REQUEST.value();
			}
			
			if (!UserTools.CheckPasswordStrength(newAccount.getPassword()))
			{
				String error = "Account create failed, password does not meet minimum complexity rules." + newAccount.getPassword();
				meLogger.error(error);
				return HttpStatus.BAD_REQUEST.value();
			}
			
			if (!accountDataHelper.CheckProfileNameIsUnique(newAccount.getProfileName()))
			{
				String error = "Profile name is already in use.  " + newAccount.getProfileName();
				meLogger.error(error);
				return HttpStatus.BAD_REQUEST.value();
			}

			long newUserId = accountDataHelper.CreateAccount(newAccount);
			newAccount.setId(newUserId);
			
			//TODO decouple.
			SendEmailConfirm(newUserId);
			
			meLogger.debug("CreateAccount() has completed.  Email: " + newAccount.getEmail() + " UserId:" + newUserId);
			
			return HttpStatus.CREATED.value();

		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process CreateAccount");
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces CreateNewAccount", ex);
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

	public long RegisterUserApp(long userId, UserApp newUserApp, CustomResponse customResponse)
	{
		try {
			//Check user can access tag list
			//HttpStatus.UNAUTHORIZED.value()
			
			meLogger.debug("RegisterUserApp() begins. UserId:" + userId);
			
			//Create new system owned tag.
			
			//Create new auto upload category. 
			
			//Get default gallery.
			
			//Get application, to use defaults.
			

			String error = "GetUserApp didn't return a valid UserApp object";
			meLogger.error(error);
			throw new WallaException("AccountService", "GetUserApp", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 
			
			//meLogger.debug("RegisterUserApp has completed. UserId:" + userId);
			
			//customResponse.setResponseCode(HttpStatus.OK.value());
			//return newUserApp.getId();
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process RegisterUserApp", wallaEx);
			customResponse.setResponseCode(wallaEx.getCustomStatus());
			return 0;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to process RegisterUserApp",ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return 0;
		}
	}

	
	public UserApp GetUserApp(long userId, long userAppId, CustomResponse customResponse)
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
				throw new WallaException("AccountService", "GetUserApp", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 
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
	
	public void setCachedData(CachedData cachedData)
	{
		this.cachedData = cachedData;
	}
	
	public void setUtilityDataHelper(UtilityDataHelperImpl utilityDataHelper)
	{
		this.utilityDataHelper = utilityDataHelper;
	}
}
