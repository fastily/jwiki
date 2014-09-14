package jwiki.util;

/**
 * Simple implementation of a Tuple. A Tuple is an immutable paired 2-set of values (e.g. (x, y)), and may consist of
 * any two Objects (may be in the same class or a different class).
 * 
 * @author Fastily
 * 
 * @param <E> The type of Object allowed for the first Object in the tuple.
 * @param <F> The type of Object allowed for the second Object in the tuple.
 */
public class Tuple<E, F>
{
	/**
	 * The x value of the tuple
	 */
	public final E x;
	
	/**
	 * The y value of the tuple
	 */
	public final F y;
	
	/**
	 * Creates a Tuple from the parameter values.
	 * 
	 * @param x The x value of the tuple
	 * @param y The y value of the tuple
	 */
	public Tuple(E x, F y)
	{
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Gets a String representation of this object. Nice for debugging.
	 * 
	 * @return A String representation of this object.
	 */
	public String toString()
	{
		return String.format("(%s, %s)", x.toString(), y.toString());
	}
}