package fbot.lib.core;

import java.text.SimpleDateFormat;

/**
 * Contains a number of constants useful to the package.
 * 
 * @author Fastily
 *
 */
public class Constants
{
	/**
	 * Indicates whether we are in debug mode or not.
	 */
	public static boolean debug = false;
	
	/**
	 * The date format with which we're using to parse returned time stamps from the server. MediaWiki returns dates in
	 * the form: <tt>2013-12-16T00:25:17Z</tt>
	 */
	protected static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	/**
	 * The maximum allowable number of results returned per query.  MediaWiki -> 500 per default.
	 */
	protected static final int maxquerysz = 500;
	
	/**
	 * The maximum allowable number of entries to request data for per group entry.
	 */
	protected static final int groupquerymax = 25;
	
}