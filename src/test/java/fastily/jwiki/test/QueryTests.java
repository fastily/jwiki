package fastily.jwiki.test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.dwrap.Contrib;
import fastily.jwiki.dwrap.ImageInfo;
import fastily.jwiki.dwrap.PageSection;
import fastily.jwiki.dwrap.Revision;
import fastily.jwiki.util.FL;
import okhttp3.HttpUrl;


/**
 * Query tests (non-admin) for jwiki's Wiki.java.  These are only simple sanity checks; this is not a comprehensive test suite.
 * 
 * @author Fastily
 *
 */
public class QueryTests
{
	/**
	 * The wiki object to use for this test set.
	 */
	private static Wiki wiki = new Wiki("test.wikipedia.org");
	
	/**
	 * Tests for namespace handling
	 */
	@Test
	public void testBasicNSHandling()
	{
		assertEquals(NS.MAIN, wiki.getNS("Main"));
		assertNull(wiki.getNS("blahblahblah"));
		
		assertEquals(NS.USER, wiki.whichNS("User:TestUser"));
		assertEquals("ABC.jpg", wiki.nss("File:ABC.jpg"));
		assertEquals("ABC.jpg", wiki.nss("fIlE:ABC.jpg"));
		assertEquals("TestUser", wiki.nss("user tALk:TestUser"));
		
		assertEquals("File:ABC.jpg", wiki.convertIfNotInNS("ABC.jpg", NS.FILE));
		assertEquals("Testing", wiki.convertIfNotInNS("Testing", NS.MAIN));
		assertEquals("User talk:TestUser", wiki.convertIfNotInNS("TestUser", NS.USER_TALK));
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
		ArrayList<String> result = wiki.allPages("Fastily/Sandbox/Page/", false, false, -1, NS.USER);
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
		ArrayList<String> result = wiki.allPages("Fastily/Sandbox/Redirect", true, false, -1, NS.USER);
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
		assertTrue(wiki.exists("uSeR:fastily/Sandbox"));
		
		assertFalse(wiki.exists("User:Fastily/NoPageHere"));
		assertFalse(wiki.exists("user:fastily/noPageHere"));
		
		// doesn't actually exist but should still return true
		assertTrue(wiki.exists("User:Fastily/Sandbox#Test98769876"));
		
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
	 * Test for getCategorySize
	 */
	@Test
	public void testGetCategorySize()
	{
		assertEquals(4, wiki.getCategorySize("Category:Fastily Test"));
		assertEquals(2, wiki.getCategorySize("Category:Fastily Test2"));
		
		assertEquals(0, wiki.getCategorySize("Category:Does0Not0Exist6"));
	}

	/**
	 * Tests getContribs
	 */
	@Test
	public void testGetContribs()
	{
		// Test 1
		ArrayList<Contrib> result = wiki.getContribs("FastilyClone", -1, false, NS.FILE);

		assertEquals("File:FCTest2.svg", result.get(0).title); // descending
		assertEquals("File:FCTest1.png", result.get(1).title);

		assertEquals(244225, result.get(0).revid);
		assertEquals(244224, result.get(1).revid);
		
		assertEquals(0, result.get(0).parentid);
		assertEquals(0, result.get(1).parentid);
		
		assertEquals(Instant.parse("2015-10-20T00:28:54Z"), result.get(0).timestamp);
		assertEquals(Instant.parse("2015-10-20T00:28:32Z"), result.get(1).timestamp);

		// Test 2
		result = wiki.getContribs("FastilyClone", 1, true, NS.FILE);
		assertEquals("File:FCTest1.png", result.get(0).title);
		
		// Test 3 - non-existent user
		result = wiki.getContribs("Fastilyy", 10, true);
		assertTrue(result.isEmpty());
	}

	/**
	 * Tests getDuplicatesOf()
	 */
	@Test
	public void testGetDuplicatesOf()
	{
		ArrayList<String> result = wiki.getDuplicatesOf("File:FastilyTest.svg", true);

		assertEquals(1, result.size());
		assertEquals("File:FastilyTestCopy.svg", result.get(0));
	}

	/**
	 * Test for getImageInfo()
	 */
	@Test
	public void testGetImageInfo()
	{
		// Test 1
		ImageInfo result = wiki.getImageInfo("File:FastilyTestR.svg").get(0);
		assertEquals(477, result.height);
		assertEquals(512, result.width);
		assertEquals(876, result.size);
		assertEquals("275e96b2660f761cca02b8d2cb5425bcaab4dd98", result.sha1);
		
		// Test 2
		result = wiki.getImageInfo("File:FastilyTest.svg").get(0);
		assertEquals(477, result.height);
		assertEquals(512, result.width);
		assertEquals(876, result.size);
		assertEquals("275e96b2660f761cca02b8d2cb5425bcaab4dd98", result.sha1);
		assertEquals("image/svg+xml", result.mime);
		assertEquals(HttpUrl.parse("https://upload.wikimedia.org/wikipedia/test/f/f7/FastilyTest.svg"), result.url);
		assertEquals("part of unit test for jwiki", result.summary);
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
		ArrayList<Revision> result = wiki.getRevisions("User:FastilyClone/Page/1", -1, false, null, null);

		assertEquals(3, result.size());
		assertEquals("1", result.get(1).text);
		assertEquals("s0", result.get(2).summary);
		assertEquals(Instant.parse("2015-10-23T05:58:54Z"), result.get(0).timestamp);

		// Test 2
		result = wiki.getRevisions("User:FastilyClone/Page/1", 2, true, null, null);

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
	 * Tests user list group rights.
	 */
	@Test
	public void testListGroupRights()
	{
		ArrayList<String> l = wiki.listUserRights("Fastily");
		assertTrue(l.contains("sysop"));
		assertTrue(l.contains("autoconfirmed"));
		
		// non-existent usernames and IPs should return null
		assertNull(wiki.listUserRights("10.0.1.1"));
		assertNull(wiki.listUserRights("Fastilyy"));
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
	 * Tests whatTranscludesHere()
	 */
	@Test
	public void testWhatTranscludesHere()
	{
		ArrayList<String> l = wiki.whatTranscludesHere("Template:FastilyTest");
		
		assertEquals(2, l.size());
		assertTrue(l.contains("User:Fastily/Sandbox/T"));
		assertTrue(l.contains("FastilyTest"));
		
		l = wiki.whatTranscludesHere("Template:FastilyTest", NS.MAIN);
		assertEquals(1, l.size());
		assertTrue(l.contains("FastilyTest"));
	}
	
	/**
	 * Tests external link fetching.
	 */
	@Test
	public void testGetExternalLinks()
	{
		ArrayList<String> l = wiki.getExternalLinks("User:Fastily/Sandbox/ExternalLink");
		
		assertTrue(l.contains("https://www.google.com"));
		assertTrue(l.contains("https://www.facebook.com"));
		assertTrue(l.contains("https://github.com"));
		
		assertEquals(3, l.size());
	}
	
	/**
	 * Test getting text extracts
	 */
	@Test
	public void testGetTextExtracts()
	{
		assertEquals("Start of an article", wiki.getTextExtract("User:Fastily/Sandbox/Article"));
	}
	
	/**
	 * Test getting talk page of a title
	 */
	@Test
	public void testGetTalkPage() 
	{
		assertEquals("File talk:Example.jpg", wiki.talkPageOf("File:Example.jpg"));
		assertEquals("Talk:Main Page", wiki.talkPageOf("Main Page"));
		assertEquals("Wikipedia talk:Test", wiki.talkPageOf("Wikipedia:Test"));
		assertEquals("TimedText talk:File:Test.webm.srt", wiki.talkPageOf("TimedText:File:Test.webm.srt"));
		
		// check error conditions
		assertNull(wiki.talkPageOf("Talk:Main Page"));
		assertNull(wiki.talkPageOf("File talk:Example.jpg"));
		assertNull(wiki.talkPageOf("Special:Upload"));
	}
	
	/**
	 * Test getting the content page associated with a talk page
	 */
	@Test
	public void testTalkPageBelongsTo()
	{
		assertEquals("File:Example.jpg", wiki.talkPageBelongsTo("File talk:Example.jpg"));
		assertEquals("Main Page", wiki.talkPageBelongsTo("Talk:Main Page"));
		assertEquals("Wikipedia:Test", wiki.talkPageBelongsTo("Wikipedia talk:Test"));
		assertEquals("TimedText:File:Test.webm.srt", wiki.talkPageBelongsTo("TimedText talk:File:Test.webm.srt"));
		
		//check error conditions
		assertNull(wiki.talkPageBelongsTo("Main Page"));
		assertNull(wiki.talkPageBelongsTo("File:Example.jpg"));
		assertNull(wiki.talkPageBelongsTo("Special:Upload"));
	}
	
	/**
	 * Test splitting a page by header
	 */
	@Test
	public void testSplitPageByHeader()
	{
		ArrayList<PageSection> l = wiki.splitPageByHeader("User:Fastily/Sandbox/HelloWorld2");
		
		assertEquals(1, l.size());
		assertEquals("Hello, World!", l.get(0).header);
		assertEquals(2, l.get(0).level);
		
		
		l = wiki.splitPageByHeader("User:Fastily/Sandbox/HelloWorld");
		assertEquals(1, l.size());
		assertNull(l.get(0).header);
		assertEquals(-1, l.get(0).level);
		assertEquals("Hello World!", l.get(0).text);
		
		l = wiki.splitPageByHeader("User:Fastily/Sandbox/Article");
		assertEquals(3, l.size());
		assertNull(l.get(0).header);
		assertEquals("Section 1", l.get(1).header);
		assertEquals("Section 2", l.get(2).header);
		
		assertEquals("Start of an article\n\n", l.get(0).text);
		assertEquals("==Section 2==\nFoo Baz Bar", l.get(2).text);
	}
	
	/**
	 * Tests resolving of redirects
	 */
	@Test
	public void testResolveRedirect()
	{
		assertEquals("User:Fastily/Sandbox/RedirectTarget", wiki.resolveRedirect("User:Fastily/Sandbox/Redirect2"));
		
		// test resolving of non-redirect
		assertEquals("User:Fastily/Sandbox/Redirect3", wiki.resolveRedirect("User:Fastily/Sandbox/Redirect3"));
	}
}