package walla.business;

import java.sql.SQLException;
import java.util.*;
import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.db.*;
import walla.utils.*;
import walla.ws.*;

import javax.imageio.ImageIO;
import javax.sql.DataSource;
import javax.xml.datatype.*;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Qualifier;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.*;

import org.im4java.core.IM4JavaException;
import org.im4java.process.*;

@Service("ImageService")
public class ImageService {

	private final String graphicsMagickPath="C:\\Program Files\\GraphicsMagick-1.3;";
	private final String destinationRoot = "C:\\temp\\WallaRepo\\";
	private GalleryDataHelperImpl galleryDataHelper;	
	private CategoryDataHelperImpl categoryDataHelper;
	private TagDataHelperImpl tagDataHelper;
	private ImageDataHelperImpl imageDataHelper;
	private UtilityDataHelperImpl utilityDataHelper;
	
	private TagService tagService;
	private CategoryService categoryService;
	private CachedData cachedData;
	
	private static final Logger meLogger = Logger.getLogger(ImageService.class);
	
	//*************************************************************************************************************
	//***********************************  Web server synchronous methods *****************************************
	//*************************************************************************************************************
	public ImageService()
	{
		ProcessStarter.setGlobalSearchPath(graphicsMagickPath);
	}
	
	public int CreateUpdateImageMeta(long userId, ImageMeta imageMeta, long imageId)
	{
		try {
			
			meLogger.debug("CreateUpdateImageMeta() begins. UserId:" + userId + " ImageId:" + imageId);
			
			//TODO Check User is logged in with Write permission
			//HttpStatus.UNAUTHORIZED.value()
			
			int responseCode = 0;
			
			if (imageMeta.getId() != imageId)
			{
				throw new WallaException("ImageService", "CreateUpdateImageMeta", "ImageId does not match Image Object data", HttpStatus.CONFLICT.value()); 
			}
			
			if (imageMeta.getStatus().intValue() == 1)
			{
				//TODO change to queued process.
				imageDataHelper.CreateImage(userId, imageMeta);
				responseCode = HttpStatus.CREATED.value();
				
				//TODO decouple.
				SetupNewImage(userId, imageId);
			}
			else if (imageMeta.getStatus().intValue() == 4)
			{
				imageDataHelper.UpdateImage(userId, imageMeta);
				responseCode = HttpStatus.OK.value();
				
				//TODO Add queued process to update any views\tags
			}
			else
			{
				throw new WallaException("ImageService", "CreateUpdateImageMeta", "Image object is not in a valid state for this action", HttpStatus.CONFLICT.value());
			}
			
			meLogger.debug("CreateUpdateImageMeta() has completed. UserId:" + userId);
			return responseCode;
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process CreateUpdateImageMeta", wallaEx);
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces CreateUpdateImageMeta", ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
	}

	public ImageMeta GetImageMeta(long userId, long imageId, CustomResponse customResponse)
	{
		try {
			//Check user can access tag list
			//HttpStatus.UNAUTHORIZED.value()
			
			meLogger.debug("GetImageMeta begins. UserId:" + userId + " ImageId:" + imageId);
			
			//Get tag list for response.
			ImageMeta imageMeta = imageDataHelper.GetImageMeta(userId, imageId);
			if (imageMeta == null)
			{
				String error = "GetImageMeta didn't return a valid Image object";
				meLogger.error(error);
				throw new WallaException("ImageService", "GetImageMeta", error, HttpStatus.NO_CONTENT.value()); 
			}
			else if (imageMeta.getStatus() == 5)
			{
				String error = "Image has now been deleted.";
				meLogger.error(error);
				throw new WallaException("ImageService", "GetImageMeta", error, HttpStatus.GONE.value()); 
			}
			
			meLogger.debug("GetImageMeta has completed. UserId:" + userId);
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			return imageMeta;
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process GetImageMeta", wallaEx);
			customResponse.setResponseCode(wallaEx.getCustomStatus());
			return null;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to process GetImageMeta",ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}
	
	public int DeleteImages(long userId, ImageList imagesToDelete)
	{
		try {
			meLogger.debug("DeleteImages begins. UserId:" + userId);

			//Mark images as inactive.
			imageDataHelper.MarkImagesAsInactive(userId, imagesToDelete);
			
			//TODO Decouple method call
			ImageDeletePermanent(userId, imagesToDelete);
			
			meLogger.debug("DeleteImages has completed. UserId:" + userId);
			
			return HttpStatus.OK.value();
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process DeleteImages");
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces DeleteImages", ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
	}

	public UploadStatusList GetUploadStatusList(long userId, ImageIdList imageIdToCheck, List<Long> filesReceived, CustomResponse customResponse) throws WallaException
	{
		try {
			meLogger.debug("GetUploadStatusList begins. UserId:" + userId);

			UploadStatusList currentUploadList = imageDataHelper.GetCurrentUploads(userId, imageIdToCheck);
			
			for (int i = 0; i < filesReceived.size(); i++)
			{
				boolean found = false;
				for (int ii = 0; ii < currentUploadList.getImageUploadRef().size(); ii++)
				{
					if (filesReceived.get(i) == currentUploadList.getImageUploadRef().get(ii).getImageId())
					{
						found = true;
						
					}
				}
				
				if (!found)
				{
					UploadStatusList.ImageUploadRef imageRef = new UploadStatusList.ImageUploadRef();
					imageRef.setImageId(filesReceived.get(i));
					imageRef.setStatus(1);

					currentUploadList.getImageUploadRef().add(imageRef);
				}
			}
			
			customResponse.setResponseCode(HttpStatus.OK.value());
					
			meLogger.debug("GetUploadStatusList has completed. UserId:" + userId);
			
			return currentUploadList;
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process GetUploadStatusList");
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces GetUploadStatusList", ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}

	public long GetImageId(long userId)
	{
		try {
			
			meLogger.debug("GetImageId() begins. UserId:" + userId);
			
			//TODO Check User is logged in with Write permission
			//HttpStatus.UNAUTHORIZED.value()
			
			long newImageId = utilityDataHelper.GetNewId("ImageId");

			meLogger.debug("GetImageId() has completed. UserId:" + userId);
			
			return newImageId;
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process GetImageId");
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces GetImageId", ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
	}
	
	public ImageList GetImageList(long userId, String type, String identity, long sectionId, int imageCursor, int size, Date clientVersionTimestamp, CustomResponse customResponse)
	{
		//identity - For Category, this must be the category Id, for tags and galleries, this is the name.
		try
		{
			meLogger.debug("GetImageList() begins. UserId:" + userId + " Type:" + type + " Name:" + identity.toString());

			ImageList imageList = null;
			switch (type.toUpperCase())
			{
				case "TAG":
					imageList = tagDataHelper.GetTagImageListMeta(userId, identity);
					break;
				case "CATEGORY":
					try
					{
						long categoryId = Long.parseLong(identity);
						imageList = categoryDataHelper.GetCategoryImageListMeta(userId, categoryId);
					}
					catch (NumberFormatException ex)
					{
						meLogger.debug("An invalid category id was supplied.  Id:" + identity.toString());
						customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
					}
					break;
				case "GALLERY":
					imageList = galleryDataHelper.GetGalleryImageListMeta(userId, identity, sectionId);
					break;
				default:
					String error = "GetImageList process failed, an invalid image list type was supplied:" + type;
					meLogger.error(error);
					throw new WallaException("ImageService", "GetImageList", error, HttpStatus.BAD_REQUEST.value()); 
			}

			//Check if gallery list changed
			if (clientVersionTimestamp != null)
			{
				Date lastUpdated = new Date(imageList.getLastChanged().toGregorianCalendar().getTimeInMillis());
				if (!lastUpdated.after(clientVersionTimestamp))
				{
					meLogger.debug("No image list generated because server timestamp (" + lastUpdated.toString() + ") is not later than client timestamp (" + clientVersionTimestamp.toString() + ")");
					customResponse.setResponseCode(HttpStatus.NOT_MODIFIED.value());
					return null;
				}
			}
			
			if (imageList.getTotalImageCount() > 0)
			{
				switch (type.toUpperCase())
				{
					case "TAG":
						tagDataHelper.GetTagImages(userId, imageCursor, size, imageList);
						break;
					case "CATEGORY":
						categoryDataHelper.GetCategoryImages(userId,imageCursor, size, imageList);
						break;
					case "GALLERY":
						galleryDataHelper.GetGalleryImages(userId, imageCursor, size, imageList);
						break;
				}
			}
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			
			meLogger.debug("GetImageList() has completed. UserId:" + userId + " Type:" + type + " Name:" + identity.toString());
			return imageList;
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process GetImageList");
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces GetImageList", ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}
	
	public ImageList GetPreviewImageList(long sectionId, int size)
	{
		ImageList imagesList = new ImageList();
		
		imagesList.setId(sectionId);
		imagesList.setImageCount(size);
		imagesList.setImageCursor(0);
		imagesList.setName("Sample name");
		imagesList.setDesc("Sample description");
		imagesList.setSectionId(sectionId);
		imagesList.setSectionImageCount(size);
		imagesList.setTotalImageCount(size);
		imagesList.setType("Gallery");
		
		ImageList.Images images = new ImageList.Images();
		
		for (int i = 0; i < size; i++)
		{
			ImageList.Images.ImageRef image = new ImageList.Images.ImageRef();
			image.setName("Sample foto " + i);
			image.setDesc("Sample description");
			image.setId((long)UserTools.RandInt(1, 10));
			images.getImageRef().add(image);
		}
		
		imagesList.setImages(images);
		
		return imagesList;
	}
	
	private String GetFilePathIfExists(long userId, String sizeFolder, long imageId, boolean isPreview)
	{
		/* Check for file exists, return path if present */
		Path folderPath = null;
		
		if (isPreview)
			folderPath = Paths.get(destinationRoot, "Preview", sizeFolder);
		else
			folderPath = Paths.get(destinationRoot, "Saved", String.valueOf(userId), sizeFolder);

		File file = UserTools.FileExistsNoExt(folderPath.toString(), String.valueOf(imageId));
		if (file != null)
		{
			return file.getPath();
		}
		return "";
	}
	
	/*
	private void ResizeAndSaveFile(long userId, String sourceImagePath, ImageMeta imageMeta, int width, int height, String folder, boolean isMain) throws IOException, InterruptedException, IM4JavaException
	{

		if (isMain)
		{
			ImageUtilityHelper.SaveMainImage(userId, sourceImagePath, imageDestinationPath, width, height);
			//Check if switch is needed.

			if (ImageUtilityHelper.CheckForPortrait(imageDestinationPath))
			{
				//Resize with portrait dimensions.
				ImageUtilityHelper.DeleteImage(imageDestinationPath);
				ImageUtilityHelper.SaveMainImage(userId, sourceImagePath, imageDestinationPath, height, width);
			}
		}
		else
		{
			ImageUtilityHelper.SaveReducedSizeImages(userId, sourceImagePath, imageDestinationPath, imageMeta, width, height);
		}
	}
	*/
	
	private String GetFolderPathOrCreate(long userId, String sizeFolder, boolean isPreview)
	{
		Path folderPath = null;
		if (isPreview)
			folderPath = Paths.get(destinationRoot, "Preview", sizeFolder);
		else
			folderPath = Paths.get(destinationRoot, "Saved", String.valueOf(userId), sizeFolder);
		
		File folder = folderPath.toFile();
		if (!folder.exists())
			folder.mkdir();
		  
		return folder.getPath();
	}
	
	private String GetFilePathCreateFolder(long userId, String sizeFolder, long imageId, boolean isPreview)
	{
		Path folderPath = null;
		if (isPreview)
			folderPath = Paths.get(destinationRoot, "Preview", sizeFolder);
		else
			folderPath = Paths.get(destinationRoot, "Saved", String.valueOf(userId), sizeFolder);
		
		File folder = folderPath.toFile();
		if (!folder.exists())
			folder.mkdir();
		  
		return Paths.get(folderPath.toString(), String.valueOf(imageId) + ".jpg").toString();
	}
	
	private void ResizeAndSaveFile(long userId, String sourceImagePath, String destinationImagePath, int width, int height, boolean isMain) throws IOException, InterruptedException, IM4JavaException
	{
		if (isMain)
		{
			ImageUtilityHelper.SaveMainImage(userId, sourceImagePath, destinationImagePath, width, height);
			//Check if switch is needed.

			if (ImageUtilityHelper.CheckForPortrait(destinationImagePath))
			{
				//Resize with portrait dimensions.
				ImageUtilityHelper.DeleteImage(destinationImagePath);
				ImageUtilityHelper.SaveMainImage(userId, sourceImagePath, destinationImagePath, height, width);
			}
		}
		else
		{
			ImageUtilityHelper.SaveReducedSizeImages(userId, sourceImagePath, destinationImagePath, width, height);
		}
	}
	
	public File GetOriginalImageFile(long userId, long imageId, CustomResponse customResponse)
	{
		try
		{
			String path = GetFilePathIfExists(userId, "Original", imageId, false);
			if (path.isEmpty())
			{
				customResponse.setMessage("Original image file could not be retreived.  ImageId:" + imageId);
				customResponse.setResponseCode(HttpStatus.GONE.value());
			}
			customResponse.setResponseCode(HttpStatus.OK.value());
			return new File(path);
		}
		catch (Exception ex)
		{
			meLogger.error("Unexpected error when trying to proces GetOriginalImageFile", ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}
	
	public BufferedImage GetScaledImageFile(long userId, long imageId, int width, int height, boolean isPreview, CustomResponse customResponse)
	{
		try
		{
			String sourceFile;
			String destFile;
			/*
			ImageMeta imageMeta = null;
			if (isPreview)
			{
				imageMeta = new ImageMeta();
				imageMeta.setId(imageId);
				imageMeta.setWidth(1920);
				imageMeta.setHeight(1080);
			}
			*/
			
			
			//Check for aspect ratio, supported ratio is 1.0 or 1.77
			double requestAspectRatio = (double)width / (double)height;
			requestAspectRatio = UserTools.DoRound(requestAspectRatio,2);
			
			if (requestAspectRatio == 1.0 || requestAspectRatio == 1.78)
			{
				if (requestAspectRatio == 1.0)
				{
					if (width > 800)
					{
						String error = "Image size requested is not supported.  Size is too large for the aspect Ratio:" 
								+ String.valueOf(requestAspectRatio) + " Width:" + width;
						throw new WallaException("ImageService", "GetImageFile", error, HttpStatus.BAD_REQUEST.value());
					}
					
					String folder = null;
					switch (width)
					{
						case 20:
							folder = "20x20";
							break;
						case 75:
							folder = "75x75";
							break;
						case 300:
							folder = "300x300";
							break;
						case 800:
							folder = "800x800";
							break;
					}

					boolean doResize = false;
					if (folder == null)
					{
						//Need to dynamically resize image
						doResize = true;
						if (width<20)
						{
							folder = "20x20";
						}
						else if (width<75)
						{
							folder = "75x75";	
						}
						else if (width<300)
						{
							folder = "300x300";	
						}
						else
						{
							folder = "800x800";
						}
					}
					
					//Check for file existing
					String imageFilePath = GetFilePathIfExists(userId, folder, imageId,isPreview);
					if (imageFilePath.isEmpty())
					{
						//if (!isPreview)
						//	imageMeta = imageDataHelper.GetImageMeta(userId, imageId);
						
						//if (imageMeta == null)
						//{
						//	String error = "GetImageFile didn't return a valid Image object. UserId:" + userId + " ImageId:" + imageId;
						//	throw new WallaException("ImageService", "GetImageFile", error, HttpStatus.INTERNAL_SERVER_ERROR.value());
						//}
						
						//File not present, so create it.
						String masterImageFilePath = GetFilePathIfExists(userId, "MainCopy", imageId, isPreview);
						if (masterImageFilePath.isEmpty())
						{
							//Master file not present, so create a new one.
							String originalFilePath = GetFilePathIfExists(userId, "Original", imageId, isPreview);
							if (originalFilePath.isEmpty())
							{
								String error = "GetImageFile didn't find a valid original Image object to process. UserId:" + userId + " ImageId:" + imageId;
								throw new WallaException("ImageService", "GetImageFile", error, HttpStatus.INTERNAL_SERVER_ERROR.value());
							}
							
							destFile = GetFilePathCreateFolder(userId, "MainCopy", imageId, isPreview);
							ResizeAndSaveFile(userId, originalFilePath, destFile, 1920, 1080, true);
							
							masterImageFilePath = destFile;
						}

						int newWidth = Integer.valueOf(folder.substring(0, folder.indexOf("x")));
						int newHeight = Integer.valueOf(folder.substring(folder.indexOf("x")+1));
						
						destFile = GetFilePathCreateFolder(userId, newWidth + "x" + newHeight, imageId, isPreview);
						ResizeAndSaveFile(userId, masterImageFilePath, destFile, newWidth, newHeight, false);
						
						imageFilePath = GetFilePathIfExists(userId, folder, imageId, isPreview);
					}
					
					if (doResize)
					{
				    	//BufferedImage bufferedImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB); 
				    	
				    	BufferedImage originalImage = ImageIO.read(new File(imageFilePath));
				    	int type = originalImage.getType() == 0? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
				    	
				    	BufferedImage resizedImage = new BufferedImage(width, height, type);
				    	Graphics2D g = resizedImage.createGraphics();
				    	g.drawImage(originalImage, 0, 0, width, height, null);
				    	g.dispose();
				    	
				    	customResponse.setResponseCode(HttpStatus.OK.value());
				    	return resizedImage;
					}
					else
					{
						customResponse.setResponseCode(HttpStatus.OK.value());
						return ImageIO.read(new File(imageFilePath));
					}
				}
				else
				{
					//If aspect ratio is 1.77 and requested size is greater than 1080, then this is too large for the main copy.
					if (height > 1080)
					{
						String error = "Image size requested is not supported.  Size is too large for the aspect Ratio:" 
								+ String.valueOf(requestAspectRatio) + " Height:" + width;
						throw new WallaException("ImageService", "GetImageFile", error, HttpStatus.BAD_REQUEST.value());
					}
					
					String imageFilePath = GetFilePathIfExists(userId, "MainCopy", imageId, isPreview);
					if (imageFilePath.isEmpty())
					{
						//Master file not present, so create a new one.
						String originalFilePath = GetFilePathIfExists(userId, "Original", imageId, isPreview);
						if (originalFilePath.isEmpty())
						{
							String error = "GetImageFile didn't find a valid original Image object to process. UserId:" + userId + " ImageId:" + imageId;
							throw new WallaException("ImageService", "GetImageFile", error, HttpStatus.INTERNAL_SERVER_ERROR.value());
						}
						
						destFile = GetFilePathCreateFolder(userId, "MainCopy", imageId, isPreview);
						ResizeAndSaveFile(userId, originalFilePath, destFile, 1920, 1080, true);
						
						imageFilePath = destFile;
					}
					
					if (height < 1080)
					{
				    	BufferedImage originalImage = ImageIO.read(new File(imageFilePath));
				    	int type = originalImage.getType() == 0? BufferedImage.TYPE_INT_ARGB : originalImage.getType();

						double currenctAspectRatio = (double)originalImage.getWidth() / (double)originalImage.getHeight();
						currenctAspectRatio = UserTools.DoRound(currenctAspectRatio,2);
						double newHeight = (double)width / currenctAspectRatio;

				    	BufferedImage resizedImage = new BufferedImage(width, (int)Math.round(newHeight), type);
				    	Graphics2D g = resizedImage.createGraphics();
				    	g.drawImage(originalImage, 0, 0, width, (int)Math.round(newHeight), null);
				    	g.dispose();
				    	
				    	customResponse.setResponseCode(HttpStatus.OK.value());
				    	return resizedImage;
					}
					else
					{
						//No resize needed.
						customResponse.setResponseCode(HttpStatus.OK.value());
						return ImageIO.read(new File(imageFilePath));
					}
				}
			}
			else
			{
				String error = "Image size requested is not supported.  Aspect ratio doesnt match a known type.  Aspect Ratio:" + String.valueOf(requestAspectRatio);
				throw new WallaException("ImageService", "GetImageFile", error, HttpStatus.BAD_REQUEST.value());
			}
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process GetImageFile", wallaEx);
			customResponse.setResponseCode(wallaEx.getCustomStatus());
			return null;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to process GetImageFile",ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		
	}
	
	public BufferedImage GetMainCopyImageFile(long userId, long imageId, boolean isPreview, CustomResponse customResponse)
	{
		try
		{
			/*
			String storeFolder = "Saved";
			ImageMeta imageMeta = null;
			
			if (isPreview)
			{
				storeFolder = "Preview";
				imageMeta = new ImageMeta();
				imageMeta.setId(imageId);
				imageMeta.setWidth(1920);
				imageMeta.setHeight(1080);
			}
			*/
			
			String imageFilePath = GetFilePathIfExists(userId, "MainCopy", imageId, isPreview);
			
			if (imageFilePath.isEmpty())
			{
				/*
				if (!isPreview)
					imageMeta = imageDataHelper.GetImageMeta(userId, imageId);
				
				if (imageMeta == null)
				{
					String error = "GetMainCopyImageFile didn't return a valid Image object. UserId:" + userId + " ImageId:" + imageId;
					throw new WallaException("ImageService", "GetMainCopyImageFile", error, HttpStatus.INTERNAL_SERVER_ERROR.value());
				}
				*/
				
				//Master file not present, so create a new one.
				String sourceFolder = GetFilePathIfExists(userId, "Original", imageId, isPreview);
				String destFolder = GetFilePathCreateFolder(userId, "MainCopy", imageId, isPreview);
				
				ResizeAndSaveFile(userId, sourceFolder, destFolder, 1920, 1080, true);
				imageFilePath = GetFilePathIfExists(userId, "MainCopy", imageId, isPreview);
			}
			
			//No resize needed.
			customResponse.setResponseCode(HttpStatus.OK.value());
			return ImageIO.read(new File(imageFilePath));
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to process GetMainCopyImageFile",ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}
	
	public BufferedImage GetAppImageFile(String imageRef, int width, int height, CustomResponse customResponse)
	{
		try
		{
			//Check for folder existing.  //Check for image existing.
			Path filePath = Paths.get(destinationRoot,"Application", width + "x" + height, imageRef + ".jpg");
			File filePathTemp = filePath.toFile();
    		if (!filePathTemp.exists())
    		{
				String error = "Image not found. ImageRef: " + imageRef + " Height: " + height + " Width: " + width;
				throw new WallaException("ImageService", "GetAppImageFile", error, HttpStatus.BAD_REQUEST.value());
    		}

			//Return image.
			customResponse.setResponseCode(HttpStatus.OK.value());
			return ImageIO.read(new File(filePath.toString()));
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process GetAppImageFile", wallaEx);
			customResponse.setResponseCode(wallaEx.getCustomStatus());
			return null;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to process GetAppImageFile",ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		
	}

	
	//*************************************************************************************************************
	//*************************************  Messaging initiated methods ******************************************
	//*************************************************************************************************************
	
	public void SetupNewImage(long userId, long imageId)
	{
		//TODO Change folder default to properties file.
		
		try
		{
			meLogger.debug("SetupNewImage begins. UserId:" + userId + " ImageId:" + imageId);
			
			//Update image to being processed.
			imageDataHelper.UpdateImageStatus(userId, imageId, 3, false, "");
			
			File uploadedFile = Paths.get(destinationRoot, "Que", String.valueOf(imageId) + "." + Long.toString(userId)).toFile();
			if (!uploadedFile.exists())
			{
				String error = "Uploaded file could not be found.  ImageId:" + imageId + " UserId:" + userId;
				throw new WallaException("ImageService", "SetupNewImage", error, 0); 
			}

			ImageMeta imageMeta = imageDataHelper.GetImageMeta(userId, imageId);
			if (imageMeta == null)
			{
				String error = "SetupNewImage didn't return a valid Image object. UserId:" + userId + " ImageId:" + imageId;
				throw new WallaException("ImageService", "SetupNewImage", error, 0); 
			}
			
			if (imageMeta.getStatus().intValue() != 3)
			{
				String error = "SetupNewImage didn't return an Image object with the correct status. UserId:" + userId + " ImageId:" + imageId + " Status:" + imageMeta.getStatus();
				throw new WallaException("ImageService", "SetupNewImage", error, 0);
			}

			/**************************************************************************/
			/****************** Enrich with Exif & Save image copies ******************/
			/**************************************************************************/
			
			String userOriginalFolderPath = GetFolderPathOrCreate(userId, "Original", false);

			//Archive original image
    		String originalImagePath = ImageUtilityHelper.SaveOriginal(userId, uploadedFile.getPath(), userOriginalFolderPath, imageId, imageMeta.getFormat());
    		
    		//Make one initial copy, to drive subsequent resizing and also to orient correctly.
			String mainImagePath = GetFilePathCreateFolder(userId, "MainCopy", imageId, false);
			ResizeAndSaveFile(userId, originalImagePath, mainImagePath, 1920, 1080, true);

			//String mainImagePath = GetFilePathIfExists(userId, "MainCopy", imageId, false);
			//if (mainImagePath.isEmpty())
			//{
			//	String error = "Unexpected error retrieving a resized image in the folder: MainCopy.  ImageId:" + imageId;
			//	throw new WallaException("ImageService", "SetupNewImage", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 
			//}
    				
			//Load image meta into memory and enrich properties.
			//TODO switch to wired class.
			String response = ImageUtilityHelper.EnrichImageMetaFromFileData(originalImagePath, imageMeta);
			if (!response.equals("OK"))
				throw new WallaException("ImageService", "SetupNewImage", response, 0); 
            
			ImageUtilityHelper.SwitchHeightWidth(mainImagePath, imageMeta);
    		
			imageDataHelper.UpdateImage(userId, imageMeta);
    		
        	/*
	       	 PC 1600x900 - 1.77, 1024x768 - 1.33
	       	 iPhone 1136x640 - 1.75, 960x640 - 1.5
	       	 iPad 2048x1536 - 1.33
	       	 Surface2 1920x1080 - 1.77
	       	 */
			
			String destFile = GetFilePathCreateFolder(userId, "75x75", imageId, false);
			ResizeAndSaveFile(userId,mainImagePath, destFile, 75, 75, false);
			
			destFile = GetFilePathCreateFolder(userId, "300x300", imageId, false);
			ResizeAndSaveFile(userId,mainImagePath, destFile, 300, 300, false);
			
			destFile = GetFilePathCreateFolder(userId, "800x800", imageId, false);
			ResizeAndSaveFile(userId,mainImagePath, destFile, 800, 800, false);

            //TODO Delete original uploaded image.
			ImageUtilityHelper.DeleteImage(uploadedFile.getPath());
			
			
			imageDataHelper.UpdateImageStatus(userId, imageId, 4, false, "");
			
			//For Each Tag associated, call TagRippleUpdates decoupled
			if (imageMeta.getTags() != null)
			{
				if (imageMeta.getTags().getTagRef().size() > 0)
				{
					for(ImageMeta.Tags.TagRef tagRef : imageMeta.getTags().getTagRef())
					{
						//TODO decouple this method.
						tagService.TagRippleUpdate(userId, tagRef.getId());
					}
				}
			}
			
			//TODO decouple
			tagService.ReGenDynamicTags(userId);
			
			//TODO For the category, call CategoryRippleUpdates decoupled		
			categoryService.CategoryRippleUpdate(userId, imageMeta.getCategoryId());
			
			meLogger.debug("SetupNewImage has completed. UserId:" + userId + " ImageId:" + imageId);
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to process SetupNewImage",ex);
			
			try {MoveImageToErrorFolder(userId, imageId);} catch (Exception logOrIgnore) {}
			try {imageDataHelper.UpdateImageStatus(userId, imageId, -1, true, ex.getMessage());} catch (Exception logOrIgnore) {}
			
		}
	}
	

	
	private void MoveImageToErrorFolder(long userId, long imageId)
	{
		Path sourceFile = Paths.get(destinationRoot, "Que", String.valueOf(imageId) + "." + Long.toString(userId));
		Path destinationFile = Paths.get(destinationRoot, "Error", String.valueOf(imageId) + "." + Long.toString(userId));
		
		File source = sourceFile.toFile();
		if (source.exists())
		{
			UserTools.MoveFile(sourceFile.toString(), destinationFile.toString(), meLogger);
		}
	}
	
	public void DeleteAllImagesCategory(long userId, long[] categoryIds)
	{
		try
		{
			//Select all images in the deleted categories.
			ImageList imagesToDelete = imageDataHelper.GetActiveImagesInCategories(userId, categoryIds);
			
			DeleteImages(userId, imagesToDelete);
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process TagRippleUpdates");
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces TagRippleUpdates", ex);
		}
	}
	
	public void ImageDeletePermanent(long userId, ImageList imagesToDelete)
	{
		try
		{
			//Distinct tag list, a valid tagimage record
			long[] tags = imageDataHelper.GetTagsLinkedToImages(userId, imagesToDelete);
			for (int i = 0; i < tags.length; i++)
			{
				//TODO decouple this method.
				tagService.TagRippleUpdate(userId, tags[i]);
			}

			//Distinct list of categories - check if category still Active.
			long[] categories = imageDataHelper.GetCategoriesLinkedToImages(userId, imagesToDelete);
			for (int i = 0; i < categories.length; i++)
			{
				//TODO decouple this method.
				categoryService.CategoryRippleUpdate(userId, categories[i]);
			}

			tagService.ReGenDynamicTags(userId);
			
			//Clear up physical files.
			//Remove all temporary files stored.
			//Archive originals into Glacial storage.
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process TagRippleUpdates");
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces TagRippleUpdates", ex);
		}
	}
	
	public void setImageDataHelper(ImageDataHelperImpl imageDataHelper)
	{
		this.imageDataHelper = imageDataHelper;
	}
	
	public void setCachedData(CachedData cachedData)
	{
		this.cachedData = cachedData;
	}
	
	public void setUtilityDataHelper(UtilityDataHelperImpl utilityDataHelper)
	{
		this.utilityDataHelper = utilityDataHelper;
	}

	public void setGalleryDataHelper(GalleryDataHelperImpl galleryDataHelper)
	{
		this.galleryDataHelper = galleryDataHelper;
	}
	
	public void setCategoryDataHelper(CategoryDataHelperImpl categoryDataHelper)
	{
		this.categoryDataHelper = categoryDataHelper;
	}
	
	public void setTagDataHelper(TagDataHelperImpl tagDataHelper)
	{
		this.tagDataHelper = tagDataHelper;
	}
	
	
	public void setTagService(TagService tagService)
	{
		this.tagService = tagService;
	}
	
	public void setCategoryService(CategoryService categoryService)
	{
		this.categoryService = categoryService;
	}
	
}