package jwiki.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.json.JSONArray;

import jwiki.dwrap.ImageInfo;
import jwiki.util.FL;
import jwiki.util.FString;
import jwiki.util.JSONParse;
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
	public static ArrayList<Tuple<String, ArrayList<String>>> listUserRights(Wiki wiki, ArrayList<String> users)
	{
		HashMap<String, String> pl = FL.pMap("list", "users", "usprop", "groups");

		RSet rs = new SQ(wiki, pl).multiTitleQuery("ususers", users);
		return FL.toAL(rs.getJOofJAStream("users")
				.map(e -> new Tuple<>(e.getString("name"), JSONParse.jsonArrayToString(e.getJSONArray("groups")))));
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
	public static HashMap<String, ImageInfo> getImageInfo(Wiki wiki, int width, int height, ArrayList<String> titles)
	{
		HashMap<String, String> pl = FL.pMap("prop", "imageinfo", "iiprop",
				FString.pipeFence("canonicaltitle", "url", "size"));

		if (width > 0)
			pl.put("iiurlwidth", "" + width);
		if (height > 0)
			pl.put("iiurlheight", "" + height);

		return new SQ(wiki, pl).multiTitleQuery("titles", titles).getJOofJOStream("pages").collect(HashMap::new,
				(m, v) -> m.put(v.getString("title"), new ImageInfo(v)), HashMap::putAll);
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
		RSet rs = new SQ(wiki, "cllimit", FL.pMap("prop", "categories")).multiTitleQuery("titles", titles);
		return JSONParse.groupJOListByStrAndJA(rs.getJOofJOStream("pages"), "title", "categories", "title");
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
		RSet rs = new SQ(wiki, FL.pMap("prop", "categoryinfo")).multiTitleQuery("titles", titles);
		return rs.getJOofJOStream("pages").collect(HashMap::new, (m, v) -> m.put(v.getString("title"),
				v.has("categoryinfo") ? v.getJSONObject("categoryinfo").getInt("size") : -1), HashMap::putAll);
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
		RSet rs = new SQ(wiki, FL.pMap("prop", "revisions", "rvprop", "content")).multiTitleQuery("titles",
				titles);
		return rs.getJOofJOStream("pages")
				.collect(HashMap::new,
						(m, v) -> m.put(v.getString("title"),
								v.has("revisions") ? v.getJSONArray("revisions").getJSONObject(0).getString("*") : ""),
				HashMap::putAll);
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

		RSet rs = new SQ(wiki, "pllimit", pl).multiTitleQuery("titles", titles);
		return JSONParse.groupJOListByStrAndJA(rs.getJOofJOStream("pages"), "title", "links", "title");
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
		RSet rs = new SQ(wiki, "lhlimit",
				FL.pMap("prop", "linkshere", "lhprop", "title", "lhshow", redirects ? "redirect" : "!redirect"))
						.multiTitleQuery("titles", titles);
		return JSONParse.groupJOListByStrAndJA(rs.getJOofJOStream("pages"), "title", "linkshere", "title");
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
		RSet rs = new SQ(wiki, "tilimit", FL.pMap("prop", "transcludedin", "tiprop", "title"))
				.multiTitleQuery("titles", titles);
		return JSONParse.groupJOListByStrAndJA(rs.getJOofJOStream("pages"), "title", "transcludedin", "title");
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
		RSet rs = new SQ(wiki, "fulimit", FL.pMap("prop", "fileusage")).multiTitleQuery("titles", titles);
		return JSONParse.groupJOListByStrAndJA(rs.getJOofJOStream("pages"), "title", "fileusage", "title");
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
		return new SQ(wiki, FL.pMap("prop", "pageprops", "ppprop", "missing")).multiTitleQuery("titles", titles)
				.getJOofJOStream("pages")
				.collect(HashMap::new, (m, v) -> m.put(v.getString("title"), !v.has("missing")), HashMap::putAll);
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
		RSet rs = new SQ(wiki, "imlimit", FL.pMap("prop", "images")).multiTitleQuery("titles", titles);
		return JSONParse.groupJOListByStrAndJA(rs.getJOofJOStream("pages"), "title", "images", "title");
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
		RSet rs = new SQ(wiki, "tllimit", FL.pMap("prop", "templates")).multiTitleQuery("titles", titles);
		return JSONParse.groupJOListByStrAndJA(rs.getJOofJOStream("pages"), "title", "templates", "title");
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
		RSet rs = new SQ(wiki, "gulimit", FL.pMap("prop", "globalusage")).multiTitleQuery("titles", titles);

		HashMap<String, ArrayList<Tuple<String, String>>> hlx = new HashMap<>();
		for (Reply r : rs.getJOofJO("pages"))
		{
			JSONArray ja = r.has("globalusage") ? r.getJSONArray("globalusage") : new JSONArray();
			FL.mapListMerge(hlx, r.getString("title"), JSONParse.strTuplesFromJAofJO(ja, "title", "wiki"));
		}

		return FL.mapToList(hlx);
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

		Stream<Reply> srl = new SQ(wiki, "dflimit", pl).multiTitleQuery("titles", titles).getJOofJOStream("pages");
		return JSONParse.groupJOListByStrAndJA(srl, "title", "duplicatefiles", "name");
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
		return new SQ(wiki, "qplimit", cap, FL.pMap("list", "querypage", "qppage", pname)).multiQuery()
				.stringFromJAOfJO("results", "title");
	}
}