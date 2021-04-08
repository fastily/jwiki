package org.fastily.jwiki.test;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.fastily.jwiki.core.NameSpace;
import org.junit.jupiter.api.Test;

import okhttp3.HttpUrl;

/**
 * Unit tests for WAction. Mocks cases where user is anonymous.
 * 
 * @author Fastily
 *
 */
public class ActionTests extends BaseMockTemplate
{
	/**
	 * Sanity check to make sure the mock Wiki object is properly initialized.
	 */
	@Test
	public void testInitializationForSanity()
	{
		assertEquals("File:Test.jpg", wiki.convertIfNotInNS("Test.jpg", NameSpace.FILE));

		try
		{
			assertTrue(server.takeRequest(2, TimeUnit.SECONDS).getHeader("User-Agent").contains("jwiki"));
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			fail(e);
		}

		assertEquals(NameSpace.FILE.valueForNameSpace, wiki.whichNS("File:Test.jpg").valueForNameSpace);
		assertEquals(NameSpace.MAIN.valueForNameSpace, wiki.whichNS("hello").valueForNameSpace);
	}

	/**
	 * Test move
	 */
	@Test
	public void testMove()
	{
		addResponse("mockMovePage");
		assertTrue(wiki.move("User:Example/12345678", "User:Example/1", true, true, true, "This is a test"));
	}

	/**
	 * Test editing
	 */
	@Test
	public void testEdit()
	{
		addResponse("mockSuccessEdit");
		assertTrue(wiki.edit("Wikipedia:Sandbox", "Hello, World!", "This is a test"));
	}

	/**
	 * Tests prepending and appending text via edit.
	 */
	@Test
	public void testAddText()
	{
		addResponse("mockSuccessEdit");
		addResponse("mockSuccessEdit");
		assertTrue(wiki.addText("Wikipedia:Sandbox", "Appending text!", "test", true));
		assertTrue(wiki.addText("Wikipedia:Sandbox", "Appending text!", "test", false));
	}

	/**
	 * Tests uploading of files
	 */
	@Test
	public void testUpload()
	{
		addResponse("mockChunkedUpload");
		addResponse("mockFileUnstash");

		try
		{
			assertTrue(wiki.upload(Paths.get(getClass().getResource("uploadTestFile.svg").toURI()), "TestSVG.svg", "desc", "summary"));
		}
		catch (Throwable e)
		{
			fail("Should never reach here - is the classpath messed up or a test resource missing?", e);
		}
	}

	/**
	 * Tests upload by url functionality.
	 */
	@Test
	public void testUploadByUrl()
	{
		addResponse("mockUploadByUrl");

		try
		{
			assertTrue(wiki.uploadByUrl(HttpUrl.parse("https://upload.wikimedia.org/wikipedia/en/a/a1/Example.jpg"), "TestFile.jpg", "desc", "summary"));
		}
		catch (Throwable e)
		{
			fail("Should never reach here - is the classpath messed up or a test resource missing?", e);
		}
	}

	/**
	 * Tests purging of pages
	 */
	@Test
	public void testPurge()
	{
		addResponse("mockPagePurge");
		wiki.purge("Foo", "Test", "Wikipedia:Sandbox");
	}
}