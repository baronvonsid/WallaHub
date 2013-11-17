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
			
			if (imageMeta.getStatus().intValue() == 0)
			{
				//TODO change to queued process.
				imageDataHelper.CreateImage(userId, imageMeta);
				responseCode = HttpStatus.CREATED.value();
				
				//TODO decouple.
				SetupNewImage(userId, imageId);
			}
			else if (imageMeta.getStatus().intValue() == 3)
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

	public ImageMeta GetImageMeta(long userId, long imageId, long machineId, CustomResponse customResponse)
	{
		try {
			//Check user can access tag list
			//HttpStatus.UNAUTHORIZED.value()
			
			meLogger.debug("GetImageMeta begins. UserId:" + userId + " ImageId:" + imageId);
			
			//Get tag list for response.
			ImageMeta imageMeta = imageDataHelper.GetImageMeta(userId, imageId, machineId);
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

	public void UpdateUploadStatusList(long userId, UploadStatusList existingUploadList)
	{
		try {
			meLogger.debug("UpdateUploadStatusList begins. UserId:" + userId);
			
			//TODO Check User is logged in with Write permission
			//HttpStatus.UNAUTHORIZED.value()
			
			//First loop removes any cached items which are old
			//Gets an array of those images which care currently in the cache and we need to check. 
			long[] imageIdToCheck = new long[existingUploadList.getImageUploadRef().size()];
			int checkCount = 0;
			UploadStatusList.ImageUploadRef[] toRemove = new UploadStatusList.ImageUploadRef[existingUploadList.getImageUploadRef().size()];
			int count = 0;
			
			for(UploadStatusList.ImageUploadRef existingImageStatus: existingUploadList.getImageUploadRef())
			{
				if (existingImageStatus.getImageStatus().equals(0) || existingImageStatus.getImageStatus().equals(1) || existingImageStatus.getImageStatus().equals(4))
				{
					GregorianCalendar calMinusTwoDays = new GregorianCalendar();
					calMinusTwoDays.add(Calendar.HOUR, -48);
					
					if (existingImageStatus.getLastUpdated().toGregorianCalendar().before(calMinusTwoDays))
					{
						//Remove old errors or pending. (48 hours)
						toRemove[count] = existingImageStatus;
						count++;
					}
					else
					{
						//Need to be check for existence as may not of made it to the DB.
						imageIdToCheck[checkCount] = existingImageStatus.getImageId();
						checkCount++;
					}
				}
			}
			
			//Apply removes.
			for (int ii = 0; ii<count; ii++)
			{
				if (toRemove[ii] != null) {existingUploadList.getImageUploadRef().remove(toRemove[ii]);}
			}
			
			
			//Pending 0, Queued 1, Processing 2, Complete 3, Error 4, Inactive 5
			UploadStatusList currentUploadList = imageDataHelper.GetCurrentUploads(userId, imageIdToCheck);

			boolean found = false;
			count = 0;
			
			//Firstly find matches in the existing list which might need to be changed or removed.
			for(UploadStatusList.ImageUploadRef existingImageStatus: existingUploadList.getImageUploadRef())
			{
				found = false;
				for(UploadStatusList.ImageUploadRef currentImageStatus: currentUploadList.getImageUploadRef())
				{
					if (currentImageStatus.getImageId().equals(existingImageStatus.getImageId()))
					{
						if (currentImageStatus.getImageStatus().equals(3))
						{
							//Completed, so remove from list.
							toRemove[count] = existingImageStatus;
							count++;
						}
						else if (!currentImageStatus.getImageStatus().equals(existingImageStatus.getImageStatus()))
						{
							//Changed, so update
							existingImageStatus.setImageStatus(currentImageStatus.getImageStatus());
							existingImageStatus.setLastUpdated(currentImageStatus.getLastUpdated());
						}

						found = true;
					}
				}
				
				/*
				if (!found && existingImageStatus.getImageStatus() != 4)
				{
					// Have been processed, so remove it.
					toRemove[count] = existingImageStatus;
					count++;
				}
				*/
			}

			//Apply removes.
			for (int ii = 0; ii<count; ii++)
			{
				if (toRemove[ii] != null) {existingUploadList.getImageUploadRef().remove(toRemove[ii]);}
			}
			
			UploadStatusList.ImageUploadRef[] toAdd = new UploadStatusList.ImageUploadRef[currentUploadList.getImageUploadRef().size()];
			count = 0;
			
			//Secondly add any entries from the current list which are not present in the existing list.
			for(UploadStatusList.ImageUploadRef currentImageStatus: currentUploadList.getImageUploadRef())
			{
				found = false;
				for(UploadStatusList.ImageUploadRef existingImageStatus: existingUploadList.getImageUploadRef())
				{
					if (currentImageStatus.getImageId().equals(existingImageStatus.getImageId()))
					{
						found = true;
					}
				}
				
				if (!found)
				{
					// Not in existing list, and a valid status,so add it
					if (currentImageStatus.getImageStatus().equals(1) || currentImageStatus.getImageStatus().equals(2) || currentImageStatus.getImageStatus().equals(4))
					{
						toAdd[count] = currentImageStatus;
						count++;
					}
				}
			}
			
			for (int ii = 0; ii<count; ii++)
			{
				if (toAdd[ii] != null) {existingUploadList.getImageUploadRef().add(toAdd[ii]);}
			}
			
			meLogger.debug("UpdateUploadStatusList has completed. UserId:" + userId);

		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process UpdateUploadStatusList");
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces UpdateUploadStatusList", ex);
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
	
	public ImageList GetImageList(long userId, String type, String identity, long sectionId, long machineId, int imageCursor, int size, Date clientVersionTimestamp, CustomResponse customResponse)
	{
		//identity - For Category, this must be the category Id, for tags and galleries, this is the name.
		try
		{
			meLogger.debug("GetImageList() begins. UserId:" + userId + " Type:" + type + " Name:" + identity.toString() + " MachineId:" + machineId);

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
						tagDataHelper.GetTagImages(userId, machineId, imageCursor, size, imageList);
						break;
					case "CATEGORY":
						categoryDataHelper.GetCategoryImages(userId, machineId, imageCursor, size, imageList);
						break;
					case "GALLERY":
						galleryDataHelper.GetGalleryImages(userId, machineId, imageCursor, size, imageList);
						break;
				}
			}
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			
			meLogger.debug("GetImageList() has completed. UserId:" + userId + " Type:" + type + " Name:" + identity.toString() + " MachineId:" + machineId);
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
	
	private String GetFilePathIfExists(long userId, String folder, long imageId)
	{
		/* Check for file exists, return path if present */
		Path folderPath = Paths.get(destinationRoot, "Saved", String.valueOf(userId), folder);
		//Path filePath = Paths.get(destinationRoot, "Saved", String.valueOf(userId), folder, String.valueOf(imageId) + ".*");

		File file = UserTools.FileExistsNoExt(folderPath.toString(), String.valueOf(imageId));
		if (file != null)
		{
			return file.getPath();
		}
		return "";
	}
	
	private void ResizeAndSaveFile(long userId, String sourceImagePath, ImageMeta imageMeta, int width, int height, boolean isMain) throws IOException, InterruptedException, IM4JavaException
	{
		String folder = width + "x" + height;
		Path imageDestinationFolderPath = Paths.get(destinationRoot, "Saved", String.valueOf(userId), folder);

		File imageDestinationFolder = imageDestinationFolderPath.toFile();
		if (!imageDestinationFolder.exists())
			imageDestinationFolder.mkdir();
		  
		String imageDestinationPath = Paths.get(imageDestinationFolderPath.toString(), String.valueOf(imageMeta.getId()) + ".jpg").toString();
		
		if (isMain)
		{
			ImageUtilityHelper.SaveMainImage(userId, sourceImagePath, imageDestinationPath, width, height);
		}
		else
		{
			ImageUtilityHelper.SaveReducedSizeImages(userId, sourceImagePath, imageDestinationPath, imageMeta, width, height);
		}
	}
	
	
	
	public File GetOriginalImageFile(long userId, long imageId, CustomResponse customResponse)
	{
		try
		{
				String path = GetFilePathIfExists(userId,"Original", imageId);
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
	
	public BufferedImage GetImageFile(long userId, long imageId, int width, int height, CustomResponse customResponse)
	{
		try
		{
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
						case 50:
							folder = "50x50";
							break;
						case 250:
							folder = "250x250";
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
						else if (width<50)
						{
							folder = "50x50";	
						}
						else if (width<250)
						{
							folder = "250x250";	
						}
						else
						{
							folder = "800x800";
						}
					}
					
					//Check for file existing
					String imageFilePath = GetFilePathIfExists(userId, folder, imageId);
					if (imageFilePath.isEmpty())
					{
						ImageMeta imageMeta = imageDataHelper.GetImageMeta(userId, imageId, 0);
						if (imageMeta == null)
						{
							String error = "GetImageFile didn't return a valid Image object. UserId:" + userId + " ImageId:" + imageId;
							throw new WallaException("ImageService", "GetImageFile", error, HttpStatus.INTERNAL_SERVER_ERROR.value());
						}
						
						//File not present, so create it.
						String masterImageFilePath = GetFilePathIfExists(userId, "1920x1080", imageId);
						if (masterImageFilePath.isEmpty())
						{
							//Master file not present, so create a new one.
							String originalFilePath = GetFilePathIfExists(userId, "Original", imageMeta.getId());
							if (originalFilePath.isEmpty())
							{
								String error = "GetImageFile didn't find a valid original Image object to process. UserId:" + userId + " ImageId:" + imageId;
								throw new WallaException("ImageService", "GetImageFile", error, HttpStatus.INTERNAL_SERVER_ERROR.value());
							}
							
							ResizeAndSaveFile(userId, GetFilePathIfExists(userId, "Original", imageMeta.getId()), imageMeta, 1920, 1080, true);
							masterImageFilePath = GetFilePathIfExists(userId, "1920x1080", imageId);
						}

						int newWidth = Integer.valueOf(folder.substring(0, folder.indexOf("x")));
						int newHeight = Integer.valueOf(folder.substring(folder.indexOf("x")+1));
						ResizeAndSaveFile(userId, masterImageFilePath, imageMeta, newWidth, newHeight, false);
						imageFilePath = GetFilePathIfExists(userId, folder, imageId);
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
					//If aspect ratio is 1.77
					if (height > 1080)
					{
						String error = "Image size requested is not supported.  Size is too large for the aspect Ratio:" 
								+ String.valueOf(requestAspectRatio) + " Height:" + width;
						throw new WallaException("ImageService", "GetImageFile", error, HttpStatus.BAD_REQUEST.value());
					}
					
					String imageFilePath = GetFilePathIfExists(userId, "1920x1080", imageId);
					if (imageFilePath.isEmpty())
					{
						ImageMeta imageMeta = imageDataHelper.GetImageMeta(userId, imageId, 0);
						if (imageMeta == null)
						{
							String error = "GetImageFile didn't return a valid Image object. UserId:" + userId + " ImageId:" + imageId;
							throw new WallaException("ImageService", "GetImageFile", error, HttpStatus.INTERNAL_SERVER_ERROR.value());
						}
						
						//Master file not present, so create a new one.
						ResizeAndSaveFile(userId, GetFilePathIfExists(userId, "Original", imageMeta.getId()), imageMeta, 1920, 1080, true);
						imageFilePath = GetFilePathIfExists(userId, "1920x1080", imageId);
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
			imageDataHelper.UpdateImageStatus(userId, imageId, 2, "");
			
			File uploadedFile = Paths.get(destinationRoot, "Que", String.valueOf(imageId) + "." + Long.toString(userId)).toFile();
			if (!uploadedFile.exists())
			{
				String error = "Uploaded file could not be found.  ImageId:" + imageId + " UserId:" + userId;
				throw new WallaException("ImageService", "SetupNewImage", error, 0); 
			}

			ImageMeta imageMeta = imageDataHelper.GetImageMeta(userId, imageId, 0);
			if (imageMeta == null)
			{
				String error = "SetupNewImage didn't return a valid Image object. UserId:" + userId + " ImageId:" + imageId;
				throw new WallaException("ImageService", "SetupNewImage", error, 0); 
			}
			
			if (imageMeta.getStatus().intValue() != 2)
			{
				String error = "SetupNewImage didn't return an Image object with the correct status. UserId:" + userId + " ImageId:" + imageId + " Status:" + imageMeta.getStatus();
				throw new WallaException("ImageService", "SetupNewImage", error, 0);
			}
			

			
			
			/**************************************************************************/
			/****************** Enrich with Exif & Save image copies ******************/
			/**************************************************************************/
			
			Path userOriginalFolderPath = Paths.get(destinationRoot, "Saved", Long.toString(userId), "Original");
			File userOriginalFolder = userOriginalFolderPath.toFile();
    		if (!userOriginalFolder.exists())
    		{
    			userOriginalFolder.mkdirs();
    		}
    			
			//Archive original image
    		String originalImagePath = ImageUtilityHelper.SaveOriginal(userId, uploadedFile.getPath(), userOriginalFolderPath.toString(), imageId, imageMeta.getFormat());
    		
			//Make one initial copy, to drive subsequent resizing and also to orient correctly.
    		File userMainImageFolder = Paths.get(destinationRoot, "Saved", Long.toString(userId), "1920x1080").toFile();
    		if (!userMainImageFolder.exists())
    		{
    			userMainImageFolder.mkdirs();
    		}
    		ResizeAndSaveFile(userId, uploadedFile.getPath(), imageMeta, 1920, 1080, true);

			String mainImagePath = GetFilePathIfExists(userId, "1920x1080", imageId);
			if (mainImagePath.isEmpty())
			{
				String error = "Unexpected error retrieving a resized image in the folder: 1920x1080.  ImageId:" + imageId;
				throw new WallaException("ImageService", "SetupNewImage", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 
			}
    				
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
			
			ResizeAndSaveFile(userId,mainImagePath, imageMeta, 20, 20, false);
			ResizeAndSaveFile(userId,mainImagePath, imageMeta, 50, 50, false);
			ResizeAndSaveFile(userId,mainImagePath, imageMeta, 250, 250, false);
			ResizeAndSaveFile(userId,mainImagePath, imageMeta, 800, 800, false);

            //TODO Delete original uploaded image.
			ImageUtilityHelper.DeleteImage(uploadedFile.getPath());
			
			
			imageDataHelper.UpdateImageStatus(userId, imageId, 3, "");
			
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
			
			//TODO For the category, call CategoryRippleUpdates decoupled		
			categoryService.CategoryRippleUpdate(userId, imageMeta.getCategoryId());
			
			meLogger.debug("SetupNewImage has completed. UserId:" + userId + " ImageId:" + imageId);
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to process SetupNewImage",ex);
			
			try {MoveImageToErrorFolder(userId, imageId);} catch (Exception logOrIgnore) {}
			try {imageDataHelper.UpdateImageStatus(userId, imageId, 4, ex.getMessage());} catch (Exception logOrIgnore) {}
			
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