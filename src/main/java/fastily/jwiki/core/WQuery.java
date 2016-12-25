package fastily.jwiki.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fastily.jwiki.util.FL;
import fastily.jwiki.util.GSONP;
import fastily.jwiki.util.MapList;
import fastily.jwiki.util.Tuple;

/**
 * Wraps the various functions of API functions of {@code action=query}.
 * 
 * @author Fastily
 *
 */
public class WQuery
{
	/**
	 * Default parameters for getting category size info
	 */
	public static final QTemplate ALLOWEDFILEXTS = new QTemplate(FL.pMap("meta", "siteinfo", "siprop", "fileextensions"));

	/**
	 * Default parameters for getting category size info
	 */
	public static final QTemplate ALLPAGES = new QTemplate(FL.pMap("list", "allpages"), "aplimit");

	/**
	 * Default parameters for getting category size info
	 */
	public static final QTemplate CATEGORYINFO = new QTemplate(FL.pMap("prop", "categoryinfo", "titles", null));

	/**
	 * Default parameters for listing category members
	 */
	public static final QTemplate CATEGORYMEMBERS = new QTemplate(FL.pMap("list", "categorymembers", "cmtitle", null), "cmlimit");

	/**
	 * Default parameters for getting Namespace information on a Wiki.
	 */
	public static final QTemplate NAMESPACES = new QTemplate(FL.pMap("meta", "siteinfo", "siprop", "namespaces|namespacealiases"));

	/**
	 * Default parameters for getting duplicate files
	 */
	public static final QTemplate DUPLICATEFILES = new QTemplate(FL.pMap("prop", "duplicatefiles", "titles", null), "dflimit");

	/**
	 * Default parameters for determining if a page exists.
	 */
	public static final QTemplate EXISTS = new QTemplate(FL.pMap("prop", "pageprops", "ppprop", "missing", "titles", null));

	/**
	 * Default parameters for getting file usage
	 */
	public static final QTemplate FILEUSAGE = new QTemplate(FL.pMap("prop", "fileusage", "titles", null), "fulimit");

	/**
	 * Default parameters for getting global usage of a file
	 */
	public static final QTemplate GLOBALUSAGE = new QTemplate(FL.pMap("prop", "globalusage", "titles", null), "gulimit");

	/**
	 * Default parameters for getting files on a page
	 */
	public static final QTemplate IMAGES = new QTemplate(FL.pMap("prop", "images", "titles", null), "imlimit");

	/**
	 * Default parameters for getting image info of a file.
	 */
	public static final QTemplate IMAGEINFO = new QTemplate(
			FL.pMap("prop", "imageinfo", "iiprop", "canonicaltitle|url|size|sha1|mime|user|timestamp|comment", "titles", null),
			"iilimit");

	/**
	 * Default parameters for getting links to a page
	 */
	public static final QTemplate LINKSHERE = new QTemplate(
			FL.pMap("prop", "linkshere", "lhprop", "title", "lhshow", null, "titles", null), "lhlimit");

	/**
	 * Default parameters for getting links on a page
	 */
	public static final QTemplate LINKSONPAGE = new QTemplate(FL.pMap("prop", "links", "titles", null), "pllimit");

	/**
	 * Default parameters for listing logs.
	 */
	public static final QTemplate LOGEVENTS = new QTemplate(FL.pMap("list", "logevents"), "lelimit");

	/**
	 * Default parameters for getting page categories.
	 */
	public static final QTemplate PAGECATEGORIES = new QTemplate(FL.pMap("prop", "categories", "titles", null), "cllimit");

	/**
	 * Default parameters for getting page text.
	 */
	public static final QTemplate PAGETEXT = new QTemplate(FL.pMap("prop", "revisions", "rvprop", "content", "titles", null));

	/**
	 * Default parameters for listing protected titles.
	 */
	public static final QTemplate PROTECTEDTITLES = new QTemplate(
			FL.pMap("list", "protectedtitles", "ptprop", "timestamp|level|user|comment"), "ptlimit");

	/**
	 * Default parameters for listing recent changes.
	 */
	public static final QTemplate RECENTCHANGES = new QTemplate(
			FL.pMap("list", "recentchanges", "rcprop", "title|timestamp|user|comment", "rctype", "edit|new|log"), "rclimit");

	/**
	 * Default parameters for resolving redirects
	 */
	public static final QTemplate RESOLVEREDIRECT = new QTemplate(FL.pMap("redirects", "", "titles", null));

	/**
	 * Default parameters for listing page revisions
	 */
	public static final QTemplate REVISIONS = new QTemplate(FL.pMap("prop", "revisions", "rvprop", "timestamp|user|comment|content", "titles", null), "rvlimit");

