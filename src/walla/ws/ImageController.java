package walla.ws;

import javax.validation.Valid;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Controller;
import org.w3c.dom.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

//To move/
import java.awt.*;  
import java.awt.image.*;  
import javax.imageio.*;  
 


import walla.datatypes.java.*;
import walla.business.*;
import walla.datatypes.auto.*;
import walla.utils.*;

/*
 * 
	Images and Uploads

	CreateUpdateImageMeta() PUT /{userName}/image/{imageId}/meta
	GetImageMeta() GET /{userName}/image/{imageId}/meta
	DeleteImage() DELETE /{userName}/image/{imageId}
	DeleteImageBulk() DELETE /{userName}/image

	UploadImage POST /image - Two requests needed for image file upload.
	GetUploadStatus GET /image/uploadstatus

	GetImageList() /{type}/{identity}/{imageCursor}/{size}

	GetOriginalImage GET /{userName}/image/{imageId}/original
	GetMainCopyImage GET /{userName}/image/{imageId}/maincopy
	GetImage GET /{userName}/image/{imageId}/{width}
	GetPreviewMainCopyImage GET /{userName}/imagepreview/{imageId}/maincopy
	GetPreviewImage GET /{userName}/imagepreview/{imageId}/{width}
*/

@Controller
@RequestMapping("/ws")
public class ImageController {
	
	@Autowired
	private CustomSessionState sessionState;
	
	@Autowired
	private ImageService imageService;
	
	@Autowired
	private ServletContext servletContext;

	/*
	private enum ImageStatus
	{
		Pending 0, Queued 1, Processing 2, Complete 3, Error 4, Inactive 5
		None = 0, FileReceived = 1, AwaitingProcessed = 2, BeingProcessed = 3, Complete = 4, Inactive = 5
	}
	*/
	
	private final String destinationRoot = "C:\\temp\\WallaRepo\\";
	private final long maxImageSizeMB = 50;
	private static final Logger meLogger = Logger.getLogger(ImageController.class);

