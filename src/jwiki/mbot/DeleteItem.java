package jwiki.mbot;

import java.util.ArrayList;

import jwiki.core.Wiki;

/**
 * Simple implementation of WAction representing a deletion action.
 * @author Fastily
 *
 */
public class DeleteItem extends WAction
{
	/**
	 * Constructor, takes title and deletion summary
	 * @param title The title to use
	 * @param summary The log summary to use
	 */
	public DeleteItem(String title, String summary)
	{
		super(title, null, summary);
	}

	/**
	 * Performs the deletion. Called by the enclosing MBot's ThreadManager.
	 * 
	 * @param wiki The wiki object to use
	 * @return True if we were successful.
	 */
	public boolean doJob(Wiki wiki)
	{
		return wiki.delete(title, summary);
	}
	
	/**
	 * Mass create DeleteItems.
	 * @param reason The reason to use when deleting this
	 * @param titles The titles to delete 
	 * @return A list of DeleteItems as specified.
	 */
	public static ArrayList<DeleteItem> makeDeleteItems(String reason, String... titles)
	{
		ArrayList<DeleteItem> l = new ArrayList<DeleteItem>();
		for(String s : titles)
			l.add(new DeleteItem(s, reason));
		return l;
	}
}