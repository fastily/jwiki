package io.github.fastily.jwiki.core;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import io.github.fastily.jwiki.dwrap.Contrib;
import io.github.fastily.jwiki.dwrap.ImageInfo;
import io.github.fastily.jwiki.dwrap.LogEntry;
import io.github.fastily.jwiki.dwrap.PageSection;
import io.github.fastily.jwiki.dwrap.ProtectedTitleEntry;
import io.github.fastily.jwiki.dwrap.RCEntry;
import io.github.fastily.jwiki.dwrap.Revision;
import io.github.fastily.jwiki.util.FL;
import io.github.fastily.jwiki.util.GSONP;
import io.github.fastily.jwiki.util.Tuple;
import okhttp3.HttpUrl;
import okhttp3.Response;

/**
 * Main entry point of jwiki. This class aggregates most of the queries/actions which jwiki can perform on a wiki. All methods are backed by static functions and are therefore thread-safe.
 * 
 * @author Fastily
 */
public class Wiki
{
	/**
	 * The Logger for this class
	 */
	private static Logger log = LoggerFactory.getLogger(Wiki.class);

	/**
	 * Builder used to create Wiki objects. All options are optional. If you're lazy and just want an anonymous Wiki pointing to en.wikipedia.org, use {@code new Wiki.Builder().build()}
	 * 
	 * @author Fastily
	 *
	 */
	public static class Builder
	{
		/**
		 * The Wiki
		 */
		private Wiki wiki = new Wiki();

		/**
		 * The Proxy to use
		 */
		private Proxy proxy;

		/**
		 * Username to login as.
		 */
		private String username;

		/**
		 * Password to login with.
		 */
		private String password;

		/**
		 * Creates a new Wiki Builder.
		 */
		public Builder()
		{

		}

		/**
		 * Configures the Wiki to be created to use the specified User-Agent for HTTP requests.
		 * 
		 * @param userAgent The User-Agent to use
		 * @return This Builder
		 */
		public Builder withUserAgent(String userAgent)
		{
			wiki.conf.userAgent = userAgent;
			return this;
		}

		/**
		 * Configures the Wiki to be created with the specified Proxy.
		 * 
		 * @param proxy The Proxy to use
		 * @return This Builder
		 */
		public Builder withProxy(Proxy proxy)
		{
			this.proxy = proxy;
			return this;
		}

		/**
		 * Configures the Wiki to be created with the specified api endpoint. This is the base endpoint of the MediaWiki instance you are targeting. Example:
		 * <a href="https://en.wikipedia.org/w/api.php">Wikipedia API</a>.
		 * 
		 * @param apiEndpoint The base api endpoint to target
		 * @return This Builder
		 */
		public Builder withApiEndpoint(HttpUrl apiEndpoint)
		{
			wiki.conf.retarget(apiEndpoint);
			return this;
		}

		/**
		 * Configures the Wiki to be created with the specified domain. This method assumes that the target API endpoint is located at {@code https://<YOUR_DOMAIN_HERE>/w/api.php}; if this is not the
		 * case, then use {@link #withApiEndpoint(HttpUrl)}
		 * 
		 * @param domain The domain to target. Example: {@code en.wikipedia.org}.
		 * @return This Builder
		 */
		public Builder withDomain(String domain)
		{
			return withApiEndpoint(HttpUrl.parse(String.format("https://%s/w/api.php", domain)));
		}

		/**
		 * Configures the Wiki to be created with the specified username and password combination. Login will be attempted when {@link #build()} is called.
		 * 
		 * @param username The username to use
		 * @param password The password to use
		 * @return This Builder
		 */
		public Builder withLogin(String username, String password)
		{
			this.username = username;
			this.password = password;
			return this;
		}

		/**
		 * Performs the task of creating the Wiki object as configured. If {@link #withApiEndpoint(HttpUrl)} or {@link #withDomain(String)} were not called, then the resulting Wiki will default to the
		 * <a href="https://en.wikipedia.org/w/api.php">Wikipedia API</a>.
		 * 
		 * @return A Wiki object
		 */
		public Wiki build()
		{
			wiki.apiclient = new ApiClient(wiki, proxy);

			if (username != null && password != null && !wiki.login(username, password))
				throw new SecurityException(String.format("Failed to log-in as %s @ %s", username, wiki.conf.hostname));

			wiki.refreshNS();

			return wiki;
		}
	}

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
	protected Conf conf = new Conf();

