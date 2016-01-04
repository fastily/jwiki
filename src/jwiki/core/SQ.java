package jwiki.core;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import jwiki.util.FString;

import static jwiki.core.Settings.*;

/**
 * A template server query object that can be used to make a variety of queries. It is advisable to create a new
 * <code>SQ</code> object for each query.
 * 
 * @author Fastily
 *
 */
public class SQ
{
	/**
	 * The low maximum limit for maximum number of list items returned for queries that return lists. Use this if a max
	 * value is needed but where the client does not know the max.
	 */
	private static final int maxResultLimit = 500;

	/**
	 * The Wiki object to use
	 */
	private Wiki wiki;

	/**
	 * The default parameters to use when creating URLBuilders.
	 */
	private HashMap<String, String> pl;

	/**
	 * The limit parameter which restricts the number of list items returned from the server. Optional, set null to
	 * disable.
	 */
	private String limString;

	/**
	 * The maximum number of results to return in a list (where applicable) for a single query. Optional, set to a number
	 * less than zero to disable.
	 */
	private int maxResults = -1;

	/**
	 * The maximum number of results to return in a list (where applicable) for a single query. This is a String derived
	 * from <code>maxResults</code>.
	 */
	private String strMax = "max";

	/**
	 * Constructor, sets a limit String and the overall maximum number of results returned in a [continuation] query.
	 * 
	 * @param wiki The Wiki object to use
	 * @param limString The limit String. Optional, set null to disable. Highly recommended that this is set, or else
	 *           MediaWiki may limit the number of returned results to 10, which adversely affects performance for
	 *           queries with many results. queries.
	 * @param maxResults The maximum number of results to return in a list (where applicable) for a single query.
	 *           Optional, set to a number less than zero to disable. Parameter is ignored if <code>limString</code> is
	 *           null.
	 * @param pl The default parameters (excluding <code>action</code>) to initialize URLBuilders with.
	 */
	protected SQ(Wiki wiki, String limString, int maxResults, HashMap<String, String> pl)
	{
		this(wiki, pl);
		this.limString = limString;

		if (maxResults > 0)
			strMax = "" + (this.maxResults = maxResults);
	}

	/**
	 * Constructor, sets a limit String.
	 * 
	 * @param wiki The wiki object to use.
	 * @param limString The limit String. Optional, set null to disable. Highly recommended that this is set, or else
	 *           MediaWiki may limit the number of returned results to 10, which adversely affects performance for
	 *           queries with many results.
	 * @param pl The default parameters (excluding <code>action</code>) to initialize URLBuilders with.
	 */
	protected SQ(Wiki wiki, String limString, HashMap<String, String> pl)
	{
		this(wiki, limString, -1, pl);
	}

	/**
	 * Constructor, use for basic queries.
	 * 
	 * @param wiki The wiki object to use.
	 * @param pl The default parameters (excluding <code>action</code>) to initialize URLBuilders with.
	 */
	protected SQ(Wiki wiki, HashMap<String, String> pl)
	{
		this.wiki = wiki;
		this.pl = pl;
	}

	/**
	 * Creates a URLBuilder. First applies the default parameters specified in the constructor, and then any optional
	 * parameters.
	 * 
	 * @param params Optional parameters to apply to the returned URLBuilder.
	 * @return A URLBuilder.
	 */
	private URLBuilder makeUB(String... params)
	{
		URLBuilder ub = wiki.makeUB("query", params);
		ub.setParams(pl);
		//ub.setParams(params);

		if (limString != null)
			ub.setParam(limString, strMax);

		return ub;
	}

	/**
	 * Performs an efficient, multi-title query for a given list of titles. Does not send any more than
	 * <code>Settings.groupQueryMax</code> titles at any once, and loops as necessary. Exercise caution when using this
	 * on large data sets; some titles return thousands of entries per query.
	 * 
	 * @param tkey The title key to pass to the server
	 * @param titles The titles to query with
	 * @return An RSet with the results.
	 */
	protected RSet multiTitleQuery(String tkey, ArrayList<String> titles)
	{
		RSet rs = new RSet();

		int size = titles.size();
		for (int start = 0, end = groupQueryMax; start < size; start += groupQueryMax, end += groupQueryMax)
		{
			if (size - start < groupQueryMax)
				end = size;
			
			rs.merge(multiQuery(tkey, FString.fenceMaker("|", titles.subList(start, end))));
			
			//rs.merge(multiQuery(tkey, URLBuilder.chainProps(titles.subList(start, end))));
		}
		return rs;
	}

	/**
	 * Performs a series of continuation-based queries that return results in a
	 * <a href="https://www.mediawiki.org/wiki/API:Lists">list</a>. Results will be limited or unlimited based on the
	 * settings of this object.
	 * 
	 * @param params Additional parameters to apply to the query before it is sent.
	 * @return An RSet with the replies from the Server.
	 */
	protected RSet multiQuery(String... params)
	{
		ArrayList<Reply> rl = new ArrayList<>();
		URLBuilder ub = makeUB(params);
		ub.setParam("continue", "");

		if (maxResults <= 0)
			while (doContQuery(ub, rl))
				;
		else
		{
			ub.setParam(limString, "" + maxResultLimit);
			for (int remain = maxResults; remain > 0; remain -= maxResultLimit)
			{
				if (remain < maxResultLimit)
					ub.setParam(limString, "" + remain);

				doContQuery(ub, rl);
			}
		}
		return new RSet(rl);
	}

	/**
	 * Performs a simple, single query.
	 * 
	 * @return The Reply from the server.
	 */
	protected Reply query()
	{
		return doQuery(makeUB());
	}

	/**
	 * Query the server and return the Reply.
	 * 
	 * @param The URLBuilder to use
	 * @return A Reply from the server or null on error.
	 */
	private Reply doQuery(URLBuilder ub)
	{
		try
		{
			return Req.get(ub.makeURL(), wiki.cookiejar);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Performs a query, checks for errors, and applies continuation parameters. This method mutates <code>ub</code>
	 * (update continue parameters) and <code>rl</code> (append the server's Reply).
	 * 
	 * @param ub The URLBuilder to use.
	 * @param rl The list of Reply objects to append the Reply from this query to.
	 * @return True if it is <i>probably</i> safe to continue making queries. False means stop.
	 */
	private boolean doContQuery(URLBuilder ub, ArrayList<Reply> rl)
	{
		Reply r = doQuery(ub);
		if (r == null || r.hasError())
			return false;

		rl.add(r);
		
		if (r.has("continue")) // continuation queries
		{
			JSONObject cont = r.getJSONObject("continue");
			for (String s : JSONObject.getNames(cont))
				ub.setParam(s, cont.get(s).toString());

			return true;
		}		
		return false;
	}
}