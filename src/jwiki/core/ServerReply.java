package jwiki.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jwiki.util.FIO;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Represents a reply from the server. A reply from a MediaWiki server is a JSONObject; this class is an extension to
 * JSONObject, and contains some preprocessing and error detecting methods.
 * 
 * @author Fastily
 *
 */
public class ServerReply extends JSONObject
{

	/**
	 * The result of the query/action, if applicable.
	 */
	private String result;

	/**
	 * A specific error code that we recieved.
	 */
	private String errcode = null;

	/**
	 * Result strings which should not be tagged as errors.
	 */
	private static final List<String> whitelist = Arrays.asList(new String[] { "NeedToken", "Success", "Continue" });

	
	/**
	 * Constructor, takes a JSONObject and creates a ServerReply from it.
	 * @param jo The JSONObject to turn into a ServerReply.
	 */
	protected ServerReply(JSONObject jo)
	{
		super(jo.toString());
	}
	
	
	/**
	 * Constructor, takes in an inputstream and reads out the bytes to a ServerReply. The inputstream is closed
	 * automatically after reading is complete.
	 * 
	 * @param is The inputstream we got from the server.
	 */
	protected ServerReply(InputStream is)
	{
		super(FIO.inputStreamToString(is, true));
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
			System.out.println(this);
	}
	
	
	/*
	 * public ServerReply(String is) { super(is); result = getStringR("result");
	 * 
	 * if (has("error")) { errcode = getStringR("code"); System.err.println("ERROR: " +
	 * getJSONObjectR("error").toString()); } else if (result != null && !whitelist.contains(result)) { errcode = result;
	 * System.err.println("ERROR: Result = " + this.toString()); }
	 * 
	 * if (Settings.debug) System.out.println(this); }
	 */

	/**
	 * Recursively search this ServerReply for a key, and return an Object for the first instance of it.
	 * 
	 * @param jo The JSONObject to search.
	 * @param key The key to look for.
	 * @return Null if the key doesn't exist.
	 */
	private static Object getR(JSONObject jo, String key)
	{
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
				// nobody cares
			}
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
	 * Recursively search this ServerReply for a key, and return it's associated value (which was originally an int) as a
	 * String.
	 * 
	 * @param key The key to look for.
	 * @return The requested value, or "-1" if the key doesn't exist.
	 */
	public String getIntRAsString(String key)
	{
		return "" + getIntR(key);
	}

	/**
	 * Recursively search this ServerReply for a key, and return it's associated value as a string.
	 * 
	 * @param key The key to look for.
	 * @return The requested value, or null if the key doesn't exist.
	 */
	public String getStringR(String key)
	{
		Object result = getR(this, key);
		return result instanceof String ? (String) result : null;
	}

	/**
	 * Recursively search this ServerReply for a key, and return it's associated value as a JSONObject.
	 * 
	 * @param key The key to look for.
	 * @return The requested value, or null if the key doesn't exist.
	 */
	public ServerReply getJSONObjectR(String key)
	{
		Object result = getR(this, key);
		return result instanceof JSONObject ? new ServerReply((JSONObject) result) : null;
	}

	/**
	 * Recursively search this ServerReply for a key, and return it's associated value as a JSONArray.
	 * 
	 * @param key The key to look for.
	 * @return The requested value, or null if the key doesn't exist.
	 */
	public JSONArray getJSONArrayR(String key)
	{
		Object result = getR(this, key);
		return result instanceof JSONArray ? (JSONArray) result : null;
	}

	/**
	 * Gets flag indicating if we got an error.
	 * 
	 * @return True if we had an error
	 */
	protected boolean hasError()
	{
		return errcode != null;
	}

	/**
	 * Checks if we have an error, excluding those codes listed in <tt>codes</tt>.
	 * 
	 * @param codes The error codes to ignore. Act like there's no error.
	 * @return False if we didn't find an error.
	 */
	protected boolean hasErrorIfIgnore(String... codes)
	{
		return errcode != null ? !Arrays.asList(codes).contains(errcode) : false;
	}

	/**
	 * Checks to see if we have a result parameter matching our specified one.
	 * 
	 * @param code The code to search for when looking for a result param.
	 * @return True if the code matches the result param.
	 */
	protected boolean resultIs(String code)
	{
		return code.equals(result);
	}

	/**
	 * Gets a list of JSONObjects contained in a single JSONObject.
	 * 
	 * @param key The key with which to get values for
	 * @return A list of JSONObjects objects associated with the specified key.
	 */
	protected ServerReply[] bigJSONObjectGet(String key)
	{
		ArrayList<ServerReply> jl = new ArrayList<ServerReply>();

		JSONObject jo = getJSONObjectR(key);
		if (jo == null)
			return new ServerReply[0];

		String[] keys = JSONObject.getNames(jo);
		if (keys != null)
			for (String s : keys)
				jl.add(new ServerReply(jo.getJSONObject(s)));

		return jl.toArray(new ServerReply[0]);
	}
}