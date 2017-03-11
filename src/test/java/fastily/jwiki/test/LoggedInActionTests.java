package fastily.jwiki.test;

import fastily.jwiki.core.Wiki;

/**
 * Unit tests for WAction. Tests are performed as if the user is logged in.
 * 
 * @author Fastily
 *
 */
public class LoggedInActionTests extends ActionTests
{
	/**
	 * Initializes a logged-in Wiki.
	 */
	protected void initWiki()
	{
		addResponse("mockTokenNotLoggedIn");
		addResponse("mockLoginSuccess");
		addResponse("mockTokenLoggedIn");
		addResponse("mockListSingleUserRights");
		addResponse("mockNSInfo");

		wiki = new Wiki("Test", "password", server.url("/w/api.php"));
	}
}