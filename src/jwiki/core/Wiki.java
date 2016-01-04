package jwiki.core;

import java.net.CookieManager;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginException;

import jwiki.dwrap.Contrib;
import jwiki.dwrap.ImageInfo;
import jwiki.dwrap.Revision;
import jwiki.util.FL;
import jwiki.util.FString;
import jwiki.util.Tuple;

/**
 * Main class of libjwiki. Most developers will only need this class. This class implements all queries/actions which
 * libjwiki can perform on a wiki. All methods are backed by static calls and are thread-safe.
 * 
 * @author Fastily
 */
public class Wiki
{
	/**
	 * Our list of currently logged in Wiki's associated with this object. Useful for global operations.
	 */
	private HashMap<String, Wiki> wl = new HashMap<>();

	/**
	 * Our edit token
	 */
	protected String token;

	/**
	 * Our namespace manager
	 */
	protected NS.NSManager nsl;

	/**
	 * Our domain
	 */
	protected final String domain;

	/**
	 * Our username &amp; password: Tuple -&gt; (user, pass).
	 */
	protected final Tuple<String, String> upx;

	/**
	 * Our cookiejar
	 */
	protected CookieManager cookiejar = new CookieManager();

	/**
	 * Our MBot for mass actions.
	 */
	private final MBot mbot;

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
		upx = new Tuple<>(user, px);
		this.domain = domain;
		mbot = new MBot(this);

		boolean isNew = parent != null;
		if (isNew)
		{
			wl = parent.wl;
			cookiejar = parent.cookiejar;
			Auth.copyCentralAuthCookies(parent, domain);
		}

		if (!Auth.doAuth(this, !isNew))
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
			return wl.containsKey(domain) ? wl.get(domain) : new Wiki(this, domain);
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
	 * Takes a Namespace prefix and gets a NS representation of it. PRECONDITION: the prefix must be a valid namespace
	 * prefix. WARNING: This method is CASE-SENSITIVE, so be sure to spell and capitalize the prefix <b>exactly</b> as it
	 * would appear on-wiki.
	 * 
	 * @param prefix The prefix to use, without the ":".
	 * @return An NS representation of the prefix.
	 */
	public NS getNS(String prefix)
	{
		return nsl.get(prefix);
	}

	/**
	 * Gets the namespace, in NS form, of a title. No namespace or an invalid namespace is assumed to be part of Main.
	 * 
	 * @param title The title to get an NS for.
	 * @return The title's NS.
	 */
	public NS whichNS(String title)
	{
		return nsl.whichNS(title);
	}

	/**
	 * Strip the namespace from a title.
	 * 
	 * @param title The title to strip the namespace from
	 * @return The title without a namespace
	 */
	public String nss(String title)
	{
		return nsl.nss(title);
	}

	/**
	 * Filters pages by namespace. Only pages with a namespace in <code>ns</code> are selected.
	 * 
	 * @param pages Titles to filter
	 * @param ns Pages in this/these namespace(s) will be returned.
	 * @return Titles belonging to a NS in <code>ns</code>
	 */
	public ArrayList<String> filterByNS(ArrayList<String> pages, NS... ns)
	{
		ArrayList<String> l = new ArrayList<>();
		ArrayList<NS> nl = new ArrayList<>();
		Collections.addAll(nl, ns);

		for (String s : pages)
			if (nl.contains(whichNS(s)))
				l.add(s);

		return l;
	}

	/**
	 * Check if a title in specified namespace and convert it if it is not.
	 * 
	 * @param title The title to check
	 * @param ns The namespace to convert the title to.
	 * @return The same title if it is in <code>ns</code>, or the converted title.
	 */
	public String convertIfNotInNS(String title, NS ns)
	{
		String text = whichNS(title).equals(ns) ? title : String.format("%s:%s", nsl.toString(ns, false), nsl.nss(title));
		return text;
	}

