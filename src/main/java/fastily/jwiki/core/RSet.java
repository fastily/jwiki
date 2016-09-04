package fastily.jwiki.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fastily.jwiki.util.FL;
import fastily.jwiki.util.JSONP;
import fastily.jwiki.util.Tuple;

/**
 * High level functions to select and extract values from a list Reply objects. Each RSet is basically just a list of
 * replies from the server. This is intended to be a sane replacement for QueryTools.
 * 
 * @author Fastily
 *
 */
public final class RSet
{
	/**
	 * The backing ArrayList
	 */
	protected final ArrayList<Reply> rl;

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
	 * Selects, for a given key, a JSONArray of JSONObjects, and collects the JSONObjects. Example usage: Assist with
	 * <code>Revision</code> and <code>Contrib</code>.
	 * 
	 * @param base The key pointing to the JSONArray to select.
	 * @return A Stream of collected JSONObjects
	 */
	private Stream<Reply> getJAofJOStream(String base)
	{
		return rl.stream().flatMap(r -> r.getJAofJO(base).stream());
	}

	/**
	 * Selects, for a given key, a JSONArray of JSONObjects, and collects the JSONObjects. Example usage: Assist with
	 * <code>Revision</code> and <code>Contrib</code>.
	 * 
	 * @param base The key pointing to the JSONArray to select.
	 * @return An ArrayList of collected JSONObjects.
	 */
	protected ArrayList<Reply> getJAofJO(String base)
	{
		return FL.toAL(getJAofJOStream(base));
	}

	/**
	 * Selects, for a given key, a JSONArray of JSONObjects, collects the JSONObjects, and maps each JSONObject based on
	 * a mapping function.
	 * 
	 * @param <T1> The type of Object contained in the ArrayList returned.
	 * 
	 * @param base The key pointing to the JSONArray to select.
	 * @param mapper The mapping function to apply to each JSONObject found in the JSONArray pointed to by
	 *           <code>base</code>
	 * @return An ArrayList of objects created from the JSONArray of JSONObjects.
	 */
	protected <T1> ArrayList<T1> getJAofJOas(String base, Function<? super Reply, ? extends T1> mapper)
	{
		return FL.toAL(getJAofJOStream(base).map(mapper));
	}

	/**
	 * Takes a JSONArray of JSONObjects and maps two values in each JSONObhject to a Map. This is NOT subject to
	 * auto-normalization.
	 * 
	 * @param The type of Object associated with each key in the HashMap returned.
	 * 
	 * @param base The key pointing to the root JSONArray of JSONObjects
	 * @param keyMapper The key mapping function to apply to each JSONObject to extract a value for the Map
	 * @param valueMapper The value mapping function to apply to each JSONObject to extract a value for the Map
	 * @return A Map derived from the JSONArray of JSONObjects.
	 */
	protected <T1> HashMap<String, T1> getJAofJOasMapWith(String base, Function<Reply, String> keyMapper,
			Function<Reply, T1> valueMapper)
	{
		return FL.toHM(getJAofJOStream(base), keyMapper, valueMapper);
	}

	/**
	 * Selects, for a given key, a String value from each JSONObject in a JSONAray. Example usage:
	 * <code>categorymembers</code>.
	 * 
	 * @param base The key pointing to the JSONArray in each Reply of the RSet.
	 * @param title The title to select in each JSONObject.
	 * @return The list of selected strings.
	 */
	protected ArrayList<String> getJAOfJOasStr(String base, String title)
	{
		return JSONP.strsFromJOs(getJAofJO(base), title);
	}

