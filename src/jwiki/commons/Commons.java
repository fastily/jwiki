package jwiki.commons;

import java.util.ArrayList;
import java.util.Arrays;

import jwiki.core.Contrib;
import jwiki.core.Wiki;
import jwiki.mbot.MBot;
import jwiki.mbot.WAction;
import jwiki.util.FError;
import jwiki.util.ReadFile;

/**
 * Special commons.wikimedia.org exclusive methods which make my life so much easier.
 * 
 * @author Fastily
 */
public class Commons
{
	/**
	 * FSV @ commonswiki
	 */
	public static final Wiki fsv = generateCOM("FSV");
	
	/**
	 * Fastily @ commonswiki
	 */
	public static final Wiki fastily = generateCOM("Fastily");
	
	/**
	 * Hide from javadoc
	 */
	private Commons()
	{
		
	}
	
	/**
	 * Creates the default wiki object.  Will cause program to exit if we encountered an error.
	 * @return FSV@commonswiki
	 */
	private static Wiki generateCOM(String user)
	{
		try
		{
			//System.out.println("This is running");
			return WikiGen.generate(user);
		}
		catch (Throwable e)
		{
			FError.errAndExit(e, String.format("Could not create %s @ commonswiki.  Stop.", user));
			return null;
		}
	}
	
	/**
	 * Deletes everything in Category:Fastily Test as uploader requested.
	 * 
	 * @param exit Set to true if program should exit after procedure completes.
	 * @return A list of files we didn't delete.
	 */
	public static String[] nukeFastilyTest(boolean exit)
	{
		ArrayList<String> fails = new ArrayList<String>();
		fails.addAll(Arrays.asList(categoryNuke("Fastily Test", CStrings.ur, false)));
		fails.addAll(Arrays.asList(nukeUploads("FSVI", CStrings.ur)));
		if (exit)
			System.exit(0);
		return fails.toArray(new String[0]);
	}
	
	/**
	 * Deletes all the files in <a href="http://commons.wikimedia.org/wiki/Category:Copyright_violations"
	 * >Category:Copyright violations</a>.
	 */
	public static String[] clearCopyVios()
	{
		return categoryNuke(CStrings.cv, CStrings.copyvio, false, "File");
	}
	
	/**
	 * Deletes everything in <a href= "http://commons.wikimedia.org/wiki/Category:Other_speedy_deletions"
	 * >Category:Other speedy deletions</a>
	 * 
	 * @param reason Delete reason
	 * @param ns Namespace(s) to restrict deletion to. Leave blank to ignore namespace.
	 * @return A list of titles we didn't delete.
	 */
	public static String[] clearOSD(String reason, String... ns)
	{
		return categoryNuke(CStrings.osd, reason, false, ns);
	}
	
	/**
	 * Deletes the titles in a category.
	 * 
	 * @param cat The category to nuke items from
	 * @param reason Delete reason
	 * @param delCat Set to true if the category should be deleted after deleting everything in it. Category is only
	 *            deleted if it is empty.
	 * @param ns Namespace filter -- anything in these namespace(s) will be deleted. Optional param -- leave blank to
	 *            select all namesapces
	 * @return A list of titles we didn't delete.
	 */
	public static String[] categoryNuke(String cat, String reason, boolean delCat, String... ns)
	{
		String[] fails = nuke(reason, fastily.getCategoryMembers(cat, ns));
		if (delCat && fastily.getCategorySize(cat) == 0)
			fastily.delete(cat, CStrings.ec);
		return fails;
	}
	
	/**
	 * Delete all files on a page.
	 * 
	 * @param dr The dr from which to get files.
	 * @return A list of pages we failed to delete.
	 */
	public static String[] drDel(String dr)
	{
		return nukeLinksOnPage(dr, "[[" + dr + "]]", "File");
	}
	
	/**
	 * Nukes empty files (ie file description pages without an associated file).
	 * 
	 * @param files A list of pages in the file namespace. PRECONDITION -- The files must be in the filenamespace.
	 * @return A list ofpages we failed to process.
	 */
	public static String[] nukeEmptyFiles(String... files)
	{
		ArrayList<WAction> l = new ArrayList<WAction>();
		for (String s : files)
			l.add(new WAction(s, null, CStrings.nfu) {
				public boolean doJob(Wiki wiki)
				{
					return wiki.getImageInfo(title) == null ? wiki.delete(title, summary) : true;
				}
			});
		
		return doAction("Fastily", l.toArray(new WAction[0]));
	}
	
	/**
	 * Checks if a category is empty and deletes it if true.
	 * 
	 * @param cats The categories to check and delete.
	 * @return A list of titles we failed to delete.
	 */
	public static String[] emptyCatDel(String... cats)
	{
		ArrayList<WAction> l = new ArrayList<WAction>();
		for (String s : cats)
			l.add(new WAction(s, null, CStrings.ec) {
				public boolean doJob(Wiki wiki)
				{
					return wiki.getCategorySize(title) <= 0 ? wiki.delete(title, summary) : true;
				}
			});
		return doAction("Fastily", l.toArray(new WAction[0]));
	}
	