	//  PUT /{userName}/image/{imageId}/meta
	@RequestMapping(value = { "/{userName}/image/{imageId}/meta" }, method = { RequestMethod.PUT }, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void CreateUpdateImageMeta(
			@RequestBody ImageMeta imageMeta,
			@PathVariable("imageId") long imageId,
			@PathVariable("userName") String userName,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("CreateUpdateImageMeta request received, User: " + userName + ", ImageId:" + imageId);}

			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName /* ,to add OAuth entity */);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				return;
			}
	
			int responseCode = 0;
			
			//Use status to work if create or update
			if (imageMeta.getStatus() == 4)
			{
				responseCode = imageService.CreateUpdateImageMeta(userId, imageMeta, imageId);	
			}
			else if (imageMeta.getStatus() == 1)
			{
				boolean found = false;
				
				for (int i = 0; i < this.sessionState.getUploadFilesReceived().size(); i++)
				{
					if (imageId == this.sessionState.getUploadFilesReceived().get(i))
					{
						found = true;
						
						this.sessionState.getUploadFilesReceived().remove(i);
						responseCode = imageService.CreateUpdateImageMeta(userId, imageMeta, imageId);
						
						continue;
					}
				}
				
				if (!found)
				{
					String error = "A corresponding image file was not found to match the meta received.";
					meLogger.error(error);
					responseCode = HttpStatus.NOT_ACCEPTABLE.value();
				}
			}
			else
			{
				String error = "Meta data cannot be updated for an image with this status.";
				meLogger.error(error);
				responseCode = HttpStatus.NOT_ACCEPTABLE.value();
			}

			httpResponse.setStatus(responseCode);
			if (meLogger.isDebugEnabled()) {meLogger.debug("CreateUpdateImageMeta tag request completed, User:" + userName.toString() + ", ImageId:" + imageId + " Response code: " + responseCode);}
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in CreateUpdateImageMeta", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
	
	//  GET - /{userName}/image/{imageId}/meta
	//  No client caching.  Check client side version against db timestamp.
	@RequestMapping(value="/{userName}/image/{imageId}/meta", method=RequestMethod.GET, 
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody ImageMeta GetImageMeta(
			@PathVariable("imageId") long imageId,
			@PathVariable("userName") String userName,
			HttpServletResponse httpResponse)
	{	
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetImageMeta request received, User:" + userName.toString() + ", ImageId:" + imageId);}
			
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName /* ,to add OAuth entity */);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				return null;
			}
			//this.sessionState.getMachineId()
			
			CustomResponse customResponse = new CustomResponse();
			ImageMeta responseImageMeta = imageService.GetImageMeta(userId, imageId, customResponse);
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetImageMeta request completed, User:" + userName.toString() + ", ImageId:" + imageId);}
			
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(customResponse.getResponseCode());
			return responseImageMeta;
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in GetImageMeta", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}
	
	//  DELETE /{userName}/image/{imageId}
	@RequestMapping(value = { "/{userName}/image/{imageId}" }, method = { RequestMethod.DELETE },  headers={"Accept-Charset=utf-8"} )
	public void DeleteImage(
			@PathVariable("imageId") long imageId,
			@PathVariable("userName") String userName,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("DeleteImage request received, User:" + userName.toString() + ", ImageId:" + imageId);}
	
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName /* ,to add OAuth entity */);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				return;
			}

			//Build object with one image reference.
			ImageList.Images.ImageRef toDelete = new ImageList.Images.ImageRef();
			toDelete.setId(imageId);
			ImageList imagesToDelete = new ImageList();
			imagesToDelete.setImages(new ImageList.Images());
			imagesToDelete.getImages().getImageRef().add(toDelete);
			
			int responseCode = imageService.DeleteImages(userId, imagesToDelete);
			
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(responseCode);
			if (meLogger.isDebugEnabled()) {meLogger.debug("DeleteImage request completed, User:" + userName.toString() + ", ImageId:" + imageId);}
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in DeleteImage", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
	
	//  DELETE /{userName}/images
	@RequestMapping(value = { "/{userName}/images" }, method = { RequestMethod.DELETE },  headers={"Accept-Charset=utf-8"}, consumes = MediaType.APPLICATION_XML_VALUE )
	public void DeleteImageBulk(
			@RequestBody ImageList imagesToDelete,
			@PathVariable("userName") String userName,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("DeleteImageBulk request received, User:" + userName.toString());}
	
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName /* ,to add OAuth entity */);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				return;
			}
	
			int responseCode = imageService.DeleteImages(userId, imagesToDelete);
			
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(responseCode);
			if (meLogger.isDebugEnabled()) {meLogger.debug("DeleteImageBulk request completed, User:" + userName.toString());}
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in DeleteImageBulk", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
	
	//  POST /image
	@RequestMapping(value = { "/{userName}/image" }, consumes=MediaType.IMAGE_JPEG_VALUE, method = { RequestMethod.POST }, headers={"Accept-Charset=utf-8", "Content-length"}, produces=MediaType.TEXT_PLAIN_VALUE )
	public @ResponseBody String UploadImage(
			@PathVariable("userName") String userName,
			@RequestHeader("Content-length") long contentLength,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("UploadImage request received, User: " + userName);}
		
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName /* ,to add OAuth entity */);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				if (meLogger.isDebugEnabled()) {meLogger.debug("UploadImage request failed, User:" + userName.toString() + ", Response code: " + HttpStatus.UNAUTHORIZED.value());}
				return "";
			}
			
			long maxImageSizeBytes = maxImageSizeMB * 1024 * 1024;
			
			if (contentLength > maxImageSizeBytes || httpRequest.getContentLength() != contentLength)
			{
				httpResponse.setStatus(HttpStatus.REQUEST_ENTITY_TOO_LARGE.value());
				if (meLogger.isDebugEnabled()) {meLogger.debug("UploadImage request failed, User:" + userName.toString() + ", Response code: " + HttpStatus.REQUEST_ENTITY_TOO_LARGE.value());}
				return "";
			}
			
			
			long newImageId = imageService.GetImageId(userId);
			if (newImageId > 0)
			{
				//Save Image to disk.
				SaveFileToTemp(userId, newImageId, httpRequest.getInputStream());
			}
			else
			{
				httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
				if (meLogger.isDebugEnabled()) {meLogger.debug("UploadImage request failed, User:" + userName.toString() + ", Response code: " + HttpStatus.INTERNAL_SERVER_ERROR.value());}
				return null;
			}

			//Save record for image upload state 
			//UploadStatusList.ImageUploadRef uploadRef = new UploadStatusList.ImageUploadRef();
			//uploadRef.setImageId(newImageId);
			//uploadRef.setImageStatus(0);
			
			//Calendar calDate = Calendar.getInstance();
			//GregorianCalendar oldGreg = new GregorianCalendar();
			//oldGreg.setTime(calDate.getTime());
			//XMLGregorianCalendar xmlOldGreg = DatatypeFactory.newInstance().newXMLGregorianCalendar(oldGreg);
			
			//uploadRef.setLastUpdated(xmlOldGreg);
			
			this.sessionState.getUploadFilesReceived().add(newImageId);

			
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("UploadImage request completed, User:" + userName.toString() + ", Response code: " + HttpStatus.ACCEPTED.value());}
			httpResponse.setStatus(HttpStatus.ACCEPTED.value());
			return "<ImageId>" + newImageId + "</ImageId>";
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in UploadImage", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}

	//  GET /{type}/{identity}/{imageCursor}/{size}
	//  To add - Filter Selection - neName=simon,neName=simon, mtName=simon, ltName=simon, sortasc=Name, sortdesc=Desc
	//  Check client side version against db timestamp.
	@RequestMapping(value="/{userName}/{type}/{identity}/{imageCursor}/{size}", method=RequestMethod.GET,
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody ImageList GetImageList(
			@PathVariable("type") String type,
			@PathVariable("identity") String identity,
			@PathVariable("userName") String userName,
			@PathVariable("imageCursor") int imageCursor,
			@PathVariable("size") int size,
			@RequestParam(value="sectionId", required=false) String paramSectionId,
			HttpServletRequest requestObject,
			HttpServletResponse httpResponse)
	{
		Date clientVersionTimestamp = null;
		
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetImageList request received, User:" + userName + ", Type: " + type + " Id:" + identity.toString());}
		
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
			
			long sectionId = -1;
			if (paramSectionId != null)
				sectionId = Long.parseLong(paramSectionId);
			
			//Thread.sleep(2000);
			// this.sessionState.getMachineId(),
			ImageList responseImageList = imageService.GetImageList(userId, type, identity, sectionId, imageCursor, size, clientVersionTimestamp, customResponse);
			
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(customResponse.getResponseCode());
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetImageList completed, User:" + userName + ", Type: " + type + " Id:" + identity.toString());}
			return responseImageList;
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in GetImageList", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}

	//  GET /image/image/uploadstatus
	//  None = 0, FileReceived = 1, AwaitingProcessed = 2, BeingProcessed = 3, Complete = 4, Inactive = 5
	//  No client caching.  Check client side version against db timestamp.
	@RequestMapping(value="/{userName}/image/uploadstatus", method=RequestMethod.POST, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody UploadStatusList GetUploadStatus(
			@RequestBody ImageIdList imageIdList,
			@PathVariable("userName") String userName,
			HttpServletResponse httpResponse)
	{	
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetUploadStatus request received, User:" + userName.toString());}

			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				return null;
			}

			CustomResponse customResponse = new CustomResponse();
			UploadStatusList uploadStatusList = imageService.GetUploadStatusList(userId, imageIdList, this.sessionState.getUploadFilesReceived(),customResponse);
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetUploadStatus request completed, User:" + userName.toString() + " Response Code:" + customResponse.getResponseCode());}
			
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(customResponse.getResponseCode());
			return uploadStatusList;
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in GetUploadStatus", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}
	
	//GET /image/{imageId}/original
	//Not client or server side caching.
	@RequestMapping(value = { "/{userName}/image/{imageId}/original" }, method = { RequestMethod.GET }, produces=MediaType.APPLICATION_OCTET_STREAM_VALUE )
	public @ResponseBody void GetOriginalImage(
			@PathVariable("userName") String userName,
			@PathVariable("imageId") long imageId,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetOriginalImage request received, User: " + userName + " ImageId:" + imageId);}
			
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName /* ,to add OAuth entity */);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				if (meLogger.isDebugEnabled()) {meLogger.debug("GetOriginalImage request failed, User:" + userName.toString() + ", Response code: " + HttpStatus.UNAUTHORIZED.value());}
				return;
			}

			// this.sessionState.getMachineId()
			
			//TODO check session allows original download.
			File file = null;
			CustomResponse customResponse = new CustomResponse();
			ImageMeta responseImageMeta = imageService.GetImageMeta(userId, imageId,customResponse);
        	if (customResponse.getResponseCode() == HttpStatus.OK.value())
        	{
        		if (responseImageMeta.getStatus().intValue() != 4)
        		{
        			String error = "Image request cannot be completed.  Image has an incorrect status for downloading.  Status:" + responseImageMeta.getStatus();
        			meLogger.error(error);
        			customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
        		}
        		else
        		{
        			file = imageService.GetOriginalImageFile(userId, imageId, customResponse);
	        		httpResponse.setHeader("Content-Disposition","attachment;filename=" + imageId + "." + responseImageMeta.getFormat());
	        		
	        		if (file != null)
	        			UserTools.PopulateServletStream(file, httpResponse.getOutputStream());
	        	}
        	}
        	
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetOriginalImage request completed, User:" + userName.toString() + ", Response code: " + customResponse.getResponseCode());}
			httpResponse.setStatus(customResponse.getResponseCode());

		}
		catch (Exception ex) {
			meLogger.error("Received Exception in GetOriginalImage", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
	
	//GET /{userName}/image/{imageId}/maincopy
	//Server side caching.  Client side caching.
	@RequestMapping(value = { "/{userName}/image/{imageId}/maincopy" }, method = { RequestMethod.GET }, produces=MediaType.IMAGE_JPEG_VALUE )
	public @ResponseBody void GetMainCopyImage(
			@PathVariable("userName") String userName,
			@PathVariable("imageId") long imageId,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetMainCopyImage request received, User: " + userName + " ImageId:" + imageId);}
			
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				if (meLogger.isDebugEnabled()) {meLogger.debug("GetMainCopyImage request failed, User:" + userName.toString() + ", Response code: " + HttpStatus.UNAUTHORIZED.value());}
				return;
			}
			
			CustomResponse customResponse = new CustomResponse();
			BufferedImage responseImage = imageService.GetMainCopyImageFile(userId, imageId, false, customResponse);
			//TODO - No cache header
			//Thread.sleep(1000);
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetMainCopyImage request completed, User:" + userName.toString() + ", Response code: " + customResponse.getResponseCode());}
			httpResponse.setStatus(customResponse.getResponseCode());
			
			if (responseImage != null)
			{
				ImageIO.write(responseImage, "jpg", httpResponse.getOutputStream());
			}
			
			//TODO add server side caching tag.
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in GetMainCopyImage", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
	
	//GET /{userName}/image/{imageId}/{width}/{height}
	//Server side caching.  Client side caching.
	@RequestMapping(value = { "/{userName}/image/{imageId}/{width}/{height}" }, method = { RequestMethod.GET }, produces=MediaType.IMAGE_JPEG_VALUE )
	public @ResponseBody void GetImage(
			@PathVariable("userName") String userName,
			@PathVariable("imageId") long imageId,
			@PathVariable("width") int width,
			@PathVariable("height") int height,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetImage request received, User: " + userName + " ImageId:" + imageId);}
			
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName /* ,to add OAuth entity */);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				if (meLogger.isDebugEnabled()) {meLogger.debug("GetImage request failed, User:" + userName.toString() + ", Response code: " + HttpStatus.UNAUTHORIZED.value());}
				return;
			}
			
			CustomResponse customResponse = new CustomResponse();
			BufferedImage responseImage = imageService.GetScaledImageFile(userId, imageId, width, height, false, customResponse);
			//TODO - No cache header
			//Thread.sleep(1000);
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetImage request completed, User:" + userName.toString() + ", Response code: " + customResponse.getResponseCode());}
			httpResponse.setStatus(customResponse.getResponseCode());
			
			if (responseImage != null)
			{
				ImageIO.write(responseImage, "jpg", httpResponse.getOutputStream());
			}
			
			//TODO add server side caching tag.
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in GetImage", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
	
	//GET /{userName}/image/{imageId}/{width}/{height}
	//Server side caching.  Client side caching.
	@RequestMapping(value = { "/{userName}/appimage/{imageRef}/{width}/{height}" }, method = { RequestMethod.GET }, produces=MediaType.IMAGE_JPEG_VALUE )
	public @ResponseBody void GetAppImage(
			@PathVariable("userName") String userName,
			@PathVariable("imageRef") String imageRef,
			@PathVariable("width") int width,
			@PathVariable("height") int height,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetAppImage request received, User: " + userName + " imageRef:" + imageRef);}
			
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName /* ,to add OAuth entity */);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				if (meLogger.isDebugEnabled()) {meLogger.debug("GetAppImage request failed, User:" + userName.toString() + ", Response code: " + HttpStatus.UNAUTHORIZED.value());}
				return;
			}
			
			CustomResponse customResponse = new CustomResponse();
			BufferedImage responseImage = imageService.GetAppImageFile(imageRef, width, height, customResponse);
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetAppImage request completed, User:" + userName.toString() + ", Response code: " + customResponse.getResponseCode());}
			httpResponse.setStatus(customResponse.getResponseCode());
			
			if (responseImage != null)
			{
				ImageIO.write(responseImage, "jpg", httpResponse.getOutputStream());
			}
			
			//TODO add server side caching tag.
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in GetAppImage", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	//GET /{userName}/imagepreview/{imageId}/maincopy
	//Server side caching.  Client side caching.
	@RequestMapping(value = { "/{userName}/imagepreview/{imageId}/maincopy" }, method = { RequestMethod.GET }, produces=MediaType.IMAGE_JPEG_VALUE )
	public @ResponseBody void GetPreviewMainCopyImage(
			@PathVariable("userName") String userName,
			@PathVariable("imageId") long imageId,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetPreviewMainCopyImage request received, User: " + userName + " ImageId:" + imageId);}
			
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				if (meLogger.isDebugEnabled()) {meLogger.debug("GetPreviewMainCopyImage request failed, User:" + userName.toString() + ", Response code: " + HttpStatus.UNAUTHORIZED.value());}
				return;
			}
			
			CustomResponse customResponse = new CustomResponse();
			BufferedImage responseImage = imageService.GetMainCopyImageFile(userId, imageId, true, customResponse);
			//TODO - No cache header
			//Thread.sleep(1000);
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetPreviewMainCopyImage request completed, User:" + userName.toString() + ", Response code: " + customResponse.getResponseCode());}
			httpResponse.setStatus(customResponse.getResponseCode());
			
			if (responseImage != null)
			{
				ImageIO.write(responseImage, "jpg", httpResponse.getOutputStream());
			}
			
			//TODO add server side caching tag.
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in GetPreviewMainCopyImage", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	//GET /{userName}/imagepreview/{imageId}/{width}/{height}
	//Server side caching.  Client side caching.
	@RequestMapping(value = { "/{userName}/imagepreview/{imageId}/{width}/{height}" }, method = { RequestMethod.GET }, produces=MediaType.IMAGE_JPEG_VALUE )
	public @ResponseBody void GetPreviewImage(
			@PathVariable("userName") String userName,
			@PathVariable("imageId") long imageId,
			@PathVariable("width") int width,
			@PathVariable("height") int height,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetPreviewImage request received, User: " + userName + " ImageId:" + imageId);}
			
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName /* ,to add OAuth entity */);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				if (meLogger.isDebugEnabled()) {meLogger.debug("GetPreviewImage request failed, User:" + userName.toString() + ", Response code: " + HttpStatus.UNAUTHORIZED.value());}
				return;
			}
			
			CustomResponse customResponse = new CustomResponse();
			BufferedImage responseImage = imageService.GetScaledImageFile(userId, imageId, width, height, true, customResponse);
			//TODO - No cache header
			//Thread.sleep(1000);
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetPreviewImage request completed, User:" + userName.toString() + ", Response code: " + customResponse.getResponseCode());}
			httpResponse.setStatus(customResponse.getResponseCode());
			
			if (responseImage != null)
			{
				ImageIO.write(responseImage, "jpg", httpResponse.getOutputStream());
			}
			
			//TODO add server side caching tag.
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in GetPreviewImage", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
	
	
	//***************************************
	//***************************************
	private void SaveFileToTemp(long userId, long imageId, InputStream inputStream)
	{
        try {
            if (inputStream != null) 
            {
        		Path uploadedFilePath = Paths.get(destinationRoot, "Que", String.valueOf(imageId) + "." + Long.toString(userId));
            	
            	File file = uploadedFilePath.toFile();
                FileOutputStream outputStream = new FileOutputStream(file);
 
                byte[] buffer = new byte[1024];
                int bytesRead;
 
                while ((bytesRead = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, bytesRead);
                }
 
                outputStream.flush();
                outputStream.close();
            }
 
        } catch (FileNotFoundException e) {
			meLogger.error("Received FileNotFoundException in SaveFileToDisk", e);
        } catch (IOException e) {
        	meLogger.error("Received IOException in SaveFileToDisk", e);
        }
	}



}


