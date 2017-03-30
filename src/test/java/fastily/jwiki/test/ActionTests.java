package fastily.jwiki.test;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fastily.jwiki.core.NS;
import fastily.jwiki.core.Wiki;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/**
 * Unit tests for WAction. Mocks cases where user is anonymous.
 * 
 * @author Fastily
 *
 */
public class ActionTests
{
	/**
	 * The mock MediaWiki server
	 */
	protected MockWebServer server;

	/**
	 * The test Wiki object to use.
	 */
	protected Wiki wiki;

	/**
	 * Initializes mock objects
	 * 
	 * @throws Throwable If the MockWebServer failed to start.
	 */
	@Before
	public void setUp() throws Throwable
	{
		server = new MockWebServer();
		server.start();

		System.err.printf("[FYI]: MockServer is @ [%s]%n", server.url("/w/api.php"));

		initWiki();
	}

	/**
	 * Disposes of mock objects
	 * 
	 * @throws Throwable If the MockWebServer failed to exit.
	 */
	@After
	public void tearDown() throws Throwable
	{
		wiki = null;

		server.shutdown();
		server = null;
	}

	/**
	 * Sanity check to make sure the mock Wiki object is properly initialized.
	 */
	@Test
	public void testInitializationForSanity()
	{
		assertEquals("File:Test.jpg", wiki.convertIfNotInNS("Test.jpg", NS.FILE));

		try
		{
			assertTrue(server.takeRequest(2, TimeUnit.SECONDS).getHeader("User-Agent").contains("jwiki"));
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			fail();
		}

		assertEquals(NS.FILE.v, wiki.whichNS("File:Test.jpg").v);
		assertEquals(NS.MAIN.v, wiki.whichNS("hello").v);
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
	 * Loads a MockResponse into the {@code server}'s queue.
	 * 
	 * @param fn The text file, without a {@code .txt} extension, to load a response from.
	 */
	protected void addResponse(String fn)
	{
		try
		{
			server.enqueue(new MockResponse()
					.setBody(String.join("\n", Files.readAllLines(Paths.get(getClass().getResource(fn + ".txt").toURI())))));
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			throw new RuntimeException("Should *never* reach here. Is a mock configuration file missing?");
		}
	}

	/**
	 * Initializes the mock Wiki object. Runs with {@code setUp()}; override this to customize {@code wiki}'s
	 * initialization behavior.
	 */
	protected void initWiki()
	{
		addResponse("mockNSInfo");
		wiki = new Wiki(null, null, server.url("/w/api.php"));
	}
}