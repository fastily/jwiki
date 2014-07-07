package ft;

import static ft.Core.*;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import jwiki.core.ClientTask;
import jwiki.core.Logger;
import jwiki.core.Namespace;
import jwiki.core.ClientRequest;
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
			+ "&ignorewarnings=1&doit=Get+text&test=%%2F";

	/**
	 * Files with these categories should not be transferred.
	 */
	private static final String[] blacklist = {
			"Category:Wikipedia files on Wikimedia Commons for which a local copy has been requested to be kept",
			"Category:Media not suitable for Commons", "Category:Wikipedia files of no use beyond Wikipedia",
			"Category:All non-free media", "Category:All Wikipedia files with unknown source",
			"Category:All Wikipedia files with unknown copyright status", "Category:Candidates for speedy deletion",
			"Category:All free in US media", "Category:Files deleted on Wikimedia Commons",
			"Category:All Wikipedia files with the same name on Wikimedia Commons",
			"Category:All Wikipedia files with a different name on Wikimedia Commons" };

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
			if (FString.arraysIntersect(enwp.getCategoriesOnPage(title), blacklist)) // check copyright
				return FError.printErrorAndReturn(title + " is not eligible for transfer", false);
			else if (!checkForDupes())
				return true;

			if (com.exists(title)) // if title already exists, randomize name
				transferTo = String.format("File:(%d) %s", new Random().nextInt(200), titleNNS);

			String desc;
			File f;
			if ((desc = getDesc()) != null && (f = downloadFile()) != null)
				return wiki.upload(Paths.get(f.getAbsolutePath()), transferTo, desc, String.format("from [[w:%s]] ([[Commons:CommonsMover|CM]])", title))
						&& flagF8(transferTo);
			return false;
		}

		/**
		 * Does the file already exist on Commons? If so, flag its enwp copy and return false.
		 * 
		 * @return True if we should continue processing this file.
		 */
		private boolean checkForDupes()
		{
			String[] dl = FString.booleanTuple(enwp.getDuplicatesOf(title), true);
			if(dl.length > 0)
			{
				flagF8(dl[0]);
				return false;
			}
			return true;
		}

		/**
		 * Flags the enwp copy of this file with {{db-f8}}.
		 * 
		 * @return True if we were successful.
		 */
		private boolean flagF8(String transfer)
		{
			return enwp.addText(
					title,
					String.format("%n{{subst:ncd%s|reviewer={{subst:REVISIONUSER}}}}", !transfer.equals(title) ? "|1="
							+ Namespace.nss(transfer) : ""), "now on Commons ([[c:Commons:CommonsMover|CM]])", false);
		}

		/**
		 * Downloads the file
		 * 
		 * @return The file if we were successful, or null.
		 */
		private File downloadFile()
		{
			String tx = String.format("%d%s", Math.abs(titleNNS.hashCode()), titleNNS.substring(titleNNS.lastIndexOf(".")));
			return ClientTask.downloadFile(title, tx, enwp) ? new File(tx) : null;
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
						ClientRequest.genericPOST(new URL(url), null, ClientRequest.urlenc, String.format(posttext, tl)));
				return s.substring(s.indexOf("{{Info"), s.indexOf("</textarea>"));
			}
			catch (Throwable e)
			{
				e.printStackTrace();
				System.err.println("Skipping " + title);
				return null;
			}
		}
	}
}