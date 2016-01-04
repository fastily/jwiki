package jwiki.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

	/**
	 * Puts or merges an ArrayList into a HashMap pointed to by <code>k</code>. If <code>k</code> is present in
	 * <code>hl</code>, merge <code>l</code> with the list pointed to by <code>k</code>. If <code>k</code> is not present
	 * in <code>hl</code>, then add <code>k</code> to <code>hl</code> with value <code>l</code>. Does nothing if
	 * <code>k</code> is in <code>hl</code> and <code>l</code> is empty.
	 * 
	 * @param <T1> The type in the ArrayList to merge.
	 * @param hl The HashMap to work with.
	 * @param k The key to look for in <code>hl</code>.
	 * @param l The list to put or merge if not found in <code>hl</code> with key <code>k</code>.
	 */
	public static <T1> void mapListMerge(HashMap<String, ArrayList<T1>> hl, String k, ArrayList<T1> l)
	{
		if (!hl.containsKey(k))
			hl.put(k, l);
		else if (!l.isEmpty())
			hl.get(k).addAll(l);
	}

	/**
	 * Extracts each key-value pair from a HashMap and return the pairs as an ArrayList of Tuple objects.
	 * 
	 * @param <T1> The key type of the HashMap
	 * @param <T2> The value type of the HashMap
	 * @param h The HashMap to work with
	 * @return An ArrayList of Tuples extracted from <code>h</code>.
	 */
	public static <T1, T2> ArrayList<Tuple<T1, T2>> mapToList(HashMap<T1, T2> h)
	{
		return toAL(h.entrySet().stream().map(e -> new Tuple<>(e.getKey(), e.getValue())));
	}

	/**
	 * Takes an ArrayList of ArrayList of type <code>T1</code> and condenses them into one list.
	 * 
	 * @param l The ArrayList to squash
	 * @return The squashed ArrayList.
	 */
	public static <T1> ArrayList<T1> flattenAL(ArrayList<ArrayList<T1>> l)
	{
		return toAL(l.stream().flatMap(ArrayList::stream));
	}

	/**
	 * Creates a HashMap with String keys and values. Pass in each pair and value (in that order) into <code>sl</code>.
	 * This will be one pair entered into resulting HashMap.
	 * 
	 * @param sl The list of elements to turn into a HashMap.
	 * @return The resulting HashMap, or null if you specified an odd number of elements.
	 */
	public static HashMap<String, String> pMap(String... sl)
	{
		if (sl.length % 2 == 1)
			return null;
	
		HashMap<String, String> l = new HashMap<>();
		for (int i = 0; i < sl.length; i += 2)
			l.put(sl[i], sl[i + 1]);
		return l;
	}
}