package fastily.jwiki.util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
	 * Default Gson object, for convenience.
	 */
	public static final Gson gson = new GsonBuilder().create();
	
	/**
	 * Gson object which generates pretty-print (human-readable) JSON.
	 */
	public static final Gson gsonPP = new GsonBuilder().setPrettyPrinting().create();
	
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
		try
		{
			return FL.toAL(FL.streamFrom(input).map(JsonElement::getAsJsonObject));
		}
		catch(Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get a JsonArray of JsonObject as a List of JsonObject.  PRECONDITION: {@code key} points to a JsonArray of JsonObject in {@code input}
	 * @param input The source JsonObject.
	 * @param key Points to a JsonArray of JsonObject
	 * @return An ArrayList of JsonObject derived from {@code input}, or an empty ArrayList on error.
	 */
	public static ArrayList<JsonObject> getJAofJO(JsonObject input, String key)
	{
		return getJAofJO(input.getAsJsonArray(key));
	}
	
	/**
	 * Extract a pair of String values from each JsonObject in an ArrayList of JsonObject
	 * @param input The source List
	 * @param kk Points to each key in to be used in the resulting Map.
	 * @param vk Points to each value in to be used in the resulting Map.
	 * @return The pairs of String values.
	 */
	public static HashMap<String, String> pairOff(ArrayList<JsonObject> input, String kk, String vk)
	{
		return new HashMap<>(input.stream().collect(Collectors.toMap(e -> gString(e, kk), e -> gString(e, vk))));
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
	 * Get a JsonArray of String objects as an ArrayList of String objects.
	 * 
	 * @param ja The source JsonArray
	 * @return The ArrayList derived from {@code ja}
	 */
	public static ArrayList<String> jaOfStrToAL(JsonArray ja)
	{
		return FL.toAL(FL.streamFrom(ja).map(JsonElement::getAsString));
	}
}