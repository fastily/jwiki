package fastily.jwiki.core;

import java.net.HttpCookie;
import java.net.URI;

import fastily.jwiki.util.FL;
import fastily.jwiki.util.FString;
import fastily.jwiki.util.JSONP;

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
	 * Attempts to perform login for a Wiki. Sets cookies if successful.
	 * 
	 * @param wiki The wiki object to use.
	 * @return True on success.
	 */
	private static boolean login(Wiki wiki)
	{
		ColorLog.info(String.format("Logging in as %s @ %s", wiki.upx.x, wiki.domain));

		String lgToken = SQ.with(wiki, FL.pMap("meta", "tokens", "type", "login")).singleQuery().getStringR("logintoken");
		return lgToken != null && WAction
				.doAction(wiki, "login", FL.pMap("lgname", wiki.upx.x, "lgpassword", wiki.upx.y, "lgtoken", lgToken)).resultIs("Success");
	}

	/**
	 * Sets the Namespace list, csrf tokens, and user groups for a Wiki.
	 * 
	 * @param wiki The Wiki object to use.
	 * @return True on success.
	 */
	private static boolean doSetup(Wiki wiki)
	{
		ColorLog.info(wiki, "Fetching namespace list and csrf tokens");

		Reply r = SQ.with(wiki,
				FL.pMap("meta", FString.pipeFence("siteinfo", "tokens"), "siprop", FString.pipeFence("namespaces", "namespacealiases"),
						"type", "csrf", "list", "users", "usprop", "groups", "ususers", wiki.upx.x))
				.singleQuery();
		try
		{
			wiki.isBot = JSONP.strsFromJA(r.getJAofJO("users").get(0).getJSONArray("groups")).contains("bot");
		}
		catch (Throwable e)
		{

		}

		wiki.nsl = new NS.NSManager(r);
		return (wiki.token = r.getStringR("csrftoken")) != null;
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