package fastily.jwiki.dwrap;

import java.time.Instant;

import com.google.gson.JsonObject;

import fastily.jwiki.util.GSONP;

/**
 * Represents a single revision in the history of a page.
 * 
 * @author Fastily
 * 
 */
public class Revision extends DataEntry
{
	/**
	 * The text of this revision
	 */
	public final String text;

	/**
	 * Constructor, creates a revision object.
	 * 
	 * @param title The title of the page we're using. NB: This isn't explicitly included in the JSONObject from the
	 *           server
	 * @param r The ServerReply containing the revision to parse.
	 */
	public Revision(JsonObject r)
	{
		super(GSONP.gString(r, "user"), null, GSONP.gString(r, "comment"), Instant.parse(GSONP.gString(r, "timestamp")));
		text = GSONP.gString(r, "*");
	}
}