package fastily.jwiki.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Colors a String and logs it to console. Your terminal must support ASCII escapes for this to work, otherwise the text
 * will not be colored.
 * 
 * @author Fastily
 *
 */
class ColorLog
{
	/**
	 * Creating logger
	 */
	private static Logger logger = LoggerFactory.getLogger(ColorLog.class);
	/**
	 * Properties system, IF DEFINED, logging on standard error; otherwise logging
	 * with Log4j
	 */
	private static final String COLORLOGPROP = "ColorLogOldMode";

	/**
	 * Flag to use Log4j
	 */
	private boolean useLog4j;
	
	/**
	 * The date formatter prefixing output.
	 */
	private static final DateTimeFormatter df = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm:ss a");
	
	/**
	 * Flag indicating whether logging with this object is allowed.
	 */
	protected boolean enabled;

	/**
	 * Constructor, creates a new ColorLog.
	 * 
	 * @param enableLogging Set true to allow this ColorLog to print log output.
	 */
	protected ColorLog(boolean enableLogging)
	{
		enabled = enableLogging;
		// If System property is defined, logging output will be used
		if (System.getProperty(COLORLOGPROP) != null) {
			useLog4j = false;
		} else { // True to use Log4j logging
			useLog4j = true;
		}
	}

	/**
	 * Logs a message for a wiki method.
	 * 
	 * @param wiki The wiki object to use
	 * @param message The String to print
	 * @param logLevel The identifier to log the message at (e.g. "INFO", "WARNING")
	 * @param color The color to print the message with. Output will only be colored if this terminal supports it.
	 */
	private void log(Wiki wiki, String message, String logLevel, CC color)
	{
		if (enabled)
			System.err.printf("%s%n%s: \u001B[3%dm%s: %s\u001B[0m%n", LocalDateTime.now().format(df), logLevel, color.v, wiki, message);
	}

	/**
	 * Output warning message for wiki. Text is yellow.
	 * 
	 * @param wiki The wiki object to use
	 * @param s The String to print.
	 * @return If logging is enabled can returns warning log using Log4j, otherwise
	 *         returns logging output
	 */
	protected void warn(Wiki wiki, String s)
	{
		if (useLog4j)
			logger.warn(wiki + " - " + s);
		else
			log(wiki, s, "WARNING", CC.YELLOW);
	}

	/**
	 * Output info message for wiki. Text is green.
	 * 
	 * @param wiki The wiki object to use
	 * @param s The String to print.
	 * @return if logging is enabled can returns info log using Log4j, otherwise
	 *         returns logging output
	 */
	protected void info(Wiki wiki, String s)
	{
		if (useLog4j)
			logger.info(wiki + " - " + s);
		else
			log(wiki, s, "INFO", CC.GREEN);
	}

	/**
	 * Output error message for wiki. Text is red.
	 * 
	 * @param wiki The wiki object to use
	 * @param s The String to print.
	 * @return If logging is enabled can returns error log using Log4j, otherwise
	 *         returns logging output
	 */
	protected void error(Wiki wiki, String s)
	{
		if (useLog4j)
			logger.error(wiki + " - " + s);
		else
			log(wiki, s, "ERROR", CC.RED);
	}

	/**
	 * Output debug message for wiki. Text is purple.
	 * 
	 * @param wiki The wiki object to use
	 * @param s The String to print.
	 * @return If logging is enabled can returns debug log using Log4j, otherwise
	 *         returns logging output
	 */
	protected void debug(Wiki wiki, String s)
	{
		if (useLog4j)
			logger.debug(wiki + " - " + s);
		else
			log(wiki, s, "DEBUG", CC.PURPLE);

	}

	/**
	 * Output miscellaneous message for wiki. Text is blue.
	 * 
	 * @param wiki The wiki object to use
	 * @param s The String to print.
	 * @return If logging is enabled can returns fyi log using Log4j, otherwise
	 *         returns logging output
	 */
	protected void fyi(Wiki wiki, String s)
	{
		if (useLog4j)
			logger.trace(wiki + " - " + s);
		else
			log(wiki, s, "FYI", CC.CYAN);
	}

	/**
	 * Represents ASCII colors.
	 * 
	 * @author Fastily
	 *
	 */
	private static enum CC
	{
		/**
		 * A font color, black, which can be applied to a String if your terminal supports it.
		 */
		BLACK(0),

		/**
		 * A font color, red, which can be applied to a String if your terminal supports it.
		 */
		RED(1),

		/**
		 * A font color, green, which can be applied to a String if your terminal supports it.
		 */
		GREEN(2),

		/**
		 * A font color, yellow, which can be applied to a String if your terminal supports it.
		 */
		YELLOW(3),

		/**
		 * A font color, blue, which can be applied to a String if your terminal supports it.
		 */
		BLUE(4),

		/**
		 * A font color, purple, which can be applied to a String if your terminal supports it.
		 */
		PURPLE(5),

		/**
		 * A font color, cyan, which can be applied to a String if your terminal supports it.
		 */
		CYAN(6),

		/**
		 * A font color, white, which can be applied to a String if your terminal supports it.
		 */
		WHITE(7);

		/**
		 * The ascii color value.
		 */
		private int v;

		/**
		 * Constructor, creates a new CC.
		 * 
		 * @param v The color code to use.
		 */
		private CC(int v)
		{
			this.v = v;
		}
	}
}