package jwiki.core;

import java.util.ArrayList;

import jwiki.util.FString;
import jwiki.util.Tuple;

/**
 * Perform multi-title queries. Use of these methods is intended for <i>advanced</i> users who wish to make queries to
 * the server over a large data set. These methods are optimized for performance, and will consolidate titles into
 * single queries to fetch the most data possible per query. If you're looking to make simple, single-item queries,
 * (which is suitable for most users) please use the methods in Wiki.java.
 * 
 * @author Fastily
 * @see Wiki
 */
public class MassClientQuery
{
	/**
	 * Constructors disallowed
	 */
	private MassClientQuery()
	{

	}

	/**
	 * Gets the list of usergroups (rights) users belong to. Sample groups: sysop, user, autoconfirmed, editor.
	 * 
	 * @param wiki The wiki object to use.
	 * @param users The list of users to get rights information for. Do not include "User:" prefix.
	 * @return The list of results keyed by username.
	 */
	public static ArrayList<Tuple<String, ArrayList<String>>> listUserRights(Wiki wiki, String... users)
	{
		return QueryTools.groupQueryForLists(wiki, wiki.makeUB("query", "list", "users", "usprop", "groups"), "users", "name",
				"groups", "ususers", users);
	}

	/**
	 * Gets the list of categories on a page.
	 * 
	 * @param wiki The wiki object to use
	 * @param titles The titles to query.
	 * @return A list of results keyed by title.
	 */
	public static ArrayList<Tuple<String, ArrayList<String>>> getCategoriesOnPage(Wiki wiki, String... titles)
	{
		return QueryTools.multiQueryForStrings(wiki, wiki.makeUB("query", "prop", "categories"), "cllimit", "categories",
				"title", "title", "titles", titles);
	}

	/**
	 * Gets the number of elements contained in a category.
	 * 
	 * @param wiki The wiki object to use
	 * @param titles The titles to query. PRECONDITION: Titles *must* begin with the "Category:" prefix
	 * @return A list of results keyed by title. Value returned will be -1 if Category entered was empty <b>and</b>
	 *         non-existent.
	 */
	public static ArrayList<Tuple<String, Integer>> getCategorySize(Wiki wiki, String... titles)
	{
		ArrayList<Tuple<String, Integer>> l = new ArrayList<>();
		for (ServerReply r : QueryTools.doGroupQuery(wiki, wiki.makeUB("query", "prop", "categoryinfo"), "titles", titles))
			for (ServerReply r1 : r.bigJSONObjectGet("pages"))
				l.add(new Tuple<String, Integer>(r1.getString("title"), new Integer(r1.getIntR("size"))));

		return l;
	}

	/**
	 * Gets the text of a page.
	 * 
	 * @param wiki The wiki to use
	 * @param titles The titles to query
	 * @return A list of results keyed by title.
	 */
	public static ArrayList<Tuple<String, ArrayList<String>>> getPageText(Wiki wiki, String... titles)
	{
		return QueryTools.multiQueryForStrings(wiki, wiki.makeUB("query", "prop", "revisions", "rvprop", "content"), null,
				"revisions", "*", "title", "titles", titles);
	}

	/**
	 * Get wiki links on a page.
	 * 
	 * @param wiki The wiki object to use
	 * @param ns Namespaces to include-only, passed in as prefixes, without the ":" (e.g. "File", "Category", "Main").
	 *           Optional, set to null to disable.
	 * @param titles The titles to query.
	 * @return A list of results keyed by title.
	 */
	public static ArrayList<Tuple<String, ArrayList<String>>> getLinksOnPage(Wiki wiki, String[] ns, String... titles)
	{
		URLBuilder ub = wiki.makeUB("query", "prop", "links");
		if (ns != null && ns.length > 0)
			ub.setParams("plnamespace", FString.enc(FString.fenceMaker("|", wiki.nsl.prefixToNumStrings(ns))));
		return QueryTools.multiQueryForStrings(wiki, ub, "pllimit", "links", "title", "title", "titles", titles);
	}

	/**
	 * Checks if a title exists.
	 * 
	 * @param wiki The wiki object to use
	 * @param titles The titles to query
	 * @return A list of results keyed by title. True = exists.
	 */
	public static ArrayList<Tuple<String, Boolean>> exists(Wiki wiki, String... titles)
	{
		ArrayList<Tuple<String, Boolean>> l = new ArrayList<>();
		for (ServerReply r : QueryTools.doGroupQuery(wiki, wiki.makeUB("query", "prop", "pageprops", "ppprop", "missing"),
				"titles", titles))
			for (ServerReply r1 : r.bigJSONObjectGet("pages"))
				l.add(new Tuple<String, Boolean>(r1.getString("title"), new Boolean(!r1.has("missing"))));

		return l;
	}

	/**
	 * Gets titles of images linked on a page.
	 * 
	 * @param wiki The wiki object to use
	 * @param titles The titles to query
	 * @return A list of results keyed by title.
	 */
	public static ArrayList<Tuple<String, ArrayList<String>>> getImagesOnPage(Wiki wiki, String... titles)
	{
		return QueryTools.multiQueryForStrings(wiki, wiki.makeUB("query", "prop", "images"), "imlimit", "images", "title",
				"title", "titles", titles);
	}

	/**
	 * Gets templates transcluded on a page.
	 * 
	 * @param wiki The wiki object to use
	 * @param titles The titles to query
	 * @return A list of results keyed by title.
	 */
	public static ArrayList<Tuple<String, ArrayList<String>>> getTemplatesOnPage(Wiki wiki, String... titles)
	{
		return QueryTools.multiQueryForStrings(wiki, wiki.makeUB("query", "prop", "templates"), "tllimit", "templates",
				"title", "title", "titles", titles);
	}

	/**
	 * Gets the global usage of a file.
	 * 
	 * @param wiki The wiki object to use
	 * @param titles The titles to query
	 * @return A list of results keyed by title. The inner tuple is of the form (title, shorthand url notation).
	 */
	public static ArrayList<Tuple<String, ArrayList<Tuple<String, String>>>> globalUsage(Wiki wiki, String... titles)
	{
		return QueryTools.multiQueryForTuples(wiki, wiki.makeUB("query", "prop", "globalusage"), "gulimit", "globalusage",
				"title", "wiki", "title", "titles", titles);
	}

	/**
	 * Gets duplicates of a file. Note that results are returned *without* a namespace prefix.
	 * 
	 * @param wiki The wiki object to use
	 * @param localOnly Set to true if you only want to look for files in the local repository.
	 * @param titles The titles to query
	 * @return A list of results keyed by title.
	 */
	public static ArrayList<Tuple<String, ArrayList<String>>> getDuplicatesOf(Wiki wiki, boolean localOnly, String... titles)
	{
		URLBuilder ub = wiki.makeUB("query", "prop", "duplicatefiles");
		if (localOnly)
			ub.setParams("dflocalonly", "");

		return QueryTools.multiQueryForStrings(wiki, ub, "dflimit", "duplicatefiles", "name", "title", "titles", titles);
	}

	/**
	 * Queries a special page.
	 * 
	 * @param wiki The wiki object to use
	 * @param cap The maximum number of results to return
	 * @param pname The name of the special page (e.g. ListDuplicatedFiles).  This is case-sensitive.
	 * @return Results of the query.
	 */
	protected static ArrayList<String> querySpecialPage(Wiki wiki, int cap, String pname)
	{
		return QueryTools.limitedQueryForStrings(wiki, wiki.makeUB("query", "list", "querypage", "qppage", pname), "qplimit",
				cap, "results", "title", null, null);
	}
}