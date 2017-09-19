package fastily.jwiki.util;

import java.io.Console;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;

import fastily.jwiki.core.Wiki;
import fastily.jwiki.util.FL;
import fastily.jwiki.util.FSystem;

/**
 * A simple, pluggable key-store manager.
 * 
 * @author Fastily
 *
 */
public class WGen
{
	/**
	 * The default filenames to save credentials under.
	 */
	private static Path px = Paths.get(".px.txt"), homePX = Paths.get(System.getProperty("user.home") + FSystem.psep + px);

	/**
	 * Cache Wiki objects to avoid multiple logins.
	 */
	private static HashMap<String, Wiki> cache = new HashMap<>();

	/**
	 * Main driver
	 * 
	 * @param args Program arguments, not used
	 * @throws Throwable On I/O error
	 */
	public static void main(String[] args) throws Throwable
	{
		Console c = System.console();
		if (c == null)
			FSystem.errAndExit("You are not running in CLI mode.");

		c.printf("Welcome to WGen!%nThis utility encodes and stores usernames/passwords%n%n");

		HashMap<String, String> ul = new HashMap<>();
		while (true)
		{
			String u = c.readLine("Enter a username: ").trim();
			c.printf("*** Characters hidden for security ***%n");
			char[] p1 = c.readPassword("Enter password for %s: ", u);
			char[] p2 = c.readPassword("Re-enter password for %s: ", u);

			if (Arrays.equals(p1, p2))
				ul.put(u, new String(p1));
			else
				c.printf("ERROR: Entered passwords do not match!%n");

			if (!c.readLine("Continue? (y/N): ").trim().matches("(?i)(y|yes)"))
				break;

			c.printf("%n");
		}

		if (ul.isEmpty())
			FSystem.errAndExit("You did not make any entries.  Doing nothing.");

		StringBuilder sb = new StringBuilder();
		ul.forEach((k, v) -> sb.append(String.format("%s\t%s%n", k, v)));

		byte[] bytes = Base64.getEncoder().encode(sb.toString().getBytes());
		Files.write(px, bytes);
		Files.write(homePX, bytes);

		c.printf("Successfully written out to '%s' and '%s'%n", px, homePX);
	}

	/**
	 * Gets a the password for a user.
	 * 
	 * @param user The username to get a password for.
	 * @return The key associated with {@code user}
	 */
	public static synchronized String pxFor(String user)
	{
		Path f;
		if (Files.isRegularFile(px))
			f = px;
		else if (Files.isRegularFile(homePX))
			f = homePX;
		else
			throw new RuntimeException("ERROR: Could not find px or homePX.  Have you run WGen yet?");

		try
		{
			for (String[] a : FL
					.toAL(Arrays.stream(new String(Base64.getDecoder().decode(Files.readAllBytes(f))).split("\n")).map(s -> s.split("\t"))))
				if (a[0].equals(user))
					return a[1];
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Creates or returns a Wiki using locally stored credentials previously saved with the main method. This method is
	 * cached.
	 * 
	 * @param user The username to use
	 * @param domain The domain (shorthand) to login at.
	 * @return The requested Wiki, or null if we have no such user/password combination.
	 */
	public static synchronized Wiki get(String user, String domain)
	{
		if (cache.containsKey(user))
			return cache.get(user).getWiki(domain);

		try
		{
			Wiki wiki = new Wiki(user, pxFor(user), domain);
			cache.put(user, wiki);
			return wiki;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}
}