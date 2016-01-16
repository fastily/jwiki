package jwiki.core;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.json.XML;

import jwiki.dwrap.ImageInfo;

/**
 * Class containing static methods which can perform miscellaneous tasks pertaining to MediaWiki.
 * 
 * @author Fastily
 *
 */
public final class WTask
{

	/**
	 * Constructors disallowed
	 */
	private WTask()
	{

	}

	/**
	 * Creates an InputStream pointing to the bytes of a file on a Wiki.
	 * 
	 * @param title The title of the file to get, including the "File" prefix.
	 * @param height The height thumbnail to retrieve (optional param, specify -1 to disable)
	 * @param width The width thumbnail to retrieve (optional param, specify -1 to disable)
	 * @param wiki The Wiki object to use
	 * @return The file's InputStream
	 * @throws IOException Network error
	 */
	private static InputStream getFileInputStream(String title, int height, int width, Wiki wiki) throws IOException
	{
		ColorLog.fyi(wiki, "Downloading " + title);

		ImageInfo x = wiki.getImageInfo(title, height, width);
		String url;
		if (x.thumburl != null)
			url = x.thumburl;
		else if (x.url != null)
			url = x.url;
		else
			throw new IOException(String.format("Could not find the file '%s' to download", title));

		return Req.genericGET(new URL(url), wiki.cookiejar);
	}

	/**
	 * Downloads and writes a media file to disk. Note that the file must be visible to you in order to download it.
	 * 
	 * @param title The title of the file to download <span style="text-decoration:underline">on the Wiki</span>
	 * @param localpath The pathname to save this file to (e.g. "<code>/Users/Fastily/Example.jpg</code> "). Note that if
	 *           a file with that name already exists at that pathname, it
	 *           <span style="color:Red;font-weight:bold">will</span> be overwritten!
	 * @param wiki The wiki object to use.
	 * @return True if we were successful.
	 */
	public static boolean downloadFile(String title, String localpath, Wiki wiki)
	{
		return downloadFile(title, localpath, -1, -1, wiki);
	}

	/**
	 * Downloads and writes a media file to disk. Note that the file must be visible to you in order to download it.
	 * 
	 * @param title The title of the file to download <span style="text-decoration:underline">on the Wiki</span>
	 * @param localpath The pathname to save this file to (e.g. "<code>/Users/Fastily/Example.jpg</code> "). Note that if
	 *           a file with that name already exists at that pathname, it
	 *           <span style="color:Red;font-weight:bold">will</span> be overwritten!
	 * @param height The height thumbnail to retrieve (optional param, specify -1 to disable)
	 * @param width The width thumbnail to retrieve (optional param, specify -1 to disable)
	 * @param wiki The wiki object to use.
	 * @return True if we were successful.
	 */
	public static boolean downloadFile(String title, String localpath, int height, int width, Wiki wiki)
	{
		byte[] bf = new byte[1024 * 512]; // 512kb buffer.
		int read;
		try (InputStream in = getFileInputStream(title, height, width, wiki);
				OutputStream out = Files.newOutputStream(Paths.get(localpath)))
		{
			while ((read = in.read(bf)) > -1)
				out.write(bf, 0, read);

			return true;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Downloads a media file from wiki and converts it to a BufferedImage, for use with GUI applications. Note that the
	 * file must be visible to you in order to download it.
	 * 
	 * @param title The title of the file to download, including the "File:" prefix.
	 * @param wiki The wiki object to use.
	 * @return A BufferedImage, or null if something went wrong.
	 */
	public static BufferedImage downloadFile(String title, Wiki wiki)
	{
		return downloadFile(title, -1, -1, wiki);
	}

	/**
	 * Downloads a media file from wiki and converts it to a BufferedImage, for use with GUI applications. Note that the
	 * file must be visible to you in order to download it.
	 * 
	 * @param title The title of the file to download, including the "File:" prefix.
	 * @param height The height thumbnail to retrieve (optional param, specify -1 to disable)
	 * @param width The width thumbnail to retrieve (optional param, specify -1 to disable)
	 * @param wiki The wiki object to use.
	 * @return A BufferedImage, or null if something went wrong.
	 */
	public static BufferedImage downloadFile(String title, int height, int width, Wiki wiki)
	{
		try
		{
			return ImageIO.read(getFileInputStream(title, height, width, wiki));
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Parses templates in a String into JSONObjects. This takes the output of
	 * <code>action=expandtemplates&amp;prop=parsetree</code> and extracts the parse trees for each template found in
	 * <code>text</code>. Note that any extraneous content (not a template) will be stripped.
	 * 
	 * @param wiki The wiki object to run the parse query on
	 * @param text A String with a template(s) (e.g. <code>"{{Test|Blah|foo=meh|baz|3=69}}"</code>)
	 * @return An ArrayList where each JSONObject represents a template found in the String.
	 */
	public static ArrayList<Reply> parseTemplate(Wiki wiki, String text)
	{
		try
		{
			return new Reply(XML.toJSONObject(
					Req.get(wiki.makeUB("expandtemplates", "text", text, "prop", "parsetree").makeURL(), wiki.cookiejar)
							.getStringR("parsetree"))).getJAOfJOAsALR("template");
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}

		return new ArrayList<>();
	}
}