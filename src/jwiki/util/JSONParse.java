package jwiki.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;

import jwiki.core.Reply;

/**
 * Static JSON parsing methods.
 * 
 * @author Fastily
 *
 */
public class JSONParse
{
	/**
	 * Constructors disallowed
	 */
	private JSONParse()
	{

	}

	/**
	 * Extracts two String values from each JSONObject in a JSONArray. PRECONDITION: Each JSONObject has a minimum of two
	 * key-value pairs which are Strings.
	 * 
	 * @param ja The JSONArray of JSONObjects where each JSONObject has at least two keys.
	 * @param k1 The key naming the first value to fetch.
	 * @param k2 The key naming the second value to fetch.
	 * @return An ArrayList of the extracted pairs.
	 */
	public static ArrayList<Tuple<String, String>> strTuplesFromJAofJO(JSONArray ja, String k1, String k2)
	{
		ArrayList<Tuple<String, String>> l = new ArrayList<>();
		for (Object o : ja)
		{
			JSONObject jo = (JSONObject) o;
			l.add(new Tuple<>(jo.getString(k1), jo.getString(k2)));
		}

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
		for (Object o : ja)
			l.add((String) o);
		return l;
	}

	/**
	 * Groups a list of JSONObjects by a String key-value pair and collects the JSONArrays (of JSONObject) associated
	 * with that pair. Specifically, extract the <code>title</code> key and flatten any associated JSONArrays keyed with
	 * <code>arrKey</code>. Then, for each JSONObject in each JSONArray, extract a String keyed to <code>strKey</code>.
	 * This is useful for parsing multi-title queries that return results in a JSONArray.
	 * 
	 * @param srl The Reply Stream to use
	 * @param title The top-level key to group JSONArrays by
	 * @param arrKey The key of each JSONArray to select
	 * @param strKey The String key in each JSONObject in the JSONArray.
	 * @return A Map of &lt;String, ArrayList-String&gt;
	 */
	public static HashMap<String, ArrayList<String>> groupJOListByStrAndJA(Stream<Reply> srl, String title, String arrKey,
			String strKey)
	{
		// Group
		HashMap<String, ArrayList<ArrayList<String>>> ht = srl
				.collect(Collectors.groupingBy(jo -> jo.getString(title), HashMap::new, Collectors.mapping(
						jo -> extractStrFromJAOfJO(jo.getJAOfJOAsALR(arrKey), strKey), Collectors.toCollection(ArrayList::new))));

		// Flatten - this ideally should be part of group
		HashMap<String, ArrayList<String>> m = new HashMap<>();
		for (Map.Entry<String, ArrayList<ArrayList<String>>> e : ht.entrySet())
			m.put(e.getKey(), FL.flattenAL(e.getValue()));

		return m;
	}

	/**
	 * Extract Strings from a JSONArray of JSONObjects for a given key.
	 * 
	 * @param rl The Reply objects to parse
	 * @param k The key to return a String for in each visited JSONObject
	 * @return The list of Strings.
	 */
	public static ArrayList<String> extractStrFromJAOfJO(ArrayList<Reply> rl, String k)
	{
		return FL.toAL(rl.stream().map(r -> r.getString(k)));
	}
}