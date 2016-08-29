package jwiki.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Frequently used, static String functions.
 * 
 * @author Fastily
 * 
 */
public final class FString
{

	/**
	 * Constructors disallowed.
	 */
	private FString()
	{

	}

	/**
	 * Reads contents of an InputStream to a String.
	 * 
	 * @param is The InputStream to read into a String
	 * @return The String we made from the InputStream, or the empty String if something went wrong.
	 */
	public static String inputStreamToString(InputStream is)
	{
		try (BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8")))
		{
			String x = "";

			String line;
			while ((line = in.readLine()) != null)
				x += line + "\n";

			is.close();
			return x.trim();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * Capitalizes the first character of a String.  PRECONDITION: <code>s</code> is not null.
	 * 
	 * @param s The String to capitalize
	 * @return A copy of <code>s</code>, with the first character capitalized.
	 */
	public static String capitalize(String s)
	{
		return s.length() < 2 ? s.toUpperCase() : s.substring(0, 1).toUpperCase() + s.substring(1);
	}
	
	
	/**
	 * Encodes a UTF-8 String into a format valid for URLs.
	 * 
	 * @param s The String to encode
	 * @return The encoded String
	 */
	public static String enc(String s)
	{
		try
		{
			return URLEncoder.encode(s, "UTF-8");
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return s;
		}
	}

	/**
	 * URL encode each String value in a HashMap. Caveat: This mutates <code>hl</code>.
	 * 
	 * @param hl The HashMap to URL encode values for.
	 * @return The same HashMap that was passed in for convenience.
	 */
	public static HashMap<String, String> encValues(HashMap<String, String> hl)
	{
		for (String s : hl.keySet())
			hl.put(s, enc(hl.get(s)));

		return hl;
	}

	/**
	 * Generates a URL parameter String from a HashMap. For example, a HashMap with
	 * <code>[(someKey:someValue), (foo:baz)]</code> will result in <code>&amp;someKey=someValue&amp;foo=baz</code>.
	 * WARNING: This does not URL encode values.
	 * 
	 * @param hl The parameter list to generate a String for.
	 * @return The URL parameter String.
	 */
	public static String makeURLParamString(HashMap<String, String> hl)
	{
		String x = "";
		for (Map.Entry<String, String> e : hl.entrySet())
			x += String.format("&%s=%s", e.getKey(), e.getValue());

		return x;
	}

	/**
	 * Make a fence with pipe characters as posts.
	 * @param planks The planks to use, in order.
	 * @return A String with the specified planks and pipe characters as posts
	 */
	public static String pipeFence(String...planks)
	{
		return String.join("|", planks);
	}
	
	/**
	 * Makes a fence with pipe characters as posts
	 * @param planks The planks to use, in order.
	 * @return A String with the specified planks and pipe characters as postsß
	 */
	public static String pipeFence(Collection<String> planks)
	{
		return pipeFence(planks.toArray(new String[0]));
	}
}