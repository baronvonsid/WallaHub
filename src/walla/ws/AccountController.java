package walla.ws;

import javax.validation.Valid;


import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Controller;
import org.w3c.dom.*;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import walla.datatypes.java.*;
import walla.business.*;
import walla.datatypes.auto.*;
import walla.utils.*;

	/*

	CreateUpdateAccount() PUT /{userName}
	GetAccount() GET /{userName}
	AckEmailConfirm() GET /{userName}/{validationString}
	CheckProfileName() GET /profilename/{profileName}
	
	CreateUpdateUserApp() PUT /{userName}/userapp
	GetUserAppMarkSession() GET /{userName}/userapp/{userAppId}
	
	VerifyApp() GET /appcheck?wsKey={wsKey}
 	SetPlatformForSession() POST /{userName}/platform?OS={OS}&machine={machineType}&major={major}&minor={minor}

	Not finished:
	//TODO add "find user app" method, in the case where the cache file is not present on the client
	Logon() POST /{userName}/logon
	ChangePassword()
	*/

@Controller
@RequestMapping("/ws")
public class AccountController {

	private static final Logger meLogger = Logger.getLogger(AccountController.class);

	@Autowired
	private CustomSessionState sessionState;
	
	@Autowired
	private AccountService accountService;
	
	//  PUT /{userName}
	@RequestMapping(value = { "/{userName}" }, method = { RequestMethod.PUT }, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void CreateUpdateAccount(
			@PathVariable("userName") String userName,
			@RequestBody Account account,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("CreateUpdateAccount request received.  Email: " + account.getEmail());}
			

			int responseCode = HttpStatus.UNAUTHORIZED.value();
			
			long userId = this.sessionState.getUserId();
			if (userId > 0)
			{
				//Only edits allowed.
				if (this.sessionState.getUserName().equalsIgnoreCase(userName) && this.sessionState.getUserName().equalsIgnoreCase(account.getProfileName()))
				{
					responseCode = accountService.CreateUpdateAccount(userId, account);
				}
				else
				{
					meLogger.error("CreateUpdateAccount request encountered a conflict between session userName: " 
						+ this.sessionState.getUserName() + ", the url userName: " + userName + " and the account profileName: "
						+ account.getProfileName());
				}
			}
			else
			{
				responseCode = accountService.CreateUpdateAccount(0, account);
			}

			httpResponse.setStatus(responseCode);
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("CreateUpdateAccount tag request completed, Email: " + account.getEmail() + " UserId: " + userId + " Response code: " + responseCode);}
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in CreateUpdateAccount", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
	
