package fastily.jwiki.core;

import java.net.CookieManager;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import javax.security.auth.login.LoginException;

import fastily.jwiki.dwrap.Contrib;
import fastily.jwiki.dwrap.ImageInfo;
import fastily.jwiki.dwrap.LogEntry;
import fastily.jwiki.dwrap.ProtectedTitleEntry;
import fastily.jwiki.dwrap.RCEntry;
import fastily.jwiki.dwrap.Revision;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.FString;
import fastily.jwiki.util.Triple;
import fastily.jwiki.util.Tuple;

/**
 * Main class of jwiki. Most developers will only need this class. This class implements all queries/actions which jwiki
 * can perform on a wiki. All methods are backed by static calls and are thread-safe.
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
	 * Flag indicating whether the logged in user is a bot.
	 */
	protected boolean isBot = false;

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
		upx = new Tuple<>(FString.capitalize(user), px);
		this.domain = domain;

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
	 * Gets a Wiki object for this domain. This method is cached. A new Wiki will be created as necessary. PRECONDITION:
	 * The <a href="https://www.mediawiki.org/wiki/Extension:CentralAuth">CentralAuth</a> extension MUST be installed on
	 * your MediaWiki cluster for this to work.
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
		if (prefix.isEmpty() || prefix.toLowerCase().equals("main"))
			return NS.MAIN;

		return nsl.nsM.containsKey(prefix) ? new NS((int) nsl.nsM.get(prefix)) : null;
	}

	/**
	 * Gets the namespace, in NS form, of a title. No namespace or an invalid namespace is assumed to be part of Main.
	 * 
	 * @param title The title to get an NS for.
	 * @return The title's NS.
	 */
	public NS whichNS(String title)
	{
		Matcher m = nsl.p.matcher(title);
		return !m.find() ? NS.MAIN : new NS((int) nsl.nsM.get(title.substring(m.start(), m.end() - 1)));
	}

	/**
	 * Strip the namespace from a title.
	 * 
	 * @param title The title to strip the namespace from
	 * @return The title without a namespace
	 */
	public String nss(String title)
	{
		return title.replaceAll(nsl.nssRegex, "");
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
		return whichNS(title).equals(ns) ? title : String.format("%s:%s", nsl.nsM.get(ns.v), nss(title));
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
		String s = getPageText(title), rx = s.replaceAll(regex, replacement);
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
		ColorLog.info("Purging cache of " + title);
		return WAction.purge(this, FL.toSAL(title)).get(title);
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
		return MQuery.listUserRights(this, FL.toSAL(user)).get(user);
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
	 * @param start The instant to start enumerating from. Start date must occur before end date. Optional param - set
	 *           null to disable.
	 * @param end The instant to stop enumerating at. Optional param - set null to disable.
	 * @return A list of page revisions
	 */
	public ArrayList<Revision> getRevisions(String title, int cap, boolean olderFirst, Instant start, Instant end)
	{
		ColorLog.info(this, "Getting revisions from " + title);
		HashMap<String, String> pl = FL.pMap("prop", "revisions", "rvprop", FString.pipeFence("timestamp", "user", "comment", "content"),
				"titles", title);
		if (olderFirst)
			pl.put("rvdir", "newer"); // MediaWiki is weird.

		if (start != null && end != null && start.isBefore(end))
		{
			pl.put("rvstart", end.toString()); // MediaWiki has start <-> end mixed up
			pl.put("rvend", start.toString());
		}

		RSet rs = SQ.with(this, "rvlimit", cap, pl).multiQuery();
		return rs.getJAofJOas("revisions", x -> new Revision(title, x));
	}

	/**
	 * Get log events. Specify at least one of the params or else an error will be thrown; wholesale fetching of logs is
	 * disabled because it is a potentially destructive action.
	 * 
	 * @param title The title to fetch logs for. Optional - set null to disable.
	 * @param user The performing user to filter log entries by. Optional - set null to disable
	 * @param type The type of log to get (e.g. delete, upload, patrol). Optional - set null to disable
	 * @param cap Limits the number of entries returned from this log. Optional - set -1 to disable
	 * @return The log entries.
	 */
	public ArrayList<LogEntry> getLogs(String title, String user, String type, int cap)
	{
		ColorLog.info(this, String.format("Fetching log entries -> title: %s, user: %s, type: %s", title, user, type));

		HashMap<String, String> pl = FL.pMap("list", "logevents");

		if (title != null)
			pl.put("letitle", title);
		if (user != null)
			pl.put("leuser", nss(user));
		if (type != null)
			pl.put("letype", type);

		if (title == null && user == null && cap < 0)
			throw new UnsupportedOperationException("Not doing this.  Fetching *entire* logs is a potentially destrutive action.");

		return SQ.with(this, "lelimit", cap, pl).multiQuery().getJAofJOas("logevents", LogEntry::new);
	}

	/**
	 * Fetches protected titles (create-protected) on the Wiki.
	 * 
	 * @param limit The maximum number of returned entries. Set -1 to disable.
	 * @param olderFirst Set to true to get older entries first.
	 * @param ns Namespace filter, limits returned titles to these namespaces. Optional param - leave blank to disable.
	 * @return An ArrayList of protected titles.
	 */
	public ArrayList<ProtectedTitleEntry> getProtectedTitles(int limit, boolean olderFirst, NS... ns)
	{
		ColorLog.info(this, "Fetching a list of protected titles");

		HashMap<String, String> pl = FL.pMap("list", "protectedtitles", "ptprop",
				FString.pipeFence("timestamp", "level", "user", "comment"));

		if (ns.length > 0)
			pl.put("ptnamespace", nsl.createFilter(ns));
		if (olderFirst)
			pl.put("ptdir", "newer"); // MediaWiki is weird.

		return SQ.with(this, "ptlimit", limit, pl).multiQuery().getJAofJOas("protectedtitles", ProtectedTitleEntry::new);
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

		HashMap<String, String> pl = FL.pMap("list", "categorymembers", "cmtitle", convertIfNotInNS(title, NS.CATEGORY));
		if (ns.length > 0)
			pl.put("cmnamespace", nsl.createFilter(ns));

		return SQ.with(this, "cmlimit", cap, pl).multiQuery().getJAOfJOasStr("categorymembers", "title");
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
		return MQuery.getLinksOnPage(this, FL.toSAL(title), ns).get(title);
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

		return SQ.with(this, "uclimit", cap, pl).multiQuery().getJAofJOas("usercontribs", Contrib::new);
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
	 * Gets a specified number of Recent Changes in between two timestamps. Note: you *must* use <code>start</code> and
	 * <code>end</code> together or not at all, otherwise the parameters will be ignored.
	 * 
	 * @param num The maximum number of entries to get
	 * @param start The instant to start enumerating from. Start date must occur before end date. Optinal param - set
	 *           null to disable.
	 * @param end The instant to stop enumerating at. Optional param - set null to disable.
	 * @return A list Recent Changes where return order is newer -&gt; Older
	 */
	public ArrayList<RCEntry> getRecentChanges(int num, Instant start, Instant end)
	{
		ColorLog.info(this, "Querying recent changes");
		HashMap<String, String> pl = FL.pMap("list", "recentchanges", "rcprop",
				FString.pipeFence("title", "timestamp", "user", "comment"), "rctype", FString.pipeFence("edit", "new", "log"));

		if (start != null && end != null && start.isBefore(end))
		{
			pl.put("rcstart", end.toString()); // MediaWiki has start <-> end mixed up
			pl.put("rcend", start.toString());
		}

		return SQ.with(this, "rclimit", num, pl).multiQuery().getJAofJOas("recentchanges", RCEntry::new);
	}

	/**
	 * Gets a specified number of the newest RecentChanges.
	 * 
	 * @param num The maximum number of entries to get
	 * @return A list Recent Changes where return order is newer -&gt; Older
	 */
	public ArrayList<RCEntry> getRecentChanges(int num)
	{
		return getRecentChanges(num, null, null);
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
		return SQ.with(this, "ailimit", FL.pMap("list", "allimages", "aisort", "timestamp", "aiuser", nss(user))).multiQuery()
				.getJAOfJOasStr("allimages", "title");
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
	 * Gets the section headers on a page
	 * 
	 * @param title The title to get section headers for
	 * @return An ArrayList with section header data. Format is ( Integer, String, Integer ) : ( Header Level, Header
	 *         Title, Header offset ).
	 */
	public ArrayList<Triple<Integer, String, Integer>> getSectionHeaders(String title)
	{
		ColorLog.info(this, "Fetching section headers for " + title);

		SQ sq = SQ.with(this, FL.pMap("prop", "sections", "page", title));
		sq.action = "parse";
		return FL.toAL(sq.singleQuery().getJAofJO("sections").stream()
				.map(e -> new Triple<>(Integer.parseInt(e.getString("level")), e.getString("line"), e.getInt("byteoffset"))));
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
	 * Gets information about a File's revisions. Does not fill the thumbnail param of ImageInfo.
	 * 
	 * @param title The title of the file to use (must be in the file namespace and exist, else return null)
	 * @return A list of ImageInfo objects, one for each revision. The order is newer -&gt; older.
	 */
	public ArrayList<ImageInfo> getImageInfo(String title)
	{
		return getImageInfo(title, -1, -1);
	}

	/**
	 * Gets information about a File's revisions.
	 * 
	 * @param title The title of the file to use (must be in the file namespace and exist, else return null)
	 * @param height The height to scale the image to. Disable scalers by passing in a number &ge; 0.
	 * @param width The width to scale the image to. Disable scalers by passing in a number &ge; 0.
	 * @return A list of ImageInfo objects, one for each revision. The order is newer -&gt; older.
	 */
	public ArrayList<ImageInfo> getImageInfo(String title, int height, int width)
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
	 * @param ns Only return results from this/these namespace(s). Optional param: leave blank to disable.
	 * @return The pages transcluding <code>title</code>.
	 */
	public ArrayList<String> whatTranscludesHere(String title, NS... ns)
	{
		ColorLog.info(this, "Getting list of pages that transclude " + title);
		return MQuery.transcludesIn(this, FL.toSAL(title), ns).get(title);
	}

	/**
	 * Gets the global usage of a file. PRECONDITION: GlobalUsage must be installed on the target Wiki.
	 * 
	 * @param title The title to query. Must start with <code>File:</code> prefix.
	 * @return A HashMap with the global usage of this file; each element is of the form <code>[ title : wiki ]</code>.
	 */
	public ArrayList<Tuple<String, String>> globalUsage(String title)
	{
		ColorLog.info(this, "Getting global usage for " + title);
		return MQuery.globalUsage(this, FL.toSAL(title)).get(title);
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
	 * @param prefix Get files starting with this String. DO NOT include a namespace prefix (e.g. "File:"). Optional
	 *           param - set null to disable
	 * @param redirectsOnly Set True to get redirects only.
	 * @param protectedOnly Set True to get protected pages only.
	 * @param cap The max number of titles to return. Specify -1 to get all pages.
	 * @param ns The namespace to filter by. Optional param - set null to disable
	 * @return A list of titles on this Wiki
	 */
	public ArrayList<String> allPages(String prefix, boolean redirectsOnly, boolean protectedOnly, int cap, NS ns)
	{
		ColorLog.info(this, "Doing all pages fetch for " + (prefix == null ? "all pages" : prefix));
		HashMap<String, String> pl = FL.pMap("list", "allpages");
		if (prefix != null)
			pl.put("apprefix", prefix);
		if (ns != null)
			pl.put("apnamespace", "" + ns.v);
		if (redirectsOnly)
			pl.put("apfilterredir", "redirects");
		if (protectedOnly)
			pl.put("apprtype", FString.pipeFence("edit", "move", "upload"));

		return SQ.with(this, "aplimit", cap, pl).multiQuery().getJAOfJOasStr("allpages", "title");
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
		return allPages(prefix, false, false, -1, namespace);
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
	 * Gets the shared (non-local) duplicates of a file. PRECONDITION: The Wiki this query is run against has the
	 * <a href="https://www.mediawiki.org/wiki/Extension:GlobalUsage">GlobalUsage</a> extension installed. Note that
	 * results are returned *without* a namespace prefix.
	 * 
	 * @param title The title of the file to query
	 * @return An ArrayList containing shared duplicates of the file
	 */
	public ArrayList<String> getSharedDuplicatesOf(String title)
	{
		ColorLog.info(this, "Getting shared duplicates of " + title);
		return MQuery.getSharedDuplicatesOf(this, FL.toSAL(title)).get(title);
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

	/**
	 * Attempts to resolve title redirects on a Wiki.
	 * 
	 * @param title The title to attempt resolution at.
	 * @return The resolved title, or the original title if it was not a redirect.
	 */
	public String resolveRedirect(String title)
	{
		ColorLog.info(this, "Resolving redirect for " + title);
		return MQuery.resolveRedirects(this, FL.toSAL(title)).get(title);
	}

	/**
	 * Gets a list of file extensions for the types of files which can be uploaded to this Wiki. WARNING: this method is
	 * not cached so save the result.
	 * 
	 * @return A list of file extensions for files which can be uploaded to this Wiki.
	 */
	public ArrayList<String> getAllowedFileExts()
	{
		ColorLog.info(this, "Fetching a list of permissible file extensions");
		return FL.toAL(SQ.with(this, FL.pMap("meta", "siteinfo", "siprop", "fileextensions")).singleQuery().getJAofJO("fileextensions")
				.stream().map(r -> r.getStringR("ext")));
	}
}