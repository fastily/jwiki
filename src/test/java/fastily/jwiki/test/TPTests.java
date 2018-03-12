package fastily.jwiki.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

import fastily.jwiki.core.WParser;
import fastily.jwiki.core.WParser.WTemplate;
import fastily.jwiki.core.WParser.WikiText;
import fastily.jwiki.core.Wiki;
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
	 * Test parsePage in WParse
	 */
	@Test
	public void testParsePage()
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
	 * Test parseText in WParse
	 */
	@Test
	public void testParseText()
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
		assertEquals("test <!-- meh --> abc", t.get("asdf").toString());
		assertEquals("big space", t.get("s").toString());

		// test drop
		t.drop();
		assertTrue(wt.getTemplates().isEmpty());
	}

	/**
	 * Tests parseText with comments in strange locations
	 */
	@Test
	public void testParseTextWithComments()
	{
		WikiText wt = WParser.parseText(wiki, wiki.getPageText("User:Fastily/Sandbox/TPTest3"));
		ArrayList<WTemplate> wtl = wt.getTemplates();

		WTemplate t = wtl.get(0);
		assertEquals("Tl", t.title);
		assertEquals("test <!-- meh --> abc", t.get("asdf").toString());
		assertEquals("<!-- ignore --> ok", t.get("bsdf").toString());
	}

	/**
	 * Test for WikiText
	 */
	@Test
	public void testWikiText()
	{
		WikiText wt = new WikiText();
		assertTrue(wt.getTemplatesR().isEmpty());

		wt.append("foo");
		assertEquals("foo", wt.toString());

		WTemplate tp1 = new WTemplate();
		tp1.title = "Template:test";
		wt.append(tp1);

		assertEquals("foo{{Template:test}}", wt.toString());

		ArrayList<WTemplate> wtl = wt.getTemplates();
		assertEquals(1, wtl.size());
		assertEquals("Template:test", wtl.get(0).title);

		tp1.normalizeTitle(wiki);
		assertEquals("Test", tp1.title);

		assertEquals("foo{{Test}}", wt.toString());

		wt.append("bar");
		assertEquals("foo{{Test}}bar", wt.toString());

		tp1.put("baz", "nyah");
		assertEquals("foo{{Test|baz=nyah}}bar", wt.toString());

		tp1.drop();
		assertEquals("foobar", wt.toString());
	}
}