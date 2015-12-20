package jwiki.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import jwiki.util.FL;
import jwiki.util.FString;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Represents a reply from the server. A reply from a MediaWiki server is a JSONObject; this class is an extension to
 * JSONObject, and contains some pre-processing and error detecting methods.
 * 
 * @author Fastily
 *
 */
public final class Reply extends JSONObject
{

	/**
	 * The result param of the query/action, if one was returned by the server. Otherwise, this is null.
	 */
	private String result;

	/**
	 * The error code returned by the server, if one was returned. Otherwise this is null.
	 */
	private String errcode = null;

	/**
	 * Result strings which should not be tagged as errors.
	 */
	private static final ArrayList<String> whitelist = FL.toSAL("NeedToken", "Success", "Continue");

	/**
	 * Constructor, takes a JSONObject and creates a Reply from it. NB: This does not perform error checking.
	 * 
	 * @param jo The JSONObject to wrap into a Reply.
	 */
	public Reply(JSONObject jo)
	{
		super(jo.toString());
	}

	/**
	 * Constructor, takes in an InputStream and reads out the bytes to a Reply. The InputStream is closed automatically
	 * after reading is complete.
	 * 
	 * @param is The InputStream we got from the server.
	 */
	protected Reply(InputStream is)
	{
		super(FString.inputStreamToString(is));

		result = getStringR("result");

		if (has("error"))
		{
			errcode = getStringR("code");
			System.err.println("ERROR: " + getJSONObjectR("error").toString());
		}
		else if (result != null && !whitelist.contains(result))
		{
			errcode = result;
			System.err.println("ERROR: Result = " + this.toString());
		}

		if (Settings.debug)
			System.out.println(toString(2));
	}

	/**
	 * Recursively search this Reply for a key, and return an Object (if any) for the first instance of it.
	 * 
	 * @param jo The JSONObject to search.
	 * @param key The key to look for.
	 * @return The first Object associated with the specified key, or null if we didn't find the key.
	 */
	private static Object getR(JSONObject jo, String key)
	{
		if (jo.has(key))
			return jo.get(key);

		String[] x = JSONObject.getNames(jo);
		if (x == null)
			return null;

		for (String s : x)
			try
			{
				Object result = getR(jo.getJSONObject(s), key);
				if (result != null)
					return result;
			}
			catch (Throwable e)
			{
				// nobody cares
			}

		return null;
	}

	/**
	 * Recursively search this ServerReply for a key, and return it's associated value as an int.
	 * 
	 * @param key The key to look for.
	 * @return The requested value, or -1 if the key doesn't exist.
	 */
	public int getIntR(String key)
	{
		Object result = getR(this, key);
		return result instanceof Integer ? ((Integer) result).intValue() : -1;
	}

	/**
	 * Recursively search this Reply for a String, and return a String (if any) for the first instance of it.
	 * 
	 * @param key The key to look for.
	 * @return A String, or null if the key with a String doesn't exist.
	 */
	public String getStringR(String key)
	{
		Object result = getR(this, key);
		return result instanceof String ? (String) result : null;
	}

	/**
	 * Recursively search this Reply for a key, and return it's associated value (if any) as a JSONObject.
	 * 
	 * @param key The key to look for.
	 * @return A JSONObject, or null if the key with a JSONObject doesn't exist.
	 */
	public Reply getJSONObjectR(String key)
	{
		Object result = getR(this, key);
		return result instanceof JSONObject ? new Reply((JSONObject) result) : null;
	}

	/**
	 * Recursively search this Reply for a String, and return a String (if any) for the first instance of it.
	 * 
	 * @param key The key to look for.
	 * @return A JSONArray, or nil if the key with a JSONArray doesn't exist.
	 */
	public JSONArray getJSONArrayR(String key)
	{
		Object result = getR(this, key);
		return result instanceof JSONArray ? (JSONArray) result : null;
	}

	/**
	 * Recursively search this Reply for a a JSONArray and return the contained JSONObjects in an ArrayList.
	 * 
	 * @param key The key to search with.
	 * @return The JSONObjects in an ArrayList, or an empty list if we couldn't find the specified object.
	 */
	public ArrayList<Reply> getJAOfJOAsALR(String key)
	{
		ArrayList<Reply> l = new ArrayList<>();
		JSONArray ja = getJSONArrayR(key);
		if (ja == null)
			return l;
		
		for(Object o : ja)
			l.add(new Reply((JSONObject) o));
		return l;
	}

	/**
	 * Gets this Reply's error code, if applicable
	 * 
	 * @return The error code, or null if everything is okay.
	 */
	public String getErrorCode()
	{
		return errcode;
	}

	/**
	 * Checks if an error was returned by the server in this Reply.
	 * 
	 * @return True if there was an error.
	 */
	public boolean hasError()
	{
		return errcode != null;
	}

	/**
	 * Checks if we have an error, excluding those codes listed in <code>codes</code>.
	 * 
	 * @param codes The error codes to ignore. Act like there's no error.
	 * @return False if we didn't find an error.
	 */
	public boolean hasErrorIfIgnore(String... codes)
	{
		return errcode != null ? !Arrays.asList(codes).contains(errcode) : false;
	}

	/**
	 * Determines if this Reply's result code matches the specified code.
	 * 
	 * @param code The code to check against this Reply's result code.
	 * @return True if the specified code matches this Reply's result code.
	 */
	public boolean resultIs(String code)
	{
		return code.equals(result);
	}

	/**
	 * Recursively finds a JSONObject with the specified key and extracts any JSONObjects contained within. PRECONDITION: The
	 * specified JSONObject can *only* contain JSONObjects
	 * 
	 * @param key The key pointing to the top level JSONObject
	 * @return A list of JSONObjects objects contained within the JSONObject pointed to by <code>key</code>.
	 */
	public ArrayList<Reply> bigJSONObjectGet(String key)
	{
		ArrayList<Reply> jl = new ArrayList<>();

		JSONObject jo = getJSONObjectR(key);
		if (jo == null)
			return jl; // jl is empty

		String[] keys = JSONObject.getNames(jo);
		if (keys != null)
			for (String s : keys)
				jl.add(new Reply(jo.getJSONObject(s)));

		return jl;
	}
}