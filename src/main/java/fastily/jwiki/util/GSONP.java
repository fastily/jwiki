package fastily.jwiki.util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * Static utility methods for use with Gson.
 * 
 * @author Fastily
 *
 */
public class GSONP
{
	/**
	 * Static JsonParser, for convenience.
	 */
	public static final JsonParser jp = new JsonParser();

	/**
	 * Type describing a HashMap with a String key and String value.
	 */
	public static Type strMapT = new TypeToken<HashMap<String, String>>() {
	}.getType();

	/**
	 * All static methods, no constructors.
	 */
	private GSONP()
	{

	}

	/**
	 * Convert a JsonObject of JsonObject to an ArrayList of JsonObject.
	 * 
	 * @param input A JsonObject containing only other JsonObject objects.
	 * @return An ArrayList of JsonObject derived from {@code input}.
	 */
	public static ArrayList<JsonObject> getJOofJO(JsonObject input)
	{
		return FL.toAL(input.entrySet().stream().map(e -> e.getValue().getAsJsonObject()));
	}

	/**
	 * Convert a JsonArray of JsonObject to an ArrayList of JsonObject.
	 * 
	 * @param input A JsonArray of JsonObject.
	 * @return An ArrayList of JsonObject derived from {@code input}.
	 */
	public static ArrayList<JsonObject> getJAofJO(JsonArray input)
	{
		return FL.toAL(StreamSupport.stream(input.spliterator(), false).map(JsonElement::getAsJsonObject));
	}

	/**
	 * Attempt to get a nested JsonObject inside {@code input}.
	 * 
	 * @param input The parent JsonObject
	 * @param keys The path to follow to access the nested JsonObject.
	 * @return The specified JsonObject or null if it could not be found.
	 */
	public static JsonObject getNestedJO(JsonObject input, List<String> keys)
	{
		JsonObject jo = input;

		try
		{
			for (String s : keys)
				jo = jo.getAsJsonObject(s);

			return jo;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Attempt to get a nested JsonArray inside {@code input}.  This means that the JsonArray is the last element in a set of nested JsonObjects.
	 * @param input The parent JsonObject
	 * @param keys The path to follow to access the nested JsonArray.
	 * @return The specified JsonArray or null if it could not be found.
	 */
	public static JsonArray getNestedJA(JsonObject input, List<String> keys)
	{
		if(keys.isEmpty())
			return new JsonArray();
		else if(keys.size() == 1)
			return input.getAsJsonArray(keys.get(0));
		
		return getNestedJO(input, keys.subList(0, keys.size()-1)).getAsJsonArray(keys.get(keys.size()-1));
	}
	

	/**
	 * Gets a String inside a nested series of JsonObjects based on the specified path.
	 * 
	 * @param input The parent/root JsonObject, as a String
	 * @param pathToString The path to the JsonObject, where each value in {@code pathToString} is a key for the next
	 *           JsonObject
	 * @param strKey The key of the actual String to fetch.
	 * @return The specified String
	 */
	public static String getStringInJO(String input, List<String> pathToString, String strKey)
	{
		return getStringInJO(jp.parse(input).getAsJsonObject(), pathToString, strKey);
	}

	/**
	 * Gets a String inside a nested series of JsonObjects based on the specified path.
	 * 
	 * @param input The parent/root JsonObject
	 * @param pathToString The path to the JsonObject, where each value in {@code pathToString} is a key for the next
	 *           JsonObject
	 * @param strKey The key of the actual String to fetch.
	 * @return The specified String
	 */
	public static String getStringInJO(JsonObject input, List<String> pathToString, String strKey)
	{
		return getNestedJO(input, pathToString).get(strKey).getAsString();
	}

	/**
	 * Gets a List of Strings from a JsonArray of JsonObject where each JsonObject has a String with the same key.
	 * 
	 * @param ja The parent JsonArray
	 * @param strKey The String key in each JsonObject of the JsonArray.
	 * @return The List of Strings recovered.
	 */
	public static ArrayList<String> getStrsFromJAofJO(JsonArray ja, String strKey)
	{
		return FL.toAL(getJAofJO(ja).stream().map(e -> gString(e, strKey)));
	}

	/**
	 * Get a String from a JsonObject. Returns null if a value for {@code key} was not found.
	 * 
	 * @param jo The JsonObject to look for {@code key} in
	 * @param key The key to look for
	 * @return The value associated with {@code key} as a String, or null if the {@code key} could not be found.
	 */
	public static String gString(JsonObject jo, String key)
	{
		return jo.has(key) ? jo.get(key).getAsString() : null;
	}

	/**
	 * Creates a Stream from a JsonArray
	 * 
	 * @param ja The JsonArray to derive a Stream from
	 * @return A Stream created from {@code ja}
	 */
	public static Stream<JsonElement> jaToStream(JsonArray ja)
	{
		return StreamSupport.stream(ja.spliterator(), false);
	}

	/**
	 * Get a JsonArray of String objects as an ArrayList of String objects.
	 * 
	 * @param ja The source JsonArray
	 * @return The ArrayList derived from {@code ja}
	 */
	public static ArrayList<String> jaOfStrToAL(JsonArray ja)
	{
		return FL.toAL(jaToStream(ja).map(JsonElement::getAsString));
	}
}