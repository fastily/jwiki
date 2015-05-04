package jwiki.core;

import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * A customizable, multi-threaded bot framework. Create a class overriding MBot.Task, and implement the
 * <code>doJob()</code> method.
 * 
 * @author Fastily
 */
public class MBot
{
	/**
	 * The resident wiki object
	 */
	private final Wiki wiki;

	/**
	 * Constructor, takes a wiki object
	 * 
	 * @param wiki The wiki object to use
	 */
	protected MBot(Wiki wiki)
	{
		this.wiki = wiki;
	}

	/**
	 * Run a job on set of Tasks.
	 * 
	 * @param ml The Tasks to process
	 * @param num The maximum number of threads to instantiate
	 * @return A list of Tasks we were unable to process.
	 */
	protected <T extends Task> ArrayList<Task> submit(ArrayList<T> ml, int num)
	{
		AtomicInteger i = new AtomicInteger();
		
		ArrayList<Task> fails = new ArrayList<>();
		int total = ml.size();
		
		try
		{
		//TODO: Can I print out a statement when a thread joins because there is no more work?
		 new ForkJoinPool(num).submit(() -> fails.addAll(ml.parallelStream().filter(t -> {
			ColorLog.log(String.format("[MBot]: Processing item %d of %d", i.incrementAndGet(), total), "INFO", ColorLog.GREEN);
			return !t.doJob(wiki);
			}).collect(Collectors.toCollection(ArrayList::new)))).get(); 
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		
		if (fails.size() > 0)
		{
			ColorLog.warn(String.format("MBot failed to process (%d): ", fails.size()));
			for (Task t : fails)
				System.err.println(ColorLog.makeString(t.title, ColorLog.PURPLE));
		}
		else
			ColorLog.fyi("MBot completed the task with no failures");

		return fails;
	}

	/**
	 * Represents an individual task.
	 * 
	 * @author Fastily
	 * 
	 */
	public static abstract class Task
	{
		/**
		 * The title to act on
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
		 * Constructor for a Task.
		 * 
		 * @param title The page to act on
		 * @param text The text to add to the page named by <code>title</code>, where applicable. Optional Param: set null
		 *           to disable.
		 * @param summary The log reason/edit summary to use
		 */
		public Task(String title, String text, String summary)
		{
			this.title = title;
			this.text = text;
			this.summary = summary;
		}

		/**
		 * Performs this Task's main task.
		 * 
		 * @param wiki The Wiki object to use.
		 * @return True if the action we tried to perform succeeded.
		 */
		public abstract boolean doJob(Wiki wiki);

		/**
		 * Creates a String representation of this Task. Useful for debugging.
		 */
		public String toString()
		{
			return String.format("(title: %s | text: %s | reason: %s)", title, text, summary);
		}

		/**
		 * Gets the title fields of the passed in MActions and returns them in a list.
		 * 
		 * @param actions The list to get titles from
		 * @param <T> An object extending this class and implementing doJob()
		 * @return The list of titles
		 */
		public static <T extends Task> ArrayList<String> toString(ArrayList<T> actions)
		{
			ArrayList<String> l = new ArrayList<>();
			for (T t : actions)
				l.add(t.title);
			return l;
		}
	}
}