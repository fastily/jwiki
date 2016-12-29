package fastily.jwiki.test;

import static org.junit.Assert.*;

import java.time.Instant;

import org.junit.Test;

import fastily.jwiki.core.Wiki;
import fastily.jwiki.dwrap.Revision;

/**
 * Action (non-admin) tests for jwiki's Wiki.java. PRECONDITION: Queries should be working because those are used to check the
 * results of the actions.  These are only simple sanity checks; this is not a comprehensive test suite.
 * 
 * @author Fastily
 *
 */
public class ActionTests
{
	/**
	 * The wiki object to use for this test set.
	 */
	private static final Wiki wiki = Config.getDefaultUser();
		
	/**
	 * Tests edit
	 */
	@Test
	public void testEdit()
	{
		String text = "Test Edit @ " + Instant.now();
		String summary = "Test Summary @ " + Instant.now();

		assertTrue(wiki.edit("User:Fastily/Sandbox/Edit", text, summary));

		Revision top = wiki.getRevisions("User:Fastily/Sandbox/Edit", 1, false, null, null).get(0);
		assertEquals(text, top.text);
		assertEquals(summary, top.summary);
	}
}