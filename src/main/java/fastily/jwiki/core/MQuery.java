package fastily.jwiki.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fastily.jwiki.dwrap.ImageInfo;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.GSONP;
import fastily.jwiki.util.GroupQueue;
import fastily.jwiki.util.MultiMap;
import fastily.jwiki.util.Tuple;

/**
 * Perform multi-title queries. Use of these methods is intended for <ins>advanced</ins> users who wish to make queries
 * to the server over a large data set. These methods are optimized for performance, and will consolidate titles into
 * single queries to fetch the most data possible per query. If you're looking to make simple, single-item queries,
 * (which is suitable for most users) please use the methods in Wiki.java.
 * 
 * @author Fastily
 * @see Wiki
 */
public final class MQuery
{
	/**
	 * The group {@code prop} query (multiple titles query) maximum
	 */
	private static final int groupQueryMax = 50;

	/**
	 * Constructors disallowed
	 */
	private MQuery()
	{

	}

	/**
	 * Generic page property ({@code prop}) fetching. This implementation fetches *all* available properties. Use this
	 * for prop queries that only return one String of interest per nested JsonObject.
	 * 
	 * @param wiki The Wiki to use
	 * @param titles The titles to query for.
	 * @param qut The query template to use. Set this according to the fetching method being implemented
	 * @param pl Additional custom parameters to apply to each generated WQuery. Optional, set null to disable.
	 * @param elemArrKey The key for each JsonArray for each title the resulting set
	 * @param elemKey The key for each String of interest contained in each JsonObject of the JsonArray pointed to by
	 *           {@code elemArrKey}
	 * @return A Map where the key is the title of the page, and the value is the List of properties fetched.
	 */
	private static MultiMap<String, JsonObject> getContProp(Wiki wiki, ArrayList<String> titles, WQuery.QTemplate qut,
			HashMap<String, String> pl, String elemArrKey)
	{
		MultiMap<String, JsonObject> l = new MultiMap<>();
		GroupQueue<String> gq = new GroupQueue<>(titles, groupQueryMax);

		while (gq.has())
		{
			WQuery wq = new WQuery(wiki, qut).set("titles", gq.poll());
			if (pl != null)
				pl.forEach(wq::set);

			while (wq.has())
				wq.next().propComp("title", elemArrKey).forEach((k, v) -> {
					l.touch(k);
					if (v != null)
						l.put(k, GSONP.getJAofJO(v.getAsJsonArray()));
				});
		}
		return l;
	}

	/**
	 * Performs a non-continuing {@code prop} query. Grabs a title and an element from each returned page.
	 * 
	 * @param wiki The Wiki to query.
	 * @param titles The titles to use
	 * @param qut The QTemplate to use
	 * @param pl Additional parameters to pass to each created WQuery, set null to disable.
	 * @param eKey The value key to get from each page element. If this cannot be found, then it is set to null.
	 * @return The {@code title} of each page as the key, and the value of the associated {@code eKey}.
	 */
	private static HashMap<String, JsonElement> getNoContProp(Wiki wiki, ArrayList<String> titles, WQuery.QTemplate qut,
			HashMap<String, String> pl, String eKey)
	{
		HashMap<String, JsonElement> m = new HashMap<>();

		GroupQueue<String> gq = new GroupQueue<>(titles, groupQueryMax);
		while (gq.has())
		{
			WQuery wq = new WQuery(wiki, qut).set("titles", gq.poll());
			if (pl != null)
				pl.forEach(wq::set);

			m.putAll(wq.next().propComp("title", eKey));
		}
		return m;
	}

	/**
	 * Performs a non-continuing {@code list} query. Grabs JsonObjects from the JsonArray in the server Response.
	 * 
	 * @param wiki The Wiki to query
	 * @param titles The titles to use
	 * @param qut The QTemplate to use
	 * @param pl Additional parameters to pass to each created WQuery, set null to disable.
	 * @param tQKey The variable name to use for each set of 50 {@code titles} in the url passed to the server.
	 * @param aKey The key pointing to the JsonArray of JsonObject in the server's Response.
	 * @return An ArrayList of JsonObject collected from the server Response(s).
	 */
	private static ArrayList<JsonObject> getNoContList(Wiki wiki, ArrayList<String> titles, WQuery.QTemplate qut,
			HashMap<String, String> pl, String tQKey, String aKey)
	{
		ArrayList<JsonObject> l = new ArrayList<>();

		GroupQueue<String> gq = new GroupQueue<>(titles, groupQueryMax);
		while (gq.has())
		{
			WQuery wq = new WQuery(wiki, qut).set(tQKey, gq.poll());
			if (pl != null)
				pl.forEach(wq::set);

			l.addAll(wq.next().listComp(aKey));
		}

		return l;
	}

