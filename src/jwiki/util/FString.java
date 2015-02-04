package jwiki.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.json.JSONArray;

/**
 * Miscellaneous String related routines I find myself using repeatedly.
 * 
 * @author Fastily
 * 
 */
public class FString
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
	public static String[] massEnc(String... strings)
	{
		ArrayList<String> l = new ArrayList<>();
		for (String s : strings)
			l.add(enc(s));

		return l.toArray(new String[0]);
	}

	/**
	 * Concatenate Strings. Solution to the fencepost problem. Makes patterned Strings like "This|So|Much|Easier".
	 * 
	 * @param post The String to go between planks. Optional param, use empty string/null to disable. You can (and
	 *           should) also specify the '%n' operator in order to add new lines.
	 * @param planks The planks of the fence post problem. Posts divide planks.
	 * @return The completed fencepost string.
	 */
	public static String fenceMaker(String post, String... planks)
	{
		if (planks.length == 0)
			return "";
		else if (planks.length == 1)
			return planks[0];

		String fmt = (post.isEmpty() || post == null ? "" : post) + "%s";

		String x = planks[0];
		for (int i = 1; i < planks.length; i++)
			x += String.format(fmt, planks[i]);

		return x;
	}

	/**
	 * Concatenate Strings. Solution to the fencepost problem. Makes patterned Strings like "This|So|Much|Easier".
	 * 
	 * @param post The String to go between planks. Optional param, use empty string/null to disable. You can (and
	 *           should) also specify the '%n' operator in order to add new lines.
	 * @param planks The planks of the fence post problem. Posts divide planks.
	 * @return The completed fencepost string.
	 */
	public static String fenceMaker(String post, ArrayList<String> planks)
	{
		return fenceMaker(post, planks.toArray(new String[0]));
	}

	/**
	 * Creates a HashMap with String keys and values. Pass in each pair and value (in that order) into <code>sl</code>.
	 * This will be one pair entered into resulting HashMap.
	 * 
	 * @param sl The list of elements to turn into a HashMap.
	 * @return The resulting HashMap, or null if you specified an odd number of elements.
	 */
	public static HashMap<String, String> makeParamMap(String... sl)
	{
		if (sl.length % 2 == 1)
			return null;

		HashMap<String, String> l = new HashMap<>();
		for (int i = 0; i < sl.length; i += 2)
			l.put(sl[i], sl[i + 1]);
		return l;
	}

	/**
	 * Converts a list of strings in a JSONArray to a list of Strings. PRECONDITION: <code>ja</code> *must* be a list of
	 * Strings or you will get strange results.
	 * 
	 * @param ja The JSONArray to get Strings from
	 * @return A list of Strings found in <code>ja</code>.
	 */
	public static ArrayList<String> jsonArrayToString(JSONArray ja)
	{
		ArrayList<String> l = new ArrayList<>();
		for (int i = 0; i < ja.length(); i++)
			l.add(ja.getString(i));

		return l;
	}

	/**
	 * Turns an array of Strings into an ArrayList of Strings.
	 * 
	 * @param strings The list of Strings to incorporate.
	 * @return The array as an ArrayList
	 */
	public static ArrayList<String> toSAL(String... strings)
	{
		return new ArrayList<>(Arrays.asList(strings));
	}
}