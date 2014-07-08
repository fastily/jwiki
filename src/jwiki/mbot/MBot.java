package jwiki.mbot;

import java.util.ArrayList;

import jwiki.core.Logger;
import jwiki.core.Wiki;

/**
 * Main entry point for the MBot library. This library was built with the intent of facilitating the use and design of
 * multi-threaded bots derived from the jwiki library.
 * 
 * @author Fastily
 * 
 */
public class MBot
{
	/**
	 * The wiki object we'll be using for queries/actions
	 */
	private Wiki wiki;
	
	/**
	 * Maximum number of threads to instantiate
	 */
	private int num;
	
	/**
	 * Constructor, auto-initializes number of threads to 20 with specified Wiki.
	 * 
	 * @param wiki The wiki objec to use.
	 */
	public MBot(Wiki wiki)
	{
		this(wiki, 20);
	}
	
	/**
	 * Constructor, wraps the wiki object to use and takes a max number of threads to instantiate.
	 * 
	 * @param wiki The wiki object to use
	 * @param num The maximum number of threads to instantiate.
	 */
	public MBot(Wiki wiki, int num)
	{
		this.wiki = wiki;
		this.num = num;
	}
	
	/**
	 * Sets the maximum permissible number of threads.
	 * 
	 * @param num the maximum number of threads
	 */
	public synchronized void setNum(int num)
	{
		this.num = num;
	}
	
	/**
	 * Starts the execution of this object.
	 * 
	 * @param ml The WAction objects to process
	 * @return A list of titles we failed to process.
	 */
	public WAction[] start(WAction[] ml)
	{
		ThreadManager m = new ThreadManager(ml, wiki, num);
		m.start();
		
		WAction[] fails = m.getFails();
		
		if (fails.length > 0)
		{
			Logger.warn(String.format("MBot failed to process (%d): ", fails.length));
			for (WAction x : fails)
				Logger.log(x.title, "PURPLE");
		}
		else
			Logger.fyi("MBot completed the task with 0 failures");
		
		return fails;
	}
	
	/**
	 * Mass delete pages.
	 * 
	 * @param reason The edit summary to use
	 * @param pages The pages to delete
	 * @return A list of pages we failed to delete.
	 */
	public WAction[] massDelete(String reason, String... pages)
	{
		return start(DeleteItem.makeDeleteItems(reason, pages).toArray(new WAction[0]));
	}
	
	/**
	 * Performs a mass undeletion
	 * @param reason The reason to use
	 * @param pages The pages to undelete
	 * @return A list of pages we did/could not undelete.
	 */
	public WAction[] massRestore(String reason, String...pages)
	{
		ArrayList<WAction> wl = new ArrayList<WAction>();
		for(String s : pages)
		{
			wl.add(new WAction(s, null, reason) {
				public boolean doJob(Wiki wiki)
				{
					return wiki.undelete(title, summary);
				}
			});
		}
		return start(wl.toArray(new WAction[0]));
	}
	
	
	/**
	 * Mass edit pages.
	 * 
	 * @param reason The edit summary
	 * @param add The text to add. Optional -- set to null to exclude
	 * @param replace The text to replace, in regex form. Optional -- set to null to exclude
	 * @param replacement The replacement text. Optional -- depends on <tt>replace</tt> being != null.
	 * @param pages The titles to edit
	 * @return A list of WActions we failed to process.
	 */
	public WAction[] massEdit(String reason, String add, String replace, String replacement, String... pages)
	{
		ArrayList<EditItem> wl = new ArrayList<EditItem>();
		for (String s : pages)
			wl.add(new EditItem(s, reason, add, replace, replacement));
		return start(wl.toArray(new EditItem[0]));
	}
}