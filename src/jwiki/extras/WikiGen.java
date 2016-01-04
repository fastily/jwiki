package jwiki.extras;

import java.io.Console;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;

import jwiki.core.ColorLog;
import jwiki.core.Wiki;
import jwiki.util.FError;
import jwiki.util.FSystem;

/**
 * Simple interactive password manager for ctools. Allows for preferential ranking of accounts used (e.g. you have
 * multiple bot accounts and want to set rankings for them)
 * 
 * @author Fastily
 *
 */
public class WikiGen
{
	/**
	 * The default file names to save credentials under.
	 */
	private static final String pf = ".pf.txt", px = ".px.txt";

	/**
	 * An additional location to save credentials under.
	 */
	private static final String homefmt = FSystem.home + FSystem.psep;

	/**
	 * Used to keep track of ranks we've assigned to usernames during the initialization process.
	 */
	private static int rankcount = 1;

	/**
	 * The default WikiGen object created at run time.
	 */
	public static final WikiGen wg = initWG();

	/**
	 * The master user/pass list.
	 */
	private HashMap<String, String> master = new HashMap<>();

	/**
	 * Credentials for primary, secondary, and tertiary accounts. These will be set if the user requested them.
	 */
	private HashMap<Integer, String> pwl = new HashMap<>();

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
		JSONObject jo = new JSONObject(new String(c.doFinal(Files.readAllBytes(findConfig(px))), "UTF-8"));

		for (String s : JSONObject.getNames(jo))
		{
			JSONObject entry = jo.getJSONObject(s);
			master.put(s, entry.getString("pass"));
			if (entry.has("rank"))
				pwl.put(entry.getInt("rank"), s);
		}
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
			FError.errAndExit("You need to be running in CLI mode");

		c.printf("Welcome to FLogin!%nThis utility will encrypt & store your usernames/passwords%n(c) 2015 Fastily%n%n");

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

			if (!userAnsweredYes(c, "Continue? (y/N): "))
				break;
		}

		if (ul.isEmpty())
			FError.errAndExit("You didn't enter any user/pass.  Program will exit.");

		// Generating our JSONObject
		JSONObject jo = new JSONObject();
		for (Map.Entry<String, String> e : ul.entrySet())
		{
			JSONObject internal = new JSONObject();
			internal.put("pass", e.getValue());
			jo.put(e.getKey(), internal);
		}

		// Do setup for ranking if applicable
		if (ul.size() <= 26 && userAnsweredYes(c, "%nSetup optional account preference? (y/N): "))
		{
			ArrayList<String> l = new ArrayList<>(ul.keySet());
			while (!l.isEmpty())
			{
				doUserRank(c, l, jo);
				if (!userAnsweredYes(c, "Continue? (y/N): "))
					break;
			}
		}

		// Encrypt and dump to file
		KeyGenerator kg = KeyGenerator.getInstance("AES");
		kg.init(128);
		SecretKey sk = kg.generateKey();
		writeFiles(pf, sk.getEncoded());

		Cipher cx = Cipher.getInstance("AES");
		cx.init(Cipher.ENCRYPT_MODE, sk);
		writeFiles(px, cx.doFinal(jo.toString().getBytes("UTF-8")));

		c.printf("Successfully written out to '%s', '%s', '%s%s' and '%s%s'%n", pf, px, homefmt, pf, homefmt, px);
	}

	/**
	 * Creates interface to allow user to rank username by preference
	 * 
	 * @param c Our console object
	 * @param l A list of usernames which have not yet been ranked. WARNING: method will remove a username from the list
	 *           when it is selected by the user
	 * @param jo The backing JSONObject which will be modified according to the user's selections.
	 */
	private static void doUserRank(Console c, ArrayList<String> l, JSONObject jo)
	{
		c.printf("Please select the account to rank %d%n", rankcount);
		for (int i = 0; i < l.size(); i++)
			c.printf("%c) %s%n", 97 + i, l.get(i));

		String selection = c.readLine("%nSelection (character): ").trim().toLowerCase();

		int index;
		if (selection.isEmpty())
			c.printf("You typed an empty String!  Doing nothing.%n");
		else if (selection.matches("[a-z]") && (index = (int) selection.charAt(0) - 97) < l.size())
			jo.getJSONObject(l.remove(index)).put("rank", rankcount++);
	}

	/**
	 * Prompt user and see if they responded yes.
	 * 
	 * @param c The console to use
	 * @param message The prompt
	 * @return True iff the user answered 'y'
	 */
	private static boolean userAnsweredYes(Console c, String message)
	{
		return c.readLine(message).trim().toLowerCase().matches("(?i)(y|yes)");
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
	 * @return The requtested wiki obejct, or null if we have no such user-password combo
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

	/**
	 * Creates or returns a wiki object using our locally stored credentials. This method is cached. Auto-sets domain to
	 * 'commons.wikimedia.org'
	 * 
	 * @param user The username to use
	 * @return The requtested wiki obejct, or null if we have no such user-password combo
	 */
	public synchronized Wiki get(String user)
	{
		return get(user, "commons.wikimedia.org");
	}

	/**
	 * Creates or returns a wiki object pointed to by the specified rank. This method is cached. Auto-sets domain to
	 * 'commons.wikimedia.org'
	 * 
	 * @param rank The preferred wiki object at this rank
	 * @return The requested wiki obejct, or null if we have no such user-password combo
	 */
	public synchronized Wiki get(int rank)
	{
		String user = pwl.get(rank);
		return user == null ? null : get(user);
	}

	/**
	 * Creates or returns a wiki object pointed to by the specified rank. This method is cached.
	 * 
	 * @param rank The preferred wiki object at this rank
	 * @param domain The domain (shorthand) to login at.
	 * @return The requtested wiki obejct, or null if we have no such user-password combo
	 */
	public synchronized Wiki get(int rank, String domain)
	{
		String user = pwl.get(rank);
		return user == null ? null : get(user, domain);
	}
}