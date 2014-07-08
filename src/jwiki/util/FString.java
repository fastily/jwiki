package jwiki.util;

import java.net.URLEncoder;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Contains personalized String related methods for my bot programs.
 * 
 * @author Fastily
 * 
 */
public class FString
{
	/**
	 * Static random object for methods that require a random object.
	 */
	private static Random r = new Random();

	/**
	 * Constructors disallowed.
	 */
	private FString()
	{
		
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
	 * Generates a random file name for upload to wiki based on the entered file name
	 * 
	 * @param p The Path to generate a random filename for.
	 * @return The random wiki-uploadable file name
	 */
	public static String generateRandomFileName(Path p)
	{
		return String.format("%#o x %s.%s", r.nextInt(0xFF), LocalTime.now().format(DateTimeFormatter.ofPattern("HH.mm.ss")),
				FIO.getExtension(p, false)); 
	}

	/**
	 * Splits a long string into a list of strings using newline chars as deliminators.
	 * 
	 * @param longstring The string to split
	 * @return A list of strings.
	 */
	public static String[] splitCombo(String longstring)
	{
		ArrayList<String> l = new ArrayList<String>();
		Scanner m = new Scanner(longstring);

		while (m.hasNextLine())
			l.add(m.nextLine().trim());

		m.close();
		return l.toArray(new String[0]);
	}

	/**
	 * Determines if two String arrays share elements.
	 * 
	 * @param a Array 1
	 * @param b Array 2
	 * @return True if the arrays intersect.
	 */
	public static boolean arraysIntersect(String[] a, String[] b)
	{
		return arraysIntersect(Arrays.asList(a), Arrays.asList(b));
	}

	/**
	 * Determines if two String Lists share elements.
	 * 
	 * @param a List 1
	 * @param b List 2
	 * @return True if the Lists intersect.
	 */
	public static boolean arraysIntersect(List<String> a, List<String> b)
	{
		for (String s : a)
			if (b.contains(s))
				return true;
		return false;
	}

	/**
	 * Determines if a String array contains an <tt>item</tt>.
	 * 
	 * @param a The array to check
	 * @param item The item to look for
	 * @return True if we found <tt>item</tt> in <tt>a</tt>.
	 */
	public static boolean arrayContains(String[] a, String item)
	{
		return Arrays.asList(a).contains(item);
	}

	/**
	 * Makes a regex for replacing titles/files on a page. Converts regex operators to their escaped counterparts.
	 * 
	 * @param title The title to convert into a regex.
	 * @return The regex.
	 */
	public static String makePageTitleRegex(String title)
	{
		String temp = new String(title);
		for (String s : new String[] { "(", ")", "[", "]", "{", "}", "^", "-", "=", "$", "!", "|", "?", "*", "+", ".", "<",
				">" })
			temp = temp.replace(s, "\\" + s);
		temp = temp.replaceAll("( |_)", "( |_)");
		return temp;
	}

	/**
	 * Splits a string on a deliminator. The deliminator is omitted. PRECONDITION: There must be a delim present, and at
	 * least one character after the end of the first instance of the deliminator.
	 * 
	 * @param s The string to split
	 * @param delim The delim to use
	 * @return A tuple representing the object split.
	 */
	public static Tuple<String, String> splitOn(String s, String delim)
	{
		String s1 = s.substring(0, s.indexOf(delim));
		String s2 = s.substring(s.indexOf(delim) + delim.length());
		return new Tuple<String, String>(s1, s2);
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
		ArrayList<String> l = new ArrayList<String>();
		for (String s : strings)
			l.add(enc(s));

		return l.toArray(new String[0]);
	}

	/**
	 * Concatenate Strings. Solution to the fencepost problem. Makes patterned Strings like "This|So|Much|Easier".
	 * 
	 * @param post The String to go between planks. Optional param, use empty string/null to disable. You can (and
	 *           shoudl) also specify the '%n' operator in order to add new lines.
	 * @param planks The planks of the fence post problem. Posts divide planks.
	 * @return The completed fencepost string.
	 */
	public static String fenceMaker(String post, String... planks)
	{
		if (planks.length == 0)
			return "";
		else if(planks.length == 1)
			return planks[0];

		String fmt = (post.isEmpty() || post == null ? "" : post) + "%s";

		String x = planks[0];
		for (int i = 1; i < planks.length; i++)
			x += String.format(fmt, planks[i]);

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
	 * Extract from a list of tuples, all String values from a &lt;String, Boolean&gt; where Boolean == value.
	 * 
	 * @param bl The list of tuples to look at
	 * @param value We'll extract a String with this value.
	 * @return A list of matching strings.
	 */
	public static String[] booleanTuple(List<Tuple<String, Boolean>> bl, boolean value)
	{
		ArrayList<String> l = new ArrayList<String>();
		for (Tuple<String, Boolean> t : bl)
			if (t.y.booleanValue() == value)
				l.add(t.x);
		return l.toArray(new String[0]);
	}
	
	/**
	 * Creates a HashMap with String keys and values. Pass in each pair and value (in that order) into
	 * <tt>sl</tt>. This will be one pair entered into resulting HashMap.
	 * 
	 * @param sl The list of elements to turn into a HashMap.
	 * @return The resulting HashMap, or null if you specified an odd number of elements.
	 */
	public static HashMap<String, String> makeParamMap(String... sl)
	{
		if (sl.length % 2 == 1)
			return null;
		
		HashMap<String, String> l = new HashMap<String, String>();
		for (int i = 0; i < sl.length; i += 2)
			l.put(sl[i], sl[i + 1]);
		return l;
	}
}