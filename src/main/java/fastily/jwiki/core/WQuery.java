package fastily.jwiki.core;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fastily.jwiki.util.FL;
import fastily.jwiki.util.GSONP;

/**
 * Wraps the various functions of API functions of {@code action=query}.
 * 
 * @author Fastily
 *
 */
class WQuery
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
	public static final QTemplate REVISIONS = new QTemplate(
			FL.pMap("prop", "revisions", "rvprop", "timestamp|user|comment|content", "titles", null), "rvlimit");

	/**
	 * Default parameters for getting templates on a page
	 */
	public static final QTemplate TEMPLATES = new QTemplate(FL.pMap("prop", "templates", "tiprop", "title", "titles", null), "tllimit");

	/**
	 * Default parameters for getting a csrf token.
	 */
	public static final QTemplate TOKENS_CSRF = new QTemplate(FL.pMap("meta", "tokens", "type", "csrf"));
	
	/**
	 * Default parameters for getting a login token.
	 */
	public static final QTemplate TOKENS_LOGIN = new QTemplate(FL.pMap("meta", "tokens", "type", "login"));

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
	public QReply next()
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
				pl.putAll(GSONP.gson.fromJson(result.getAsJsonObject("continue"), GSONP.strMapT));
			else
				canCont = false;

			if(wiki.conf.debug)
				ColorLog.debug(wiki, GSONP.gsonPP.toJson(result));
			
			return new QReply(result);
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
	protected static class QTemplate
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
	 * A Response from the server for query modules. Contains pre-defined comprehension methods for convenience.
	 * 
	 * @author Fastily
	 *
	 */
	protected static class QReply
	{
		/**
		 * Default path to json for {@code prop} queries.
		 */
		protected static final ArrayList<String> defaultPropPTJ = FL.toSAL("query", "pages");

		/**
		 * Tracks {@code normalized} titles. The key is the {@code from} (non-normalized) title and the value is the
		 * {@code to} (normalized) title.
		 */
		private HashMap<String, String> normalized = null;

		/**
		 * The JsonObject which was passed as input
		 */
		protected final JsonObject input;

		/**
		 * Creates a new QReply. Will parse the {@code normalized} JsonArray if it is found in {@code input}.
		 * 
		 * @param input The Response received from the server.
		 */
		private QReply(JsonObject input)
		{
			if (input.has("normalized"))
				normalized = GSONP.pairOff(GSONP.getJAofJO(input, "normalized"), "from", "to");

			this.input = input;
		}

		/**
		 * Performs simple {@code list} query Response comprehension. Collects listed JsonObject items in an ArrayList.
		 * 
		 * @param k Points to the JsonArray of JsonObject, under {@code query}, of interest.
		 * @return A lightly processed ArrayList of {@code list} data.
		 */
		protected ArrayList<JsonObject> listComp(String k)
		{
			return input.has("query") ? GSONP.getJAofJO(input.getAsJsonObject("query"), k) : new ArrayList<>();
		}

		/**
		 * Performs simple {@code prop} query Response comprehension. Collects two values from each returned {@code prop}
		 * query item in a HashMap. Title normalization is automatically applied.
		 * 
		 * @param kk Points to the String to set as the HashMap key in each {@code prop} query item.
		 * @param vk Points to the JsonElement to set as the HashMap value in each {@code prop} query item.
		 * @return A lightly processed HashMap of {@code prop} data.
		 */
		protected HashMap<String, JsonElement> propComp(String kk, String vk)
		{
			HashMap<String, JsonElement> m = new HashMap<>();

			JsonObject x = GSONP.getNestedJO(input, defaultPropPTJ);
			if (x == null)
				return m;

			for (JsonObject jo : GSONP.getJOofJO(x))
				m.put(GSONP.gString(jo, kk), jo.get(vk));

			return normalize(m);
		}

		/**
		 * Performs simple {@code meta} query Response comprehension.
		 * 
		 * @param k The key to get a JsonElement for.
		 * @return The JsonElement pointed to by {@code k} or null/empty JsonObject on error.
		 */
		protected JsonElement metaComp(String k)
		{
			return input.has("query") ? input.getAsJsonObject("query").get(k) : new JsonObject();
		}

		/**
		 * Performs title normalization when it is automatically done by MediaWiki. MediaWiki will return a
		 * {@code normalized} JsonArray when it fixes lightly malformed titles. This is intended for use with {@code prop}
		 * style queries.
		 * 
		 * @param <V> Any Object.
		 * @param m The Map of elements to normalize.
		 * @return {@code m}, for chaining convenience.
		 */
		protected <V> HashMap<String, V> normalize(HashMap<String, V> m)
		{
			if (normalized != null)
				normalized.forEach((f, t) -> {
					if (m.containsKey(t))
						m.put(f, m.get(t));
				});

			return m;
		}
	}
}