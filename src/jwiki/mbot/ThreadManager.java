package jwiki.mbot;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import jwiki.core.ColorLog;
import jwiki.core.Wiki;
import jwiki.util.ProgressTracker;

/**
 * Manages and generates threads on behalf of MBot.
 * 
 * @author Fastily
 *
 */
public class ThreadManager
{
	/**
	 * The list tracking MActions left to process.
	 */
	private final ConcurrentLinkedQueue<WAction> todo = new ConcurrentLinkedQueue<>();

	/**
	 * The list tracking MActions that failed.
	 */
	private final ConcurrentLinkedQueue<WAction> fails = new ConcurrentLinkedQueue<>();

	/**
	 * The progress tracker to use
	 */
	private final ProgressTracker pt;

	/**
	 * The wiki object to use for queries.
	 */
	private Wiki wiki;

	/**
	 * The maximum number of threads permitted to execute simultaneously
	 */
	private int num;

	/**
	 * Constructor, takes a list of items to process and the wiki object to work with.
	 * 
	 * @param wl The list of items to process.
	 * @param wiki The wiki object to use.
	 * @param num The maximum number of threads permitted to execute simultaneously
	 */
	protected <T extends WAction> ThreadManager(ArrayList<T> wl, Wiki wiki, int num)
	{
		todo.addAll(wl);
		pt = new ProgressTracker(wl.size());

		this.wiki = wiki;
		this.num = num;
	}

	/**
	 * Runs this ThreadManager. Creates the maximum number of threads requested (if necessary) and
	 * processes each MAction.
	 */
	protected void start()
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
		
		WAction curr;
		while ((curr = todo.poll()) != null)
		{
			pt.inc(me);
			if (!curr.doJob(wiki))
				fails.add(curr);
		}
		ColorLog.fyi(me + "There's nothing left for me!");
	}

	/**
	 * Gets the list of failed MActions. This will be empty until <code>start()</code> has finished executing.
	 * 
	 * @return The list of failures (i.e. WAction objects whose <code>doJob()</code> functions returned false).
	 */
	protected ArrayList<WAction> getFails()
	{
		return new ArrayList<WAction>(fails);
	}
}