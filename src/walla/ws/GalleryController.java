package walla.ws;

import javax.validation.Valid;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Controller;
import org.w3c.dom.*;

import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
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
	CreateUpdateGallery() PUT /{profileName}/gallery/{galleryName}
	DeleteGallery() DELETE /{profileName}/gallery/{galleryName}
	GetGalleryMeta() GET /{profileName}/gallery/{galleryName}
	GetGalleryList() GET /{profileName}/galleries
	GetGalleryOptions() GET /{profileName}/gallery/galleryoptions
	GetGallerySections() GET /{profileName}/gallery/gallerysections
	PostGalleryPreview() POST /{profileName}/gallery/preview
	*/

@Controller
@RequestMapping("/ws")
public class GalleryController {

	private static final Logger meLogger = Logger.getLogger(GalleryController.class);

	@Autowired
	private CustomSessionState sessionState;
	
	@Autowired
	private GalleryService galleryService;
	
	//  PUT /{profileName}/gallery/{galleryName}
	@RequestMapping(value = { "/{profileName}/gallery/{galleryName}" }, method = { RequestMethod.PUT }, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void CreateUpdateGallery(
			@RequestBody Gallery newGallery,
			@PathVariable("galleryName") String galleryName,
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			CustomSessionState customSession = UserTools.GetValidSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return;
			}
	
			responseCode = galleryService.CreateUpdateGallery(customSession.getUserId(), newGallery, galleryName);
			if (responseCode == HttpStatus.MOVED_PERMANENTLY.value())
			{
				String newLocation = "/" + profileName + "/gallery/" + newGallery.getName();
				response.addHeader("Location", newLocation);
			}
		}
		catch (Exception ex) {
			meLogger.error(ex);
		}
		finally { UserTools.LogWebMethod("CreateUpdateGallery", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}
	
	//  DELETE /{profileName}/gallery/{galleryName}
	@RequestMapping(value = { "/{profileName}/gallery/{galleryName}" }, method = { RequestMethod.DELETE } , 
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void DeleteGallery(
			@RequestBody Gallery existingGallery, 
			@PathVariable("galleryName") String galleryName,
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			CustomSessionState customSession = UserTools.GetValidSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return;
			}
	
			responseCode = galleryService.DeleteGallery(customSession.getUserId(), existingGallery, galleryName);
		}
		catch (Exception ex) {
			meLogger.error(ex);
		}
		finally { UserTools.LogWebMethod("DeleteGallery", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}
	
	//  GET - /{profileName}/gallery/{galleryName}
	//  No client caching.  Check client side version against db timestamp.
	@RequestMapping(value="/{profileName}/gallery/{galleryName}", method=RequestMethod.GET, 
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody Gallery GetGalleryMeta(
			@PathVariable("galleryName") String galleryName,
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			CustomSessionState customSession = UserTools.GetValidSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return null;
			}
			
			CustomResponse customResponse = new CustomResponse();
			Gallery responseGallery = galleryService.GetGalleryMeta(customSession.getUserId(), galleryName, customResponse);
			
			responseCode = customResponse.getResponseCode();
			return responseGallery;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { UserTools.LogWebMethod("GetGalleryMeta", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}

	//  GET /{profileName}/galleries
	//  No client caching.  Check client side version against db timestamp.
	@RequestMapping(value="/{profileName}/galleries", method=RequestMethod.GET, 
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody GalleryList GetGalleryList(
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		Date clientVersionTimestamp = null;
		try
		{
			response.addHeader("Cache-Control", "no-cache");

			CustomSessionState customSession = UserTools.GetValidSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return null;
			}
			
			long headerDateLong = request.getDateHeader("If-Modified-Since");
			if (headerDateLong > 0)
				clientVersionTimestamp = new Date(headerDateLong);
			
			CustomResponse customResponse = new CustomResponse();
			GalleryList galleryList = galleryService.GetGalleryListForUser(customSession.getUserId(), clientVersionTimestamp, customResponse);
			responseCode = customResponse.getResponseCode();
			
			return galleryList;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { UserTools.LogWebMethod("GetGalleryList", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}

	//  GET - /{profileName}/gallery/galleryoptions
	@RequestMapping(value="/{profileName}/gallery/galleryoptions", method=RequestMethod.GET, 
	produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody GalleryOptions GetGalleryOptions(
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		Date clientVersionTimestamp = null;
		try
		{
			response.addHeader("Cache-Control", "private, max-age=86400");
			CustomSessionState customSession = UserTools.GetValidSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return null;
			}
			
			long headerDateLong = request.getDateHeader("If-Modified-Since");
			if (headerDateLong > 0)
				clientVersionTimestamp = new Date(headerDateLong);

			CustomResponse customResponse = new CustomResponse();
			GalleryOptions galleryOptions = galleryService.GetGalleryOptions(customSession.getUserId(), clientVersionTimestamp, customResponse);
	
			responseCode = customResponse.getResponseCode();

			return galleryOptions;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { UserTools.LogWebMethod("GetGalleryOptions", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}
	
	//  GET - /{profileName}/gallery/gallerysections
	//  No client caching.  No Server caching
	@RequestMapping(value="/{profileName}/gallery/gallerysections", method=RequestMethod.POST, consumes = MediaType.APPLICATION_XML_VALUE,
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody Gallery GetGallerySections(
			@RequestBody Gallery requestGallery,
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			CustomSessionState customSession = UserTools.GetValidSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return null;
			}
			
			CustomResponse customResponse = new CustomResponse();
			Gallery responseGallery = galleryService.GetGallerySections(customSession.getUserId(), requestGallery, customResponse);
			
			responseCode = customResponse.getResponseCode();
			return responseGallery;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { UserTools.LogWebMethod("GetGallerySections", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}

	//  POST /{profileName}/gallery/preview
	@RequestMapping(value = { "/{profileName}/gallerypreview" }, method = { RequestMethod.POST }, produces=MediaType.TEXT_PLAIN_VALUE, 
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody String PostGalleryPreview(
			@RequestBody Gallery galleryPreview,
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			CustomSessionState customSession = UserTools.GetValidSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return null;
			}
	
			galleryService.ResetGallerySectionForPreview(galleryPreview);
			
			synchronized(customSession) {
				if (customSession.getGalleryTempKey() != null && customSession.getGalleryTempKey().length() != 32)
					customSession.setGalleryTempKey(UserTools.GetComplexString());
				
				customSession.setGalleryPreview(galleryPreview);
			}

			responseCode = HttpStatus.OK.value();
			return "<GalleryTempKey>" + customSession.getGalleryTempKey() + "</GalleryTempKey>";
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { UserTools.LogWebMethod("PostGalleryPreview", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}

}
