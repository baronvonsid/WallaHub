package walla.business;

import com.drew.imaging.*;
import com.drew.metadata.*;
import com.drew.metadata.exif.*;
import com.drew.metadata.jpeg.*;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.GregorianCalendar;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.im4java.core.GraphicsMagickCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.springframework.http.HttpStatus;

import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.db.*;
import walla.utils.*;
import walla.ws.*;

public final class ImageUtilityHelper 
{
	private static Logger meLogger = null;
	
	public static String EnrichImageMetaFromFileData(String imageFilePath, ImageMeta imageMeta)
	{
		meLogger = Logger.getLogger(ImageUtilityHelper.class);
	
		File imageFile = new File(imageFilePath);
		
		/* Size, Format, Date taken */
		String response = EnrichMetaFromFile(imageFile, imageMeta);
		if (!response.equals("OK"))
			return response;
		
		Metadata fileMetaData = null;
		try 
		{
			fileMetaData = ImageMetadataReader.readMetadata(imageFile);
		} 
		catch (ImageProcessingException | IOException e) 
		{
			response = LoadFileIntoMemoryReadAttributes(imageFile, imageMeta);
			if (!response.equals("OK"))
				return "Meta data not supported, image could not be loaded." + response;
		}
		
		if (imageMeta.getFormat().equals("JPG"))
		{
			JpegDirectory jpegDirectory = fileMetaData.getDirectory(JpegDirectory.class);
			if (jpegDirectory != null)
			{
				response = EnrichMetaFromJPEG(jpegDirectory, imageMeta);
				if (!response.equals("OK"))
					return response;
			}
		}
		
		ExifIFD0Directory exifDirectory = fileMetaData.getDirectory(ExifIFD0Directory.class);
		if (exifDirectory != null)
		{
			response = EnrichMetaFromEXIF(exifDirectory, imageMeta);
			if (!response.equals("OK"))
				return response;
		}
		
		ExifSubIFDDirectory exifSubDirectory = fileMetaData.getDirectory(ExifSubIFDDirectory.class);
		if (exifSubDirectory != null)
		{
			response = EnrichMetaFromEXIFSub(exifSubDirectory, imageMeta);
			if (!response.equals("OK"))
				return response;
		}

		return "OK";
		/*
		for (Directory directory : fileMetaData.getDirectories()) 
		{   
		System.out.println(directory.getName());
		for (com.drew.metadata.Tag tag : directory.getTags()) 
		{         
		System.out.println(tag);     
			}
		}
		*/
		
		/* TODO Get Manufacturer and apply */
		/* TODO Process Manufacturer specifics and flash */
		/* TODO GPSInfo -> Phase 2. */
		/*
		Orientation:
		1 = Horizontal (normal) 
		2 = Mirror horizontal 
		3 = Rotate 180 
		4 = Mirror vertical 
		5 = Mirror horizontal and rotate 270 CW 
		6 = Rotate 90 CW 
		7 = Mirror horizontal and rotate 90 CW 
		8 = Rotate 270 CW
		 */

	}
	
