package jwiki.core;

/**
 * Contains a number of constants useful to the package.
 * 
 * @author Fastily
 *
 */
public class Settings
{
	/**
	 * Indicates whether we are in debug mode or not.
	 */
	public static boolean debug = false;

	/**
	 * The user agent we're using to make https requests.
	 */
	public static String useragent = String.format("fastily-jwiki using %s (%s)", System.getProperty("os.name"),
			System.getProperty("os.version"));

	/**
	 * The maximum allowable number of results returned per query. MediaWiki -> 500 per default.
	 */
	protected static final int maxquerysz = 500;

	/**
	 * The maximum allowable number of entries to request data for per group entry.
	 */
	protected static final int groupquerymax = 50;

	/**
	 * The communications protocol we'll be using. Default = https.
	 */
	protected static final String comprotocol = "https://";
	
	/**
	 * Constructors disallowed.
	 */
	private Settings()
	{

	}
}