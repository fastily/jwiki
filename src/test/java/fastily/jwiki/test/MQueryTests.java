package fastily.jwiki.test;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import fastily.jwiki.core.MQuery;
import fastily.jwiki.core.Wiki;
import fastily.jwiki.dwrap.ImageInfo;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.Tuple;

/**
 * Tests for MQuery in jwiki.  These are only simple sanity checks; this is not a comprehensive test suite.
 * @author Fastily
 *
 */
public class MQueryTests
{
	/**
	 * The Wiki object to use for this test set.
	 */
	private static final Wiki wiki = Config.getDefaultUser();
	
	/**
	 * Test for listUserRights.
	 */
	@Test
	public void testListUserRights()
	{
		HashMap<String, ArrayList<String>> result = MQuery.listUserRights(wiki, FL.toSAL("FastilyClone", "Fastily"));
		
		assertTrue(result.containsKey("Fastily"));
		assertTrue(result.containsKey("FastilyClone"));
		
		ArrayList<String> subResult = result.get("Fastily");
		assertTrue(subResult.contains("sysop"));
		assertTrue(subResult.contains("autoconfirmed"));
		
		subResult = result.get("FastilyClone");
		assertTrue(subResult.contains("autoconfirmed"));
	}
	
	/**
	 * Test for getImageInfo.
	 */
	@Test
	public void testGetImageInfo()
	{
		HashMap<String, ArrayList<ImageInfo>> result = MQuery.getImageInfo(wiki, FL.toSAL("File:FastilyTestCircle1.svg", "File:FastilyTestCircle2.svg"));
		
		assertTrue(result.containsKey("File:FastilyTestCircle1.svg"));
		assertTrue(result.containsKey("File:FastilyTestCircle2.svg"));
		
		ImageInfo subResult = result.get("File:FastilyTestCircle1.svg").get(0);
		assertEquals("Fastily", subResult.user);
		assertEquals(Instant.parse("2016-03-21T02:12:43Z"), subResult.timestamp);
		assertEquals(new Tuple<>(512, 502), subResult.dimensions);
		assertEquals("0bfe3100d0277c0d42553b9d16db71a89cc67ef7", subResult.sha1);
		
		subResult = result.get("File:FastilyTestCircle2.svg").get(0);
		assertEquals("Fastily", subResult.user);
		assertEquals(Instant.parse("2016-03-21T02:13:15Z"), subResult.timestamp);
		assertEquals(new Tuple<>(512, 502), subResult.dimensions);
		assertEquals("bbe1ffbfb03ec9489ffdb3f33596b531c7b222ef", subResult.sha1);
	}
	
	/**
	 * Test for getCategoriesOnPage.
	 */
	@Test
	public void testGetCategoriesOnPage()
	{
		HashMap<String, ArrayList<String>> result = MQuery.getCategoriesOnPage(wiki, FL.toSAL("User:Fastily/Sandbox/Page/2", "User:Fastily/Sandbox/Page/3"));
		
		assertTrue(result.containsKey("User:Fastily/Sandbox/Page/2"));
		assertTrue(result.containsKey("User:Fastily/Sandbox/Page/3"));
		
		ArrayList<String> subResult = result.get("User:Fastily/Sandbox/Page/2");
		assertEquals(2, subResult.size());
		assertTrue(subResult.contains("Category:Fastily Test"));
		assertTrue(subResult.contains("Category:Fastily Test2"));
		
		subResult = result.get("User:Fastily/Sandbox/Page/3");
		assertEquals(1, subResult.size());
		assertTrue(subResult.contains("Category:Fastily Test"));
	}
	
	/**
	 * Test for getCategorySize.
	 */
	@Test
	public void testGetCategorySize()
	{
		HashMap<String, Integer> result = MQuery.getCategorySize(wiki, FL.toSAL("Category:Fastily Test", "Category:Fastily Test2"));
		
		assertTrue(result.containsKey("Category:Fastily Test"));
		assertTrue(result.containsKey("Category:Fastily Test2"));
		
		assertEquals(new Integer(4), result.get("Category:Fastily Test"));
		assertEquals(new Integer(2), result.get("Category:Fastily Test2"));
	}
	
	/**
	 * Test for getPageText.
	 */
	@Test
	public void testGetPageText()
	{
		HashMap<String, String> result = MQuery.getPageText(wiki, FL.toSAL("User:Fastily/Sandbox/HelloWorld", "Category:Fastily Test"));
		
		assertTrue(result.containsKey("User:Fastily/Sandbox/HelloWorld"));
		assertTrue(result.containsKey("Category:Fastily Test"));
		
		assertEquals("Hello World!", result.get("User:Fastily/Sandbox/HelloWorld"));
		assertEquals("jwiki unit testing!", result.get("Category:Fastily Test"));
	}
}