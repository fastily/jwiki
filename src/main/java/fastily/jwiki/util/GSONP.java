package fastily.jwiki.util;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.HttpUrl;

/**
 * Static utility methods for use with Gson.
 * 
 * @author Fastily
 *
 */
public class GSONP
{
	/**
	 * Default json deserializer for Instant objects.
	 */
	private static JsonDeserializer<Instant> instantDeserializer = (j, t, c) -> Instant.parse(j.getAsJsonPrimitive().getAsString());

	/**
	 * Default json deserializer for HttpUrl objects.
	 */
	private static JsonDeserializer<HttpUrl> httpurlDeserializer = (j, t, c) -> HttpUrl.parse(j.getAsJsonPrimitive().getAsString());

	/**
	 * Static JsonParser, for convenience.
	 */
	public static final JsonParser jp = new JsonParser();

	/**
	 * Default Gson object, for convenience.
	 */
	public static final Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, instantDeserializer)
			.registerTypeAdapter(HttpUrl.class, httpurlDeserializer).create();

	/**
	 * Gson object which generates pretty-print (human-readable) JSON.
	 */
	public static final Gson gsonPP = new GsonBuilder().setPrettyPrinting().create();

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
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get a JsonArray of JsonObject as a List of JsonObject. PRECONDITION: {@code key} points to a JsonArray of
	 * JsonObject in {@code input}
	 * 
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
	 * 
	 * @param input The source List
	 * @param kk Points to each key in to be used in the resulting Map.
	 * @param vk Points to each value in to be used in the resulting Map.
	 * @return The pairs of String values.
	 */
	public static HashMap<String, String> pairOff(ArrayList<JsonObject> input, String kk, String vk)
	{
		return new HashMap<>(input.stream().collect(Collectors.toMap(e -> getStr(e, kk), e -> getStr(e, vk))));
	}

	/**
	 * Performs a nested JO lookup for the specified path to see if it exists.
	 * 
	 * @param jo The JsonObject to check.
	 * @param keys The key path to follow.
	 * @return True if the path specified by {@code keys} exists, or false otherwise.
	 */
	public static boolean nestedHas(JsonObject jo, List<String> keys)
	{
		JsonObject last = jo;

		try
		{
			for (int i = 0; i < keys.size() - 1; i++)
				last = last.getAsJsonObject(keys.get(i));
		}
		catch (Throwable e)
		{
			return false;
		}

		return last.has(keys.get(keys.size() - 1));
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
	 * Attempt to get a nested JsonArray inside {@code input}. This means that the JsonArray is the last element in a set
	 * of nested JsonObjects.
	 * 
	 * @param input The parent JsonObject
	 * @param keys The path to follow to access the nested JsonArray.
	 * @return The specified JsonArray or null if it could not be found.
	 */
	public static JsonArray getNestedJA(JsonObject input, List<String> keys)
	{
		if (keys.isEmpty())
			return new JsonArray();
		else if (keys.size() == 1)
			return input.getAsJsonArray(keys.get(0));

		return getNestedJO(input, keys.subList(0, keys.size() - 1)).getAsJsonArray(keys.get(keys.size() - 1));
	}

	/**
	 * Get a String from a JsonObject. Returns null if a value for {@code key} was not found.
	 * 
	 * @param jo The JsonObject to look for {@code key} in
	 * @param key The key to look for
	 * @return The value associated with {@code key} as a String, or null if the {@code key} could not be found.
	 */
	public static String getStr(JsonObject jo, String key)
	{
		if (!jo.has(key))
			return null;

		JsonElement e = jo.get(key);
		return e.isJsonPrimitive() ? e.getAsString() : null;
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