	/**
	 * Creates a template URLBuilder with a custom action &amp; params. PRECONDITION: <code>params</code> must be
	 * URLEncoded.
	 * 
	 * @param action The action to use
	 * @param params The params to use.
	 * @return The template URLBuilder.
	 */
	protected URLBuilder makeUB(String action, String... params)
	{
		return new URLBuilder(domain, action, params.length > 0 ? FL.pMap(params) : null);
	}

	/**
	 * Submit a task to be processed using concurrency.
	 * 
	 * @param tasks The tasks to process
	 * @param maxThreads The maximum number of threads to instantiate.
	 * @param <T1> An object implementing doJob() in MBot.Task
	 * @return A list of tasks we couldn't execute.
	 */
	public <T1 extends MBot.Task> ArrayList<MBot.Task> submit(ArrayList<T1> tasks, int maxThreads)
	{
		return mbot.submit(tasks, maxThreads);
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
		return WAction.edit(this, title, text, reason, true);
	}

	/**
	 * Appends text to a page. If <code>title</code> does not exist, then create the page normally with <code>text</code>
	 * 
	 * @param title The title to edit.
	 * @param add The text to append
	 * @param reason The reason to use.
	 * @param top Set to true to prepend text. False will append text.
	 * @return True if we were successful.
	 */
	public boolean addText(String title, String add, String reason, boolean top)
	{
		return WAction.addText(this, title, add, reason, !top);
	}

	/**
	 * Removes text from a page. Does nothing if the replacement requested wouldn't change any text on wiki (method still
	 * returns true however).
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
	 * Replaces text on a page. Does nothing if the replacement requested wouldn't change any text on wiki (method still
	 * returns true however).
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
		if (s == null)
			return false;

		String rx = s.replaceAll(regex, replacement);
		return rx.equals(s) ? true : edit(title, rx, reason);
	}

	/**
	 * Undo the top revision of a page. PRECONDITION: <code>title</code> must point to a valid page.
	 * 
	 * @param title The title to edit
	 * @param reason The reason to use
	 * @return True if we were successful.
	 */
	public boolean undo(String title, String reason)
	{
		return WAction.undo(this, title, reason);
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
		return WAction.purge(this, title);
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
		return WAction.delete(this, title, reason);
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
		ColorLog.info(this, "Restoring " + title);
		return WAction.undelete(this, title, reason, false);
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
		return WAction.upload(this, p, title, text, reason);
	}

	/* //////////////////////////////////////////////////////////////////////////////// */
	/* ///////////////////////////////// QUERIES ////////////////////////////////////// */
	/* //////////////////////////////////////////////////////////////////////////////// */

	/**
	 * Gets the list of usergroups (rights) a user belongs to. Sample groups: sysop, user, autoconfirmed, editor.
	 * 
	 * @param user The user to get rights information for. Do not include "User:" prefix.
	 * @return The usergroups <code>user</code> belongs to.
	 */
	public ArrayList<String> listGroupsRights(String user)
	{
		ColorLog.info(this, "Getting user rights for " + user);
		return MQuery.listUserRights(this, FL.toSAL(user)).get(0).y;
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
		return MQuery.getPageText(this, FL.toSAL(title)).get(title);
	}

	/**
	 * Gets the revisions of a page.
	 * 
	 * @param title The title to query
	 * @param cap The maximum number of results to return. Optional param: set to any number zero or less to disable.
	 * @param olderFirst Set to true to enumerate from older → newer revisions
	 * @return A list of page revisions
	 */
	public ArrayList<Revision> getRevisions(String title, int cap, boolean olderFirst)
	{
		ColorLog.info(this, "Getting revisions from " + title);
		HashMap<String, String> pl = FL.pMap("prop", "revisions", "rvprop",
				FString.pipeFence("timestamp", "user", "comment", "content"), "titles", title);
		if (olderFirst)
			pl.put("rvdir", "newer"); // MediaWiki is weird.

		RSet rs = new SQ(this, "rvlimit", cap, pl).multiQuery();
		return FL.toAL(rs.getJOofJAStream("revisions").map(x -> new Revision(title, x)));
	}

