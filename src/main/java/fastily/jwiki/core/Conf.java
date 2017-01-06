package fastily.jwiki.core;

import fastily.jwiki.util.Tuple;

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
	public boolean debug = false; //TODO: ENABLE DEBUG MODE

	/**
	 * The default <code>User-Agent</code> header for HTTP requests.
	 */
	public String userAgent = String.format("jwiki on %s %s with JVM %s", System.getProperty("os.name"),
			System.getProperty("os.version"), System.getProperty("java.version"));

	/**
	 * Default HTTP communication protocol.
	 */
	public String comms = "https";

	/**
	 * Default Wiki API path (goes after domain).
	 */
	public String scptPath = "w/api.php";

	//TODO: set bot flag!
	/**
	 * Flag indicating whether the logged in user is a bot. 
	 */
	protected boolean isBot = false;

	/**
	 * The base API domain. e.g. <code>en.wikipedia.org</code>
	 */
	public final String domain;

	/**
	 * The low maximum limit for maximum number of list items returned for queries that return lists. Use this if a max
	 * value is needed but where the client does not know the max.
	 */
	protected int maxResultLimit = 500;

	/**
	 * The group query (multiple titles query) maximum
	 */
	protected int groupQueryMax = 50;
	
	/**
	 * Username (no namespace prefix) and password, only set if user is logged in.
	 */
	protected Tuple<String, String> upx = null; //TODO: px should be char[]

	/**
	 * CSRF token. Used for actions that change Wiki content.
	 */
	protected String token = "+\\";

	/**
	 * Constructor, should only be called by new instances of Wiki.
	 */
	protected Conf(String domain)
	{
		this.domain = domain;
	}
}