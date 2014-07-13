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
public class Revision extends DataEntry
{
	/**
	 * The text of this revision
	 */
	public final String text;

	/**
	 * Constructor, creates a revision object.
	 * 
	 * @param title The title of the page we're using. This isn't included in the JSONObject representing a revision.
	 * @param rev The JSONObject representation of the revision to parse.
	 */
	private Revision(String title, JSONObject rev)
	{
		super(rev.getString("user"), title, rev.getString("comment"), Instant.parse(rev.getString("timestamp")));
		text = rev.getString("*");
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
			// e.printStackTrace();
			Logger.fyi("Looks like the page, " + title + ", doesn't have revisions");
			return new ArrayList<Revision>();
		}
	}
}