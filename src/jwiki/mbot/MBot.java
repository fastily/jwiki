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
	 * @param wl The WAction objects to process
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
		ArrayList<DeleteItem> wl = new ArrayList<DeleteItem>();
		for (String s : pages)
			wl.add(new DeleteItem(s, reason));
		return start(wl.toArray(new DeleteItem[0]));
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
	
	/**
	 * Represents an item to delete. Simple implentation of WAction
	 * 
	 * @author Fastily
	 * 
	 */
	public static class DeleteItem extends WAction
	{
		/**
		 * Constructor, takes title and edit summary
		 * 
		 * @param title The title to delete
		 * @param reason The edit summary to use
		 */
		public DeleteItem(String title, String reason)
		{
			super(title, null, reason);
		}
		
		/**
		 * Attempts to delete the page with the given wiki.
		 * 
		 * @param wiki The wiki object to use
		 * @return True if we were successful.
		 */
		public boolean doJob(Wiki wiki)
		{
			return wiki.delete(title, summary);
		}
	}
	
	/**
	 * Sipmle implementation of an item to edit, using MBot.
	 * 
	 * @author Fastily
	 * 
	 */
	public static class EditItem extends WAction
	{
		/**
		 * The text to add
		 */
		private String add;
		
		/**
		 * The text to replace, as a regex
		 */
		private String replace;
		
		/**
		 * The replacement text. Only used if <tt>replace</tt> is != null
		 */
		private String replacement;
		
		/**
		 * Constructor, sets params
		 * 
		 * @param title The title to use
		 * @param reason The edit summary
		 * @param add The text to add. Optional -- set to null to exclude
		 * @param replace The text to replace, in regex form. Optional -- set to null to exclude
		 * @param replacement The replacement text. Optional -- depends on <tt>replace</tt> being != null.
		 */
		public EditItem(String title, String reason, String add, String replace, String replacement)
		{
			super(title, null, reason);
			this.add = add;
			this.replace = replace;
			this.replacement = replacement;
		}
		
		/**
		 * Does the edit, as specified by WAction.
		 * 
		 * @param wiki The wiki object to use.
		 * @return True if we were successful.
		 */
		public boolean doJob(Wiki wiki)
		{
			text = wiki.getPageText(title);
			if (text == null)
				return false;
			
			if (replace == null && add != null) // simple append text
				return wiki.edit(title, text + add, summary);
			else if (replace != null && replacement != null && add != null) // replace & append
				return wiki.edit(title, text.replaceAll(replace, replacement) + add, summary);
			else if (replace != null && replacement != null) // replace only
				return wiki.edit(title, text.replaceAll(replace, replacement), summary);
			else
				// all null, or replace != null && replacement == null
				Logger.error(String.format("For '%s', why is everything null?", title));
			
			return false;
		}
	}
}