	/**
	 * Used to make calls to and from the API.
	 */
	protected ApiClient apiclient;

	/**
	 * Constructor, creates a new Wiki
	 */
	private Wiki()
	{

	}

	/**
	 * Constructor, creates a new Wiki for use with CentralAuth. See {@link #getWiki(String)}
	 * 
	 * @param apiEndpoint The API endpoint to target.
	 * @param parent The parent Wiki which spawns this new Wiki.
	 */
	private Wiki(HttpUrl apiEndpoint, Wiki parent)
	{
		conf.retarget(apiEndpoint);

		wl = parent.wl;
		apiclient = new ApiClient(parent, this);

		refreshLoginStatus();
		refreshNS();
	}

	/* //////////////////////////////////////////////////////////////////////////////// */
	/* ///////////////////////////// AUTH FUNCTIONS /////////////////////////////////// */
	/* //////////////////////////////////////////////////////////////////////////////// */

	/**
	 * Performs a login with the specified username and password. Does nothing if this Wiki is already logged in as a user.
	 * 
	 * @param user The username to use
	 * @param password The password to use
	 * @return True if the user is now logged in.
	 */
	public synchronized boolean login(String user, String password)
	{
		if (conf.uname != null) // do not login more than once
			return true;

		log.info("{}: Try login for {}", this, user);
		try
		{
			if (WAction.postAction(this, "login", false, FL.pMap("lgname", user, "lgpassword", password, "lgtoken", getTokens(WQuery.TOKENS_LOGIN, "logintoken"))) == WAction.ActionResult.SUCCESS)
			{
				refreshLoginStatus();

				log.info("{}: Logged in as {}", this, user);
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
	 * @param params Each parameter and its corresponding value. For example, the parameters, {@code &amp;foo=bar&amp;baz=blah}, should be passed in as {{@code "foo", "bar", "baz", "blah"}}.
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
	 * Refresh the Namespace list.
	 */
	private void refreshNS()
	{
		log.info("{}: Fetching Namespace List", this);
		nsl = new NS.NSManager(new WQuery(this, WQuery.NAMESPACES).next().input.getAsJsonObject("query"));
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
	 * Takes a Namespace prefix and gets a NS representation of it. PRECONDITION: the prefix must be a valid namespace prefix. WARNING: This method is CASE-SENSITIVE, so be sure to spell and capitalize
	 * the prefix <b>exactly</b> as it would appear on-wiki.
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
	 * Gets a Wiki object for this domain. This method is cached. A new Wiki will be created as necessary. PRECONDITION: The
	 * <a href="https://www.mediawiki.org/wiki/Extension:CentralAuth">CentralAuth</a> extension is installed on the target MediaWiki farm.
	 * 
	 * @param domain The domain to use
	 * @return The Wiki, or null on error.
	 */
	public synchronized Wiki getWiki(String domain)
	{
		if (conf.uname == null)
			return null;

		log.info("{}: Get Wiki for {} @ {}", this, whoami(), domain);
		try
		{
			return wl.containsKey(domain) ? wl.get(domain) : new Wiki(conf.baseURL.newBuilder().host(domain).build(), this);
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
	 * @return The title of the content page associated with the specified talk page, or null if {@code title} is a special page or is already a content page.
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
	 * Move a page.
	 * 
	 * @param title The original title to move
	 * @param newTitle The new title to move the old page to
	 * @param moveTalk Flag indicating if {@code title}'s talk page (assuming it exists) should be moved. Optional, set false to disable.
	 * @param moveSubpages Flag indicating if {@code title}'s subpages should also be moved. Requires admin/pagemover rights, otherwise this does nothing. Optional, set false to disable.
	 * @param supressRedirect Flag indicating if a redirect to {@code newTitle} should be automatically generated at {@code title}. Requires admin/pagemover rights, otherwise this does nothing.
	 *           Optional, set false to disable.
	 * @param reason The edit summary to use
	 * @return True if the operation succeeded
	 */
	public boolean move(String title, String newTitle, boolean moveTalk, boolean moveSubpages, boolean supressRedirect, String reason)
	{
		return WAction.move(this, title, newTitle, moveTalk, moveSubpages, supressRedirect, reason);
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
	 * Removes text from a page. Does nothing if the replacement requested wouldn't change any text on wiki (method still returns true however).
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
	 * Replaces text on a page. Does nothing if the replacement requested wouldn't change any text on wiki (method still returns true however).
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
	 * Undelete a page. You must have admin rights on the wiki you are trying to perform this task on, otherwise it won't go through.
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

	/**
	 * Upload a file by URL. The URL must be on the upload by url whitelist for the target Wiki or this method will automatically fail.
	 * 
	 * @param url The URL the target file is located at.
	 * @param title The title to upload to.
	 * @param desc The text to put on the file description page
	 * @param summary The edit summary
	 * @return True if the upload was successful.
	 */
	public boolean uploadByUrl(HttpUrl url, String title, String desc, String summary)
	{
		return WAction.uploadByUrl(this, url, title, desc, summary);
	}

	/* //////////////////////////////////////////////////////////////////////////////// */
	/* ///////////////////////////////// QUERIES ////////////////////////////////////// */
	/* //////////////////////////////////////////////////////////////////////////////// */

	/**
	 * Get a list of pages from the Wiki.
	 * 
	 * @param prefix Only return titles starting with this prefix. DO NOT include a namespace prefix (e.g. {@code File:}). Optional param - set null to disable
	 * @param redirectsOnly Set true to get redirects only.
	 * @param protectedOnly Set true to get protected pages only.
	 * @param cap The max number of titles to return. Optional param - set {@code -1} to get all pages.
	 * @param ns The namespace to filter by. Optional param - set null to disable
	 * @return A list of titles on this Wiki, as specified.
	 */
	public ArrayList<String> allPages(String prefix, boolean redirectsOnly, boolean protectedOnly, int cap, NS ns)
	{
		log.info("{}: Doing all pages fetch for {}", this, prefix == null ? "all pages" : prefix);

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
		log.info("{}: Checking to see if title exists: {}", this, title);
		return MQuery.exists(this, FL.toSAL(title)).get(title);
	}

	/**
	 * Gets a list of pages linking to a file.
	 * 
	 * @param title The title to query. PRECONDITION: This must be a valid file name prefixed with the "File:" prefix, or you will get strange results.
	 * @return A list of pages linking to the file.
	 */
	public ArrayList<String> fileUsage(String title)
	{
		log.info("{}: Fetching local file usage of {}", this, title);
		return MQuery.fileUsage(this, FL.toSAL(title)).get(title);
	}

	/**
	 * Gets a list of file extensions for the types of files which can be uploaded to this Wiki. WARNING: this method is not cached so save the result.
	 * 
	 * @return A list of file extensions for files which can be uploaded to this Wiki.
	 */
	public ArrayList<String> getAllowedFileExts()
	{
		log.info("{}: Fetching a list of permissible file extensions", this);
		return FL.toAL(new WQuery(this, WQuery.ALLOWEDFILEXTS).next().listComp("fileextensions").stream().map(e -> GSONP.getStr(e, "ext")));
	}

	/**
	 * Get the categories of a page.
	 * 
	 * @param title The title to get categories of.
	 * @return A list of categories, or the empty list if something went wrong.
	 */
	public ArrayList<String> getCategoriesOnPage(String title)
	{
		log.info("{}: Getting categories of {}", this, title);
		return MQuery.getCategoriesOnPage(this, FL.toSAL(title)).get(title);
	}

	/**
	 * Get a limited number of titles in a category.
	 * 
	 * @param title The category to query, including the "Category:" prefix.
	 * @param ns Namespace filter. Any title not in the specified namespace(s) will be ignored. Leave blank to select all namespaces. CAVEAT: skipped items are counted against {@code cap}.
	 * @return The list of titles, as specified, in the category.
	 */
	public ArrayList<String> getCategoryMembers(String title, NS... ns)
	{
		log.info("{}: Getting category members from {}", this, title);

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
	 * @return The number of elements in the category. Value returned will be -1 if Category entered was empty <b>and</b> non-existent.
	 */
	public int getCategorySize(String title)
	{
		log.info("{}: Getting category size of {}", this, title);
		return MQuery.getCategorySize(this, FL.toSAL(title)).get(title);
	}

	/**
	 * Gets the contributions of a user.
	 * 
	 * @param user The user to get contribs for, without the "User:" prefix.
	 * @param cap The maximum number of results to return. Optional, disable with -1 (<b>caveat</b>: this will get *all* of a user's contributions)
	 * @param olderFirst Set to true to enumerate from older → newer revisions
	 * @param createdOnly Filter returned titles for instances where the contribution was a page creation. Optional, set false to disable.
	 * @param ns Restrict titles returned to the specified Namespace(s). Optional, leave blank to select all namespaces.
	 * @return A list of contributions.
	 */
	public ArrayList<Contrib> getContribs(String user, int cap, boolean olderFirst, boolean createdOnly, NS... ns)
	{
		log.info("{}: Fetching contribs of {}", this, user);

		WQuery wq = new WQuery(this, cap, WQuery.USERCONTRIBS).set("ucuser", user);
		if (ns.length > 0)
			wq.set("ucnamespace", nsl.createFilter(ns));
		if (olderFirst)
			wq.set("ucdir", "newer");
		if (createdOnly)
			wq.set("ucshow", "new");

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
		log.info("{}: Getting duplicates of {}", this, title);
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
		log.info("{}: Getting external links on {}", this, title);
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
		log.info("{}: Getting image info for {}", this, title);
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
		log.info("{}: Getting files on {}", this, title);
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
		log.info("{}: Getting wiki links on {}", this, title);
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
		return FL.toAL(MQuery.exists(this, getLinksOnPage(title, ns)).entrySet().stream().filter(t -> t.getValue() == exists).map(Map.Entry::getKey));
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
		log.info("{}: Fetching log entries -> title: %s, user: %s, type: %s", this, title, user, type);

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
		log.info("{}: Getting page text of {}", this, title);
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
		log.info("{}: Fetching a list of protected titles", this);

		WQuery wq = new WQuery(this, limit, WQuery.PROTECTEDTITLES);
		if (ns.length > 0)
			wq.set("ptnamespace", nsl.createFilter(ns));
		if (olderFirst)
			wq.set("ptdir", "newer"); // MediaWiki is weird.

		ArrayList<ProtectedTitleEntry> l = new ArrayList<>();
		while (wq.has())
			l.addAll(FL.toAL(wq.next().listComp("protectedtitles").stream().map(jo -> GSONP.gson.fromJson(jo, ProtectedTitleEntry.class))));

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
		log.info("{}: Fetching random page(s)", this);

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
	 * Gets a specified number of Recent Changes in between two timestamps. WARNING: if you use both {@code start} and {@code end}, then {@code start} MUST be earlier than {@code end}. If you set both
	 * {@code start} and {@code end} to null, then the default behavior is to fetch the last 30 seconds of recent changes.
	 * 
	 * @param start The Instant to start enumerating from. Can be used without {@code end}. Optional param - set null to disable.
	 * @param end The Instant to stop enumerating at. {@code start} must be set, otherwise this will be ignored. Optional param - set null to disable.
	 * @return A list Recent Changes where return order is newer -&gt; Older
	 */
	public ArrayList<RCEntry> getRecentChanges(Instant start, Instant end)
	{
		log.info("{}: Querying recent changes", this);

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
	 * @param start The instant to start enumerating from. Start date must occur before end date. Optional param - set null to disable.
	 * @param end The instant to stop enumerating at. Optional param - set null to disable.
	 * @return A list of page revisions
	 */
	public ArrayList<Revision> getRevisions(String title, int cap, boolean olderFirst, Instant start, Instant end)
	{
		log.info("{}: Getting revisions from {}", this, title);

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
	 * Gets the shared (non-local) duplicates of a file. PRECONDITION: The Wiki this query is run against has the <a href="https://www.mediawiki.org/wiki/Extension:GlobalUsage">GlobalUsage</a>
	 * extension installed.
	 * 
	 * @param title The title of the file to query
	 * @return An ArrayList containing shared duplicates of the file
	 */
	public ArrayList<String> getSharedDuplicatesOf(String title)
	{
		log.info("{}: Getting shared duplicates of {}", this, title);
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
		log.info("{}: Getting templates transcluded on {}", this, title);
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
		log.info("{}: Getting a text extract for {}", this, title);
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
		log.info("{}: Fetching uploads for {}", this, user);

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
		log.info("{}: Getting global usage for {}", this, title);
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
		log.info("{}: Getting user rights for {}", this, user);
		return MQuery.listUserRights(this, FL.toSAL(user)).get(user);
	}

	/**
	 * Performs a prefix search using the specified prefix and namespace, and returns the best matching titles.
	 * 
	 * @param namespace The namespace to filter by (inclusive)
	 * @param prefix Get all titles in the specified namespace, that start with this String. To select subpages only, append a {@code /} to the end of this parameter.
	 * @return The list of titles starting with the specified prefix
	 */
	public ArrayList<String> prefixIndex(NS namespace, String prefix)
	{
		log.info("{}: Doing prefix index search for {}", this, prefix);
		return allPages(prefix, false, false, -1, namespace);
	}

	/**
	 * Queries a special page.
	 * 
	 * @param title The special page to query, without the {@code Special:} prefix. CAVEAT: this is CASE-sensitive, so be sure to use the exact title (e.g. {@code UnusedFiles},
	 *           {@code BrokenRedirects}). For a full list of titles, see <a href="https://www.mediawiki.org/w/api.php?action=help&modules=query+querypage">the official documentation</a>.
	 * @param cap The maximum number of elements to return. Use {@code -1} to get everything, but be careful because some pages can have 10k+ entries.
	 * @return A List of titles returned by this special page.
	 */
	public ArrayList<String> querySpecialPage(String title, int cap)
	{
		log.info("{}: Querying special page {}", this, title);

		WQuery wq = new WQuery(this, cap, WQuery.QUERYPAGES).set("qppage", nss(title));
		ArrayList<String> l = new ArrayList<>();

		while (wq.has())
			try
			{
				l.addAll(FL.toAL(FL.streamFrom(GSONP.getNestedJA(wq.next().input, FL.toSAL("query", "querypage", "results"))).map(e -> GSONP.getStr(e.getAsJsonObject(), "title"))));
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
		log.info("{}: Resolving redirect for {}", this, title);
		return MQuery.resolveRedirects(this, FL.toSAL(title)).get(title);
	}

	/**
	 * Performs a search on the Wiki.
	 * 
	 * @param query The query string to search the Wiki with.
	 * @param limit The maximum number of entries to return. Optional, specify {@code -1} to disable (not recommended if your wiki is big).
	 * @param ns Limit search to these namespaces. Optional, leave blank to disable. The default behavior is to search all namespaces.
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
		log.info("{}: Splitting {} by header", this, title);

		try
		{
			return PageSection.pageBySection(
					GSONP.getJAofJO(GSONP.getNestedJA(JsonParser.parseString(basicGET("parse", "prop", "sections", "page", title).body().string()).getAsJsonObject(), FL.toSAL("parse", "sections"))),
					getPageText(title));
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
		log.info("{}: Getting links to {}", this, title);
		return MQuery.linksHere(this, redirects, FL.toSAL(title)).get(title);
	}

	/**
	 * Gets a list of direct links to a page. CAVEAT: This does not get any pages linking to a redirect pointing to this page; in order to do this you will first need to obtain a list of redirects to
	 * the target, and then call <code>whatLinksHere()</code> on each of those redirects.
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
	 * @param title The title to query. You *must* include the namespace prefix (e.g. "Template:") or you will get strange results.
	 * @param ns Only return results from this/these namespace(s). Optional param: leave blank to disable.
	 * @return The pages transcluding <code>title</code>.
	 */
	public ArrayList<String> whatTranscludesHere(String title, NS... ns)
	{
		log.info("{}: Getting list of pages that transclude {}", this, title);
		return MQuery.transcludesIn(this, FL.toSAL(title), ns).get(title);
	}
}