	/**
	 * Retrieve one String value from each JsonObject ArrayList for each pair in a MapList.
	 * 
	 * @param m The MapList to work with
	 * @param elemKey The key pointing to String to get in each JsonObject.
	 * @return Each title, and the values that were found for it.
	 */
	private static HashMap<String, ArrayList<String>> parsePropToSingle(MultiMap<String, JsonObject> m, String elemKey)
	{
		HashMap<String, ArrayList<String>> xl = new HashMap<>();
		m.l.forEach((k, v) -> xl.put(k, FL.toAL(v.stream().map(e -> GSONP.gString(e, elemKey)))));

		return xl;
	}

	/**
	 * Retrieve one String value from each JsonObject ArrayList for each pair in a MapList. Assumes that the key to
	 * select from each JsonObject is {@code title}.
	 * 
	 * @param m The MapList to work with
	 * @return Each title, and the values that were found for it.
	 */
	private static HashMap<String, ArrayList<String>> parsePropToSingle(MultiMap<String, JsonObject> m)
	{
		return parsePropToSingle(m, "title");
	}

	/**
	 * Retrieve two String value from each JsonObject ArrayList for each pair in a MapList.
	 * 
	 * @param m The MapList to work with
	 * @param elemKey1 The key pointing to the first String to get in each JsonObject.
	 * @param elemKey2 The key pointing to the second String to get in each JsonObject.
	 * @return Each title, and the values that were found for it.
	 */
	private static HashMap<String, ArrayList<Tuple<String, String>>> parsePropToDouble(MultiMap<String, JsonObject> m, String elemKey1,
			String elemKey2)
	{
		HashMap<String, ArrayList<Tuple<String, String>>> xl = new HashMap<>();
		m.l.forEach(
				(k, v) -> xl.put(k, FL.toAL(v.stream().map(e -> new Tuple<>(GSONP.gString(e, elemKey1), GSONP.gString(e, elemKey2))))));

		return xl;
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
		HashMap<String, ArrayList<String>> l = new HashMap<>();
		getNoContList(wiki, users, WQuery.USERRIGHTS, null, "ususers", "users")
				.forEach(jo -> l.put(GSONP.gString(jo, "name"), GSONP.jaOfStrToAL(jo.getAsJsonArray("groups"))));

		return l;
	}

	/**
	 * Gets ImageInfo objects for each revision of a File.
	 * 
	 * @param wiki The Wiki object to use
	 * @param titles The titles to query
	 * @return A map with titles keyed to respective lists of ImageInfo.
	 */
	public static HashMap<String, ArrayList<ImageInfo>> getImageInfo(Wiki wiki, ArrayList<String> titles)
	{
		HashMap<String, ArrayList<ImageInfo>> l = new HashMap<>();
		getContProp(wiki, titles, WQuery.IMAGEINFO, null, "imageinfo").l
				.forEach((k, v) -> l.put(k, FL.toAL(v.stream().map(ImageInfo::new))));

		// MediaWiki imageinfo is not a well-behaved module
		l.forEach((k, v) -> Collections.sort(v));

		return l;
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
		return parsePropToSingle(getContProp(wiki, titles, WQuery.PAGECATEGORIES, null, "categories"));
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
		HashMap<String, Integer> l = new HashMap<>();
		getNoContProp(wiki, titles, WQuery.CATEGORYINFO, null, "categoryinfo")
				.forEach((k, v) -> l.put(k, v == null ? 0 : v.getAsJsonObject().get("size").getAsInt()));
		return l;
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
		HashMap<String, String> l = new HashMap<>();
		getNoContProp(wiki, titles, WQuery.PAGETEXT, null, "revisions").forEach((k, v) -> {
			if (v == null)
				l.put(k, "");
			else
			{
				ArrayList<JsonObject> jl = GSONP.getJAofJO(v.getAsJsonArray());
				l.put(k, jl.isEmpty() ? "" : GSONP.gString(jl.get(0), "*"));
			}
		});

		return l;
	}

