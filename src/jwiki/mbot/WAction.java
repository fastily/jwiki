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
	 * The title we'll be editing.
	 */
	public final String title;

	/**
	 * The text we'll be adding to the page (if applicable)
	 */
	protected String text;

	/**
	 * The reason/edit summary to use
	 */
	protected String summary;
	
	/**
	 * Constructor for a WAction.
	 * 
	 * @param title Title to use
	 * @param text Text to use (specify null if not applicable)
	 * @param reason Reason to use
	 */
	protected WAction(String title, String text, String summary)
	{
		this.title = title;
		this.text = text;
		this.summary = summary;
	}

	/**
	 * Performs this WAction's main job. To be explicitly defined in each subclass.
	 * 
	 * @param wiki The Wiki object to use.
	 * @return True if the action we tried to perform succeeded.
	 */
	public abstract boolean doJob(Wiki wiki);
	
	/**
	 * Creates a String representation of this WAction.  Useful for debugging.
	 * 
	 * @return A debug string.
	 */
	public String toString()
	{
		return String.format("(title: %s | text: %s | reason: %s)", title, text, summary);
	}
	
	/**
	 * Grabs the title fields of the passed in MActions and returns them in an Array.
	 * 
	 * @param actions The actions to grab titles from
	 * @return The list of titles as Strings.
	 */
	public static String[] convertToString(WAction... actions)
	{
		ArrayList<String> l = new ArrayList<String>();
		for (WAction w : actions)
			l.add(w.title);
		
		return l.toArray(new String[0]);
	}
}