	//  GET - /{userName}
	@RequestMapping(value="/{userName}", method=RequestMethod.GET, 
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody Account GetAccount(
			@PathVariable("userName") String userName,
			HttpServletResponse httpResponse)
	{	
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetAccount request received, User:" + userName.toString());}
			
			if (this.sessionState.getUserId() < 0 || !userName.equalsIgnoreCase(this.sessionState.getUserName()))
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				if (meLogger.isDebugEnabled()) {meLogger.debug("GetAccount request not authorised, User:" + userName.toString());}
				return null;
			}
			
			CustomResponse customResponse = new CustomResponse();
			Account account = accountService.GetAccount(this.sessionState.getUserId(), customResponse);
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetAccount request completed, User:" + userName.toString());}
			
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(customResponse.getResponseCode());
			return account;
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in GetAccount", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}
	
	//  GET - /{userName}/email?valid={validationString}
	@RequestMapping(value="/{userName}/{validationString}", method=RequestMethod.GET, 
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void AckEmailConfirm(
			@PathVariable("userName") String userName,
			@RequestParam("validationString") String validationString,
			HttpServletResponse httpResponse)
	{	
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("AckEmailConfirm request received, User:" + userName.toString());}
			
			int response = accountService.AckEmailConfirm(userName, validationString);
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("AckEmailConfirm request completed, User:" + userName.toString());}
			
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(response);

			//TODO redirect to nice page.
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in AckEmailConfirm", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
	
	//  PUT /{userName}/userapp
	@RequestMapping(value = { "/{userName}/userapp" }, method = { RequestMethod.PUT }, produces=MediaType.TEXT_PLAIN_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody String CreateUpdateUserApp(
			@PathVariable("userName") String userName,
			@RequestBody UserApp userApp,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("CreateUpdateUserApp request received.  User:" + userName.toString());}

			if (this.sessionState.getUserId() < 0 || !userName.equalsIgnoreCase(this.sessionState.getUserName()))
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				if (meLogger.isDebugEnabled()) {meLogger.debug("CreateUpdateUserApp request failed, User:" + userName.toString() + ", Response code: " + HttpStatus.UNAUTHORIZED.value());}
				return "";
			}

			if (this.sessionState.getPlatformId() < 1)
			{
				//Platform must be set for this session to continue.
				httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
				if (meLogger.isDebugEnabled()) {meLogger.debug("CreateUpdateUserApp request failed because no platform was setup, User:" + userName.toString() + ", Response code: " + HttpStatus.BAD_REQUEST.value());}
				return "";
			}
			
			CustomResponse customResponse = new CustomResponse();
			
			long userAppId = userApp.getId();
			if (userAppId == 0)
			{
				userAppId = accountService.CreateUserApp(this.sessionState.getUserId(), this.sessionState.getAppId(), this.sessionState.getPlatformId(), userApp, customResponse);
			}
			else
			{
				accountService.UpdateUserApp(this.sessionState.getUserId(), this.sessionState.getAppId(), this.sessionState.getPlatformId(), userApp, customResponse);
			}
			
			httpResponse.setStatus(customResponse.getResponseCode());
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("CreateUpdateUserApp request completed, User:" + userName.toString() + ", Response code: " + customResponse.getResponseCode());}
			
