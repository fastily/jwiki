package jwiki.util;

import jwiki.core.Logger;

/**
 * Simple progress tracker that outputs our progress to std out. Does not enforce curr &le; end. This class is only for
 * logging/tracking.
 * 
 * @author Fastily
 * 
 */
public class ProgressTracker
{
	/**
	 * The current value we're at.
	 */
	private int curr = 0;
	
	/**
	 * The total number of tasks we're supposed to complete.
	 */
	private int end;
	
	/**
	 * Message to output. Must contain 2x <tt>%d</tt>s, otherwise we'll get funny errors. First <tt>%d</tt> is
	 * numerator, second one is denominator.
	 */
	private String msg = "Processing item %d of %d";
	
	/**
	 * Constructor, takes an endpoint. All ProgressTrackers start at position 0 with curr = 0.
	 * 
	 * @param end The endpoint (e.g. number of tasks to complete)
	 */
	public ProgressTracker(int end)
	{
		this.end = end;
	}
	
	/**
	 * Changes the default log message. Must contain 2x <tt>%d</tt>s, otherwise we'll get funny errors. First
	 * <tt>%d</tt> is numerator, second one is denominator.
	 * 
	 * @param msg The new message.
	 */
	public synchronized void setMessage(String msg)
	{
		this.msg = msg;
	}
	
	/**
	 * Increments this ProgressTracker by one. Outputs log info to std out.
	 * 
	 * @return The current value of the counter.
	 */
	public synchronized int inc()
	{
		return inc("");
	}
	
	/**
	 * Increments this ProgressTracker by one. Allows use of a custom header message. So basically print "header + msg".
	 * 
	 * @param head The custom header message to use.
	 * @return The current value of the counter.
	 */
	public synchronized int inc(String head)
	{
		Logger.log(head + String.format(msg, ++curr, end), "green");
		return curr;
	}
	
}