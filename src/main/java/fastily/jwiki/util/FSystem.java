package fastily.jwiki.util;

import java.nio.file.FileSystems;

import fastily.jwiki.core.ColorLog;

/**
 * System properties and static error handling methods.
 * 
 * @author Fastily
 * 
 */
public final class FSystem
{
	/**
	 * The default line separator for text files by OS. For Windows it's '\r\n' and for Mac/Unix it's just '\n'.
	 */
	public static final String lsep = System.getProperty("line.separator");
	
	/**
	 * The default separator for pathnames by OS. For Windows it is '\' for Mac/Unix it is '/'
	 */
	public static final String psep = FileSystems.getDefault().getSeparator();
	
	/**
	 * The user's home directory.
	 */
	public static final String home = System.getProperty("user.home");
	
	/**
	 * All static methods, no constructors allowed.
	 */
	private FSystem()
	{
		
	}

	/**
	 * Prints an error message to std err and exits with status 1.
	 * 
	 * @param s Error message to print
	 */
	public static void errAndExit(String s)
	{
		if (s != null)
			ColorLog.error(s);
		System.exit(1);
	}

	/**
	 * Prints stack trace from specified error and exit.
	 * 
	 * @param e The error object.
	 * @param s Additional error message. Disable with null.
	 */
	public static void errAndExit(Throwable e, String s)
	{
		e.printStackTrace();
		errAndExit(s);
	}
}