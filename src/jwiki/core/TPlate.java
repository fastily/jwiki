package jwiki.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

/**
 * A simple, object-oriented representation of a MediaWiki template.
 * 
 * @author Fastily
 *
 */
public class TPlate
{
	/**
	 * The name of this template
	 */
	public final String title;

	/**
	 * Internal HashMap backing TPlate parameters.
	 */
	private final HashMap<String, Object> params = new HashMap<>();

	/**
	 * Constructor, recursively parses a Reply into a TPlate
	 * 
	 * @param r The root Reply object from the server. This MUST be the root JSONObject.
	 */
	private TPlate(Reply r)
	{
		if(Settings.debug)
			System.out.println(r.toString(2));
		
		title = r.getString("title");

		if (r.has("part"))
		{
			ArrayList<Reply> pl = new ArrayList<>();

			Object rawpl = r.get("part");
			if (rawpl instanceof JSONArray)
				pl.addAll(r.getJAofJO("part"));
			else if (rawpl instanceof JSONObject)
				pl.add(new Reply((JSONObject) rawpl));

			Reply k, v;
			for (Reply p : pl)
				params.put((k = p.getJSONObjectR("name")) != null ? "" + k.getInt("index") : p.getStringR("name"),
						(v = p.getJSONObjectR("value")) != null ? new TPlate(v.getJSONObjectR("template")) : p.getStringR("value"));
		}
	}

	/**
	 * Gets the template parameter for the specified key.
	 * 
	 * @param k The key to look for
	 * @return The Object associated with the key (either a String or TPlate) or null if the key does not exist.
	 */
	public Object get(String k)
	{
		return params.get(k);
	}

	/**
	 * Gets the String parameter for the specified key.
	 * 
	 * @param k The key to look for.
	 * @return The specified String, or null if no matching String was not found.
	 */
	public String getStringFor(String k)
	{
		return hasStringFor(k) ? (String) params.get(k) : null;
	}

	/**
	 * Gets the TPlate parameter for the specified key.
	 * 
	 * @param k The key to look for.
	 * @return The specified TPlate, or null if no matching TPlate was not found.
	 */
	public TPlate getTPlateFor(String k)
	{
		return hasTPlateFor(k) ? (TPlate) params.get(k) : null;
	}

	/**
	 * Checks if the TPlate contains a given key
	 * 
	 * @param k The key to look for
	 * @return True if the key was found.
	 */
	private boolean has(String k)
	{
		return params.containsKey(k);
	}

	/**
	 * Checks iff this TPlate contains a String as a parameter for the given key.
	 * 
	 * @param k The key to look for.
	 * @return True if the specified String exists.
	 */
	public boolean hasStringFor(String k)
	{
		return has(k) && params.get(k) instanceof String;
	}

	/**
	 * Checks iff this TPlate contains a TPlate as a parameter for the given key.
	 * 
	 * @param k The key to look for.
	 * @return True if the specified TPlate exists.
	 */
	public boolean hasTPlateFor(String k)
	{
		return has(k) && params.get(k) instanceof TPlate;
	}

	/**
	 * Flattens this TPlate into a String representation
	 */
	public String toString()
	{
		String x = "{{" + title;
		for (Map.Entry<String, Object> e : params.entrySet())
			x += String.format("|%s=%s", e.getKey(), e.getValue());
		x += "}}";

		return x;
	}

	/**
	 * Parses a MediaWiki template into a TPlate object. PRECONDITION: <code>text</code> is a syntactically legal
	 * template.
	 * 
	 * @param wiki The wiki object to run the parse query on
	 * @param text The template to parse (e.g. <code>"{{Test|Blah|foo=meh|baz|3=132}}"</code>)
	 * @return A TPlate object. Null on error.
	 */
	public static TPlate parse(Wiki wiki, String text)
	{
		try
		{
			return new TPlate(new Reply(
					XML.toJSONObject(Req.get(wiki.makeUB("expandtemplates", "text", text, "prop", "parsetree").makeURL(), wiki.cookiejar)
							.getStringR("parsetree"))).getJSONObjectR("template"));
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}
}