package fbot.lib.core;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.CookieManager;
import java.net.URL;

import javax.imageio.ImageIO;

/**
 * Class containing static methods which can perform miscellaneous tasks pertaining to MediaWiki.
 * @author Fastily
 *
 */
public class FTask
{
	
	/**
	 * Hiding constructor from javadoc.
	 */
	private FTask()
	{
		
	}
	
	/**
	 * Gets the resource pointed to by a URL as raw bytes. CAVEAT: if the resources pointed to by the URL exceed 128MB,
	 * you will get a OutOfMemoryError. Avoid this by setting your heap space accordingly (e.g.
	 * <tt>java -Xmx512M myprog</tt> will set the max heap space to 512M for <tt>myprog.java</tt>).
	 * 
	 * @param url The URL to use.
	 * @param cookiejar The cookiejar to use. This parameter is optional; specifiy null to disable it.
	 * @return A stream of bytes represented by the URL pointed to, or null if something went wrong.
	 */
	private static byte[] getBytes(String url, CookieManager cookiejar)
	{
		try
		{
			BufferedInputStream in = new BufferedInputStream(Request.getInputStream(new URL(url), cookiejar));
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int c;
			while ((c = in.read()) != -1)
				out.write(c);
			
			in.close();
			return out.toByteArray();
			
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Downloads and writes a media file to disk. Note that the file must be visible to you in order to download it.
	 * 
	 * @param title The title of the file to download <ins>on the Wiki</ins> <b>including</b> the " <tt>File:</tt>"
	 *            prefix.
	 * @param localpath The pathname to save this file to (e.g. "<tt>/Users/Fastily/Example.jpg</tt> "). Note that if a
	 *            file with that name already exists at that pathname, it <span
	 *            style="color:Red;font-weight:bold">will</span> be overwritten!
	 * @param wiki The wiki object to use.
	 * 
	 * @return True if the operation was successful.
	 */
	public static boolean downloadFile(String title, String localpath, W wiki)
	{
		return downloadFile(title, localpath, wiki, -1, -1);
	}
	
	/**
	 * Downloads and writes a media file to disk. Note that the file must be visible to you in order to download it.
	 * 
	 * @param title The title of the file to download <ins>on the Wiki</ins> <b>including</b> the " <tt>File:</tt>"
	 *            prefix.
	 * @param localpath The pathname to save this file to (e.g. "<tt>/Users/Fastily/Example.jpg</tt> "). Note that if a
	 *            file with that name already exists at that pathname, it <span
	 *            style="color:Red;font-weight:bold">will</span> be overwritten!
	 * @param wiki The wiki object to use.
	 * @param height The height (in pixels) to scale to. Specify a number less than 1 to disable this feature.
	 * @param width The width (in pixels) to scale to. Specify a number less than 1 to disable this feature.
	 * 
	 * @return True if the operation was successful.
	 */
	public static boolean downloadFile(String title, String localpath, W wiki, int height, int width)
	{
		ImageInfo x = wiki.getImageInfo(title, height, width);
		String url;
		if (x.getThumbURL() != null)
			url = x.getThumbURL();
		else if (x.getURL() != null)
			url = x.getURL();
		else
			return false;
		
		try
		{
			FileOutputStream fos = new FileOutputStream(localpath);
			fos.write(getBytes(url, wiki.settings.cookiejar));
			fos.close();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Downloads a media file from wiki and converts it to a BufferedImage, for use with GUI applications. Note that the
	 * file must be visible to you in order to download it.
	 * 
	 * @param title The title of the file to download, including the "File:" prefix.
	 * @param wiki The wiki object to use.
	 * @return A BufferedImage, or null if something went wrong.
	 * @throws IOException I/O error.
	 */
	public static BufferedImage downloadFile(String title, W wiki) throws IOException
	{
		return downloadFile(title, wiki, -1, -1);
	}
	
	/**
	 * Downloads a media file from wiki and converts it to a BufferedImage, for use with GUI applications. Note that the
	 * file must be visible to you in order to download it.
	 * 
	 * @param title The title of the file to download, including the "File:" prefix.
	 * @param wiki The wiki object to use.
	 * @param height The height (in pixels) to scale to. Specify a number less than 1 to disable this feature.
	 * @param width The width (in pixels) to scale to. Specify a number less than 1 to disable this feature.
	 * 
	 * @return A BufferedImage, or null if something went wrong.
	 * @throws IOException I/O error.
	 */
	public static BufferedImage downloadFile(String title, W wiki, int height, int width) throws IOException
	{
		ImageInfo x = wiki.getImageInfo(title, height, width);
		String url;
		if (x.getThumbURL() != null)
			url = x.getThumbURL();
		else if (x.getURL() != null)
			url = x.getURL();
		else
			return null;
		
		return ImageIO.read(new ByteArrayInputStream(getBytes(url, wiki.settings.cookiejar)));
	}
	
}