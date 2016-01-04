package jwiki.test;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import jwiki.core.NS;
import jwiki.core.Wiki;
import jwiki.dwrap.Contrib;
import jwiki.dwrap.ImageInfo;
import jwiki.dwrap.Revision;
import jwiki.extras.WikiGen;
import jwiki.util.FError;
import jwiki.util.FL;
import jwiki.util.Tuple;


/**
 * Query tests (non-admin) for jwiki's Wiki.java. Caveat: this is by no means comprehensive.
 * 
 * @author Fastily
 *
 */
public class QueryTests
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
	 * Tests for namespace handling
	 */
	@Test
	public void testBasicNSHandling()
	{
		assertEquals(NS.MAIN, wiki.getNS("Main"));
		assertEquals(NS.USER, wiki.whichNS("User:TestUser"));
		assertEquals("ABC.jpg", wiki.nss("File:ABC.jpg"));

		assertEquals("File:ABC.jpg", wiki.convertIfNotInNS("ABC.jpg", NS.FILE));
		assertEquals("Testing", wiki.convertIfNotInNS("Testing", NS.MAIN));
	}

	/**
	 * Test for prefix index.
	 */
	@Test
	public void testPrefixIndex()
	{
		ArrayList<String> result = wiki.prefixIndex(NS.USER, "Fastily/Sandbox/Page/");
		ArrayList<String> expected = FL.toSAL("User:Fastily/Sandbox/Page/1", "User:Fastily/Sandbox/Page/2",
				"User:Fastily/Sandbox/Page/3");

		assertEquals(3, result.size());
		assertTrue(result.containsAll(expected));
	}

	/**
	 * Tests PrefixIndex functionality of allPages. NB: PrefixIndex is built off all-pages.
	 */
	@Test
	public void testAllpages1()
	{
		ArrayList<String> result = wiki.allPages("Fastily/Sandbox/Page/", false, -1, NS.USER);
		ArrayList<String> expected = FL.toSAL("User:Fastily/Sandbox/Page/1", "User:Fastily/Sandbox/Page/2",
				"User:Fastily/Sandbox/Page/3");

		assertEquals(3, result.size());
		assertTrue(result.containsAll(expected));
	}

	/**
	 * Tests allpages() for redirect selection.
	 */
	@Test
	public void testAllPages2()
	{
		ArrayList<String> result = wiki.allPages("Fastily/Sandbox/Redirect", true, -1, NS.USER);
		ArrayList<String> expected = FL.toSAL("User:Fastily/Sandbox/Redirect1", "User:Fastily/Sandbox/Redirect2");

		assertEquals(2, result.size());
		assertTrue(result.containsAll(expected));

		assertFalse(result.contains("User:Fastily/Sandbox/Redirect3")); // Redirect3 isn't actually a redirect
	}

	/**
	 * Test exists()
	 */
	@Test
	public void testExists()
	{
		assertTrue(wiki.exists("Main Page"));
		assertTrue(wiki.exists("User:Fastily/Sandbox"));

		assertFalse(wiki.exists("User:Fastily/NoPageHere"));
	}

	/**
	 * Test for fileUsage()
	 */
	@Test
	public void testFileUsage()
	{
		// Test 1
		ArrayList<String> result = wiki.fileUsage("File:FastilyTest.svg");
		ArrayList<String> expected = FL.toSAL("User:Fastily/Sandbox/ImageLinks", "User:Fastily/Sandbox/Page");

		assertEquals(2, result.size());
		assertTrue(result.containsAll(expected));
		
		// Test 2
		result = wiki.fileUsage("File:Fastily NonExistent File.png");
		//expected = new ArrayList<>();
		
		assertEquals(0, result.size());
	}

	/**
	 * Test to determine if namespace filtering is working
	 */
	@Test
	public void testFilterByNS()
	{
		ArrayList<String> l = new ArrayList<>(
				Arrays.asList("File:123.png", "User:Fastily", "Category:Foo", "Template:Tester", "User:Fastily/Sandbox"));

		// test 1
		ArrayList<String> result = wiki.filterByNS(l, NS.USER);

		assertEquals(2, result.size());
		assertTrue(result.contains("User:Fastily"));
		assertTrue(result.contains("User:Fastily/Sandbox"));

		// test 2
		result = wiki.filterByNS(l, NS.TEMPLATE);

		assertEquals(1, result.size());
		assertTrue(result.contains("Template:Tester"));
	}

	/**
	 * Test to determine if we're getting all categories on a page.
	 */
	@Test
	public void testGetCategoriesOnPage()
	{
		ArrayList<String> result = wiki.getCategoriesOnPage("User:Fastily/Sandbox/Page/2");
		ArrayList<String> expected = FL.toSAL("Category:Fastily Test", "Category:Fastily Test2");

		assertEquals(2, result.size());
		assertTrue(result.containsAll(expected));
	}

	/**
	 * Test for most basic usage of getCategoryMembers. No namespace filter.
	 */
	@Test
	public void testGetCategoryMembers1()
	{
		ArrayList<String> result = wiki.getCategoryMembers("Fastily Test2");
		ArrayList<String> expected = FL.toSAL("User:Fastily/Sandbox/Page/2", "File:FastilyTest.png");

		assertEquals(2, result.size());
		assertTrue(result.containsAll(expected));
	}

	/**
	 * Test for getCategoryMembers with namespace filter.
	 */
	@Test
	public void testGetCategoryMembers2()
	{
		ArrayList<String> result = wiki.getCategoryMembers("Fastily Test2", NS.FILE);

		assertEquals(1, result.size());
		assertTrue(result.contains("File:FastilyTest.png"));
	}

	/**
	 * Test for getCategoryMembers with limit and filter.
	 */
	@Test
	public void testGetCategoryMembers3()
	{
		ArrayList<String> result = wiki.getCategoryMembers("Fastily Test", 2, NS.USER);
		ArrayList<String> possible = FL.toSAL("User:Fastily/Sandbox/Page/1", "User:Fastily/Sandbox/Page/2",
				"User:Fastily/Sandbox/Page/3");

		assertEquals(2, result.size());
		assertTrue(possible.containsAll(result));
	}

	/**
	 * Test for getCategorySize
	 */
	@Test
	public void testGetCategorySize()
	{
		assertEquals(4, wiki.getCategorySize("Category:Fastily Test"));
		assertEquals(2, wiki.getCategorySize("Category:Fastily Test2"));
	}

	/**
	 * Tests getContribs
	 */
	@Test
	public void testGetContribs()
	{
		// Test 1
		ArrayList<Contrib> result = wiki.getContribs("FastilyClone", NS.FILE);

		assertEquals("File:FCTest2.svg", result.get(0).title); // descending
		assertEquals("File:FCTest1.png", result.get(1).title);

		assertEquals(Instant.parse("2015-10-20T00:28:54Z"), result.get(0).timestamp);
		assertEquals(Instant.parse("2015-10-20T00:28:32Z"), result.get(1).timestamp);

		// Test 2
		result = wiki.getContribs("FastilyClone", 1, true, NS.FILE);
		assertEquals("File:FCTest1.png", result.get(0).title);
	}

	/**
	 * Tests getDuplicatesOf()
	 */
	@Test
	public void testGetDuplicatesOf()
	{
		ArrayList<String> result = wiki.getDuplicatesOf("File:FastilyTest.svg", true);

		assertEquals(1, result.size());
		assertEquals("FastilyTestCopy.svg", result.get(0));
	}

	/**
	 * Test for getImageInfo()
	 */
	@Test
	public void testGetImageInfo()
	{
		// Test 1
		ImageInfo result = wiki.getImageInfo("File:FastilyTestR.svg");
		assertEquals(new Tuple<>(512, 477), result.dimensions);
		assertEquals("File:FastilyTest.svg", result.redirectsTo);
		assertEquals(876, result.size);
		assertEquals("File:FastilyTestR.svg", result.title);
		assertNull(result.thumbdimensions);

		// Test 2
		result = wiki.getImageInfo("File:FastilyTest.svg");
		assertEquals(new Tuple<>(512, 477), result.dimensions);
		assertEquals(876, result.size);
		assertEquals("File:FastilyTest.svg", result.title);
		assertEquals("https://upload.wikimedia.org/wikipedia/test/f/f7/FastilyTest.svg", result.url);
		assertNull(result.thumbdimensions);

		// Test 3
		result = wiki.getImageInfo("File:FastilyTest.svg", 250, 250);
		assertEquals(new Tuple<>(250, 233), result.thumbdimensions);
	}

	/**
	 * Test for getImagesOnPage()
	 */
	@Test
	public void testGetImagesOnPage()
	{
		ArrayList<String> result = wiki.getImagesOnPage("User:Fastily/Sandbox/Page");
		ArrayList<String> expected = FL.toSAL("File:FastilyTest.svg", "File:FastilyTest.png");

		assertEquals(2, result.size());
		assertTrue(result.containsAll(expected));
	}

	/**
	 * Test for getting *all* links on a page
	 */
	@Test
	public void testGetLinksOnPage1()
	{
		ArrayList<String> result = wiki.getLinksOnPage("User:Fastily/Sandbox/Page", NS.USER);
		ArrayList<String> expected = FL.toSAL("User:Fastily/Sandbox/Page/1", "User:Fastily/Sandbox/Page/2",
				"User:Fastily/Sandbox/Page/3", "User:Fastily/Sandbox/Page/4");

		assertEquals(4, result.size());
		assertTrue(result.containsAll(expected));
	}

	/**
	 * Test for getting only dead links on a page
	 */
	@Test
	public void testGetLinksOnPage2()
	{
		ArrayList<String> result = wiki.getLinksOnPage(false, "User:Fastily/Sandbox/Page", NS.USER);

		assertEquals(1, result.size());
		assertEquals("User:Fastily/Sandbox/Page/4", result.get(0));
	}

	/**
	 * Tests getPageText()
	 */
	@Test
	public void testGetPageText()
	{
		assertEquals("Hello World!", wiki.getPageText("User:Fastily/Sandbox/HelloWorld"));
		assertEquals("jwiki unit testing!", wiki.getPageText("Category:Fastily Test"));

		assertTrue(wiki.getPageText("User:Fastily/NoPageHere").isEmpty());
	}

	/**
	 * Tests getRevisions()
	 */
	@Test
	public void testGetRevisions()
	{
		// Test 1
		ArrayList<Revision> result = wiki.getRevisions("User:FastilyClone/Page/1");

		assertEquals(3, result.size());
		assertEquals("1", result.get(1).text);
		assertEquals("s0", result.get(2).summary);
		assertEquals(Instant.parse("2015-10-23T05:58:54Z"), result.get(0).timestamp);

		// Test 2
		result = wiki.getRevisions("User:FastilyClone/Page/1", 2, true);

		assertEquals(2, result.size());
		assertEquals("s1", result.get(1).summary);
		assertEquals("0", result.get(0).text);
	}

	/**
	 * Test for getTemplatesOnPage()
	 */
	@Test
	public void testGetTemplatesOnPage()
	{
		ArrayList<String> result = wiki.getTemplatesOnPage("User:Fastily/Sandbox/T");
		ArrayList<String> expected = FL.toSAL("User:Fastily/Sandbox/T/1", "Template:FastilyTest");

		assertEquals(2, result.size());
		assertTrue(result.containsAll(expected));
	}

	/**
	 * Test getting a user's uploads
	 */
	@Test
	public void testGetUserUploads()
	{
		ArrayList<String> result = wiki.getUserUploads("FastilyClone");
		ArrayList<String> expected = FL.toSAL("File:FCTest2.svg", "File:FCTest1.png");

		assertEquals(2, result.size());
		assertTrue(result.containsAll(expected));
	}

	/**
	 * Tests global usage. This does nothing at the moment, because gu isn't testable using testwiki. Might be doable if
	 * we get a test-commons
	 */
	@Test
	public void testGlobalUsage()
	{
		// nothing for now
	}

	/**
	 * Test for listing duplicate files. Basically does the same thing as the special page by the same name. This does
	 * nothing because the results returned by the server are variable
	 */
	@Test
	public void testListDuplicateFiles()
	{
		// nothing for now
	}

	/**
	 * Tests user list group rights.
	 */
	@Test
	public void testListGroupRights()
	{
		ArrayList<String> l = wiki.listGroupsRights("Fastily");
		assertTrue(l.contains("sysop"));
		assertTrue(l.contains("autoconfirmed"));
	}

	/**
	 * Tests what links here
	 */
	@Test
	public void testWhatLinksHere()
	{
		// test case where just getting *direct* links (no links to redirects considered)
		ArrayList<String> l = wiki.whatLinksHere("User:Fastily/Sandbox/Link/1");

		assertEquals(3, l.size());
		assertTrue(l.contains("User:Fastily/Sandbox/Link"));
		assertTrue(l.contains("User:Fastily/Sandbox/Link/2"));
		assertTrue(l.contains("User:Fastily/Sandbox/Link/3"));

		// test case where fetching redirects
		l = wiki.whatLinksHere("User:Fastily/Sandbox/Link/1", true);

		assertEquals(1, l.size());
		assertTrue(l.contains("User:Fastily/Sandbox/Link/4"));
	}
	
	/**
	 * Tests what transcludes here
	 */
	@Test
	public void whatTranscludesHere()
	{
		ArrayList<String> l = wiki.whatTranscludesHere("Template:FastilyTest");
		
		assertEquals(1, l.size());
		assertTrue(l.contains("User:Fastily/Sandbox/T"));
	}
}