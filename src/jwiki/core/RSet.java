package jwiki.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;

import jwiki.util.FL;
import jwiki.util.Tuple;

/**
 * High level functions to select and extract values from a list Reply objects. Each RSet is basically just a list of
 * replies from the server. This is intended to be a sane replacement for QueryTools.
 * 
 * @author Fastily
 *
 */
public class RSet
{
	private ArrayList<Reply> rl;

	/**
	 * Constructor, creates an RSet with the specified ArrayList of Reply objects.
	 * 
	 * @param rl The ArrayList to create the RSet with.
	 */
	protected RSet(ArrayList<Reply> rl)
	{
		this.rl = rl;
	}

	/**
	 * Constructor, creates an empty RSet.
	 */
	protected RSet()
	{
		this(new ArrayList<>());
	}

	/**
	 * Merges the specified RSet into this RSet.
	 * 
	 * @param rs The RSet to merge into this RSet.
	 */
	protected void merge(RSet rs)
	{
		rl.addAll(rs.rl);
	}

	/**
	 * Selects an &lt;Integer, String&gt; from JSONObjects in a Reply. Example usage: <code>siteinfo</code> →
	 * <code>namespace</code>.
	 * 
	 * @param base The key pointing to the JSONObject that contains JSONObjects in each Reply of the RSet.
	 * @param key1 The key in each selected JSONObject whose value is the Integer
	 * @param key2 The key in each selected JSONObject whose value is the String
	 * @return A list of &lt;Integer, String&gt; found in the list of JSONObjects
	 */
	protected ArrayList<Tuple<Integer, String>> intStringFromJO(String base, String key1, String key2)
	{
		return FL.toAL(
				rl.stream().flatMap(r -> r.bigJSONObjectGet(base).stream()).map(jo -> new Tuple<>(jo.getInt(key1), jo.getString(key2))));
	}

	/**
	 * Selects, for a given key, a JSONArray of JSONObjects, and collects the JSONObjects. Example usage: Assist with
	 * <code>Revision</code> and <code>Contrib</code>.
	 * 
	 * @param base The key pointing to the JSONArray to select.
	 * @return A Stream of collected JSONObjects
	 */
	protected Stream<Reply> getJOofJAStream(String base)
	{
		return rl.stream().flatMap(r -> r.getJAOfJOAsALR(base).stream());
	}

	/**
	 * Selects, for a given key, a String value from each JSONObject in a JSONAray. Example usage:
	 * <code>categorymembers</code>.
	 * 
	 * @param base The key pointing to the JSONArray in each Reply of the RSet.
	 * @param title The title to select in each JSONObject.
	 * @return The list of selected strings.
	 */
	protected ArrayList<String> stringFromJAOfJO(String base, String title)
	{
		return FL.toAL(getJOofJAStream(base).map(jo -> jo.getString(title)));
	}

	/**
	 * Selects, for a given key, a JSONArray of JSONObjects, and collects the JSONObjects. Example usage: Assist with
	 * <code>Revision</code> and <code>Contrib</code>.
	 * 
	 * @param base The key pointing to the JSONArray to select.
	 * @return An ArrayList of collected JSONObjects.
	 */
	protected ArrayList<Reply> getJOofJA(String base)
	{
		return FL.toAL(getJOofJAStream(base));
	}

	/**
	 * Retrieves JSONObjects within a JSONObject of JSONObjects in a Stream.
	 * 
	 * @param base The key pointing to the JSONObject to retrieve JSONObjects from.
	 * @return A Stream with JSONObjects found in the JSONObject for <code>key</code>
	 */
	protected Stream<Reply> getJOofJOStream(String base)
	{
		return rl.stream().flatMap(r -> r.bigJSONObjectGet(base).stream());
	}

	/**
	 * Retrieves JSONObjects within a JSONObject of JSONObjects in a Stream.
	 * 
	 * @param base The key pointing to the JSONObject to retrieve JSONObjects from.
	 * @return An ArrayList with JSONObjects found in the JSONObject for <code>key</code>
	 */
	protected ArrayList<Reply> getJOofJO(String base)
	{
		return FL.toAL(getJOofJOStream(base));
	}

	/**
	 * Converts a list of strings in a JSONArray to a list of Strings. PRECONDITION: <code>ja</code> *must* be a list of
	 * Strings or you will get strange results.
	 * 
	 * @param ja The JSONArray to get Strings from
	 * @return A list of Strings found in <code>ja</code>.
	 */
	protected static ArrayList<String> jaToString(JSONArray ja)
	{
		ArrayList<String> l = new ArrayList<>();
		for (Object o : ja)
			l.add((String) o);
		return l;
	}

	/**
	 * Extract Strings from a JSONArray of JSONObjects for a given key.
	 * 
	 * @param rl The Reply objects to parse
	 * @param k The key to return a String for in each visited JSONObject
	 * @return The list of Strings.
	 */
	protected static ArrayList<String> extractStrFromJAOfJO(ArrayList<Reply> rl, String k)
	{
		return FL.toAL(rl.stream().map(r -> r.getString(k)));
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
	protected static HashMap<String, ArrayList<String>> groupJOListByStrAndJA(Stream<Reply> srl, String title, String arrKey,
			String strKey)
	{
		return srl.collect(Collectors.groupingBy(jo -> jo.getString(title), HashMap::new, Collectors.mapping(
				jo -> extractStrFromJAOfJO(jo.getJAOfJOAsALR(arrKey), strKey), Collector.of(ArrayList::new, ArrayList::addAll, (x, y) -> {
					x.addAll(y);
					return x;
				}))));
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
	protected static HashMap<String, String> strTuplesFromJAofJO(JSONArray ja, String k1, String k2)
	{
		HashMap<String, String> l = new HashMap<>();
		for (Object o : ja)
		{
			JSONObject jo = (JSONObject) o;
			l.put(jo.getString(k1), jo.getString(k2));
		}

		return l;
	}
}