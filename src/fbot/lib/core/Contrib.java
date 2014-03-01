package fbot.lib.core;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fbot.lib.core.aux.JSONParse;

/**
 * Represents a contribution made by a user.
 * @author Fastily
 *
 */
public class Contrib
{
	/**
	 * The name of the user who made the contribution.
	 */
	private String user;
	
	/**
	 * Title and edit summary.
	 */
	private String title, summary;
	
	/**
	 * The date at which this edit was made.
	 */
	private Date timestamp;
	
	/**
	 * Revision id, parent page id.
	 */
	private int revid, parentid;
	
	/**
	 * Creates a Contrib object from a JSONObject returned by the server, representing a single contribtion.
	 * @param jo The JSONObject returned by the server, representing a contribution.
	 * @throws JSONException Bad reply
	 * @throws ParseException Eh?
	 */
	private Contrib(JSONObject jo) throws JSONException, ParseException
	{
		user = jo.getString("user");
		title = jo.getString("title");
		summary = jo.getString("comment");
		timestamp = Constants.sdf.parse(jo.getString("timestamp"));
		revid = jo.getInt("revid");
		parentid = jo.getInt("parentid");
	}
	
	/**
	 * Creates an array of Contrib objects from the given reply by the server.
	 * @param reply The reply made by the server.
	 * @return A list of Contrib objects created from this server response.
	 */
	protected static Contrib[] makeContribs(JSONObject reply)
	{
		ArrayList<Contrib> l = new ArrayList<Contrib>();
		try
		{
			JSONArray jl = JSONParse.getJSONArrayR(reply, "usercontribs");
			for (int i = 0; i < jl.length(); i++)
				l.add(new Contrib(jl.getJSONObject(i)));
			
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return new Contrib[0];
		}
		return l.toArray(new Contrib[0]);
	}
	
	/**
	 * Gets the name of the user who made the edit.
	 * @return The name of the user who made the edit.
	 */
	public String getUser()
	{
		return user;
	}
	
	/**
	 * Get the title of the page.
	 * @return The title of the page
	 */
	public String getTitle()
	{
		return title;
	}
	
	/**
	 * Get the edit summary.
	 * @return The edit summary.
	 */
	public String getSummary()
	{
		return summary;
	}
	
	/**
	 * Get the date the edit was made
	 * @return The date the edit was made
	 */
	public Date getDate()
	{
		return timestamp;
	}
	
	/**
	 * Get the unique revision ID for this edit.
	 * @return The unique revision ID for this edit
	 */
	public int getRevId()
	{
		return revid;
	}
	
	/**
	 * Get the Parent Page ID for this revision.
	 * @return The Parent Page ID for this revision.
	 */
	public int getParentId()
	{
		return parentid;
	}
	
	/**
	 * Gets a String representation for this object.  Nice for debugging.
	 * @return A String representation for this object
	 */
	public String toString()
	{
		return String.format("----%nUser: %s%nTitle: %s%nSummary: %s%nTimestamp: %s%nRevID:%d%nParentID: %d%n----", user, title,
				summary, timestamp.toString(), revid, parentid);
	}
}