	/**
	 * Delete the contributions of a user in the specified namespace.
	 * 
	 * @param user The user whose contribs we'll be deleting.
	 * @param reason Delete reason
	 * @param ns Namespace(s) of the items to delete.
	 * @return A list of titles we didn't delete.
	 */
	public static String[] nukeContribs(String user, String reason, String... ns)
	{
		ArrayList<String> l = new ArrayList<String>();
		for (Contrib c : fastily.getContribs(user, ns))
			l.add(c.getTitle());
		
		return nuke(reason, l.toArray(new String[0]));
	}
	
	/**
	 * Delete uploads of a user.
	 * 
	 * @param user The user whose uploads we'll be deleting. Do not use "User:" prefix.
	 * @param reason The reason to use
	 * @return A list of titles we didn't delet
	 */
	public static String[] nukeUploads(String user, String reason)
	{
		return nuke(reason, fastily.getUserUploads(user));
	}
	
	/**
	 * Deletes all links on a page in the specified namespace.
	 * 
	 * @param title The title to fetch links from
	 * @param reason Delete reason
	 * @param ns links returned will be in these namespace(s). Optional param -- leave blank to select all namespaces.
	 * @return The links on the title in the requested namespace
	 * 
	 * @see #nukeLinksOnPage(String, String)
	 * 
	 */
	public static String[] nukeLinksOnPage(String title, String reason, String... ns)
	{
		return nuke(reason, fastily.getLinksOnPage(title, ns));
	}
	
	/**
	 * Deletes all images linked on a page.
	 * @param title The title to fetch images from.
	 * @param reason The reason to use when deleting
	 * @return A list of files we failed to process.
	 */
	public static String[] nukeImagesOnPage(String title, String reason)
	{
		return nuke(reason, fastily.getImagesOnPage(title));
	}
	
	/**
	 * Delete pages on Commons.
	 * 
	 * @param reason Delete reason.
	 * @param pages Pages to delete.
	 * 
	 * @return A list of pages we failed to delete
	 * @see #nuke(String, String, String...)
	 */
	public static String[] nuke(String reason, String... pages)
	{
		return WAction.convertToString(WikiGen.genM("Fastily").massDelete(reason, pages));
	}
	
	/**
	 * Process a list of WActions.
	 * 
	 * @param user The username to run method with. Should be corresponding user with WikiGen
	 * @param pages The WActions to process.
	 * @return A list of pages we didn't process.
	 */
	public static String[] doAction(String user, WAction... pages)
	{
		return WAction.convertToString(WikiGen.genM(user).start(pages));
	}
	
	/**
	 * Nukes the pages that are in the specified namespace. Anything not in the specified namespace will not be deleted,
	 * even if it passed in.
	 * 
	 * @param reason Delete reason.
	 * @param ns Namespace to include only. This should be the namespace header, without the ":" (e.g. "File"). For main
	 *            namespace, specify the empty String.
	 * @param pages The pages to screen and then delete.
	 * 
	 * @return A list of pages we failed to delete
	 * 
	 * @see #nuke(String, String...)
	 */
	public static String[] nuke(String reason, String ns, String... pages)
	{
		int ni = fastily.whichNS(ns);
		ArrayList<String> todo = new ArrayList<String>();
		
		for (String s : pages)
			if (fastily.whichNS(s) == ni)
				todo.add(s);
		
		return nuke(reason, todo.toArray(new String[0]));
	}
	
	/**
	 * Delete pages listed in a text file. Encoding should be UTF-8. One item per line.
	 * 
	 * @param path The path to the file to use.
	 * @param reason Reason to use when deleting
	 * @return A list of pages we failed to delete.
	 * @see #nuke(String, String...)
	 */
	public static String[] nukeFromFile(String path, String reason)
	{
		return nuke(reason, new ReadFile(path).getList());
	}
	
	/**
	 * Removes <tt>{{delete}}</tt> templates from the listed titles.
	 * 
	 * @param reason Reason to use
	 * @param titles The titles to remove <tt>{{delete}}</tt> from
	 * @return A list of titles we didn't remove the templates froms.
	 */
	public static String[] removeDelete(String reason, String... titles)
	{
		return WAction.convertToString(new MBot(fsv).massEdit(reason, "", CStrings.drregex, "", titles));
	}
	
	/**
	 * Removes all no perm, lic, src templates from listed titles.
	 * 
	 * @param reason Reason to use
	 * @param titles The titles to remove no perm/lic/src templates from
	 * @return A list of titles we failed to remove the templates from.
	 */
	public static String[] removeLSP(String reason, String... titles)
	{
		return WAction.convertToString(new MBot(fsv).massEdit(reason, "", CStrings.delregex, "", titles));
	}
	
	/**
	 * Adds text to pages.
	 * 
	 * @param reason The reason to use.
	 * @param text The text to add.
	 * @param titles The titles to work with.
	 * @return A list of titles we couldn't add text to.
	 */
	public static String[] addText(String reason, String text, String... titles)
	{
		return WAction.convertToString(new MBot(fsv).massEdit(reason, text, null, null, titles));
	}
	
}