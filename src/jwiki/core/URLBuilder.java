package jwiki.core;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jwiki.util.FString;

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
	private HashMap<String, String> pl = new HashMap<>();

	/**
	 * Constructor, takes the domain name we'll be working with.
	 * 
	 * @param domain The domain name to use, in shorthand (e.g. 'commons.wikimedia.org')
	 */
	protected URLBuilder(String domain)
	{
		if (domain != null && !domain.isEmpty())
			base = String.format(Settings.compro + "%s/w/api.php?format=json&action=", domain);
	}

	/**
	 * Sets the action to use. (e.g. query, edit, delete)
	 * 
	 * @param action The action to use.
	 */
	protected void setAction(String action)
	{
		this.action = action;
	}

	/**
	 * Sets the params of this object. Note that subsequent calls of this method will not overwrite keys-value pairs that
	 * are not named in the passed in parameters.
	 * 
	 * @param params The params, in key-value order to set the object's state with. i.e there must be an even number of
	 *           arguments or you'll get an error
	 */
	protected void setParams(String... params)
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
	protected URL makeURL()
	{
		try
		{
			ArrayList<String> hold = new ArrayList<>();
			for (Map.Entry<String, String> e : pl.entrySet())
			{
				hold.add(e.getKey());
				hold.add(e.getValue());
			}

			return new URL(base + action + chainParams(hold.toArray(new String[0])));
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Chains parameters for use in a URL. Pass in parameters as pairs. For example,
	 * <code>chainParams("title", "foo", "cmcontinue", "derp")</code> gives you "<code>&title=foo&cmcontinue=derp</code>
	 * ". You must pass in an even number of parameters, or you'll get an UnsupportedOperationException.
	 * 
	 * @param params The parameters to chain.
	 * @return The chained properties as a string.
	 */
	protected static String chainParams(String... params)
	{
		if (params.length % 2 == 1)
			throw new UnsupportedOperationException("params contains an odd number of elements:" + params.length);
		String x = "";
		for (int i = 0; i < params.length; i += 2)
			x += String.format("&%s=%s", params[i], params[i + 1]);

		return x;
	}

	/**
	 * Chains and encodes URL parameter properties. Properties: e.g. "timestamp|user|comments|content". Auto applies URL
	 * encoding.
	 * 
	 * @param props The properties to chain.
	 * @return The chained set of properties, separated by pipes as necessary.
	 */
	protected static String chainProps(String... props)
	{
		return FString.enc(FString.fenceMaker("|", props));
	}
}