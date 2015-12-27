package jwiki.core;

import java.net.HttpCookie;
import java.net.URI;

import jwiki.util.FString;

/**
 * Perform wiki authentication and initialization tasks for a Wiki.
 * 
 * @author Fastily
 *
 */
public final class Auth
{
	/**
	 * No constructors allowed.
	 */
	private Auth()
	{

	}

	/**
	 * Attempts to perform login for a Wiki.  Sets cookies if successful.
	 * 
	 * @param wiki The wiki object to use.
	 * @return True on success.
	 */
	private static boolean login(Wiki wiki)
	{
		ColorLog.info(String.format("Logging in as %s @ %s", wiki.upx.x, wiki.domain));

		URLBuilder ub = wiki.makeUB("login");
		Reply r = WAction.doAction(wiki, ub, "lgname", wiki.upx.x);
		return r == null || r.hasError() || !r.resultIs("NeedToken") ? false : WAction.doAction(wiki, ub, "lgname",
				wiki.upx.x, "lgpassword", wiki.upx.y, "lgtoken", r.getStringR("token")).resultIs("Success");
	}

	/**
	 * Fetches namespace list and edit token for a wiki. Sets edit token and namespace handler if successful.
	 * 
	 * @param wiki The wiki object to use
	 * @return True on success.
	 */
	private static boolean doSetup(Wiki wiki)
	{
		ColorLog.info(wiki, "Fetching namespace list and csrf tokens");
		
		Reply r = new SQ(wiki, FString.paramMap("meta", URLBuilder.chainProps("siteinfo", "tokens"),
				"siprop", URLBuilder.chainProps("namespaces", "namespacealiases"), "type", "csrf")).query();
		
		//Reply r = QueryTools.doSingleQuery(wiki, wiki.makeUB("query", "meta", URLBuilder.chainProps("siteinfo", "tokens"),
		//		"siprop", URLBuilder.chainProps("namespaces", "namespacealiases"), "type", "csrf"));
		return (wiki.nsl = NS.NSManager.makeNSManager(r)) != null
				&& (wiki.token = r.getStringR("csrftoken")) != null;
	}

	/**
	 * Performs authentication and initialization.
	 * 
	 * @param wiki The wiki object to use
	 * @param newLogin Set to true if this is the first time we're logging in. Only necessary if we don't already have
	 *           centralauth cookies. If your wiki is not using the CentralAuth extension, set this to true.
	 * @return True on success.
	 */
	protected static boolean doAuth(Wiki wiki, boolean newLogin)
	{
		return (newLogin ? login(wiki) : true) && doSetup(wiki);
	}

	/**
	 * Copies CentralAuth cookies from one domain to another. Stores them in the specified Wiki's global cookie jar.
	 * 
	 * @param wiki The wiki object to use
	 * @param domain The domain to use for the copied CentralAuth cookies.
	 */
	protected static void copyCentralAuthCookies(Wiki wiki, String domain)
	{
		try
		{
			String cn;
			for (HttpCookie hc : wiki.cookiejar.getCookieStore().get(new URI(Settings.comPro + wiki.domain)))
				if ((cn = hc.getName()).contains("centralauth"))
					wiki.cookiejar.getCookieStore().add(new URI(Settings.comPro + domain), new HttpCookie(cn, hc.getValue()));
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}
}