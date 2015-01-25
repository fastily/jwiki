package jwiki.dwrap;

import java.time.Instant;

import jwiki.core.Reply;

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
	public Revision(String title, Reply r)
	{
		super(r.getString("user"), title, r.getString("comment"), Instant.parse(r.getString("timestamp")));
		text = r.getString("*");
	}
}