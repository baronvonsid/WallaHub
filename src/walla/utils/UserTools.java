package walla.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.UUID;
import javax.servlet.ServletOutputStream;

import org.apache.log4j.Logger;

public final class UserTools {

	
	public static long CheckUser(String userName)
	{
		//Check user is logged on and return user Id.
		
		if (userName.equals("simon2345"))
		{
			return -1;
			
		}
		else
		{
			return 100001;
		}
	}
	
	public static void Copyfile(String sourceFile, String destinationFile) throws IOException
	{
		  File f1 = new File(sourceFile);
		  File f2 = new File(destinationFile);
		  
		  InputStream in = new FileInputStream(f1);
		  OutputStream out = new FileOutputStream(f2);
		
		  byte[] buf = new byte[1024];
		  int len;
		  while ((len = in.read(buf)) > 0)
		  {
			  out.write(buf, 0, len);
		  }
		  in.close();
		  out.close();
	}
	
	public static File FileExistsNoExt(String folderPath, final String fileName) 
	{
		File folder = new File(folderPath);
		FilenameFilter select = new FileListFilter(fileName);
		File[] matchingFiles = folder.listFiles(select);
		
		/*
		
		File[] matchingFiles = folder.listFiles(new FilenameFilter() 
								{
									public boolean accept(File pathname) 
									{
										return pathname.getName().equals(fileName + "*");
									}
								});
								*/
		if (matchingFiles.length == 1)
		{
			return matchingFiles[0];
		}
		else
		{
			return null;
		}
		
	}
	
	static class FileListFilter implements FilenameFilter 
	{
		  private String name; 

		  public FileListFilter(String name) {
		    this.name = name;
		  }

		  public boolean accept(File directory, String filename) {
		      return filename.startsWith(name);
		    }
		  }
	
	
	public static void PopulateServletStream(File fileIn, ServletOutputStream outStream) throws IOException
	{
		FileInputStream inStream = new FileInputStream(fileIn);                	
		//ServletOutputStream out = httpResponse.getOutputStream();
		 
		byte[] outputByte = new byte[4096];
		//copy binary content to output stream
		while(inStream.read(outputByte, 0, 4096) != -1)
		{
			outStream.write(outputByte, 0, 4096);
		}
		inStream.close();
		outStream.flush();
		outStream.close();
	}
	
	public static void MoveFile(String sourceFile, String destinationFile, Logger meLogger)
	{
		try
		{
			  File source = new File(sourceFile);
			  source.renameTo(new File(destinationFile));
		}
		catch (Exception ex)
		{
			//Capture error and suppress subsequent error bubbling.
			meLogger.error("File failed to be moved.  Source:" + sourceFile + "  Destination:" + destinationFile + " Error received:" + ex.getMessage());
		}
	}

	
	public static double DoRound(double unrounded, int precision)
	{
	    BigDecimal bd = new BigDecimal(unrounded);
	    BigDecimal rounded = bd.setScale(precision, BigDecimal.ROUND_HALF_UP);
	    return rounded.doubleValue();
	}
	
	public static String GetComplexUrl()
	{
		UUID identifier = java.util.UUID.randomUUID();
		return identifier.toString();
	}
	
	public static String GetCssName(int styleId)
	{
		String cssName = null;
		
		switch (styleId)
		{
			case 0:
				cssName = "darkness";
				break;
			case 1:
				cssName = "lightness";
				break;
			case 2:
				cssName = "cupertino";
				break;
			case 3:
				cssName = "blacktie";
				break;
		}
		return cssName;
	}
	
	public static String GetJspName(int presentationId)
	{
		String jspName = null;
		
		switch (presentationId)
		{
			case 0:
				jspName = "GalleryViewer-Standard";
				break;
			case 1:
				jspName = "GalleryViewer-Lightbox";
				break;
		}
		return jspName;
		
	}
	
}
