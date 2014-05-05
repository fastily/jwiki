package ft;

import static ft.Core.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import jwiki.commons.CStrings;
import jwiki.core.Logger;
import jwiki.core.Wiki;
import jwiki.mbot.MBot;
import jwiki.mbot.WAction;
import jwiki.util.FCLI;
import jwiki.util.FString;
import jwiki.util.ReadFile;
import jwiki.util.WikiGen;
import jwiki.util.WikiFile;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Random file uploader. Use in conjunction with bash tools for diagnostics.
 * 
 * @author Fastily
 * 
 */
public class CC
{
	/**
	 * Upload test text.
	 */
	private static final String utt = "Recreating [[bugzilla:36587]] (i.e. [[Special:UploadStash|upload stash]] bug) & "
			+ "collecting data to log.\n{{Warning|'''Test area only!  File may be non-free.''' This is just a test"
			+ " file and any license does not apply.}}\n[[Category:Fastily Test]]";
	
	/**
	 * The help string for this method.
	 */
	private static final String hstring = "CC [-m] [-nr] [-help] [-h number] [-r retries] [-f] [-nd|-sd] [-t <textfile>|<files or directories>]";
	
	/**
	 * Flag indicating whether we should suppress deletions
	 */
	private static boolean nd = false;
	
	/**
	 * The number of times we should repeat in event of failure
	 */
	private static int repeats;
	
	/**
	 * Flag indicating if we should surpress recursive search for files.
	 */
	private static boolean nr;
	
	/**
	 * Main driver.
	 * 
	 * @param args Prog args
	 * @throws ParseException eh?
	 */
	public static void main(String[] args) throws ParseException
	{
		CommandLine l = init(args, makeOptList(), hstring);
		
		if (l.hasOption('f'))
			com.nukeFastilyTest(true);
		
		nd = l.hasOption("nd") || l.hasOption("sd");
		repeats = Integer.parseInt(l.getOptionValue('r', "1"));
		nr = l.hasOption("nr");
		
		CCW[] ccwl;
		if (l.hasOption('t'))
			ccwl = generateCCW(new ReadFile(l.getOptionValue('t')).getList());
		else
			ccwl = generateCCW(l.getArgs());
		
		WAction[] ml = new MBot(user, Integer.parseInt(l.getOptionValue('h', "1"))).start(ccwl);
		
		if (l.hasOption('m'))
			user.edit("User:" + user.whoami() + "/A7",
					"Generated at ~~~~~\n\n" + FString.fenceMaker("%n", WAction.convertToString(ml)), "Update report");
	}
	
	/**
	 * Grabs the files we're planning to upload and converts them to CCW objects. If nothing is passed into
	 * <tt>paths</tt>, search the classpath for files to uplaod.
	 * 
	 * @param paths The paths of the file(s) or directories to search for files.
	 * @return A list of uploadable files we found.
	 */
	private static CCW[] generateCCW(String... paths)
	{
		HashSet<WikiFile> sl = new HashSet<WikiFile>();
		for (String s : paths)
		{
			WikiFile t = new WikiFile(s);
			if (t.isDir())
				sl.addAll(Arrays.asList(t.listFilesR(!nr)));
			else if (t.canUp())
				sl.add(t);
		}
		
		if (sl.isEmpty())
			sl.addAll(Arrays.asList(new WikiFile(".").listFilesR(!nr)));
		
		ArrayList<CCW> x = new ArrayList<CCW>();
		for (WikiFile f : sl)
			x.add(new CCW(f));
		return x.toArray(new CCW[0]);
	}
	
	/**
	 * Makes a list of options for us.
	 * 
	 * @return The list of options.
	 */
	private static Options makeOptList()
	{
		Options ol = new Options();
		
		ol.addOption("nd", false, "Surpress deletion after upload");
		ol.addOption("sd", false, "Alias of '-nd'");
		ol.addOption("f", false, "Nuke 'Category:Fastily Test' and exit.  Overrides other options");
		ol.addOption("m", false, "Make a note of failures on-wiki");
		ol.addOption("nr", false, "Turn off recursive file search");
		
		ol.addOption(FCLI.makeArgOption("h", "Sets the number of threads of execution", "#threads"));
		ol.addOption(FCLI.makeArgOption("r", "Number of times to repeat in event of failure", "#retries"));
		ol.addOption(FCLI.makeArgOption("t", "Select files to upload from a text file", "<textfile>"));
		
		return ol;
	}
	
	/**
	 * Inner class implementing WAction so we can use this class with MBot.
	 * 
	 * @author Fastily
	 * 
	 */
	private static class CCW extends WAction
	{
		/**
		 * Our deletion account
		 */
		private static Wiki ft = WikiGen.generate("Fastily");
		
		/**
		 * The WikiFile to upload
		 */
		private WikiFile f;
		
		/**
		 * Constructor, takes a WikiFile to upload. PRECONDITION: The WikiFile must have 'uploadable' flag set.
		 * 
		 * @param f The WikiFile to upload
		 */
		protected CCW(WikiFile f)
		{
			super(f.getPath(), utt, "");
			this.f = f;
		}
		
		/**
		 * Performs upload & delete
		 * 
		 * @param wiki The wiki object to use
		 * @return True if we were sucessful.
		 */
		public boolean doJob(Wiki wiki)
		{
			for (int i = 0; i < repeats; i++)
			{
				String fn = "File:" + FString.generateRandomFileName(f);
				Logger.fyi(wiki, String.format("(%d/%d): Upload '%s' -> '%s'", i + 1, repeats, f, fn));
				
				if (wiki.upload(f.getFile(), fn, text, " "))
				{
					if (!nd)
						ft.delete(fn, CStrings.ur);
					return true;
				}
			}
			return false;
		}
	}
}