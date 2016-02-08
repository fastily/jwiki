package jwiki.util;

import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

import jwiki.core.ColorLog;

/**
 * Allows for routines to be performed concurrently while printing progress messages.
 * 
 * @author Fastily
 *
 */
public final class PTask
{
	/**
	 * Tracks the number of Tasks completed
	 */
	private AtomicInteger i = new AtomicInteger();

	/**
	 * The total number of Tasks to complete
	 */
	private int total;

	/**
	 * Constructor, creates a new PTask
	 * 
	 * @param tasks The tasks to process
	 */
	private PTask(ArrayList<Tuple<String, Task>> tasks)
	{
		total = tasks.size();
	}

	/**
	 * Runs a Task, updates the task counter, and returns the result of the Task.
	 * 
	 * @param t The Task to run
	 * @param p The parent PTask
	 * @return The boolean result of the Task. True usually means that the task succeeded.
	 */
	private static boolean doTask(Task t, PTask p)
	{
		ColorLog.fyi(String.format("[PTask]: executing %d of %d", p.i.incrementAndGet(), p.total));
		return t.run();
	}

	/**
	 * Executes a list of Tasks concurrently with the default number of threads. Default is (# of cores - 1). This method
	 * blocks the parent thread executing it until all subtasks are complete.
	 * 
	 * @param tasks The list of Tasks to execute, where each Task has a unique String identifying it. The Tuple's key
	 *           should be the task title, and the value should be the actual Task.
	 * @return A list of Tasks that failed.
	 */
	public static ArrayList<String> execute(ArrayList<Tuple<String, Task>> tasks)
	{
		PTask p = new PTask(tasks);
		ArrayList<String> fails = FL.toAL(tasks.parallelStream().filter(t -> !doTask(t.y, p)).map(t -> t.x));

		if (fails.size() > 0)
		{
			ColorLog.warn(String.format("[PTask]: failed %d items: ", fails.size()));
			for (String s : fails)
				System.err.println(ColorLog.makeString(s, ColorLog.PURPLE));
		}
		else
			ColorLog.fyi("[PTask]: no failures");

		return fails;
	}

	/**
	 * Excutes a list of Tasks concurrently with a specified number of threads. This method blocks the parent thread
	 * executing it until all subtasks are complete.
	 * 
	 * @param tasks The list of Tasks to execute, where each Task has a unique String identifying it. The Tuple's key
	 *           should be the task title, and the value should be the actual Task.
	 * @param maxThreads The maximum number of threads to instantiate.
	 * @return A list of Tasks that failed.
	 */
	public static ArrayList<String> execute(ArrayList<Tuple<String, Task>> tasks, int maxThreads)
	{
		ArrayList<String> fails = new ArrayList<>();
		try
		{
			new ForkJoinPool(maxThreads).submit(() -> fails.addAll(execute(tasks))).get();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}

		return fails;
	}

	/**
	 * An atomic routine which can be executed in parallel. A Task's run() function should return True if the task
	 * succeeded, else False.
	 * 
	 * @author Fastily
	 *
	 */
	public interface Task
	{
		/**
		 * An atomic routine to be executed concurrently via PTask.
		 * 
		 * @return True if the intended task was successful.
		 */
		public boolean run();
	}
}