	/**
	 * Default parameters for getting templates on a page
	 */
	public static final QTemplate TEMPLATES = new QTemplate(FL.pMap("prop", "templates", "tiprop", "title", "titles", null), "tllimit");

	/**
	 * Default parameters for {@code meta=tokens}.
	 */
	public static final QTemplate TOKENS = new QTemplate(FL.pMap("meta", "tokens", "type", "login|csrf"));

	/**
	 * Default parameters for getting a page's transclusions.
	 */
	public static final QTemplate TRANSCLUDEDIN = new QTemplate(FL.pMap("prop", "transcludedin", "tiprop", "title", "titles", null),
			"tilimit");

	/**
	 * Default parameters for listing user contributions.
	 */
	public static final QTemplate USERCONTRIBS = new QTemplate(FL.pMap("list", "usercontribs", "ucuser", null), "uclimit");

	/**
	 * Default parameters for listing users and their rights.
	 */
	public static final QTemplate USERRIGHTS = new QTemplate(FL.pMap("list", "users", "usprop", "groups", "ususers", null));

	/**
	 * Default parameters for listing user uploads
	 */
	public static final QTemplate USERUPLOADS = new QTemplate(FL.pMap("list", "allimages", "aisort", "timestamp", "aiuser", null),
			"ailimit");

	/**
	 * The master parameter list. Tracks current query status.
	 */
	private final HashMap<String, String> pl = FL.pMap("action", "query", "format", "json");

	/**
	 * The List of limit Strings.
	 */
	private final ArrayList<String> limStrList = new ArrayList<>();

	/**
	 * The Wiki object to perform queries with
	 */
	private final Wiki wiki;

	/**
	 * Flag indicating if this query can be continued.
	 */
	private boolean canCont = true;

	/**
	 * Tracks and limits entries returned, if applicable.
	 */
	private int queryLimit, totalLimit = -1, currCount = 0;

	/**
	 * Constructor, creates a new WQuery
	 * 
	 * @param wiki The Wiki object to perform queries with
	 * @param qut The QueryUnitTemplate objects to instantiate this WQuery with.
	 */
	public WQuery(Wiki wiki, QTemplate... qut)
	{
		this.wiki = wiki;
		this.queryLimit = wiki.conf.maxResultLimit;

		for (QTemplate qt : qut)
		{
			pl.putAll(qt.defaultFields);
			if (qt.limString != null)
				limStrList.add(qt.limString);
		}
	}

	/**
	 * Constructor, creates a limited WQuery.
	 * 
	 * @param wiki The Wiki object to perform queries with.
	 * @param totalLimit The maximum number of items to return until WQuery is exhausted. Actual number of items returned
	 *           may be less. Optional, disable with -1.
	 * @param qut The QueryUnitTemplate objects to instantiate this WQuery with.
	 */
	public WQuery(Wiki wiki, int totalLimit, QTemplate... qut)
	{
		this(wiki, qut);
		this.totalLimit = totalLimit;
	}

	/**
	 * Test if this WQuery has any queries remaining.
	 * 
	 * @return True if this WQuery can still be used to make continuation queries.
	 */
	public boolean has()
	{
		return canCont;
	}

