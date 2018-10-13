package fastily.jwiki.test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

import fastily.jwiki.core.NS;
import fastily.jwiki.dwrap.LogEntry;
import fastily.jwiki.dwrap.ProtectedTitleEntry;
import fastily.jwiki.dwrap.RCEntry;
import fastily.jwiki.util.Tuple;

/**
 * Tests queries which may have dynamic/variable outputs.
 * 
 * @author Fastily
 *
 */
public class MockQueryTests extends BaseMockTemplate
{
	/**
	 * Mock fetching of random pages
	 */
	@Test
	public void testGetRandomPages()
	{
		addResponse("mockRandom");
		ArrayList<String> l = wiki.getRandomPages(3, NS.FILE, NS.MAIN);

		assertEquals(3, l.size());
		assertEquals(3, new HashSet<>(l).size());
	}

	/**
	 * Test fetching of global usage.
	 */
	@Test
	public void testGlobalUsage()
	{
		addResponse("mockGlobalUsage");

		ArrayList<Tuple<String, String>> l = wiki.globalUsage("File:Example.jpg");

		assertFalse(l.isEmpty());

		assertEquals("TestTest", l.get(0).x);
		assertEquals("ay.wikipedia.org", l.get(0).y);

		assertEquals("Foobar", l.get(1).x);
		assertEquals("bat-smg.wikipedia.org", l.get(1).y);

		assertEquals("Hello", l.get(2).x);
		assertEquals("ka.wiktionary.org", l.get(2).y);
	}

	/**
	 * Test protected title fetching
	 */
	@Test
	public void testProtectedTitles()
	{
		addResponse("mockProtectedTitles");

		ArrayList<ProtectedTitleEntry> l = wiki.getProtectedTitles(3, true);

		assertFalse(l.isEmpty());

		assertEquals("File:Test.jpg", l.get(0).title);
		assertEquals("Foo", l.get(0).user);
		assertEquals("summary1", l.get(0).summary);
		assertEquals(Instant.parse("2007-12-28T03:22:03Z"), l.get(0).timestamp);
		assertEquals("sysop", l.get(0).level);

		assertEquals("TestTest", l.get(1).title);
		assertEquals("Foo", l.get(1).user);
		assertEquals("summary2", l.get(1).summary);
		assertEquals(Instant.parse("2007-12-28T06:41:03Z"), l.get(1).timestamp);
		assertEquals("sysop", l.get(1).level);

		assertEquals("File:Example.jpg", l.get(2).title);
		assertEquals("Bar", l.get(2).user);
		assertEquals("summary3", l.get(2).summary);
		assertEquals(Instant.parse("2007-12-28T06:43:00Z"), l.get(2).timestamp);
		assertEquals("autoconfirmed", l.get(2).level);
	}

	/**
	 * Test recent changes fetching.
	 */
	@Test
	public void testRecentChanges()
	{
		addResponse("mockRecentChanges");

		ArrayList<RCEntry> l = wiki.getRecentChanges(Instant.parse("2017-12-31T02:06:08Z"), Instant.parse("2017-12-31T02:06:09Z"));

		assertFalse(l.isEmpty());

		assertEquals("edit", l.get(0).type);
		assertEquals("Title1", l.get(0).title);
		assertEquals("127.0.0.1", l.get(0).user);
		assertEquals(Instant.parse("2017-12-31T02:06:09Z"), l.get(0).timestamp);
		assertEquals("comment1", l.get(0).summary);

		assertEquals("new", l.get(1).type);
		assertEquals("Title2", l.get(1).title);
		assertEquals("TestUser", l.get(1).user);
		assertEquals(Instant.parse("2017-12-31T02:10:32Z"), l.get(1).timestamp);
		assertEquals("comment2", l.get(1).summary);

		assertEquals("log", l.get(2).type);
		assertEquals("Title3", l.get(2).title);
		assertEquals("Foobar", l.get(2).user);
		assertEquals(Instant.parse("2017-12-31T02:09:57Z"), l.get(2).timestamp);
		assertEquals("", l.get(2).summary);
	}