/*
Images and Uploads

CreateUpdateImageMeta() PUT /{userName}/image/{imageId}/meta
UpdateMultiImageMeta() PUT /image/multi/meta
DeleteImage() DELETE /{userName}/image/{imageId}
GetImageMeta() GET /{userName}/image/{imageId}/meta

GetOriginalImage GET /image/{imageId}
GetViewedImage GET /image/{platformId}/{imageId}
GetThumbnail GET /image/{platformId}/thumb/{imageId}

GetImageStatus GET /image/{imageId}/status  (received 0 \ processing 1\ complete 2\ error 3\ inactive 4)

UploadImage POST /image
Two requests needed for image file upload.

First - Send Headers
	Content-length: 1234567890
	Expect: 100-Continue
	X-WallaUploadId: {client generated id}
	
	Validate - HttpStatus.LENGTH_REQUIRED.value() is present.
	Depending on size: Respond with 417 Expectation Failed or 100 Continue
	Add to user session array of accepted upload ids.

Second, standard post
	Content-length: 1234567890
	X-WallaUploadId: {client generated id}
	
	Validate - Upload Id is valid for user.
			   Content length is correct and within tolerance.
	
	If upload is successful response with {imageId} in body
	httpResponse.setStatus(HttpStatus.ACCEPTED.value());

Client send Meta upload request on receiving Accepted.

*/

