package jwiki.util;

import java.util.ArrayList;

import org.json.JSONArray;

import jwiki.core.Reply;

/**
 * Static JSON-parsing methods
 * 
 * @author Fastily
 *
 */
public class JSONP
{
	/**
	 * Constructors disallowed
	 */
	private JSONP()
	{

	}

	/**
	 * Extract a String value from JSONObjects for a given key.
	 * 
	 * @param rl The Reply objects to parse
	 * @param k The key to return a String for in each visited JSONObject
	 * @return A list of Strings.
	 */
	public static ArrayList<String> strFromJOs(ArrayList<Reply> rl, String k)
	{
		return FL.toAL(rl.stream().map(r -> r.getString(k)));
	}

	/**
	 * Extract a 2-String Tuple from JSONObjects for given keys.
	 * 
	 * @param rl The Reply objects to parse
	 * @param key The key to find a String for in each visited JSONObject
	 * @param valueKey The value key to find a String for in each visited JSONObject
	 * @return A list of Tuples
	 */
	public static ArrayList<Tuple<String, String>> strPairsFromJOs(ArrayList<Reply> rl, String key, String valueKey)
	{
		return FL.toAL(rl.stream().map(r -> new Tuple<>(r.getString(key), r.getString(valueKey))));
	}

	/**
	 * Collects the Strings in a JSONArray as a List.
	 * 
	 * @param ja The JSONArray to collect Strings from
	 * @return An ArrayList with all Strings in <code>ja</code>
	 */
	public static ArrayList<String> strsFromJA(JSONArray ja)
	{
		return FL.toAL(FL.streamFrom(ja).filter(o -> o instanceof String).map(o -> (String) o));
	}
}