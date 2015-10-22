package jwiki.test;

import java.io.BufferedWriter;
import java.io.Console;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;

import jwiki.util.FError;
import jwiki.util.FSystem;

/**
 * Contains methods to create and read from jwiki-test configuration file. CLI interface is stand-alone
 * 
 * @author Fastily
 *
 */
public class InitConfig
{
	/**
	 * Config file is output to user's home directory with this name
	 */
	private static final String configName = "jwiki-test-config.txt";

	/**
	 * Main Driver for creating config file
	 * 
	 * @param args Ignored, not accepting any.
	 * @throws Throwable Something blew up
	 */
	public static void main(String[] args) throws Throwable
	{

		Console c = System.console();
		if (c == null)
			FError.errAndExit("[ERROR]: You must be in CLI mode");

		c.printf(
				"Welcome to InitConfig for jwiki-tests!%nThis is a dumb utility that will create a config file for the jwiki unit tests.%n"
						+ "(c) Fastily%n%nWARNING: Anything you enter here will be saved as plain-text, so do use a dummy account%n%n");

		while (true)
		{
			String dm = c.readLine("%nEnter the domain to use (e.g. test.wikipedia.org): ").trim();
			String u = c.readLine("Enter a username: ").trim();

			c.printf("!!! Characters hidden for security !!!%n");

			char[] p1 = c.readPassword("Enter password for '%s': ", u);
			char[] p2 = c.readPassword("Confirm/Re-enter password for '%s': ", u);

			if (Arrays.equals(p1, p2))
			{
				try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(FSystem.home, configName), StandardOpenOption.CREATE,
						StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING))
				{
					bw.write(String.format("%s%n%s%n%s", dm, u, new String(p1)));
				}
				catch (Throwable e)
				{
					FError.errAndExit(e, "Something went *really* wrong. Check error.");
				}

				c.printf("Ok looks good.  Program wrote config file to '%s/%s'%nExiting now.%n%n", FSystem.home, configName);
				break;
			}

			c.printf("Entered passwords do not match!%n");

			if (!c.readLine("Continue? (Y/N): ").trim().matches("(?i)(y|yes)"))
				break;
		}
	}

	/**
	 * Reads in data from the config file and loads it into an ArrayList. Order is: domain, username, password.
	 * 
	 * @return The ArrayList described above.
	 */
	protected static ArrayList<String> readConfig()
	{
		ArrayList<String> l = new ArrayList<>();
		try
		{
			l.addAll(Files.readAllLines(Paths.get(FSystem.home, configName)));
		}
		catch (Throwable e)
		{
			FError.errAndExit(e, "Config file does not exist.  Please recreate it by running InitConfig");
		}

		if (l.size() != 3)
			FError.errAndExit("Config file is mangled, please recreate it by running InitConfig.");

		return l;
	}
}