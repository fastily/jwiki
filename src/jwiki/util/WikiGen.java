package jwiki.util;

import java.util.HashMap;

import jwiki.core.Namespace;
import jwiki.core.Wiki;
import jwiki.mbot.MBot;

/**
 * Generates Wiki objects from login credentials created after running '<tt>java FLogin</tt>'.
 * 
 * @author Fastily
 *
 */
public class WikiGen
{
	/**
	 * Our password storage. You must have a file named '.px.txt' in your home directory for this to work.
	 */
	private static final HashMap<String, String> px = FLogin.genPXList();
	
	/**
	 * Create a cache so we don't login multiple times. Combination is: entry -> (username, wiki object)
	 */
	private static final HashMap<String, Wiki> cache = new HashMap<String, Wiki>();
	
	/**
	 * Hiding constructor from javadoc
	 */
	private WikiGen()
	{
		
	}
	
	/**
	 * Generates a Wiki object for me based off of the domain passed in, matched with the appropriate password.
	 * 
	 * @param user The username to use
	 * @param domain The domain to use
	 * 
	 * @return The wiki object, logged in. Null if we encountered an error (login/network) of some sort.
	 * @see #generate(String)
	 */
	public static Wiki generate(String user, String domain)
	{
		user = Namespace.nss(user); //idiot proofing
		
		if (cache.containsKey(user))
			return cache.get(user).getWiki(domain);
		
		Wiki wiki = null;
		try
		{
			if(!px.containsKey(user)) //we shouldn't run if someone requests a username we don't have.
				FError.errAndExit(String.format("'%s' does not have a password on file. Run FLogin again.", user));
			
			wiki = new Wiki(user, px.get(user), domain);
			cache.put(user, wiki);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
		return wiki;
	}
	
	/**
	 * Generates a wiki object based off the username passed in for 'commons.wikimedia.org'.
	 * 
	 * @param user The username to use
	 * @return The wiki object, logged in
	 * 
	 * @see #generate(String, String)
	 */
	public static Wiki generate(String user)
	{
		return generate(user, "commons.wikimedia.org");
	}
	
	/**
	 * Creates an MBot based off user and domain.
	 * 
	 * @param user User to use
	 * @param domain Domain to use
	 * @return An MBot with specified settings.
	 * 
	 * @see #genM(String)
	 */
	public static MBot genM(String user, String domain)
	{
		return new MBot(generate(user, domain));
	}
	
	/**
	 * Creates an MBot based off user. Domain = 'commons.wikimedia.org'
	 * 
	 * @param user Username to use
	 * @return MBot with specified settings.
	 */
	public static MBot genM(String user)
	{
		return new MBot(generate(user));
	}
	
	/**
	 * Create MBot based off user and thread count. Domain = 'commons.wikimedia.org'
	 * 
	 * @param user The user to use
	 * @param threads The maxmimum number of threads to instantiate.
	 * @return The MBot.
	 */
	public static MBot genM(String user, int threads)
	{
		return new MBot(generate(user), threads);
	}
}