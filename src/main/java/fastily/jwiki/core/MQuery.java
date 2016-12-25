package fastily.jwiki.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import fastily.jwiki.dwrap.ImageInfo;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.GSONP;
import fastily.jwiki.util.GroupQueue;
import fastily.jwiki.util.MapList;
import fastily.jwiki.util.Tuple;

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
	 * Default {@code prop} query path to json.
	 */
	protected static final ArrayList<String> propPTJ = FL.toSAL("query", "pages");

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
	 * @param elemArrKey The key for each JsonArray for each title the resulting set
	 * @param elemKey The key for each String of interest contained in each JsonObject of the JsonArray pointed to by
	 *           {@code elemArrKey}
	 * @return A Map where the key is the title of the page, and the value is the List of properties fetched.
	 */
	private static HashMap<String, ArrayList<String>> genericGetProp(Wiki wiki, ArrayList<String> titles, WQuery.QTemplate qut,
			String elemArrKey, String elemKey)
	{
		return genericGetProp(wiki, titles, qut, new HashMap<>(), elemArrKey, elemKey);
	}

	/**
	 * Generic page property ({@code prop}) fetching. This implementation fetches *all* available properties. Use this
	 * for prop queries that only return one String of interest per nested JsonObject.
	 * 
	 * @param wiki The Wiki to use
	 * @param titles The titles to query for.
	 * @param qut The query template to use. Set this according to the fetching method being implemented
	 * @param pl Additional custom parameters to apply to each generated WQuery. Optional, pass empty HashMap to disable.
	 * @param elemArrKey The key for each JsonArray for each title the resulting set
	 * @param elemKey The key for each String of interest contained in each JsonObject of the JsonArray pointed to by
	 *           {@code elemArrKey}
	 * @return A Map where the key is the title of the page, and the value is the List of properties fetched.
	 */
	private static HashMap<String, ArrayList<String>> genericGetProp(Wiki wiki, ArrayList<String> titles, WQuery.QTemplate qut,
			HashMap<String, String> pl, String elemArrKey, String elemKey)
	{
		MapList<String, String> l = new MapList<>();

		GroupQueue<String> gq = new GroupQueue<>(titles, 50);
		while (gq.has())
		{
			WQuery wq = new WQuery(wiki, qut).set("titles", gq.poll()); // .adjustLimit(3);
			pl.forEach(wq::set);

			while (wq.has())
				l.merge(WQuery.QXtract.extractJOListByStrAndJA(wq.next(), propPTJ, "title", elemArrKey, elemKey));
		}

		return l.l;
	}
	
	/**
	 * 
	 * @param wiki
	 * @param titles
	 * @param qut
	 * @param pl
	 * @param elemArrKey
	 * @param elemKey1
	 * @param elemKey2
	 * @return
	 */
	private static HashMap<String, ArrayList<Tuple<String, String>>> genericGetProp(Wiki wiki, ArrayList<String> titles, WQuery.QTemplate qut,
			HashMap<String, String> pl, String elemArrKey, String elemKey1, String elemKey2)
	{
		MapList<String, Tuple<String, String>> l = new MapList<>();

		GroupQueue<String> gq = new GroupQueue<>(titles, 50);
		while (gq.has())
		{
			WQuery wq = new WQuery(wiki, qut).set("titles", gq.poll());
			pl.forEach(wq::set);

			while (wq.has())
				l.merge(WQuery.QXtract.extractJOListByStrAndJA(wq.next(), propPTJ, "title", elemArrKey, elemKey1, elemKey2));
		}

		return l.l;
	}

	private static HashMap<String, ArrayList<Tuple<String, String>>> genericGetProp(Wiki wiki, ArrayList<String> titles, WQuery.QTemplate qut,
			String elemArrKey, String elemKey1, String elemKey2)
	{
		return genericGetProp(wiki, titles, qut, new HashMap<>(), elemArrKey, elemKey1, elemKey2);
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
		return WQuery.QXtract.extractJAFromJAofJO(new WQuery(wiki, WQuery.USERRIGHTS).set("ususers", users).next(), FL.toSAL("query"),
				"users", "name", "groups");
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
		MapList<String, ImageInfo> l = new MapList<>();
		
		WQuery wq = new WQuery(wiki, WQuery.IMAGEINFO).set("titles", titles);
		while(wq.has())
			for(JsonObject jo : GSONP.getJOofJO(GSONP.getNestedJO(wq.next(), propPTJ)))
			{
				String k = GSONP.gString(jo, "title");
				l.touch(k);
				if(jo.has("imageinfo"))
					l.put(k, FL.toAL(GSONP.getJAofJO(jo.getAsJsonArray("imageinfo")).stream().map(ImageInfo::new)));
			}
		
		// MediaWiki imageinfo is not a well-behaved module
		l.l.forEach((k, v) -> Collections.sort(v));
		
		return l.l;
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
		return genericGetProp(wiki, titles, WQuery.PAGECATEGORIES, "categories", "title");
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

		GroupQueue<String> gq = new GroupQueue<>(titles, 50);
		while (gq.has())
			l.putAll(WQuery.QXtract.extractIntFromJOofJO(new WQuery(wiki, WQuery.CATEGORYINFO).set("titles", gq.poll()).next(), propPTJ,
					"title", "categoryinfo", "size"));

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
		return new HashMap<>(genericGetProp(wiki, titles, WQuery.PAGETEXT, "revisions", "*").entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().isEmpty() ? "" : e.getValue().get(0))));
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

		return genericGetProp(wiki, titles, WQuery.LINKSONPAGE, pl, "links", "title");
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
		return genericGetProp(wiki, titles, WQuery.LINKSHERE, FL.pMap("lhshow", (redirects ? "" : "!") + "redirect"), "linkshere",
				"title");
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

		return genericGetProp(wiki, titles, WQuery.TRANSCLUDEDIN, pl, "transcludedin", "title");
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
		return genericGetProp(wiki, titles, WQuery.FILEUSAGE, "fileusage", "title");
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
		HashMap<String, Boolean> l = new HashMap<>();

		GroupQueue<String> gq = new GroupQueue<>(titles, 50);
		while (gq.has())
			for (JsonObject jo : GSONP
					.getJOofJO(GSONP.getNestedJO(new WQuery(wiki, WQuery.EXISTS).set("titles", gq.poll()).next(), propPTJ)))
				l.put(jo.get("title").getAsString(), !jo.has("missing"));

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
		return genericGetProp(wiki, titles, WQuery.IMAGES, "images", "title");
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
		return genericGetProp(wiki, titles, WQuery.TEMPLATES, "templates", "title");
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
		return genericGetProp(wiki, titles, WQuery.GLOBALUSAGE, "globalusage", "title", "wiki");
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

		GroupQueue<String> gq = new GroupQueue<>(titles, 50);
		while (gq.has())
			for (JsonObject jo : GSONP.getJAofJO(
					GSONP.getNestedJO(new WQuery(wiki, WQuery.RESOLVEREDIRECT).set("titles", gq.poll()).next(), FL.toSAL("query"))
							.getAsJsonArray("redirects")))
				l.put(jo.get("from").getAsString(), jo.get("to").getAsString());

		titles.stream().filter(s -> !l.containsKey(s)).forEach(s -> l.put(s, s));
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
		
		HashMap<String, ArrayList<String>> l = genericGetProp(wiki, titles, WQuery.DUPLICATEFILES, pl, "duplicatefiles", "name");
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
		return new HashMap<>(genericGetProp(wiki, titles, WQuery.DUPLICATEFILES, "duplicatefiles", "name", "shared").entrySet().stream()
		.collect(Collectors.toMap(Map.Entry::getKey, e -> FL.toAL(e.getValue().stream().filter(t -> t.y != null).map(t -> t.x.replace('_', ' '))))));
	}
}