package fastily.jwiki.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
	 * The date formatter prefixing output.
	 */
	private static final DateTimeFormatter df = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm:ss a");

	/**
	 * Indicates whether we are using a terminal that supports color.
	 */
	private static final boolean noColor = System.getProperty("os.name").contains("Windows") || System.getProperty("os.version").startsWith("10.6");

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
	 * Formats a String for ASCII colored escape output if the system you're using supports it. Returns non-escaped ASCII
	 * text otherwise.
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
	 * Logs a message to the standard error output stream
	 * 
	 * @param s The message
	 * @param l The identifier to log the message at (e.g. "INFO", "WARNING")
	 * @param c The color to print the message with. Output will only be colored if this terminal supports it.
	 */
	public static void log(String s, String l, ColorLog c)
	{
		System.err.printf("%s%n%s: %s%n", LocalDateTime.now().format(df), l, makeString(s, c));
	}

	/**
	 * Logs a message for a wiki method.
	 * 
	 * @param wiki The wiki object to use
	 * @param s The String to print
	 * @param l The identifier to log the message at (e.g. "INFO", "WARNING")
	 * @param c The color to print the message with. Output will only be colored if this terminal supports it.
	 */
	protected static void log(Wiki wiki, String s, String l, ColorLog c)
	{
		log(String.format("[%s @ %s]: %s", wiki.whoami(), wiki.conf.domain, s), l, c);
	}

	/**
	 * Output warning message. Text is yellow.
	 * 
	 * @param s The String to print.
	 */
	public static void warn(String s)
	{
		log(s, "WARNING", YELLOW);
	}

	/**
	 * Output warning message for wiki. Text is yellow.
	 * 
	 * @param wiki The wiki object to use
	 * @param s The String to print.
	 */
	protected static void warn(Wiki wiki, String s)
	{
		log(wiki, s, "WARNING", YELLOW);
	}

	/**
	 * Output info message. Text is green.
	 * 
	 * @param s The String to print.
	 */
	public static void info(String s)
	{
		log(s, "INFO", GREEN);
	}

	/**
	 * Output info message for wiki. Text is green.
	 * 
	 * @param wiki The wiki object to use
	 * @param s The String to print.
	 */
	protected static void info(Wiki wiki, String s)
	{
		log(wiki, s, "INFO", GREEN);
	}

	/**
	 * Output error message. Text is red.
	 * 
	 * @param s The String to print.
	 */
	public static void error(String s)
	{
		log(s, "ERROR", RED);
	}

	/**
	 * Output error message for wiki. Text is red.
	 * 
	 * @param wiki The wiki object to use
	 * @param s The String to print.
	 */
	protected static void error(Wiki wiki, String s)
	{
		log(wiki, s, "ERROR", RED);
	}
	
	/**
	 * Output debug message. Text is purple.
	 * 
	 * @param s The String to print.
	 */
	public static void debug(String s)
	{
		log(s, "DEBUG", PURPLE);
	}

	/**
	 * Output debug message for wiki. Text is purple.
	 * 
	 * @param wiki The wiki object to use
	 * @param s The String to print.
	 */
	protected static void debug(Wiki wiki, String s)
	{
		log(wiki, s, "DEBUG", PURPLE);
	}
	
	/**
	 * Output miscellaneous message. Text is blue.
	 * 
	 * @param s The String to print.
	 */
	public static void fyi(String s)
	{
		log(s, "FYI", CYAN);
	}

	
	
	/**
	 * Output miscellaneous message for wiki. Text is blue.
	 * 
	 * @param wiki The wiki object to use
	 * @param s The String to print.
	 */
	protected static void fyi(Wiki wiki, String s)
	{
		log(wiki, s, "FYI", CYAN);
	}
}