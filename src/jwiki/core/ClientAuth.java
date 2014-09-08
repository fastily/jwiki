package jwiki.core;

import java.net.HttpCookie;
import java.net.URI;

/**
 * Perform wiki authentication and initialization tasks.
 * 
 * @author Fastily
 *
 */
public class ClientAuth
{
	/**
	 * No constructors allowed; all static methods.
	 */
	private ClientAuth()
	{

	}

	/**
	 * Logs us in, and sets the cookies of the passed in Wiki object.
	 * 
	 * @param wiki The wiki object to use.
	 * @return True if we were successful.
	 */
	private static boolean login(Wiki wiki)
	{
		ColorLog.info(String.format("Logging in as %s @ %s", wiki.whoami(), wiki.domain));

		URLBuilder ub = wiki.makeUB("login");

		URLBuilder posttext = new URLBuilder(null);
		posttext.setParams("lgname", wiki.whoami());

		try
		{
			ServerReply r = ClientRequest.post(ub.makeURL(), posttext.getParamsAsString(), wiki.cookiejar, null);
			if (r.hasError())
				return false;
			else if (r.resultIs("NeedToken"))
			{
				posttext.setParams("lgpassword", wiki.upx.y, "lgtoken", r.getStringR("token"));
				return ClientRequest.post(ub.makeURL(), posttext.getParamsAsString(), wiki.cookiejar, null).resultIs("Success");
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Obtain an edit token, and assigns it to the wiki object passed in.
	 * 
	 * @param wiki The wiki object to obtain an edit token for.
	 * @return True if we were successful.
	 */
	private static boolean generateEditToken(Wiki wiki)
	{
		ColorLog.info(wiki, "Fetching edit token");

		URLBuilder ub = wiki.makeUB("tokens");
		try
		{
			wiki.token = ClientRequest.get(ub.makeURL(), wiki.cookiejar).getStringR("edittoken");
			return wiki.token != null;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Generates the namespace list for the wiki object passed in, and assigns it to said wiki object.
	 * 
	 * @param wiki The wiki object to generate an namespace list for.
	 * @return True if we were successful.
	 */
	private static boolean generateNSL(Wiki wiki)
	{
		ColorLog.info(wiki, "Generating namespace list");
		URLBuilder ub = wiki.makeUB("query", "meta", "siteinfo", "siprop", "namespaces");

		try
		{
			wiki.nsl = Namespace.makeNamespace(ClientRequest.get(ub.makeURL(), wiki.cookiejar).getJSONObjectR("namespaces"));
			return wiki.nsl != null;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Performs authentication and initialization.
	 * 
	 * @param wiki The wiki object to perform auth & init tasks on.
	 * @param newLogin Set to true if this is the first time we're logging in. Only necessary if we don't already have
	 *           centralauth cookies. If your wiki is not using the CentralAuth extension, set this to true.
	 * @return True if we were successful.
	 */
	protected static boolean doAuth(Wiki wiki, boolean newLogin)
	{
		return (newLogin ? login(wiki) : true) && generateEditToken(wiki) && generateNSL(wiki);
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
			for (HttpCookie hc : wiki.cookiejar.getCookieStore().get(new URI(Settings.comprotocol + wiki.domain)))
				if ((cn = hc.getName()).contains("centralauth"))
					wiki.cookiejar.getCookieStore().add(new URI(Settings.comprotocol + domain), new HttpCookie(cn, hc.getValue()));
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}
}