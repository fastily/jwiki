package io.github.fastily.jwiki.test;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import io.github.fastily.jwiki.core.Wiki;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/**
 * Template for mock tests.
 * 
 * @author Fastily
 *
 */
public class BaseMockTemplate
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
	@BeforeEach
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
	@AfterEach
	public void tearDown() throws Throwable
	{
		wiki = null;

		server.shutdown();
		server = null;
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
					.setBody(String.join("\n", Files.readAllLines(Paths.get(getClass().getResource(fn + ".json").toURI())))));
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			throw new IllegalStateException("Should *never* reach here. Is a mock configuration file missing?");
		}
	}

	/**
	 * Initializes the mock Wiki object. Runs with {@code setUp()}; override this to customize {@code wiki}'s
	 * initialization behavior.
	 */
	protected void initWiki()
	{
		addResponse("mockNSInfo");
		wiki = new Wiki.Builder().withApiEndpoint(server.url("/w/api.php")).build();
	}
}