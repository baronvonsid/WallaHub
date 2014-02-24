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

			boolean checkUrl = false;
			boolean securityPassed = false;
			
			if (urlComplex != null)
				checkUrl = true;
			
			//Retrieve user id and check user is valid for the login.
			long userId = UserTools.CheckUser(userName /* ,to add OAuth entity */);
			if (userId < 0)
			{
				httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
			}
	
			CustomResponse customResponse = new CustomResponse();
			Gallery gallery = galleryService.GetGalleryMeta(userId, galleryName, customResponse);
			if (customResponse.getResponseCode() == HttpStatus.OK.value())
			{
				if (checkUrl)
				{
					if (urlComplex.equalsIgnoreCase(gallery.getUrlComplex()))
						securityPassed = true;
					meLogger.debug("Complex url checked out for gallery, user, url combination.");
				}
				else
				{
					meLogger.info("Password protected"); 
					securityPassed = true;
				}
				
				if (securityPassed)
				{
					model.addAttribute("userId", userId); 
					model.addAttribute("userName", userName); 
					
					//TODO - switch to cached item.
					model.addAttribute("css", UserTools.GetCssName(gallery.getStyleId())); 
					model.addAttribute("groupingType", gallery.getGroupingType()); /* 0-None, 1-category, 2-tag */
					if (gallery.getGroupingType().intValue() > 0)
						model.addAttribute("sectionList", gallery.getSections().getSectionRef());
					
					model.addAttribute("totalImageCount", gallery.getTotalImageCount()); 
					
					//Get gallery viewer style
					responseJsp = UserTools.GetJspName(gallery.getPresentationId());
					
					//Get gallery name and description
					model.addAttribute("name", gallery.getName()); 
					model.addAttribute("desc", gallery.getDesc());
					
					model.addAttribute("showGalleryName", gallery.isShowGalleryName());
					model.addAttribute("showGalleryDesc", gallery.isShowGalleryDesc());
					model.addAttribute("showImageName", gallery.isShowImageName());
					model.addAttribute("showImageDesc", gallery.isShowImageDesc());
					model.addAttribute("showImageMeta", gallery.isShowImageMeta());
					
					if (responseJsp.equals("GalleryViewer-Lightbox"))
					{
						int imageListMax = 1000;
						ImageList imageList = imageService.GetImageList(userId, "gallery", 
								galleryName, -1, 0, imageListMax, null, customResponse);

						if (customResponse.getResponseCode() == HttpStatus.OK.value())
						{
							model.addAttribute("imageList", imageList);
						}
					}
				}
				else
				{
					//No match, no session created.
					//Pause for 1 second and return error.
					Thread.sleep(1000);
					customResponse.setResponseCode(HttpStatus.UNAUTHORIZED.value());
					model.addAttribute("errorMessage", "Gallery could not be loaded.  The error code received was: " + customResponse.getResponseCode()); 
				}
			}
			else
			{
				model.addAttribute("errorMessage", "Gallery could not be loaded.  The error code received was: " + customResponse.getResponseCode()); 
			}
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetGalleryViewer request completed, User:" + userName.toString() + ", Gallery:" + galleryName + " Response code: " + customResponse.getResponseCode());}

			//httpResponse.setStatus(customResponse.getResponseCode());
			
			return responseJsp;
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in GetGalleryViewer", ex);
			model.addAttribute("errorMessage", "Gallery could not be loaded.  Error message: " + ex.getMessage()); 
			
			return responseJsp;
		}
	}
	
	
	//, headers={"Accept-Charset=utf-8"}
	
	//  GET /{userName}/gallery/{galleryName}/{sectionId}/{imageCursor}/{size}
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
			//TODO change class based on gallery presentation settings.
			boolean noName = true;
			
			//Get image size from cache object.  Used for image URL construction.
			String imageSize = "250";
			
			httpResponse.addHeader("Cache-Control", "no-cache");
			httpResponse.setStatus(customResponse.getResponseCode());
			
			if (customResponse.getResponseCode() == HttpStatus.OK.value())
			{
				Gallery gallery = galleryService.GetGalleryMeta(userId, galleryName, customResponse);
				if (customResponse.getResponseCode() == HttpStatus.OK.value())
				{
				    PrintWriter out = httpResponse.getWriter();
				    
				    int lastImage = imageList.getImageCount() + imageList.getImageCursor();
				    
				    out.println("<section id=\"imagesPane\""
				    		+ " class=\"ImagesPaneStyle\""
				    		+ " data-section-id=\"" + sectionId + "\"" 
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
								
								if (current.getName() == null || current.getName().isEmpty())
									current.setName("");
								
								if (current.getDesc() == null || current.getDesc().isEmpty())
									current.setDesc("");
								
								String name = "";
								String fullNameDesc = "";
								
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
								
								String imageUrl = "../../../ws/" + userName + "/image/" + current.getId() + "/" + 1920 + "/" + 1080 + "/";
								String thumbUrl = "../../../ws/" + userName + "/image/" + current.getId() + "/" + imageSize + "/" + imageSize + "/";
								
								out.println("<article class=\"" + (noName ? "ImagesArticleNoNameStyle" : "ImagesArticleStyle") + "\""
										+ "id=\"imageId" + current.getId() + "\""
										+ "data-image-id=\"" + current.getId() + "\">");
								 
								out.println("<a class=\"image-popup-no-margins\" href=\"" + imageUrl + "\" title=\"" + fullNameDesc + "\">");
								out.println("<img class=\"thumbStyle\" title=\"" + name + "\""
										+ " src=\"" + thumbUrl + "\"/>");
								out.println("</a>");
								
								/*
								if (!noName)
								{
									out.println("<div class=\"ImagesArticleStyle\"><span>" + current.getName() + "</span></div>");
								}
								*/
								out.println("</article>");

							}
						}
					}
					
					out.println("</section>");
					out.close();
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
	

}
