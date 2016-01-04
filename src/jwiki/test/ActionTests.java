package jwiki.test;

import static org.junit.Assert.*;

import java.time.Instant;

import org.junit.BeforeClass;
import org.junit.Test;

import jwiki.core.Wiki;
import jwiki.dwrap.Revision;
import jwiki.extras.WikiGen;
import jwiki.util.FError;

/**
 * Action (non-admin) tests for jwiki's Wiki.java. PRECONDITION: Queries should be working because those are used to check the
 * results of the actions. Caveat: This is by no means comprehensive
 * 
 * @author Fastily
 *
 */
public class ActionTests
{
	
	/**
	 * The wiki object to use for this test set.
	 */
	private static Wiki wiki;
	
	/**
	 * Initializes the wiki object for this test set.  Exits if it is unable to create the required Wiki object.
	 */
	@BeforeClass
	public static void initWiki()
	{
		wiki = WikiGen.wg.get(Config.user, Config.domain);
		if(wiki == null)
			FError.errAndExit(String.format("Login for [ %s @ %s ] failed, exiting%n", Config.user, Config.domain));
	}
	
	/**
	 * Tests edit
	 */
	@Test
	public void testEdit()
	{
		String text = "Test Edit @ " + Instant.now();
		String summary = "Test Summary @ " + Instant.now();

		assertTrue(wiki.edit("User:Fastily/Sandbox/Edit", text, summary));

		Revision top = wiki.getRevisions("User:Fastily/Sandbox/Edit", 1, false).get(0);
		assertEquals(text, top.text);
		assertEquals(summary, top.summary);
	}
}