package jwiki.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import jwiki.core.aux.Tuple;

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
		
		return l.toArray(new String[0]);
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
	 * Makes a regex for replacing titles/files on a page. Converts regex operators to their escaped counterparts.
	 * 
	 * @param title The title to convert into a regex.
	 * @return The regex.
	 */
	public static String makePageTitleRegex(String title)
	{
		String temp = new String(title);
		for (String s : new String[] { "(", ")", "[", "]", "{", "}", "^", "-", "=", "$", "!", "|", "?", "*", "+", ".", "<", ">" })
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
}