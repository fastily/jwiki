package jwiki.dwrap;

import org.json.JSONArray;

import jwiki.core.Reply;
import jwiki.util.Tuple;

/**
 * Container object for a result returned by the imageinfo MediaWiki module.
 * 
 * @author Fastily
 * 
 */
public class ImageInfo
{
	/**
	 * The file associated with this ImageInfo
	 */
	public final String title;
	
	/**
	 * The image size (in bytes)
	 */
	public final int size;

	/**
	 * The image's width x height (in pixels)
	 */
	public final Tuple<Integer, Integer> dimensions;

	/**
	 * The thumbnail's width x height
	 */
	public final Tuple<Integer, Integer> thumbdimensions;

	/**
	 * A URL to the full size image.
	 */
	public final String url;

	/**
	 * A URL to a thumb nail (if you requested it, otherwise null)
	 */
	public final String thumburl;

	/**
	 * The title the selected file redirects to, if applicable.
	 */
	public final String redirectsTo;

	/**
	 * Constructor, takes a JSONObject containing image info returned by the server.
	 * 
	 * @param r The Reply to use.
	 */
	public ImageInfo(Reply r)
	{
		JSONArray ja = r.getJSONArrayR("imageinfo");
		if (ja == null)
		{
			size = 0;
			dimensions = thumbdimensions = null;
			url = thumburl = redirectsTo = title = null;
		}
		else
		{
			Reply params = new Reply(ja.getJSONObject(0));
			size = params.getIntR("size");
			dimensions = new Tuple<>(params.getIntR("width"), params.getIntR("height"));
			
			if(params.has("thumburl"))
			{
				thumburl = params.getString("thumburl");
				thumbdimensions = new Tuple<>(params.getIntR("thumbwidth"), params.getIntR("thumbheight"));
			}
			else
			{
				thumburl = null;
				thumbdimensions = null;
			}
			
			url = params.getString("url");
			title = r.getString("title");
			redirectsTo = title.equals(params.getString("canonicaltitle")) ? null : params.getString("canonicaltitle");
		}
	}
}