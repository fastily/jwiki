package jwiki.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import jwiki.util.FString;
import jwiki.util.Tuple;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Contains base methods for making queries to the MediaWiki API. Use of this class is intended for anybody wishing to
 * implement their own custom API queries that I have not included in <tt>Wiki.java</tt>. This is not for the faint of
 * heart, so consider yourself warned :P
 * 
 * @see Wiki
 * @author Fastily
 *
 */
public class QueryTools
{
	/**
	 * The maximum list query size for users.
	 */
	private static final int maxQuerySize = 500;

	/**
	 * Constructors disallowed.
	 */
	private QueryTools()
	{

	}

	/* //////////////////////////////////////////////////////////////////////////////// */
	/* ////////////////////////////// QUERY FUNCTIONS ///////////////////////////////// */
	/* //////////////////////////////////////////////////////////////////////////////// */

	/**
	 * Performs a series of continuation queries until error or end of continues.
	 * 
	 * @param wiki The wiki object to use
	 * @param ub The URLBuilder to use
	 * @param rl The list to put retrieved ServerReplies in.
	 */
	private static void doQuerySet(Wiki wiki, URLBuilder ub, ArrayList<ServerReply> rl)
	{
		while (true)
		{
			ServerReply r = doSingleQuery(wiki, ub);
			if (r == null || r.hasError())
				break;

			rl.add(r);

			if (r.has("continue"))
				applyContinue(ub, r);
			else
				break;
		}
	}

