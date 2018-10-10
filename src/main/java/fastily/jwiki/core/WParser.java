package fastily.jwiki.core;

import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import fastily.jwiki.util.FL;
import fastily.jwiki.util.GSONP;

/**
 * Parses wikitext into a DOM-style, manipulatable format that is easy to work with.
 * 
 * @author Fastily
 *
 */
public class WParser
{
	/**
	 * No constructors needed
	 */
	private WParser()
	{

	}

	/**
	 * Runs a parse query for wikitext/pages and then parses the result into a WikiText object.
	 * 
	 * @param wiki The Wiki object to use
	 * @param queryParams Parameters to POST to the server
	 * @return A WikiText object, or null on error.
	 */
	private static WikiText parse(Wiki wiki, HashMap<String, String> queryParams)
	{
		queryParams.put("prop", "parsetree");
		try
		{
			XMLEventReader r = XMLInputFactory.newInstance()
					.createXMLEventReader(new StringReader(GSONP
							.getStr(GSONP.getNestedJO(GSONP.jp.parse(wiki.basicPOST("parse", queryParams).body().string()).getAsJsonObject(),
									FL.toSAL("parse", "parsetree")), "*")));

			WikiText root = new WikiText();
			while (r.hasNext())
			{
				XMLEvent e = r.nextEvent();

				if (e.isStartElement() && nameIs(e.asStartElement(), "template"))
					root.append(parseTemplate(r, root));
				else if (e.isCharacters())
					root.append(cToStr(e));
			}
			return root;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Parses the text of a page into a WikiText object.
	 * 
	 * @param wiki The Wiki to use
	 * @param page The title of the page to parse.
	 * @return A WikiText representation of {@code page}, or null on error.
	 */
	public static WikiText parsePage(Wiki wiki, String page)
	{
		return parse(wiki, FL.pMap("page", page));
	}

	/**
	 * Parses the text of a page into a WikiText object.
	 * 
	 * @param wiki The Wiki to use
	 * @param text The wikitext to parse
	 * @return A WikiText representation of {@code text}, or null on error.
	 */
	public static WikiText parseText(Wiki wiki, String text)
	{
		return parse(wiki, FL.pMap("text", text, "contentmodel", "wikitext"));
	}

	/**
	 * Parses a template. This function is to be called upon encountering a {@code template} StartElement.
	 * 
	 * @param r The XMLEventReader to use.
	 * @param parent The parent WikiText the resulting WTemplate is to belong to, if applicable. Set null to disable.
	 * @return The parsed WTemplate.
	 * @throws Throwable On parse error.
	 */
	private static WTemplate parseTemplate(XMLEventReader r, WikiText parent) throws Throwable
	{
		WTemplate t = new WTemplate(parent);

		String lastNameParsed = "";
		while (r.hasNext())
		{
			XMLEvent e = r.nextEvent();
			if (e.isStartElement())
			{
				StartElement se = e.asStartElement();
				switch (se.getName().getLocalPart())
				{
					case "title":
						t.title = getNextElementText(r).trim();
						break;
					case "name":
						Attribute index = se.getAttributeByName(new QName("index"));
						lastNameParsed = index != null ? index.getValue() : getNextElementText(r).trim();
						break;
					case "equals":
						getNextElementText(r);
						break;
					case "value":
						t.put(lastNameParsed, parseTValue(r));
						break;
					default:
						// do nothing - skip part tags
				}
			}
			else if (e.isEndElement() && nameIs(e.asEndElement(), "template"))
				break;
		}
		return t;
	}

	/**
	 * Parses a template parameter value. PRECONDITION: the next element in {@code r} is of type Characters or a
	 * StartElement for a new template.
	 * 
	 * @param r The XMLEventReader to use.
	 * @return WikiText representing this template's parameter value.
	 * @throws Throwable On parse error.
	 */
	private static WikiText parseTValue(XMLEventReader r) throws Throwable
	{
		WikiText root = new WikiText();

		while (r.hasNext())
		{
			XMLEvent e = r.nextEvent();

			if (e.isStartElement() && nameIs(e.asStartElement(), "template"))
				root.append(parseTemplate(r, root));
			else if (e.isCharacters())
				root.append(cToStr(e));
			else if (e.isEndElement() && nameIs(e.asEndElement(), "value"))
				break;
		}
		return root;
	}

	/**
	 * Gets the next Characters event(s) contained by the next pair of XMLEvent objects. Useful because sometimes a pair
	 * of XML elements may be followed by more than one Characters event.
	 * 
	 * @param r An XMLEventReader where the next XMLEvent object(s) is/are a Characters event(s).
	 * @return The Strings of the Characters events as a String.
	 * @throws Throwable On parse error.
	 */
	private static String getNextElementText(XMLEventReader r) throws Throwable
	{
		StringBuilder x = new StringBuilder();

		while (r.hasNext())
		{
			XMLEvent e = r.nextEvent();

			if (e.isStartElement())
				getNextElementText(r); // skip nested blocks, these are usually strangely placed comments
			else if (e.isCharacters())
				x.append(cToStr(e));
			else if (e.isEndElement())
				break;
		}

		return x.toString();
	}

	/**
	 * Converts a Characters XMLEvent to a String.
	 * 
	 * @param e The XMLEvent (a Characters object) to convert to a String.
	 * @return {@code e} as a String
	 */
	private static String cToStr(XMLEvent e)
	{
		return e.asCharacters().getData();
	}

	/**
	 * Check if the name portion of StartElement {@code e} is equal to String {@code n}
	 * 
	 * @param e The StartElement to check
	 * @param n The String to check against
	 * @return True if {@code e} is the same as {@code n}
	 */
	private static boolean nameIs(StartElement e, String n)
	{
		return e.getName().getLocalPart().equals(n);
	}

	/**
	 * Check if the name portion of EndElement {@code e} is equal to String {@code n}
	 * 
	 * @param e The EndElement to check
	 * @param n The String to check against
	 * @return True if {@code e} is the same as {@code n}
	 */
	private static boolean nameIs(EndElement e, String n)
	{
		return e.getName().getLocalPart().equals(n);
	}

	/**
	 * Mutable representation of parsed wikitext. May contain Strings and templates.
	 * 
	 * @author Fastily
	 *
	 */
	public static class WikiText
	{
		/**
		 * Data structure backing wikitext storage.
		 */
		protected ArrayDeque<Object> l = new ArrayDeque<>();

		/**
		 * Creates a new WikiText object
		 * 
		 * @param objects Any Objects to pre-load this WikiText object with. Acceptable values are of type String or
		 *           WTemplate.
		 */
		public WikiText(Object... objects)
		{
			for (Object o : objects)
				append(o);
		}

		/**
		 * Appends an Object to this WikiText object.
		 * 
		 * @param o The Object to append. Acceptable values are of type String or WTemplate.
		 */
		public void append(Object o)
		{
			if (o instanceof String)
				l.add((l.peekLast() instanceof String ? l.pollLast().toString() : "") + o);
			else if (o instanceof WTemplate)
			{
				WTemplate t = (WTemplate) o;
				t.parent = this;
				l.add(o);
			}
			else
				throw new IllegalArgumentException("What is '" + o + "' ?");
		}

		/**
		 * Find top-level WTemplates contained by this WikiText
		 * 
		 * @return A List of top-level WTemplates in this WikiText.
		 */
		public ArrayList<WTemplate> getTemplates()
		{
			return FL.toAL(l.stream().filter(o -> o instanceof WTemplate).map(o -> (WTemplate) o));
		}

		/**
		 * Recursively finds WTemplate objects contained by this WikiText.
		 * 
		 * @return A List of all WTemplate objects in this WikiText.
		 */
		public ArrayList<WTemplate> getTemplatesR()
		{
			ArrayList<WTemplate> wtl = new ArrayList<>();
			getTemplatesR(wtl);

			return wtl;
		}

		/**
		 * Recursively finds WTemplate objects contained by this WikiText.
		 * 
		 * @param wtl Any WTemplate objects found will be added to this List.
		 * 
		 * @see #getTemplatesR()
		 */
		private void getTemplatesR(ArrayList<WTemplate> wtl)
		{
			l.stream().filter(o -> o instanceof WTemplate).map(o -> (WTemplate) o).forEach(t -> {
				for (WikiText wt : t.params.values())
					wt.getTemplatesR(wtl);

				wtl.add(t);
			});
		}

		/**
		 * Render this WikiText object as a String. Trims whitespace by default.
		 */
		public String toString()
		{
			return toString(true);
		}

		/**
		 * Render this WikiText as a String.
		 * 
		 * @param doTrim If true, then trim whitespace.
		 * @return A String representation of this WikiText.
		 */
		public String toString(boolean doTrim)
		{
			StringBuilder b = new StringBuilder("");
			for (Object o : l)
				b.append(o);

			String out = b.toString();
			return doTrim ? out.trim() : out;
		}
	}

	/**
	 * Mutable representation of a parsed wikitext template.
	 * 
	 * @author Fastily
	 *
	 */
	public static class WTemplate
	{
		/**
		 * The parent WikiText object, if necessary
		 */
		protected WikiText parent;

		/**
		 * This WTemplate's title
		 */
		public String title = "";

		/**
		 * The Map tracking this object's parameters.
		 */
		protected LinkedHashMap<String, WikiText> params = new LinkedHashMap<>();

		/**
		 * Creates a new, empty WTemplate object.
		 */
		public WTemplate()
		{
			this(null);
		}

		/**
		 * Creates a new WTemplate with a parent.
		 * 
		 * @param parent The parent WikiText object this WTemplate belongs to.
		 */
		protected WTemplate(WikiText parent)
		{
			this.parent = parent;
		}

		/**
		 * Normalize the title of the WTemplate, according to {@code wiki}. In other words, remove the 'Template:'
		 * namespace, convert, capitalize the first letter, convert underscores to spaces.
		 * 
		 * @param wiki The Wiki to normalize against.
		 */
		public void normalizeTitle(Wiki wiki) // TODO: Account for non-template NS
		{
			if (wiki.whichNS(title).equals(NS.TEMPLATE))
				title = wiki.nss(title);

			title = title.length() <= 1 ? title.toUpperCase() : "" + Character.toUpperCase(title.charAt(0)) + title.substring(1);
			title = title.replace('_', ' ');
		}

		/**
		 * Test if the specified key {@code k} exists in this WTemplate. This does not check whether the parameter is
		 * empty or not.
		 * 
		 * @param k The key to check
		 * @return True if there is a mapping for {@code k} in this WTemplate.
		 */
		public boolean has(String k)
		{
			return params.containsKey(k) && !params.get(k).l.isEmpty();
		}

		/**
		 * Gets the specified WikiText value associated with key {@code k} in this WTemplate.
		 * 
		 * @param k The key to get WikiText for.
		 * @return The WikiText, or null if there is no mapping for {@code k}
		 */
		public WikiText get(String k)
		{
			return params.get(k);
		}

		/**
		 * Puts a new parameter in this Template.
		 * 
		 * @param k The name of the parameter
		 * @param v The value of the parameter; acceptable types are WikiText, String, and WTemplate.
		 */
		public void put(String k, Object v)
		{
			if (v instanceof WikiText)
				params.put(k, (WikiText) v);
			else if (v instanceof String || v instanceof WTemplate)
				params.put(k, new WikiText(v));
			else
				throw new IllegalArgumentException(String.format("'%s' is not an acceptable type", v));
		}

		/**
		 * Appends {@code o} to the end of the WikiText associated with {@code k}
		 * 
		 * @param k The key to associate new text with.
		 * @param o The Object to append to the value keyed by {@code k} in this WTemplate
		 */
		public void append(String k, Object o)
		{
			if (has(k))
				params.get(k).append(o);
			else
				put(k, o);
		}

		/**
		 * Removes the mapping for the specified key, {@code k}
		 * 
		 * @param k Removes the mapping for this key, if possible
		 */
		public void remove(String k)
		{
			params.remove(k);
		}

		/**
		 * Removes this WTemplate from its parent WikiText object, if applicable.
		 */
		public void drop()
		{
			if (parent == null)
				return;

			parent.l.remove(this);
			parent = null;
		}

		/**
		 * Re-map the a key to a new name.
		 * 
		 * @param oldK The old name
		 * @param newK The new name
		 */
		public void remap(String oldK, String newK)
		{
			params.put(newK, params.remove(oldK));
		}

		/**
		 * Get the keyset (all parameters) for this WTemplate. The resulting keyset does not back the internal Map.
		 * 
		 * @return The keyset for this WTemplate.
		 */
		public HashSet<String> keySet()
		{
			return new HashSet<>(params.keySet());
		}

		/**
		 * Generates a String (wikitext) representation of this Template.
		 * 
		 * @param indent Set true to add a newline between each parameter.
		 * @return A String representation of this Template.
		 */
		public String toString(boolean indent)
		{
			String base = (indent ? "%n" : "") + "|%s=%s";

			String x = "";
			for (Map.Entry<String, WikiText> e : params.entrySet())
				x += String.format(base, e.getKey(), e.getValue());

			if (indent)
				x += "\n";

			return String.format("{{%s%s}}", title, x);
		}

		/**
		 * Renders this WTemplate as a String.
		 */
		public String toString()
		{
			return toString(false);
		}
	}
}