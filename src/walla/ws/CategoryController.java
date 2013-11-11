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
	
	CreateCategory() POST /{userName}/category
	UpdateCategory() PUT /{userName}/category/{categoryId}
	DeleteCategory() DELETE /{userName}/category/{categoryId}
	GetCategoryMeta() GET /{userName}/category/{categoryId}
	
	GetCategoryList() GET /{userName}/categories
	MoveToNewCategory() PUT {userName}/category/{categoryId}/images	
	
	*/

@Controller
@RequestMapping("/ws")
public class CategoryController {

	private static final Logger meLogger = Logger.getLogger(CategoryController.class);

	@Autowired
	private CustomSessionState sessionState;
	
	@Autowired
	private CategoryService categoryService;
	
	// POST /{userName}/category
	//no client cache, no server cache
	@RequestMapping(value = { "/{userName}/category" }, method = { RequestMethod.POST }, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody String CreateCategory(
			@RequestBody Category newCategory,
			@PathVariable("userName") String userName,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("CreateCategory request received, User: " + userName + ", Category:" + newCategory.getName());}
			
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName /* ,to add OAuth entity */);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
			}
	
			CustomResponse customResponse = new CustomResponse();
			long newCategoryId = categoryService.CreateCategory(userId, newCategory, customResponse);
	
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(customResponse.getResponseCode());
			if (meLogger.isDebugEnabled()) {meLogger.debug("CreateCategory request completed, User:" + userName.toString() + ", Category:" + newCategory.getName() + " Response code: " + customResponse.getResponseCode());}
			return "<CategoryId>" + newCategoryId + "</CategoryId>";
			
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in CreateCategory", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return "0";
		}
	}
	
	// PUT /{userName}/category/{categoryId}
	//no client cache, no server cache
	@RequestMapping(value = { "/{userName}/category/{categoryId}" }, method = { RequestMethod.PUT }, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void UpdateCategory(
			@RequestBody Category newCategory,
			@PathVariable("userName") String userName,
			@PathVariable("categoryId") long categoryId,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("UpdateCategory request received, User: " + userName + ", CategoryId:" + categoryId);}
			
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName /* ,to add OAuth entity */);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
			}
	
			int responseCode = categoryService.UpdateCategory(userId, newCategory, categoryId);
	
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(responseCode);
			if (meLogger.isDebugEnabled()) {meLogger.debug("UpdateCategory request completed, User:" + userName.toString() + ", CategoryId:" + categoryId + " Response code: " + responseCode);}
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in UpdateCategory", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
	
	// DELETE /{userName}/category/{categoryId}
	// no client cache, no server cache
	@RequestMapping(value = { "/{userName}/category/{categoryId}" }, method = { RequestMethod.DELETE } , 
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void DeleteCategory(
			@RequestBody Category existingCategory, 
			@PathVariable("categoryId") long categoryId,
			@PathVariable("userName") String userName,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("Delete category request received, User:" + userName.toString() + ", CategoryId:" + categoryId);}
	
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName /* ,to add OAuth entity */);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				return;
			}
	
			int responseCode = categoryService.DeleteCategory(userId, existingCategory, categoryId);
			
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(responseCode);
			if (meLogger.isDebugEnabled()) {meLogger.debug("Delete category request completed, User:" + userName.toString() + ", CategoryId:" + categoryId);}
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in DeleteCategory", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
	
	//  GET - /{userName}/category/{categoryId}
	//  No client caching.  Check client side version against db timestamp.
	@RequestMapping(value="/{userName}/category/{categoryId}", method=RequestMethod.GET, 
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody Category GetCategoryMeta(
			@PathVariable("categoryId") long categoryId,
			@PathVariable("userName") String userName,
			HttpServletResponse httpResponse)
	{	
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetCategoryMeta request received, User:" + userName.toString() + ", CategoryId:" + categoryId);}
			
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName /* ,to add OAuth entity */);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				return null;
			}
			
			CustomResponse customResponse = new CustomResponse();
			Category responseCategory = categoryService.GetCategoryMeta(userId, categoryId, customResponse);
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetCategoryMeta request completed, User:" + userName.toString() + ", CategoryId:" + categoryId);}
			
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(customResponse.getResponseCode());
			return responseCategory;
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in GetCategoryMeta", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}
	
	/*
	//  GET /{userName}/category/{categoryId}/{imageCursor}/{size}
	//  TODO add - Filter Selection - neName=simon,neName=simon, mtName=simon, ltName=simon, sortasc=Name, sortdesc=Desc
	//  Check client side version against db timestamp.
	@RequestMapping(value="/{userName}/category/{categoryId}/{imageCursor}/{size}", method=RequestMethod.GET, 
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody ImageList GetCategoryImageList(
			@PathVariable("categoryId") long categoryId,
			@PathVariable("userName") String userName,
			@PathVariable("imageCursor") int imageCursor,
			@PathVariable("size") int size,
			HttpServletRequest requestObject,
			HttpServletResponse httpResponse)
	{
		Date clientVersionTimestamp = null;
		
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("Get category image list request received, User:" + userName.toString() + ", CategoryId:" + categoryId);}
		
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
			ImageList responseCategoryImageList = categoryService.GetCategoryWithImages(userId, categoryId, this.sessionState.getMachineId(), imageCursor, size, clientVersionTimestamp, customResponse);
			
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(customResponse.getResponseCode());
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("Get category image list completed, User:" + userName.toString() + ", CategoryId:" + categoryId);}
			return responseCategoryImageList;
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in GetCategoryImageList", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}
	*/
	
	//  GET /{userName}/categories
	//  No client caching.  Check client side version against db timestamp.
	@RequestMapping(value="/{userName}/categories", method=RequestMethod.GET, 
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody CategoryList GetCategoryList(
			@PathVariable("userName") String userName,
			HttpServletRequest requestObject,
			HttpServletResponse httpResponse)
	{
		Date clientVersionTimestamp = null;
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("Get category list for a user request received, User:" + userName.toString());}

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
			CategoryList categoryList = categoryService.GetCategoryListForUser(userId, clientVersionTimestamp, customResponse);
	
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(customResponse.getResponseCode());
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("Get category list for a user completed, User:" + userName.toString());}
			
			return categoryList;
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in GetCategoryList", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}
	
	// PUT {userName}/category/{categoryId}/images
	// No client caching
	@RequestMapping(value = { "/{userName}/category/{categoryId}/images" }, method = { RequestMethod.PUT }, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void MoveToNewCategory(
			@RequestBody ImageMoveList moveList,
			@PathVariable("categoryId") long categoryId,
			@PathVariable("userName") String userName,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("MoveToNewCategory request received, User: " + userName + ", CategoryId:" + categoryId);}
	
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName /* ,to add OAuth entity */);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
			}
	
			int responseCode = categoryService.MoveToNewCategory(userId, categoryId, moveList);
	
			httpResponse.setStatus(responseCode);
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("MoveToNewCategory request completed, User:" + userName.toString() + ", CategoryId:" + categoryId + " Responce code: " + responseCode);}
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in MoveToNewCategory", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

}


/*

Views

CreateUpdateView() PUT /{userName}/view/{tagName}
DeleteView() DELETE /{userName}/view/{tagName}
GetViewMeta() GET /{userName}/view/{tagName}
GetViewImageList() GET /{userName}/view/{viewName}/{platformId}/{imageCursor}/Filter=?{selection Parameters}
GetViewsAvailable() GET /{userName}/views

*/