	/**
	 * Send a single query to the server and read the reply.
	 * 
	 * @param wiki The wiki object to use.
	 * @param ub The URLBuilder to use.
	 * @return The reply from the server.
	 */
	public static ServerReply doSingleQuery(Wiki wiki, URLBuilder ub)
	{
		try
		{
			return ClientRequest.get(ub.makeURL(), wiki.cookiejar);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Performs a multi-query on a URL. This method will continue making requests to the server until all the data in the
	 * requested set has been returned.
	 * 
	 * @param wiki The wiki object to use
	 * @param ub The URLBuilder to use
	 * @param limString The limit string (e.g. ailimit). If this is set in <tt>ub</tt>, it <i>will</i> be overwritten.
	 *           This is an optional param. Set to null to disable.
	 * @param tkey The parameter to pass to the server which will be paired with <tt>titles</tt>. Note that
	 *           <tt>titles</tt> in <tt>ub</tt> *will* be overwritten.
	 * @param titles The list of titles to send to the server. PRECONDITION: These should not be URL-encoded.
	 * @return The replies from the server.
	 */
	public static ArrayList<ServerReply> doMultiQuery(Wiki wiki, URLBuilder ub, String limString, String tkey,
			String... titles)
	{
		ArrayList<ServerReply> l = new ArrayList<>();

		ub.setParams("continue", ""); // MW 1.21+
		if (limString != null)
			ub.setParams(limString, "max");

		LinkedList<String> atl = new LinkedList<>(Arrays.asList(titles));

		while (!atl.isEmpty())
		{
			ArrayList<String> t = new ArrayList<>();
			for (int i = 0; i < Settings.groupquerymax && atl.peek() != null; i++)
				t.add(atl.poll());
			ub.setParams(tkey, FString.enc(FString.fenceMaker("|", t.toArray(new String[0]))));

			doQuerySet(wiki, ub, l);
		}
		return l;
	}

	/**
	 * Performs a multi-query on a URL. This method will continue making requests to the server until all the data in the
	 * requested set has been returned. Note that this method does not accept a title param - this method should be used
	 * to query software lists (e.g. allpages)
	 * 
	 * @param wiki The wiki object to use
	 * @param ub The URLBuilder to use
	 * @return The replies from the server.
	 */
	public static ArrayList<ServerReply> doNoTitleMultiQuery(Wiki wiki, URLBuilder ub)
	{
		ArrayList<ServerReply> l = new ArrayList<>();
		ub.setParams("continue", ""); // MW 1.21+
		doQuerySet(wiki, ub, l);

		return l;
	}

	/**
	 * Performs a group query for multiple titles. This method efficiently passes the maximum number of titles to the
	 * server and then saves the results as ServerReplies.
	 * 
	 * @param wiki The wiki object to use
	 * @param ub The URLBuilder to use. CAVEAT: anything assigned to the '<tt>titles</tt>' param <i>will</i> be
	 *           overwritten with elements passed into this method's '<tt>titles</tt>'. In other words, don't set the '
	 *           <tt>titles</tt>' param when creating <tt>ub</tt>
	 * @param tkey The parameter name for the list of titles to pass to the server.
	 * @param titles The titles to query.
	 * @return The replies from the server.
	 */
	public static ArrayList<ServerReply> doGroupQuery(Wiki wiki, URLBuilder ub, String tkey, String... titles)
	{
		ArrayList<ServerReply> srl = new ArrayList<>();

		LinkedList<String> l = new LinkedList<String>(Arrays.asList(titles));
		while (!l.isEmpty())
		{
			ArrayList<String> t = new ArrayList<>();
			for (int i = 0; i < Settings.groupquerymax && l.peek() != null; i++)
				t.add(l.poll());

			ub.setParams(tkey, FString.enc(FString.fenceMaker("|", titles)));
			System.out.println(ub.makeURL().toString());

			ServerReply r = doSingleQuery(wiki, ub);
			if (r != null)
				srl.add(r);
			else
				break;
		}
		return srl;
	}

	/**
	 * Performs a series of queries on a single item but restricts number of returned entries. NB: this method does not
	 * actually perform any parsing - it only collects replies. You will have to do the parsing yourself!
	 * 
	 * @param wiki The wiki object to use
	 * @param ub The URLBuilder to use
	 * @param limString The limit string (e.g. ailimit). If this is set in <tt>ub</tt>, it <i>will</i> be overwritten.
	 * @param cap The maximum number of entries to return.
	 * @param tkey The parameter name for the list of titles to pass to the server. Optional param - set to null to
	 *           disable.
	 * @param title The titles to query. Optional param - used by tkey. Also set this to null if you disabled tkey.
	 * @return A list of ServerReplies we collected.
	 */
	public static ArrayList<ServerReply> doLimitedQuery(Wiki wiki, URLBuilder ub, String limString, int cap, String tkey,
			String title)
	{
		if (tkey != null)
			ub.setParams(tkey, FString.enc(title));

		ArrayList<ServerReply> l = new ArrayList<>();
		if (maxQuerySize >= cap)
		{
			ub.setParams(limString, "" + cap);
			ServerReply r = doSingleQuery(wiki, ub);
			if (r != null)
				l.add(r);
		}
		else
			for (int fetch_num = maxQuerySize, done = 0; done < cap;)
			{
				ub.setParams(limString, "" + fetch_num);
				ServerReply r = doSingleQuery(wiki, ub);
				if (r != null)
					l.add(r);

				if (!applyContinue(ub, r))
					break;

				done += fetch_num;
				if (cap - done < maxQuerySize) // runs n times instead of n-1, but meh, still O(n)
					fetch_num = cap - done;
			}
		return l;
	}

	/**
	 * Identifies the <tt>continue</tt> JSONObject in a ServerReply and applies it to a URLBuilder. PRECONDITION: there
	 * MUST be a <tt>continue</tt> obejct in <tt>r</tt>; this method does not explicitly test for MediaWiki errors or for
	 * the presence of a <tt>continue</tt> object.a
	 * 
	 * @param ub The URLBuilder to apply the continue params to
	 * @param r The ServerReply from the most recent, previous request to the server created by <tt>ub</tt>.
	 * @return True if we encountered no errors.
	 */
	public static boolean applyContinue(URLBuilder ub, ServerReply r)
	{
		try
		{
			JSONObject cont = r.getJSONObject("continue");
			for (String s : JSONObject.getNames(cont))
				ub.setParams(s, FString.enc(cont.get(s).toString()));

			return true;
		}
		catch (Throwable e)
		{
			return false;
		}
	}

	/* //////////////////////////////////////////////////////////////////////////////// */
	/* //////////////////////////////// PARSE TOOLS /////////////////////////////////// */
	/* //////////////////////////////////////////////////////////////////////////////// */

	/**
	 * Gets a specified String (using its key) from each JSONObject in a JSONArray in a JSONObject. Returns an empty list
	 * if there were no results or if something went wrong.
	 * 
	 * @param r The main ServerReply whose JSONArray we'll be using.
	 * @param arrayKey The key pointing to the JSONArray we want to use
	 * @param arrayElementKey The key pointing to the String in each JSONObject contained in the JSONArray pointed to by
	 *           <tt>arrayKey</tt>
	 * @return The list of Strings we found.
	 */
	private static ArrayList<String> getStringsFromJSONObjectArray(ServerReply r, String arrayKey, String arrayElementKey)
	{
		ArrayList<String> l = new ArrayList<>();

		JSONArray ja = r.getJSONArrayR(arrayKey);
		if (ja == null)
			return l;

		for (int i = 0; i < ja.length(); i++)
			l.add(ja.getJSONObject(i).getString(arrayElementKey));

		return l;
	}

	/**
	 * Grabs two Strings (and packs them into a Tuple) from a JSONArray of JSONObjects containing Strings.
	 * 
	 * @param r The main ServerReply whose JSONArray we'll be using.
	 * @param arrayKey The key pointing to the JSONArray we want to use
	 * @param arrayElementKey1 The key pointing to the String in each JSONObject contained in the JSONArray pointed to by
	 *           <tt>arrayKey</tt> that we want to use as the first element in the Tuple
	 * @param arrayElementKey2 The key pointing to the String in each JSONObject contained in the JSONArray pointed to by
	 *           <tt>arrayKey</tt> that we want to use as the second element in the Tuple
	 * @return The list of Tuples we found.
	 */
	private static ArrayList<Tuple<String, String>> getTuplesFromJSONObjectArray(ServerReply r, String arrayKey,
			String arrayElementKey1, String arrayElementKey2)
	{
		ArrayList<Tuple<String, String>> l = new ArrayList<>();
		if (!r.has(arrayKey))
			return l;

		JSONArray ja = r.getJSONArrayR(arrayKey);
		for (int i = 0; i < ja.length(); i++)
		{
			JSONObject jo = ja.getJSONObject(i);
			l.add(new Tuple<String, String>(jo.getString(arrayElementKey1), jo.getString(arrayElementKey2)));
		}

		return l;
	}

	/**
	 * Puts a list into a HashMap pointed to by <tt>title</tt>. If <tt>title</tt> is already present in <tt>hl</tt>,
	 * merge <tt>l</tt> with value mapped to <tt>title</tt>. Does nothing if <tt>title</tt> is in <tt>hl</tt> and if
	 * <tt>l</tt> is empty.
	 * 
	 * @param hl The HashMap to perform changes to
	 * @param title The key to look for
	 * @param l The list to add or merge.
	 */
	private static <T> void mapListMerge(HashMap<String, ArrayList<T>> hl, String title, ArrayList<T> l)
	{
		if (!hl.containsKey(title))
			hl.put(title, l);
		else if (!l.isEmpty())
			hl.get(title).addAll(l);
	}

	/**
	 * Converts a HashMap into an ArrayList of Tuple <String, Generics List>.
	 * 
	 * @param hl The HashMap to use
	 * @return An ArrayList of Tuple <String, Generics List>.
	 */
	private static <T> ArrayList<Tuple<String, ArrayList<T>>> mapToList(HashMap<String, ArrayList<T>> hl)
	{
		ArrayList<Tuple<String, ArrayList<T>>> l = new ArrayList<>();
		for (Map.Entry<String, ArrayList<T>> e : hl.entrySet())
			l.add(new Tuple<String, ArrayList<T>>(e.getKey(), e.getValue()));

		return l;
	}

	/* //////////////////////////////////////////////////////////////////////////////// */
	/* /////////////////////// MIXED QUERY & PARSE TOOLS ////////////////////////////// */
	/* //////////////////////////////////////////////////////////////////////////////// */

	/**
	 * Parses a reply from the server in which the heierarchy is pages -> id -> name, [{title : blargh},...]. To be used
	 * with multiple item/title simultaneous queries. This method will fetch single items.
	 * 
	 * @param wiki The wiki object to use
	 * @param ub The URLObject to use
	 * @param limString The limit string to use with this query (e.g. ailimit). Note that this parameter <i>will</i> be
	 *           overwritten in <tt>ub</tt>. This is an optional param - disable with null.
	 * @param arrayKey The key pointing to the JSONArray we want to use
	 * @param arrayElementKey The key pointing to the String in each JSONObject contained in the JSONArray pointed to by
	 *           <tt>arrayKey</tt>
	 * @param titlekey The key pointing the title this object is associated with - contained within each top level
	 * @param tkey The parameter to pass to the server which will be paired with <tt>titles</tt>
	 * @param titles The list of titles to send to the server. PRECONDITION: These should not be URL-encoded.
	 * @return A list of results we retrieved from the data set, where each tuple is <tt>(title, list_of_results)</tt>.
	 */
	protected static ArrayList<Tuple<String, ArrayList<String>>> multiQueryForStrings(Wiki wiki, URLBuilder ub,
			String limString, String arrayKey, String arrayElementKey, String titlekey, String tkey, String... titles)
	{
		HashMap<String, ArrayList<String>> hl = new HashMap<>();

		for (ServerReply r1 : doMultiQuery(wiki, ub, limString, tkey, titles))
			for (ServerReply r2 : r1.bigJSONObjectGet("pages"))
				mapListMerge(hl, r2.getString(titlekey), getStringsFromJSONObjectArray(r2, arrayKey, arrayElementKey));

		return mapToList(hl);
	}

	/**
	 * Parses a reply from the server in which the heierarchy is pages -> id -> name, [{title : blargh},...]. To be used
	 * with multiple item/title simultaneous queries. This method will fetch two items (in a tuple).
	 * 
	 * @param wiki The wiki object to use
	 * @param ub The URLObject to use
	 * @param limString The limit string to use with this query (e.g. ailimit). Note that this parameter <i>will</i> be
	 *           overwritten in <tt>ub</tt>. This is an optional param - disable with null.
	 * @param arrayKey The key pointing to the JSONArray we want to use
	 * @param arrayElementKey1 The key pointing to the String in each JSONObject contained in the JSONArray pointed to by
	 *           <tt>arrayKey</tt> that will be used for the first element in the returned Tuple
	 * @param arrayElementKey2 The key pointing to the String in each JSONObject contained in the JSONArray pointed to by
	 *           <tt>arrayKey</tt> that will be used for the second element in the returned Tuple
	 * @param titlekey The key pointing the title this object is associated with - contained within each top level
	 * @param tkey The parameter to pass to the server which will be paired with <tt>titles</tt>
	 * @param titles The list of titles to send to the server. PRECONDITION: These should not be URL-encoded.
	 * @return A list of results we retrieved from the data set, where each tuple is <tt>(title, list_of_results)</tt>.
	 */
	protected static ArrayList<Tuple<String, ArrayList<Tuple<String, String>>>> multiQueryForTuples(Wiki wiki, URLBuilder ub,
			String limString, String arrayKey, String arrayElementKey1, String arrayElementKey2, String titlekey, String tkey,
			String... titles)
	{
		HashMap<String, ArrayList<Tuple<String, String>>> hl = new HashMap<>();

		for (ServerReply r1 : doMultiQuery(wiki, ub, limString, tkey, titles))
			for (ServerReply r2 : r1.bigJSONObjectGet("pages"))
				mapListMerge(hl, r2.getString(titlekey),
						getTuplesFromJSONObjectArray(r2, arrayKey, arrayElementKey1, arrayElementKey2));

		return mapToList(hl);
	}

	/**
	 * Makes a group query and parses the reply where the reply is a list of JSONObjects with a JSONArray of Strings we
	 * want.
	 * 
	 * @param wiki The wiki object to use
	 * @param ub The URLBuilder to use. Caveat: if you set key <tt>titles</tt>, it will be overwritten!
	 * @param topArrayKey The key pointing to the list of JSONObjects we want.
	 * @param titlekey The String to key to each value (ArrayList) in the returned list.
	 * @param arrayKey The key pointing to each array
	 * @param tkey The parameter to pass to the server which will be paired with <tt>titles</tt>
	 * @param titles The list of titles to send to the server.
	 * @return A list of results from the server.
	 */
	protected static ArrayList<Tuple<String, ArrayList<String>>> groupQueryForLists(Wiki wiki, URLBuilder ub,
			String topArrayKey, String titlekey, String arrayKey, String tkey, String... titles)
	{
		ArrayList<Tuple<String, ArrayList<String>>> l = new ArrayList<>();

		for (ServerReply r : doGroupQuery(wiki, ub, tkey, titles))
		{
			JSONArray ja = r.getJSONArrayR(topArrayKey);
			for (int i = 0; i < ja.length(); i++)
			{
				JSONObject jo = ja.getJSONObject(i);
				l.add(new Tuple<String, ArrayList<String>>(jo.getString(titlekey), FString.jsonArrayToString(jo
						.getJSONArray(arrayKey))));
			}
		}
		return l;
	}

	/**
	 * Performs a series of queries on a single item but restricts number of returned entries. This will only work on
	 * datasets where the content we're interested can be represented by a list of Strings.
	 * 
	 * @param wiki The wiki object to use.
	 * @param ub The URLBuilder to use.
	 * @param limString The limit string to use (e.g. ailimit). Note that this <i>will</i> be overwritten if previously
	 *           set in <tt>ub</tt>.
	 * @param cap The maximum number of elements to return
	 * @param arrayKey The key pointing to the JSONArray we want to use
	 * @param arrayElementKey The key pointing to the String in each JSONObject contained in the JSONArray pointed to by
	 *           <tt>arrayKey</tt>
	 * @param tkey The parameter name for the list of titles to pass to the server. Optional param - set to null to
	 *           disable.
	 * @param title The titles to query. Optional param - used by tkey. Also set this to null if you disabled tkey.
	 * @return A list of results we retrieved from the data set,.
	 */
	protected static ArrayList<String> limitedQueryForStrings(Wiki wiki, URLBuilder ub, String limString, int cap,
			String arrayKey, String arrayElementKey, String tkey, String title)
	{
		ArrayList<String> l = new ArrayList<>();
		for (ServerReply r : doLimitedQuery(wiki, ub, limString, cap, tkey, title))
			l.addAll(getStringsFromJSONObjectArray(r, arrayKey, arrayElementKey));
		return l;
	}

	/**
	 * Parses a reply from the server in which the hierarchy is pages -> id -> name, [{title : blargh},...]. To be used
	 * with single item/title queries.
	 * 
	 * @param wiki The wiki object to use
	 * @param ub The URLBuilder to use
	 * @param limString The limit string to use with this query (e.g. ailimit). Note that this parameter <i>will</i> be
	 *           overwritten in <tt>ub</tt>. This is an optional param - disable with null.
	 * @param arrayKey The key pointing to the JSONArray we want to use
	 * @param arrayElementKey The key pointing to the String in each JSONObject contained in the JSONArray pointed to by
	 *           <tt>arrayKey</tt>
	 * @param tkey The parameter to pass to the server which will be paired with <tt>titles</tt>
	 * @param titles The list of titles to send to the server. PRECONDITION: These should not be URL-encoded.
	 * @return A list of results we retrieved from the data set.
	 */
	protected static ArrayList<String> queryForStrings(Wiki wiki, URLBuilder ub, String limString, String arrayKey,
			String arrayElementKey, String tkey, String... titles)
	{
		ArrayList<String> l = new ArrayList<>();
		for (ServerReply r : tkey != null ? doMultiQuery(wiki, ub, limString, tkey, titles) : doNoTitleMultiQuery(wiki, ub))
			l.addAll(getStringsFromJSONObjectArray(r, arrayKey, arrayElementKey));
		return l;
	}

}