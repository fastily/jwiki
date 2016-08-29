package jwiki.core;

/**
 * Constants and global Settings for jwiki.
 * 
 * @author Fastily
 *
 */
public final class Settings
{
	/**
	 * Flag indicating whether jwiki should print debug information to console.
	 */
	public static boolean debug = false;

	/**
	 * The value to set in the <code>user-agent</code> header of https requests.
	 */
	public static String userAgent = String.format("jwiki on %s %s with JVM %s", System.getProperty("os.name"),
			System.getProperty("os.version"), System.getProperty("java.version"));

	/**
	 * The primary network communication protocol. Default = https.
	 */
	protected static final String comPro = "https://";

	/**
	 * Constructors disallowed.
	 */
	private Settings()
	{

	}
}