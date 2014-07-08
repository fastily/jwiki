package jwiki.core;

import java.net.CookieManager;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.security.auth.login.LoginException;

import jwiki.util.FString;
import jwiki.util.Tuple;

/**
 * Entry point.  Query or perform actions on a Wiki.  Thread-safe.
 * 
 * @author Fastily
 * 
 */
public class Wiki
{
	/**
	 * Our list of currently logged in Wiki's associated with this object. Useful for global operations.
	 */
	private HashMap<String, Wiki> wl = new HashMap<String, Wiki>();

	/**
	 * Our edit token
	 */
	protected String token;

	/**
	 * Our namespace list
	 */
	protected Namespace nsl;

	/**
	 * Our domain
	 */
	protected final String domain;

	/**
	 * Our username & password: Tuple -> (user, pass).
	 */
	protected final Tuple<String, String> upx;

	/**
	 * Our cookiejar
	 */
	protected CookieManager cookiejar = new CookieManager();

	/**
	 * Constructor, sets username, password, and domain. The user password combo must be valid or program will exit
	 * 
	 * @param user The username to use
	 * @param px The password to use
	 * @param domain The domain to use
	 * @param parent The parent wiki who spawned this wiki. If this is the first wiki, disable with null.
	 * @throws LoginException If we failed to log-in.
	 */
	private Wiki(String user, String px, String domain, Wiki parent) throws LoginException
	{
		upx = new Tuple<String, String>(Namespace.nss(user), px);
		this.domain = domain;

		boolean isNew = parent != null;
		if (isNew)
		{
			//System.out.println("WE ARE HERE!!!!!");
			wl = parent.wl;
			cookiejar = parent.cookiejar;
			ClientAuth.copyCentralAuthCookies(parent, domain);
		}

		if(!ClientAuth.doAuth(this, !isNew))
			throw new LoginException(String.format("Failed to log-in as %s @ %s", upx.x, domain));

		wl.put(domain, this);
	}

	/**
	 * Internal constructor, use it to spawn a new wiki @ a different domain associated with this object.
	 * 
	 * @param curr The parent wiki object spawning this child wiki object.
	 * @param domain The new domain of the child.
	 * @throws LoginException If we failed to login.
	 */
	private Wiki(Wiki curr, String domain) throws LoginException
	{
		this(curr.upx.x, curr.upx.y, domain, curr);
	}

	/**
	 * Constructor, auto initializes first domain to Wikimedia Commons.
	 * 
	 * @param user The username to use
	 * @param px The password to use
	 * @throws LoginException If we failed to login
	 */
	public Wiki(String user, String px) throws LoginException
	{
		this(user, px, "commons.wikimedia.org");
	}

	/**
	 * Constructor, takes user, password, and domain to login as.
	 * 
	 * @param user The username to use
	 * @param px The password to use
	 * @param domain The domain name, in shorthand form (e.g. en.wikipedia.org)
	 * @throws LoginException If we failed to login
	 */
	public Wiki(String user, String px, String domain) throws LoginException
	{
		this(user, px, domain, null);
	}

