package jwiki.core;

import java.util.logging.Level;
import java.util.logging.Logger;

import jwiki.util.FSystem;

/**
 * Colors a String and logs it to console. Your terminal must support ASCII escapes for this to work, otherwise the text
 * will not be colored.
 * 
 * @author Fastily
 *
 */
public enum ColorLog
{
	/**
	 * A font color, black, which can be applied to a String if your terminal supports it.
	 */
	BLACK(30),

	/**
	 * A font color, red, which can be applied to a String if your terminal supports it.
	 */
	RED(31),

	/**
	 * A font color, green, which can be applied to a String if your terminal supports it.
	 */
	GREEN(32),

	/**
	 * A font color, yellow, which can be applied to a String if your terminal supports it.
	 */
	YELLOW(33),

	/**
	 * A font color, blue, which can be applied to a String if your terminal supports it.
	 */
	BLUE(34),

	/**
	 * A font color, purple, which can be applied to a String if your terminal supports it.
	 */
	PURPLE(35),

	/**
	 * A font color, cyan, which can be applied to a String if your terminal supports it.
	 */
	CYAN(36),

	/**
	 * A font color, white, which can be applied to a String if your terminal supports it.
	 */
	WHITE(37);

	/**
	 * Indicates whether we are using a terminal that supports color.
	 */
	private static final boolean noColor = FSystem.isWindows || System.getProperty("os.version").startsWith("10.6");

	/**
	 * The logger we'll be using to log events.
	 */
	private static final Logger log = Logger.getLogger("jwiki");

	/**
	 * The value of the enum
	 */
	private int v;

	/**
	 * Constructor, takes an ASCII color value.
	 */
	private ColorLog(int v)
	{
		this.v = v;
	}

	/**
	 * Formats a String for ASCII colored escape output if the system you're using supports it.
	 * 
	 * @param text The text to color
	 * @param c The color to use
	 * @return The colored string, or the same string if this system does not support ASCII color escapes.
	 */
	public static String makeString(String text, ColorLog c)
	{
		return noColor ? text : String.format("\u001B[%dm%s\u001B[0m", c.v, text);
	}

	/**
	 * Logs a message to the std error stream
	 * 
	 * @param s The message
	 * @param l The level to log the message at
	 * @param c The color to print the message with. Output will only be colored if this terminal supports it.
	 */
	public static void log(String s, Level l, ColorLog c)
	{
		log.log(l, makeString(s, c));
	}

	/**
	 * Logs a message for a wiki method.
	 * 
	 * @param wiki The wiki object to use
	 * @param s The String to print
	 * @param l The level to log the message at
	 * @param c The color to print the message with. Output will only be colored if this terminal supports it.
	 */
	protected static void log(Wiki wiki, String s, Level l, ColorLog c)
	{
		log(String.format("[%s @ %s]: %s", wiki.upx.x, wiki.domain, s), l, c);
	}

	/**
	 * Output warning message. Text is yellow.
	 * 
	 * @param s The String to print.
	 */
	public static void warn(String s)
	{
		log(s, Level.WARNING, YELLOW);
	}

	/**
	 * Output warning message for wiki. Text is yellow.
	 * 
	 * @param wiki The wiki object to use
	 * @param s The String to print.
	 */
	protected static void warn(Wiki wiki, String s)
	{
		log(wiki, s, Level.WARNING, YELLOW);
	}

	/**
	 * Output info message. Text is green.
	 * 
	 * @param s The String to print.
	 */
	public static void info(String s)
	{
		log(s, Level.INFO, GREEN);
	}

	/**
	 * Output info message for wiki. Text is green.
	 * 
	 * @param wiki The wiki object to use
	 * @param s The String to print.
	 */
	protected static void info(Wiki wiki, String s)
	{
		log(wiki, s, Level.INFO, GREEN);
	}

	/**
	 * Output error message. Text is red.
	 * 
	 * @param s The String to print.
	 */
	public static void error(String s)
	{
		log(s, Level.SEVERE, RED);
	}

	/**
	 * Output error message for wiki. Text is red.
	 * 
	 * @param wiki The wiki object to use
	 * @param s The String to print.
	 */
	protected static void error(Wiki wiki, String s)
	{
		log(wiki, s, Level.SEVERE, RED);
	}

	/**
	 * Output miscellaneous message. Text is blue.
	 * 
	 * @param s The String to print.
	 */
	public static void fyi(String s)
	{
		log(s, Level.INFO, CYAN);
	}

	/**
	 * Output miscellaneous message for wiki. Text is blue.
	 * 
	 * @param wiki The wiki object to use
	 * @param s The String to print.
	 */
	protected static void fyi(Wiki wiki, String s)
	{
		log(wiki, s, Level.INFO, CYAN);
	}
}