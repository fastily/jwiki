package jwiki.dwrap;

import java.time.Instant;

import jwiki.core.Reply;

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
	public Contrib(Reply r)
	{
		super(r.getString("user"), r.getString("title"), r.getString("comment"), Instant.parse(r.getString("timestamp")));
		revid = r.getInt("revid");
		parentid = r.getInt("parentid");
	}
}