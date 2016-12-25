package fastily.jwiki.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.google.gson.JsonObject;

import fastily.jwiki.util.FL;
import fastily.jwiki.util.GSONP;

/**
 * Contains default namespaces and methods to get non-standard namespaces.
 * 
 * @author Fastily
 *
 */
public final class NS
{
	/**
	 * Main namespace
	 */
	public static final NS MAIN = new NS(0);

	/**
	 * Talk namespace for main
	 */
	public static final NS TALK = new NS(1);
	/**
	 * User namespace
	 */
	public static final NS USER = new NS(2);

	/**
	 * User talk namespace
	 */
	public static final NS USER_TALK = new NS(3);

	/**
	 * Project namespace
	 */
	public static final NS PROJECT = new NS(4);

	/**
	 * Project talk namespace
	 */
	public static final NS PROJECT_TALK = new NS(5);

	/**
	 * File namespace
	 */
	public static final NS FILE = new NS(6);

	/**
	 * File talk namespace
	 */
	public static final NS FILE_TALK = new NS(7);

	/**
	 * MediaWiki namespace
	 */
	public static final NS MEDIAWIKI = new NS(8);

	/**
	 * Media talk namespace
	 */
	public static final NS MEDIA_TALK = new NS(9);

	/**
	 * Template namespace
	 */
	public static final NS TEMPLATE = new NS(10);

	/**
	 * Template talk namespace
	 */
	public static final NS TEMPLATE_TALK = new NS(11);

	/**
	 * Help namespace
	 */
	public static final NS HELP = new NS(12);

	/**
	 * Help talk namespace
	 */
	public static final NS HELP_TALK = new NS(13);

	/**
	 * Category namespace
	 */
	public static final NS CATEGORY = new NS(14);

	/**
	 * Category talk namespace.
	 */
	public static final NS CATEGORY_TALK = new NS(15);

	/**
	 * This NS's value.
	 */
	public final int v;

	/**
	 * Constructor
	 * 
	 * @param v The namespace value to initialize the NS with.
	 */
	protected NS(int v)
	{
		this.v = v;
	}
	
	/**
	 * Gets a hash code for this object.  This is simply the namespace number.
	 */
	public int hashCode()
	{
		return v;
	}
	
	/**
	 * Determines if two namespaces are the same namespace
	 */
	public boolean equals(Object x)
	{
		return x instanceof NS && v == ((NS)x).v;
	}
	
	/**
	 * A namespace manager object. One for each Wiki object.
	 * 
	 * @author Fastily
	 */
	protected static class NSManager
	{
		/**
		 * The Map of all valid namespace-number pairs.
		 */
		protected final HashMap<Object, Object> nsM = new HashMap<>();
		
		/**
		 * The List of valid namespaces as Strings.
		 */
		protected final ArrayList<String> nsL = new ArrayList<>();
		
		/**
		 * Regex used to strip the namespace from a title.
		 */
		protected final String nssRegex;

		/**
		 * Pattern version of <code>nssRegex</code>
		 */
		protected final Pattern p;
		
		/**
		 * Constructor, takes a Reply with Namespace data.
		 * @param r A Reply object with a <code>namespaces</code> JSONObject.
		 */
		protected NSManager(JsonObject r)
		{
			for (JsonObject x : GSONP.getJOofJO(r.getAsJsonObject("namespaces")))
			{
				String name = x.get("*").getAsString();
				if (name.isEmpty())
					name = "Main";
				
				int id = x.get("id").getAsInt();
				nsM.put(name, id);
				nsM.put(id, name);
				
				nsL.add(name);
			}

			for(JsonObject x : GSONP.getJAofJO(r.getAsJsonArray("namespacealiases")))
			{
				String name = x.get("*").getAsString();
				nsM.put(name, x.get("id").getAsInt());
				nsL.add(name);
			}
			
			nssRegex = String.format("(?i)^(%s):", FL.pipeFence(FL.toAL(nsL.stream().map(s -> s.replace(" ", "(_| )")))));
			p = Pattern.compile(nssRegex);
		}

		/**
		 * Generates a filter for use with params passed to API. This DOES NOT URLEncode.
		 * 
		 * @param nsl The namespaces to select.
		 * @return The raw filter string.
		 */
		protected String createFilter(NS... nsl)
		{	
			return FL.pipeFence(FL.toSet(Stream.of(nsl).map(e -> "" + e.v)));
		}
	}
}