package jwiki.commons;

import java.util.ArrayList;
import java.util.Arrays;

import jwiki.core.Contrib;
import jwiki.core.Wiki;
import jwiki.mbot.MBot;
import jwiki.mbot.WAction;
import jwiki.util.ReadFile;

/**
 * Special multi-threaded commons.wikimedia.org exclusive methods which make life so much easier.
 * 
 * @author Fastily
 */
public class Commons
{
	/**
	 * The admin object to use.
	 */
	private Wiki admin;

	/**
	 * Admin MBot
	 */
	private MBot mbwiki;

	/**
	 * Wiki MBot
	 */
	private MBot mbadmin;

	/**
	 * Creates a Commons object for us. Current domain of <tt>wiki</tt> does not have to be set to Commons. All tasks
	 * will be run out of this wiki object.
	 * 
	 * @param wiki The wiki object to create the Commons object from.
	 */
	public Commons(Wiki wiki)
	{
		this(wiki, (wiki.isAdmin() ? wiki : null));
	}

	/**
	 * Creates a Commons object for us. Non-admin tasks will be assigned to <tt>wiki</tt>, whereas admin tasks will be
	 * run from <tt>admin</tt>. Current domain of either object does not have to be set to Commons.
	 * 
	 * @param wiki wiki object to use for non-admin tasks.
	 * @param admin wiki object for admin tasks. If you're not an admin, you ought to specify this to be null, otherwise
	 *           you may get strange behavior if you execute admin tasks.
	 */
	public Commons(Wiki wiki, Wiki admin)
	{
		mbwiki = new MBot(wiki, 3);
		if (admin != null)
		{
			this.admin = admin.getWiki("commons.wikimedia.org");
			mbadmin = new MBot(admin);
		}
	}

	/**
	 * Process a list of WActions.
	 * 
	 * @param mb The MBot to use.
	 * @param pages The pages to process.
	 * @return A list of titles we didn't process.
	 */
	private String[] doAction(MBot mb, WAction... pages)
	{
		return WAction.convertToString(mb.start(pages));
	}

	/**
	 * Process a list of WActions.
	 * 
	 * @param mb The MBot to use
	 * @param pages The pages to process
	 * @return A list of objects we couldn't process.
	 */
	private String[] doAction(MBot mb, ArrayList<WAction> pages)
	{
		return doAction(mb, pages.toArray(new WAction[0]));
	}

	/**
	 * Deletes everything in Category:Fastily Test as uploader requested.
	 * 
	 * @param exit Set to true if program should exit after procedure completes.
	 * @return A list of files we didn't delete.
	 */
	public String[] nukeFastilyTest(boolean exit)
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
	public String[] clearCopyVios()
	{
		return categoryNuke(CStrings.cv, CStrings.copyvio, false, "File");
	}

	/**
	 * Deletes everything in <a href= "http://commons.wikimedia.org/wiki/Category:Other_speedy_deletions" >Category:Other
	 * speedy deletions</a>
	 * 
	 * @param reason Delete reason
	 * @param ns Namespace(s) to restrict deletion to. Leave blank to ignore namespace.
	 * @return A list of titles we didn't delete.
	 */
	public String[] clearOSD(String reason, String... ns)
	{
		return categoryNuke(CStrings.osd, reason, false, ns);
	}

	/**
	 * Deletes the titles in a category.
	 * 
	 * @param cat The category to nuke items from
	 * @param reason Delete reason
	 * @param delCat Set to true if the category should be deleted after deleting everything in it. Category is only
	 *           deleted if it is empty.
	 * @param ns Namespace filter -- anything in these namespace(s) will be deleted. Optional param -- leave blank to
	 *           select all namesapces
	 * @return A list of titles we didn't delete.
	 */
	public String[] categoryNuke(String cat, String reason, boolean delCat, String... ns)
	{
		String[] fails = nuke(reason, admin.getCategoryMembers(cat, ns));
		if (delCat && admin.getCategorySize(cat) == 0)
			admin.delete(cat, CStrings.ec);
		return fails;
	}

	/**
	 * Delete all files on a page.
	 * 
	 * @param dr The dr from which to get files.
	 * @return A list of pages we failed to delete.
	 */
	public String[] drDel(String dr)
	{
		return nukeLinksOnPage(dr, "[[" + dr + "]]", "File");
	}

	/**
	 * Performs a mass restoration.
	 * 
	 * @param reason The reason to use.
	 * @param pages The pages to restore
	 * @return A list of pages we didn't/couldn't restore.
	 */
	public String[] restore(String reason, String... pages)
	{
		return WAction.convertToString(mbadmin.massRestore(reason, pages));
	}

