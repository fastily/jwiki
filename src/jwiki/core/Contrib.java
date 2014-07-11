package jwiki.core;

import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
	public final String user;
	
	/**
	 * Title and edit summary.
	 */
	public final String title;
	
	/**
	 * The edit summary used in this contribution.
	 */
	public final String summary;
	
	/**
	 * The date and time at which this edit was made.
	 */
	public final Instant timestamp;
	
	/**
	 * Revision id.
	 */
	public final int revid;
	
	/**
	 * This revision's parent ID
	 */
	public final int parentid;
	
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
		timestamp = Instant.parse(jo.getString("timestamp"));
		revid = jo.getInt("revid");
		parentid = jo.getInt("parentid");
	}
	
	/**
	 * Creates an array of Contrib objects from the given reply by the server.
	 * @param reply The reply made by the server.
	 * @return A list of Contrib objects created from this server response.
	 */
	protected static Contrib[] makeContribs(ServerReply reply)
	{
		ArrayList<Contrib> l = new ArrayList<Contrib>();
		try
		{
			JSONArray jl = reply.getJSONArrayR("usercontribs");
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
	 * Gets a String representation for this object.  Nice for debugging.
	 * @return A String representation for this object
	 */
	public String toString()
	{
		return String.format("----%nUser: %s%nTitle: %s%nSummary: %s%nTimestamp: %s%nRevID:%d%nParentID: %d%n----", user, title,
				summary, timestamp.toString(), revid, parentid);
	}
}