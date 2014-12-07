package jwiki.core;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import jwiki.util.ProgressTracker;

/**
 * Facilitates the use and design of multi-threaded bots derived from the jwiki library.
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
		ThreadManager m = new ThreadManager(ml, num);
		m.start();

		ArrayList<Task> fails = new ArrayList<Task>(m.fails);
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
	 * Manages and generates threads on behalf of MBot.
	 * 
	 * @author Fastily
	 *
	 */
	private class ThreadManager
	{
		/**
		 * The list tracking MActions left to process.
		 */
		private final ConcurrentLinkedQueue<Task> todo = new ConcurrentLinkedQueue<>();

		/**
		 * The list tracking MActions that failed.
		 */
		private final ConcurrentLinkedQueue<Task> fails = new ConcurrentLinkedQueue<>();

		/**
		 * The progress tracker to use
		 */
		private final ProgressTracker pt;

		/**
		 * The maximum number of threads permitted to execute simultaneously
		 */
		private int num;

		/**
		 * Constructor, takes a list of items to process and the wiki object to work with.
		 * 
		 * @param wl The list of items to process.
		 * @param num The maximum number of threads permitted to execute simultaneously
		 */
		private <T extends Task> ThreadManager(ArrayList<T> wl, int num)
		{
			todo.addAll(wl);
			pt = new ProgressTracker(wl.size());

			this.num = num;
		}

		/**
		 * Runs this ThreadManager. Creates the maximum number of threads requested (if necessary) and processes each
		 * MAction.
		 */
		private void start()
		{
			ArrayList<Thread> threads = new ArrayList<>();
			int tcnt = Math.min(todo.size(), num); // dynamically recalculated like an idiot by JVM. Keep out of for loop.
			for (int i = 0; i < tcnt; i++)
			{
				Thread t = new Thread(() -> doJob());
				threads.add(t);
				t.start();
			}

			for (Thread t : threads)
			{
				try
				{
					t.join();
				}
				catch (Throwable e)
				{
					e.printStackTrace();
				}
			}
		}

		/**
		 * Helper function called by threads consuming elements of <code>todo</code>.
		 */
		private void doJob()
		{
			if (todo.peek() == null)
				return;

			String me = Thread.currentThread().getName() + ": ";

			Task curr;
			while ((curr = todo.poll()) != null)
			{
				pt.inc(me);
				if (!curr.doJob(wiki))
					fails.add(curr);
			}
			ColorLog.fyi(me + "There's nothing left for me!");
		}
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
		 * Performs this Task's main task. *Must* be explicitly defined in each subclass.
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