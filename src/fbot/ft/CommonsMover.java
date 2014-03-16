package fbot.ft;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

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
	private static final String url = "http://tools.wmflabs.org/commonshelper/?";
	// http://bots.wmflabs.org/~richs/commonshelper.php
	
	/**
	 * The templated text to post to the wmflabs tool.
	 */
	private static final String posttext = "language=en&project=wikipedia&image=%s&newname="
			+ "&username=&commonsense=1&tusc_user=&tusc_password=&doit=Get+text&test=1";
	
	/**
	 * Our wiki object for Commons.
	 */
	protected static Wiki com;
	
	/**
	 * The wiki object for en.wikipedia.org
	 */
	protected static Wiki enwp;
	
	/**
	 * Maximum number of threads to instatiate. Default: 2
	 */
	protected static int maxthreads = 2;
	
	/**
	 * Main driver.
	 * 
	 * @param args Prog args.
	 */
	public static void main(String[] args)
	{
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
		 * The title passed into the constuctor, without the namespace.
		 */
		private String titleNNS;
		
		/**
		 * Constructor, takes the title to transfer, including the "File:" prefix.
		 * 
		 * @param title The title to use.
		 */
		protected TransferItem(String title)
		{
			super(title, null, null);
			titleNNS = Namespace.nss(title);
		}
		
		/**
		 * Performs a transfer of this item.
		 * 
		 * @param wiki The version of this wiki logged in @ commonswiki.
		 * @return True if we were successful.
		 */
		public boolean doJob(Wiki wiki)
		{
			if (wiki.exists(getTitle()))
			{
				String text = enwp.getPageText(getTitle());
				if (text != null && !text.matches("(?si).*?\\{\\{(db\\-f8)\\}\\}.*?"))
					return enwp.addText(getTitle(), "\n{{db-f8}}", "f8", true);
			}
			
			File f = downloadFile();
			String desc = getDesc();
			return f != null && desc != null ? wiki.upload(f, getTitle(), desc,
					String.format("from [[w:%s|%s]]", getTitle(), getTitle()))
					&& enwp.addText(getTitle(), "\n{{db-f8}}", "f8", true) : false;
		}
		
		/**
		 * Flags the enwp copy of this file with {{db-f8}}.
		 * 
		 * @return True if we were successful.
		 */
		protected boolean flagF8()
		{
			return enwp.addText(getTitle(), "\n{{db-f8}}", "F8", true);
		}
		
		/**
		 * Downloads the file
		 * 
		 * @return The file if we were successful, or null.
		 */
		protected File downloadFile()
		{
			String path = "./" + titleNNS;
			return FTask.downloadFile(getTitle(), path, enwp) ? new File(path) : null;
		}
		
		/**
		 * Gets the WMFLabs generated description text for this file.
		 * 
		 * @return The description text, or null if something went wrong.
		 */
		protected String getDesc()
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