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
	CreateUpdateTag() PUT /{userName}/tag/{tagName}
	DeleteTag() DELETE /{userName}/tag/{tagName}
	GetTagMeta() GET /{userName}/tag/{tagName}

	GetTagList() GET /{userName}/tags
	AddImagesToTag() PUT /{userName}/tag/{tagName}/images
	RemoveImagesFromTag DELETE /{userName}/tag/{tagName}/images
	*/

@Controller
@RequestMapping("/ws")
public class TagController {

	private static final Logger meLogger = Logger.getLogger(TagController.class);

	@Autowired
	private CustomSessionState sessionState;
	
	@Autowired
	private TagService tagService;
	
	//  PUT /{userName}/tag/{tagName}
	@RequestMapping(value = { "/{userName}/tag/{tagName}" }, method = { RequestMethod.PUT }, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void CreateUpdateTag(
			@RequestBody Tag newTag,
			@PathVariable("tagName") String tagName,
			@PathVariable("userName") String userName,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("CreateUpdateTag request received, User: " + userName + ", Tag:" + tagName);}
	
		//			@Valid BindingResult validResult,	
			/*  Schema validation.  Bug\Feature in Spring. SPR-9378 - Fixed in 3.1.3 & 3.2
			 *  @RequestBody @Valid Tag newTag,
			 @Valid BindingResult validResult
			if (validResult.hasErrors())
			{
				meLogger.error("Tag request is not valid, User:" + userName.toString() + ", Tag:" + tagName.toString() + " Validation: " + validResult.getAllErrors().get(0).toString());
				httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
				return null;
			}
			*/
			
			/*
			 * HttpStatus.BAD_REQUEST.value() is schema\tag mapping fails.
			 * HttpStatus.INTERNAL_SERVER_ERROR.value() - unexpected error.
			 */
			
			/*
			if (validResult.hasErrors())
			{
				meLogger.error("Tag request is not valid, User:" + userName.toString() + ", Tag:" + tagName.toString() + " Validation: " + validResult.getAllErrors().get(0).toString());
				httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
				return null;
			}
			*/
			
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName /* ,to add OAuth entity */);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
			}
	
			int responseCode = tagService.CreateUpdateTag(userId, newTag, tagName);
			if (responseCode == HttpStatus.MOVED_PERMANENTLY.value())
			{
				String newLocation = "/" + userName + "/tag/" + newTag.getName();
				httpResponse.addHeader("Location", newLocation);
			}
	
			httpResponse.setStatus(responseCode);
			if (meLogger.isDebugEnabled()) {meLogger.debug("CreateUpdateTag tag request completed, User:" + userName.toString() + ", Tag:" + tagName.toString() + " Response code: " + responseCode);}
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in CreateUpdateTag", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
	
	//  DELETE /{userName}/tag/{tagName}
	@RequestMapping(value = { "/{userName}/tag/{tagName}" }, method = { RequestMethod.DELETE } , 
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void DeleteTag(
			@RequestBody Tag existingTag, 
			@PathVariable("tagName") String tagName,
			@PathVariable("userName") String userName,
			HttpServletResponse httpResponse)
	{
		//@RequestHeader String Accept,
		
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("Delete tag request received, User:" + userName.toString() + ", Tag:" + tagName.toString());}
	
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName /* ,to add OAuth entity */);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				return;
			}
	
			int responseCode = tagService.DeleteTag(userId, existingTag, tagName);
			
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(responseCode);
			if (meLogger.isDebugEnabled()) {meLogger.debug("Delete tag request completed, User:" + userName.toString() + ", Tag:" + tagName.toString());}
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in DeleteTag", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
	
	//  GET - /{userName}/tag/{tagName}
	//  No client caching.  Check client side version against db timestamp.
	@RequestMapping(value="/{userName}/tag/{tagName}", method=RequestMethod.GET, 
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody Tag GetTagMeta(
			@PathVariable("tagName") String tagName,
			@PathVariable("userName") String userName,
			HttpServletResponse httpResponse)
	{	
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("Get tag meta request received, User:" + userName.toString() + ", Tag:" + tagName.toString());}
			
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName /* ,to add OAuth entity */);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				return null;
			}
			
			CustomResponse customResponse = new CustomResponse();
			Tag responseTag = tagService.GetTagMeta(userId, tagName, customResponse);
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("Get tag Meta request completed, User:" + userName.toString() + ", Tag:" + tagName.toString());}
			
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(customResponse.getResponseCode());
			return responseTag;
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in GetTagMeta", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}

	//  GET /{userName}/tags
	//  No client caching.  Check client side version against db timestamp.
	@RequestMapping(value="/{userName}/tags", method=RequestMethod.GET, 
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody TagList GetTagList(
			@PathVariable("userName") String userName,
			HttpServletRequest requestObject,
			HttpServletResponse httpResponse)
	{
		Date clientVersionTimestamp = null;
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("Get tag list for a user request received, User:" + userName.toString());}

			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName /* ,to add OAuth entity */);
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
			TagList tagList = tagService.GetTagListForUser(userId, clientVersionTimestamp, customResponse);
	
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(customResponse.getResponseCode());
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("Get tag list for a user completed, User:" + userName.toString());}
			
			return tagList;
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in GetTagList", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}
	
	// PUT /{userName}/tag/{tagName}/images
	// No client caching
	@RequestMapping(value = { "/{userName}/tag/{tagName}/images" }, method = { RequestMethod.PUT }, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void AddImagesToTag(
			@RequestBody ImageMoveList moveList,
			@PathVariable("tagName") String tagName,
			@PathVariable("userName") String userName,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("AddImagesToTag request received, User: " + userName + ", TagName:" + tagName);}
	
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName /* ,to add OAuth entity */);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
			}
	
			int responseCode = tagService.AddRemoveTagImages(userId, tagName, moveList, true);
			httpResponse.setStatus(responseCode);
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("AddImagesToTag request completed, User:" + userName.toString() + ", TagName:" + tagName + " Response code: " + responseCode);}
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in AddImagesToTag", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
	
	// PUT /{userName}/tag/{tagName}/images
	// No client caching
	@RequestMapping(value = { "/{userName}/tag/{tagName}/images" }, method = { RequestMethod.DELETE }, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void RemoveImagesFromTag(
			@RequestBody ImageMoveList moveList,
			@PathVariable("tagName") String tagName,
			@PathVariable("userName") String userName,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("RemoveImagesFromTag request received, User: " + userName + ", TagName:" + tagName);}
	
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName /* ,to add OAuth entity */);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
			}
	
			int responseCode = tagService.AddRemoveTagImages(userId, tagName, moveList, false);
			httpResponse.setStatus(responseCode);
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("RemoveImagesFromTag request completed, User:" + userName.toString() + ", TagName:" + tagName + " Response code: " + responseCode);}
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in RemoveImagesFromTag", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
}
