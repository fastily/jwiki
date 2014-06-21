package jwiki.core;

/**
 * Container object for results returned by imageinfo MediaWiki module.
 * 
 * @author Fastily
 * 
 */
public class ImageInfo
{
	/**
	 * Represents image size (in bytes), image width (in pixels), and image height (in pixels).
	 */
	private int size, width, height;
	
	/**
	 * URLs to the full size image and a URL to a thumbnail (if applicable, otherwise null)
	 */
	private String url, thumburl;
	
	/**
	 * Constructor, takes a JSONObject containing image info returned by the server.
	 * 
	 * @param jo The JSONObject to use.
	 */
	protected ImageInfo(ServerReply r)
	{
		size = r.getIntR("size"); 
		width = r.getIntR("width");
		height = r.getIntR("height");
		url = r.getStringR("url");
		thumburl = r.getStringR("thumburl");
	}
	
	/**
	 * Gets the size of the image (in bytes).
	 * 
	 * @return The size of the image (in bytes)
	 */
	public int getSize()
	{
		return size;
	}
	
	/**
	 * Gets the width of the image (in pixels).
	 * 
	 * @return The width of the image (in pixels)
	 */
	public int getWidth()
	{
		return width;
	}
	
	/**
	 * Gets the height of the image (in pixels).
	 * 
	 * @return The height of the image (in pixels)
	 */
	public int getHeight()
	{
		return height;
	}
	
	/**
	 * Gets the URL of the original image.
	 * 
	 * @return The URL to the original image.
	 */
	public String getURL()
	{
		return url;
	}
	
	/**
	 * Gets the URL of the thumbnail of the image (if applicable).
	 * 
	 * @return The URL to the thumbnail of the original image (or null if it doesn't exist).
	 */
	public String getThumbURL()
	{
		return thumburl;
	}
	
	/**
	 * Gets a String representation of this object.
	 * 
	 * @return A String representation of this object.
	 */
	public String toString()
	{
		return String.format("----%nSize: %d%nWidth: %d%nHeight %d%nURL: %s%nThumbURL: %s%n----%n", size, width, height, url,
				thumburl);
	}
	
}