package jwiki.core;

import java.time.Instant;
import java.util.ArrayList;

import org.json.JSONArray;

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
	private Revision(String title, Reply r)
	{
		super(r.getString("user"), title, r.getString("comment"), Instant.parse(r.getString("timestamp")));
		text = r.getString("*");
	}

	/**
	 * Makes revisions with the reply from the server.
	 * 
	 * @param srl The list of replies from the server.
	 * @return Revision data parsed from the server reply.
	 */
	protected static ArrayList<Revision> makeRevs(ArrayList<Reply> srl)
	{
		ArrayList<Revision> rl = new ArrayList<>();

		for (Reply r : srl)
		{
			JSONArray ja = r.getJSONArrayR("revisions");
			for (int i = 0; i < ja.length(); i++)
				rl.add(new Revision(r.getStringR("title"), new Reply(ja.getJSONObject(i))));
		}
		return rl;
	}
}