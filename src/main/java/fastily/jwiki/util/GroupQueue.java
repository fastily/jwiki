package fastily.jwiki.util;

import java.util.ArrayList;

/**
 * A simple read-only Queue that allows multiple items to be polled at once.
 * 
 * @author Fastily
 *
 * @param <T> The type of Object contained in the queue.
 */
public class GroupQueue<T>
{
	/**
	 * The backing data structure.
	 */
	private final ArrayList<T> l;

	/**
	 * The size, start, end, and maximum size of polls.
	 */
	private int size, start = 0, end, maxPoll;

	/**
	 * Constructor, creates a new GroupQueue.
	 * 
	 * @param l The backing ArrayList to use. This will not be modified.
	 * @param maxPoll The maximum number of elements to poll at once.
	 */
	public GroupQueue(ArrayList<T> l, int maxPoll)
	{
		this.l = l;
		size = l.size();
		end = maxPoll;

		this.maxPoll = maxPoll;
	}

	/**
	 * Polls this Queue and returns &le; <code>maxPoll</code> elements.
	 * 
	 * @return An ArrayList with the first <code>maxPoll</code> elements if possible. Returns the empty list if there is
	 *         nothing left.
	 */
	public ArrayList<T> poll()
	{
		if (!has())
			return new ArrayList<>();

		if (size - start < maxPoll)
			end = size;

		ArrayList<T> temp = new ArrayList<>(l.subList(start, end));

		start += maxPoll;
		end += maxPoll;

		return temp;
	}

	/**
	 * Determines whether there are elements remaining in the queue.
	 * 
	 * @return True if there are elements left in the queue.
	 */
	public boolean has()
	{
		return start < size;
	}
}