package fbot.lib.util;

import java.io.PrintStream;

import javax.swing.JOptionPane;

/**
 * Error handling methods.
 * 
 * @author Fastily
 *
 */
public class FError
{
	/**
	 * Hide from javadoc
	 */
	private FError()
	{
		
	}
	
	/**
	 * Prints an error message to std out and exits with status 1.
	 * 
	 * @param s Error message to print
	 * @see #errAndExit(String, PrintStream)
	 */
	public static void errAndExit(String s)
	{
		errAndExit(s, System.out);
	}
	
	/**
	 * Prints an error message and exits with status 1.
	 * @param s Error message to print
	 * @param ps Printstream to print to.
	 * @see #errAndExit(String)
	 */
	public static void errAndExit(String s, PrintStream ps)
	{
		ps.println(s);
		System.exit(1);
	}
	
	
	/**
	 * Prints stack trace from specified error and exit.
	 * 
	 * @param e The error object.
	 * @see #errAndExit(Throwable, String)
	 */
	public static void errAndExit(Throwable e)
	{
		errAndExit(e, "");
	}
	
	/**
	 * Prints stack trace from specified error and exit.
	 * 
	 * @param e The error object.
	 * @param s Additional error message.
	 * @see #errAndExit(Throwable)
	 */
	public static void errAndExit(Throwable e, String s)
	{
		e.printStackTrace();
		System.out.println(s);
		System.exit(1);
	}
	
	
	/**
	 * Shows error as Messagebox and exits.
	 * 
	 * @param s The error message
	 * @param err Exit error code.
	 * @see #showErrorAndExit(String)
	 */
	public static void showErrorAndExit(String s, int err)
	{
		JOptionPane.showMessageDialog(null, s);
		System.exit(err);
	}
	
	/**
	 * Shows error as Messagebox and exits.
	 * 
	 * @param s The error message
	 * @see #showErrorAndExit(String, int)
	 */
	public static void showErrorAndExit(String s)
	{
		showErrorAndExit(s, 1);
	}
	
	/**
	 * Print an error message and return a boolean
	 * @param err The error message to print to std err.
	 * @param value The value to return
	 * @return <tt>value</tt>
	 */
	public static boolean printErrorAndReturn(String err, boolean value)
	{
		System.err.println(err);
		return value;
	}
	
}