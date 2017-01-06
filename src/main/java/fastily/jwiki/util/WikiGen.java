package fastily.jwiki.util;

import java.io.Console;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.Gson;

import fastily.jwiki.core.ColorLog;
import fastily.jwiki.core.Wiki;

/**
 * A simple console based-credential manager.
 * 
 * @author Fastily
 *
 */
public final class WikiGen
{
	/**
	 * The default file names to save credentials under.
	 */
	private static final String pf = ".pfx.txt", px = ".pxx.txt";

	/**
	 * An additional location to save credentials under.
	 */
	private static final String homefmt = FSystem.home + FSystem.psep;

	/**
	 * The default WikiGen object created at run time.
	 */
	public static final WikiGen wg = initWG();

	/**
	 * The master user/pass list.
	 */
	private HashMap<String, String> master = new HashMap<>();

	/**
	 * Cache saving Wiki objects so we don't do multiple log-ins by accident.
	 */
	private HashMap<String, Wiki> cache = new HashMap<>();

	/**
	 * Constructor, decodes encrypted passwords and makes them available to the program.
	 * 
	 * @throws Throwable If something went very wrong.
	 */
	private WikiGen() throws Throwable
	{
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(Files.readAllBytes(findConfig(pf)), "AES"));
		
		GSONP.jp.parse(new String(c.doFinal(Files.readAllBytes(findConfig(px))), "UTF-8")).getAsJsonObject()
		.entrySet().stream().forEach(e -> master.put(e.getKey(), e.getValue().getAsString()));
	}

	/**
	 * Main driver - run this to start interactive password manager.
	 * 
	 * @param args Prog args
	 * @throws Throwable If something went very wrong.
	 */
	public static void main(String[] args) throws Throwable
	{
		Console c = System.console();
		if (c == null)
			FSystem.errAndExit("You need to be running in CLI mode");

		c.printf("Welcome to WikiGen!%nThis utility will encrypt & store your usernames/passwords%n(c) 2017 Fastily%n%n");

		// let user enter user & pw combos
		HashMap<String, String> ul = new HashMap<>();
		while (true)
		{
			String u = c.readLine("Enter a username: ").trim();
			c.printf("!!! Characters hidden for security !!! %n");
			char[] p1 = c.readPassword("Enter password for %s: ", u);
			char[] p2 = c.readPassword("Confirm/Re-enter password for %s: ", u);

			if (Arrays.equals(p1, p2))
				ul.put(u, new String(p1));
			else
				c.printf("Entered passwords do not match!%n");

			if(!c.readLine("Continue? (y/N): ").trim().toLowerCase().matches("(?i)(y|yes)"))
				break;
			
			c.printf("%n");
		}

		if (ul.isEmpty())
			FSystem.errAndExit("You didn't enter any user/pass.  Program will exit.");

		// Encrypt and dump to file
		KeyGenerator kg = KeyGenerator.getInstance("AES");
		kg.init(128);
		SecretKey sk = kg.generateKey();
		writeFiles(pf, sk.getEncoded());

		Cipher cx = Cipher.getInstance("AES");
		cx.init(Cipher.ENCRYPT_MODE, sk);
		writeFiles(px, cx.doFinal(new Gson().toJson(ul, GSONP.strMapT).getBytes("UTF-8")));

		c.printf("Successfully written out to '%s', '%s', '%s%s' and '%s%s'%n", pf, px, homefmt, pf, homefmt, px);
	}

	/**
	 * Dump bytes to a file
	 * 
	 * @param f The location to write to
	 * @param bytes The bytes to dump
	 * @throws Throwable i/o error
	 */
	private static void writeFiles(String f, byte[] bytes) throws Throwable
	{
		Files.write(Paths.get(f), bytes);
		Files.write(Paths.get(homefmt + f), bytes);
	}

	/**
	 * Helper used to locate a pf or px file
	 * 
	 * @param baseloc Set to pf or px depending on which one we're looking for
	 * @return A path representing the first instance of the requested file. Null if we couldn't find any instance(s) of
	 *         the requested file.
	 */
	private static Path findConfig(String baseloc)
	{
		Path p;
		if (Files.exists((p = Paths.get(baseloc))))
			return p;
		else if (Files.exists(p = Paths.get(homefmt + baseloc)))
			return p;
		return null;
	}

	/**
	 * Try to create a WikiGen object. If you have not run WikiGen yet, then <code>wg</code> will be set to null.
	 * 
	 * @return A default WikiGen object.
	 */
	private static WikiGen initWG()
	{
		try
		{
			return new WikiGen();
		}
		catch (Throwable e)
		{
			ColorLog.warn("INFO: WikiGen must be run before wg can be used.");
			return null;
		}
	}

	/**
	 * Creates or returns a wiki object using our locally stored credentials. This method is cached.
	 * 
	 * @param user The username to use
	 * @param domain The domain (shorthand) to login at.
	 * @return The requested wiki object, or null if we have no such user-password combo
	 */
	public synchronized Wiki get(String user, String domain)
	{
		if (cache.containsKey(user))
			return cache.get(user).getWiki(domain);

		try
		{
			Wiki wiki = new Wiki(user, master.get(user), domain);
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