			if (customResponse.getResponseCode() == HttpStatus.OK.value() || customResponse.getResponseCode() == HttpStatus.CREATED.value())
			{
				return "<UserAppId>" + userAppId + "</UserAppId>";
			}
			else
			{
				return "<UserAppId>0</UserAppId>";
			}
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in CreateUpdateUserApp", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return "<UserAppId>0</UserAppId>";
		}
	}
	
	
	
	
	// GET /{userName}/userapp/{userAppId}
	@RequestMapping(value = { "/{userName}/userapp/{userAppId}" }, method = { RequestMethod.GET }, 
			headers={"Accept-Charset=utf-8"}, produces=MediaType.APPLICATION_XML_VALUE )
	public @ResponseBody UserApp GetUserAppMarkSession(
			@PathVariable("userAppId") long userAppId,
			@PathVariable("userName") String userName,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetUserAppMarkSession request received, User:" + userName.toString());}
			
			if (this.sessionState.getUserId() < 0 || !userName.equalsIgnoreCase(this.sessionState.getUserName()))
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				if (meLogger.isDebugEnabled()) {meLogger.debug("GetUserAppMarkSession request failed, User:" + userName.toString() + ", Response code: " + HttpStatus.UNAUTHORIZED.value());}
				return null;
			}

			if (this.sessionState.getPlatformId() < 1)
			{
				//Platform must be set for this session to continue.
				httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
				if (meLogger.isDebugEnabled()) {meLogger.debug("GetUserAppMarkSession request failed because no platform was setup, User:" + userName.toString() + ", Response code: " + HttpStatus.BAD_REQUEST.value());}
				return null;
			}
			
			CustomResponse customResponse = new CustomResponse();
			UserApp userApp = accountService.GetUserApp(this.sessionState.getUserId(), this.sessionState.getAppId(), this.sessionState.getPlatformId(), userAppId, customResponse);
			this.sessionState.setUserAppId(userApp.getId());
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetUserAppMarkSession request completed, User:" + userName.toString());}
			
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(customResponse.getResponseCode());
			return userApp;
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in GetUserAppMarkSession", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}

	// GET /profilename/{profileName}
	@RequestMapping(value="/profilename/{profileName}", method=RequestMethod.GET, 
	produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody String CheckProfileName(
		@PathVariable("profileName") String profileName,
		HttpServletResponse httpResponse)
	{	
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("CheckProfileName request received, profileName: " + profileName);}
			
			//Check for session validated by the picture thingy.
			//if (this.sessionState.isRobot())
			//{
			//	httpResponse.addHeader("Cache-Control", "no-cache");
			//	httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
			//	return "";
			//}
			
			String profileReturn = "USED";
			if (accountService.CheckProfileNameIsUnique(profileName))
			{
				profileReturn = "OK";
			}
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("CheckProfileName request completed, profileName: " + profileName);}
			
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(HttpStatus.OK.value());
			return "<ProfileNameCheck>" + profileReturn + "</ProfileNameCheck>";
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in CheckProfileName", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}
	
	// POST /appcheck?wsKey={wsKey}
	@RequestMapping(value = { "/appcheck" }, method = { RequestMethod.POST }, 
			headers={"Accept-Charset=utf-8"}, produces=MediaType.APPLICATION_XML_VALUE )
	public void VerifyApp(
			@RequestParam("wsKey") String wsKey,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("VerifyApp request received");}
			
			//Check nothing.
			
			CustomResponse customResponse = new CustomResponse();
			int appId = accountService.VerifyApp(wsKey, customResponse);
			if (customResponse.getResponseCode() == HttpStatus.OK.value())
			{
				this.sessionState.setAppId(appId);
			}

			if (meLogger.isDebugEnabled()) {meLogger.debug("VerifyApp request completed");}
			
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(customResponse.getResponseCode());
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in VerifyApp", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
	
	// POST /{userName}/platform?OS={OS}&machine={machineType}&major={major}&minor={minor}
	@RequestMapping(value = { "/{userName}/platform" }, method = { RequestMethod.POST }, 
			headers={"Accept-Charset=utf-8"}, produces=MediaType.APPLICATION_XML_VALUE )
	public void SetPlatformForSession(
			@RequestParam("OS") String OS,
			@RequestParam("machineType") String machineType,
			@RequestParam("major") int major,
			@RequestParam("minor") int minor,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("SetPlatformForSession request received");}
			
			//Check Session is valid - logged in.
			
			CustomResponse customResponse = new CustomResponse();
			int platformId = accountService.GetPlatformId(OS, machineType, major, minor, customResponse);
			this.sessionState.setPlatformId(platformId);
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("SetPlatformForSession request completed");}
			
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(customResponse.getResponseCode());
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in SetPlatformForSession", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
	
	//POST /logon?userName={userName}
	@RequestMapping(value = { "/logon" }, method = { RequestMethod.POST }, 
			headers={"Accept-Charset=utf-8"}, produces=MediaType.APPLICATION_XML_VALUE )
	public void Logon(
			@RequestParam("userName") String userName,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("Logon request received");}
			
			//Check nothing.
			int response = HttpStatus.UNAUTHORIZED.value();
			long userId = accountService.LogonCheck(userName);
			if (userId > 0)
			{
				this.sessionState.setUserId(userId);
				this.sessionState.setUserName(userName);
				response = HttpStatus.OK.value();
			}
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("Logon request completed");}
			
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(response);
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in Logon", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
	
	//  GET - /{userName}
	@RequestMapping(value="/session", method=RequestMethod.GET, 
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody String GetSession(HttpServletResponse httpResponse)
	{	
		try
		{
			httpResponse.setStatus(HttpStatus.OK.value());
			return "UserName:" + this.sessionState.getUserName() + " UserID:" + this.sessionState.getUserId() +
					" PlatformId:" + this.sessionState.getPlatformId() +
					" AppId:" + this.sessionState.getAppId() +
					" UserAppId:" + this.sessionState.getUserAppId();
		}
		catch (Exception ex) {
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return "";
		}
	}
}
