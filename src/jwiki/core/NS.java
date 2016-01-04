package jwiki.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jwiki.util.FString;

/**
 * Contains default namespaces and methods to get non-standard namespaces.
 * 
 * @author Fastily
 *
 */
public final class NS
{
	/**
	 * Main namespace
	 */
	public static final NS MAIN = new NS(0);

	/**
	 * Talk namespace for main
	 */
	public static final NS TALK = new NS(1);
	/**
	 * User namespace
	 */
	public static final NS USER = new NS(2);

	/**
	 * User talk namespace
	 */
	public static final NS USER_TALK = new NS(3);

	/**
	 * Project namespace
	 */
	public static final NS PROJECT = new NS(4);

	/**
	 * Project talk namespace
	 */
	public static final NS PROJECT_TALK = new NS(5);

	/**
	 * File namespace
	 */
	public static final NS FILE = new NS(6);

	/**
	 * File talk namespace
	 */
	public static final NS FILE_TALK = new NS(7);

	/**
	 * MediaWiki namespace
	 */
	public static final NS MEDIAWIKI = new NS(8);

	/**
	 * Media talk namespace
	 */
	public static final NS MEDIA_TALK = new NS(9);

	/**
	 * Template namespace
	 */
	public static final NS TEMPLATE = new NS(10);

	/**
	 * Template talk namespace
	 */
	public static final NS TEMPLATE_TALK = new NS(11);

	/**
	 * Help namespace
	 */
	public static final NS HELP = new NS(12);

	/**
	 * Help talk namespace
	 */
	public static final NS HELP_TALK = new NS(13);

	/**
	 * Category namespace
	 */
	public static final NS CATEGORY = new NS(14);

	/**
	 * Category talk namespace.
	 */
	public static final NS CATEGORY_TALK = new NS(15);

	/**
	 * This NS's value.
	 */
	public final int v;

	/**
	 * Constructor
	 * 
	 * @param v The namesapce value to initialize the NS with.
	 */
	private NS(int v)
	{
		this.v = v;
	}

	/**
	 * Grabs the integer value of NS objects and returns them in a list of Strings.
	 * @param nsl The NS objects
	 * @return The list with integer conterparts for each namespace. 
	 */
	private static ArrayList<String> toString(NS... nsl)
	{
		HashSet<String> l = new HashSet<>();
		for (NS n : nsl)
			l.add("" + n.v);

		return new ArrayList<>(l);
	}
	
	/**
	 * Determines if two namespaces are the same namespace
	 */
	public boolean equals(Object x)
	{
		return x instanceof NS && v == ((NS)x).v;
	}
	
	
	/**
	 * A namespace manager object. One for each Wiki object.
	 * 
	 * @author Fastily
	 */
	protected static class NSManager
	{
		/**
		 * String to integer mapping of namespaces.  This is the inverse of <code>b</code>.
		 */
		private HashMap<String, Integer> a = new HashMap<>();

		/**
		 * Integer to String mapping of namespaces.  This is the inverse of <code>a</code>.
		 */
		private HashMap<Integer, String> b = new HashMap<>();
		
		/**
		 * Regex used to strip the namespace from a title.
		 */
		private String nssRegex;

		/**
		 * Pattern version of <code>nssRegex</code>
		 */
		private Pattern p;
		
		/**
		 * Constructor
		 */
		private NSManager()
		{

		}

		/**
		 * Generate namespace manager for a wiki.
		 * 
		 * @param r The reply from the server.
		 * @return The NSManager.
		 */
		protected static NSManager makeNSManager(Reply r)
		{
			NSManager m = new NSManager();
			for (Reply x : r.bigJSONObjectGet("namespaces"))
			{
				String name = x.getString("*");
				if (name.isEmpty())
					name = "Main";

				m.a.put(name, new Integer(x.getInt("id")));
			}

			m.b.putAll(m.a.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey)));

			for(Reply ra : r.getJAOfJOAsALR("namespacealiases"))
				m.a.put(ra.getString("*"), ra.getInt("id"));
			
			ArrayList<String> tlx = new ArrayList<>();
			for (String s : m.a.keySet())
				tlx.add(s.replace(" ", "(_| )"));

			m.nssRegex = String.format("(?i)^(%s):", FString.fenceMaker("|", tlx));
			m.p = Pattern.compile(m.nssRegex);

			return m;
		}

		/**
		 * Generates a filter for use with params passed to API. This DOES NOT URLEncode.
		 * 
		 * @param nsl The namespaces to select.
		 * @return The raw filter string.
		 */
		protected String createFilter(NS... nsl)
		{
			return FString.fenceMaker("|", NS.toString(nsl)); 
		}

		/**
		 * Gets a namespace object for the specified namespace. PRECONDITION: This method is CASE-SENSITIVE, so be sure
		 * your spelling and capitalization are correct.  Be sure that the wiki you're in supports this
		 * 
		 * @param s The namespace (exact spelling and capitalization), without the suffix ":"
		 * @return A NS object for the namespace or null if no namespace is associated with <code>s</code>
		 */
		protected NS get(String s)
		{
			if (s.isEmpty() || s.equals("Main"))
				return MAIN;

			return a.containsKey(s) ? new NS(a.get(s)) : null;
		}
		
		/**
		 * Strips any namespace prefix from <code>s</code>, if possible.
		 * @param s The title to strip the namespace prefix from
		 * @return <code>s</code>, without a namespace prefix.
		 */
		protected String nss(String s)
		{
			return s.replaceAll(nssRegex, "");
		}
		
		/**
		 * Gets the namespace this title belongs to
		 * @param s The title to check
		 * @return A NS for this title.
		 */
		protected NS whichNS(String s)
		{
			Matcher m = p.matcher(s);
			return !m.find() ? MAIN : new NS(a.get(s.substring(m.start(), m.end()-1)).intValue());
		}
		
		/**
		 * Get the String prefix associated with a namespace.
		 * @param n The namespace to get a String prefix for.
		 * @param addColon Set to true to add a colon at the end of the String.
		 * @return The namespace in String form as specified, or null if we couldn't find the namespace.
		 */
		protected String toString(NS n, boolean addColon)
		{
			return b.containsKey(n.v) ? b.get(n.v) + (addColon ? ":" : "") : null;
		}
	}
}