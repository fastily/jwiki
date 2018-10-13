package fastily.jwiki.core;

import java.net.Proxy;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;

import com.google.gson.JsonElement;

import fastily.jwiki.dwrap.Contrib;
import fastily.jwiki.dwrap.ImageInfo;
import fastily.jwiki.dwrap.LogEntry;
import fastily.jwiki.dwrap.PageSection;
import fastily.jwiki.dwrap.ProtectedTitleEntry;
import fastily.jwiki.dwrap.RCEntry;
import fastily.jwiki.dwrap.Revision;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.GSONP;
import fastily.jwiki.util.Tuple;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Main entry point of the jwiki API. This class contains most queries/actions which jwiki can perform on a wiki. Unless
 * stated otherwise, all methods are thread-safe.
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
	 * Our namespace manager
	 */
	protected NS.NSManager nsl;

	/**
	 * Default configuration and settings for this Wiki.
	 */
	public final Conf conf;

	/**
	 * Used to make calls to and from the API.
	 */
	protected final ApiClient apiclient;

	/**
	 * Constructor, configures all possible params. If the username and password are set but not valid then a
	 * SecurityException will be thrown.
	 * 
	 * @param user The username to use. Optional - set null to disable.
	 * @param px The password to login with. Optional - depends on user not being null, set null to disable.
	 * @param baseURL The URL pointing to the target MediaWiki API endpoint.
	 * @param proxy The Proxy to use. Optional - set null to disable.
	 * @param interceptor An OkHttp interceptor, useful for pre/post flight modifications. Optional - set null to
	 *           disable.
	 * @param parent The parent Wiki which spawned this Wiki using {@code getWiki()}. If this is the first Wiki, disable
	 *           with null.
	 * @param enableLogging Set true to enable std err log messages. Set false to disable std err log messages.
	 */
	private Wiki(String user, String px, HttpUrl baseURL, Proxy proxy, Interceptor interceptor, Wiki parent, boolean enableLogging)
	{
		conf = new Conf(baseURL, new ColorLog(enableLogging));

		if (parent != null) // CentralAuth login
		{
			wl = parent.wl;
			apiclient = new ApiClient(parent, this);

			refreshLoginStatus();
		}
		else
		{
			apiclient = new ApiClient(this, proxy, interceptor);

			if (user != null && px != null && !login(user, px))
				throw new SecurityException(String.format("Failed to log-in as %s @ %s", conf.uname, conf.hostname));
		}

		conf.log.info(this, "Fetching Namespace List");
		nsl = new NS.NSManager(new WQuery(this, WQuery.NAMESPACES).next().input.getAsJsonObject("query"));
	}

	/**
	 * Constructor, creates a Wiki with the specified domain and other optional parameters.
	 * 
	 * @param user The username to use. Optional - set null to disable.
	 * @param px The password to login with. Optional - depends on user not bei
	 * @param domain The domain name. Use shorthand form, ex: {@code en.wikipedia.org}.
	 * @param proxy The Proxy to use. Optional - set null to disable.
	 * @param interceptor An OkHttp interceptor, useful for pre/post flight modifications. Optional - set null to
	 *           disable.
	 * @param enableLogging Set true to enable std err log messages. Set false to disable std err log messages.
	 */
	private Wiki(String user, String px, String domain, Proxy proxy, Interceptor interceptor, boolean enableLogging)
	{
		this(user, px, HttpUrl.parse(String.format("https://%s/w/api.php", domain)), proxy, interceptor, null, enableLogging);
	}

	/**
	 * Constructor, creates an anonymous Wiki with the specified API endpoint, proxy, and/or interceptor.
	 * 
	 * @param user The username to use. Optional - set null to disable.
	 * @param px The password to use. Optional - set null to disable. CAVEAT: ignored if user is null.
	 * @param baseURL The URL pointing to the target MediaWiki API endpoint.
	 * @param proxy The Proxy to use. Optional - set null to disable.
	 * @param interceptor An OkHttp interceptor, useful for pre/post flight modifications. Optional - set null to
	 *           disable.
	 * @param enableLogging Set true to enable std err log messages. Set false to disable std err log messages.
	 */
	public Wiki(String user, String px, HttpUrl baseURL, Proxy proxy, Interceptor interceptor, boolean enableLogging)
	{
		this(user, px, baseURL, proxy, interceptor, null, enableLogging);
	}

	/**
	 * Constructor, creates an anonymous Wiki with the specified domain and interceptor. CAVEAT: This method assumes that
	 * the base API endpoint you are targeting is located at {@code https://<WIKI_DOMAIN>/w/api.php}. If this is not the
	 * case, then please use {@link #Wiki(String, String, HttpUrl, Proxy, Interceptor, boolean)}.
	 * 
	 * @param domain The domain name. Use shorthand form, ex: {@code en.wikipedia.org}.
	 * @param interceptor An OkHttp interceptor, useful for pre/post flight modifications. Optional - set null to
	 *           disable.
	 */
	public Wiki(String domain, Interceptor interceptor)
	{
		this(null, null, domain, null, interceptor, true);
	}

	/**
	 * Constructor, takes user, password, and domain to login as. CAVEAT: This method assumes that the base API endpoint
	 * you are targeting is located at {@code https://<WIKI_DOMAIN>/w/api.php}. If this is not the case, then please use
	 * {@link #Wiki(String, String, HttpUrl, Proxy, Interceptor, boolean)}.
	 * 
	 * @param user The username to use
	 * @param px The password to use
	 * @param domain The domain name. Use shorthand form, ex: {@code en.wikipedia.org}.
	 */
	public Wiki(String user, String px, String domain)
	{
		this(user, px, domain, null, null, true);
	}

	/**
	 * Constructor, creates an anonymous Wiki which is not logged in. CAVEAT: This method assumes that the base API
	 * endpoint you are targeting is located at {@code https://<WIKI_DOMAIN>/w/api.php}. If this is not the case, then
	 * please use {@link #Wiki(String, String, HttpUrl, Proxy, Interceptor, boolean)}.
	 * 
	 * @param domain The domain name. Use shorthand form, ex: {@code en.wikipedia.org}.
	 */
	public Wiki(String domain)
	{
		this(null, null, domain);
	}

	/**
	 * Constructor, creates an anonymous Wiki which is not logged in, pointed at the specified API endpoint. Use this for
	 * third-party/non-WMF Wikis.
	 * 
	 * @param apiEndpoint The API endpoint to use.
	 */
	public Wiki(HttpUrl apiEndpoint)
	{
		this(null, null, apiEndpoint, null, null, true);
	}

	/* //////////////////////////////////////////////////////////////////////////////// */
	/* ///////////////////////////// AUTH FUNCTIONS /////////////////////////////////// */
	/* //////////////////////////////////////////////////////////////////////////////// */

	/**
	 * Performs a login with the specified username and password. Does nothing if this Wiki is already logged in as a
	 * user.
	 * 
	 * @param user The username to use
	 * @param password The password to use
	 * @return True if the user is now logged in.
	 */
	public synchronized boolean login(String user, String password)
	{
		if (conf.uname != null) // do not login more than once
			return true;

		conf.log.info(this, "Try login for " + user);
		try
		{
			if (WAction.postAction(this, "login", false, FL.pMap("lgname", user, "lgpassword", password, "lgtoken",
					getTokens(WQuery.TOKENS_LOGIN, "logintoken"))) == WAction.ActionResult.SUCCESS)
			{
				refreshLoginStatus();

				conf.log.info(this, "Logged in as " + user);
				return true;
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Refresh the login status of a Wiki. This runs automatically on login or creation of a new CentralAuth'd Wiki.
	 */
	public void refreshLoginStatus()
	{
		conf.uname = GSONP.getStr(new WQuery(this, WQuery.USERINFO).next().metaComp("userinfo").getAsJsonObject(), "name");
		conf.token = getTokens(WQuery.TOKENS_CSRF, "csrftoken");
		wl.put(conf.hostname, this);

		conf.isBot = listUserRights(conf.uname).contains("bot");
	}

	/**
	 * Fetch tokens
	 * 
	 * @param wqt The {@code tokens} QTemplate to use
	 * @param tk The key pointing to the String with the specified token.
	 * @return The token, or null on error.
	 */
	private String getTokens(WQuery.QTemplate wqt, String tk)
	{
		try
		{
			return GSONP.getStr(new WQuery(this, wqt).next().metaComp("tokens").getAsJsonObject(), tk);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/* //////////////////////////////////////////////////////////////////////////////// */
	/* /////////////////////////// UTILITY FUNCTIONS ////////////////////////////////// */
	/* //////////////////////////////////////////////////////////////////////////////// */

	/**
	 * Performs a basic GET action on this Wiki. Use this to implement custom or non-standard API calls.
	 * 
	 * @param action The action to perform.
	 * @param params Each parameter and its corresponding value. For example, the parameters,
	 *           {@code &amp;foo=bar&amp;baz=blah}, should be passed in as {{@code "foo", "bar", "baz", "blah"}}.
	 *           URL-encoding will be applied automatically.
	 * @return The Response from the server, or null on error.
	 */
	public Response basicGET(String action, String... params)
	{
		HashMap<String, String> pl = FL.pMap(params);
		pl.put("action", action);
		pl.put("format", "json");

		try
		{
			return apiclient.basicGET(pl);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Performs a basic POST action on this Wiki. Use this to implement custom or non-standard API calls.
	 * 
	 * @param action The action to perform.
	 * @param form The form data to post. This will be automatically URL-encoded.
	 * @return The Response from the server, or null on error.
	 */
	public Response basicPOST(String action, HashMap<String, String> form)
	{
		form.put("format", "json");

		try
		{
			return apiclient.basicPOST(FL.pMap("action", action), form);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Check if a title in specified namespace and convert it if it is not.
	 * 
	 * @param title The title to check
	 * @param ns The namespace to convert the title to.
	 * @return The same title if it is in {@code ns}, or the converted title.
	 */
	public String convertIfNotInNS(String title, NS ns)
	{
		return whichNS(title).equals(ns) ? title : String.format("%s:%s", nsl.nsM.get(ns.v), nss(title));
	}

	/**
	 * Turns logging to std error on/off.
	 * 
	 * @param enabled Set false to disable logging, or true to enable logging.
	 */
	public void enableLogging(boolean enabled)
	{
		conf.log.enabled = enabled;
	}

	/**
	 * Filters pages by namespace. Only pages with a namespace in {@code ns} are selected.
	 * 
	 * @param pages Titles to filter
	 * @param ns Pages in this/these namespace(s) will be returned.
	 * @return Titles belonging to a NS in {@code ns}
	 */
	public ArrayList<String> filterByNS(ArrayList<String> pages, NS... ns)
	{
		HashSet<NS> l = new HashSet<>(Arrays.asList(ns));
		return FL.toAL(pages.stream().filter(s -> l.contains(whichNS(s))));
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
		if (prefix.isEmpty() || prefix.equalsIgnoreCase("main"))
			return NS.MAIN;

		return nsl.nsM.containsKey(prefix) ? new NS((int) nsl.nsM.get(prefix)) : null;
	}

	/**
	 * Gets a Wiki object for this domain. This method is cached. A new Wiki will be created as necessary. PRECONDITION:
	 * The <a href="https://www.mediawiki.org/wiki/Extension:CentralAuth">CentralAuth</a> extension is installed on the
	 * target MediaWiki farm.
	 * 
	 * @param domain The domain to use
	 * @return The Wiki, or null on error.
	 */
	public synchronized Wiki getWiki(String domain)
	{
		if (conf.uname == null)
			return null;

		conf.log.fyi(this, String.format("Get Wiki for %s @ %s", whoami(), domain));
		try
		{
			return wl.containsKey(domain) ? wl.get(domain)
					: new Wiki(null, null, conf.baseURL.newBuilder().host(domain).build(), null, null, this, conf.log.enabled);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
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
	 * Strips the namespaces from a Collection of titles.
	 * 
	 * @param l The Collection of titles to strip namespaces from
	 * @return A List where each of the titles does not have a namespace.
	 */
	public ArrayList<String> nss(Collection<String> l)
	{
		return FL.toAL(l.stream().map(this::nss));
	}

	/**
	 * Get the talk page of {@code title}.
	 * 
	 * @param title The title to get a talk page for.
	 * @return The talk page of {@code title}, or null if {@code title} is a special page or is already a talk page.
	 */
	public String talkPageOf(String title)
	{
		int i = whichNS(title).v;
		return i < 0 || i % 2 == 1 ? null : (String) nsl.nsM.get(i + 1) + ":" + nss(title);
	}

	/**
	 * Get the name of a page belonging to a talk page ({@code title}).
	 * 
	 * @param title The talk page whose content page will be determined.
	 * @return The title of the content page associated with the specified talk page, or null if {@code title} is a
	 *         special page or is already a content page.
	 */
	public String talkPageBelongsTo(String title)
	{
		NS ns = whichNS(title);

		if (ns.v < 0 || ns.v % 2 == 0)
			return null;
		else if (ns.equals(NS.TALK))
			return nss(title);

		return (String) nsl.nsM.get(ns.v - 1) + ":" + nss(title);
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
	 * Gets this Wiki's logged in user.
	 * 
	 * @return The user who is logged in, or null if not logged in.
	 */
	public String whoami()
	{
		return conf.uname == null ? "<Anonymous>" : conf.uname;
	}

	/**
	 * Gets a String representation of this Wiki, in the format {@code [username @ domain]}
	 */
	public String toString()
	{
		return String.format("[%s @ %s]", whoami(), conf.hostname);
	}

	/* //////////////////////////////////////////////////////////////////////////////// */
	/* /////////////////////////////////// ACTIONS //////////////////////////////////// */
	/* //////////////////////////////////////////////////////////////////////////////// */

	/**
	 * Appends text to a page. If {@code title} does not exist, then create the page normally with {@code text}
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
		return WAction.edit(this, title, text, reason);
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
	 * Purges page caches.
	 * 
	 * @param titles The titles to purge.
	 */
	public void purge(String... titles)
	{
		WAction.purge(this, FL.toSAL(titles));
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
		String rx = s.replaceAll(regex, replacement);

		return rx.equals(s) || edit(title, rx, reason);
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
		return WAction.undelete(this, title, reason);
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
		return WAction.upload(this, title, text, reason, p);
	}

	/* //////////////////////////////////////////////////////////////////////////////// */
	/* ///////////////////////////////// QUERIES ////////////////////////////////////// */
	/* //////////////////////////////////////////////////////////////////////////////// */

	/**
	 * Get a list of pages from the Wiki.
	 * 
	 * @param prefix Only return titles starting with this prefix. DO NOT include a namespace prefix (e.g.
	 *           {@code File:}). Optional param - set null to disable
	 * @param redirectsOnly Set true to get redirects only.
	 * @param protectedOnly Set true to get protected pages only.
	 * @param cap The max number of titles to return. Optional param - set {@code -1} to get all pages.
	 * @param ns The namespace to filter by. Optional param - set null to disable
	 * @return A list of titles on this Wiki, as specified.
	 */
	public ArrayList<String> allPages(String prefix, boolean redirectsOnly, boolean protectedOnly, int cap, NS ns)
	{
		conf.log.info(this, "Doing all pages fetch for " + (prefix == null ? "all pages" : prefix));

		WQuery wq = new WQuery(this, cap, WQuery.ALLPAGES);
		if (prefix != null)
			wq.set("apprefix", prefix);
		if (ns != null)
			wq.set("apnamespace", "" + ns.v);
		if (redirectsOnly)
			wq.set("apfilterredir", "redirects");
		if (protectedOnly)
			wq.set("apprtype", "edit|move|upload");

		ArrayList<String> l = new ArrayList<>();
		while (wq.has())
			l.addAll(FL.toAL(wq.next().listComp("allpages").stream().map(jo -> GSONP.getStr(jo, "title"))));

		return l;
	}

	/**
	 * Checks if a title exists.
	 * 
	 * @param title The title to query.
	 * @return True if the title exists.
	 */
	public boolean exists(String title)
	{
		conf.log.info(this, "Checking to see if title exists: " + title);
		return MQuery.exists(this, FL.toSAL(title)).get(title);
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
		conf.log.info(this, "Fetching local file usage of " + title);
		return MQuery.fileUsage(this, FL.toSAL(title)).get(title);
	}

	/**
	 * Gets a list of file extensions for the types of files which can be uploaded to this Wiki. WARNING: this method is
	 * not cached so save the result.
	 * 
	 * @return A list of file extensions for files which can be uploaded to this Wiki.
	 */
	public ArrayList<String> getAllowedFileExts()
	{
		conf.log.info(this, "Fetching a list of permissible file extensions");
		return FL
				.toAL(new WQuery(this, WQuery.ALLOWEDFILEXTS).next().listComp("fileextensions").stream().map(e -> GSONP.getStr(e, "ext")));
	}

	/**
	 * Get the categories of a page.
	 * 
	 * @param title The title to get categories of.
	 * @return A list of categories, or the empty list if something went wrong.
	 */
	public ArrayList<String> getCategoriesOnPage(String title)
	{
		conf.log.info(this, "Getting categories of " + title);
		return MQuery.getCategoriesOnPage(this, FL.toSAL(title)).get(title);
	}

	/**
	 * Get a limited number of titles in a category.
	 * 
	 * @param title The category to query, including the "Category:" prefix.
	 * @param ns Namespace filter. Any title not in the specified namespace(s) will be ignored. Leave blank to select all
	 *           namespaces. CAVEAT: skipped items are counted against {@code cap}.
	 * @return The list of titles, as specified, in the category.
	 */
	public ArrayList<String> getCategoryMembers(String title, NS... ns)
	{
		conf.log.info(this, "Getting category members from " + title);

		WQuery wq = new WQuery(this, WQuery.CATEGORYMEMBERS).set("cmtitle", convertIfNotInNS(title, NS.CATEGORY));
		if (ns.length > 0)
			wq.set("cmnamespace", nsl.createFilter(ns));

		ArrayList<String> l = new ArrayList<>();
		while (wq.has())
			l.addAll(FL.toAL(wq.next().listComp("categorymembers").stream().map(e -> GSONP.getStr(e, "title"))));

		return l;
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
		conf.log.info(this, "Getting category size of " + title);
		return MQuery.getCategorySize(this, FL.toSAL(title)).get(title);
	}

	/**
	 * Gets the contributions of a user.
	 * 
	 * @param user The user to get contribs for, without the "User:" prefix.
	 * @param cap The maximum number of results to return. Optional, disable with -1 (<b>caveat</b>: this will get *all*
	 *           of a user's contributions)
	 * @param olderFirst Set to true to enumerate from older → newer revisions
	 * @param ns Restrict titles returned to the specified Namespace(s). Optional, leave blank to select all namespaces.
	 * @return A list of contributions.
	 */
	public ArrayList<Contrib> getContribs(String user, int cap, boolean olderFirst, NS... ns)
	{
		conf.log.info(this, "Fetching contribs of " + user);

		WQuery wq = new WQuery(this, cap, WQuery.USERCONTRIBS).set("ucuser", user);
		if (ns.length > 0)
			wq.set("ucnamespace", nsl.createFilter(ns));
		if (olderFirst)
			wq.set("ucdir", "newer");

		ArrayList<Contrib> l = new ArrayList<>();
		while (wq.has())
			l.addAll(FL.toAL(wq.next().listComp("usercontribs").stream().map(jo -> GSONP.gson.fromJson(jo, Contrib.class))));

		return l;
	}

	/**
	 * List duplicates of a file.
	 * 
	 * @param title The title to query. PRECONDITION: You MUST include the namespace prefix (e.g. "File:")
	 * @param localOnly Set to true to restrict results to <span style="font-weight:bold;">local</span> duplicates only.
	 * @return Duplicates of this file.
	 */
	public ArrayList<String> getDuplicatesOf(String title, boolean localOnly)
	{
		conf.log.info(this, "Getting duplicates of " + title);
		return MQuery.getDuplicatesOf(this, localOnly, FL.toSAL(title)).get(title);
	}

	/**
	 * Gets a list of external URLs on a page.
	 * 
	 * @param title The title to query
	 * @return A List of external links found on the page.
	 */
	public ArrayList<String> getExternalLinks(String title)
	{
		conf.log.info(this, "Getting external links on " + title);
		return MQuery.getExternalLinks(this, FL.toSAL(title)).get(title);
	}

	/**
	 * Gets information about a File's revisions. Does not fill the thumbnail param of ImageInfo.
	 * 
	 * @param title The title of the file to use (must be in the file namespace and exist, else return null)
	 * @return A list of ImageInfo objects, one for each revision. The order is newer -&gt; older.
	 */
	public ArrayList<ImageInfo> getImageInfo(String title)
	{
		conf.log.info(this, "Getting image info for " + title);
		return MQuery.getImageInfo(this, FL.toSAL(title)).get(title);
	}

	/**
	 * Gets titles of images linked on a page.
	 * 
	 * @param title The title to query
	 * @return The images found on <code>title</code>
	 */
	public ArrayList<String> getImagesOnPage(String title)
	{
		conf.log.info(this, "Getting files on " + title);
		return MQuery.getImagesOnPage(this, FL.toSAL(title)).get(title);
	}

	/**
	 * Gets the username of the editor who last edited a page.
	 * 
	 * @param title The title to query
	 * @return The most recent editor of {@code title} (excluding {@code User:} prefix) or null on error.
	 */
	public String getLastEditor(String title)
	{
		try
		{
			return getRevisions(title, 1, false, null, null).get(0).user;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
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
		conf.log.info(this, "Getting wiki links on " + title);
		return MQuery.getLinksOnPage(this, FL.toSAL(title), ns).get(title);
	}

	/**
	 * Gets all existing or non-existing wiki links on a page.
	 * 
	 * @param exists Fetch mode. Set true to get existing pages and false to get missing/non-existent pages.
	 * @param title The title to query
	 * @param ns Namespaces to include-only. Optional, leave blank to select all namespaces.
	 * @return The list of existing links on {@code title}
	 */
	public ArrayList<String> getLinksOnPage(boolean exists, String title, NS... ns)
	{
		return FL.toAL(MQuery.exists(this, getLinksOnPage(title, ns)).entrySet().stream().filter(t -> t.getValue() == exists)
				.map(Map.Entry::getKey));
	}

	/**
	 * List log events. Order is newer -&gt; older.
	 * 
	 * @param title The title to fetch logs for. Optional - set null to disable.
	 * @param user The performing user to filter log entries by. Optional - set null to disable
	 * @param type The type of log to get (e.g. delete, upload, patrol). Optional - set null to disable
	 * @param cap Limits the number of entries returned from this log. Optional - set -1 to disable
	 * @return The log entries.
	 */
	public ArrayList<LogEntry> getLogs(String title, String user, String type, int cap)
	{
		conf.log.info(this, String.format("Fetching log entries -> title: %s, user: %s, type: %s", title, user, type));

		WQuery wq = new WQuery(this, cap, WQuery.LOGEVENTS);
		if (title != null)
			wq.set("letitle", title);
		if (user != null)
			wq.set("leuser", nss(user));
		if (type != null)
			wq.set("letype", type);

		ArrayList<LogEntry> l = new ArrayList<>();
		while (wq.has())
			l.addAll(FL.toAL(wq.next().listComp("logevents").stream().map(jo -> GSONP.gson.fromJson(jo, LogEntry.class))));

		return l;
	}

	/**
	 * Gets the first editor (creator) of a page. Specifically, get the author of the first revision of {@code title}.
	 * 
	 * @param title The title to query
	 * @return The page creator (excluding {@code User:} prefix) or null on error.
	 */
	public String getPageCreator(String title)
	{
		try
		{
			return getRevisions(title, 1, true, null, null).get(0).user;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Gets the text of a page.
	 * 
	 * @param title The title to query
	 * @return The text of the page, or an empty string if the page is non-existent/something went wrong.
	 */
	public String getPageText(String title)
	{
		conf.log.info(this, "Getting page text of " + title);
		return MQuery.getPageText(this, FL.toSAL(title)).get(title);
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
		conf.log.info(this, "Fetching a list of protected titles");

		WQuery wq = new WQuery(this, limit, WQuery.PROTECTEDTITLES);
		if (ns.length > 0)
			wq.set("ptnamespace", nsl.createFilter(ns));
		if (olderFirst)
			wq.set("ptdir", "newer"); // MediaWiki is weird.

		ArrayList<ProtectedTitleEntry> l = new ArrayList<>();
		while (wq.has())
			l.addAll(
					FL.toAL(wq.next().listComp("protectedtitles").stream().map(jo -> GSONP.gson.fromJson(jo, ProtectedTitleEntry.class))));

		return l;
	}

	/**
	 * Gets a list of random pages.
	 * 
	 * @param limit The number of titles to retrieve. PRECONDITION: {@code limit} cannot be a negative number.
	 * @param ns Returned titles will be in these namespaces. Optional param - leave blank to disable.
	 * @return A list of random titles on this Wiki.
	 */
	public ArrayList<String> getRandomPages(int limit, NS... ns)
	{
		conf.log.info(this, "Fetching random page(s)");

		if (limit < 0)
			throw new IllegalArgumentException("limit for getRandomPages() cannot be a negative number");

		ArrayList<String> l = new ArrayList<>();
		WQuery wq = new WQuery(this, limit, WQuery.RANDOM);

		if (ns.length > 0)
			wq.set("rnnamespace", nsl.createFilter(ns));

		while (wq.has())
			l.addAll(FL.toAL(wq.next().listComp("random").stream().map(e -> GSONP.getStr(e, "title"))));

		return l;
	}

	/**
	 * Gets a specified number of Recent Changes in between two timestamps. WARNING: if you use both {@code start} and
	 * {@code end}, then {@code start} MUST be earlier than {@code end}. If you set both {@code start} and {@code end} to
	 * null, then the default behavior is to fetch the last 30 seconds of recent changes.
	 * 
	 * @param start The Instant to start enumerating from. Can be used without {@code end}. Optional param - set null to
	 *           disable.
	 * @param end The Instant to stop enumerating at. {@code start} must be set, otherwise this will be ignored. Optional
	 *           param - set null to disable.
	 * @return A list Recent Changes where return order is newer -&gt; Older
	 */
	public ArrayList<RCEntry> getRecentChanges(Instant start, Instant end)
	{
		conf.log.info(this, "Querying recent changes");

		Instant s = start, e = end;
		if (s == null)
			s = (e = Instant.now()).minusSeconds(30);
		else if (e != null && e.isBefore(s)) // implied s != null
			throw new IllegalArgumentException("start is before end, cannot proceed");

		// MediaWiki has start <-> end backwards
		WQuery wq = new WQuery(this, WQuery.RECENTCHANGES).set("rcend", s.toString());
		if (e != null)
			wq.set("rcstart", e.toString());

		ArrayList<RCEntry> l = new ArrayList<>();
		while (wq.has())
			l.addAll(FL.toAL(wq.next().listComp("recentchanges").stream().map(jo -> GSONP.gson.fromJson(jo, RCEntry.class))));

		return l;
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
		conf.log.info(this, "Getting revisions from " + title);

		WQuery wq = new WQuery(this, cap, WQuery.REVISIONS).set("titles", title);
		if (olderFirst)
			wq.set("rvdir", "newer"); // MediaWiki is weird.

		if (start != null && end != null && start.isBefore(end))
		{
			wq.set("rvstart", end.toString()); // MediaWiki has start <-> end reversed
			wq.set("rvend", start.toString());
		}

		ArrayList<Revision> l = new ArrayList<>();
		while (wq.has())
		{
			JsonElement e = wq.next().propComp("title", "revisions").get(title);
			if (e != null)
				l.addAll(FL.toAL(GSONP.getJAofJO(e.getAsJsonArray()).stream().map(jo -> GSONP.gson.fromJson(jo, Revision.class))));
		}
		return l;
	}

	/**
	 * Gets the shared (non-local) duplicates of a file. PRECONDITION: The Wiki this query is run against has the
	 * <a href="https://www.mediawiki.org/wiki/Extension:GlobalUsage">GlobalUsage</a> extension installed.
	 * 
	 * @param title The title of the file to query
	 * @return An ArrayList containing shared duplicates of the file
	 */
	public ArrayList<String> getSharedDuplicatesOf(String title)
	{
		conf.log.info(this, "Getting shared duplicates of " + title);
		return MQuery.getSharedDuplicatesOf(this, FL.toSAL(title)).get(title);
	}

	/**
	 * Gets templates transcluded on a page.
	 * 
	 * @param title The title to query.
	 * @return The templates transcluded on <code>title</code>
	 */
	public ArrayList<String> getTemplatesOnPage(String title)
	{
		conf.log.info(this, "Getting templates transcluded on " + title);
		return MQuery.getTemplatesOnPage(this, FL.toSAL(title)).get(title);
	}

	/**
	 * Gets a text extract (the lead paragraph) of a page.
	 * 
	 * @param title The title to get a text extract for.
	 * @return The text extract. Null if {@code title} does not exist or is a special page.
	 */
	public String getTextExtract(String title)
	{
		conf.log.info(this, "Getting a text extract for " + title);
		return MQuery.getTextExtracts(this, FL.toSAL(title)).get(title);
	}

	/**
	 * Get a user's uploads.
	 * 
	 * @param user The username, without the "User:" prefix. PRECONDITION: <code>user</code> must be a valid username.
	 * @return This user's uploads
	 */
	public ArrayList<String> getUserUploads(String user)
	{
		conf.log.info(this, "Fetching uploads for " + user);

		ArrayList<String> l = new ArrayList<>();
		WQuery wq = new WQuery(this, WQuery.USERUPLOADS).set("aiuser", nss(user));
		while (wq.has())
			l.addAll(FL.toAL(wq.next().listComp("allimages").stream().map(e -> GSONP.getStr(e, "title"))));

		return l;
	}

	/**
	 * Gets the global usage of a file. PRECONDITION: GlobalUsage must be installed on the target Wiki.
	 * 
	 * @param title The title to query. Must start with <code>File:</code> prefix.
	 * @return A HashMap with the global usage of this file; each element is of the form <code>[ title : wiki ]</code>.
	 */
	public ArrayList<Tuple<String, String>> globalUsage(String title)
	{
		conf.log.info(this, "Getting global usage for " + title);
		return MQuery.globalUsage(this, FL.toSAL(title)).get(title);
	}

	/**
	 * Gets the list of usergroups (rights) a user belongs to. Sample groups: sysop, user, autoconfirmed, editor.
	 * 
	 * @param user The user to get rights information for. Do not include "User:" prefix.
	 * @return The usergroups {@code user} belongs to, or null if {@code user} is an IP or non-existent user.
	 */
	public ArrayList<String> listUserRights(String user)
	{
		conf.log.info(this, "Getting user rights for " + user);
		return MQuery.listUserRights(this, FL.toSAL(user)).get(user);
	}

	/**
	 * Does the same thing as Special:PrefixIndex.
	 * 
	 * @param namespace The namespace to filter by (inclusive)
	 * @param prefix Get all titles in the specified namespace, that start with this String. To select subpages only,
	 *           append a {@code /} to the end of this parameter.
	 * @return The list of titles starting with the specified prefix
	 */
	public ArrayList<String> prefixIndex(NS namespace, String prefix)
	{
		conf.log.info(this, "Doing prefix index search for " + prefix);
		return allPages(prefix, false, false, -1, namespace);
	}

	/**
	 * Queries a special page.
	 * 
	 * @param title The special page to query, without the {@code Special:} prefix. CAVEAT: this is CASE-sensitive, so be
	 *           sure to use the exact title (e.g. {@code UnusedFiles}, {@code BrokenRedirects}). For a full list of
	 *           titles, see <a href="https://www.mediawiki.org/w/api.php?action=help&modules=query+querypage">the
	 *           official documentation</a>.
	 * @param cap The maximum number of elements to return. Use {@code -1} to get everything, but be careful because some
	 *           pages can have 10k+ entries.
	 * @return A List of titles returned by this special page.
	 */
	public ArrayList<String> querySpecialPage(String title, int cap)
	{
		conf.log.info(this, "Querying special page " + title);

		WQuery wq = new WQuery(this, cap, WQuery.QUERYPAGES).set("qppage", nss(title));
		ArrayList<String> l = new ArrayList<>();

		while (wq.has())
			try
			{
				l.addAll(FL.toAL(FL.streamFrom(GSONP.getNestedJA(wq.next().input, FL.toSAL("query", "querypage", "results")))
						.map(e -> GSONP.getStr(e.getAsJsonObject(), "title"))));
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
		return l;
	}

	/**
	 * Attempts to resolve title redirects on a Wiki.
	 * 
	 * @param title The title to attempt resolution at.
	 * @return The resolved title, or the original title if it was not a redirect.
	 */
	public String resolveRedirect(String title)
	{
		conf.log.info(this, "Resolving redirect for " + title);
		return MQuery.resolveRedirects(this, FL.toSAL(title)).get(title);
	}

	/**
	 * Performs a search on the Wiki.
	 * 
	 * @param query The query string to search the Wiki with.
	 * @param limit The maximum number of entries to return. Optional, specify {@code -1} to disable (not recommended if
	 *           your wiki is big).
	 * @param ns Limit search to these namespaces. Optional, leave blank to disable. The default behavior is to search
	 *           all namespaces.
	 * @return A List of titles found by the search.
	 */
	public ArrayList<String> search(String query, int limit, NS... ns)
	{
		WQuery wq = new WQuery(this, limit, WQuery.SEARCH).set("srsearch", query);

		if (ns.length > 0)
			wq.set("srnamespace", nsl.createFilter(ns));

		ArrayList<String> l = new ArrayList<>();
		while (wq.has())
			l.addAll(FL.toAL(wq.next().listComp("search").stream().map(e -> GSONP.getStr(e, "title"))));

		return l;
	}

	/**
	 * Splits the text of a page by header.
	 * 
	 * @param title The title to query
	 * @return An ArrayList where each section (in order) is contained in a PageSection object.
	 */
	public ArrayList<PageSection> splitPageByHeader(String title)
	{
		conf.log.info(this, "Splitting " + title + " by header");

		try
		{
			return PageSection.pageBySection(GSONP.getJAofJO(GSONP.getNestedJA(
					GSONP.jp.parse(basicGET("parse", "prop", "sections", "page", title).body().string()).getAsJsonObject(),
					FL.toSAL("parse", "sections"))), getPageText(title));
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return new ArrayList<>();
		}
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
		conf.log.info(this, "Getting links to " + title);
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
	 * Gets a list of pages transcluding a template.
	 * 
	 * @param title The title to query. You *must* include the namespace prefix (e.g. "Template:") or you will get
	 *           strange results.
	 * @param ns Only return results from this/these namespace(s). Optional param: leave blank to disable.
	 * @return The pages transcluding <code>title</code>.
	 */
	public ArrayList<String> whatTranscludesHere(String title, NS... ns)
	{
		conf.log.info(this, "Getting list of pages that transclude " + title);
		return MQuery.transcludesIn(this, FL.toSAL(title), ns).get(title);
	}
}