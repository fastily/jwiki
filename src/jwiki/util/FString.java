package jwiki.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
	
	
	public static String pipeFence(String...planks)
	{
		return fenceMaker("|", Arrays.asList(planks));
	}

	/**
	 * Generates a URL parameter String from a HashMap. For example, a HashMap with
	 * <code>[(someKey:someValue), (foo:baz)]</code> will result in <code>&amp;someKey=someValue&amp;foo=baz</code>.
	 * Caveat: This does not URL encode values.
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
	 * Concatenate Strings. Solution to the fencepost problem. Makes patterned Strings like "This|So|Much|Easier".
	 * 
	 * @param post The String to go between planks. Optional param, use empty string/null to disable. You can (and
	 *           should) also specify the '%n' operator in order to add new lines.
	 * @param planks The planks of the fence post problem. Posts divide planks.
	 * @return The completed fencepost string.
	 * 
	 */
	public static String fenceMaker(String post, List<String> planks)
	{
		if (planks.isEmpty())
			return "";
		else if (planks.size() == 1)
			return planks.get(0);

		String fmt = (post.isEmpty() || post == null ? "" : post) + "%s";

		String x = planks.get(0);
		for (int i = 1; i < planks.size(); i++)
			x += String.format(fmt, planks.get(i));

		return x;
	}
}