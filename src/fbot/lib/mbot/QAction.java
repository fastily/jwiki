package fbot.lib.mbot;

import fbot.lib.core.W;
import fbot.lib.core.aux.Tuple;

/**
 * Abstract class representing Query Actions.
 * @author Fastily
 *
 */
public abstract class QAction extends MAction
{
	/**
	 * Stores the result.  Should be set upon completing doJob(). Leave as null if doJob() failed.
	 */
	protected Tuple<String, ?> result;
	
	/**
	 * Constructor, sets the title we'll be querying.
	 * @param title The title to query.
	 */
	protected QAction(String title)
	{
		super(title);
	}
	
	/**
	 * Gets this QAction's result.
	 * @return The result created by this QAction.
	 */
	public Tuple<String, ?> getResult()
	{
		return result;
	}
	
	/**
	 * Performs this QAction's main job. To be explicitly defined in each subclass.
	 * 
	 * @param wiki The W object to use.
	 * @return True if the action we tried to perform succeeded.
	 */
	public abstract boolean doJob(W wiki);
	
}