package fbot.lib.core;

import java.net.CookieManager;
import java.util.HashMap;

import fbot.lib.core.aux.Logger;

/**
 * Container for an individual Wiki object's settings.
 * 
 * @author Fastily
 * 
 */
public class Credentials
{
	/**
	 * Our username & password
	 */
	protected String user, px;
	
	/**
	 * Our cookiejar
	 */
	protected final CookieManager cookiejar = new CookieManager();
	
	/**
	 * Our credential archive. This is useful for cross-wiki operations.
	 */
	protected final HashMap<String, CredStore> cs_archive = new HashMap<String, CredStore>();
	
	/**
	 * The current Credential Storage we're using. Contains our edit token and domain name. Again, useful for x-wiki
	 * operations.
	 */
	protected CredStore curr;
	
	/**
	 * Constructor, creates a settings object.
	 * 
	 * @param user Our username.
	 * @param px Our password.
	 */
	protected Credentials(String user, String px)
	{
		this.user = user;
		this.px = px;
	}
	
	/**
	 * Sets our current domain. If we're already logged in, we just set the mycredstore field. If we're not logged in,
	 * try to log in.
	 * 
	 * @param domain The domain to sign in to.
	 * @return True if we've set the domain successfully.
	 */
	protected boolean setTo(String domain)
	{
		Logger.fyi("Attemting to assign domain to " + domain);
		if (cs_archive.containsKey(domain))
		{
			curr = cs_archive.get(domain);
			return true;
		}
		
		CredStore temp = new CredStore(domain, this);
		if (temp.verify())
		{
			cs_archive.put(domain, temp);
			curr = temp;
			return true;
		}
		
		return false;
	}
	
	/**
	 * Manages and associates edit tokens with domains.
	 * 
	 * @author Fastily
	 * 
	 */
	protected static class CredStore
	{
		/**
		 * Our edit token
		 */
		protected String edittoken;
		
		/**
		 * The domain for the edit token.
		 */
		protected String domain;
		
		/**
		 * The namespace list.
		 */
		protected Namespace nsl;
		
		/**
		 * The Credentials object we're associated with.
		 */
		private Credentials sx;
		
		/**
		 * Constructor, takes domain and reference to calling Credentials object.
		 * 
		 * @param domain The domain to use, in shorthand (e.g. 'commons.wikimedia.org').
		 * @param sx A reference to the calling Credentials object.
		 */
		private CredStore(String domain, Credentials sx)
		{
			this.domain = domain;
			this.sx = sx;
		}
		
		/**
		 * Attempts to log us in and checks if our results are legitimate.
		 * 
		 * @return True if we logged in successfully.
		 */
		private boolean verify()
		{
			return login(domain) && (edittoken = findEditToken(domain)) != null && (nsl = generateNSL()) != null;
		}
		
		/**
		 * Attempts to log us in at the current domain.
		 * 
		 * @param domain The domain to login at. Should be passed in shorthand (e.g. 'commons.wikimedia.org')
		 * @return True if we were successful at logging in and setting the cookies and credentials.
		 */
		private boolean login(String domain)
		{
			Logger.info("Logging in as " + sx.user);
			try
			{
				URLBuilder ub = new URLBuilder(domain);
				ub.setAction("login");
				
				URLBuilder posttext = new URLBuilder(null);
				posttext.setParams("lgname", sx.user);
				
				Reply r = Request.post(ub.makeURL(), posttext.getParamsAsText(), sx.cookiejar, null);
				if (r.hasError())
					return false;
				else if (r.resultIs("NeedToken"))
				{
					posttext.setParams("lgpassword", sx.px, "lgtoken", r.getString("token"));
					return Request.post(ub.makeURL(), posttext.getParamsAsText(), sx.cookiejar, null).resultIs("Success");
				}
				
				return false;
			}
			catch (Throwable e)
			{
				e.printStackTrace();
				return false;
			}
		}
		
		/**
		 * Finds us an edit token on the wiki we are set to.
		 * 
		 * @param domain The domain to grab an edit token from.
		 * @return The edit token, or null if something went wrong.
		 */
		private String findEditToken(String domain)
		{
			Logger.info("Fetching edit token");
			try
			{
				URLBuilder ub = new URLBuilder(domain);
				//ub.setAction("query");
				//ub.setParams("prop", "info", "intoken", "edit", "titles", "Fastily");
				ub.setAction("tokens");
				ub.setParams("type", "edit");
				return Request.get(ub.makeURL(), sx.cookiejar).getString("edittoken");
			}
			catch (Throwable e)
			{
				e.printStackTrace();
				return null;
			}
		}
		
		/**
		 * Generates our namespace list.
		 * 
		 * @return The namespace list if we were successful, or null if something went wrong.
		 */
		private Namespace generateNSL()
		{
			Logger.info("Generating namespace list");
			try
			{
				URLBuilder ub = new URLBuilder(domain);
				ub.setAction("query");
				ub.setParams("meta", "siteinfo", "siprop", "namespaces");
				return Namespace.makeNamespace(Request.get(ub.makeURL(), sx.cookiejar).getJSONObject("namespaces"));
			}
			catch (Throwable e)
			{
				e.printStackTrace();
				return null;
			}
		}
	}
}