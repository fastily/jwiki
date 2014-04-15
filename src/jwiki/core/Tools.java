package jwiki.core;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Miscellaneous tools to assist with MediaWiki API queries & responses.
 * 
 * @author Fastily
 * 
 */
public class Tools
{
	/**
	 * Encode the UTF-8 String into a format valid for URLs.
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
	 * URLEncodes multiple Strings at once.
	 * 
	 * @param strings Strings to encode
	 * @return A list of Strings, URLEncoded, in the same order they were passed in.
	 */
	protected static String[] massEnc(String... strings)
	{
		ArrayList<String> l = new ArrayList<String>();
		for (String s : strings)
			l.add(enc(s));
		
		return l.toArray(new String[0]);
	}
	
	/**
	 * Reads contents of an InputStream to a String.
	 * 
	 * @param is The InputStream to read to File
	 * @param close Set to true to close the InputStream after we're done.
	 * @return The String we made from the InputStream, or the empty String if something went wrong.a
	 */
	public static String inputStreamToString(InputStream is, boolean close)
	{
		try
		{
			String x = "";
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			
			String line;
			while ((line = in.readLine()) != null)
				x += line + "\n";
			
			if (close)
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
	 * Capitalizes the first character of a String
	 * 
	 * @param s The String to work with
	 * @return A new copy of the passed in String, with the first character capitalized.
	 */
	public static String capitalize(String s)
	{
		return s.length() < 2 ? s.toUpperCase() : s.substring(0, 1).toUpperCase() + s.substring(1);
	}
	
	/**
	 * Solution to the fencepost problem. Makes patterned Strings like "This|So|Much|Easier".
	 * 
	 * @param post The String to go between planks.
	 * @param planks The planks of the fence post problem. Posts divide planks.
	 * @return The completed fencepost string.
	 */
	public static String fenceMaker(String post, String... planks)
	{
		if (planks.length == 0)
			return "";
		
		String x = planks[0];
		for (int i = 1; i < planks.length; i++)
			x += post + planks[i];
		
		return x;
	}
	
	/**
	 * Splits an array of Strings into an array of array of Strings.
	 * 
	 * @param max The maximum number of elements per array.
	 * @param strings The list of Strings to split.
	 * @return The split array of String[]s.
	 */
	public static String[][] splitStringArray(int max, String... strings)
	{
		ArrayList<String[]> l = new ArrayList<String[]>();
		
		if (strings.length <= max)
			return new String[][] { strings };
		
		int overflow = strings.length % max;
		for (int i = 0; i < strings.length - overflow; i += max)
			l.add(Arrays.copyOfRange(strings, i, i + max));
		
		if (overflow > 0)
			l.add(Arrays.copyOfRange(strings, strings.length - overflow, strings.length));
		
		return l.toArray(new String[0][]);
	}
	
	/**
	 * Creates a HashMap with keys as String, and Objects as values. Pass in each pair and value (in that order) into
	 * <tt>ol</tt>. This will be one pair entered into resulting HashMap.
	 * 
	 * @param ol The list of elements to turn into a HashMap.
	 * @return The resulting HashMap, or null if you specified an odd number of elements.
	 */
	protected static HashMap<String, Object> makeParamMap(Object... ol)
	{
		HashMap<String, Object> l = new HashMap<String, Object>();
		
		if (ol.length % 2 == 1)
			return null;
		
		for (int i = 0; i < ol.length; i += 2)
			if (ol[i] instanceof String)
				l.put((String) ol[i], ol[i + 1]);
		
		return l;
	}
	
	/**
	 * Close an InputStream without generating exceptions.
	 * @param is The InputStream to close.
	 * @return True if we closed it successfully.
	 */
	public static boolean closeInputStream(InputStream is)
	{
		try
		{
			is.close();
			return true;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return false;
		}
		
	}
}