	/**
	 * Gets all the revisions of a page. Caveat: Pages such as the admin's notice board have ~10<sup>6</sup> revisions.
	 * Watch your memory usage. Revisions are returned in order of newer → older revisions
	 * 
	 * @param title The title to query.
	 * @return A list of page revisions
	 */
	public ArrayList<Revision> getRevisions(String title)
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
		return MQuery.getCategorySize(this, FL.toSAL(title)).get(title);
	}

	/**
	 * Get all titles in a category.
	 * 
	 * @param title The category to query, including the "Category:" prefix.
	 * @param ns Namespace filter. Any title not in the specified namespace(s) will be ignored. Leave blank to select all
	 *           namespaces.
	 * @return The list of titles in the category.
	 */
	public ArrayList<String> getCategoryMembers(String title, NS... ns)
	{
		return getCategoryMembers(title, -1, ns);
	}

	/**
	 * Get a limited number of titles in a category. This could be seen as an optimizing routine - the method does not
	 * fetch any more items than requested from the server.
	 * 
	 * @param title The category to query, including the "Category:" prefix.
	 * @param cap The maximum number of elements to return. Optional param - set to 0 to disable.
	 * @param ns Namespace filter. Any title not in the specified namespace(s) will be ignored. Leave blank to select all
	 *           namespaces.
	 * @return The list of titles, as specified, in the category.
	 */
	public ArrayList<String> getCategoryMembers(String title, int cap, NS... ns)
	{
		ColorLog.info(this, "Getting category members from " + title);

		HashMap<String, String> pl = FL.pMap("list", "categorymembers", "cmtitle",
				convertIfNotInNS(title, NS.CATEGORY));
		if (ns.length > 0)
			pl.put("cmnamespace", nsl.createFilter(ns));

		return new SQ(this, "cmlimit", cap, pl).multiQuery().stringFromJAOfJO("categorymembers", "title");
	}

	/**
	 * Get the categories of a page.
	 * 
	 * @param title The title to get categories of.
	 * @return A list of categories, or the empty list if something went wrong.
	 */
	public ArrayList<String> getCategoriesOnPage(String title)
	{
		ColorLog.info(this, "Getting categories of " + title);
		return MQuery.getCategoriesOnPage(this, FL.toSAL(title)).get(title);
	}

	/**
	 * Gets wiki links on a page.
	 * 
	 * @param title The title to query
	 * @param ns Namespaces to include-only. Optional, leave blank to select all namespaces.
	 * @return The list of wiki links on the page.
	 */
	public ArrayList<String> getLinksOnPage(String title, NS... ns)
	{
		ColorLog.info(this, "Getting wiki links on " + title);
		return MQuery.getLinksOnPage(this, ns, FL.toSAL(title)).get(title);
	}

	/**
	 * Gets all existing or non-existing wiki links on a page.
	 * 
	 * @param exists Fetch mode. Set true to get existing pages and false to get missing/non-existent pages.
	 * @param title The title to query
	 * @param ns Namespaces to include-only. Optional, leave blank to select all namespaces.
	 * @return The list of existing links on <code>title</code>
	 */
	public ArrayList<String> getLinksOnPage(boolean exists, String title, NS... ns)
	{
		return FL.toAL(MQuery.exists(this, getLinksOnPage(title, ns)).entrySet().stream().filter(t -> t.getValue() == exists)
				.map(Map.Entry::getKey));
	}

	/**
	 * Gets the contributions of a user.
	 * 
	 * @param user The user to get contribs for, without the "User:" prefix.
	 * @param cap The maximum number of results to return.
	 * @param olderFirst Set to true to enumerate from older → newer revisions
	 * @param ns Restrict titles returned to the specified Namespace(s). Optional, leave blank to select all namespaces.
	 * @return A list of contributions.
	 */
	public ArrayList<Contrib> getContribs(String user, int cap, boolean olderFirst, NS... ns)
	{
		ColorLog.info(this, "Fetching contribs of " + user);
		HashMap<String, String> pl = FL.pMap("list", "usercontribs", "ucuser", user);
		if (ns.length > 0)
			pl.put("ucnamespace", nsl.createFilter(ns));
		if (olderFirst)
			pl.put("ucdir", "newer");

		RSet rs = new SQ(this, "uclimit", cap, pl).multiQuery();
		return FL.toAL(rs.getJOofJAStream("usercontribs").map(Contrib::new));
	}

	/**
	 * Gets all contributions of a user. Revisions are returned in order of newer → older revisions. Some users have well
	 * over a million contributions. Watch your memory usage!
	 * 
	 * @param user The user to get contribs for, without the "User:" prefix.
	 * @param ns Restrict titles returned to the specified Namespace(s). Optional, leave blank to select all namespaces.
	 * @return A list of contributions.
	 */
	public ArrayList<Contrib> getContribs(String user, NS... ns)
	{
		return getContribs(user, -1, false, ns);
	}

	/**
	 * Get a user's uploads.
	 * 
	 * @param user The username, without the "User:" prefix. PRECONDITION: <code>user</code> must be a valid username.
	 * @return This user's uploads
	 */
	public ArrayList<String> getUserUploads(String user)
	{
		ColorLog.info(this, "Fetching uploads for " + user);
		HashMap<String, String> pl = FL.pMap("list", "allimages", "aisort", "timestamp", "aiuser", nsl.nss(user));
		return new SQ(this, "ailimit", pl).multiQuery().stringFromJAOfJO("allimages", "title");
	}

	/**
	 * Gets a list of pages linking to a file.
	 * 
	 * @param title The title to query. PRECONDITION: This must be a valid file name prefixed with the "File:" prefix, or
	 *           you will get strange results.
	 * @return A list of pages linking to the file.
	 */
	public ArrayList<String> fileUsage(String title)
	{
		ColorLog.info(this, "Fetching local file usage of " + title);
		return MQuery.fileUsage(this, FL.toSAL(title)).get(title);
	}

	/**
	 * Gets titles of images linked on a page.
	 * 
	 * @param title The title to query
	 * @return The images found on <code>title</code>
	 */
	public ArrayList<String> getImagesOnPage(String title)
	{
		ColorLog.info(this, "Getting files on " + title);
		return MQuery.getImagesOnPage(this, FL.toSAL(title)).get(title);
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
		return MQuery.exists(this, FL.toSAL(title)).get(title);
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
		ColorLog.info(this, "Getting image info for " + title);
		return MQuery.getImageInfo(this, width, height, FL.toSAL(title)).get(title);
	}

	/**
	 * Gets templates transcluded on a page.
	 * 
	 * @param title The title to query.
	 * @return The templates transcluded on <code>title</code>
	 */
	public ArrayList<String> getTemplatesOnPage(String title)
	{
		ColorLog.info(this, "Getting templates transcluded on " + title);
		return MQuery.getTemplatesOnPage(this, FL.toSAL(title)).get(title);
	}

	/**
	 * Gets a list of pages transcluding a template.
	 * 
	 * @param title The title to query. You *must* include the namespace prefix (e.g. "Template:") or you will get
	 *           strange results.
	 * @return The pages transcluding <code>title</code>.
	 */
	public ArrayList<String> whatTranscludesHere(String title)
	{
		ColorLog.info(this, "Getting list of pages that transclude " + title);
		return MQuery.transcludesIn(this, FL.toSAL(title)).get(title);
	}

	/**
	 * Gets the global usage of a file.
	 * 
	 * @param title The title to query. Must start with "File:" prefix.
	 * @return A list of tuples, (title of page, short form of wiki this page is from), denoting the global usage of this
	 *         file. Returns empty list if something went wrong.
	 */
	public ArrayList<Tuple<String, String>> globalUsage(String title)
	{
		ColorLog.info(this, "Getting global usage for " + title);
		return MQuery.globalUsage(this, FL.toSAL(title)).get(0).y;
	}

	/**
	 * Gets a list of links or redirects to a page.
	 * 
	 * @param title The title to query
	 * @param redirects Set to true to get redirects only. Set to false to filter out all redirects.
	 * @return A list of links or redirects to this page.
	 */
	public ArrayList<String> whatLinksHere(String title, boolean redirects)
	{
		ColorLog.info("Getting links to " + title);
		return MQuery.linksHere(this, redirects, FL.toSAL(title)).get(title);
	}

	/**
	 * Gets a list of direct links to a page. CAVEAT: This does not get any pages linking to a redirect pointing to this
	 * page; in order to do this you will first need to obtain a list of redirects to the target, and then call
	 * <code>whatLinksHere()</code> on each of those redirects.
	 * 
	 * @param title The title to query
	 * @return A list of links to this page.
	 */
	public ArrayList<String> whatLinksHere(String title)
	{
		return whatLinksHere(title, false);
	}

	/**
	 * Get a list of all pages from the Wiki.
	 * 
	 * @param prefix Get files starting with this String. DO NOT include a namespace prefix (e.g. "File:"). Param is
	 *           optional, use null or empty string to disable.
	 * @param redirectsonly Set this to true to get redirects only.
	 * @param cap The max number of titles to return. Specify -1 to get all pages.
	 * @param ns The namespace to filter by. Set null to disable
	 * @return A list of titles on this Wiki
	 */
	public ArrayList<String> allPages(String prefix, boolean redirectsonly, int cap, NS ns)
	{
		ColorLog.info(this, "Doing all pages fetch for " + prefix == null ? "all pages" : prefix);
		HashMap<String, String> pl = FL.pMap("list", "allpages");
		if (prefix != null)
			pl.put("apprefix", prefix);
		if (ns != null)
			pl.put("apnamespace", "" + ns.v);
		if (redirectsonly)
			pl.put("apfilterredir", "redirects");

		return new SQ(this, "aplimit", cap, pl).multiQuery().stringFromJAOfJO("allpages", "title");
	}

	/**
	 * Does the same thing as Special:PrefixIndex.
	 * 
	 * @param namespace The namespace to filter by (inclusive)
	 * @param prefix Get all titles in the specified namespace, that start with this String.
	 * @return The list of titles starting with the specified prefix
	 */
	public ArrayList<String> prefixIndex(NS namespace, String prefix)
	{
		ColorLog.info(this, "Doing prefix index search for " + prefix);
		return allPages(prefix, false, -1, namespace);
	}

	/**
	 * Gets duplicates of a file. Note that results are returned *without* a namespace prefix.
	 * 
	 * @param title The title to query. PRECONDITION: You MUST include the namespace prefix (e.g. "File:")
	 * @param localOnly Set to true to restrict results to <b>local</b> duplicates only.
	 * @return Duplicates of this file.
	 */
	public ArrayList<String> getDuplicatesOf(String title, boolean localOnly)
	{
		ColorLog.info(this, "Getting duplicates of " + title);
		return MQuery.getDuplicatesOf(this, localOnly, FL.toSAL(title)).get(title);
	}

	/**
	 * Gets a list of duplicated files on the Wiki.
	 * 
	 * @param cap The maximum number of titles to return
	 * @return Duplicated files on the Wiki.
	 */
	public ArrayList<String> listDuplicateFiles(int cap)
	{
		ColorLog.info(this, "Getting duplicated files on the wiki");
		return MQuery.querySpecialPage(this, cap, "ListDuplicatedFiles");
	}
}