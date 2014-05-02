package ft;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import jwiki.core.Wiki;
import jwiki.mbot.WAction;
import jwiki.util.FError;
import jwiki.util.FString;
import jwiki.util.ReadFile;
import jwiki.util.WikiGen;
import jwiki.util.WikiFile;

/**
 * Uploads my files to Commons. Accepts directories as arguments; Commons-acceptable files in the directories will be
 * uploaded with the parent directory name as the title, along with the current date. Categories will be set to the name
 * of the parent directory. You can specify a custom description by adding <tt>---</tt> in the title of the parent
 * directory. Any text after these three minus signs will be added to the description of the file(s) in this directory.
 * Program will also output a file named <tt>fails.txt</tt> in the working directory if we failed to upload any file(s).
 * This file can be passed in as an argument in a future run to attempt a re-upload.
 * 
 * @author Fastily
 * 
 */
public class Up
{
	/**
	 * The format string for file description pages.
	 */
	private static final String base = "=={{int:filedesc}}==\n{{Information\n|Description=%s\n|Source={{own}}\n"
			+ "|Date=%s\n|Author=~~~\n|Permission=\n|other_versions=\n}}\n\n=={{int:license-header}}==\n"
			+ "{{self|GFDL|cc-by-sa-3.0,2.5,2.0,1.0}}\n\n[[Category:%s]]\n[[Category:Files by Fastily]]";
	
	/**
	 * Formats dates for use in file description pages.
	 */
	private static final SimpleDateFormat ddf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/**
	 * Today's date to use in the title of uploaded files.
	 */
	private static final String titledate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
	
	/**
	 * Mapping construct tracking how many times we've seen files of a particular directory. Used to provide
	 * incrementing titles for files on Wiki.
	 */
	private static final HashMap<String, Integer> tracker = new HashMap<String, Integer>();
	
	/**
	 * Main driver.
	 * 
	 * @param args Prog args
	 * @throws InterruptedException Eh?
	 */
	public static void main(String[] args) throws InterruptedException
	{	
		ArrayList<UploadItem> l = new ArrayList<UploadItem>();
		for (WikiFile wf : parseArgs(args))
			l.add(genUI(wf));
		
		String[] fails = WAction.convertToString(WikiGen.genM("Fastily", 2).start(l.toArray(new UploadItem[0])));
		if (fails.length > 0)
		{
			try
			{
				FileWriter fw = new FileWriter("fails.txt");
				fw.write(FString.fenceMaker("%n", fails));
				fw.close();
			}
			catch(Throwable e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Generates an upload item for the given WikiFile.
	 * 
	 * @param f WikiFile to make upload item for.
	 * @return The upload item
	 */
	private static UploadItem genUI(WikiFile f)
	{
		String date = ddf.format(new Date(f.getFile().lastModified()));
		String parent = f.getParent(false);
		
		String desc;
		String cat;
		if (!parent.contains("---"))
		{
			desc = parent;
			cat = parent;
		}
		else
		{
			String[] temp = parent.split("\\-\\-\\-");
			if (temp.length > 2) // make sure this is a valid combo.
				FError.errAndExit(String.format("'%s' is not a valid title", parent));
			
			cat = temp[0].trim();
			desc = temp[1].trim();
		}
		
		return new UploadItem(f, genTitle(cat, f), String.format(base, desc, date, cat));
	}
	
	/**
	 * Generates a title for the given wiki file.
	 * 
	 * @param cat The category the file should be in. This is parsed out from the name of the WikiFile's parent
	 *            directory.
	 * @param f The WikiFile to use
	 * @return The title for the given WikiFile.
	 */
	private static String genTitle(String cat, WikiFile f)
	{
		int i = 1;
		if (tracker.containsKey(cat))
		{
			i = tracker.get(cat).intValue() + 1;
			tracker.put(cat, new Integer(i));
		}
		else
			tracker.put(cat, new Integer(i));
		
		return String.format("%s %d %s.%s", cat, i, titledate, f.getExtension(false).toLowerCase());
	}
	
	/**
	 * Parses command arguments accordingly.
	 * 
	 * @param args Prog args
	 * @return A list of WikiFiles we were able to get from command line input.
	 */
	private static WikiFile[] parseArgs(String[] args)
	{
		ArrayList<WikiFile> l = new ArrayList<WikiFile>();
		for (String s : args)
		{
			WikiFile w = new WikiFile(s);
			if (w.isDir())
				l.addAll(Arrays.asList(w.listFilesR(true)));
			else if (w.getExtension(false).matches("(?i)(txt)"))
			{
				l.clear();
				for (String x : new ReadFile(w.getFile()).getList())
					l.add(new WikiFile(x));
				break;
			}
		}
		return l.toArray(new WikiFile[0]);
	}
	
	/**
	 * Represents an item to upload.
	 * 
	 * @author Fastily
	 * 
	 */
	private static class UploadItem extends WAction
	{
		/**
		 * The WikiFile to use
		 */
		private WikiFile f;
		
		/**
		 * The title to upload to
		 */
		private String uploadTo;
		
		/**
		 * Constructor
		 * 
		 * @param f The WikiFile to upload
		 * @param title The title to upload to on wiki
		 * @param text The text to use on the description page.
		 */
		private UploadItem(WikiFile f, String title, String text)
		{
			super(f.getPath(), text, null);
			uploadTo = title;
			this.f = f;
		}
		
		/**
		 * Performs the upload.
		 * 
		 * @param wiki The wiki object to use
		 * @return True if we had no errors.
		 */
		public boolean doJob(Wiki wiki)
		{
			return wiki.upload(f.getFile(), uploadTo, text, " ");
		}
	}
}