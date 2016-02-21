package jwiki.dwrap;

import java.time.Instant;
import java.util.ArrayList;

import jwiki.core.Reply;
import jwiki.util.FL;
import jwiki.util.Tuple;

/**
 * Container object for a result returned by the imageinfo MediaWiki module.
 * 
 * @author Fastily
 * 
 */
public class ImageInfo extends DataEntry implements Comparable<ImageInfo>
{
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
	 * The sha1 hash for this file
	 */
	public final String sha1;

	/**
	 * A URL to the full size image.
	 */
	public final String url;

	/**
	 * The MIME string of the file.
	 */
	public final String mime;

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
	 * @param title The page this ImageInfo is to be created for.
	 * @param r The Reply to use.
	 */
	public ImageInfo(String title, Reply r)
	{
		super(r.getStringR("user"), title, r.getStringR("comment"), Instant.parse(r.getStringR("timestamp")));
		size = r.getIntR("size");
		dimensions = new Tuple<>(r.getIntR("width"), r.getIntR("height"));

		if (r.has("thumburl"))
		{
			thumburl = r.getStringR("thumburl");
			thumbdimensions = new Tuple<>(r.getIntR("thumbwidth"), r.getIntR("thumbheight"));
		}
		else
		{
			thumburl = null;
			thumbdimensions = null;
		}

		url = r.getStringR("url");
		sha1 = r.getStringR("sha1");
		mime = r.getStringR("mime");

		redirectsTo = title.equals(r.getStringR("canonicaltitle")) ? null : r.getStringR("canonicaltitle");
	}

	/**
	 * Generates ImageInfo objects from an ArrayList of JSONObjects.
	 * 
	 * @param title The page these ImageInfo objects are to be created for.
	 * @param ja The list of JSONObjects to convert to ImageInfo.
	 * @return A list of ImageInfo.
	 */
	public static ArrayList<ImageInfo> makeImageInfos(String title, ArrayList<Reply> ja)
	{
		if (ja == null || ja.isEmpty())
			return new ArrayList<>();

		return FL.toAL(ja.stream().map(jo -> new ImageInfo(title, jo)));
	}

	/**
	 * Compares the timestamps of two ImageInfo objects. Orders items newer -> older.
	 */
	public int compareTo(ImageInfo o)
	{
		return o.timestamp.compareTo(timestamp);
	}
}