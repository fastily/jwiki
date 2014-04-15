package jwiki.core;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds a URL with the specified domain. Retains state and is easily modifiable, so it can be used to make multiple,
 * similar queries.
 * 
 * @author Fastily
 * 
 */
public class URLBuilder
{
	/**
	 * The domain we'll be using.
	 */
	private String domain;
	
	/**
	 * The base of the URL, constructed with the domain.
	 */
	private String base;
	
	/**
	 * The action to use. e.g. query, delete, edit.
	 */
	private String action;
	
	/**
	 * The parameter list to append to the URL.
	 */
	private HashMap<String, String> pl = new HashMap<String, String>();
	
	/**
	 * Constructor, takes the domain name we'll be working with.
	 * 
	 * @param domain The domain name to use, in shorthand (e.g. 'commons.wikimedia.org')
	 */
	public URLBuilder(String domain)
	{
		setDomain(domain);
	}
	
	/**
	 * Gets the currently set domain.
	 * 
	 * @return The domain, shorthand.
	 */
	public String getDomain()
	{
		return domain;
	}
	
	/**
	 * Sets the domain. The domain should be passed in shorthand style (e.g. 'commons.wikimedia.org').
	 * 
	 * @param domain The domain to use.
	 */
	public void setDomain(String domain)
	{
		this.domain = domain;
		base = String.format("https://%s/w/api.php?format=json&action=", domain);
	}
	
	/**
	 * Sets the action to use. (e.g. query, edit, delete)
	 * 
	 * @param action The action to use.
	 */
	public void setAction(String action)
	{
		this.action = action;
	}
	
	/**
	 * Sets the params of this object. Note that subsequent calls of this method will not overwrite keys-value pairs
	 * that are not named in the passed in parameters.
	 * 
	 * @param params The params, in key-value order to set the object's state with. i.e there must be an even number of
	 *            arguments or you'll get an error
	 */
	public void setParams(String... params)
	{
		if (params.length % 2 == 1)
			throw new UnsupportedOperationException("params cannot be odd # of elements: " + params.length);
		
		for (int i = 0; i < params.length; i += 2)
			pl.put(params[i], params[i + 1]);
	}
	
	/**
	 * Makes a URL using the current state of the object.
	 * 
	 * @return A URL based off the state of the object, or null if something went wrong.
	 */
	public URL makeURL()
	{
		try
		{
			return new URL(base + action + chainParams(getParamsAsList()));
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Gets the params of this object as a list. Guaranteed even number of items, where every key is followed by a
	 * value.
	 * 
	 * @return The params as a list.
	 */
	public String[] getParamsAsList()
	{
		ArrayList<String> hold = new ArrayList<String>();
		for (Map.Entry<String, String> e : pl.entrySet())
		{
			hold.add(e.getKey());
			hold.add(e.getValue());
		}
		
		return hold.toArray(new String[0]);
	}
	
	
	/**
	 * Gets the params of this object as text.  Everything will be properly chained, and in order.
	 * @return The list of params as a String.
	 */
	public String getParamsAsText()
	{
		return URLBuilder.chainParams(getParamsAsList());
	}
	
	/**
	 * Clears all params stored in the object.
	 */
	public void clearParams()
	{
		pl = new HashMap<String, String>();
	}
	
	/**
	 * Sets the action field to null.
	 */
	public void clearAction()
	{
		action = null;
	}
	
	/**
	 * Removes all params stored in the object and sets the action field to null.
	 */
	public void clearAll()
	{
		clearParams();
		clearAction();
	}
	
	/**
	 * Chains parameters for use in a URL. Pass in parameters as pairs. For example,
	 * <tt>chainParams("title", "foo", "cmcontinue", "derp")</tt> gives you "<tt>&title=foo&cmcontinue=derp</tt>". You
	 * must pass in an even number of parameters, or you'll get an UnsupportedOperationException.
	 * 
	 * @param params The parameters to chain.
	 * @return The chained properties as a string.
	 */
	public static String chainParams(String... params)
	{
		if (params.length % 2 == 1)
			throw new UnsupportedOperationException("params contains an odd number of elements:" + params.length);
		String x = "";
		for (int i = 0; i < params.length; i += 2)
			x += String.format("&%s=%s", params[i], params[i + 1]);
		
		return x;
	}
	
	/**
	 * Chains and encodes URL parameter properties. Properties: e.g. "timestamp|user|comments|content".
	 * 
	 * @param props The properties to chain.
	 * @return The chained set of properties, separated by pipes as necessary.
	 */
	public static String chainProps(String... props)
	{
		String x = "";
		
		if (props.length >= 1)
			x += props[0];
		for (int i = 1; i < props.length; i++)
			x += "|" + props[i];
		
		return Tools.enc(x);
	}
	
}