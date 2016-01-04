package jwiki.util;

import jwiki.core.ColorLog;

/**
 * Error handling methods.
 * 
 * @author Fastily
 *
 */
public final class FError
{
	/**
	 * Constructors disallowed
	 */
	private FError()
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

	/**
	 * Print an error message and return a boolean
	 * 
	 * @param s The error message to print to std err.
	 * @param value The value to return
	 * @return <code>value</code>
	 */
	public static boolean printErrAndRet(String s, boolean value)
	{
		ColorLog.error(s);
		return value;
	}
}