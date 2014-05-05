package ft;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import jwiki.core.Wiki;
import jwiki.mbot.MBot;
import jwiki.mbot.WAction;
import jwiki.util.FString;
import jwiki.util.WikiGen;

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
	 * Wiki representing the archive bot.
	 */
	private static final Wiki archivebot = WikiGen.generate("ArchiveBot");
	
	/**
	 * Main driver.
	 * 
	 * @param args Prog args.
	 */
	public static void main(String[] args)
	{
		archivebot.nullEdit("User:ArchiveBot/DL");
		ArrayList<ProcLog> pl = new ArrayList<ProcLog>();
		for (String s : archivebot.getValidLinksOnPage("User:ArchiveBot/DL"))
			pl.add(new ProcLog(s));
		WikiGen.genM("ArchiveBot", 1).start(pl.toArray(new ProcLog[0]));
		
		String x = "Report generated @ ~~~~~\n";
		for (String s : singles)
			x += String.format("%n{{%s}}", s);
		archivebot.edit("User:ArchiveBot/SingletonDR", x, "Update report");
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
		 * The archive's header.  We'll apply this if we're creating a new archive.
		 */
		private String aHeader;
		
		/**
		 * Constructor, takes a log title as the argument.
		 * 
		 * @param title The log title.
		 */
		private ProcLog(String title)
		{
			super(title, null, "Archiving %d threads %s [[%s]]");
			
			String d = title.substring(title.indexOf('/'));
			aHeader = String.format("{{Deletion requests/Archive%s}}\n", d.replace('/', '|'));
			archive = "Commons:Deletion requests/Archive" + d;
		}
		
		/**
		 * Performs the analysis and archive.
		 * 
		 * @param wiki The wiki object to use.
		 * @return True if we were successful.
		 */
		public boolean doJob(Wiki wiki)
		{
			DRItem[] l = fetchDRs(wiki);
			new MBot(wiki, 5).start(l);
			
			ArrayList<String> toArchive = new ArrayList<String>();
			for (DRItem d : l)
			{
				if (d.canA)
					toArchive.add(d.title);
				else if (d.isSingle)
					singles.add(d.title);
			}
			
			String[] al = toArchive.toArray(new String[0]);
			if (al.length > 0) // for efficiency.
			{
				String archiveText = wiki.getPageText(archive);
				wiki.edit(archive, (archiveText != null ? archiveText : aHeader) + pool(al), String.format(summary, toArchive.size(), "from", title));
				wiki.edit(title, extract(wiki.getPageText(title), al), String.format(summary, toArchive.size(), "to", archive));
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
				x = x.replaceAll("(?i)\\s\\{\\{(" + FString.makePageTitleRegex(s) + ").*?\\}\\}", "");
			return x;
		}
		
		/**
		 * Grabs a list of DRs transcluded on the log represented by this object.
		 * 
		 * @param wiki Wiki object to use
		 * @return A list of DRs transcluded on this log.
		 */
		private DRItem[] fetchDRs(Wiki wiki)
		{
			ArrayList<DRItem> l = new ArrayList<DRItem>();
			for (String s : wiki.exists(wiki.getTemplatesOnPage(title), true))
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
	private static class DRItem extends WAction
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
			super(title, null, null);
		}
		
		/**
		 * Analyzes the DR.
		 * 
		 * @param wiki The wiki object to use
		 * @return True if we were successful.
		 */
		public boolean doJob(Wiki wiki)
		{
			text = wiki.getPageText(title);
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
				temp = temp.replaceAll("(?si)\\<(includeonly|noinclude)\\>.*?\\</(includeonly|noinclude)\\>", "");
				temp = temp.replaceAll("(?i)__(NOTOC)__", "").trim();
				canA = temp
						.matches("(?si)\\{\\{(delh|DeletionHeader).*?\\}\\}.*?\\{(DeletionFooter/Old|Delf|DeletionFooter|Udelf).*?\\}\\}");
			}
		}
		
		/**
		 * Tests if this DR is a Singleton DR (one contributor, one file, uncontested).
		 * 
		 * @param wiki The wiki object to use.
		 */
		private void isSingleton(Wiki wiki)
		{
			isSingle = text != null
					&& !text.matches("(?si).*?\\{\\{(delh|DeletionHeader|DeletionFooter/Old|Delf|DeletionFooter|Udelf).*?\\}\\}.*?")
					&& !text.matches(String.format("(?si).*?%s.*?%s.*?", stamp, stamp))
					&& wiki.getLinksOnPage(title, "File").length == 1;
		}
	}
}