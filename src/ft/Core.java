package ft;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import jwiki.commons.Commons;
import jwiki.core.Wiki;
import jwiki.util.FCLI;
import jwiki.util.WikiGen;

/**
 * The initializer utilities that should be shared amongst all bots in this package.
 * 
 * @author Fastily
 * 
 */
public class Core
{
	/**
	 * Use this to perform admin tasks
	 */
	protected static Wiki admin = null;
	
	/**
	 * Used to perform normal tasks
	 */
	protected static Wiki user = null;
	
	/**
	 * Use this to perform mass Commons actions
	 */
	protected static Commons com = null;
	
	/**
	 * Reads command line args and sets up Wiki objects accordingly.
	 * 
	 * @param args Prog args
	 * @param ol The Option list
	 * @param hstring If '-help' is specified in the command line, print this and exit.
	 * @return A CommandLine object.
	 */
	protected static CommandLine init(String[] args, Options ol, String hstring)
	{
		ol.addOption("help", false, "Print this help message and exit");
		ol.addOption(FCLI.makeArgOption("user", "Set the user to login as", "username"));
		ol.addOption(FCLI.makeArgOption("admin", "Set the admin account to use", "username"));
		
		CommandLine l = FCLI.gnuParse(ol, args, hstring);
		
		boolean hasUser = l.hasOption("user");
		boolean hasAdmin = l.hasOption("admin");
		
		if (hasUser && hasAdmin)
		{
			admin = WikiGen.generate(l.getOptionValue("admin"));
			user = WikiGen.generate(l.getOptionValue("user"));
		}
		else if (hasUser ^ hasAdmin)
		{
			if (hasUser)
				admin = user = WikiGen.generate(l.getOptionValue("user"));
			else
				user = admin = WikiGen.generate(l.getOptionValue("admin"));
		}
		else
		{
			admin = WikiGen.generate("Fastily");
			user = WikiGen.generate("FastilyClone");
		}
		com = new Commons(user, admin);
		return l;
	}
}