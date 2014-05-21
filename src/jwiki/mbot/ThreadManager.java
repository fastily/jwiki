package jwiki.mbot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

import jwiki.core.Logger;
import jwiki.core.Wiki;
import jwiki.util.ProgressTracker;

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
	protected ThreadManager(WAction[] wl, Wiki wiki, int num)
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
	protected void start()
	{
		ArrayList<Thread> threads = new ArrayList<Thread>();
		int tcnt = Math.min(todo.size(), num); // dynamically recalculated. Keep out of for loop.
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
	 * Should be called by a thread to processing of todo.
	 */
	private void doJob()
	{
		if (todo.peek() == null)
			return;
		
		String me = Thread.currentThread().getName() + ": ";
		WAction curr;
		
		while ((curr = todo.poll()) != null)
		{
			pt.inc(me);
			if (!curr.doJob(wiki))
				fails.add(curr);
		}
		Logger.fyi(me + "There's nothing left for me!");
	}
	
	/**
	 * Gets the list of failures. This will be empty until you've run start().
	 * 
	 * @return The current list of failures (i.e. WAction objects whose doJob() functions returned false).
	 */
	protected WAction[] getFails()
	{
		return fails.toArray(new WAction[0]);
	}
}