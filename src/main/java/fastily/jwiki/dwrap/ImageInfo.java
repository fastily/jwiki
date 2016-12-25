package fastily.jwiki.dwrap;

import java.net.URL;
import java.time.Instant;

import com.google.gson.JsonObject;

import fastily.jwiki.util.GSONP;
import fastily.jwiki.util.Tuple;

/**
 * Container object for a result returned by the ImageInfo MediaWiki module.
 * 
 * @author Fastily
 * 
 */
public final class ImageInfo extends DataEntry implements Comparable<ImageInfo>
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
	 * The sha1 hash for this file
	 */
	public final String sha1;

	/**
	 * A URL to the full size image.
	 */
	public final URL url;

	/**
	 * The MIME string of the file.
	 */
	public final String mime;

	/**
	 * Constructor, takes a JSONObject containing image info returned by the server.
	 * 
	 * @param r The Reply to use.
	 */
	public ImageInfo(JsonObject r)
	{
		super(GSONP.gString(r, "user"), null, GSONP.gString(r, "comment"), Instant.parse(GSONP.gString(r, "timestamp")));
		size = r.get("size").getAsInt();
		dimensions = new Tuple<>(r.get("width").getAsInt(), r.get("height").getAsInt());
		url = genURL(GSONP.gString(r, "url"));

		sha1 = GSONP.gString(r, "sha1");
		mime = GSONP.gString(r, "mime");
	}

	/**
	 * Compares the timestamps of two ImageInfo objects. Orders items newer -&gt; older.
	 */
	public int compareTo(ImageInfo o)
	{
		return o.timestamp.compareTo(timestamp);
	}

	/**
	 * Creates a URL from a String.
	 * 
	 * @param u The String to turn into a URL
	 * @return The URL, or null if something went wrong.
	 */
	private static URL genURL(String u)
	{
		try
		{
			return u == null ? null : new URL(u);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}
}