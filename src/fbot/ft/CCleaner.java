package fbot.ft;

import java.util.ArrayList;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import fbot.lib.commons.CStrings;
import fbot.lib.commons.Commons;
import fbot.lib.mbot.MBot;
import fbot.lib.mbot.WAction;
import fbot.lib.util.FCLI;

/**
 * Assist with cleanup on Commons.
 * 
 * @author Fastily
 * 
 */
public class CCleaner
{
	/**
	 * Main driver.
	 * 
	 * @param args Prog args
	 */
	public static void main(String[] args)
	{
		// Constants.debug = true;
		CommandLine l = parseArgs(args);
		if (l.hasOption('p'))
			Commons.nukeLinksOnPage(l.getOptionValue('p'), l.getOptionValue('r'), "File");
		else if (l.hasOption("dr"))
			Commons.drDel(l.getOptionValue("dr"));
		else if (l.hasOption('u'))
			Commons.nukeUploads(l.getOptionValue('u'), l.getOptionValue('r'));
		else if (l.hasOption('c'))
			Commons.categoryNuke(l.getOptionValue('c'), l.getOptionValue('r'), false);
		else if (l.hasOption('t'))
			talkPageClear();
		else
		{
			Commons.categoryNuke(CStrings.cv, CStrings.copyvio, false, "File");
			Commons.emptyCatDel(Commons.com.getCategoryMembers(CStrings.osd, "Category"));
			Commons.emptyCatDel(Commons.com.getCategoryMembers("Non-media deletion requests", "Category"));
			Commons.nukeEmptyFiles(Commons.com.getCategoryMembers(CStrings.osd, "File"));
			
			if(l.hasOption('d'))
				unknownClear();
			
			if(l.hasOption('a'))
				DRArchive.main(new String[0]);
			else if(l.hasOption("ac"))
				DRArchive.main(new String[] {"-c"});
		}
	}
	
	/**
	 * Parses prog arguments.
	 * 
	 * @param args The arguments recieved by main
	 * @return A CommandLine object with parsed args.
	 */
	private static CommandLine parseArgs(String[] args)
	{
		Options ol = new Options();
		
		OptionGroup og = new OptionGroup();
		og.addOption(FCLI.makeArgOption("dr", "Delete all files linked in a DR", "DR"));
		og.addOption(FCLI.makeArgOption("p", "Set mode to delete all files linked on a page", "title"));
		og.addOption(FCLI.makeArgOption("u", "Set mode to delete all uploads by a user", "username"));
		og.addOption(FCLI.makeArgOption("c", "Set mode to delete all category members", "category"));
		og.addOption(new Option("t", false, "Clears orphaned talk pages from DBR"));
		og.addOption(new Option("a", false, "Archive DRs ready for archiving"));
		og.addOption(new Option("ac", false, "Close all Singleton DRs"));
		ol.addOptionGroup(og);
		
		ol.addOption(FCLI.makeArgOption("r", "Reason param, for use with options that require a reason", "reason"));
		ol.addOption("help", false, "Print this help message and exit");
		ol.addOption("d", false, "Deletes everything we can in Category:Unknown");
		
		return FCLI.gnuParse(ol, args, "CCleaner [-dr|-t|[-p <title>|-u <user>|-c <cat>] -r <reason>]] [-d] [-a|-ac]");
	}
	
	/**
	 * Deletes all pages on "Commons:Database reports/Orphaned talk pages".
	 * 
	 * @return A list of pages we failed to process
	 */
	private static String[] talkPageClear()
	{
		ArrayList<String> l = new ArrayList<String>();
		Scanner m = new Scanner(Commons.com.getPageText("Commons:Database reports/Orphaned talk pages"));
		
		String ln;
		while (m.hasNextLine())
			if ((ln = m.nextLine()).contains("{{plnr"))
				l.add(ln.substring(ln.indexOf("=") + 1, ln.indexOf("}}")));
		m.close();
		
		return Commons.nuke("Orphaned talk page", l.toArray(new String[0]));
	}
	
	/**
	 * Clears daily categories in Category:Unknown. List is grabbed from <a
	 * href="https://commons.wikimedia.org/wiki/User:FSV/UC">User:FSV/UC</a>
	 * 
	 * @return A list of pages we failed to process
	 */
	private static String[] unknownClear()
	{
		Commons.com.nullEdit("User:FSV/UC");
		
		ArrayList<String> catlist = new ArrayList<String>();
		ArrayList<WAction> l = new ArrayList<WAction>();
		
		for(String c : Commons.com.getValidLinksOnPage("User:FSV/UC"))
		{
			catlist.add(c);
			String r;
			if (c.contains("permission"))
				r = String.format("[[COM:OTRS|No permission]] since %s", c.substring(c.indexOf("as of") + 6));
			else if (c.contains("license"))
				r = String.format("No license since %s", c.substring(c.indexOf("as of") + 6));
			else
				r = String.format("No source since %s", c.substring(c.indexOf("as of") + 6));
			
			for (String s : Commons.com.getCategoryMembers(c, "File"))
				l.add(new MBot.DeleteItem(s, r));
		}
		Commons.doAction("Fastily", l.toArray(new WAction[0]));
		return Commons.emptyCatDel(catlist.toArray(new String[0]));
	}
	
}