	/**
	 * Restore pages from a list in a file.
	 * 
	 * @param reason The reason to use
	 * @param path The path to the file
	 * @return A list of pages we didn't/couldn't restore.
	 */
	public String[] restoreFromFile(String path, String reason)
	{
		return restore(reason, new ReadFile(path).getList());
	}

	/**
	 * Nukes empty files (ie file description pages without an associated file).
	 * 
	 * @param files A list of pages in the file namespace. PRECONDITION -- The files must be in the filenamespace.
	 * @return A list ofpages we failed to process.
	 */
	public String[] nukeEmptyFiles(String... files)
	{
		ArrayList<WAction> l = new ArrayList<WAction>();
		for (String s : files)
			l.add(new WAction(s, null, CStrings.nfu) {
				public boolean doJob(Wiki wiki)
				{
					return wiki.getImageInfo(title) == null ? wiki.delete(title, summary) : true;
				}
			});

		return doAction(mbadmin, l);
	}

	/**
	 * Checks if a category is empty and deletes it if true.
	 * 
	 * @param cats The categories to check and delete.
	 * @return A list of titles we failed to delete.
	 */
	public String[] emptyCatDel(String... cats)
	{
		ArrayList<WAction> l = new ArrayList<WAction>();
		for (String s : cats)
			l.add(new WAction(s, null, CStrings.ec) {
				public boolean doJob(Wiki wiki)
				{
					return wiki.getCategorySize(title) <= 0 ? wiki.delete(title, summary) : true;
				}
			});
		return doAction(mbadmin, l);
	}

	/**
	 * Delete the contributions of a user in the specified namespace.
	 * 
	 * @param user The user whose contribs we'll be deleting.
	 * @param reason Delete reason
	 * @param ns Namespace(s) of the items to delete.
	 * @return A list of titles we didn't delete.
	 */
	public String[] nukeContribs(String user, String reason, String... ns)
	{
		ArrayList<String> l = new ArrayList<String>();
		for (Contrib c : admin.getContribs(user, ns))
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
	public String[] nukeUploads(String user, String reason)
	{
		return nuke(reason, admin.getUserUploads(user));
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
	public String[] nukeLinksOnPage(String title, String reason, String... ns)
	{
		return nuke(reason, admin.getLinksOnPage(title, ns));
	}

	/**
	 * Deletes all images linked on a page.
	 * 
	 * @param title The title to fetch images from.
	 * @param reason The reason to use when deleting
	 * @return A list of files we failed to process.
	 */
	public String[] nukeImagesOnPage(String title, String reason)
	{
		return nuke(reason, admin.getImagesOnPage(title));
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
	public String[] nuke(String reason, String... pages)
	{
		return WAction.convertToString(mbadmin.massDelete(reason, pages));
	}

	/**
	 * Nukes the pages that are in the specified namespace. Anything not in the specified namespace will not be deleted,
	 * even if it passed in.
	 * 
	 * @param reason Delete reason.
	 * @param ns Namespace to include only. This should be the namespace header, without the ":" (e.g. "File"). For main
	 *           namespace, specify the empty String.
	 * @param pages The pages to screen and then delete.
	 * 
	 * @return A list of pages we failed to delete
	 * 
	 * @see #nuke(String, String...)
	 */
	public String[] nuke(String reason, String ns, String... pages)
	{
		int ni = admin.whichNS(ns);
		ArrayList<String> todo = new ArrayList<String>();

		for (String s : pages)
			if (admin.whichNS(s) == ni)
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
	public String[] nukeFromFile(String path, String reason)
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
	public String[] removeDelete(String reason, String... titles)
	{
		return WAction.convertToString(mbwiki.massEdit(reason, "", CStrings.drregex, "", titles));
	}

	/**
	 * Removes all no perm, lic, src templates from listed titles.
	 * 
	 * @param reason Reason to use
	 * @param titles The titles to remove no perm/lic/src templates from
	 * @return A list of titles we failed to remove the templates from.
	 */
	public String[] removeLSP(String reason, String... titles)
	{
		return WAction.convertToString(mbwiki.massEdit(reason, "", CStrings.delregex, "", titles));
	}

	/**
	 * Adds text to pages.
	 * 
	 * @param reason The reason to use.
	 * @param text The text to add.
	 * @param titles The titles to work with.
	 * @return A list of titles we couldn't add text to.
	 */
	public String[] addText(String reason, String text, String... titles)
	{
		return WAction.convertToString(mbwiki.massEdit(reason, text, null, null, titles));
	}

}