	/**
	 * Retrieves JSONObjects within a JSONObject of JSONObjects in a Stream.
	 * 
	 * @param base The key pointing to the JSONObject to retrieve JSONObjects from.
	 * @return A Stream with JSONObjects found in the JSONObject for <code>key</code>
	 */
	private Stream<Reply> getJOofJOStream(String base)
	{
		return rl.stream().flatMap(r -> r.getJOofJO(base).stream());
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
	 * Takes a JSONObject of JSONObjects and maps two values in each JSONObhject to a Map.
	 * 
	 * @param base The key pointing to the root JSONObject of JSONObjects
	 * @param keyMapper The key mapping function to apply to each JSONObject to extract a value for the Map
	 * @param valueMapper The value mapping function to apply to each JSONObject to extract a value for the Map
	 * @return A Map derived from the JSONObject of JSONObjects.
	 */
	protected <T1> HashMap<String, T1> getJOofJOasMapWith(String base, Function<Reply, String> keyMapper,
			Function<Reply, T1> valueMapper)
	{
		return normalize(FL.toHM(getJOofJOStream(base), keyMapper, valueMapper));
	}

	/**
	 * Groups JSONObjects by a shared title key, and collects a specific String from each JSONObject in the specified
	 * JSONArray contained in each JSONObject from <code>srl</code>. Specifically, extract the <code>title</code> key and
	 * flatten any associated JSONArrays keyed with <code>arrKey</code>. Then, for each JSONObject in each JSONArray,
	 * extract a String keyed to <code>strKey</code>. This is useful for parsing multi-title queries that return results
	 * in a JSONArray.
	 * 
	 * @param base The key pointing to each JSONObject with JSONObjects to act on.
	 * @param title The top-level key to group JSONArrays by
	 * @param arrKey The key of each JSONArray to select
	 * @param strKey The String key in each JSONObject in the JSONArray.
	 * @return A HashMap of [ String : ArrayList&lt;String&gt; ]
	 */
	protected HashMap<String, ArrayList<String>> groupJOListByStrAndJA(String base, String title, String arrKey, String strKey)
	{
		return normalize(getJOofJOStream(base).collect(Collectors.groupingBy(jo -> jo.getString(title), HashMap::new, Collectors
				.mapping(jo -> JSONP.strsFromJOs(jo.getJAofJO(arrKey), strKey), Collector.of(ArrayList::new, ArrayList::addAll, (x, y) -> {
					x.addAll(y);
					return x;
				})))));
	}

	/**
	 * Groups JSONObjects by a shared title key, and collects a specific Tuple of (String, String) from each JSONObject
	 * in the specified JSONArray contained in each JSONObject from <code>srl</code>. Specifically, extract the
	 * <code>title</code> key and flatten any associated JSONArrays keyed with <code>arrKey</code>. Then, for each
	 * JSONObject in each JSONArray, extract a String keyed to <code>strKey</code> and value keyed to <code>strVal</code>
	 * into a Tuple. This is useful for parsing multi-title queries that return results in a JSONArray.
	 * 
	 * @param base The key pointing to each JSONObject with JSONObjects to act on.
	 * @param title The top-level key to group JSONArrays by
	 * @param arrKey The key of each JSONArray to select
	 * @param strKey The key String (for the resulting Tuple) to get in each JSONObject in the JSONArray.
	 * @param strVal The value String (for the resulting Tuple) to get in each JSONObject in the JSONArray.
	 * @return The specified HashMap
	 */
	protected HashMap<String, ArrayList<Tuple<String, String>>> groupJOListByStrAndJAPair(String base, String title, String arrKey,
			String strKey, String strVal)
	{
		return normalize(getJOofJOStream(base).collect(Collectors.groupingBy(jo -> jo.getString(title), HashMap::new,
				Collectors.mapping(jo -> JSONP.strPairsFromJOs(jo.getJAofJO(arrKey), strKey, strVal),
						Collector.of(ArrayList::new, ArrayList::addAll, (x, y) -> {
							x.addAll(y);
							return x;
						})))));
	}

	/**
	 * Passively/Lazily applies title normalization when it is auto-applied by MediaWiki. MediaWiki will return a
	 * <code>normalized</code> JSONArray when it fixes lightly malformed titles. jwiki library functions expect clients
	 * to verify that titles are not malformed before being passed as parameters, so this helps to improve robustness.
	 * 
	 * @param m The Map to normalize against <code>rl</code>.
	 * @return The Map, <code>m</code>.
	 */
	private <T1> HashMap<String, T1> normalize(HashMap<String, T1> m)
	{
		getJAofJOasMapWith("normalized", r -> r.getString("to"), r -> r.getString("from")).entrySet().stream()
				.forEach(e -> m.put(e.getValue(), m.get(e.getKey())));

		return m;
	}
}