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
	CreateAccount() PUT /
	GetAccount() GET /{userName}/
	AckEmailConfirm() GET /{userName}/{validationString}

	GetUserApp() GET /{userName}/userapp/{userAppId}
	RegisterUserApp() PUT /{userName}/userapp/

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
	
	//  PUT /
	@RequestMapping(value = { "/" }, method = { RequestMethod.PUT }, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void CreateAccount(
			@RequestBody Account newAccount,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("CreateAccount request received.  Email: " + newAccount.getEmail());}
			
			//TODO Check session state, must of been past the image validation thing.
			//httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
			//return 0;
	
			int responseCode = accountService.CreateAccount(newAccount);
	
			httpResponse.setStatus(responseCode);
			if (meLogger.isDebugEnabled()) {meLogger.debug("CreateAccount tag request completed, Email: " + newAccount.getEmail() + " UserId: " + newAccount.getId() + " Response code: " + responseCode);}
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in CreateAccount", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
	
	//  GET - /{userName}/
	//  No client caching.  Check client side version against db timestamp.
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
	//  No client caching.  Check client side version against db timestamp.
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
	
	//  PUT /{userName}/userapp/
	@RequestMapping(value = { "/{userName}/userapp/" }, method = { RequestMethod.PUT }, produces=MediaType.TEXT_PLAIN_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public String RegisterUserApp(
			@PathVariable("userName") String userName,
			@RequestBody UserApp newUserApp,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("RegisterUserApp request received.  User:" + userName.toString());}
			
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				if (meLogger.isDebugEnabled()) {meLogger.debug("RegisterUserApp request failed, User:" + userName.toString() + ", Response code: " + HttpStatus.UNAUTHORIZED.value());}
				return "";
			}

			CustomResponse customResponse = new CustomResponse();
			long newUserAppId = accountService.RegisterUserApp(userId, newUserApp, customResponse);
			httpResponse.setStatus(customResponse.getResponseCode());
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("RegisterUserApp request completed, User:" + userName.toString() + ", Response code: " + customResponse.getResponseCode());}
			return "<UserAppId>" + newUserAppId + "</UserAppId>";
			
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in RegisterUserApp", ex);
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
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetUserApp request received, User:" + userName.toString());}
			
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				return null;
			}
			
			CustomResponse customResponse = new CustomResponse();
			UserApp userApp = accountService.GetUserApp(userId, userAppId, customResponse);
			this.sessionState.setUserAppId(userApp.getId());
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetUserApp request completed, User:" + userName.toString());}
			
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(customResponse.getResponseCode());
			return userApp;
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in GetUserApp", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}

}
