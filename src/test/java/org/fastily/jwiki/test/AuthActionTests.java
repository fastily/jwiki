package org.fastily.jwiki.test;

import static org.junit.jupiter.api.Assertions.*;

import org.fastily.jwiki.core.Wiki;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for WAction. Tests are performed as if the user is logged in.
 * 
 * @author Fastily
 *
 */
public class AuthActionTests extends BaseMockTemplate
{
	/**
	 * Initializes a logged-in Wiki.
	 */
	protected void initWiki()
	{
		addResponse("mockTokenNotLoggedIn");
		addResponse("mockLoginSuccess");
		addResponse("mockUserInfo");
		addResponse("mockTokenLoggedIn");
		addResponse("mockListSingleUserRights");
		addResponse("mockNSInfo");
		
		wiki = new Wiki.Builder().withApiEndpoint(server.url("/w/api.php")).withLogin("Test", "password").build();
	}

	/**
	 * Verify that the username was set properly.
	 */
	@Test
	public void testWhoAmI()
	{
		assertEquals("Test", wiki.whoami());
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