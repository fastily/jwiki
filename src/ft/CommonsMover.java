package ft;

import static ft.Core.*;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import jwiki.core.FTask;
import jwiki.core.Logger;
import jwiki.core.Namespace;
import jwiki.core.Request;
import jwiki.core.Wiki;
import jwiki.mbot.MBot;
import jwiki.mbot.WAction;
import jwiki.util.FCLI;
import jwiki.util.FError;
import jwiki.util.FIO;
import jwiki.util.FString;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 * Command line utility to transwiki items from enwp to Commons. Performs rudimentary checks for inappropriate files.
 * 
 * @author Fastily
 * 
 */
public class CommonsMover
{
	/**
	 * Help/Usage string for command line.
	 */
	private static final String hstring = "CommonsMover [-help] [-c <category>|-u <user>|<titles>] [-t <threads>]";
	
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
			"Category:All Wikipedia files with unknown copyright status", "Category:Candidates for speedy deletion",
			"Category:All free in US media" };
	
	/**
	 * Our wiki object for commons & enwp.
	 */
	protected static Wiki com, enwp;
	
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
		CommandLine l = init(args, makeOptList(), hstring);
		com = user;
		enwp = user.getWiki("en.wikipedia.org");
		
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
		new MBot(user, maxthreads).start(tfl.toArray(new TransferItem[0]));
	}
	
	/**
	 * Make a list of CLI options for us.
	 * 
	 * @return The list of Command line options.
	 */
	private static Options makeOptList()
	{
		Options ol = new Options();
		
		ol.addOptionGroup(FCLI.makeOptGroup(FCLI.makeArgOption("c", "Transfer all files in this category", "category"),
				FCLI.makeArgOption("u", "Transfer al files uploaded by this user", "user")));
		ol.addOption(FCLI.makeArgOption("t", "Set the maximum number of threads", "threads"));
		
		return ol;
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
				return FError.printErrorAndReturn(title + " is not eligible for transfer", false);
			else if (!resolveDuplicates())
				return true;
			
			File f = downloadFile();
			String desc = getDesc();
			if (f != null && desc != null)
				return wiki.upload(f, transferTo, desc, String.format("from [[w:%s]]", title)) && flagF8();
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
			if (!com.exists(title))
				return true;
			try
			{
				if (com.getImageInfo(title).getSize() == enwp.getImageInfo(title).getSize())
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
			return !FString.arraysIntersect(enwp.getCategoriesOnPage(title), blacklist);
		}
		
		/**
		 * Flags the enwp copy of this file with {{db-f8}}.
		 * 
		 * @return True if we were successful.
		 */
		private boolean flagF8()
		{
			return enwp.addText(title, String.format("%n{{subst:ncd%s}}", !transferTo.equals(title) ? "|" + transferTo : ""),
					"F8", false);
		}
		
		/**
		 * Downloads the file
		 * 
		 * @return The file if we were successful, or null.
		 */
		private File downloadFile()
		{
			String tx = String.format("%d%s", titleNNS.hashCode(), titleNNS.substring(titleNNS.lastIndexOf(".")));
			return FTask.downloadFile(title, tx, enwp) ? new File(tx) : null;
		}
		
		/**
		 * Gets the WMFLabs generated description text for this file.
		 * 
		 * @return The description text, or null if something went wrong.
		 */
		private String getDesc()
		{
			Logger.fyi("Generating description page for " + title);
			try
			{
				String tl = FString.enc(titleNNS);
				String s = FIO.inputStreamToString(
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