	/**
	 * Test log entry fetching.
	 */
	@Test
	public void testGetLogs()
	{
		// Test 1
		addResponse("mockLogEntry1");
		ArrayList<LogEntry> l = wiki.getLogs("File:Example.jpg", "Fastily", "delete", -1);

		assertEquals(3, l.size());

		assertEquals("File:Example.jpg", l.get(0).title);
		assertEquals("Fastily", l.get(0).user);
		assertEquals("summary1", l.get(0).summary);
		assertEquals("delete", l.get(0).action);
		assertEquals("delete", l.get(0).type);
		assertEquals(Instant.parse("2010-04-25T01:17:52Z"), l.get(0).timestamp);

		assertEquals(Instant.parse("2010-04-25T01:17:48Z"), l.get(1).timestamp);
		assertEquals("summary2", l.get(1).summary);

		assertEquals(Instant.parse("2010-04-25T01:17:45Z"), l.get(2).timestamp);
		assertEquals("summary3", l.get(2).summary);

		// Test 2
		addResponse("mockLogEntry2");
		l = wiki.getLogs("Test", null, null, -1);

		assertEquals(3, l.size());

		assertEquals("Test", l.get(0).title);
		assertEquals("Fastily", l.get(0).user);
		assertEquals("restore reason", l.get(0).summary);
		assertEquals("restore", l.get(0).action);
		assertEquals("delete", l.get(0).type);
		assertEquals(Instant.parse("2017-12-29T07:19:21Z"), l.get(0).timestamp);

		assertEquals(Instant.parse("2017-12-19T08:06:22Z"), l.get(1).timestamp);
		assertEquals("delete reason", l.get(1).summary);
		assertEquals("delete", l.get(1).action);
		assertEquals("delete", l.get(1).type);

		assertEquals(Instant.parse("2017-12-19T07:57:31Z"), l.get(2).timestamp);
		assertEquals("", l.get(2).summary);
		assertEquals("patrol", l.get(2).action);
		assertEquals("patrol", l.get(2).type);
		assertEquals("FastilyBot", l.get(2).user);
		assertEquals("Test", l.get(2).title);
	}

	/**
	 * Tests querying of special pages.
	 */
	@Test
	public void testQuerySpecialPage()
	{
		addResponse("mockQuerySpecialPage");

		ArrayList<String> l = wiki.querySpecialPage("Deadendpages", 10);

		assertEquals(3, l.size());

		assertTrue(l.contains("TestPage"));
		assertTrue(l.contains("File:Example.jpg"));
		assertTrue(l.contains("Talk:Main page"));
	}

	/**
	 * Tests listing of all pages
	 */
	@Test
	public void testGetAllPages()
	{
		addResponse("mockAllPages");

		ArrayList<String> l = wiki.allPages(null, false, false, 3, NS.MAIN);

		assertEquals(3, l.size());

		assertTrue(l.contains("Test"));
		assertTrue(l.contains("Foobar"));
		assertTrue(l.contains("Cats"));
	}

	/**
	 * Tests page searching
	 */
	@Test
	public void testSearch()
	{
		addResponse("mockSearch");

		ArrayList<String> l = wiki.search("GitHub", 5, NS.MAIN);

		assertEquals(5, l.size());

		assertTrue(l.contains("GitHub"));
		assertTrue(l.contains("Git"));
		assertTrue(l.contains("GitHub Pages"));
	}

	/**
	 * Tests fetching of shared duplicate files
	 */
	@Test
	public void testGetSharedDuplicateOf()
	{
		addResponse("mockSharedDuplicateFiles");

		ArrayList<String> l = wiki.getSharedDuplicatesOf("File:Test.jpg");

		assertEquals(1, l.size());
		assertTrue(l.contains("File:TestTest.jpg"));
	}

}