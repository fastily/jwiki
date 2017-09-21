package fastily.jwiki.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.Test;

import fastily.jwiki.core.Wiki;
import fastily.jwiki.tp.WParser;
import fastily.jwiki.tp.WTemplate;
import fastily.jwiki.tp.WikiText;
import fastily.jwiki.util.FL;

/**
 * Unit tests for jwiki's template parsing package.
 * 
 * @author Fastily
 *
 */
public class TPTests
{
	/**
	 * The Wiki object to use for this test set.
	 */
	private static Wiki wiki = new Wiki("test.wikipedia.org");

	/**
	 * Basic test for WParse
	 */
	@Test
	public void testWParseTest()
	{
		WikiText wt = WParser.parsePage(wiki, "User:Fastily/Sandbox/TPTest1");

		// Test getTemplates
		HashSet<String> l = FL.toSet(wt.getTemplates().stream().map(t -> t.title));
		assertTrue(l.contains("Tl"));
		assertTrue(l.contains("int:license-header"));
		assertTrue(l.contains("Ombox"));
		assertEquals(3, l.size());

		// Test recursive getTemplates
		l = FL.toSet(wt.getTemplatesR().stream().map(t -> t.title));
		assertTrue(l.contains("Tlx"));
		assertTrue(l.contains("Ombox"));
		assertTrue(l.contains("Tl"));
		assertTrue(l.contains("int:license-header"));
		assertTrue(l.contains("="));
		assertEquals(5, l.size());
	}

	/**
	 * Test for WTemplate.
	 */
	@Test
	public void testWTemplate()
	{
		WikiText wt = WParser.parseText(wiki, wiki.getPageText("User:Fastily/Sandbox/TPTest2"));
		ArrayList<WTemplate> wtl = wt.getTemplates();
		assertEquals(1, wtl.size());

		WTemplate t = wtl.get(0);

		// verify internals
		assertEquals("Tl", t.title);
		assertEquals("TEST1", t.get("1").toString());
		assertEquals("{{Tlx|1=FOOBAR|n=123456}}", t.get("another").toString());
		assertEquals("", t.get("empty").toString());

		// test drop
		t.drop();
		assertTrue(wt.getTemplates().isEmpty());
	}
}