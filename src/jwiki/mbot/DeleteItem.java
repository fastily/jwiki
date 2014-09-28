package jwiki.mbot;

import java.util.ArrayList;

import jwiki.core.Wiki;

/**
 * Simple MBot deletion function.
 * 
 * @author Fastily
 *
 */
public class DeleteItem extends WAction
{
	/**
	 * Constructor, takes title and deletion summary
	 * 
	 * @param title The title to use
	 * @param summary The log summary to use
	 */
	public DeleteItem(String title, String summary)
	{
		super(title, null, summary);
	}

	/**
	 * Performs the deletion.
	 */
	public boolean doJob(Wiki wiki)
	{
		return wiki.delete(title, summary);
	}

	/**
	 * Mass create DeleteItems from titles & deletion reason.
	 * 
	 * @param reason The log summary to enter
	 * @param titles Titles to delete
	 * @return A list of DeleteItems
	 */
	public static ArrayList<DeleteItem> makeDeleteItems(String reason, ArrayList<String> titles)
	{
		ArrayList<DeleteItem> l = new ArrayList<>();
		for (String s : titles)
			l.add(new DeleteItem(s, reason));
		return l;
	}

}