	/**
	 * Get wiki links on a page.
	 * 
	 * @param wiki The wiki object to use
	 * @param titles The titles to query.
	 * @param ns Namespaces to include-only. Optional param: leave blank to disable.
	 * @return A list of results keyed by title.
	 */
	public static HashMap<String, ArrayList<String>> getLinksOnPage(Wiki wiki, ArrayList<String> titles, NS... ns)
	{
		HashMap<String, String> pl = new HashMap<>();
		if (ns != null && ns.length > 0)
			pl.put("plnamespace", wiki.nsl.createFilter(ns));

		return parsePropToSingle(getContProp(wiki, titles, WQuery.LINKSONPAGE, pl, "links"));
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
		return parsePropToSingle(
				getContProp(wiki, titles, WQuery.LINKSHERE, FL.pMap("lhshow", (redirects ? "" : "!") + "redirect"), "linkshere"));
	}

	/**
	 * Gets a list of pages transcluding a template.
	 * 
	 * @param wiki The wiki object to use
	 * @param titles The titles to query
	 * @param ns Only return results from this/these namespace(s). Optional param: leave blank to disable.
	 * @return A list of results keyed by title.
	 */
	public static HashMap<String, ArrayList<String>> transcludesIn(Wiki wiki, ArrayList<String> titles, NS... ns)
	{
		HashMap<String, String> pl = new HashMap<>();
		if (ns.length > 0)
			pl.put("tinamespace", wiki.nsl.createFilter(ns));

		return parsePropToSingle(getContProp(wiki, titles, WQuery.TRANSCLUDEDIN, pl, "transcludedin"));
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
		return parsePropToSingle(getContProp(wiki, titles, WQuery.FILEUSAGE, null, "fileusage"));
	}

	/**
	 * Checks if list of titles exists.
	 * 
	 * @param wiki The wiki object to use
	 * @param titles The titles to query
	 * @return Results keyed by title. {@code true} means the title exists.
	 */
	public static HashMap<String, Boolean> exists(Wiki wiki, ArrayList<String> titles)
	{
		HashMap<String, Boolean> l = new HashMap<>();
		getNoContProp(wiki, titles, WQuery.EXISTS, null, "missing").forEach((k, v) -> l.put(k, v == null));
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
		exists(wiki, titles).forEach((k, v) -> {
			if(v == exists)
				l.add(k);
		});
		
		return l;
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
		return parsePropToSingle(getContProp(wiki, titles, WQuery.IMAGES, null, "images"));
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
		return parsePropToSingle(getContProp(wiki, titles, WQuery.TEMPLATES, null, "templates"));
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
		return parsePropToDouble(getContProp(wiki, titles, WQuery.GLOBALUSAGE, null, "globalusage"), "title", "wiki");
	}

	/**
	 * Resolves title redirects on a Wiki.
	 * 
	 * @param wiki The Wiki to run the query against
	 * @param titles The titles to attempt resolving.
	 * @return A HashMap where each key is the original title, and the value is the resolved title.
	 */
	public static HashMap<String, String> resolveRedirects(Wiki wiki, ArrayList<String> titles)
	{
		HashMap<String, String> l = new HashMap<>();
		titles.stream().forEach(s -> l.put(s, s));

		getNoContList(wiki, titles, WQuery.RESOLVEREDIRECT, null, "titles", "redirects")
				.forEach(jo -> l.put(GSONP.gString(jo, "from"), GSONP.gString(jo, "to")));

		return l;
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
		HashMap<String, String> pl = new HashMap<>();
		if (localOnly)
			pl.put("dflocalonly", "");

		HashMap<String, ArrayList<String>> l = parsePropToSingle(getContProp(wiki, titles, WQuery.DUPLICATEFILES, pl, "duplicatefiles"),
				"name");
		l.forEach((k, v) -> v.replaceAll(s -> s.replace('_', ' ')));

		return l;
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
		HashMap<String, ArrayList<Tuple<String, String>>> xl = parsePropToDouble(
				getContProp(wiki, titles, WQuery.DUPLICATEFILES, null, "duplicatefiles"), "name", "shared");

		HashMap<String, ArrayList<String>> l = new HashMap<>();
		xl.forEach((k, v) -> l.put(k, FL.toAL(v.stream().filter(t -> t.y != null).map(t -> t.x.replace('_', ' ')))));
		return l;
	}
}