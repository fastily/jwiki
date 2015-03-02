package jwiki.core;

import java.util.ArrayList;

import jwiki.dwrap.ImageInfo;
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
public class MQuery
{
	/**
	 * Constructors disallowed
	 */
	private MQuery()
	{

	}

	/**
	 * Gets the list of usergroups (rights) users belong to. Sample groups: sysop, user, autoconfirmed, editor.
	 * 
	 * @param wiki The wiki object to use.
	 * @param users The list of users to get rights information for. Do not include "User:" prefix.
	 * @return The list of results keyed by username.
	 */
	public static ArrayList<Tuple<String, ArrayList<String>>> listUserRights(Wiki wiki, ArrayList<String> users)
	{
		return QueryTools.groupQueryForLists(wiki, wiki.makeUB("query", "list", "users", "usprop", "groups"), "users", "name",
				"groups", "ususers", users);
	}

	/**
	 * Gets imageinfo for files.
	 * 
	 * @param wiki The wiki objec to use
	 * @param width Generate a thumbnail of the file with this width. Optional param, set to -1 to disable
	 * @param height Generate a thumbnail of the file with this height. Optional param, set to -1 to disable
	 * @param titles The titles to query
	 * @return A list of ImageInfo, keyed by title.
	 */
	public static ArrayList<Tuple<String, ImageInfo>> getImageInfo(Wiki wiki, int width, int height, ArrayList<String> titles)
	{
		URLBuilder ub = wiki.makeUB("query", "prop", "imageinfo", "iiprop",
				URLBuilder.chainProps("canonicaltitle", "url", "size"));

		if (width > 0)
			ub.setParams("iiurlwidth", "" + width);
		if (height > 0)
			ub.setParams("iiurlheight", "" + height);

		ArrayList<Tuple<String, ImageInfo>> l = new ArrayList<>();
		for (Reply r : QueryTools.doGroupQuery(wiki, ub, "titles", titles))
			for (Reply x : r.bigJSONObjectGet("pages"))
				l.add(new Tuple<>(x.getString("title"), new ImageInfo(x)));

		return l;
	}

