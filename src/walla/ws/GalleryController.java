package walla.ws;

import javax.validation.Valid;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Controller;
import org.w3c.dom.*;

import java.util.Date;
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
	CreateUpdateGallery() PUT /{userName}/gallery/{galleryName}
	GetGalleryList() GET /{userName}/galleries
	DeleteGallery() DELETE /{userName}/gallery/{galleryName}
	GetGalleryMeta() GET /{userName}/gallery/{galleryName}
	*/

@Controller
@RequestMapping("/ws")
public class GalleryController {

	private static final Logger meLogger = Logger.getLogger(GalleryController.class);

	@Autowired
	private CustomSessionState sessionState;
	
	@Autowired
	private GalleryService galleryService;
	
	//  PUT /{userName}/gallery/{galleryName}
	@RequestMapping(value = { "/{userName}/gallery/{galleryName}" }, method = { RequestMethod.PUT }, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void CreateUpdateGallery(
			@RequestBody Gallery newGallery,
			@PathVariable("galleryName") String galleryName,
			@PathVariable("userName") String userName,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("CreateUpdateGallery request received, User: " + userName + ", Gallery:" + galleryName);}

			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName /* ,to add OAuth entity */);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
			}
	
			int responseCode = galleryService.CreateUpdateGallery(userId, newGallery, galleryName);
			if (responseCode == HttpStatus.MOVED_PERMANENTLY.value())
			{
				String newLocation = "/" + userName + "/gallery/" + newGallery.getName();
				httpResponse.addHeader("Location", newLocation);
			}
	
			httpResponse.setStatus(responseCode);
			if (meLogger.isDebugEnabled()) {meLogger.debug("CreateUpdateGallery tag request completed, User:" + userName.toString() + ", Gallery:" + galleryName + " Response code: " + responseCode);}
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in CreateUpdateGallery", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
	
	//  DELETE /{userName}/gallery/{galleryName}
	@RequestMapping(value = { "/{userName}/gallery/{galleryName}" }, method = { RequestMethod.DELETE } , 
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void DeleteGallery(
			@RequestBody Gallery existingGallery, 
			@PathVariable("galleryName") String galleryName,
			@PathVariable("userName") String userName,
			HttpServletResponse httpResponse)
	{
		//@RequestHeader String Accept,
		
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("Delete gallery request received, User:" + userName.toString() + ", Gallery:" + galleryName.toString());}
	
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				return;
			}
	
			int responseCode = galleryService.DeleteGallery(userId, existingGallery, galleryName);
			
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(responseCode);
			if (meLogger.isDebugEnabled()) {meLogger.debug("Delete gallery request completed, User:" + userName.toString() + ", Gallery:" + galleryName.toString());}
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in DeleteGallery", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
	
	//  GET - /{userName}/gallery/{galleryName}
	//  No client caching.  Check client side version against db timestamp.
	@RequestMapping(value="/{userName}/gallery/{galleryName}", method=RequestMethod.GET, 
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody Gallery GetGalleryMeta(
			@PathVariable("galleryName") String galleryName,
			@PathVariable("userName") String userName,
			HttpServletResponse httpResponse)
	{	
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("Get gallery meta request received, User:" + userName.toString() + ", Gallery:" + galleryName.toString());}
			
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				return null;
			}
			
			CustomResponse customResponse = new CustomResponse();
			Gallery responseGallery = galleryService.GetGalleryMeta(userId, galleryName, customResponse);
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("Get gallery Meta request completed, User:" + userName.toString() + ", Gallery:" + galleryName.toString());}
			
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(customResponse.getResponseCode());
			return responseGallery;
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in GetGalleryMeta", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}

	//  GET /{userName}/galleries
	//  No client caching.  Check client side version against db timestamp.
	@RequestMapping(value="/{userName}/galleries", method=RequestMethod.GET, 
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody GalleryList GetGalleryList(
			@PathVariable("userName") String userName,
			HttpServletRequest requestObject,
			HttpServletResponse httpResponse)
	{
		Date clientVersionTimestamp = null;
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("Get gallery list for a user request received, User:" + userName.toString());}

			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				return null;
			}
			
			long headerDateLong = requestObject.getDateHeader("If-Modified-Since");
			if (headerDateLong > 0)
			{
				clientVersionTimestamp = new Date(headerDateLong);
			}
			
			CustomResponse customResponse = new CustomResponse();
			GalleryList galleryList = galleryService.GetGalleryListForUser(userId, clientVersionTimestamp, customResponse);
	
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(customResponse.getResponseCode());
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("Get gallery list for a user completed, User:" + userName.toString());}
			
			return galleryList;
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in GetGalleryList", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}


}
