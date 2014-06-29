package walla.web;

import javax.validation.Valid;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Controller;
import org.w3c.dom.*;

import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
import walla.datatypes.java.*;
import walla.business.*;
import walla.datatypes.auto.*;
import walla.utils.*;

	/*
	GetGalleryViewer() GET /{userName}/gallery/{galleryName}
	GetGalleryImageList() GET /{userName}/gallery/{galleryName}/{sectionId}/{imageCursor}/{size}
	*/

@Controller
@RequestMapping("web")
public class GalleryViewer {

	private static final Logger meLogger = Logger.getLogger(GalleryViewer.class);

	private long previewUserId = 66666666;
	
	@Autowired
	private CustomSessionState sessionState;
	
	@Autowired
	private GalleryService galleryService;
	 
	@Autowired
	private ImageService imageService;
	
	//  GET /{userName}/gallery/{galleryName}
	@RequestMapping(value = { "/{userName}/gallery/{galleryName}" }, method = { RequestMethod.GET }, produces=MediaType.APPLICATION_XHTML_XML_VALUE )
	public String GetGalleryViewer(
			@PathVariable("galleryName") String galleryName,
			@RequestParam(value="key", required=false) String urlComplex,
			@PathVariable("userName") String userName,
			Model model,
			HttpServletResponse httpResponse)
	{
		String responseJsp = "GalleryViewerError";
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetGalleryViewer request received, User: " + userName + ", Gallery:" + galleryName);}

			boolean securityPassed = false;
			boolean useUrlComplex = false;
			long userId = -1;
			
			if (urlComplex != null && urlComplex.length() == 36)
				useUrlComplex = true;
			
			if (this.sessionState.getUserId() > 0 && userName.equalsIgnoreCase(this.sessionState.getUserName()))
			{
				securityPassed = true;
				userId = this.sessionState.getUserId();
			}
			
			if (!securityPassed && !useUrlComplex)
			{
				Thread.sleep(3000);
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				if (meLogger.isDebugEnabled()) {meLogger.debug("GetGalleryViewer request not authorised, User:" + userName.toString());}
				return null;
			}

			if (!securityPassed)
			{
				userId = galleryService.GetUserForGallery(userName, galleryName, urlComplex);
				if (userId < 1)
				{
					Thread.sleep(3000);
					httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
					if (meLogger.isDebugEnabled()) {meLogger.debug("GetGalleryViewer request not authorised, User:" + userName.toString());}
					return null;
				}
			}
			
			//**************************************************
			//**********  Passed security checks  **************
			//**************************************************
			CustomResponse customResponse = new CustomResponse();
			Gallery gallery = galleryService.GetGalleryMeta(userId, galleryName, customResponse);
			if (customResponse.getResponseCode() == HttpStatus.OK.value())
			{
				if (useUrlComplex && !urlComplex.equalsIgnoreCase(gallery.getUrlComplex()))
				{
					Thread.sleep(3000);
					String error = "Gallery could not be loaded.  The url didn't match an available gallery, User:" + userName.toString() + " UrlComplex:" + urlComplex;
					model.addAttribute("errorMessage", error);
					meLogger.error(error);
				}
				else
				{
					String presentationJsp = CombineModelAndGallery(userId, userName, model, gallery, false, customResponse);
					if (presentationJsp != null)
						responseJsp = presentationJsp;
				}
			}
			else
			{
				Thread.sleep(3000);
				String error = "Gallery could not be loaded.  The error code received was: " + customResponse.getResponseCode();
				model.addAttribute("errorMessage", error);
				meLogger.error(error);
			}
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetGalleryViewer request completed, User:" + userName.toString() + ", Gallery:" + galleryName + " Response code: " + customResponse.getResponseCode());}

			return responseJsp;
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in GetGalleryViewer", ex);
			model.addAttribute("errorMessage", "Gallery could not be loaded.  Error message: " + ex.getMessage()); 
			return responseJsp;
		}
	}
	
	//  GET /{userName}/gallery/{galleryName}/{sectionId}/{imageCursor}/{size}?preview=true
	@RequestMapping(value="/{userName}/gallery/{galleryName}/{sectionId}/{imageCursor}/{size}", method=RequestMethod.GET, 
			produces=MediaType.TEXT_HTML_VALUE )
	public void GetGalleryImageList(
			@PathVariable("galleryName") String galleryName,
			@PathVariable("sectionId") long sectionId,
			@PathVariable("userName") String userName,
			@PathVariable("imageCursor") int imageCursor,
			@PathVariable("size") int size,
			HttpServletRequest requestObject,
			HttpServletResponse httpResponse)
	{
		Date clientVersionTimestamp = null;
		
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetGalleryImageList request received, User:" + userName + ", Gallery: " + galleryName + " Section Id:" + sectionId);}
		
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				return;
			}
			
			long headerDateLong = requestObject.getDateHeader("If-Modified-Since");
			if (headerDateLong > 0)
			{
				clientVersionTimestamp = new Date(headerDateLong);
			}
			
			CustomResponse customResponse = new CustomResponse();
			
			ImageList imageList = imageService.GetImageList(userId, "gallery", galleryName, sectionId, imageCursor, size, clientVersionTimestamp, customResponse);
			
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(customResponse.getResponseCode());
			
			if (customResponse.getResponseCode() == HttpStatus.OK.value())
			{
				Gallery gallery = galleryService.GetGalleryMeta(userId, galleryName, customResponse);
				if (customResponse.getResponseCode() == HttpStatus.OK.value())
				{
				    PrintWriter out = httpResponse.getWriter();
				    WriteOutImageList(userName, out, gallery, imageList,false);
				}
				else
				{
					httpResponse.setStatus(customResponse.getResponseCode());
				}
			}
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetImageList completed, User:" + userName + ", Gallery: " + galleryName + " Section Id:" + sectionId);}
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in GetGalleryImageList", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
	
	//  GET /{userName}/gallerypreview/sample?key=1234567890
	@RequestMapping(value = { "/{userName}/gallerypreview/sample" }, method = { RequestMethod.GET }, produces=MediaType.APPLICATION_XHTML_XML_VALUE )
	public String GetGalleryPreview(
			@RequestParam(value="key", required=false) String galleryTempId,
			@PathVariable("userName") String userName,
			Model model,
			HttpServletResponse httpResponse)
	{
		String responseJsp = "GalleryPreviewError";
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetGalleryPreview request received, User: " + userName);}

			if (this.sessionState.getUserId() < 1 || !userName.equalsIgnoreCase(this.sessionState.getUserName()))
			{
				Thread.sleep(3000);
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				if (meLogger.isDebugEnabled()) {meLogger.debug("GetGalleryPreview request not authorised, User:" + userName.toString());}
				return null;
			}
			
			if (!this.sessionState.getGalleryTempKey().equalsIgnoreCase(galleryTempId))
			{
				Thread.sleep(3000);
				String error = "GetGalleryPreview request not associated with a valid Gallery, User:" + userName.toString();
				model.addAttribute("errorMessage", error);
				meLogger.error(error);
			}
			else
			{
				CustomResponse customResponse = new CustomResponse();
				String presentationJsp = CombineModelAndGallery(previewUserId, userName, model, this.sessionState.getGalleryPreview(), true, customResponse);
				if (presentationJsp != null)
					responseJsp = presentationJsp;
			}
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetGalleryPreview request completed, User:" + userName.toString());}

			return responseJsp;
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in GetGalleryPreview", ex);
			model.addAttribute("errorMessage", "Preview could not be loaded.  Error message: " + ex.getMessage()); 
			return responseJsp;
		}
	}
	
	//  GET /{userName}/gallerypreview/sample/{sectionId}/{imageCursor}/{size}?preview=true
	@RequestMapping(value="/{userName}/gallerypreview/sample/{sectionId}/{imageCursor}/{size}", method=RequestMethod.GET, 
			produces=MediaType.TEXT_HTML_VALUE )
	public void GetGalleryPreviewImageList(
			@PathVariable("sectionId") long sectionId,
			@PathVariable("userName") String userName,
			@PathVariable("imageCursor") int imageCursor,
			@PathVariable("size") int size,
			HttpServletRequest requestObject,
			HttpServletResponse httpResponse)
	{
		try
		{
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetGalleryPreviewImageList request received, User:" + userName + " Section Id:" + sectionId);}
		
			if (this.sessionState.getUserId() < 1 || !userName.equalsIgnoreCase(this.sessionState.getUserName()))
			{
				Thread.sleep(3000);
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
				if (meLogger.isDebugEnabled()) {meLogger.debug("GetGalleryPreviewImageList request not authorised, User:" + userName.toString());}
			}
			
			ImageList imageList = imageService.GetPreviewImageList(sectionId, size);
			Gallery gallery = this.sessionState.getGalleryPreview();
		    PrintWriter out = httpResponse.getWriter();
			
			WriteOutImageList(userName, out, gallery, imageList, true);
			
			httpResponse.setStatus(HttpStatus.OK.value());
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetGalleryPreviewImageList completed, User:" + userName + " Section Id:" + sectionId);}
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in GetGalleryPreviewImageList", ex);
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
	
	private String CombineModelAndGallery(long userId, String userName, Model model, Gallery gallery, boolean isPreview, CustomResponse customResponse) throws WallaException
	{
		model.addAttribute("userId", userId); 
		model.addAttribute("userName", userName); 
		model.addAttribute("isPreview", isPreview); 
		
		Style style = galleryService.GetStyle(gallery.getStyleId());
		model.addAttribute("css", style.getCssFolder()); 
		
		Presentation presentation = galleryService.GetPresentation(gallery.getPresentationId());
		model.addAttribute("jsp", presentation.getJspName());
		model.addAttribute("imageSize", presentation.getThumbWidth());
		
		model.addAttribute("groupingType", gallery.getGroupingType()); /* 0-None, 1-category, 2-tag */
		
		if (gallery.getGroupingType().intValue() > 0)
			model.addAttribute("sectionList", gallery.getSections().getSectionRef());
		
		model.addAttribute("totalImageCount", gallery.getTotalImageCount()); 

		//Get gallery name and description
		model.addAttribute("name", gallery.getName()); 
		model.addAttribute("desc", gallery.getDesc());
		
		model.addAttribute("showGalleryName", gallery.isShowGalleryName());
		model.addAttribute("showGalleryDesc", gallery.isShowGalleryDesc());
		model.addAttribute("showImageName", gallery.isShowImageName());
		model.addAttribute("showImageDesc", gallery.isShowImageDesc());
		model.addAttribute("showImageMeta", gallery.isShowImageMeta());
		
		if (presentation.getMaxSections() == 0)
		{
			//Get image list embedded into initial jsp response.
			ImageList imageList;
			if (isPreview)
			{
				imageList = imageService.GetPreviewImageList(1, presentation.getMaxImagesInSection());
				model.addAttribute("imageList", imageList);
			}
			else
			{
				imageList = imageService.GetImageList(userId, "gallery", 
						gallery.getName(), -1, 0, presentation.getMaxImagesInSection(), null, customResponse);
				
				if (customResponse.getResponseCode() == HttpStatus.OK.value())
				{
					model.addAttribute("imageList", imageList);
				}
				else
				{
					meLogger.error(customResponse.getMessage());
					model.addAttribute("errorMessage", customResponse.getMessage());
					return null;
				}
			}
		}

		return presentation.getJspName();
	}
	
	private void WriteOutImageList(String userName, PrintWriter out, Gallery gallery, ImageList imageList, boolean isPreview) throws WallaException
	{
	    int lastImage = imageList.getImageCount() + imageList.getImageCursor();
	    
		Presentation presentation = galleryService.GetPresentation(gallery.getPresentationId());
		int imageSize = presentation.getThumbWidth();
		
	    out.println("<section id=\"imagesPane\""
	    		+ " class=\"ImagesPaneStyle\""
	    		+ " data-section-id=\"" + imageList.getSectionId() + "\"" 
	    		+ " data-section-image-count=\"" + imageList.getSectionImageCount() + "\""
	    		+ " data-images-first=\"" + imageList.getImageCursor() + "\""
	    		+ " data-images-last=\"" + lastImage + "\">");

		if (imageList.getImages() != null)
		{
			if (imageList.getImages().getImageRef().size() > 0)
			{
				//Construct update SQL statements
				for (Iterator<ImageList.Images.ImageRef> imageIterater = imageList.getImages().getImageRef().iterator(); imageIterater.hasNext();)
				{
					ImageList.Images.ImageRef current = (ImageList.Images.ImageRef)imageIterater.next();

					String name = "";
					String fullNameDesc = "";
					String imageUrl = "";
					String thumbUrl = "";
					
					if (gallery.isShowImageName() && gallery.isShowGalleryDesc())
					{
						name = current.getName();
						fullNameDesc = current.getName() + ((current.getDesc().length() > 0) ? ". " + current.getDesc() : "");
					}
					else if (gallery.isShowImageName())
					{
						name = current.getName();
						fullNameDesc = current.getName();
					}
					else if (gallery.isShowGalleryDesc())
					{
						name = current.getDesc();
						fullNameDesc = current.getDesc();
					}
					
					if (name == null)
						name = "";
						
					if (fullNameDesc == null)
						fullNameDesc = "";
					
					if (isPreview)
					{
						imageUrl = "../../../ws/" + userName + "/imagepreview/" + current.getId() + "/" + 1920 + "/" + 1080 + "/";
						thumbUrl = "../../../ws/" + userName + "/imagepreview/" + current.getId() + "/" + imageSize + "/" + imageSize + "/";
					}
					else
					{
						imageUrl = "../../../ws/" + userName + "/image/" + current.getId() + "/" + 1920 + "/" + 1080 + "/";
						thumbUrl = "../../../ws/" + userName + "/image/" + current.getId() + "/" + imageSize + "/" + imageSize + "/";
					}
					
					StringBuilder output = new StringBuilder();
					
					if (gallery.isShowImageName() || gallery.isShowGalleryDesc())
					{
						int addHeight = 20;
						
						if (gallery.isShowImageName() || gallery.isShowGalleryDesc())
							addHeight = 40;
						
						output.append("<article class=\"ImagesArticleStyle\" style=\"width:" + imageSize + "px;height:" + (imageSize + addHeight) + "px;\" ");
						output.append("id=\"imageId" + current.getId() + "\" data-image-id=\"" + current.getId() + "\">");

						output.append("<a class=\"image-popup-no-margins\" href=\"" + imageUrl + "\" title=\"" + name + "\">");
						output.append("<img class=\"thumbStyle\" title=\"" + name + "\" src=\"" + thumbUrl + "\"/></a>");
						
						output.append("<div class=\"ImagesArticleStyle\" style=\"width:" + imageSize + "px;height:" + addHeight + "px;\"><span>" + fullNameDesc + "</span></div></article>");
					}
					else
					{
						output.append("<article class=\"ImagesArticleNoNameStyle\" ");
						output.append("id=\"imageId" + current.getId() + "\" data-image-id=\"" + current.getId() + "\">");

						output.append("<a class=\"image-popup-no-margins\" href=\"" + imageUrl + "\" title=\"" + name + "\">");
						output.append("<img class=\"thumbStyle\" title=\"" + name + "\" src=\"" + thumbUrl + "\"/></a>");
					}

					
					
					out.println(output.toString());

				}
			}
		}
		out.println("</section>");
		out.close();
	}
	
}
