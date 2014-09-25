package jwiki.mbot;

import java.util.ArrayList;

import jwiki.core.ColorLog;
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
	 * Set the maximum permissible number of threads.
	 * 
	 * @param num the maximum number of threads to allow simultaneously
	 */
	public synchronized void setNum(int num)
	{
		this.num = num;
	}
	
	/**
	 * Run a job on set of WActions.
	 * 
	 * @param ml The WActions to process
	 * @return A list of WActions that failed.
	 */
	public <T extends WAction> ArrayList<WAction> start(ArrayList<T> ml)
	{
		ThreadManager m = new ThreadManager(ml, wiki, num);
		m.start();
		
		ArrayList<WAction> fails = m.getFails();
		
		if (fails.size() > 0)
		{
			ColorLog.warn(String.format("MBot failed to process (%d): ", fails.size()));
			for (WAction x : fails)
				System.err.println(ColorLog.makeString(x.title, ColorLog.PURPLE));
		}
		else
			ColorLog.fyi("MBot completed the task with no failures");
		
		return fails;
	}
	
	/**
	 * Mass delete pages.
	 * 
	 * @param reason The log summary
	 * @param pages The pages to delete
	 * @return A list of pages we failed to delete.
	 */
	public ArrayList<WAction> massDelete(String reason, String... pages)
	{
		return start(DeleteItem.makeDeleteItems(reason, pages));
	}
	
	/**
	 * Mass undelete pages.
	 * @param reason The log summary
	 * @param pages The pages to undelete
	 * @return A list of pages we did/could not undelete.
	 */
	public ArrayList<WAction> massRestore(String reason, String...pages)
	{
		ArrayList<WAction> wl = new ArrayList<>();
		for(String s : pages)
		{
			wl.add(new WAction(s, null, reason) {
				public boolean doJob(Wiki wiki)
				{
					return wiki.undelete(title, summary);
				}
			});
		}
		return start(wl);
	}
	
	
	/**
	 * Mass edit pages. WARNING: <code>add</code> and <code>replace</code> should not both be null, unless you want the method to do nothing.
	 * 
	 * @param reason The edit summary
	 * @param add The text to add. Optional param: set null to exclude
	 * @param replace The text to replace, in regex form. Optional param: set null to exclude
	 * @param replacement The replacement text. Optional param: depends on <code>replace</code> being != null.
	 * @param pages The pages to edit
	 * @return A list of WActions we failed to process.
	 */
	public ArrayList<WAction> massEdit(String reason, String add, String replace, String replacement, String... pages)
	{
		ArrayList<EditItem> wl = new ArrayList<>();
		for (String s : pages)
			wl.add(new EditItem(s, reason, add, replace, replacement));
		return start(wl);
	}
}