	/**
	 * Attempts to perform the next query in this sequence.
	 * 
	 * @return A JsonObject with the response from the server, or null if something went wrong.
	 */
	public JsonObject next()
	{
		// sanity check
		if (pl.containsValue(null))
			throw new IllegalStateException(String.format("Fill in *all* the null fields -> %s", pl));
		else if (!canCont)
			return null;

		try
		{
			if (totalLimit > 0 && (currCount += queryLimit) > totalLimit)
			{
				adjustLimit(queryLimit - (currCount - totalLimit));
				canCont = false;
			}

			JsonObject result = GSONP.jp.parse(wiki.apiclient.basicGET(pl).body().string()).getAsJsonObject();
			if (result.has("continue"))
				pl.putAll(wiki.conf.gson.fromJson(result.getAsJsonObject("continue"), GSONP.strMapT));
			else
				canCont = false;

			return result;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Sets a key-value pair. DO NOT URL-encode. These are the parameters that will be passed to the MediaWiki API.
	 * 
	 * @param key The parameter key to set
	 * @param value The parameter value to set
	 * @return This WQuery. Useful for chaining.
	 */
	public WQuery set(String key, String value)
	{
		pl.put(key, value);
		return this;
	}

	/**
	 * Sets a key-values pair. DO NOT URL-encode. These are the parameters that will be passed to the MediaWiki API.
	 * 
	 * @param key The parameter key to set
	 * @param values The parameter value to set; these will be pipe-fenced.
	 * @return This WQuery. Useful for chaining.
	 */
	public WQuery set(String key, ArrayList<String> values)
	{
		return set(key, FL.pipeFence(values));
	}

	/**
	 * Configure this WQuery to fetch a maximum of {@code limit} items per query. Does nothing if this query does not use
	 * limit Strings.
	 * 
	 * @param limit The new limit. Set as -1 to get the maximum number of items per query.
	 * 
	 * @return This WQuery, for chaining convenience.
	 */
	public WQuery adjustLimit(int limit)
	{
		String limitString;
		if (limit <= 0 || limit > wiki.conf.maxResultLimit)
		{
			limitString = "max";
			queryLimit = wiki.conf.maxResultLimit;
		}
		else
		{
			limitString = "" + limit;
			queryLimit = limit;
		}

		for (String s : limStrList)
			pl.put(s, limitString);

		return this;
	}

	/**
	 * Stores parameter definition rules for a given query and can use these rules to generate a QueryUnit.
	 * 
	 * @author Fastily
	 *
	 */
	public static class QTemplate
	{
		/**
		 * The default fields for this query type
		 */
		private final HashMap<String, String> defaultFields;

		/**
		 * Optional limit parameter. Will be null if not applicable in this definition.
		 */
		private final String limString;

		/**
		 * Constructor, creates a new QueryUnitTemplate
		 * 
		 * @param defaultFields The default parameters for the query described by this QueryUnitTemplate.
		 * @param reqtFields The required parameters (user-specified) for the query described by this QueryUnitTemplate.
		 */
		public QTemplate(HashMap<String, String> defaultFields)
		{
			this(defaultFields, null);
		}

		/**
		 * Constructor, creates a new QueryUnitTemplate with a limit String.
		 * 
		 * @param defaultFields The default parameters for the query described by this QueryUnitTemplate.
		 * @param limString The limit String parameter. Optional, set null to disable.
		 */
		public QTemplate(HashMap<String, String> defaultFields, String limString)
		{
			this.defaultFields = defaultFields;

			this.limString = limString;
			if (limString != null)
				defaultFields.put(limString, "max");
		}
	}

	/**
	 * Static functions to transform a JsonObject query response into meaningful java data structures.
	 * 
	 * @author Fastily
	 *
	 */
	public static class QXtract // TODO: Normalize extractions!
	{
		/**
		 * Constructors disallowed
		 */
		private QXtract()
		{

		}

		/**
		 * Gets a String and JsonArray from each JsonObject in a JsonArray of JsobObject.
		 * 
		 * @param src The root JsonObject
		 * @param pathToJson The path to the nested JsonObject that contains the key {@code rootArrKey}
		 * @param rootArrKey The key pointing to the target JsonArray of JsonObject
		 * @param titleKey The key for each String to extract from each JsonObject in JsonArray pointed to by
		 *           {@code rootArrKey}
		 * @param elemArrKey The key for each List of String to extract from each JsonObject in JsonArray pointed to by
		 *           {@code rootArrKey}
		 * @return A HashMap of extracted String and ArrayList.
		 */
		public static HashMap<String, ArrayList<String>> extractJAFromJAofJO(JsonObject src, ArrayList<String> pathToJson,
				String rootArrKey, String titleKey, String elemArrKey)
		{
			HashMap<String, ArrayList<String>> l = new HashMap<>();
			GSONP.jaToStream(GSONP.getNestedJO(src, pathToJson).getAsJsonArray(rootArrKey)).map(JsonElement::getAsJsonObject)
					.forEach(jo -> l.put(jo.get(titleKey).getAsString(), GSONP.jaOfStrToAL(jo.getAsJsonArray(elemArrKey))));

			return normalize(src, l);
		}

		/**
		 * Gets a String and ArrayList of String from each JsonObject in a JsonArray in a JsonObject in a JsonObject of
		 * JsonObject.
		 * 
		 * @param src The root JsonObject
		 * @param pathToJson The path to the nested JsonObject that contains all other JsonObjects
		 * @param titleKey The key for each String to extract from each JsonObject in the JsonObject pointed to by
		 *           {@code pathToJson}
		 * @param elemArrKey The key for each List of JsonObject in each JsonObject pointed to by {@code pathToJson}
		 * @param elemKey The key for each String to extract from each JsonObject in the JsonObject pointed to by
		 *           {@code pathToJson}
		 * @return A MapList of expected String and List of Strings.
		 */
		public static MapList<String, String> extractJOListByStrAndJA(JsonObject src, ArrayList<String> pathToJson, String titleKey,
				String elemArrKey, String elemKey)
		{
			MapList<String, String> l = new MapList<>();
			for (JsonObject jo : GSONP.getJOofJO(GSONP.getNestedJO(src, pathToJson)))
			{
				String k = jo.get(titleKey).getAsString();
				l.touch(k); // ensure entry if title does not have this property.
				if (jo.has(elemArrKey))
					l.put(k, GSONP.getStrsFromJAofJO(jo.getAsJsonArray(elemArrKey), elemKey));
			}

			normalize(src, l.l);
			return l;
		}

		/**
		 * Gets a String and ArrayList of Tuple&lt;String, String&gt; from each JsonObject in a JsonArray in a JsonObject
		 * in a JsonObject of JsonObject.
		 * 
		 * @param src The root JsonObject
		 * @param pathToJson The path to the nested JsonObject that contains all other JsonObjects
		 * @param titleKey The key for each String to extract from each JsonObject in the JsonObject pointed to by
		 *           {@code pathToJson}
		 * @param elemArrKey The key for each List of JsonObject in each JsonObject pointed to by {@code pathToJson}
		 * @param elemKey1 Key #1 for each String to extract from each JsonObject in the JsonObject pointed to by
		 *           {@code pathToJson}. This is the {@code x} value of each returned Tuple.
		 * @param elemKey2 Key #2 for each String to extract from each JsonObject in the JsonObject pointed to by
		 *           {@code pathToJson}. This is the {@code y} value of each returned Tuple.
		 * @return A MapList of expected String and List of Tuple Strings.
		 */
		public static MapList<String, Tuple<String, String>> extractJOListByStrAndJA(JsonObject src, ArrayList<String> pathToJson,
				String titleKey, String elemArrKey, String elemKey1, String elemKey2)
		{
			MapList<String, Tuple<String, String>> l = new MapList<>();
			for (JsonObject jo : GSONP.getJOofJO(GSONP.getNestedJO(src, pathToJson)))
			{
				String k = jo.get(titleKey).getAsString();
				l.touch(k); // ensure entry if title does not have this property.
				if (jo.has(elemArrKey))
					l.put(k, FL.toAL(GSONP.getJAofJO(jo.getAsJsonArray(elemArrKey)).stream()
							.map(j -> new Tuple<>(GSONP.gString(j, elemKey1), GSONP.gString(j, elemKey2)))));
			}

			normalize(src, l.l);
			return l;
		}

		/**
		 * Gets a String and Integer from each JsonObject in a JsonObject of JsonObject. If {@code elemJOKey} could not be
		 * found, then the corresponding value assigned to the value contained in {@code titleKey} for that set will be
		 * {@code -1}.
		 * 
		 * @param src The root JsonObject
		 * @param pathToJson The path to the top level JsonObject that contains other JsonObjects of interest.
		 * @param titleKey A String in each JsonObject from {@code pathToJson}
		 * @param elemJOKey The key pointing to a JsonObject of interest in each JsonObject of {@code pathToJson}.
		 * @param elemKey The key pointing to each Integer in each JsonObject pointed to by each instance of
		 *           {@code elemJOKey}.
		 * @return A HashMap with each {@code titleKey} and {@code elemKey}.
		 */
		public static HashMap<String, Integer> extractIntFromJOofJO(JsonObject src, ArrayList<String> pathToJson, String titleKey,
				String elemJOKey, String elemKey)
		{
			return normalize(src,
					new HashMap<>(GSONP.getJOofJO(GSONP.getNestedJO(src, pathToJson)).stream()
							.collect(Collectors.toMap(j -> j.get(titleKey).getAsString(),
									j -> j.has(elemJOKey) ? j.getAsJsonObject(elemJOKey).get(elemKey).getAsInt() : -1))));
		}

		public static ArrayList<String> extractStrFromJAofJO()
		{
			return null;
		}

		/**
		 * Applies title normalization when it is auto-applied by MediaWiki. MediaWiki will return a
		 * <code>normalized</code> JSONArray when it fixes lightly malformed titles. Does nothing if there is no
		 * {@code normalized} JsonArray.
		 * 
		 * @param src The source JsonObject to work with.
		 * @param m The result Map to normalize
		 * @return {@code m}, for chaining convenience.
		 */
		protected static <T1> HashMap<String, T1> normalize(JsonObject src, HashMap<String, T1> m)
		{
			JsonObject jo = GSONP.getNestedJO(src, FL.toSAL("query"));
			if (jo != null && jo.has("normalized"))
				for (JsonObject j : GSONP.getJAofJO(jo.getAsJsonArray("normalized")))
					m.put(j.get("from").getAsString(), m.get(j.get("to").getAsString()));

			return m;
		}
	}
}