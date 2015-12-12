package jwiki.core;

import java.util.ArrayList;
import java.util.stream.Collectors;

import jwiki.util.Tuple;

/**
 * High level functions to select and extract values from a JSONObject.  This is intended to be a sane replacement for QueryTools.
 * 
 * @author Fastily
 *
 */
public class QExtract
{
	/**
	 * Constructors disallowed
	 */
	private QExtract()
	{

	}

	/**
	 * Selects Integer, String tuples from JSONObjects in a Reply.
	 * 
	 * @param r The reply to use
	 * @param base The base key, which point to a JSONObject that contains JSONObjects
	 * @param key1 The key in each selected JSONObject whose value is the Integer
	 * @param key2 The key in each selected JSONObject whose value is the String
	 * @return A list of (Integer, String) tuples found in the list of JSONObjects
	 */
	protected static ArrayList<Tuple<Integer, String>> intStringFromJSONObjects(Reply r, String base, String key1, String key2)
	{
		return r.bigJSONObjectGet(base).stream().map(jo -> new Tuple<>(jo.getInt(key1), jo.getString(key2)))
				.collect(Collectors.toCollection(ArrayList::new));
	}
}