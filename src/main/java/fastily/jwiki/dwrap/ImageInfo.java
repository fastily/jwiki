package fastily.jwiki.dwrap;

import java.time.Instant;

import com.google.gson.JsonObject;

import fastily.jwiki.util.GSONP;
import fastily.jwiki.util.Tuple;
import okhttp3.HttpUrl;

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
	 * The url of the full size image.
	 */
	public final HttpUrl url;

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
		super(GSONP.getStr(r, "user"), null, GSONP.getStr(r, "comment"), Instant.parse(GSONP.getStr(r, "timestamp")));
		size = r.get("size").getAsInt();
		dimensions = new Tuple<>(r.get("width").getAsInt(), r.get("height").getAsInt());
		url = HttpUrl.parse(GSONP.getStr(r, "url"));

		sha1 = GSONP.getStr(r, "sha1");
		mime = GSONP.getStr(r, "mime");
	}

	/**
	 * Compares the timestamps of two ImageInfo objects. Orders items newer -&gt; older.
	 */
	public int compareTo(ImageInfo o)
	{
		return o.timestamp.compareTo(timestamp);
	}
}