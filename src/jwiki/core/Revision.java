package jwiki.core;

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
	 * Interal field value. Page title, summary, user who made edit, and text of edit.
	 */
	private String title, summary, user, text;
	
	//TODO: Fixme
	/**
	 * The timestamp set by the info returned by the server. MediaWiki returns dates in the form:
	 * <tt>2013-12-16T00:25:17Z</tt>. You can parse it with the pattern <tt>yyyy-MM-dd'T'HH:mm:ss'Z'</tt>. CAVEAT:
	 * MediaWiki sometimes returns garbage values for no apparent reason. Try+Catch is a MUST when parsing via
	 * SimpleDateFormat (one is provided in the Constants class).
	 */
	private String timestamp;
	
	/**
	 * Constructor, creates a revision object.
	 * 
	 * @param title The title of the page we're using. This isn't included in the JSONObject representing a revision.
	 * @param rev The JSONObject representation of the revision to parse.
	 */
	private Revision(String title, JSONObject rev)
	{
		this.title = title;
		timestamp = rev.getString("timestamp");
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
	protected static Revision[] makeRevs(ServerReply reply)
	{
		ArrayList<Revision> rl = new ArrayList<Revision>();
		String title = "";
		try
		{
			title = reply.getStringR("title");
			JSONArray revs = reply.getJSONArrayR("revisions");
			
			for (int i = 0; i < revs.length(); i++)
				rl.add(new Revision(title, revs.getJSONObject(i)));
			
			return rl.toArray(new Revision[0]);
		}
		catch (Throwable e)
		{
			//e.printStackTrace();
			Logger.fyi("Looks like the page, " + title + ", doesn't have revisions");
			return new Revision[0];
		}
	}
	
	/**
	 * Gets the title associated with the revision.
	 * 
	 * @return The title associated with the revision.
	 */
	public String getTitle()
	{
		return title;
	}
	
	/**
	 * Gets the edit summary associated with the revision.
	 * 
	 * @return The edit summary associated with this revision.
	 */
	public String getSummary()
	{
		return summary;
	}
	
	/**
	 * Gets the name of the user who made the revision. Excludes 'User:' prefix.
	 * 
	 * @return The name of the user who made the revision.
	 */
	public String getUser()
	{
		return user;
	}
	
	/**
	 * Gets the text of a revision.
	 * 
	 * @return The text of this revision.
	 */
	public String getText()
	{
		return text;
	}
	
	/**
	 * Gets the time at which this revision was made.
	 * 
	 * @return A date representing the time the revision was made.
	 */
	public String getTimestamp()
	{
		return timestamp;
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