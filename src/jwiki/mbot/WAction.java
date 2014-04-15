package jwiki.mbot;

import jwiki.core.Wiki;

/**
 * Basis for all task wrappers in MBot.
 * 
 * @author Fastily
 * 
 */
public abstract class WAction extends MAction
{
	/**
	 * The title we'll be editing.
	 */
	protected String title;

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
		super(title);
		this.text = text;
		this.summary = summary;
	}

	/**
	 * Performs this WAction's main job. To be explicitly defined in each subclass.
	 * 
	 * @param wiki The W object to use.
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
}