package fbot.lib.util;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

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
	 * @param file The WikiFile to generate a random filename for.
	 * @return The random wiki-uploadable file name
	 */
	public static String generateRandomFileName(WikiFile file)
	{
		return String.format("%#o x %s.%s", r.nextInt(0xFF), new SimpleDateFormat("HH.mm.ss").format(new Date()),
				file.getExtension(false));
	}
	
	/**
	 * Splits a String in half and returns a part of it. Only splits on the first or last occurrence of <tt>delim</tt>.
	 * 
	 * @param s The String to split.
	 * @param delim The split token to use. The String will be split on this String.
	 * @param first Set to true to split at the first occurrence of <tt>delim</tt>
	 * @param front Set to true to get the first half of the split, e.g. array[0].
	 * @return
	 */
	public static String splitGrab(String s, String delim, boolean first, boolean front)
	{
		int pos = first ? s.indexOf(delim) : s.lastIndexOf(delim);
		if (pos == -1)
			return s;
		return front ? s.substring(0, pos) : s.substring(pos + 1);
	}
	
	/**
	 * Takes a list of Strings and concatenates it into a single string. Each item in the list separated by the system
	 * default newline character.
	 * 
	 * @param list The list of files to concatenate
	 * @return The concatenated Strings, separated by newlines.
	 */
	public static String listCombo(String... list)
	{
		String x = "";
		for (String s : list)
			x += s + FSystem.lsep;
		return x;
	}
	
	/**
	 * Concatenate a list of Strings into one String. Will be concatenated in the order they appear.
	 * 
	 * @param strings The Strings to concatenate.
	 * @return The list of Strings as one String.
	 */
	public static String concatStringArray(String... strings)
	{
		String x = "";
		for (String s : strings)
			x += s;
		return x;
	}
	
	/**
	 * Determines if two String arrays share elements.
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
	 * @param a List 1
	 * @param b List 2
	 * @return True if the Lists intersect.
	 */
	public static boolean arraysIntersect(List<String> a, List<String> b)
	{
		for(String s : a)
			if(b.contains(s))
				return true;
		return false;
	}
}