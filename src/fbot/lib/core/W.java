package fbot.lib.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fbot.lib.core.aux.Tuple;

/**
 * Entry point, helps with backwards compatibility.
 * 
 * @author Fastily
 * 
 */
public class W extends Wiki
{
	/**
	 * Constructor, auto intializes first domain to Wikimedia Commons.
	 * 
	 * @param user The username to use
	 * @param px The password to use
	 */
	public W(String user, String px)
	{
		super(user, px);
	}
	
	/**
	 * Constructor, sets username, password, and domain. The user password combo must be valid or program will exit
	 * 
	 * @param user The username to use
	 * @param px The password to use
	 * @param domain The domain to use
	 */
	public W(String user, String px, String domain)
	{
		super(user, px, domain);
	}
	
	/**
	 * Edit a page, and check if the request actually went through.
	 * 
	 * @param wiki The wiki to use.
	 * @param title The title to use
	 * @param text The text to use
	 * @param reason The edit summary to use
	 * 
	 * @return True if the operation was successful.
	 */
	public boolean edit(String title, String text, String reason)
	{
		return FAction.edit(this, title, text, reason);
	}
	
	/**
	 * Undo the top revision of a page. PRECONDITION: <tt>title</tt> must point to a valid page.
	 * 
	 * @param title The title to edit
	 * @param reason The reason to use
	 * @return True if we were successful.
	 */
	public boolean undo(String title, String reason)
	{
		return FAction.undo(this, title, reason);
	}
	
	
	/**
	 * Null edits a page.
	 * @param title The title to null edit
	 * @return True if we were successful.
	 */
	public boolean nullEdit(String title)
	{
		return edit(title, getPageText(title), "null edit");
	}
	
	/**
	 * Purge a page.
	 * @param title The title to purge
	 * @return True if we were successful.
	 */
	public boolean purge(String title)
	{
		return FAction.purge(this, title);	
	}
	
	/**
	 * Gets the text of a page on the specified wiki.
	 * 
	 * @param title The page to get text from.
	 * @return The text of the page, or null if some error occurred.
	 */
	public String getPageText(String title)
	{
		return FQuery.getPageText(this, title);
	}
	
	/**
	 * Gets the revisions of a page.
	 * 
	 * @param title The title to use.
	 * @param num The max number of revisions to return. Specify -1 to get all revisions
	 * @param olderfirst Set to true to start enumerating from the oldest revisions of the page to the newest.
	 * @return The list of revisions, as specified, or an empty list if something went wrong.
	 */
	public Revision[] getRevisions(String title, int num, boolean olderfirst)
	{
		return FQuery.getRevisions(this, title, num, olderfirst);
	}
	
	/**
	 * Gets all the revisions of a page in descending order (newest -> oldest). Caveat: Pages such as the admin's notice
	 * board have ~10<sup>6</sup> revisions. Watch your memory usage.
	 * 
	 * @param title The title to use.
	 * @return The list of revisions.
	 */
	public Revision[] getRevisions(String title)
	{
		return getRevisions(title, -1, false);
	}
	
	/**
	 * Deletes a page. You must have admin rights or this won't work.
	 * 
	 * @param title Title to delete
	 * @param reason The reason to use
	 * @return True if the operation was successful.
	 */
	public boolean delete(String title, String reason)
	{
		return FAction.delete(this, title, reason);
	}
	
	/**
	 * Undelete a page. You must have admin rights on the wiki you are trying to perform this task on, otherwise it
	 * won't go through.
	 * 
	 * @param title The title to undelete
	 * @param reason The reason to use
	 * @return True if we successfully undeleted the page.
	 */
	public boolean undelete(String title, String reason)
	{
		return FAction.undelete(this, title, reason);
	}
	
	/**
	 * Gets the number of elements in a category.
	 * 
	 * @param title The category to check, including category prefix.
	 * @return The number of items in the category, or -1 if something went wrong.
	 */
	public int getCategorySize(String title)
	{
		return FQuery.getCategorySize(this, title);
	}
	
	/**
	 * Gets ALL elements in a category.
	 * 
	 * @param title The title to retrieve pages from, including the "Category:" prefix.
	 * @param ns Namespaces to include-only, passed in as prefixes, without the ":" (e.g. "File", "Category", "Main").
	 *            Optional, leave blank to select all namespaces.
	 * @return The list of elements in the category.
	 */
	public String[] getCategoryMembers(String title, String... ns)
	{
		return getCategoryMembers(title, -1, ns);
	}
	
	/**
	 * Gets the elements in a category.
	 * 
	 * @param title The title to retrieve pages from, including the "Category:" prefix.
	 * @param max The maximum number of elements to return.
	 * @param ns Namespaces to include-only, passed in as prefixes, without the ":" (e.g. "File", "Category", "Main").
	 *            Optional, leave blank to select all namespaces.
	 * @return The list of elements in the category.
	 */
	public String[] getCategoryMembers(String title, int max, String... ns)
	{
		return FQuery.getCategoryMembers(this, title, max, ns);
	}
	
	/**
	 * Gets the links on a page.
	 * 
	 * @param title The title to get links from
	 * @param ns Namespaces to include-only, passed in as prefixes, without the ":" (e.g. "File", "Category", "Main").
	 *            Optional, leave blank to select all namespaces.
	 * @return The list of links on the page.
	 */
	public String[] getLinksOnPage(String title, String... ns)
	{
		return FQuery.getLinksOnPage(this, title, ns);
	}
	
