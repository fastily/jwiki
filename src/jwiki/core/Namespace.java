package jwiki.core;

import java.util.ArrayList;
import java.util.HashMap;

import jwiki.core.aux.Tuple;

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
	 * The back-end storage system for namespace.
	 */
	private HashMap<Object, Tuple<Integer, String>> l = new HashMap<Object, Tuple<Integer, String>>();
	
	/**
	 * Private constructor, used by makeNamespace()
	 */
	private Namespace()
	{
		
	}
	
	/**
	 * Makes a namepsace object with a given JSONObject from the server.
	 * 
	 * @param jo JSONObject from the server.
	 * @return The Namespace.
	 */
	protected static Namespace makeNamespace(JSONObject jo)
	{
		
		Namespace ns = new Namespace();
		for (String s : JSONObject.getNames(jo))
		{
			JSONObject curr = jo.getJSONObject(s);
			String name = curr.getString("*");
			if (name.isEmpty()) // omit empty string, which is actually Main.
				continue;
			Integer id = new Integer(curr.getInt("id"));
			
			Tuple<Integer, String> t = new Tuple<Integer, String>(id, name);
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
		if (l.containsKey(i))
			return l.get(i).y;
		return i.intValue() == 0 ? "Main" : null; // edge case & error
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
	protected String prefixToNumString(String prefix)
	{
		return "" + convert(prefix);
	}
	
	
	/**
	 * Takes several prefixes and simultaenously converts them to their numerical representations.
	 * 
	 * @param prefixes The prefixes to convert, without the ":".
	 * @return The numerical representations for the prefixes, in the order passed in.
	 */
	protected String[] prefixToNumStrings(String... prefixes)
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