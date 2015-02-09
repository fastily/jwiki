package jwiki.core;

import java.util.ArrayList;
import java.util.HashMap;

import jwiki.util.Tuple;

/**
 * Represents a namespace list for a Wiki object.
 * 
 * @author Fastily
 * 
 */
public class Namespace
{
	/**
	 * Stores key-value pairs for namespace.
	 */
	private HashMap<Object, Tuple<Integer, String>> l = new HashMap<>();
	
	/**
	 * Private constructor
	 */
	private Namespace()
	{
		
	}
	
	/**
	 * Makes a namespace object with a reply from the server.
	 * 
	 * @param r Reply from the server.
	 * @return The Namespace list
	 */
	protected static Namespace makeNamespace(Reply r)
	{
		Namespace ns = new Namespace();
		for(Reply x : r.bigJSONObjectGet("namespaces"))
		{
			String name = x.getString("*");
			if(name.isEmpty())
				name = "Main";
			
			Integer id = new Integer(x.getInt("id"));
			Tuple<Integer, String> t = new Tuple<>(id, name);
			ns.l.put(name.toLowerCase(), t);
			ns.l.put(id, t);
		}
		return ns;
	}
	
	/**
	 * Gets the number of the namespace for the title passed in. No namespace is assumed to be main namespace.
	 * 
	 * @param title The title to check the namespace number for.
	 * @return The integer number of the namespace of the title.
	 */
	protected int whichNS(String title)
	{
		int i = title.lastIndexOf(":");
		return i == -1 ? 0 : convert(title.substring(0, i));
	}
	
	/**
	 * Converts an int id value to a String namespace title.
	 * 
	 * @param i The id to lookup.
	 * @return The namespace title, or null if we couldn't find it.
	 */
	protected String convert(int i)
	{
		return convert(new Integer(i));
	}
	
	/**
	 * Converts an Integer id value to a String namespace title.
	 * 
	 * @param i The id to lookup.
	 * @return The namespace title, or null if we couldn't find it.
	 */
	protected String convert(Integer i)
	{
		return l.get(i).y;
	}
	
	/**
	 * Converts a String prefix to an integer representation of a namespace. PRECONDITION: the prefix should not start
	 * or end with ':' or contain any canonical names (e.g. COM/WP).
	 * 
	 * @param prefix The prefix to check.
	 * @return The integer number for the prefix, or throws an IllegalOperationException if the String passed in has no
	 *         associated namespace.
	 */
	protected int convert(String prefix)
	{
		String x = prefix.toLowerCase();
		if (l.containsKey(x))
			return l.get(x).x.intValue();
		else if (prefix.trim().isEmpty())
			return 0;
		else
			throw new IllegalArgumentException(String.format("'%s' is not a recognized prefix.", prefix));
	}
	
	/**
	 * Takes several prefixes and simultaneously converts them to their numerical representations.
	 * 
	 * @param prefixes The prefixes to convert, without the ":".
	 * @return The numerical representations for the prefixes, in the order passed in.
	 */
	protected String[] prefixToNumStrings(String... prefixes)
	{
		ArrayList<String> l = new ArrayList<>();
		for (String s : prefixes)
			l.add("" + convert(s));
		return l.toArray(new String[0]);
	}
	
	/**
	 * Strips the namespace from a title, if applicable.
	 * 
	 * @param title The title to strip the namespace of.
	 * @return The title, without its namespace, or the same String: if there was no namespace to strip OR if ':' was
	 *         the last character in the sequence.
	 */
	public static String nss(String title)
	{
		int i = title.indexOf(':');
		return i > 0 && i + 1 != title.length() ? title.substring(i + 1) : title;
	}
}