/*


	//  GET /image/{imageId}/status
	//  ReceivedImage 1, Awaiting 2, Processing 3, Complete 4, Error 5, Inactive 6
	//  No client caching.  Check client side version against db timestamp.
	@RequestMapping(value="/{userName}/image/uploadstatus", method=RequestMethod.GET, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody UploadStatusList GetUploadStatus(
			@PathVariable("userName") String userName,
			HttpServletResponse httpResponse)
	{	
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetUploadStatus request received, User:" + userName.toString());}

			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				return null;
			}
			
			//Check Session object for uploads which have been passed over to process.
			boolean toCheck = false;
			for(UploadStatusList.ImageUploadRef uploadRefCheck: this.sessionState.getUploadStatusList().getImageUploadRef())
			{
				meLogger.info("Upload status: " + uploadRefCheck.getImageId() + " " + uploadRefCheck.getImageStatus());
				if (uploadRefCheck.getImageStatus() == 1 || uploadRefCheck.getImageStatus() == 2) { toCheck = true; }
			}
			
			if (toCheck || !this.sessionState.getInitUploadList())
			{
				imageService.UpdateUploadStatusList(userId, this.sessionState.getUploadStatusList());
			}

			this.sessionState.setInitUploadList(true);
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetUploadStatus request completed, User:" + userName.toString());}
			
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(HttpStatus.OK.value());
			return this.sessionState.getUploadStatusList();
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in GetUploadStatus", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}
	

	//  POST /image
	@RequestMapping(value = { "/{userName}/image" }, consumes=MediaType.IMAGE_JPEG_VALUE, method = { RequestMethod.POST }, headers={"Accept-Charset=utf-8"}, produces=MediaType.TEXT_PLAIN_VALUE )
	public @ResponseBody String UploadImage(
			@PathVariable("userName") String userName,
			@RequestHeader("Content-length") long contentLength,
			@RequestHeader("X-WallaUploadId") String clientUploadId,
			@RequestHeader("Expect") String expectHeader,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("UploadImage request received, User: " + userName);}

			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				return "0";
			}
		

			CustomResponse customResponse = new CustomResponse();
			long newImageId = 0;
			
			if (expectHeader.equalsIgnoreCase("100-Continue"))
			{
			
				Content-length: 1234567890
				Expect: 100-Continue
				X-WallaUploadId: {client generated id}
				
				Validate - HttpStatus.LENGTH_REQUIRED.value() is present.
				Depending on size: Respond with 417 Expectation Failed or 100 Continue
				Add to user session array of accepted upload ids.


				if (contentLength > maxImageSize)
				{
					customResponse.setResponseCode(HttpStatus.REQUEST_ENTITY_TOO_LARGE.value());
				}
				else if (clientUploadId.length() < 1)
				{
					customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				}
				else
				{
					//Add to custom id user cache
					boolean conflict = false;
					
					for(CustomSessionState.ImageUploadRef uploadRefCheck: this.sessionState.getImageUploadRef())
					{
						if (uploadRefCheck.getClientUploadId().equals(clientUploadId)) { conflict = true; }
					}
					
					
					if (conflict)
					{
						customResponse.setResponseCode(HttpStatus.CONFLICT.value());
					}
					else
					{
						
						CustomSessionState.ImageUploadRef uploadRef = sessionState.new ImageUploadRef();
						uploadRef.setClientUploadId(clientUploadId);
						uploadRef.setImageId(0);
						uploadRef.setImageSize(contentLength);
						this.sessionState.getImageUploadRef().add(uploadRef);
						
						customResponse.setResponseCode(HttpStatus.CONTINUE.value());
					}
				}
			}
			else
			{

				Second, standard post
				Content-length: 1234567890
				X-WallaUploadId: {client generated id}
				
				Validate - Upload Id is valid for user.
						   Content length is correct and within tolerance.
				
				If upload is successful response with {imageId} in body
				httpResponse.setStatus(HttpStatus.ACCEPTED.value());


				boolean found = false;
				
				CustomSessionState.ImageUploadRef uploadRef = null;
				
				for(CustomSessionState.ImageUploadRef uploadRefCheck: this.sessionState.getImageUploadRef())
				{
					if (uploadRefCheck.getClientUploadId().equals(clientUploadId)) { uploadRef = uploadRefCheck;}
				}
				
				if (uploadRef == null)
				{
					//Check upload id is valid
					customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				}
				else if (uploadRef.getImageSize() != contentLength || httpRequest.getContentLength() != contentLength)
				{
					//Check content length is same
					//Check actual upload size matches
					customResponse.setResponseCode(HttpStatus.EXPECTATION_FAILED.value());	
				}
				else if (httpRequest.getContentLength() != contentLength)
				{
					customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());	
				}
				else
				{
					newImageId = imageService.GetImageId(userId);
					if (newImageId > 0)
					{
						//Save Image to disk.
						uploadRef.setUploadLocation(SaveFileToDisk(userId, newImageId));
						uploadRef.setImageId(newImageId);
					}
					else
					{
						customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());	
					}
				}
			}
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("UploadImage request completed, User:" + userName.toString() + ", Response code: " + customResponse.getResponseCode());}
			httpResponse.setStatus(customResponse.getResponseCode());
			return Long.toString(newImageId);
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in UploadImage", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return "0";
		}
	}



*/

