package fastily.jwiki.test;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;

import org.junit.Test;

import fastily.jwiki.core.NS;
import fastily.jwiki.dwrap.LogEntry;
import fastily.jwiki.dwrap.RCEntry;

/**
 * Tests queries which may have dynamic/variable outputs.
 * 
 * @author Fastily
 *
 */
public class MockQueryTests extends MockTemplate
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
	 * Tests global usage. This does nothing at the moment, because gu isn't testable using testwiki. Might be doable if
	 * we get a test-commons
	 */
	@Test
	public void testGlobalUsage()
	{
		// TODO:
	}

	/**
	 * Test for listing duplicate files. Basically does the same thing as the special page by the same name. This does
	 * nothing because the results returned by the server are variable
	 */
	@Test
	public void testListDuplicateFiles()
	{
		// TODO:
	}

	@Test
	public void testProtectedTitles()
	{
		//TODO:
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
}