	private static String EnrichMetaFromFile(File currentFile, ImageMeta imageMeta)
	{
		//Enrich:
		/* Size, Format, Date taken */
		try
		{
			Path path = currentFile.toPath();
	
			BasicFileAttributeView attributeView = Files.getFileAttributeView(path, BasicFileAttributeView.class);
			BasicFileAttributes attributes = attributeView.readAttributes();
			
			imageMeta.setSize(attributes.size());

	        GregorianCalendar gc = new GregorianCalendar();
	        gc.setTimeInMillis(attributes.lastModifiedTime().toMillis());
	        XMLGregorianCalendar xmlGc = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc); 
	        imageMeta.setTakenDateFile(xmlGc);
			
			/* Supported types 
			•JPG,JPEG,TIF,TIFF,PSD,PNG,BMP,GIF,CR2,ARW,NEF
			•Need to investigate CRW/NEF/ORF/RW2 and other RAW types.
			 */
	        
			String extension = imageMeta.getOriginalFileName().substring(imageMeta.getOriginalFileName().lastIndexOf(".")+1);
			
			switch (extension.toUpperCase())
			{
				case "JPG":
				case "JPEG":
					imageMeta.setFormat("JPG");
					break;
				case "TIF":
				case "TIFF":
					imageMeta.setFormat("TIF");
					break;
				case "PSD":
				case "PNG":
				case "BMP":
				case "GIF":
				case "CR2":
				case "ARW":
				case "NEF":
					imageMeta.setFormat(extension.toUpperCase());
					break;
				default:
					String error = "ImageId:" + imageMeta.getId() + " Format not supported:" + extension.toUpperCase();
					throw new WallaException("ImageUtilityHelper", "EnrichMetaFromFile", error, 0); 
			}
			return "OK";
		}
		catch (WallaException wallaEx) {
			meLogger.error("Error when trying to process EnrichMetaFromFile", wallaEx);
			return wallaEx.getMessage();
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces EnrichMetaFromFile", ex);
			return ex.getMessage();
		}
	}
	
	private static String LoadFileIntoMemoryReadAttributes(File currentFile, ImageMeta imageMeta)
	{
		BufferedImage img = null;
		
		try
		{
			img = ImageIO.read(currentFile);
			imageMeta.setHeight(img.getHeight());
			imageMeta.setWidth(img.getWidth());
			
			return "OK";
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces LoadFileIntoMemoryReadAttributes", ex);
			return ex.getMessage();
		}
	}
	
	private static String EnrichMetaFromJPEG(JpegDirectory jpegDirectory, ImageMeta imageMeta)
	{
		/*
 		height - integer
 		width - integer
		*/
		try
		{
			JpegDescriptor descriptor = new JpegDescriptor(jpegDirectory); 
			
			for (com.drew.metadata.Tag tag : jpegDirectory.getTags()) 
			{ 
				switch (tag.getTagType())
				{
					case JpegDirectory.TAG_JPEG_IMAGE_HEIGHT:
						imageMeta.setHeight(jpegDirectory.getInt(JpegDirectory.TAG_JPEG_IMAGE_HEIGHT));
						break;
					case JpegDirectory.TAG_JPEG_IMAGE_WIDTH:
						imageMeta.setWidth(jpegDirectory.getInt(JpegDirectory.TAG_JPEG_IMAGE_WIDTH));
						break;
				}
			}
			return "OK";
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces EnrichMetaFromJPEG", ex);
			return ex.getMessage();
		}
	}
	
	private static String EnrichMetaFromEXIF(ExifIFD0Directory exifDirectory, ImageMeta imageMeta)
	{
		try
		{
			/*
			Create Date - Datetime
			Make - string
			Model - string
			Orientation - integer
			 */
			//ExifIFD0Descriptor descriptor = new ExifIFD0Descriptor(exifDirectory); 
			
			for (com.drew.metadata.Tag tag : exifDirectory.getTags()) 
			{ 
				switch (tag.getTagType())
				{
					case ExifIFD0Directory.TAG_DATETIME:
						
				        GregorianCalendar gc = new GregorianCalendar();
				        gc.setTimeInMillis(exifDirectory.getDate(ExifIFD0Directory.TAG_DATETIME).getTime());
				        XMLGregorianCalendar xmlGc = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc); 
						imageMeta.setTakenDateMeta(xmlGc);
						break;
					case ExifIFD0Directory.TAG_MAKE:
						imageMeta.setCameraMaker(exifDirectory.getString(ExifIFD0Directory.TAG_MAKE));
						break;
					case ExifIFD0Directory.TAG_MODEL:
						imageMeta.setCameraModel(exifDirectory.getString(ExifIFD0Directory.TAG_MODEL));
						break;
					case ExifIFD0Directory.TAG_ORIENTATION:
						imageMeta.setOrientation(exifDirectory.getInt(ExifIFD0Directory.TAG_ORIENTATION));
						break;
					case 256:  //RICOH only ?
						imageMeta.setHeight(exifDirectory.getInt(256));
						break;
					case 257: //RICOH only ?
						imageMeta.setHeight(exifDirectory.getInt(257));
						break;
				}
			}
			return "OK";
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces EnrichMetaFromEXIF", ex);
			return ex.getMessage();
		}
	}
	
	private static String EnrichMetaFromEXIFSub(ExifSubIFDDirectory exifSubDirectory, ImageMeta imageMeta)
	{
		try
		{
			ExifSubIFDDescriptor descriptor = new ExifSubIFDDescriptor(exifSubDirectory); 
			
			/*
			Aperture string F9.1 
			ShutterSpeed string 1/255 sec
			ISO 100 integer
			Height 1024 integer
			Width 768 integer
			Create Date
			Time zone offsett integer
			 */
			
			for (com.drew.metadata.Tag tag : exifSubDirectory.getTags()) 
			{ 
				switch (tag.getTagType())
				{
					case ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL:
						/*Check for empty date, if so leave as blank*/
						//String tempDate = exifSubDirectory.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
						Date tempDate = exifSubDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
						if (tempDate != null) 
						{
					        GregorianCalendar gc = new GregorianCalendar();
					        gc.setTimeInMillis(tempDate.getTime());
					        XMLGregorianCalendar xmlGc = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc); 
							imageMeta.setTakenDateMeta(xmlGc);
						}
						break;
					//case ExifSubIFDDirectory.TAG_TIME_ZONE_OFFSET:
						//System.out.println("timezoneoffset:" + exifSubDirectory.getInt(ExifSubIFDDirectory.TAG_TIME_ZONE_OFFSET));
						//break;
					case ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT:
						imageMeta.setHeight(exifSubDirectory.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT));
						break;
					case ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH:
						imageMeta.setWidth(exifSubDirectory.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH));
						break;
					case ExifSubIFDDirectory.TAG_SHUTTER_SPEED:
						imageMeta.setShutterSpeed(descriptor.getShutterSpeedDescription());
						break;
					case ExifSubIFDDirectory.TAG_APERTURE:
						imageMeta.setAperture(descriptor.getApertureValueDescription());
						break;
					case ExifSubIFDDirectory.TAG_FNUMBER:
						if (imageMeta.getAperture() == null)
						{
							imageMeta.setAperture("f" + exifSubDirectory.getDouble(ExifSubIFDDirectory.TAG_FNUMBER));
						}
						break;
					case ExifSubIFDDirectory.TAG_EXPOSURE_TIME:
						if (imageMeta.getShutterSpeed() == null)
						{
							imageMeta.setShutterSpeed(descriptor.getExposureTimeDescription());
						}
						break;
					case ExifSubIFDDirectory.TAG_ISO_EQUIVALENT:
						imageMeta.setISO(exifSubDirectory.getInt(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT));
						//System.out.println("isoequivalent:" + descriptor.getIsoEquivalentDescription());
						//System.out.println("isoequivalent:" + exifSubDirectory.getInt(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT));
						break;
				}
			}
			return "OK";
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces EnrichMetaFromEXIF", ex);
			return ex.getMessage();
		}
	}

	/********************************************************************************/
	/********************************************************************************/
	/**************************  Image file manipulations  **************************/
	/********************************************************************************/
	/********************************************************************************/
	
	public static void DeleteImage(String filePath)
	{
		File deleteFile = new File(filePath);
		deleteFile.delete();
	}
	
	public static void SwitchHeightWidth(String mainImagePath, ImageMeta imageMeta) throws IOException
	{
		int imageMetaWidth = imageMeta.getWidth().intValue();
		int imageMetaHeight = imageMeta.getHeight().intValue();
		
		BufferedImage img = ImageIO.read(new File(mainImagePath));
		int updatedHeight = img.getHeight();
		int updatedWidth = img.getWidth();

		//Calculate aspect ratio or target image.
		double updatedAspectRatio = (double)updatedWidth / (double)updatedHeight;
		updatedAspectRatio = UserTools.DoRound(updatedAspectRatio,2);
		
		//Calculate aspect ratio of current image.
		double originalAspectRatio = (double)imageMetaWidth / (double)imageMetaHeight;
		originalAspectRatio = UserTools.DoRound(originalAspectRatio,2);
		
		if (updatedAspectRatio != originalAspectRatio)
		{
			//Switch them.
			imageMeta.setHeight(imageMetaWidth);
			imageMeta.setWidth(imageMetaHeight);
		}
		
	}
	
	public static void SaveMainImage(long userId, String sourceFilePath, String destinationFilePath, int targetWidth, int targetHeight) throws IOException, InterruptedException, IM4JavaException
	{
		//Using the original image, save a JPEG version, correctly orientated with no EXIF
		
		//Build up GraphicMagick command.
		GraphicsMagickCmd cmd = new GraphicsMagickCmd("convert");
		IMOperation op = new IMOperation();
		op.addImage(sourceFilePath);
		op.autoOrient();
		op.strip();
		op.resize(targetWidth,targetHeight);
		op.addImage(destinationFilePath);

		cmd.run(op);
	}
	
	public static void SaveOriginal(long userId, String fromFilePath, String toFolderPath, long imageId, String extension) throws IOException, InterruptedException, IM4JavaException
	{		
		Path destinationFile = Paths.get(toFolderPath, imageId + "." + extension);

		UserTools.Copyfile(fromFilePath, destinationFile.toString());
	}
	
	public static void SaveReducedSizeImages(long userId, String sourceFilePath, String destinationFilePath, ImageMeta imageMeta, int targetWidth, int targetHeight) throws IOException, InterruptedException, IM4JavaException
	{
		//Load image into memory, then save of JPEGs of particular sizes.
		//50, 300, 800.  Square Dimensions.  Cropping required.
		//800 and 1600.  Maintain aspect ratio.
		
		BufferedImage img = null;
		
		img = ImageIO.read(new File(sourceFilePath));
		int imageMetaHeight = img.getHeight();
		int imageMetaWidth = img.getWidth();
		
		//int imageMetaWidth = imageMeta.getWidth().intValue();
		//int imageMetaHeight = imageMeta.getHeight().intValue();
		
		GraphicsMagickCmd cmd = new GraphicsMagickCmd("convert");
		
		// create the operation, add images and operators/options
		IMOperation op = new IMOperation();
		op.addImage(sourceFilePath);
		
		//Calculate aspect ratio or target image.
		double targetAspectRatio = (double)targetWidth / (double)targetHeight;
		targetAspectRatio = UserTools.DoRound(targetAspectRatio,2);
		
		//Calculate aspect ratio of current image.
		double currentAspectRatio = (double)imageMetaWidth / (double)imageMetaHeight;
		currentAspectRatio = UserTools.DoRound(currentAspectRatio,2);
		
		if (targetWidth > imageMetaWidth || targetHeight > imageMetaHeight)
		{
			op.resize(targetWidth, targetHeight, "^");
		}
		
		//Both target dimensions OK for a standard reduction is required.
		
		if (targetAspectRatio == currentAspectRatio)
		{
			//Same aspect, so just force a resize to the target dimensions
			op.resize(targetWidth,targetHeight);
		}
		else if (targetAspectRatio > currentAspectRatio)
		{
			//Target is wider vs current.
			//ie. a larger difference between width and height compared to current, so crop height.
			
			//Calculate best height for current to match target aspect ratio.
			int newTempHeight = (int)Math.floor((double)imageMetaWidth / targetAspectRatio);
			
			//Calculate height padding to centralise crop.
			int padding = (int) Math.floor((double)(imageMetaHeight - newTempHeight) / 2);

			op.crop(imageMetaWidth, newTempHeight, 0, padding);
		}
		else
		{
			//Target is thinner vs current.
			//Crop width.
		
			//Calculate best width for current to match target aspect ratio.
			int newTempWidth = (int)Math.floor((double)imageMetaHeight * targetAspectRatio);
			
			//Calculate width padding for crop
			int padding = (int)Math.floor((double)(imageMetaWidth - newTempWidth) / 2);
			
			op.crop(newTempWidth, imageMetaHeight, padding, 0);
		}
		
		op.resize(targetWidth,targetHeight);

		/*
		//If both dimensions smaller, so just leave the image dimensions as-is.
		
		//Image size is less than the target size needed.  So just do what you can.
		if (targetWidth < imageMetaWidth && targetHeight > imageMetaHeight)
		{
			//Image height is short.
			op.resize(targetWidth,imageMetaHeight);
			
		}
		
		if (targetWidth > imageMetaWidth && targetHeight < imageMetaHeight)
		{
			//Image if too skinny, so just resize height
			op.resize(targetWidth,imageMetaHeight);
		}
		*/

		op.quality(90.0);
		op.strip();

		op.addImage(destinationFilePath);

		cmd.run(op);
		
	
	}
}


/*
EXIF - Use to get at format.  Not needed currently.
private boolean GetEmbeddedFormat(Metadata metaData, ImageMeta imageMeta)
{
	//Check for particular embedded image information
	for (Directory directory : metaData.getDirectories()) 
	{     
		if (directory.getName().equals("JPEG1"))
		{
			imageMeta.setFormat("JPG");
		}

		if (directory.getName().equals("TIFF"))
		{
			imageMeta.setFormat("TIF");
		}
		
		//TODO Add in other formats.
	}
	
	return true;
}
*/