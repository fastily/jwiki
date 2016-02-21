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
	 * Indicates whether jwiki should log events to terminal for debugging.
	 */
	public static boolean debug = false;

	/**
	 * Indicates whether jwiki should produce more verbose logging statements.
	 */
	public static boolean verbose = false;

	/**
	 * The user agent we're using to make https requests.
	 */
	public static String userAgent = String.format("jwiki on %s %s with JVM %s", System.getProperty("os.name"),
			System.getProperty("os.version"), System.getProperty("java.version"));

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