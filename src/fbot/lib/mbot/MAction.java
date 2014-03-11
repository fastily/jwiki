package fbot.lib.mbot;

import java.util.ArrayList;

import fbot.lib.core.Wiki;

/**
 * Abstract superclass for all items used by MBot.
 * 
 * @author Fastily
 * 
 */
public abstract class MAction
{
	/**
	 * The title we'll be editing.
	 */
	protected String title;
	
	/**
	 * Indicates whether we succeeded or not. Could ideally be set in doJob()
	 */
	protected boolean succeeded = false;
	
	/**
	 * Constructor, allows us to set the name of the item we're working with.
	 * 
	 * @param title Set the title of the item we're working with.
	 */
	protected MAction(String title)
	{
		this.title = title;
	}
	
	/**
	 * Fetches the title of this MAction.
	 * 
	 * @return The title field.
	 */
	public String getTitle()
	{
		return title;
	}
	
	/**
	 * Indicates if we were successful.
	 * 
	 * @return True if we ran and the operation succeeded.
	 */
	public boolean didSucceed()
	{
		return succeeded;
	}
	
	/**
	 * Gets a String representation of this object. Useful for debugging.
	 * 
	 * @return A String representation of this object.
	 */
	public String toString()
	{
		return String.format("(title: %s | succeeded: %b)", title, succeeded);
	}
	
	/**
	 * Grabs the title fields of the passed in MActions and returns them in an Array.
	 * 
	 * @param actions The actions to grab titles from
	 * @return The list of titles as Strings.
	 */
	public static String[] convertToString(MAction... actions)
	{
		ArrayList<String> l = new ArrayList<String>();
		for (MAction m : actions)
			l.add(m.title);
		
		return l.toArray(new String[0]);
	}
	
	/**
	 * Performs this MAction's main job. To be explicitly defined in each subclass.
	 * 
	 * @param wiki The W object to use.
	 * @return True if the action we tried to perform succeeded.
	 */
	public abstract boolean doJob(Wiki wiki);
}