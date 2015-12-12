package jwiki.util;

/**
 * Simple implementation of a Tuple. A Tuple is an immutable paired 2-set of values (e.g. (x, y)), and may consist of
 * any two Objects (may be in the same class or a different class).
 * 
 * @author Fastily
 * 
 * @param <K> The type of Object allowed for the first Object in the tuple.
 * @param <V> The type of Object allowed for the second Object in the tuple.
 */
public class Tuple<K, V>
{
	/**
	 * The x value of the tuple
	 */
	public final K x;

	/**
	 * The y value of the tuple
	 */
	public final V y;

	/**
	 * Creates a Tuple from the parameter values.
	 * 
	 * @param x The x value of the tuple
	 * @param y The y value of the tuple
	 */
	public Tuple(K x, V y)
	{
		this.x = x;
		this.y = y;
	}

	/**
	 * Gets a String representation of this object. Nice for debugging.
	 */
	public String toString()
	{
		return String.format("< %s, %s >", x, y);
	}

	/**
	 * Gets a hashcode for this object. Good for mapping constructs.
	 */
	public int hashCode()
	{
		return x.hashCode() ^ y.hashCode();
	}

	/**
	 * Determines if two tuples are equal. Equal tuples have the same two elements in the same order.
	 */
	public boolean equals(Object o)
	{
		if (!(o instanceof Tuple))
			return false;

		Tuple<?, ?> temp = (Tuple<?, ?>) o;
		return x.equals(temp.x) && y.equals(temp.y);
	}
}