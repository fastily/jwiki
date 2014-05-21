package jwiki.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jwiki.util.FString;
import jwiki.util.JSONParse;
import jwiki.util.Tuple;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Contains methods to (without any exceptions) query a wiki. Returns logical empty results if we weren't successful.
 * 
 * @author Fastily
 * 
 */
public class FQuery
{

	/**
	 * Hiding from javadoc
	 */
	private FQuery()
	{

	}

	/**
	 * Generic query-continue fetching method. Continuously performs a query until there are no more results to get, or
	 * until we have all the results we want.
	 * 
	 * @param ub The URLBuilder to use. You should have initialized action and any static parameters.
	 * @param max The maximum number of results to return, or -1 if you want ALL results on the server.
	 * @param limString The limit parameter name (e.g. "rvlimit"), which will cap the max number of results returned per
	 *           query.
	 * @param contString The param to look for when doing a continue (e.g. "rvcontinue").
	 * @param isStr Set to true if we should expect a String for the continue token returned. Otherwise, the token is
	 *           assumed to be an int.
	 * @param wiki The wiki object calling this.
	 * @return A list of JSONObjects, each representing a single query.
	 */
	private static JSONObject[] fatQuery(URLBuilder ub, int max, String limString, String contString, boolean isStr, Wiki wiki)
	{
		boolean unlim = max < 0; // if max is negative, get ALL items possible.
		ArrayList<JSONObject> jl = new ArrayList<JSONObject>();

		int completed = 0; // how many items have we retrieved so far?
		int fetch_num = Constants.maxquerysz; // the number of items to fetch this time.

		try
		{
			while (true)
			{
				if (completed >= max && !unlim)
					break;

				if (!unlim && max - completed < Constants.maxquerysz)
					fetch_num = max - completed;

				ub.setParams(limString, "" + fetch_num);
				Reply r = Request.get(ub.makeURL(), wiki.cookiejar);
				if (r.hasError()) // if there are errors, we'll probably get them on the 1st try
					break;

				JSONObject reply = r.getReply();
				jl.add(reply);
				completed += fetch_num;

				if (!reply.has("query-continue"))
					break;

				ub.setParams(contString,
						FString.enc((isStr ? JSONParse.getStringR(reply, contString) : "" + JSONParse.getIntR(reply, contString))));
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return new JSONObject[0];
		}

		return jl.toArray(new JSONObject[0]);
	}

	/**
	 * Performs a big fat query. Performs a query-continue until there are no more results to get, or until we have all
	 * the results we want.
	 * 
	 * @param ub The URLBuilder to use. You should have initialized action and any static parameters.
	 * @param max The maximum number of results to return, or -1 if you want ALL results on the server.
	 * @param limString The limit parameter name (e.g. "rvlimit"), which will cap the max number of results returned per
	 *           query.
	 * @param contString The param to look for when doing a continue (e.g. "rvcontinue").
	 * @param isStr Set to true if we should expect a String for the continue token returned. Otherwise, the token is
	 *           assumed to be an int.
	 * @param array The JSONArray key to grab
	 * @param arrayEl The JSONArray element to grab
	 * @param wiki The wiki object calling this.
	 * @return A list of titles, as requested.
	 */
	private static String[] multiFatQuery(URLBuilder ub, int max, String limString, String contString, boolean isStr,
			String array, String arrayEl, Wiki wiki)
	{
		ArrayList<String> l = new ArrayList<String>();
		for (JSONObject jo : fatQuery(ub, max, limString, contString, isStr, wiki))
		{
			JSONArray ja = JSONParse.getJSONArrayR(jo, array);
			for (int i = 0; i < ja.length(); i++)
				l.add(ja.getJSONObject(i).getString(arrayEl));
		}

		return l.toArray(new String[0]);
	}

	/**
	 * Makes a generic group query for multiple pages. This will make multi-queries and save the individual results into
	 * JSONObjects.
	 * 
	 * @param ub The URLBuilder to use. This should be derived from the wiki object passed in. PRECONDITION: The action
	 *           parameter of the URLBuilder must be set.
	 * @param parentKey The returned parent key pointing to the list of JSONObjects that we're interested in.
	 * @param wiki The wiki object to use. The wiki object should be logged in with cookies set.
	 * @param titlekey The title param to use in the URLBuilder. This will be set multiple times if titles.length >
	 *           Constants.groupquerymax.
	 * @param titles The titles to get results for.
	 * @return A list of JSONObjects containing data for each of the items we requested.
	 */
	private static JSONObject[] groupQuery(URLBuilder ub, String parentKey, Wiki wiki, String titlekey, String... titles)
	{
		ArrayList<JSONObject> jl = new ArrayList<JSONObject>();
		try
		{
			for (String[] tl : FString.splitStringArray(Constants.groupquerymax, titles))
			{
				ub.setParams(titlekey, FString.enc(FString.fenceMaker("|", tl)));

				Reply r = Request.get(ub.makeURL(), wiki.cookiejar);
				if (r.hasError())
					break;

				JSONObject parent = JSONParse.getJSONObjectR(r.getReply(), parentKey);
				if (parent != null)
					for (String s : JSONObject.getNames(parent))
						jl.add(parent.getJSONObject(s));
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return new JSONObject[0];
		}

		return jl.toArray(new JSONObject[0]);
	}

	/**
	 * Generates an edit token, and assigns it to the wiki object passed in.
	 * 
	 * @param wiki The wiki object to generate an edit token for.
	 * @return True if we were successful.
	 */
	protected static boolean generateEditToken(Wiki wiki)
	{
		Logger.info(wiki, "Fetching edit token");
		try
		{
			URLBuilder ub = new URLBuilder(wiki.domain);
			ub.setAction("tokens");
			ub.setParams("type", "edit");

			wiki.token = Request.get(ub.makeURL(), wiki.cookiejar).getString("edittoken");
			return wiki.token != null;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Generates the namespace list for the wiki object passed in, and assigns it to said wiki object.
	 * 
	 * @param wiki The wiki object to generate an namespace list for.
	 * @return True if we were successful.
	 */
	protected static boolean generateNSL(Wiki wiki)
	{
		Logger.info(wiki, "Generating namespace list");
		try
		{
			URLBuilder ub = new URLBuilder(wiki.domain);
			ub.setAction("query");
			ub.setParams("meta", "siteinfo", "siprop", "namespaces");
			wiki.nsl = Namespace.makeNamespace(Request.get(ub.makeURL(), wiki.cookiejar).getJSONObject("namespaces"));
			return wiki.nsl != null;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Gets the backlinks of a page.
	 * 
	 * @param wiki The wiki object to use
	 * @param title The title to check
	 * @param redirs Set to true to fetch redirects only. Set to false to filter out all redirects.
	 * @return
	 */
	private static String[] getBackLinks(Wiki wiki, String title, boolean redirs)
	{
		Logger.info(wiki, "Fetching backlinks to " + title);
		URLBuilder ub = wiki.makeUB();
		ub.setAction("query");
		ub.setParams("list", "backlinks", "bltitle", FString.enc(title), "blfilterredir", redirs ? "redirects"
				: "nonredirects");

		ArrayList<String> l = new ArrayList<String>();
		for (JSONObject jo : fatQuery(ub, -1, "bllimit", "blcontinue", true, wiki))
		{
			JSONArray jl = JSONParse.getJSONArrayR(jo, "backlinks");
			for (int i = 0; i < jl.length(); i++)
				l.add(jl.getJSONObject(i).getString("title"));
		}
		return l.toArray(new String[0]);
	}

	/**
	 * Gets the direct links to a page (excluding links from redirects). To get links from redirects, use
	 * <tt>getRedirects()</tt> and call this method on each element in the list returned.
	 * 
	 * @param wiki The wiki object to use
	 * @param title The title to use
	 * @return A list of links to this page.
	 */
	public static String[] whatLinksHere(Wiki wiki, String title)
	{
		return getBackLinks(wiki, title, false);
	}

	/**
	 * Gets the redirects of a page.
	 * 
	 * @param wiki The wiki object to use
	 * @param title The title to check.
	 * @return The redirects linking to this page.
	 */
	public static String[] getRedirects(Wiki wiki, String title)
	{
		return getBackLinks(wiki, title, true);
	}

	/**
	 * Gets the text of a page on the specified wiki.
	 * 
	 * @param wiki The wiki to use
	 * @param title The page to get text from.
	 * @return The text of the page, or null if some error occurred.
	 */
	public static String getPageText(Wiki wiki, String title)
	{
		Revision[] rl = getRevisions(wiki, title, 1, false);
		return rl.length >= 1 && rl[0] != null ? rl[0].getText() : null;
	}

	/**
	 * Gets the revisions of a page.
	 * 
	 * @param wiki The wiki object to use.
	 * @param title The title of the page to use.
	 * @param num The number of results to return. Set to -1 to get all the revisions.
	 * @param asc Set to true to list the oldest revisions first.
	 * @return The revisions, as specified.
	 */
	public static Revision[] getRevisions(Wiki wiki, String title, int num, boolean olderfirst)
	{
		Logger.info(wiki, "Fetching revisions of " + title);
		URLBuilder ub = wiki.makeUB();
		ub.setAction("query");
		ub.setParams("prop", "revisions", "rvprop", URLBuilder.chainProps("timestamp", "user", "comment", "content"), "rvdir",
				(olderfirst ? "newer" : "older"), "titles", FString.enc(title));

		ArrayList<Revision> rl = new ArrayList<Revision>();
		for (JSONObject jo : fatQuery(ub, num, "rvlimit", "rvcontinue", false, wiki))
			rl.addAll(Arrays.asList(Revision.makeRevs(jo)));

		return rl.toArray(new Revision[0]);

	}

	/**
	 * Gets a list of the number of elements in a category.
	 * 
	 * @param wiki The wiki object to use
	 * @param cat The title to search. Does not have to include "Category:" prefix.
	 * @param max The maximum number of results to return, or -1 for all results.
	 * @param ns Include these namespaces only. Specify as Strings (e.g. "File", "Category", "Main")
	 * @return The list as specified, or the empty list if something went wrong. Check StackTraces.
	 */
	public static String[] getCategoryMembers(Wiki wiki, String cat, int max, String... ns)
	{
		String title = wiki.whichNS(cat) == 0 ? String.format("%s:%s", wiki.getNS(14), cat) : cat;

		Logger.info(wiki, "Fetching category members of " + title);
		URLBuilder ub = wiki.makeUB();
		ub.setAction("query");
		ub.setParams("list", "categorymembers", "cmtitle", FString.enc(title));

		if (ns.length > 0)
			ub.setParams("cmnamespace", FString.enc(FString.fenceMaker("|", wiki.nsl.prefixToNumStrings(ns))));

		ArrayList<String> l = new ArrayList<String>();
		for (JSONObject jo : fatQuery(ub, max, "cmlimit", "cmcontinue", true, wiki))
		{
			JSONArray jl = JSONParse.getJSONArrayR(jo, "categorymembers");
			for (int i = 0; i < jl.length(); i++)
				l.add(jl.getJSONObject(i).getString("title"));
		}

		return l.toArray(new String[0]);
	}

	/**
	 * Gets the categories a page is categorized in.
	 * 
	 * @param wiki The wiki object to use
	 * @param title The title to get categories of.
	 * @return A list of categories, or the empty list if something went wrong.
	 */
	public static String[] getCategoriesOnPage(Wiki wiki, String title)
	{
		Logger.info(wiki, "Getting categories of " + title);
		URLBuilder ub = wiki.makeUB();
		ub.setAction("query");
		ub.setParams("prop", "categories", "titles", FString.enc(title));

		ArrayList<String> l = new ArrayList<String>();
		for (JSONObject jo : fatQuery(ub, -1, "cllimit", "clcontinue", true, wiki))
		{
			JSONArray jl = JSONParse.getJSONArrayR(jo, "categories");
			for (int i = 0; i < jl.length(); i++)
				l.add(jl.getJSONObject(i).getString("title"));
		}
		return l.toArray(new String[0]);
	}

	/**
	 * Gets a list of links on a page.
	 * 
	 * @param wiki The wiki object to use
	 * @param title The title to fetch links from
	 * @param ns include-only links in these namespace(s).
	 * @return A list of links on the page.
	 */
	public static String[] getLinksOnPage(Wiki wiki, String title, String... ns)
	{
		Logger.info(wiki, "Fetching page links of " + title);
		URLBuilder ub = wiki.makeUB();
		ub.setAction("query");
		ub.setParams("prop", "links", "titles", FString.enc(title));

		if (ns.length > 0)
			ub.setParams("plnamespace", FString.enc(FString.fenceMaker("|", wiki.nsl.prefixToNumStrings(ns))));

		ArrayList<String> l = new ArrayList<String>();
		for (JSONObject jo : fatQuery(ub, -1, "pllimit", "plcontinue", true, wiki))
		{
			JSONArray jl = JSONParse.getJSONArrayR(jo, "links");
			if (jl == null) // if there are 0 links, no JSONArray is returned by server.
				break;

			for (int i = 0; i < jl.length(); i++)
				l.add(jl.getJSONObject(i).getString("title"));
		}
		return l.toArray(new String[0]);
	}

	/**
	 * Gets a list of a user's contributions.
	 * 
	 * @param wiki The wiki object to use.
	 * @param user The user to get contributions for, without the "User:" prefix.
	 * @param max The maximum number of results to return. Specify -1 to get all of the user's contributions -- CAVEAT:
	 *           this could take awhile if the user has many contributions.
	 * @param ns Only include results from these namespaces. Leave blank to get all namespaces.
	 * @return
	 */
	public static Contrib[] getContribs(Wiki wiki, String user, int max, String... ns)
	{
		Logger.info(wiki, "Fetching contribs of " + user);
		URLBuilder ub = wiki.makeUB();
		ub.setAction("query");
		ub.setParams("list", "usercontribs", "ucuser", FString.enc(user));

		if (ns.length > 0)
			ub.setParams("ucnamespace", FString.enc(FString.fenceMaker("|", wiki.nsl.prefixToNumStrings(ns))));

		ArrayList<Contrib> l = new ArrayList<Contrib>();
		for (JSONObject jo : fatQuery(ub, max, "uclimit", "ucstart", true, wiki))
			l.addAll(Arrays.asList(Contrib.makeContribs(jo)));

		return l.toArray(new Contrib[0]);
	}

	/**
	 * Gets the number of elements in a category.
	 * 
	 * @param wiki The wiki object to query.
	 * @param title The category to check, including category prefix.
	 * @return The number of elements in a category
	 */
	public static int getCategorySize(Wiki wiki, String title)
	{
		Logger.info(wiki, "Fetching category size of " + title);
		URLBuilder ub = wiki.makeUB();
		ub.setAction("query");
		ub.setParams("prop", "categoryinfo", "titles", FString.enc(title));

		try
		{
			Reply r = Request.get(ub.makeURL(), wiki.cookiejar);
			if (r.hasError())
				return -1;

			return r.getInt("size");
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * Gets the list of local pages that are displaying the given image.
	 * 
	 * @param wiki The wiki object to use.
	 * @param file The file to check. Must be a valid file name, including the "File:" prefix.
	 * @return The list of pages linking to this file, or the empty array if something went wrong/file doesn't exist.
	 */
	public static String[] imageUsage(Wiki wiki, String file)
	{
		if (wiki.whichNS(file) != wiki.getNS("File"))
		{
			System.err.println("'%s' must be a valid filename and include the 'File:' prefix");
			return new String[0];
		}

		Logger.info(wiki, "Fetching image usage of " + file);
		URLBuilder ub = wiki.makeUB();
		ub.setAction("query");
		ub.setParams("list", "imageusage", "iutitle", FString.enc(file));

		ArrayList<String> l = new ArrayList<String>();
		for (JSONObject jo : fatQuery(ub, -1, "iulimit", "iucontinue", true, wiki))
		{
			JSONArray jl = JSONParse.getJSONArrayR(jo, "imageusage");
			for (int i = 0; i < jl.length(); i++)
				l.add(jl.getJSONObject(i).getString("title"));
		}

		return l.toArray(new String[0]);
	}

	/**
	 * Gets a list of pages transcluding <tt>title</tt>.
	 * 
	 * @param wiki The wiki object to use
	 * @param title The title to get transclusions of.
	 * @return A list of transclusions, or the empty list if something went wrong.
	 */
	public static String[] whatTranscludesHere(Wiki wiki, String title)
	{
		Logger.info(wiki, "Fetching transclusions of " + title);
		URLBuilder ub = wiki.makeUB();
		ub.setAction("query");
		ub.setParams("list", "embeddedin", "eititle", FString.enc(title));

		ArrayList<String> l = new ArrayList<String>();
		for (JSONObject jo : fatQuery(ub, -1, "eilimit", "eicontinue", true, wiki))
		{
			JSONArray jl = JSONParse.getJSONArrayR(jo, "embeddedin");
			for (int i = 0; i < jl.length(); i++)
				l.add(jl.getJSONObject(i).getString("title"));
		}

		return l.toArray(new String[0]);
	}

	/**
	 * Gets the images linked on a page. By this I mean images which are displayed on a page.
	 * 
	 * @param wiki The wiki object to use.
	 * @param title The title to check for images.
	 * @return The list of images on the page, or the empty array if something went wrong.
	 */
	public static String[] getImagesOnPage(Wiki wiki, String title)
	{
		Logger.info(wiki, "Fetching images linked to " + title);
		URLBuilder ub = wiki.makeUB();
		ub.setAction("query");
		ub.setParams("prop", "images", "titles", FString.enc(title));

		ArrayList<String> l = new ArrayList<String>();
		for (JSONObject jo : fatQuery(ub, -1, "imlimit", "imcontinue", true, wiki))
		{
			JSONArray jl = JSONParse.getJSONArrayR(jo, "images");
			if (jl != null) // false if the 'title' is a non-existent page
				for (int i = 0; i < jl.length(); i++)
					l.add(jl.getJSONObject(i).getString("title"));
		}
		return l.toArray(new String[0]);
	}

	/**
	 * Gets a list of pages on the Wiki.
	 * 
	 * @param wiki The wiki object to use
	 * @param prefix Get files starting with this String. DO NOT include a namespace prefix (e.g. "File:"). Param is
	 *           optional, use null or empty string to disable.
	 * @param redirectsonly Set this to true to get redirects only.
	 * @param max The max number of titles to return. Specify -1 to get all pages.
	 * @param ns The namespace identifier (e.g. "File").
	 * @return A list of titles as specified.
	 */
	public static String[] allPages(Wiki wiki, String prefix, boolean redirectsonly, int max, String ns)
	{
		Logger.info(String.format("Grabbing a list of all pages with prefix " + prefix));
		URLBuilder ub = wiki.makeUB("query", "list", "allpages", "apnamespace", "" + wiki.getNS(ns));

		if (redirectsonly)
			ub.setParams("apfilterredir", "redirects");
		if (prefix != null && prefix.length() > 0)
			ub.setParams("apprefix", FString.enc(prefix));

		return multiFatQuery(ub, max, "aplimit", "apcontinue", true, "allpages", "title", wiki);
	}

	/**
	 * Checks to see if a page/pages exist. Returns a set of tuples (in no particular order), in the form
	 * <tt>(String title, Boolean exists)</tt>.
	 * 
	 * @param wiki The wiki object to use.
	 * @param titles The title(s) to check.
	 * @return A List of tuples (String, Boolean) indicating whether a the passed in title(s) exist(s). Returns an empty
	 *         list if something went wrong.
	 */
	public static List<Tuple<String, Boolean>> exists(Wiki wiki, String... titles)
	{
		Logger.info(wiki, "Checking to see if some pages exist");
		URLBuilder ub = wiki.makeUB();
		ub.setAction("query");
		ub.setParams("prop", "pageprops", "ppprop", "missing");

		ArrayList<Tuple<String, Boolean>> l = new ArrayList<Tuple<String, Boolean>>();
		for (JSONObject jo : groupQuery(ub, "pages", wiki, "titles", titles))
		{
			boolean flag = JSONParse.getStringR(jo, "missing") != null ? false : true;
			l.add(new Tuple<String, Boolean>(JSONParse.getStringR(jo, "title"), new Boolean(flag)));
		}

		return l;
	}

	/**
	 * Gets all uploads of a user.
	 * 
	 * @param wiki The wiki object to use.
	 * @param user The username, without the "User:" prefix. <tt>user</tt> must be a valid username.
	 * @return A list this user's uploads.
	 */
	public static String[] getUserUploads(Wiki wiki, String user)
	{
		Logger.info(wiki, "Grabbing uploads of User:" + user);
		return multiFatQuery(wiki.makeUB("query", "list", "allimages", "aisort", "timestamp", "aiuser", FString.enc(user)),
				-1, "ailimit", "aicontinue", true, "allimages", "title", wiki);
	}

	/**
	 * Get some information about a file on Wiki.
	 * 
	 * @param wiki The wiki object to use
	 * @param title The title of the file to use (must be in the file namespace and exist, else return Null)
	 * @param height The height to scale the image to. Disable scalers by passing in a number >= 0.
	 * @param width The width to scale the image to. Disable scalers by passing in a number >= 0.
	 * @return An ImageInfo object, or null if something went wrong.
	 */
	public static ImageInfo getImageInfo(Wiki wiki, String title, int height, int width)
	{
		if (wiki.whichNS(title) != wiki.getNS("File"))
			return null;

		Logger.info(wiki, "Fetching image info for " + title);
		URLBuilder ub = wiki.makeUB("query", "prop", "imageinfo", "iiprop", FString.enc("url|size"), "titles",
				FString.enc(title));

		if (height > 0 && width > 0)
			ub.setParams("iiurlheight", "" + height, "iiurlwidth", "" + width);

		try
		{
			Reply r = Request.get(ub.makeURL(), wiki.cookiejar);

			JSONArray ja; // mw oddly returns the imageinfo in a single element JSONArray
			return (ja = r.getJSONArray("imageinfo")) == null ? null : new ImageInfo(ja.getJSONObject(0));
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Gets the templates transcluded on a page.
	 * 
	 * @param wiki The wiki object to use
	 * @param title The title to get templates from.
	 * @return A list of templates on the page.
	 */
	public static String[] getTemplatesOnPage(Wiki wiki, String title)
	{
		Logger.info(wiki, "Fetching transcluded templates on " + title);
		return multiFatQuery(wiki.makeUB("query", "prop", "templates", "titles", FString.enc(title)), -1, "tllimit",
				"tlcontinue", true, "templates", "title", wiki);
	}

	/**
	 * Gets the global file usage for a media file.
	 * 
	 * @param wiki The wiki object to use
	 * @param title The title to check. Must start with "File:" prefix.
	 * @return A list of tuples, (title of page, short form of wiki this page is from), denoting the global usage of this
	 *         file. Returns empty list if something went wrong.
	 */
	public static ArrayList<Tuple<String, String>> globalUsage(Wiki wiki, String title)
	{
		if (wiki.whichNS(title) != wiki.getNS("File"))
			return null;

		Logger.info(wiki, "Fetching global usage of " + title);
		URLBuilder ub = wiki.makeUB("query", "prop", "globalusage", "guprop", "namespace", "titles", FString.enc(title));

		ArrayList<Tuple<String, String>> l = new ArrayList<Tuple<String, String>>();
		for (JSONObject jo : fatQuery(ub, -1, "gulimit", "gucontinue", true, wiki))
		{
			JSONArray ja = JSONParse.getJSONArrayR(jo, "globalusage");
			for (int i = 0; i < ja.length(); i++)
			{
				JSONObject curr = ja.getJSONObject(i);
				l.add(new Tuple<String, String>(curr.getString("title"), curr.getString("wiki")));
			}
		}
		return l;
	}

	/**
	 * Gets the list of groups a user is in.
	 * 
	 * @param wiki The wiki object to use. You must be logged in to use this functionality.
	 * @return A list of user groups, or the empty list if something went wrong.
	 */
	public static ArrayList<String> listGroupsRights(Wiki wiki)
	{
		Logger.info(wiki, "Getting our user groups list");
		URLBuilder ub = wiki.makeUB("query", "list", "users", "usprop", "groups", "ususers", FString.enc(wiki.upx.x));

		ArrayList<String> l = new ArrayList<String>();
		try
		{
			Reply r = Request.get(ub.makeURL(), wiki.cookiejar);
			if (r.hasError())
				return l;

			JSONArray jl = r.getJSONArray("users").getJSONObject(0).getJSONArray("groups");
			for (int i = 0; i < jl.length(); i++)
				l.add(jl.getString(i));
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}

		return l;
	}

	/**
	 * Gets a list of pages returned by a special page.
	 * 
	 * @param wiki The wiki object to use
	 * @param page The special page to query
	 * @param max The maximum number of pages to return. Specify -1 to disable.
	 * @return The list of pages as requested.
	 */
	protected static String[] listSpecialPages(Wiki wiki, String page, int max)
	{
		Logger.info(wiki, String.format("Getting a list of %d pages from %s", max, page));
		return multiFatQuery(wiki.makeUB("query", "list", "querypage", "qppage", FString.enc(page)), max, "qplimit",
				"qpoffset", false, "results", "title", wiki);
	}

	/**
	 * Gets a list of duplicate files. Tuple returned is as follows <tt>(String, Boolean)</tt>, where the string is the
	 * title of the duplicate, and where the boolean indicates whether the file is part of the local repository (enwp is
	 * local repository and commons is the shared repository). In other words, this param will be TRUE if a dupe was
	 * found in the shared repo. If you're using a wiki that is not associated with another wiki, ignore this param.
	 * 
	 * @param wiki The wiki object to use
	 * @param file The file to get duplicates of
	 * @return The list of files.
	 */
	public static ArrayList<Tuple<String, Boolean>> getDuplicatesOf(Wiki wiki, String file)
	{
		Logger.info(wiki, "Getting dupes of " + file);
		String head = wiki.getNS(6); //MediaWiki is stupid and doesn't return File prefixes. 


		ArrayList<Tuple<String, Boolean>> l = new ArrayList<Tuple<String, Boolean>>();
		for (JSONObject jo : fatQuery(wiki.makeUB("query", "prop", "duplicatefiles", "titles", FString.enc(file)), -1,
				"dflimit", "dfcontinue", true, wiki))
		{
			JSONArray ja = JSONParse.getJSONArrayR(jo, "duplicatefiles");
			if(ja == null) // In the event there are no duplicates
				continue;
			
			for (int i = 0; i < ja.length(); i++)
			{
				JSONObject jx = ja.getJSONObject(i);
				l.add(new Tuple<String, Boolean>(head + ":" + jx.getString("name"), new Boolean(jx.has("shared"))));
			}
		}
		return l;
	}
}