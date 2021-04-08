package org.fastily.jwiki.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.fastily.jwiki.util.GSONP;

import com.google.gson.JsonObject;
import org.fastily.jwiki.util.FastlyUtilities;

/**
 * Contains default namespaces and methods to get non-standard namespaces.
 * 
 * @author Fastily
 *
 */
public final class NameSpace
{
	/**
	 * Main namespace
	 */
	public static final NameSpace MAIN = new NameSpace(0);

	/**
	 * Talk namespace for main
	 */
	public static final NameSpace TALK = new NameSpace(1);
	/**
	 * User namespace
	 */
	public static final NameSpace USER = new NameSpace(2);

	/**
	 * User talk namespace
	 */
	public static final NameSpace USER_TALK = new NameSpace(3);

	/**
	 * Project namespace
	 */
	public static final NameSpace PROJECT = new NameSpace(4);

	/**
	 * Project talk namespace
	 */
	public static final NameSpace PROJECT_TALK = new NameSpace(5);

	/**
	 * File namespace
	 */
	public static final NameSpace FILE = new NameSpace(6);

	/**
	 * File talk namespace
	 */
	public static final NameSpace FILE_TALK = new NameSpace(7);

	/**
	 * MediaWiki namespace
	 */
	public static final NameSpace MEDIAWIKI = new NameSpace(8);

	/**
	 * Mediawiki talk namespace
	 */
	public static final NameSpace MEDIAWIKI_TALK = new NameSpace(9);

	/**
	 * Template namespace
	 */
	public static final NameSpace TEMPLATE = new NameSpace(10);

	/**
	 * Template talk namespace
	 */
	public static final NameSpace TEMPLATE_TALK = new NameSpace(11);

	/**
	 * Help namespace
	 */
	public static final NameSpace HELP = new NameSpace(12);

	/**
	 * Help talk namespace
	 */
	public static final NameSpace HELP_TALK = new NameSpace(13);

	/**
	 * Category namespace
	 */
	public static final NameSpace CATEGORY = new NameSpace(14);

	/**
	 * Category talk namespace.
	 */
	public static final NameSpace CATEGORY_TALK = new NameSpace(15);

	/**
	 * This NameSpace's value.
	 */
	public final int valueForNameSpace;

	/**
	 * Constructor
	 * 
	 * @param valueForNameSpace The namespace value to initialize the NS with.
	 */
	protected NameSpace(int valueForNameSpace)
	{
		this.valueForNameSpace = valueForNameSpace;
	}

	/**
	 * Gets a hash code for this object. This is simply the namespace number.
	 */
	public int hashCode()
	{
		return valueForNameSpace;
	}

	/**
	 * Determines if two namespaces are the same namespace
	 */
	public boolean equals(Object x)
	{
		return x instanceof NameSpace && valueForNameSpace == ((NameSpace) x).valueForNameSpace;
	}

	/**
	 * A namespace manager object. One for each Wiki object.
	 * 
	 * @author Fastily
	 */
	protected static class NamespaceManager
	{
		/**
		 * The Map of all valid namespace-number pairs.
		 */
		protected final HashMap<Object, Object> nameSpaceToNumbersAsMap = new HashMap<>();

		/**
		 * The List of valid namespaces as Strings.
		 */
		protected final ArrayList<String> nameSpaceSToStringAsList = new ArrayList<>();

		/**
		 * Regex used to strip the namespace from a title.
		 */
		protected final String nameSpaceRegex;

		/**
		 * Pattern version of <code>nssRegex</code>
		 */
		protected final Pattern pattern;

		/**
		 * Constructor, takes a Reply with Namespace data.
		 * 
		 * @param r A Reply object with a <code>namespaces</code> JSONObject.
		 */
		protected NamespaceManager(JsonObject r)
		{
			for (JsonObject x : GSONP.getJOofJO(r.getAsJsonObject("namespaces")))
			{
				String name = x.get("*").getAsString();
				if (name.isEmpty())
					name = "Main";

				int id = x.get("id").getAsInt();
				nameSpaceToNumbersAsMap.put(name, id);
				nameSpaceToNumbersAsMap.put(id, name);

				nameSpaceSToStringAsList.add(name);
			}

			for (JsonObject x : GSONP.getJAofJO(r.getAsJsonArray("namespacealiases")))
			{
				String name = x.get("*").getAsString();
				nameSpaceToNumbersAsMap.put(name, x.get("id").getAsInt());
				nameSpaceSToStringAsList.add(name);
			}

			nameSpaceRegex = String.format("(?i)^(%s):", FastlyUtilities.pipeFence(FastlyUtilities.toAL(nameSpaceSToStringAsList.stream().map(s -> s.replace(" ", "(_| )")))));
			pattern = Pattern.compile(nameSpaceRegex);
		}

		/**
		 * Generates a filter for use with params passed to API. This DOES NOT URLEncode.
		 * 
		 * @param nsl The namespaces to select.
		 * @return The raw filter string.
		 */
		protected String createFilter(NameSpace... nsl)
		{
			return FastlyUtilities.pipeFence(FastlyUtilities.toSet(Stream.of(nsl).map(e -> "" + e.valueForNameSpace)));
		}
	}
}
