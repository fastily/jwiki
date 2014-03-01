package fbot.ft;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fbot.lib.commons.WikiGen;
import fbot.lib.core.Namespace;
import fbot.lib.core.Tools;
import fbot.lib.core.W;
import fbot.lib.core.aux.Logger;
import fbot.lib.core.aux.Tuple;
import fbot.lib.util.FError;

/**
 * Reverts CommonsDelinker edits. Takes arguments via command line. Pass in as many files as you want, w/ or w/o "File:"
 * prefix.
 * 
 * @author Fastily
 * 
 */
public class Relinker
{
	/**
	 * Main driver
	 * 
	 * @param args Prog args
	 */
	public static void main(String[] args)
	{
		for (String arg : new String[] { "Malacanang palace view.jpg"})
			process(makeList(arg));
	}
	
	/**
	 * Gets the table HTML for a CommonsDelinker log for the specified title.
	 * 
	 * @param title The title to get logs for (with or without "File:" prefix).
	 * @return The HTML of the CommonsDelinker log.
	 */
	private static String getTableText(String title)
	{
		String html = getHTMLOf(String.format("https://tools.wmflabs.org/delinker/index.php?image=%s&status=ok&max=500",
				Tools.enc(Namespace.nss(title))));
		
		Matcher m = Pattern.compile("(?si)class\\=\"table table\\-hover.*?\\</table\\>",
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(html);
		if (m.find())
			return html.substring(m.start(), m.end());
		
		FError.errAndExit("Error: We didn't find a log for " + title);
		return null; // dead code. shut up compiler.
	}
	
	/**
	 * Makes a list of Tuples, where the first entry is the name of the page, and the second is the wiki url.
	 * 
	 * @param title The title, with or without "File:" prefix to retrieve entries for.
	 * @return A list of k-v pairs as specified.
	 */
	private static ArrayList<Tuple<String, String>> makeList(String title)
	{
		ArrayList<Tuple<String, String>> l = new ArrayList<Tuple<String, String>>();
		String text = getTableText(title);
		Matcher m = Pattern.compile("\\<tr\\>.*?\\</tr\\>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(text);
		while (m.find())
		{
			String curr = text.substring(m.start(), m.end());
			if (curr.contains("<b>Timestamp</b>")) // first group is table header.
				continue;
			
			String[] cl = curr.split("\n");
			l.add(new Tuple<String, String>(getAnchorArg(cl[4]), getAnchorArg(cl[3])));
		}
		return l;
	}
	
	/**
	 * Gets the argument of an anchor tag in html. PRECONDITION: The line MUST be an anchor tag.
	 * 
	 * @param l The line to parse.
	 * @return The argument of the anchor tag
	 */
	private static String getAnchorArg(String l)
	{
		return l.substring(l.indexOf("\">") + 2, l.indexOf("</a>"));
	}
	
	/**
	 * Scrape HTML of Webpage.
	 * 
	 * @param url The URL to scrape from.
	 * @return The text on the webpage.
	 */
	private static String getHTMLOf(String url)
	{
		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openConnection().getInputStream(), "UTF-8"));
			String x = "";
			String line;
			while ((line = in.readLine()) != null)
				x += line + "\n";
			in.close();
			
			return x.trim();
		}
		catch (Throwable e)
		{
			FError.errAndExit(e);
			return null; // dead code. shut up compiler.
		}
	}
	
	/**
	 * Perform the actual reverts.
	 * 
	 * @param l The list of Tuples to process, where x=title & y=domain.
	 */
	private static void process(ArrayList<Tuple<String, String>> l)
	{
		W wiki = WikiGen.generate("FSV");
		String last = null;
		
		for (Tuple<String, String> t : l)
		{
			if (!t.getY().equals(last))
			{
				wiki.switchDomain(t.getY());
				last = t.getY();
			}
			try
			{
				if (wiki.getRevisions(t.getX(), 1, false)[0].getUser().contains("CommonsDelinker"))
					wiki.undo(t.getX(), "Reverting CommonsDelinker");
			}
			catch (Throwable e)
			{
				System.out.println(t.getX());
				Logger.warn(t.getX() + " doesn't seem to exist");
			}
		}
	}
}