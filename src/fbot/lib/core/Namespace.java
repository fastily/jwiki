package fbot.lib.core;

import java.io.IOException;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

/**
 * Represents a namespace list for a Wiki object.
 * 
 * @author Fastily
 * 
 */
public class Namespace
{
	/**
	 * Local namespace name associated by number. One of two mirrored hashmaps.
	 */
	private HashMap<String, Integer> l1 = new HashMap<String, Integer>();
	
	/**
	 * Local namespace numer associated with name. One of two mirrored hashmaps.
	 */
	private HashMap<Integer, String> l2 = new HashMap<Integer, String>();
	
	/**
	 * Constructor, takes a domain and the current cookiejar with which to make queries.
	 * 
	 * @param domain The domain we're loading the namespace list for, in shorthand.
	 * @param cookiejar The global cookie jar for this wiki.
	 * @throws IOException If something went wrong in initializtion.
	 */
	protected Namespace(String domain, CookieManager cookiejar) throws IOException
	{
		URLBuilder ub = new URLBuilder(domain);
		ub.setAction("query");
		ub.setParams("meta", "siteinfo", "siprop", "namespaces");
		
		Reply r = Request.get(ub.makeURL(), cookiejar);
		if (r.hasError())
			throw new IOException("Failed to initialize");
		
		JSONObject nsl = r.getJSONObject("namespaces");
		for (String s : JSONObject.getNames(nsl))
		{
			JSONObject curr = nsl.getJSONObject(s);
			String name = curr.getString("*");
			Integer id = new Integer(curr.getInt("id"));
			
			if (name.equals("")) // omit empty string, which is actually Main.
				continue;
			
			l1.put(name.toLowerCase(), id);
			l2.put(id, name);
		}
	}
	
	/**
	 * Gets the number of the namespace for the title passed in. No namespace is assumed to be main namespace.
	 * 
	 * @param title The title to check the namespace number for.
	 * @return The integer number of the namespace of the title.
	 */
	public int whichNS(String title)
	{
		int i = title.lastIndexOf(":");
		if (i == -1)
			return 0;
		
		return convert(title.substring(0, i));
	}
	
	/**
	 * Converts a namespace number to a String prefix for the namespace.
	 * 
	 * @param i The namespace number to get a String prefix for.
	 * @return The namespace name, or null if it doesn't exist.
	 */
	public String convert(int i)
	{
		Integer x = new Integer(i);
		if (l2.containsKey(x))
			return l2.get(x);
		else if (i == 0)
			return "Main";
		else
			return null;
	}
	
	/**
	 * Converts a String prefix to an integer representation of a namespace. PRECONDITION: the prefix should not contain
	 * ':' or contain any irregular capitalization/cannonical names.
	 * 
	 * @param prefix The prefix to check.
	 * @return The integer number for the prefix, or throws an IllegalOperationException if the String passed in has no
	 *         associated namespace.
	 */
	public int convert(String prefix)
	{
		String x = prefix.toLowerCase();
		if (l1.containsKey(x))
			return l1.get(x).intValue();
		else if (prefix.equals("") || prefix.equals("Main"))
			return 0;
		else
			throw new IllegalArgumentException(String.format("'%s' is not a recognized prefix.", prefix));
		
	}
	
	/**
	 * Takes a prefix and converts it to its numerical representation.
	 * 
	 * @param prefix The prefix to convert, without the ":".
	 * @return The numerical representation for the prefix.
	 */
	public String prefixToNumString(String prefix)
	{
		return "" + convert(prefix);
	}
	
	/**
	 * Takes several prefixes and simultaenously converts them to their numerical representations.
	 * 
	 * @param prefixes The prefixes to convert, without the ":".
	 * @return The numerical representations for the prefixes, in the order passed in.
	 */
	public String[] prefixToNumStrings(String... prefixes)
	{
		ArrayList<String> l = new ArrayList<String>();
		for (String s : prefixes)
			l.add(prefixToNumString(s));
		
		return l.toArray(new String[0]);
	}
	
	/**
	 * Strips the namespace from a file, if possible.
	 * 
	 * @param title The title to strip the namespace of.
	 * @return The title, without its namespace, or the same String: if there was no namespace to strip OR if ':' was
	 *         the last character in the sequence.
	 */
	public static String nss(String title)
	{
		int i = title.lastIndexOf(':');
		return i > 0 && i + 1 != title.length() ? title.substring(i + 1) : title;
	}
}