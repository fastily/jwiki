package jwiki.core;

import java.io.PrintStream;
import java.util.HashMap;

import jwiki.util.FSystem;

/**
 * Makes printing to std out in color possible in unix terminals. No changes are made to output if we're using a windows
 * box, so this is safe to use cross-platform. We're using ASCII escapes to implement color. Valid colors for log() are
 * <tt>{ "BLACK", "RED", "GREEN", "YELLOW", "BLUE", "PURPLE", "CYAN", "WHITE" }</tt>. Contains several default methods
 * for outputting text.
 * 
 * @author Fastily
 * 
 */
public class Logger
{
	/**
	 * If this is true, we should not be modifying text to be displayed in color.
	 */
	private static boolean noColor = FSystem.isWindows || System.getProperty("os.version").startsWith("10.6");
	
	/**
	 * Our color library
	 */
	private static final HashMap<String, String> colors = init();
	
	/**
	 * The printstream that we'll be using. This can be set accordingly.
	 */
	private static PrintStream ps = System.out;
	
	/**
	 * Hiding from javadoc
	 */
	private Logger()
	{
		
	}
	
	/**
	 * Initializes the Color Library.
	 * 
	 * @return Our Color library.
	 */
	private static HashMap<String, String> init()
	{
		HashMap<String, String> m = new HashMap<String, String>();
		
		String[] tl = new String[] { "BLACK", "RED", "GREEN", "YELLOW", "BLUE", "PURPLE", "CYAN", "WHITE" };
		for (int i = 0; i < tl.length; i++)
			m.put(tl[i], String.format("\u001B[3%dm%%s\u001B[0m", i));
		
		return m;
	}
	
	/**
	 * Sets the PrintStream we're using. Default = System.out.
	 * 
	 * @param out The PrintStream to use to output to.
	 */
	public static synchronized void setPrintStream(PrintStream out)
	{
		ps = out;
	}
	
	/**
	 * Prints a String to std out in the specified color. Only works in unix terminals, so printing in color will be
	 * disabled if we're using Windows. Doesn't work in Eclipse with default settings. Oh well :x
	 * 
	 * @param s The String to print.
	 * @param code The color to print in. Options are black, red, green, yellow, blue, purple, cyan, or white.
	 *            Capitalization irrelevant.
	 * 
	 */
	public static void log(String s, String code)
	{
		if (noColor)
			ps.println(s);
		else if (colors.containsKey(code.toUpperCase()))
			ps.println(String.format(colors.get(code.toUpperCase()), s));
		else
			ps.println(s);
	}
	
	/**
	 * Prints a String to std out in the specified color, as a logging function for a wiki. Only prints in color for
	 * unix terminals.
	 * 
	 * @param wiki The wiki object to use.
	 * @param s The String to print.
	 * @param code The color code.
	 * @see #log(String, String)
	 */
	public static void log(Wiki wiki, String s, String code)
	{
		if (wiki != null)
			log(String.format("[%s @ %s]: %s", wiki.upx.x, wiki.domain, s), code);
		else
			log(s, code);
	}
	
	/**
	 * Output info message. Text is green.
	 * 
	 * @param wiki The wiki object to log from
	 * @param s The String to print.
	 */
	public static void info(Wiki wiki, String s)
	{
		log(wiki, s, "GREEN");
	}
	
	/**
	 * Note that an event occurred. Output text is Green.
	 * 
	 * @param s The String to print
	 */
	public static void info(String s)
	{
		log(s, "GREEN");
	}
	
	/**
	 * Output warning message. Text is yellow.
	 * 
	 * @param wiki The wiki object to log from
	 * @param s String to print
	 */
	public static void warn(Wiki wiki, String s)
	{
		log(wiki, s, "YELLOW");
	}
	
	/**
	 * Output warning. Text is Yellow.
	 * 
	 * @param s The String to print
	 */
	public static void warn(String s)
	{
		log(s, "YELLOW");
	}
	
	/**
	 * Output error message. Text is Red.
	 * 
	 * @param wiki The wiki object to log from
	 * @param s String to print
	 */
	public static void error(Wiki wiki, String s)
	{
		log(wiki, s, "RED");
	}
	
	/**
	 * Output error message. Text is Red.
	 * 
	 * @param s The String to print
	 */
	public static void error(String s)
	{
		log(s, "RED");
	}
	
	/**
	 * For miscellaneous information. Text is Cyan.
	 * 
	 * @param wiki The wiki object to log from
	 * @param s The String to print
	 */
	public static void fyi(Wiki wiki, String s)
	{
		log(wiki, s, "CYAN");
	}
	
	/**
	 * For miscellaneous information. Text is Cyan.
	 * 
	 * @param s The String to print.
	 */
	public static void fyi(String s)
	{
		log(s, "CYAN");
	}
}