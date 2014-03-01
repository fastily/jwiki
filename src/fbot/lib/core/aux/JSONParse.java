package fbot.lib.core.aux;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * JSONObject parsing methods. Allows for quick searches of layered JSONObjects, and gets values.
 * 
 * @author Fastily
 * 
 */
public class JSONParse
{
	/**
	 * Peels off layers of a JSONObject using keys specified. Discards everything we peeled off the outside layers.
	 * 
	 * @param jo The object to start with
	 * @param keys The keys to peel on.
	 * @return The resulting JSONObject.
	 */
	public static JSONObject peel(JSONObject jo, String... keys)
	{
		JSONObject x = jo;
		for (String s : keys)
			x = x.getJSONObject(s);
		
		return x;
	}
	
	/**
	 * Gets the i<sup>th</sup> String in a JSONOBject.
	 * 
	 * @param jo The JSONObject to search.
	 * @param index The key at this index with which to get a String from.
	 * @return The desired value, or null if the JSONObject has no k-v pairs, or if the index is out of bounds.
	 */
	public static String getIthString(JSONObject jo, int index)
	{
		Object result = getIth(jo, index);
		return result instanceof String ? (String) result : null;
	}
	
	/**
	 * Gets the i<sup>th</sup> JSONObject in a JSONOBject.
	 * 
	 * @param jo The JSONObject to search.
	 * @param index The key at this index with which to get a JSONOBject from.
	 * @return The desired value, or null if the JSONObject has no k-v pairs, or if the index is out of bounds.
	 */
	public static JSONObject getIthJSONObject(JSONObject jo, int index)
	{
		Object result = getIth(jo, index);
		return result instanceof JSONObject ? (JSONObject) result : null;
	}
	
	/**
	 * Gets the i<sup>th</sup> int in a JSONOBject.
	 * 
	 * @param jo The JSONObject to search.
	 * @param index The key at this index with which to get an int value from.
	 * @return The desired value, or null if the JSONObject has no k-v pairs, or if the index is out of bounds.
	 */
	public static int getIthInt(JSONObject jo, int index)
	{
		Object result = getIth(jo, index);
		return result instanceof Integer ? ((Integer) result).intValue() : -1;
	}

	/**
	 * Gets the i<sup>th</sup> JSONArray in a JSONOBject.
	 * 
	 * @param jo The JSONObject to search.
	 * @param index The key at this index with which to get a JSONOBject from.
	 * @return The desired value, or null if the JSONObject has no k-v pairs, or if the index is out of bounds.
	 */
	public static JSONArray getIthJSONArray(JSONObject jo, int index)
	{
		Object result = getIth(jo, index);
		return result instanceof JSONArray ? (JSONArray) result : null;
	}
	
	
	/**
	 * Recursively search a JSONObject for a key, and return an int for the first instance of it.
	 * 
	 * @param jo The JSONObject to search.
	 * @param key The key to look for.
	 * @return The requested value, or Null if the key doesn't exist.
	 */
	public static int getIntR(JSONObject jo, String key)
	{
		Object result = getR(jo, key);
		return result instanceof Integer ? ((Integer) result).intValue() : -1;
	}
	
	/**
	 * Recursively search a JSONObject for a key, and return an String for the first instance of it.
	 * 
	 * @param jo The JSONObject to search.
	 * @param key The key to look for.
	 * @return The requested value, or Null if the key doesn't exist.
	 */
	public static String getStringR(JSONObject jo, String key)
	{
		Object result = getR(jo, key);
		return result instanceof String ? (String) result : null;
	}
	
	/**
	 * Recursively search a JSONObject for a key, and return a JSONObject for the first instance of it.
	 * 
	 * @param jo The JSONObject to search.
	 * @param key The key to look for.
	 * @return The requested value, or Null if the key doesn't exist.
	 */
	public static JSONObject getJSONObjectR(JSONObject jo, String key)
	{
		Object result = getR(jo, key);
		return result instanceof JSONObject ? (JSONObject) result : null;
	}
	
	/**
	 * Recursively search a JSONObject for a key, and return a JSONArray for the first instance of it.
	 * 
	 * @param jo The JSONObject to search.
	 * @param key The key to look for.
	 * @return The requested value, or Null if the key doesn't exist.
	 */
	public static JSONArray getJSONArrayR(JSONObject jo, String key)
	{
		Object result = getR(jo, key);
		return result instanceof JSONArray ? (JSONArray) result : null;
	}
	
	
	/**
	 * Gets the i<sup>th</sup> object in a JSONOBject.
	 * 
	 * @param jo The JSONObject to search
	 * @param index The key at this index with which to get an object from.
	 * @return The desired value, or null if the JSONObject has no k-v pairs, or if the index is out of bounds.
	 */
	private static Object getIth(JSONObject jo, int index)
	{
		String[] x = JSONObject.getNames(jo);
		if (x == null || x.length > index)
			return null;
		
		return jo.get(x[index]);
	}
	
	/**
	 * Recursively search a JSONObject for a key, and return an Object for the first instance of it.
	 * 
	 * @param jo The JSONObject to search.
	 * @param key The key to look for.
	 * @return Null if the key doesn't exist.
	 */
	private static Object getR(JSONObject jo, String key)
	{
		//System.out.println(jo);
		if (jo.has(key))
			return jo.get(key);
		
		String[] x = JSONObject.getNames(jo);
		if (x == null)
			return null;
		
		for (String s : x)
		{
			try
			{
				Object result = getR(jo.getJSONObject(s), key);
				if (result != null)
					return result;
			}
			catch (Throwable e)
			{
				//e.printStackTrace();
			}
		}
		
		return null;
	}
}