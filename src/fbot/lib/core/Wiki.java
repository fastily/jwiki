package fbot.lib.core;

/**
 * Entry point. New style.
 * 
 * @author Fastily
 * 
 */
public class Wiki
{
	/**
	 * Our credentials. Public for now, but we should fix this.
	 */
	protected Settings settings;
	
	/**
	 * Constructor, auto intializes first domain to Wikimedia Commons.
	 * 
	 * @param user The username to use
	 * @param px The password to use
	 */
	public Wiki(String user, String px)
	{
		this(user, px, "commons.wikimedia.org");
	}
	
	/**
	 * Constructor, sets username, password, and domain. The user password combo must be valid or program will exit
	 * 
	 * @param user The username to use
	 * @param px The password to use
	 * @param domain The domain to use
	 */
	public Wiki(String user, String px, String domain)
	{
		settings = new Settings(user, px);
		settings.setTo(domain);
	}
	
	/**
	 * Allows us to switch domains. Will try to login & set cookies. Synchronized because there is no reason to be
	 * multi-threading this.
	 * 
	 * @param domain The domain to switch to, in shorthand
	 * @return True if we successfully logged in the new domain and set credentials properly.
	 */
	public synchronized boolean switchDomain(String domain)
	{
		return settings.setTo(domain);
	}
	
	/**
	 * Gets our current domain, in shorthand
	 * 
	 * @return The current domain, in shorthand (e.g. 'commons.wikimedia.org')
	 */
	public synchronized String getCurrentDomain()
	{
		return settings.getCurrentDomain();
	}
	
	/**
	 * Gets the user we're logged in as.
	 * 
	 * @return The user we're logged in as.
	 */
	public String whoami()
	{
		return settings.user;
	}
	
	/**
	 * Takes a namespace and gets its number. PRECONDITION: the prefix must be a valid namespace prefix.
	 * 
	 * @param prefix The prefix to use, without the ":".
	 * @return The numerical representation of the namespace.
	 */
	public int getNS(String prefix)
	{
		return settings.getNSL().convert(prefix);
	}
	
	/**
	 * Takes a namespace number and returns its name.
	 * 
	 * @param num The namespace number to get the canonical name for.
	 * @return The namespace prefix associated with this number, or null if it doesn't exist.
	 */
	public String getNS(int num)
	{
		return settings.getNSL().convert(num);
	}
	
	/**
	 * Gets the number of the namespace for the title passed in. No namespace is assumed to be main namespace.
	 * 
	 * @param title The title to check the namespace number for.
	 * @return The integer number of the namespace of the title.
	 */
	public int whichNS(String title)
	{
		return settings.getNSL().whichNS(title);
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
		return whichNS(title) == getNS(ns) ? title : String.format("%s:%s", ns, title);
	}
	
	/**
	 * Checks if we're verified for the specified domain.
	 * @return True if we're verified for the specified domain.
	 */
	public boolean isVerified(String domain)
	{
		return settings.isVerifiedFor(domain);
	}
	
	
	/**
	 * Convenience method, makes a URLBuilder.
	 * 
	 * @return A URLBuilder with our current domain.
	 */
	protected URLBuilder makeUB()
	{
		return new URLBuilder(getCurrentDomain());
	}
	
}