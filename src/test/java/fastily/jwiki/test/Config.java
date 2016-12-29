package fastily.jwiki.test;

import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FError;
import fastily.jwiki.util.WikiGen;

/**
 * Configuration for tests
 * 
 * @author Fastily
 *
 */
public final class Config
{
	/* //////////////////////////////////////////////////////////////////////////////// */
	/* /////////////////////////////// START CONFIG /////////////////////////////////// */
	/* //////////////////////////////////////////////////////////////////////////////// */

	/**
	 * The domain, in shorthand, we'll be running tests on
	 */
	protected static final String domain = "test.wikipedia.org";

	/**
	 * The user we'll be running tests with
	 */
	protected static final String user = "FastilyClone";

	/**
	 * The admin user we'll be running tests with.
	 */
	protected static final String adminUser = "Fastily";

	/* //////////////////////////////////////////////////////////////////////////////// */
	/* //////////////////////////////// END CONFIG /////////////////////////////////// */
	/* //////////////////////////////////////////////////////////////////////////////// */

	/**
	 * No constructors allowed.
	 */
	private Config()
	{

	}

	/**
	 * Gets a Wiki with the default user at the default domain, per the constants set in Config. PRECONDITION:
	 * User/password combinations have been setup using WikiGen.
	 * 
	 * @return The Wiki object with the default (non-admin) user.
	 */
	protected static Wiki getDefaultUser()
	{
		return getUser(user, domain);

	}

	/**
	 * Gets a Wiki with the default admin user at the default domain, per the constants set in Config. PRECONDITION:
	 * User/password combinations have been setup using WikiGen.
	 * 
	 * @return The Wiki object with the default (admin) user.
	 */
	protected static Wiki getDefaultAdmin()
	{
		return getUser(adminUser, domain);
	}

	/**
	 * Gets a Wiki with the specified user at the specified domain. PRECONDITION: The specified user/password
	 * combinations have been setup using WikiGen.
	 * 
	 * @param u The username to use
	 * @param d The domain (in shorthand) to login at
	 * @return The Wiki object with the specified user.
	 */
	protected static Wiki getUser(String u, String d)
	{
		Wiki wiki = WikiGen.wg.get(u, d);
		if (wiki == null)
			FError.errAndExit(String.format("Login for [ %s @ %s ] failed, exiting%n", u, d));

		return wiki;
	}
}