package jwiki.core;

import java.net.CookieManager;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

import javax.security.auth.login.LoginException;

import jwiki.util.FString;
import jwiki.util.Tuple;

/**
 * Main class of jwiki; most developers will only need this class. This class implements all queries/actions which jwiki
 * can perform on a wiki. All methods are backed by static calls and are, to my knowledge, totally thread-safe. I had
 * three specific design goals/principles in mind when creating jwiki:
 * <ol>
 * <li><span style="text-decoration:underline">Simplicity</span> &mdash; <i>Anybody</i> with a even beginner's knowledge
 * of Java shall be able to use this framework. I make it a point to avoid horrible things like complex objects and
 * convoluted calls; this project isn't intended to show folks how amazing I am at the Java language, it's designed for
 * the purpose of making their lives easy.</li>
 * <li><span style="text-decoration:underline">Speediness</span> &mdash; This framework shall emphasize performance.
 * Time is a precious resource so why waste it waiting for some dumb program to finish :)</li>
 * <li><span style="text-decoration:underline">Shortness</span> &mdash; Changes or queries to a Wiki shall be easy to
 * perform. I designed this framework so that API calls to a Wiki can be constructed in seconds with one line of code
 * consisting of, for the most part, Java primitive types. I believe one should spend less time coding, and more time
 * getting done what one initially set out to complete.</li>
 * </ol>
 * <br>
 * Here's a simple example that will edit an article by replacing the article text with some text of your choosing.
 * 
 * @author Fastily
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
			wl = parent.wl;
			cookiejar = parent.cookiejar;
			ClientAuth.copyCentralAuthCookies(parent, domain);
		}

		if (!ClientAuth.doAuth(this, !isNew))
			throw new LoginException(String.format("Failed to log-in as %s @ %s", upx.x, domain));

		wl.put(domain, this);
	}

	/**
	 * Internal constructor, use it to spawn a new wiki at a different domain associated with this object.
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

	/* //////////////////////////////////////////////////////////////////////////////// */
	/* /////////////////////////// UTILITY FUNCTIONS ////////////////////////////////// */
	/* //////////////////////////////////////////////////////////////////////////////// */

	/**
	 * Gets a Wiki object for this domain. This method is cached, to save bandwidth. We will create a new wiki as
	 * necessary. PRECONDITION: The <a href="https://www.mediawiki.org/wiki/Extension:CentralAuth">CentralAuth</a>
	 * extension MUST be installed on your MediaWiki cluster for this to work.
	 * 
	 * @param domain The domain to use
	 * @return The wiki, or null if something went wrong.
	 */
	public synchronized Wiki getWiki(String domain)
	{
		ColorLog.fyi(this, String.format("Get Wiki for %s @ %s", whoami(), domain));
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
	 * Creates a URLBuilder with a custom action & params. PRECONDITION: all <tt>params</tt> must be URLEncoded.
	 * 
	 * @param action The custom action to use
	 * @param params The params to use.
	 * @return The requested URLBuilder.
	 */
	protected URLBuilder makeUB(String action, String... params)
	{
		URLBuilder ub = new URLBuilder(domain);
		ub.setAction(action);
		ub.setParams(params);
		return ub;
	}

	/* //////////////////////////////////////////////////////////////////////////////// */
	/* /////////////////////////////////// ACTIONS //////////////////////////////////// */
	/* //////////////////////////////////////////////////////////////////////////////// */

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

	/* //////////////////////////////////////////////////////////////////////////////// */
	/* ///////////////////////////////// QUERIES ////////////////////////////////////// */
	/* //////////////////////////////////////////////////////////////////////////////// */

	/**
	 * Gets the list of usergroups (rights) a user belongs to. Sample groups: sysop, user, autoconfirmed, editor.
	 * 
	 * @param user The user to get rights information for. Do not include "User:" prefix.
	 * @return The usergroups <tt>user</tt> belongs to.
	 */
	public ArrayList<String> listGroupsRights(String user)
	{
		ColorLog.info(this, "Getting user rights for " + user);
		return MassClientQuery.listUserRights(this, user).get(0).y;
	}

	/**
	 * Gets the text of a page.
	 * 
	 * @param title The title to query
	 * @return The text of the page, or an empty string if the page is non-existent/something went wrong.
	 */
	public String getPageText(String title)
	{
		ColorLog.info(this, "Getting page text of " + title);
		ArrayList<String> temp = MassClientQuery.getPageText(this, title).get(0).y;
		return temp.isEmpty() ? "" : temp.get(0);
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
	 * Gets all the revisions of a page in descending order (newest -&gt; oldest). Caveat: Pages such as the admin's
	 * notice board have ~10<sup>6</sup> revisions. Watch your memory usage.
	 * 
	 * @param title The title to use.
	 * @return The list of revisions.
	 */
	public Revision[] getRevisions(String title)
	{
		return getRevisions(title, -1, false);
	}

	/**
	 * Gets the number of elements contained in a category.
	 * 
	 * @param title The title to query. PRECONDITION: Title *must* begin with the "Category:" prefix
	 * @return The number of elements in the category. Value returned will be -1 if Category entered was empty <b>and</b>
	 *         non-existent.
	 */
	public int getCategorySize(String title)
	{
		ColorLog.info(this, "Getting category size of " + title);
		return MassClientQuery.getCategorySize(this, title).get(0).y.intValue();
	}

	/**
	 * Get all titles in a category.
	 * 
	 * @param title The category to query, including the "Category:" prefix.
	 * @param ns Namespaces to include-only, passed in as prefixes, without the ":" (e.g. "File", "Category", "Main").
	 *           Optional, leave blank to select all namespaces.
	 * @return The list of titles in the category.
	 */
	public ArrayList<String> getCategoryMembers(String title, String... ns)
	{
		ColorLog.info(this, "Getting category members from " + title);
		URLBuilder ub = makeUB("query", "list", "categorymembers", "cmtitle", FString.enc(convertIfNotInNS(title, "Category")));
		if (ns.length > 0)
			ub.setParams("cmnamespace", FString.enc(FString.fenceMaker("|", nsl.prefixToNumStrings(ns))));
		return QueryTools.queryForStrings(this, ub, "cmlimit", "categorymembers", "title");
	}

	/**
	 * Get a limited number of titles in a category. This could be seen as an optimizing routine - the method does not
	 * fetch any more items than requested from the server.
	 * 
	 * @param title The category to query, including the "Category:" prefix.
	 * @param cap The maximum number of elements to return
	 * @param ns Namespaces to include-only, passed in as prefixes, without the ":" (e.g. "File", "Category", "Main").
	 *           Optional, leave blank to select all namespaces.
	 * @return The list of titles, as specified, in the category.
	 */
	public ArrayList<String> getCategoryMembers(String title, int cap, String... ns)
	{
		ColorLog.info(this, "Getting category members from " + title);
		URLBuilder ub = makeUB("query", "list", "categorymembers", "cmtitle", FString.enc(convertIfNotInNS(title, "Category")));
		if (ns.length > 0)
			ub.setParams("cmnamespace", FString.enc(FString.fenceMaker("|", nsl.prefixToNumStrings(ns))));
		return QueryTools.limitedQueryForStrings(this, ub, "cmlimit", cap, "categorymembers", "title");
	}

	/**
	 * Gets the categories a page is categorized in.
	 * 
	 * @param title The title to get categories of.
	 * @return A list of categories, or the empty list if something went wrong.
	 */
	public ArrayList<String> getCategoriesOnPage(String title)
	{
		ColorLog.info(this, "Getting categories on " + title);
		return MassClientQuery.getCategoriesOnPage(this, title).get(0).y;
	}

	/**
	 * Gets wiki links on a page.
	 * 
	 * @param title The title to query
	 * @param ns Namespaces to include-only, passed in as prefixes, without the ":" (e.g. "File", "Category", "Main").
	 *           Optional, leave blank to select all namespaces.
	 * @return The list of wiki links on the page.
	 */
	public ArrayList<String> getLinksOnPage(String title, String... ns)
	{
		ColorLog.info(this, "Getting wiki links on " + title);
		return MassClientQuery.getLinksOnPage(this, ns, title).get(0).y;
	}

	/**
	 * Gets all existing or non-existing wiki links on a page.
	 * 
	 * @param exists Fetch mode. Set true to get existing pages and false to get missing/non-existent pages.
	 * @param title The title to query
	 * @param ns Namespaces to include-only, passed in as prefixes, without the ":" (e.g. "File", "Category", "Main").
	 *           Optional, leave blank to select all namespaces.
	 * @return The list of existing links on <tt>title</tt>
	 */
	public ArrayList<String> getLinksOnPage(boolean exists, String title, String... ns)
	{
		ArrayList<String> l = new ArrayList<String>();
		for(Tuple<String, Boolean> t : MassClientQuery.exists(this, getLinksOnPage(title, ns).toArray(new String[0])))
			if(t.y.booleanValue() == exists)
				l.add(t.x);
		return l;
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
	 * Checks if a title exists.
	 * 
	 * @param title The title to query.
	 * @return True if the title exists.
	 */
	public boolean exists(String title)
	{
		ColorLog.info(this, "Checking to see if title exists: " + title);
		return MassClientQuery.exists(this, title).get(0).y.booleanValue();
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