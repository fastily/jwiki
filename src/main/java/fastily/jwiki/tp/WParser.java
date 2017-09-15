package fastily.jwiki.tp;

import java.io.StringReader;
import java.util.HashMap;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.GSONP;

/**
 * Parses wikitext into a DOM-style, machine-manipulatable format that is sane to work with. This is the entry point for
 * this API.
 * 
 * @author Fastily
 *
 */
public class WParser
{
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
							.gString(GSONP.getNestedJO(GSONP.jp.parse(wiki.basicPOST("parse", queryParams).body().string()).getAsJsonObject(),
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
						lastNameParsed = parseTKey(r, se).trim();
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
	 * Parses a template parameter key. PRECONDTION: {@code e} is a {@code name} StartElement.
	 * 
	 * @param r The XMLEventReader to use
	 * @param e The StartElement for a {@code name} event. May try to read an index attribute as parameter if there is no
	 *           Characters element following this tag.
	 * @return The name of this template parameter.
	 * @throws Throwable On parse error.
	 */
	private static String parseTKey(XMLEventReader r, StartElement e) throws Throwable
	{
		Attribute index = e.getAttributeByName(new QName("index"));
		return index != null ? index.getValue() : getNextElementText(r);
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
		String x = "";

		while (r.hasNext())
		{
			XMLEvent e = r.nextEvent();

			if (e.isStartElement())
				throw new RuntimeException("What is " + e + " doing in element text?");
			else if (e.isCharacters())
				x += cToStr(e);
			else if (e.isEndElement())
				break;
		}

		return x;
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
}