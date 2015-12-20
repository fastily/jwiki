package jwiki.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Static Collections and Stream utilities.
 * 
 * @author Fastily
 *
 */
public class FL
{
	/**
	 * Constructors disallowed
	 */
	private FL()
	{

	}

	/**
	 * Collects the elements of a Stream into an ArrayList.
	 * 
	 * @param s The target Stream
	 * @return An ArrayList containing the elements of the <code>s</code>
	 */
	public static <T1> ArrayList<T1> toAL(Stream<T1> s)
	{
		return s.collect(Collectors.toCollection(ArrayList::new));
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