	/**
	 * Gets the list of categories on a page.
	 * 
	 * @param wiki The wiki object to use
	 * @param titles The titles to query.
	 * @return A list of results keyed by title.
	 */
	public static ArrayList<Tuple<String, ArrayList<String>>> getCategoriesOnPage(Wiki wiki, ArrayList<String> titles)
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
	public static ArrayList<Tuple<String, Integer>> getCategorySize(Wiki wiki, ArrayList<String> titles)
	{
		ArrayList<Tuple<String, Integer>> l = new ArrayList<>();
		for (Reply r : QueryTools.doGroupQuery(wiki, wiki.makeUB("query", "prop", "categoryinfo"), "titles", titles))
			for (Reply r1 : r.bigJSONObjectGet("pages"))
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
	public static ArrayList<Tuple<String, ArrayList<String>>> getPageText(Wiki wiki, ArrayList<String> titles)
	{
		return QueryTools.multiQueryForStrings(wiki, wiki.makeUB("query", "prop", "revisions", "rvprop", "content"), null,
				"revisions", "*", "title", "titles", titles);
	}

	/**
	 * Get wiki links on a page.
	 * 
	 * @param wiki The wiki object to use
	 * @param ns Namespaces to include-only. Optional, set to null to disable.
	 * @param titles The titles to query.
	 * @return A list of results keyed by title.
	 */
	public static ArrayList<Tuple<String, ArrayList<String>>> getLinksOnPage(Wiki wiki, NS[] ns, ArrayList<String> titles)
	{
		URLBuilder ub = wiki.makeUB("query", "prop", "links");
		if (ns != null && ns.length > 0)
			ub.setParams("plnamespace", wiki.nsl.createFilter(true, ns));
		return QueryTools.multiQueryForStrings(wiki, ub, "pllimit", "links", "title", "title", "titles", titles);
	}

	/**
	 * Get pages redirecting to or linking to a page.
	 * 
	 * @param wiki The wiki object to use
	 * @param redirects Set to true to search for redirects. False searches for non-redirects.
	 * @param titles The titles to query
	 * @return A list of results keyed by title.
	 */
	public static ArrayList<Tuple<String, ArrayList<String>>> linksHere(Wiki wiki, boolean redirects, ArrayList<String> titles)
	{
		return QueryTools.multiQueryForStrings(wiki,
				wiki.makeUB("query", "prop", "linkshere", "lhprop", "title", "lhshow", redirects ? "redirect" : "!redirect"),
				"lhlimit", "linkshere", "title", "title", "titles", titles);
	}

	/**
	 * Gets a list of pages transcluding a template.
	 * 
	 * @param wiki The wiki object to use
	 * @param titles The titles to query
	 * @return A list of results keyed by title.
	 */
	public static ArrayList<Tuple<String, ArrayList<String>>> transcludesIn(Wiki wiki, ArrayList<String> titles)
	{
		return QueryTools.multiQueryForStrings(wiki, wiki.makeUB("query", "prop", "transcludedin", "tiprop", "title"),
				"tilimit", "transcludedin", "title", "title", "titles", titles);
	}

	/**
	 * Gets a list of pages linking to a file.
	 * 
	 * @param wiki The wiki object to use
	 * @param titles The titles to query. PRECONDITION: These must be valid file names prefixed with the "File:" prefix,
	 *           or you will get strange results.
	 * @return A list of results keyed by title.
	 */
	public static ArrayList<Tuple<String, ArrayList<String>>> fileUsage(Wiki wiki, ArrayList<String> titles)
	{
		return QueryTools.multiQueryForStrings(wiki, wiki.makeUB("query", "prop", "fileusage"), "fulimit", "fileusage",
				"title", "title", "titles", titles);
	}

	/**
	 * Checks if a title exists.
	 * 
	 * @param wiki The wiki object to use
	 * @param titles The titles to query
	 * @return A list of results keyed by title. True = exists.
	 */
	public static ArrayList<Tuple<String, Boolean>> exists(Wiki wiki, ArrayList<String> titles)
	{
		ArrayList<Tuple<String, Boolean>> l = new ArrayList<>();
		for (Reply r : QueryTools.doGroupQuery(wiki, wiki.makeUB("query", "prop", "pageprops", "ppprop", "missing"), "titles",
				titles))
			for (Reply r1 : r.bigJSONObjectGet("pages"))
				l.add(new Tuple<String, Boolean>(r1.getString("title"), new Boolean(!r1.has("missing"))));

		return l;
	}

	/**
	 * Checks if a title exists. Can filter results based on whether pages exist.
	 * 
	 * @param wiki The wiki object to use
	 * @param exists Set to true to select all pages that exist. False selects all that don't exist
	 * @param titles The titles to query
	 * @return A list of titles that exist or don't exist.
	 */
	public static ArrayList<String> exists(Wiki wiki, boolean exists, ArrayList<String> titles)
	{
		ArrayList<String> l = new ArrayList<>();
		for (Tuple<String, Boolean> t : exists(wiki, titles))
			if (t.y.booleanValue() == exists)
				l.add(t.x);

		return l;
	}

	/**
	 * Gets titles of images linked on a page.
	 * 
	 * @param wiki The wiki object to use
	 * @param titles The titles to query
	 * @return A list of results keyed by title.
	 */
	public static ArrayList<Tuple<String, ArrayList<String>>> getImagesOnPage(Wiki wiki, ArrayList<String> titles)
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
	public static ArrayList<Tuple<String, ArrayList<String>>> getTemplatesOnPage(Wiki wiki, ArrayList<String> titles)
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
	public static ArrayList<Tuple<String, ArrayList<Tuple<String, String>>>> globalUsage(Wiki wiki, ArrayList<String> titles)
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
	public static ArrayList<Tuple<String, ArrayList<String>>> getDuplicatesOf(Wiki wiki, boolean localOnly,
			ArrayList<String> titles)
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
	 * @param pname The name of the special page (e.g. ListDuplicatedFiles). This is case-sensitive.
	 * @return Results of the query.
	 */
	protected static ArrayList<String> querySpecialPage(Wiki wiki, int cap, String pname)
	{
		return QueryTools.limitedQueryForStrings(wiki, wiki.makeUB("query", "list", "querypage", "qppage", pname), "qplimit",
				cap, "results", "title", null, null);
	}
}