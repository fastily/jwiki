package jwiki.core;

import java.time.Instant;
import java.util.ArrayList;

import org.json.JSONArray;

/**
 * Represents a contribution made by a user.
 * 
 * @author Fastily
 *
 */
public class Contrib extends DataEntry
{
	/**
	 * This contribution's revision id.
	 */
	public final int revid;

	/**
	 * This contribution's parent ID
	 */
	public final int parentid;

	/**
	 * Creates a Contrib object from a JSONObject returned by the server, representing a single contribution.
	 * 
	 * @param r The JSONObject returned by the server, representing a contribution.
	 */
	private Contrib(Reply r)
	{
		super(r.getString("user"), r.getString("title"), r.getString("comment"), Instant.parse(r.getString("timestamp")));
		revid = r.getInt("revid");
		parentid = r.getInt("parentid");
	}

	/**
	 * Creates a list of Contrib objects from the reply by the server.
	 * 
	 * @param srl The replies from the server.
	 * @return A list of Contribs created from the server's reply.
	 */
	protected static ArrayList<Contrib> makeContribs(ArrayList<Reply> srl)
	{
		ArrayList<Contrib> l = new ArrayList<>();
		for (Reply r : srl)
		{
			JSONArray jl = r.getJSONArrayR("usercontribs");
			for (int i = 0; i < jl.length(); i++)
				l.add(new Contrib(new Reply(jl.getJSONObject(i))));
		}
		return l;
	}
}