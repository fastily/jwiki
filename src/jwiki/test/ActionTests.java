package jwiki.test;

import static jwiki.test.Config.*;
import static org.junit.Assert.*;

import java.time.Instant;

import org.junit.Test;

import jwiki.dwrap.Revision;

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