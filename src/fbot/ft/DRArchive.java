package fbot.ft;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import fbot.lib.commons.Commons;
import fbot.lib.commons.WikiGen;
import fbot.lib.core.W;
import fbot.lib.mbot.MBot;
import fbot.lib.mbot.QAction;
import fbot.lib.mbot.WAction;
import fbot.lib.util.FCLI;

/**
 * Archives all closed DRs older than 7 days.
 * 
 * @author Fastily
 * 
 */
public class DRArchive
{
	/**
	 * The list of singleton DRs. This list is dumped to wiki after program finishes normal execution.
	 */
	private static ConcurrentLinkedQueue<String> singles = new ConcurrentLinkedQueue<String>();
	
	/**
	 * Matches a single signature timestamp
	 */
	private static final String stamp = "\\d{2}?:\\d{2}?, \\d{1,}? (January|February|March|April|"
			+ "May|June|July|August|September|October|November|December) \\d{4}?";
	
	/**
	 * Main driver.
	 * 
	 * @param args Prog args.
	 */
	public static void main(String[] args)
	{
		CommandLine l = parseArgs(args);
		if (l.hasOption('c'))
		{
			ArrayList<ProcDR> dl = new ArrayList<ProcDR>();
			for(String s : Commons.com.getTemplatesOnPage("User:Fastily/SingletonDR"))
				if(s.startsWith("Commons:Deletion requests/"))
					dl.add(new ProcDR(s));
			Commons.doAction("Fastily", dl.toArray(new ProcDR[0]));
		}
		else
		{
			Commons.com.nullEdit("User:FSV/DL");
			ArrayList<ProcLog> pl = new ArrayList<ProcLog>();
			for (String s : Commons.com.getValidLinksOnPage("User:FSV/DL"))
				pl.add(new ProcLog(s));
			WikiGen.genM("FSV", 5).start(pl.toArray(new ProcLog[0]));
			
			String x = "Report generated @ ~~~~~\n";
			for (String s : singles)
				x += String.format("%n{{%s}}", s);
			Commons.com.edit("User:Fastily/SingletonDR", x, "Update report");
		}
	}
	
	/**
	 * Parses the command line arguments.
	 * 
	 * @param args The program args recieved by main
	 * @return A set of parsed args
	 */
	private static CommandLine parseArgs(String[] args)
	{
		Options ol = new Options();
		
		ol.addOption("c", false, "If this is set, close all DRs of 'User:Fastily/SingletonDR'");
		ol.addOption("help", false, "Print this help message and exit");
		
		return FCLI.gnuParse(ol, args, "DRArchive [-c]");
	}
	
	/**
	 * Represents a log to process.
	 * 
	 * @author Fastily
	 * 
	 */
	private static class ProcLog extends WAction
	{
		/**
		 * This log's archive. Generated in constructor.
		 */
		private String archive;
		
		/**
		 * Constructor, takes a log title as the argument.
		 * 
		 * @param title The log title.
		 */
		private ProcLog(String title)
		{
			super(title, null, "Archiving %d threads %s [[%s]]");
			archive = "Commons:Deletion requests/Archive" + title.substring(title.indexOf('/'));
		}
		
		/**
		 * Performs the analysis and archive.
		 * 
		 * @param wiki The wiki object to use.
		 * @return True if we were successful.
		 */
		public boolean doJob(W wiki)
		{
			DRItem[] l = fetchDRs(wiki);
			new MBot(wiki, 10).start(l);
			
			ArrayList<String> toArchive = new ArrayList<String>();
			for (DRItem d : l)
			{
				if (d.canA)
					toArchive.add(d.getTitle());
				else if (d.isSingle)
					singles.add(d.getTitle());
			}
			
			String[] al = toArchive.toArray(new String[0]);
			if (al.length > 0) // for efficiency.
			{
				wiki.edit(getTitle(), extract(wiki.getPageText(getTitle()), al),
						String.format(summary, toArchive.size(), "to", archive));
				wiki.edit(archive, wiki.getPageText(archive) + pool(al),
						String.format(summary, toArchive.size(), "from", getTitle()));
			}
			return true;
		}
		
