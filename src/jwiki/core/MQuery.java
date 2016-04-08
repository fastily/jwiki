package jwiki.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jwiki.dwrap.ImageInfo;
import jwiki.util.FL;
import jwiki.util.FString;
import jwiki.util.JSONP;
import jwiki.util.MapList;
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
public final class MQuery
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
	public static HashMap<String, ArrayList<String>> listUserRights(Wiki wiki, ArrayList<String> users)
	{
		return SQ.with(wiki, FL.pMap("list", "users", "usprop", "groups")).multiTitleQuery("ususers", users).getJAofJOasMapWith("users",
				e -> e.getString("name"), e -> JSONP.strsFromJA(e.getJSONArray("groups")));
	}

	/**
	 * Gets ImageInfo objects for each revision of a File.
	 * 
	 * @param wiki The Wiki object to use
	 * @param width Generate a thumbnail of the file with this width. Optional param, set to -1 to disable
	 * @param height Generate a thumbnail of the file with this height. Optional param, set to -1 to disable
	 * @param titles The titles to query
	 * @return A map with titles keyed to respective lists of ImageInfo.
	 */
	public static HashMap<String, ArrayList<ImageInfo>> getImageInfo(Wiki wiki, int width, int height, ArrayList<String> titles)
	{
		HashMap<String, String> pl = FL.pMap("prop", "imageinfo", "iiprop",
				FString.pipeFence("canonicaltitle", "url", "size", "sha1", "mime", "user", "timestamp", "comment"));

		if (width > 0)
			pl.put("iiurlwidth", "" + width);
		if (height > 0)
			pl.put("iiurlheight", "" + height);

		String t;
		MapList<String, ImageInfo> ml = new MapList<>();
		for (Reply r : SQ.with(wiki, "iilimit", pl).multiTitleQuery("titles", titles).getJOofJO("pages"))
			ml.put(t = r.getStringR("title"), ImageInfo.makeImageInfos(t, r.getJAOfJO("imageinfo")));

		for (Map.Entry<String, ArrayList<ImageInfo>> e : ml.l.entrySet()) // dirty hack because MediaWiki ImageInfo is
																								// terrible
			Collections.sort(e.getValue());

		return ml.l;
	}

	/**
	 * Gets the list of categories on a page.
	 * 
	 * @param wiki The wiki object to use
	 * @param titles The titles to query.
	 * @return A list of results keyed by title.
	 */
	public static HashMap<String, ArrayList<String>> getCategoriesOnPage(Wiki wiki, ArrayList<String> titles)
	{
		return SQ.with(wiki, "cllimit", FL.pMap("prop", "categories")).multiTitleQuery("titles", titles).groupJOListByStrAndJA("pages",
				"title", "categories", "title");
	}

	/**
	 * Gets the number of elements contained in a category.
	 * 
	 * @param wiki The wiki object to use
	 * @param titles The titles to query. PRECONDITION: Titles *must* begin with the "Category:" prefix
	 * @return A list of results keyed by title. Value returned will be -1 if Category entered was empty <b>and</b>
	 *         non-existent.
	 */
	public static HashMap<String, Integer> getCategorySize(Wiki wiki, ArrayList<String> titles)
	{
		return SQ.with(wiki, FL.pMap("prop", "categoryinfo")).multiTitleQuery("titles", titles).getJOofJOasMapWith("pages",
				r -> r.getString("title"), r -> r.has("categoryinfo") ? r.getJSONObject("categoryinfo").getInt("size") : -1);
	}

	/**
	 * Gets the text of a page.
	 * 
	 * @param wiki The wiki to use
	 * @param titles The titles to query
	 * @return A list of results keyed by title.
	 */
	public static HashMap<String, String> getPageText(Wiki wiki, ArrayList<String> titles)
	{
		return SQ.with(wiki, FL.pMap("prop", "revisions", "rvprop", "content")).multiTitleQuery("titles", titles).getJOofJOasMapWith(
				"pages", r -> r.getString("title"),
				r -> r.has("revisions") ? r.getJSONArray("revisions").getJSONObject(0).getString("*") : "");
	}

	/**
	 * Get wiki links on a page.
	 * 
	 * @param wiki The wiki object to use
	 * @param ns Namespaces to include-only. Optional, set to null to disable.
	 * @param titles The titles to query.
	 * @return A list of results keyed by title.
	 */
	public static HashMap<String, ArrayList<String>> getLinksOnPage(Wiki wiki, NS[] ns, ArrayList<String> titles)
	{
		HashMap<String, String> pl = FL.pMap("prop", "links");
		if (ns != null && ns.length > 0)
			pl.put("plnamespace", wiki.nsl.createFilter(ns));

		return SQ.with(wiki, "pllimit", pl).multiTitleQuery("titles", titles).groupJOListByStrAndJA("pages", "title", "links", "title");
	}

	/**
	 * Get pages redirecting to or linking to a page.
	 * 
	 * @param wiki The wiki object to use
	 * @param redirects Set to true to search for redirects. False searches for non-redirects.
	 * @param titles The titles to query
	 * @return A list of results keyed by title.
	 */
	public static HashMap<String, ArrayList<String>> linksHere(Wiki wiki, boolean redirects, ArrayList<String> titles)
	{
		return SQ.with(wiki, "lhlimit", FL.pMap("prop", "linkshere", "lhprop", "title", "lhshow", redirects ? "redirect" : "!redirect"))
				.multiTitleQuery("titles", titles).groupJOListByStrAndJA("pages", "title", "linkshere", "title");
	}

	/**
	 * Gets a list of pages transcluding a template.
	 * 
	 * @param wiki The wiki object to use
	 * @param titles The titles to query
	 * @return A list of results keyed by title.
	 */
	public static HashMap<String, ArrayList<String>> transcludesIn(Wiki wiki, ArrayList<String> titles)
	{
		return SQ.with(wiki, "tilimit", FL.pMap("prop", "transcludedin", "tiprop", "title")).multiTitleQuery("titles", titles)
				.groupJOListByStrAndJA("pages", "title", "transcludedin", "title");
	}

	/**
	 * Gets a list of pages linking (displaying/thumbnailing) a file.
	 * 
	 * @param wiki The wiki to use
	 * @param titles The titles to query. PRECONDITION: These must be valid file names prefixed with the "File:" prefix.
	 * @return A Map of results keyed by title.
	 */
	public static HashMap<String, ArrayList<String>> fileUsage(Wiki wiki, ArrayList<String> titles)
	{
		return SQ.with(wiki, "fulimit", FL.pMap("prop", "fileusage")).multiTitleQuery("titles", titles).groupJOListByStrAndJA("pages",
				"title", "fileusage", "title");
	}

	/**
	 * Checks if list of titles exists.
	 * 
	 * @param wiki The wiki object to use
	 * @param titles The titles to query
	 * @return Results keyed by title. True -&gt; exists.
	 */
	public static HashMap<String, Boolean> exists(Wiki wiki, ArrayList<String> titles)
	{
		return SQ.with(wiki, FL.pMap("prop", "pageprops", "ppprop", "missing")).multiTitleQuery("titles", titles)
				.getJOofJOasMapWith("pages", r -> r.getString("title"), r -> !r.has("missing"));
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
		return FL.toAL(exists(wiki, titles).entrySet().stream().filter(t -> t.getValue() == exists).map(Map.Entry::getKey));
	}

	/**
	 * Gets titles of images linked on a page.
	 * 
	 * @param wiki The wiki object to use
	 * @param titles The titles to query
	 * @return A list of results keyed by title.
	 */
	public static HashMap<String, ArrayList<String>> getImagesOnPage(Wiki wiki, ArrayList<String> titles)
	{
		return SQ.with(wiki, "imlimit", FL.pMap("prop", "images")).multiTitleQuery("titles", titles).groupJOListByStrAndJA("pages",
				"title", "images", "title");
	}

	/**
	 * Get templates transcluded on a page.
	 * 
	 * @param wiki The wiki object to use
	 * @param titles The titles to query
	 * @return A list of results keyed by title.
	 */
	public static HashMap<String, ArrayList<String>> getTemplatesOnPage(Wiki wiki, ArrayList<String> titles)
	{
		return SQ.with(wiki, "tllimit", FL.pMap("prop", "templates")).multiTitleQuery("titles", titles).groupJOListByStrAndJA("pages",
				"title", "templates", "title");
	}

	/**
	 * Gets the global usage of a file.
	 * 
	 * @param wiki The wiki object to use
	 * @param titles The titles to query
	 * @return A list of results keyed by title. The inner tuple is of the form (title, shorthand url notation).
	 */
	public static HashMap<String, ArrayList<Tuple<String, String>>> globalUsage(Wiki wiki, ArrayList<String> titles)
	{
		return SQ.with(wiki, "gulimit", FL.pMap("prop", "globalusage")).multiTitleQuery("titles", titles)
				.groupJOListByStrAndJAPair("pages", "title", "globalusage", "title", "wiki");
	}

	/**
	 * Resolves title redirects on a Wiki.
	 * 
	 * @param wiki The Wiki to run the query against
	 * @param titles The titles to attempt resoloving.
	 * @return A HashMap where each key is the original title, and the value is the resolved title.
	 */
	public static HashMap<String, String> resolveRedirects(Wiki wiki, ArrayList<String> titles)
	{
		HashMap<String, String> hl = new HashMap<>();
		RSet rs = SQ.with(wiki, null, FL.pMap("redirects", "")).multiTitleQuery("titles", titles);

		for (Reply r : rs.getJAofJO("redirects")) // add titles which are redirects with resolved title
			hl.put(r.getStringR("from"), r.getStringR("to"));

		for (String s : titles) // add titles that are not redirects
			if (!hl.containsKey(s))
				hl.put(s, s);

		return hl;
	}

	/**
	 * Gets duplicates of a file. Note that results are returned *without* a namespace prefix.
	 * 
	 * @param wiki The wiki object to use
	 * @param localOnly Set to true if you only want to look for files in the local repository.
	 * @param titles The titles to query
	 * @return A list of results keyed by title.
	 */
	public static HashMap<String, ArrayList<String>> getDuplicatesOf(Wiki wiki, boolean localOnly, ArrayList<String> titles)
	{
		HashMap<String, String> pl = FL.pMap("prop", "duplicatefiles");
		if (localOnly)
			pl.put("dflocalonly", "");

		return SQ.with(wiki, "dflimit", pl).multiTitleQuery("titles", titles).groupJOListByStrAndJA("pages", "title", "duplicatefiles",
				"name");
	}

	/**
	 * Gets shared (non-local) duplicates of a file. PRECONDITION: The Wiki this query is run against has the
	 * <a href="https://www.mediawiki.org/wiki/Extension:GlobalUsage">GlobalUsage</a> extension installed. Note that
	 * results are returned *without* a namespace prefix.
	 * 
	 * @param wiki The wiki object to use
	 * @param titles The titles to query
	 * @return A list of results keyed by title.
	 */
	public static HashMap<String, ArrayList<String>> getSharedDuplicatesOf(Wiki wiki, ArrayList<String> titles)
	{
		HashMap<String, ArrayList<String>> hl = new HashMap<>();
		for (Reply r : SQ.with(wiki, "dflimit", FL.pMap("prop", "duplicatefiles")).multiTitleQuery("titles", titles).getJOofJO("pages"))
		{
			String title = r.getString("title");

			if (!hl.containsKey(title))
				hl.put(title, new ArrayList<>());
			if (r.has("duplicatefiles"))
				hl.get(title)
						.addAll(FL.toAL(r.getJAOfJO("duplicatefiles").stream().filter(e -> e.has("shared")).map(e -> wiki.convertIfNotInNS(e.getString("name").replace("_", " "), NS.FILE))));
		}

		return hl;
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
		return SQ.with(wiki, "qplimit", cap, FL.pMap("list", "querypage", "qppage", pname)).multiQuery().getJAOfJOasStr("results",
				"title");
	}
}