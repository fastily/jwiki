package fbot.ft;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import fbot.lib.commons.WikiGen;
import fbot.lib.core.FTask;
import fbot.lib.core.Namespace;
import fbot.lib.core.Request;
import fbot.lib.core.Tools;
import fbot.lib.core.Wiki;
import fbot.lib.core.aux.Logger;
import fbot.lib.mbot.WAction;
import fbot.lib.util.FCLI;
import fbot.lib.util.FError;
import fbot.lib.util.FString;

/**
 * Command line utility to transwiki items from enwp to Commons.
 * 
 * @author Fastily
 * 
 */
public class CommonsMover
{
	/**
	 * The URL to post to.
	 */
	private static final String url = "http://tools.wmflabs.org/commonshelper/index.php";
	
	/**
	 * The template text to post to the wmflabs tool.
	 */
	private static final String posttext = "language=en&project=wikipedia&image=%s&newname="
			+ "&ignorewarnings=1&username=&commonsense=1&tusc_user=&tusc_password=&doit=Get+text&test=1";
	
	/**
	 * Files with these categories should not be transferred.
	 */
	private static final String[] blacklist = {
			"Category:Wikipedia files on Wikimedia Commons for which a local copy has been requested to be kept",
			"Category:Media not suitable for Commons", "Category:Wikipedia files of no use beyond Wikipedia",
			"Category:All non-free media", "Category:All Wikipedia files with unknown source",
			"Category:All Wikipedia files with unknown copyright status", "Category:Candidates for speedy deletion" };
	
	/**
	 * Our wiki object for Commons.
	 */
	protected static Wiki com;
	
	/**
	 * The wiki object for en.wikipedia.org
	 */
	protected static Wiki enwp;
	
	/**
	 * Maximum number of threads to instantiate. Default: 2
	 */
	protected static int maxthreads = 2;
	
	/**
	 * Main driver.
	 * 
	 * @param args Prog args.
	 */
	public static void main(String[] args)
	{
		 args = new String[] {"File:PhoenicianB-01.png"};
		CommandLine l = parseArgs(args);
		com = WikiGen.generate("FSV");
		enwp = com.getWiki("en.wikipedia.org");
		
		if (l.hasOption('t'))
			maxthreads = Integer.parseInt(l.getOptionValue('t'));
		
		ArrayList<String> tl = new ArrayList<String>();
		if (l.hasOption('c'))
			tl.addAll(Arrays.asList(enwp.getCategoryMembers(l.getOptionValue('c'), "File")));
		else if (l.hasOption('u'))
			tl.addAll(Arrays.asList(enwp.getUserUploads(l.getOptionValue('u'))));
		else if (l.getArgs().length > 0)
		{
			ArrayList<String> x = new ArrayList<String>();
			for (String s : l.getArgs())
				x.add(enwp.convertIfNotInNS(s, "File"));
			tl.addAll(Arrays.asList(enwp.exists(x.toArray(new String[0]), true)));
		}
		
		ArrayList<TransferItem> tfl = new ArrayList<TransferItem>();
		for (String s : tl)
			tfl.add(new TransferItem(s));
		WikiGen.genM("FSV", maxthreads).start(tfl.toArray(new TransferItem[0]));
	}
	
	/**
	 * Parse command line args.
	 * 
	 * @param args The arguments passed into main.
	 * @return A CommandLine object representing the args passed in.
	 */
	private static CommandLine parseArgs(String[] args)
	{
		Options ol = new Options();
		
		ol.addOption("help", false, "Print this help message and exit");
		ol.addOptionGroup(FCLI.makeOptGroup(FCLI.makeArgOption("c", "Transfer all files in this category", "category"),
				FCLI.makeArgOption("u", "Transfer al files uploaded by this user", "user")));
		ol.addOption(FCLI.makeArgOption("t", "Set the maximum number of threads", "threads"));
		return FCLI.gnuParse(ol, args, "CommonsMover [-help] [-c <category>|-u <user>|<titles>] [-t <threads>]");
	}
	
	/**
	 * Represents a file to transfer from enwp to commons.
	 * 
	 * @author Fastily
	 * 
	 */
	protected static class TransferItem extends WAction
	{
		/**
		 * The title passed into the constructor, without the namespace.
		 */
		private String titleNNS;
		
		/**
		 * The title to transfer to on Commons.
		 */
		private String transferTo;
		
		/**
		 * Constructor, takes the title to transfer, including the "File:" prefix.
		 * 
		 * @param title The title to use.
		 */
		protected TransferItem(String title)
		{
			super(title, null, null);
			titleNNS = Namespace.nss(title);
			transferTo = title;
		}
	
		/**
		 * Performs a transfer of this item.
		 * 
		 * @param wiki The version of this wiki logged in @ commonswiki.
		 * @return True if we were successful.
		 */
		public boolean doJob(Wiki wiki)
		{
			if (!isEligible())
				return FError.printErrorAndReturn(getTitle() + " is not eligible for transfer", false);
			else if (!resolveDuplicates())
				return true;
			
			File f = downloadFile();
			String desc = getDesc();
			if (f != null && desc != null)
				return wiki.upload(f, transferTo, desc, String.format("from [[w:%s]]", getTitle())) && flagF8();
			return false;
		}
		
		/**
		 * Performs continuity sanity check. a) duplicate file on Commons under same name? b) different file on Commons
		 * under the same name (if so, adjust title)
		 * 
		 * @return True if we should continue processing this file.
		 */
		private boolean resolveDuplicates()
		{
			if (!com.exists(getTitle()))
				return true;
			try
			{
				if (com.getImageInfo(getTitle()).getSize() == enwp.getImageInfo(getTitle()).getSize())
				{
					flagF8();
					return false;
				}
				else
				{
					transferTo = String.format("File:(%d) %s", new Random().nextInt(9), titleNNS);
					return true;
				}
			}
			catch (Throwable e)
			{
				e.printStackTrace();
				return false;
			}
		}
		
		/**
		 * Crude check to see if this file is okay to transfer. Should stop most inappropriate transfers.
		 * 
		 * @return True if this file is probably okay to transfer, based on the file description page.
		 */
		private boolean isEligible()
		{
			return !FString.arraysIntersect(enwp.getCategoriesOnPage(getTitle()), blacklist);
		}
		
		/**
		 * Flags the enwp copy of this file with {{db-f8}}.
		 * 
		 * @return True if we were successful.
		 */
		private boolean flagF8()
		{
			return enwp.addText(getTitle(),
					String.format("\n{{db-f8%s}}", !transferTo.equals(getTitle()) ? "|" + Namespace.nss(transferTo) : ""), "F8",
					true);
		}
		
		/**
		 * Downloads the file
		 * 
		 * @return The file if we were successful, or null.
		 */
		private File downloadFile()
		{
			String path = "./" + titleNNS;
			return FTask.downloadFile(getTitle(), path, enwp) ? new File(path) : null;
		}
		
		/**
		 * Gets the WMFLabs generated description text for this file.
		 * 
		 * @return The description text, or null if something went wrong.
		 */
		private String getDesc()
		{
			Logger.fyi("Generating description page for " + getTitle());
			try
			{
				String tl = Tools.enc(titleNNS);
				String s = Tools.inputStreamToString(
						Request.genericPost(new URL(url), null, Request.urlenc, String.format(posttext, tl)), true);
				return s.substring(s.indexOf("{{Info"), s.indexOf("</textarea>"));
			}
			catch (Throwable e)
			{
				e.printStackTrace();
				return null;
			}
		}
	}
}