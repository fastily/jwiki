package jwiki.core;

import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a contribution made by a user.
 * 
 * @author Fastily
 *
 */
public class Contrib extends DataEntry
{
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
	 * 
	 * @param jo The JSONObject returned by the server, representing a contribution.
	 * @throws JSONException Bad reply
	 * @throws ParseException Eh?
	 */
	private Contrib(JSONObject jo) throws JSONException, ParseException
	{
		super(jo.getString("user"), jo.getString("title"), jo.getString("comment"), Instant.parse(jo.getString("timestamp")));
		revid = jo.getInt("revid");
		parentid = jo.getInt("parentid");
	}

	/**
	 * Creates an array of Contrib objects from the given reply by the server.
	 * 
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
}