package jwiki.mbot;

import java.util.ArrayList;

import jwiki.core.Wiki;

/**
 * Represents a single task to multithread in MBot. 
 * 
 * @author Fastily
 * 
 */
public abstract class WAction
{
	/**
	 * The page to act on
	 */
	public final String title;

	/**
	 * The text to add to the page named by <code>title</code>, where applicable
	 */
	protected String text;

	/**
	 * The log reason/edit summary to use
	 */
	protected String summary;
	
	/**
	 * Constructor for a WAction.
	 * 
	 * @param title The page to act on
	 * @param text The text to add to the page named by <code>title</code>, where applicable. Optional Param: set null to disable.
	 * @param reason The log reason/edit summary to use
	 */
	protected WAction(String title, String text, String summary)
	{
		this.title = title;
		this.text = text;
		this.summary = summary;
	}

	/**
	 * Performs this WAction's main task. *Must* be explicitly defined in each subclass.
	 * 
	 * @param wiki The Wiki object to use.
	 * @return True if the action we tried to perform succeeded.
	 */
	public abstract boolean doJob(Wiki wiki);
	
	/**
	 * Creates a String representation of this WAction.  Useful for debugging.
	 */
	public String toString()
	{
		return String.format("(title: %s | text: %s | reason: %s)", title, text, summary);
	}
	
	/**
	 * Gets the title fields of the passed in MActions and returns them in a list.
	 * 
	 * @param actions The list to get titles from
	 * @return The list of titles
	 */
	public static <T extends WAction> ArrayList<String> convertToString(ArrayList<T> actions)
	{
		ArrayList<String> l = new ArrayList<>();
		for (WAction w : actions)
			l.add(w.title);
		return l;
	}
}