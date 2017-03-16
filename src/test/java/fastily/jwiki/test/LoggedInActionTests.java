package fastily.jwiki.test;


import static org.junit.Assert.*;

import org.junit.Test;

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
	
	/**
	 * Test privileged delete.
	 */
	@Test
	public void testDelete()
	{
		addResponse("mockDeleteSuccess");
		assertTrue(wiki.delete("Test", "Test Reason"));
	}
	
	/**
	 * Test privileged undelete.
	 */
	@Test
	public void testUndelete()
	{
		addResponse("mockUndeleteSuccess");
		assertTrue(wiki.undelete("Test", "test"));
	}
}