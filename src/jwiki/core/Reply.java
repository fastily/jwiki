package jwiki.core;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import jwiki.util.FIO;
import jwiki.util.JSONParse;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Wraps a standard, text reply from the server. Checks for any errors.
 * 
 * @author Fastily
 * 
 */
public class Reply
{
	/**
	 * The reply from the server.
	 */
	private JSONObject reply;
	
	/**
	 * Error codes
	 */
	private String error = null;
	
	/**
	 * The result of the query/action, if applicable.
	 */
	private String result;
	
	/**
	 * Result strings which should not be tagged as errors.
	 */
	private static final List<String> whitelist = Arrays.asList(new String[] {"NeedToken", "Success", "Continue"});
	
	/**
	 * Creates a reply object from an InputStream. The InputStream is closed after being read.
	 * 
	 * @param is The InputStream to read the reply from.
	 */
	protected Reply(InputStream is)
	{
		reply = new JSONObject(FIO.inputStreamToString(is, true));
		result = getString("result");
		
		if(Constants.debug)
			System.out.println(reply.toString());
		
		if (reply.has("error") || result != null && !whitelist.contains(result))
		{
			error = reply.toString();
			System.err.println("ERROR: " + error);
		}	
	}
	
	/**
	 * Gets the reply from the server as a JSONObject.
	 * 
	 * @return The reply from the server.
	 */
	protected JSONObject getReply()
	{
		return reply;
	}
	
	/**
	 * Gets flag indicating if we got an error.
	 * 
	 * @return True if we had an error
	 */
	protected boolean hasError()
	{
		return error != null;
	}
	

	/**
	 * Checks if we have an error, excluding those codes listed in <tt>codes</tt>.
	 * @param codes The error codes to ignore.  Act like there's no error.
	 * @return True if we didn't find an error (excluding those in codes)
	 */
	protected boolean hasErrorIgnore(String... codes)
	{
		if(!hasError())
			return false;
		
		String ec = getString("code"); //MW only returns one error at a time.
		if(ec != null)
			for(String code : codes)
				if(code.equals(ec))
					return false;
		
		return true;
	}
	
	/**
	 * Returns a informative String indicating the error.
	 * 
	 * @return The error code and info.
	 */
	protected String getError()
	{
		return error;
	}
	
	/**
	 * Gets first instance of this key in the reply and returns its value as a String.
	 * 
	 * @param key The key to use
	 * @return The result, or null if we couldn't find it.
	 */
	protected String getString(String key)
	{
		return JSONParse.getStringR(reply, key);
	}
	
	/**
	 * Gets first instance of this key in the reply and returns its value as a JSONObject.
	 * 
	 * @param key The key to use
	 * @return The result, or null if we couldn't find it.
	 */
	protected JSONObject getJSONObject(String key)
	{
		return JSONParse.getJSONObjectR(reply, key);
	}
	
	/**
	 * Gets first instance of this key in the reply and returns its value as an int.
	 * 
	 * @param key The key to use
	 * @return The result, or -1 if we couldn't find it.
	 */
	protected int getInt(String key)
	{
		return JSONParse.getIntR(reply, key);
	}
	
	/**
	 * Gets first instance of this key in the reply and returns its value as a JSONArray.
	 * 
	 * @param key The key to use
	 * @return The result, or null if we couldn't find it.
	 */
	protected JSONArray getJSONArray(String key)
	{
		return JSONParse.getJSONArrayR(reply, key);
	}
	
	
	/**
	 * Tries to access the first instance of a result parameter.
	 * 
	 * @return The result of this Reply object, or null if we don't have a result param.
	 */
	protected String getResult()
	{
		
		return result;
	}
	
	/**
	 * Checks to see if we have a result parameter matching our specified one.
	 * 
	 * @param code The code to search for when looking for a result param.
	 * @return True if the code matches the result param.
	 */
	protected boolean resultIs(String code)
	{
		return code.equals(getResult());
	}
	
}