package fastily.jwiki.core;

import okhttp3.HttpUrl;

/**
 * Per-Wiki configurable settings.
 * 
 * @author Fastily
 *
 */
public final class Conf
{
	/**
	 * Toggles logging of debug information to std err. Disabled (false) by default.
	 */
	public boolean debug = false;

	/**
	 * The {@code User-Agent} header to use for HTTP requests.
	 */
	public String userAgent = String.format("jwiki on %s %s with JVM %s", System.getProperty("os.name"),
			System.getProperty("os.version"), System.getProperty("java.version"));

	/**
	 * The url pointing to the base MediaWiki API endpoint.
	 */
	protected final HttpUrl baseURL;

	/**
	 * Default Wiki API path (goes after domain). Don't change this after logging in.
	 */
	protected String scptPath = "w/api.php";

	/**
	 * Flag indicating whether the logged in user is a bot.
	 */
	protected boolean isBot = false;

	/**
	 * The hostname of the Wiki to target. Example: {@code en.wikipedia.org}
	 */
	public final String hostname;

	/**
	 * The low maximum limit for maximum number of list items returned for queries that return lists. Use this if a max
	 * value is needed but where the client does not know the max.
	 */
	protected int maxResultLimit = 500;

	/**
	 * User name (without namespace prefix), only set if user is logged in.
	 */
	protected String uname = null;

	/**
	 * The logger associated with this Conf.
	 */
	protected ColorLog log;

	/**
	 * CSRF token. Used for actions that change Wiki content.
	 */
	protected String token = "+\\";

	/**
	 * Constructor, should only be called by new instances of Wiki.
	 * 
	 * @param baseURL The url pointing to the base MediaWiki API endpoint.
	 * @param log The logger associated with this log
	 */
	protected Conf(HttpUrl baseURL, ColorLog log)
	{
		this.baseURL = baseURL;
		hostname = baseURL.host();

		this.log = log;
	}
}