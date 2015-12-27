package jwiki.core;

/**
 * Contains a number of constants useful to the package.
 * 
 * @author Fastily
 *
 */
public final class Settings
{
	/**
	 * Indicates whether we are in debug mode or not.
	 */
	public static boolean debug = false;

	/**
	 * The user agent we're using to make https requests.
	 */
	public static String userAgent = String.format("libjwiki on %s (%s) using JRE %s", System.getProperty("os.name"),
			System.getProperty("os.version"), System.getProperty("java.version"));

	/**
	 * The maximum allowable number of entries to request data for per group entry.
	 */
	protected static int groupQueryMax = 50;

	/**
	 * The communications protocol we'll be using. Default = https.
	 */
	protected static final String comPro = "https://";
	
	/**
	 * Constructors disallowed.
	 */
	private Settings()
	{

	}
}