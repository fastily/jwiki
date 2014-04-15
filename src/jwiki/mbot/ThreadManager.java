package jwiki.mbot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

import jwiki.core.Logger;
import jwiki.core.Wiki;
import jwiki.core.aux.ProgressTracker;
import jwiki.util.FSystem;

/**
 * Manages and generates threads on behalf of MBot.
 * @author Fastily
 *
 */
public class ThreadManager
{
	/**
	 * Our list of MActions to act upon.
	 */
	private final ConcurrentLinkedQueue<WAction> todo = new ConcurrentLinkedQueue<WAction>();
	
	/**
	 * The list of MActions that failed for whatever reason.
	 */
	private final ConcurrentLinkedQueue<WAction> fails = new ConcurrentLinkedQueue<WAction>();
	
	/**
	 * Our progress tracker
	 */
	private final ProgressTracker pt;
	
	/**
	 * Our wiki object which we'll be using for queries.
	 */
	private Wiki wiki;
	
	/**
	 * The maximum number of threads to run
	 */
	private int num;
	
	/**
	 * Constructor, takes a list of items to process and the wiki object to work with.
	 * 
	 * @param wl The list of items to process.
	 * @param wiki The wiki object to use.
	 * @param num The number of threads to run.
	 */
	public ThreadManager(WAction[] wl, Wiki wiki, int num)
	{
		todo.addAll(Arrays.asList(wl));
		pt = new ProgressTracker(wl.length);
		
		this.wiki = wiki;
		this.num = num;
	}
	
	/**
	 * Starts running this ThreadManager. It'll create a maximum number of threads as specified in the constructor and
	 * then attempt to process every passed in MAction.
	 */
	public void start()
	{
		ArrayList<Thread> threads = new ArrayList<Thread>();
		int tcnt = Math.min(todo.size(), num); // dynamically recalculated. Keep out of for loop.
		for (int i = 0; i < tcnt; i++)
		{
			
			Thread t = new Thread(new Job(this));
			threads.add(t);
			t.start();
		}
		
		FSystem.waitOnThreads(threads.toArray(new Thread[0]));
	}
	
	/**
	 * Gets the list of failures. This will be empty until you've run start().
	 * 
	 * @return The current list of failures (i.e. WAction objects whose doJob() functions returned false).
	 */
	public WAction[] getFails()
	{
		return fails.toArray(new WAction[0]);
	}
	
	/**
	 * Subclass which represents an individual thread in this process.
	 * 
	 * @author Fastily
	 * 
	 */
	private static class Job implements Runnable
	{
		/**
		 * Represents the ThreadManager which spawned us.
		 */
		private ThreadManager m;
		
		/**
		 * Creates this object with the specified parent ThreadManager.
		 * 
		 * @param m The parent ThreadManager.
		 */
		protected Job(ThreadManager m)
		{
			this.m = m;
		}
		
		/**
		 * Activates this thread. It'll run until there are no more todos in the linked queue of the ThreadManager.
		 */
		public void run()
		{
			if (m.todo.peek() == null)
				return;
			
			String me = Thread.currentThread().getName() + ": ";
			WAction curr;
			
			while ((curr = m.todo.poll()) != null)
			{
				m.pt.inc(me);
				if (!curr.doJob(m.wiki))
					m.fails.add(curr);
			}
			Logger.fyi(me + "There's nothing left for me!");
		}
	}
}