	/**
	 * Gets all existing links on a page.
	 * @param title The title to get links from
	 * @param ns Namespaces to include-only, passed in as prefixes, without the ":" (e.g. "File", "Category", "Main").
	 *            Optional, leave blank to select all namespaces.
	 * @return The list of existing links on a page.
	 */
	public String[] getValidLinksOnPage(String title, String...ns)
	{
		return exists(getLinksOnPage(title, ns), true);
	}
	
	/**
	 * Gets the contributions of a user.
	 * 
	 * @param user The username to use, without the "User:" prefix.
	 * @param max The maximum number of results to return.
	 * @param ns Namespace filter. Optional, leave blank to select all namespaces.
	 * @return The list of contributions, as specified.
	 */
	public Contrib[] getContribs(String user, int max, String... ns)
	{
		return FQuery.getContribs(this, user, max, ns);
	}
	
	/**
	 * Gets the contributions of a user.
	 * 
	 * @param user The username to use, without the "User:" prefix.
	 * @param ns Namespace filter. Optional, leave blank to select all namespaces.
	 * @return The list of contributions, as specified.
	 */
	public Contrib[] getContribs(String user, String... ns)
	{
		return getContribs(user, -1, ns);
	}
	
	/**
	 * Gets all uploads of a user.
	 * 
	 * @param user The username, without the "User:" prefix. <tt>user</tt> must be a valid username.
	 * @return A list this user's uploads.
	 */
	public String[] getUserUploads(String user)
	{
		return FQuery.getUserUploads(this, user);
	}
	
	/**
	 * Gets the list of local pages that are displaying the given images.
	 * 
	 * @param file The file to check. Must be a valid file name, including the "File:" prefix.
	 * @return The list of pages linking to this file, or the empty array if something went wrong/file doesn't exist.
	 */
	public String[] imageUsage(String file)
	{
		return FQuery.imageUsage(this, file);
	}
	
	/**
	 * Gets the images linked on a page. By this I mean images which are displayed on a page.
	 * 
	 * @param title The title to check for images.
	 * @return The list of images on the page, or the empty array if something went wrong.
	 */
	public String[] getImagesOnPage(String title)
	{
		return FQuery.getImagesOnPage(this, title);
	}
	
	/**
	 * Determines whether the specified title exists on the wiki.
	 * 
	 * @param title The title to check.
	 * @return True if the title exists.
	 * @see #exists(String[])
	 */
	public boolean exists(String title)
	{
		return exists(new String[] { title }).get(0).y.booleanValue();
	}
	
	/**
	 * Checks to see if a page/pages exist. Returns a set of tuples (in no particular order), in the form
	 * <tt>(String title, Boolean exists)</tt>.
	 * 
	 * @param titles The title(s) to check.
	 * @return A List of tuples (String, Boolean) indicating whether a the passed in title(s) exist(s). Returns an empty
	 *         list if something went wrong.
	 * @see #exists(String)
	 */
	public List<Tuple<String, Boolean>> exists(String[] titles)
	{
		return FQuery.exists(this, titles);
	}
	
	
	/**
	 * Check if a title exists, and depending on the second param, return all existing or non-existent tiltes.
	 * @param titles The titles to check 
	 * @param e Set to true to get all existing files, or false to get all non-existent files.
	 * @return A list of titles as specified.
	 */
	public String[] exists(String[] titles, boolean e)
	{
		ArrayList<String> l = new ArrayList<String>();
		for(Tuple<String, Boolean> t : exists(titles))
			if(!(t.y.booleanValue() ^ e)) //xnor
				l.add(t.x);
		return l.toArray(new String[0]);
	}
	
	
	/**
	 * Get some information about a file on Wiki. Does not fill the thumbnail param of ImageInfo.
	 * 
	 * @param title The title of the file to use (must be in the file namespace and exist, else return Null)
	 * @return An ImageInfo object, or null if something went wrong.
	 */
	public ImageInfo getImageInfo(String title)
	{
		return getImageInfo(title, -1, -1);
	}
	
	/**
	 * Get some information about a file on Wiki.
	 * 
	 * @param title The title of the file to use (must be in the file namespace and exist, else return Null)
	 * @param height The height to scale the image to. Disable scalers by passing in a number >= 0.
	 * @param width The width to scale the image to. Disable scalers by passing in a number >= 0.
	 * @return An ImageInfo object, or null if something went wrong.
	 */
	public ImageInfo getImageInfo(String title, int height, int width)
	{
		return FQuery.getImageInfo(this, title, height, width);
	}
	
	/**
	 * Gets the templates transcluded on a page.
	 * 
	 * @param title The title to get templates from.
	 * @return A list of templates on the page.
	 */
	public String[] getTemplatesOnPage(String title)
	{
		return FQuery.getTemplatesOnPage(this, title);
	}
	
	/**
	 * Upload a media file.
	 * 
	 * @param f The file to use
	 * @param title The title to upload to. Must include "File:" prefix.
	 * @param text The text to put on the file description page
	 * @param reason The edit summary
	 * @return True if we were successful.
	 */
	public boolean upload(File f, String title, String text, String reason)
	{
		return FAction.upload(this, f, title, text, reason);
	}
	
	/**
	 * Gets the global file usage for a media file.
	 * 
	 * @param title The title to check. Must start with "File:" prefix.
	 * @return A list of tuples, (title of page, short form of wiki this page is from), denoting the global usage of
	 *         this file. Returns null if something went wrong.
	 */
	public List<Tuple<String, String>> globalUsage(String title)
	{
		return FQuery.globalUsage(this, title);
	}
	
}