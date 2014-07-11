package jwiki.core;

import java.time.Instant;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Represents a revision in the history of a page.
 * 
 * @author Fastily
 * 
 */
public class Revision
{
	/**
	 * The title of the page this revision was made to
	 */
	public final String title;
	
	/**
	 * The edit summary of this revision
	 */
	public final String summary;
	
	/**
	 * The user who made this revision
	 */
	public final String user; 
	
	/**
	 * The text of this revision
	 */
	public final String text; 
	
	/**
	 * The timestamp set by the info returned by the server. MediaWiki returns dates in the form:
	 * <tt>2013-12-16T00:25:17Z</tt>. You can parse it with the pattern <tt>yyyy-MM-dd'T'HH:mm:ss'Z'</tt>. CAVEAT:
	 * MediaWiki sometimes returns garbage values for no apparent reason. 
	 */
	public final Instant timestamp;
	
	/**
	 * Constructor, creates a revision object.
	 * 
	 * @param title The title of the page we're using. This isn't included in the JSONObject representing a revision.
	 * @param rev The JSONObject representation of the revision to parse.
	 */
	private Revision(String title, JSONObject rev)
	{
		
		this.title = title;
		timestamp = Instant.parse(rev.getString("timestamp"));
		summary = rev.getString("comment");
		text = rev.getString("*");
		user = rev.getString("user");
	}
	
	/**
	 * Makes revisions with the JSON reply from the server.
	 * 
	 * @param reply The reply from the server.
	 * @return Revision data parsed from the JSONObject.
	 */
	protected static ArrayList<Revision> makeRevs(ServerReply reply)
	{
		ArrayList<Revision> rl = new ArrayList<Revision>();
		String title = "";
		try
		{
			title = reply.getStringR("title");
			JSONArray revs = reply.getJSONArrayR("revisions");
			
			for (int i = 0; i < revs.length(); i++)
				rl.add(new Revision(title, revs.getJSONObject(i)));
			
			return rl;
		}
		catch (Throwable e)
		{
			//e.printStackTrace();
			Logger.fyi("Looks like the page, " + title + ", doesn't have revisions");
			return new ArrayList<Revision>();
		}
	}
	
	/**
	 * Gets a String representation of this object . Nice for debugging.
	 * 
	 * @return A string representation of this object.
	 */
	public String toString()
	{
		return String.format("----%nTitle:%s%nSummary:%s%nUser:%s%nText:%s%nTimestamp:%s%n----", title, summary, user, text,
				timestamp.toString());
	}
}