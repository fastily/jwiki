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
	 * Toggles logging of debug information to std err. Disabled by default.
	 */
	public boolean debug = false;

	/**
	 * The default {@code User-Agent} header for HTTP requests.
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
	 * The base domain. e.g. {@code en.wikipedia.org}
	 */
	public final String domain;

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
	 * CSRF token. Used for actions that change Wiki content.
	 */
	protected String token = "+\\";

	/**
	 * Constructor, should only be called by new instances of Wiki.
	 * @param baseURL The url pointing to the base MediaWiki API endpoint.
	 */
	protected Conf(HttpUrl baseURL)
	{
		this.baseURL = baseURL;
		domain = baseURL.host();
	}
}