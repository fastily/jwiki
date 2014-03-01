package fbot.lib.core.aux;

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
	private final E x;
	
	/**
	 * The y value of the tuple
	 */
	private final F y;
	
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
	 * Gets the X value of the tuple.
	 * 
	 * @return The X value of the tuple.
	 */
	public E getX()
	{
		return x;
	}
	
	/**
	 * Gets the Y value of the tuple.
	 * 
	 * @return The Y value of the tuple.
	 */
	public F getY()
	{
		return y;
	}
	
	/**
	 * Checks to see if two tuples are equal in value.
	 * 
	 * @param other The other tuple to compare this one with.
	 * @return True if the two tuples are equal in value.
	 */
	public boolean equals(Tuple<E, F> other)
	{
		return other != null && other instanceof Tuple && y.equals(other.y) && x.equals(other.x);
	}
	
	
	/**
	 * Gets a String representation of this object.  Nice for debugging.
	 * 
	 * @return A String representation of this object.
	 */
	public String toString()
	{
		return String.format("(%s, %s)", x.toString(), y.toString());
	}
	
}