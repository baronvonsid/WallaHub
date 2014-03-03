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
import org.springframework.web.bind.annotation.PathVariable;
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
 	VerifyApp() GET /appcheck
 	SetPlatformForSession() /GET /platform?OS={OS}&machine={machine}&major={major}&minor={minor}

	CreateAccount() PUT /{userName}
	GetAccount() GET /{userName}
	AckEmailConfirm() GET /{userName}/{validationString}

	GetUserApp() GET /{userName}/userapp/{userAppId}
	CreateUpdateUserApp() PUT /{userName}/userapp

	CheckProfileName() GET /profilename/{profileName}

	UpdateAccount() PUT /{userName}
	Logon()
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
			@RequestBody Account account,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("CreateUpdateAccount request received.  Email: " + account.getEmail());}
			
			//TODO Check session state, must of been past the image validation thing.
			//httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
			//return 0;
	
			long userId = this.sessionState.getUserId();
			
			int responseCode = accountService.CreateUpdateAccount(userId, account);
			httpResponse.setStatus(responseCode);
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("CreateUpdateAccount tag request completed, Email: " + account.getEmail() + " UserId: " + account.getId() + " Response code: " + responseCode);}
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in CreateUpdateAccount", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
	
	//  GET - /{userName}/
	@RequestMapping(value="/{userName}/", method=RequestMethod.GET, 
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody Account GetAccount(
			@PathVariable("userName") String userName,
			HttpServletResponse httpResponse)
	{	
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetAccount request received, User:" + userName.toString());}
			
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				return null;
			}
			
			CustomResponse customResponse = new CustomResponse();
			Account account = accountService.GetAccount(userId, customResponse);
			
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
	
	//  GET - /{userName}/{validationString}/
	@RequestMapping(value="/{userName}/{validationString}/", method=RequestMethod.GET, 
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void AckEmailConfirm(
			@PathVariable("userName") String userName,
			@PathVariable("validationString") String validationString,
			HttpServletResponse httpResponse)
	{	
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("AckEmailConfirm request received, User:" + userName.toString());}
			
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				return;
			}
			
			int response = accountService.AckEmailConfirm(userId, validationString);
			
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
	public String CreateUpdateUserApp(
			@PathVariable("userName") String userName,
			@RequestBody UserApp userApp,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("CreateUpdateUserApp request received.  User:" + userName.toString());}
			
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				if (meLogger.isDebugEnabled()) {meLogger.debug("CreateUpdateUserApp request failed, User:" + userName.toString() + ", Response code: " + HttpStatus.UNAUTHORIZED.value());}
				return "";
			}

			CustomResponse customResponse = new CustomResponse();
			
			long userAppId = userApp.getId();
			if (userAppId == 0)
			{
				userAppId = accountService.CreateUserApp(userId, this.sessionState.getAppId(), this.sessionState.getPlatformId(), userApp, customResponse);
			}
			else
			{
				accountService.UpdateUserApp(userId, this.sessionState.getAppId(), this.sessionState.getPlatformId(), userApp, customResponse);
			}
			
			httpResponse.setStatus(customResponse.getResponseCode());
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("CreateUpdateUserApp request completed, User:" + userName.toString() + ", Response code: " + customResponse.getResponseCode());}
			return "<UserAppId>" + userAppId + "</UserAppId>";
			
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in CreateUpdateUserApp", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return "0";
		}
	}
	
	// GET /{userName}/userapp/{userAppId}
	@RequestMapping(value = { "/{userName}/userapp/{userAppId}" }, method = { RequestMethod.GET }, 
			headers={"Accept-Charset=utf-8"}, produces=MediaType.APPLICATION_XML_VALUE )
	public UserApp GetUserAppMarkSession(
			@PathVariable("userAppId") long userAppId,
			@PathVariable("userName") String userName,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetUserAppMarkSession request received, User:" + userName.toString());}
			
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				return null;
			}
			
			CustomResponse customResponse = new CustomResponse();
			UserApp userApp = accountService.GetUserApp(userId, this.sessionState.getAppId(), this.sessionState.getPlatformId(), userAppId, customResponse);
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

	// GET /profilename/{profileName}/
	@RequestMapping(value="/profilename/{profileName}/", method=RequestMethod.GET, 
	produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public String CheckProfileName(
		@PathVariable("profileName") String profileName,
		HttpServletResponse httpResponse)
	{	
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("CheckProfileName request received, profileName: " + profileName);}
			
			//Check for session validated by the picture thingy.
			
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
		
	public String VerifyApp()
	{
		//Pass in application key
		
		//Check for key existing in Walla
		
		//If not, then send back forbidden message
		
		//If exists - but retired, then send back status message, but do not allow further interactions
		
		//If exists, but due to be retired or new version available, send back information about upgrade, but continue
		
		//Else send back OK.
		
		return "";
	}
	
	
	
	// POST /{userName}/platform?OS={OS}&machine={machine}&major={major}&minor={minor}
	@RequestMapping(value = { "/{userName}/platform?OS={OS}&machine={machine}&major={major}&minor={minor}" }, method = { RequestMethod.POST }, 
			headers={"Accept-Charset=utf-8"}, produces=MediaType.APPLICATION_XML_VALUE )
	public void SetPlatformForSession(
			@PathVariable("OS") String OS,
			@PathVariable("machine") String machine,
			@PathVariable("major") String major,
			@PathVariable("minor") String minor,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("SetPlatformForSession request received");}
			
			//Check Session is valid - logged in.
			
			CustomResponse customResponse = new CustomResponse();
			int platformId = accountService.GetPlatformId(OS, machine, major, minor, customResponse);
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
	
	
}