		/**
		 * Convert <tt>titles</tt> to template form, one per newline.
		 * 
		 * @param titles The titles to convert
		 * @return A string with all the templates.
		 */
		private String pool(String... titles)
		{
			String x = "";
			for (String s : titles)
				x += String.format("%n{{%s}}", s);
			return x;
		}
		
		/**
		 * Remove all template instances of <tt>titles</tt> from <tt>base</tt>.
		 * 
		 * @param base Base string.
		 * @param titles The templated titles to remove.
		 * @return <tt>base</tt> without the templated versions of <tt>titles</tt>
		 */
		private String extract(String base, String... titles)
		{
			String x = base;
			for (String s : titles)
				x = x.replace("\n{{" + s + "}}", "");
			return x;
		}
		
		/**
		 * Grabs a list of DRs transcluded on the log represented by this object.
		 * 
		 * @param wiki Wiki object to use
		 * @return A list of DRs transcluded on this log.
		 */
		private DRItem[] fetchDRs(W wiki)
		{
			ArrayList<DRItem> l = new ArrayList<DRItem>();
			for (String s : wiki.exists(wiki.getTemplatesOnPage(getTitle()), true))
				if (s.startsWith("Commons:Deletion requests/"))
					l.add(new DRItem(s));
			return l.toArray(new DRItem[0]);
		}
	}
	
	/**
	 * Represents a DR.
	 * 
	 * @author Fastily
	 * 
	 */
	private static class DRItem extends QAction
	{
		/**
		 * The raw text of this DR
		 */
		private String text;
		
		/**
		 * Flag indicating if this is a singleton DR.
		 */
		private boolean isSingle = false;
		
		/**
		 * Flag indicating if this DR is ready to be archived.
		 */
		private boolean canA = false;
		
		/**
		 * Constructor, creates a DRItem.
		 * 
		 * @param title The title of the DR on wiki.
		 */
		private DRItem(String title)
		{
			super(title);
		}
		
		/**
		 * Analyzes the DR.
		 * 
		 * @param wiki The wiki object to use
		 * @return True if we were successful.
		 */
		public boolean doJob(W wiki)
		{
			text = wiki.getPageText(getTitle());
			canArchive();
			if (!canA)
				isSingleton(wiki);
			return true;
		}
		
		/**
		 * Tests if this DR is ready for archiving & sets flags.
		 */
		private void canArchive()
		{
			if (text == null)
				canA = false;
			else
			{
				String temp = text.replaceAll("(?i)\\[\\[(Category:).+?\\]\\]", "");
				temp = temp.replaceAll("(?si)\\<(includeonly)\\>.*?\\</(includeonly)\\>", "").trim();
				canA = temp
						.matches("(?si)\\{\\{(delh|DeletionHeader).*?\\}\\}.*?\\{(DeletionFooter/Old|Delf|DeletionFooter|Udelf).*?\\}\\}");
			}
		}
		
		/**
		 * Tests if this DR is a Singleton DR (one contributor, one file, uncontested).
		 * 
		 * @param wiki The wiki object to use.
		 */
		private void isSingleton(W wiki)
		{
			isSingle = text != null
					&& !text.matches("(?si).*?\\{\\{(delh|DeletionHeader|DeletionFooter/Old|Delf|DeletionFooter|Udelf).*?\\}\\}.*?")
					&& !text.matches(String.format("(?si).*?%s.*?%s.*?", stamp, stamp))
					&& wiki.getLinksOnPage(getTitle(), "File").length == 1;
		}
	}
	
	/**
	 * Represents a DR to process and close.
	 * 
	 * @author Fastily
	 * 
	 */
	private static class ProcDR extends WAction
	{
		/**
		 * Constructor.
		 * 
		 * @param title The DR to process
		 */
		private ProcDR(String title)
		{
			super(title, null, String.format("[[%s]]", title));
		}
		
		/**
		 * Delete all files on the page and mark the DR as closed.
		 * 
		 * @param wiki The wiki object to use
		 * @return True if we were successful
		 */
		public boolean doJob(W wiki)
		{
			Commons.nukeLinksOnPage(getTitle(), summary, "File");
			text = wiki.getPageText(getTitle());
			return text != null ? wiki.edit(getTitle(), String.format("{{delh}}%n%s%n----%n'''Deleted''' -~~~~%n{{delf}}", text),
					"deleted") : false;
		}
	}
}