	/**
	 * Gets a Wiki object for this domain. This method is cached, to save bandwidth. We will create a new wiki as
	 * necessary.
	 * 
	 * @param domain The domain to use
	 * @return The wiki, or null if something went wrong.
	 */
	public synchronized Wiki getWiki(String domain)
	{
		Logger.fyi(this, String.format("Get Wiki for %s @ %s", whoami(), domain));
		try
		{
			return isVerifiedFor(domain) ? wl.get(domain) : new Wiki(this, domain);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Gets the user we're logged in as.
	 * 
	 * @return The user we're logged in as.
	 */
	public String whoami()
	{
		return upx.x;
	}

	/**
	 * Takes a namespace and gets its number. PRECONDITION: the prefix must be a valid namespace prefix.
	 * 
	 * @param prefix The prefix to use, without the ":".
	 * @return The numerical representation of the namespace.
	 */
	protected int getNS(String prefix)
	{
		return nsl.convert(prefix);
	}

	/**
	 * Takes a namespace number and returns its name.
	 * 
	 * @param num The namespace number to get the canonical name for.
	 * @return The namespace prefix associated with this number, or null if it doesn't exist.
	 */
	protected String getNS(int num)
	{
		return nsl.convert(num);
	}

	/**
	 * Gets the number of the namespace for the title passed in. No namespace is assumed to be main namespace.
	 * 
	 * @param title The title to check the namespace number for.
	 * @return The integer number of the namespace of the title.
	 */
	public int whichNS(String title)
	{
		return nsl.whichNS(title);
	}

	/**
	 * Check if title in specified namespace. If not in specified namespace, convert it.
	 * 
	 * @param title The title to check
	 * @param ns The namespace, as a String (without ":"). Case-insensitive.
	 * @return The same title if it is in the specified namespace, else the title will be converted to the namespace.
	 */
	public String convertIfNotInNS(String title, String ns)
	{
		return whichNS(title) == getNS(ns) ? title : String.format("%s:%s", ns, Namespace.nss(title));
	}

	/**
	 * Checks if we're verified for the specified domain.
	 * 
	 * @param domain Do we have login credentials for this domain?
	 * 
	 * @return True if we're verified for the specified domain.
	 */
	public boolean isVerifiedFor(String domain)
	{
		return wl.containsKey(domain);
	}

	/**
	 * Convenience method, makes a URLBuilder.
	 * 
	 * @return A URLBuilder with our current domain.
	 */
	protected URLBuilder makeUB()
	{
		return new URLBuilder(domain);
	}

	/**
	 * Creates a URLBuilder with a custom action & params. PRECONDITION: all <tt>params</tt> must be URLEncoded.
	 * 
	 * @param action The custom action to use
	 * @param params The params to use.
	 * @return The requested URLBuilder.
	 */
	protected URLBuilder makeUB(String action, String... params)
	{
		URLBuilder ub = makeUB();
		ub.setAction(action);
		ub.setParams(params);
		return ub;
	}

	// //////////////////////////////////////////////////////////////////////////////// //
	// ///////////////////////// END OF UTILITY FUNCTIONS ///////////////////////////// //
	// //////////////////////////////////////////////////////////////////////////////// //

	/**
	 * Edit a page, and check if the request actually went through.
	 * 
	 * @param title The title to use
	 * @param text The text to use
	 * @param reason The edit summary to use
	 * 
	 * @return True if the operation was successful.
	 */
	public boolean edit(String title, String text, String reason)
	{
		return ClientAction.edit(this, title, text, reason);
	}

	/**
	 * Appends text to a page. If <tt>title</tt> does not exist, then create the page normally with <tt>text</tt>
	 * 
	 * @param title The title to edit.
	 * @param add The text to append
	 * @param reason The reason to use.
	 * @param top Set to true to prepend text. False will append text.
	 * @return True if we were successful.
	 */
	public boolean addText(String title, String add, String reason, boolean top)
	{
		String s = getPageText(title);
		return s == null ? edit(title, add, reason) : edit(title, top ? add + s : s + add, reason);
	}

	/**
	 * Removes text from a page.
	 * 
	 * @param title The title to perform the replacement at.
	 * @param regex A regex matching the text to remove.
	 * @param reason The edit summary.
	 * @return True if we were successful.
	 */
	public boolean replaceText(String title, String regex, String reason)
	{
		return replaceText(title, regex, "", reason);
	}

	/**
	 * Replaces text on a page.
	 * 
	 * @param title The title to perform replacement on.
	 * @param regex The regex matching the text to replace.
	 * @param replacement The replacing text.
	 * @param reason The edit summary.
	 * @return True if were were successful.
	 */
	public boolean replaceText(String title, String regex, String replacement, String reason)
	{
		String s = getPageText(title);
		return s != null ? edit(title, s.replaceAll(regex, replacement), reason) : false;
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
		return ClientAction.undo(this, title, reason);
	}

	/**
	 * Null edits a page.
	 * 
	 * @param title The title to null edit
	 * @return True if we were successful.
	 */
	public boolean nullEdit(String title)
	{
		return edit(title, getPageText(title), "null edit");
	}

	/**
	 * Purge a page.
	 * 
	 * @param title The title to purge
	 * @return True if we were successful.
	 */
	public boolean purge(String title)
	{
		return ClientAction.purge(this, title);
	}

	/**
	 * Gets the list of groups a user is in.
	 * 
	 * @return A list of user groups, or the empty list if something went wrong.
	 */
	public ArrayList<String> listGroupsRights()
	{
		return ClientQuery.listGroupsRights(this);
	}

	/**
	 * Determines if we're an admin. Note that this method does not cache, so you should make one yourself if you need to
	 * know a user's rights status multiple times.
	 * 
	 * @return True if this user is a sysop.
	 */
	public boolean isAdmin()
	{
		return listGroupsRights().contains("sysop");
	}

	/**
	 * Gets the text of a page on the specified wiki.
	 * 
	 * @param title The page to get text from.
	 * @return The text of the page, or null if some error occurred.
	 */
	public String getPageText(String title)
	{
		return ClientQuery.getPageText(this, title);
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
		return ClientQuery.getRevisions(this, title, num, olderfirst);
	}

	/**
	 * Gets all the revisions of a page in descending order (newest -&gt; oldest). Caveat: Pages such as the admin's notice
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
		return ClientAction.delete(this, title, reason);
	}

	/**
	 * Undelete a page. You must have admin rights on the wiki you are trying to perform this task on, otherwise it won't
	 * go through.
	 * 
	 * @param title The title to undelete
	 * @param reason The reason to use
	 * @return True if we successfully undeleted the page.
	 */
	public boolean undelete(String title, String reason)
	{
		return ClientAction.undelete(this, title, reason);
	}

	/**
	 * Gets the number of elements in a category.
	 * 
	 * @param title The category to check, including category prefix.
	 * @return The number of items in the category, or -1 if something went wrong.
	 */
	public int getCategorySize(String title)
	{
		return ClientQuery.getCategorySize(this, title);
	}

	/**
	 * Gets ALL elements in a category.
	 * 
	 * @param title The title to retrieve pages from, including the "Category:" prefix.
	 * @param ns Namespaces to include-only, passed in as prefixes, without the ":" (e.g. "File", "Category", "Main").
	 *           Optional, leave blank to select all namespaces.
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
	 *           Optional, leave blank to select all namespaces.
	 * @return The list of elements in the category.
	 */
	public String[] getCategoryMembers(String title, int max, String... ns)
	{
		return ClientQuery.getCategoryMembers(this, title, max, ns);
	}

	/**
	 * Gets the categories a page is categorized in.
	 * 
	 * @param title The title to get categories of.
	 * @return A list of categories, or the empty list if something went wrong.
	 */
	public String[] getCategoriesOnPage(String title)
	{
		return ClientQuery.getCategoriesOnPage(this, title);
	}

	/**
	 * Gets the links on a page.
	 * 
	 * @param title The title to get links from
	 * @param ns Namespaces to include-only, passed in as prefixes, without the ":" (e.g. "File", "Category", "Main").
	 *           Optional, leave blank to select all namespaces.
	 * @return The list of links on the page.
	 */
	public String[] getLinksOnPage(String title, String... ns)
	{
		return ClientQuery.getLinksOnPage(this, title, ns);
	}

	/**
	 * Gets all existing links on a page.
	 * 
	 * @param title The title to get links from
	 * @param ns Namespaces to include-only, passed in as prefixes, without the ":" (e.g. "File", "Category", "Main").
	 *           Optional, leave blank to select all namespaces.
	 * @return The list of existing links on a page.
	 */
	public String[] getValidLinksOnPage(String title, String... ns)
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
		return ClientQuery.getContribs(this, user, max, ns);
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
		return ClientQuery.getUserUploads(this, user);
	}

	/**
	 * Gets the list of local pages that are displaying the given image.
	 * 
	 * @param file The file to check. Must be a valid file name, including the "File:" prefix.
	 * @return The list of pages linking to this file, or the empty array if something went wrong/file doesn't exist.
	 */
	public String[] imageUsage(String file)
	{
		return ClientQuery.imageUsage(this, file);
	}

	/**
	 * Gets the images linked on a page. By this I mean images which are displayed on a page.
	 * 
	 * @param title The title to check for images.
	 * @return The list of images on the page, or the empty array if something went wrong.
	 */
	public String[] getImagesOnPage(String title)
	{
		return ClientQuery.getImagesOnPage(this, title);
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
	public List<Tuple<String, Boolean>> exists(String... titles)
	{
		return ClientQuery.exists(this, titles);
	}

	/**
	 * Check if a title exists, and depending on the second param, return all existing or non-existent tiltes.
	 * 
	 * @param titles The titles to check
	 * @param e Set to true to get all existing files, or false to get all non-existent files.
	 * @return A list of titles as specified.
	 */
	public String[] exists(String[] titles, boolean e)
	{
		return FString.booleanTuple(exists(titles), e);
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
	 * @param height The height to scale the image to. Disable scalers by passing in a number &ge; 0.
	 * @param width The width to scale the image to. Disable scalers by passing in a number &ge; 0.
	 * @return An ImageInfo object, or null if something went wrong.
	 */
	public ImageInfo getImageInfo(String title, int height, int width)
	{
		return ClientQuery.getImageInfo(this, title, height, width);
	}

	/**
	 * Gets the templates transcluded on a page.
	 * 
	 * @param title The title to get templates from.
	 * @return A list of templates on the page.
	 */
	public String[] getTemplatesOnPage(String title)
	{
		return ClientQuery.getTemplatesOnPage(this, title);
	}

	/**
	 * Gets a list of pages transcluding <tt>title</tt>.
	 * 
	 * @param title The title to get transclusions of.
	 * @return A list of transclusions, or the empty list if something went wrong.
	 */
	public String[] whatTranscludesHere(String title)
	{
		return ClientQuery.whatTranscludesHere(this, title);
	}

	/**
	 * Upload a media file.
	 * 
	 * @param p The file to use
	 * @param title The title to upload to. Must include "File:" prefix.
	 * @param text The text to put on the file description page
	 * @param reason The edit summary
	 * @return True if we were successful.
	 */
	public boolean upload(Path p, String title, String text, String reason)
	{
		return ClientAction.upload(this, p, title, text, reason);
	}

	/**
	 * Gets the global file usage for a media file.
	 * 
	 * @param title The title to check. Must start with "File:" prefix.
	 * @return A list of tuples, (title of page, short form of wiki this page is from), denoting the global usage of this
	 *         file. Returns empty list if something went wrong.
	 */
	public ArrayList<Tuple<String, String>> globalUsage(String title)
	{
		return ClientQuery.globalUsage(this, title);
	}

	/**
	 * Gets the direct links to a page (excluding links from redirects). To get links from redirects, use
	 * <tt>getRedirects()</tt> and call this method on each element in the list returned.
	 * 
	 * @param title The title to use
	 * @return A list of links to this page.
	 */
	public String[] whatLinksHere(String title)
	{
		return ClientQuery.whatLinksHere(this, title);
	}

	/**
	 * Gets the redirects of a page.
	 * 
	 * @param title The title to check.
	 * @return The redirects linking to this page.
	 */
	public String[] getRedirects(String title)
	{
		return ClientQuery.getRedirects(this, title);
	}

	/**
	 * Gets a list of pages on the Wiki.
	 * 
	 * @param prefix Get files starting with this String. DO NOT include a namespace prefix (e.g. "File:"). Param is
	 *           optional, use null or empty string to disable.
	 * @param redirectsonly Set this to true to get redirects only.
	 * @param max The max number of titles to return. Specify -1 to get all pages.
	 * @param ns The namespace identifier (e.g. "File").
	 * @return A list of titles as specified.
	 */
	public String[] allPages(String prefix, boolean redirectsonly, int max, String ns)
	{
		return ClientQuery.allPages(this, prefix, redirectsonly, max, ns);
	}

	/**
	 * Does the same thing as Special:PrefixIndex.
	 * 
	 * @param namespace The namespace identifier, without the ':' (e.g. "File")
	 * @param prefix Get all titles in the specified namespace, that start with this String.
	 * @return The list of titles, as specified.
	 */
	public String[] prefixIndex(String namespace, String prefix)
	{
		return allPages(prefix, false, -1, namespace);
	}

	/**
	 * Gets a list of duplicate files. Tuple returned is as follows <tt>(String, Boolean)</tt>, where the string is the
	 * title of the duplicate, and where the boolean indicates whether the file is part of the local repository (enwp is
	 * local repository and commons is the shared repository). If you're using a wiki that is not associated with another
	 * wiki, ignore this param.
	 * 
	 * @param file The file to get duplicates of
	 * @return The list of files.
	 */
	public ArrayList<Tuple<String, Boolean>> getDuplicatesOf(String file)
	{
		return ClientQuery.getDuplicatesOf(this, file);
	}

	/**
	 * List all duplicate files.
	 * 
	 * @param page The page to fetch dupes of
	 * @param max The maximum number of dupes to get. Disable with -1.
	 * @return A list of duplicate files.
	 */
	public String[] listDuplicateFiles(String page, int max)
	{
		return ClientQuery.listSpecialPages(this, page, max);
	}
}