package fastily.jwiki.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.Test;

import fastily.jwiki.core.NS;

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

	/**
	 * Test log fetching.  Test is empty for now because I don't have a good way to test in an evironment which stays static.
	 */
	@Test
	public void testGetLogs()
	{
		// TODO:
	}
}