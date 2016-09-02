package fastily.jwiki.util;

/**
 * A simple implementation of a 3-set of immutable values.
 * 
 * @author Fastily
 *
 * @param <X> The first element
 * @param <Y> The second element
 * @param <Z> The third element
 */
public class Triple<X, Y, Z> extends Tuple<X, Y>
{
	/**
	 * The z value of the Triple
	 */
	public final Z z;

	/**
	 * Constructor
	 * 
	 * @param x The first element
	 * @param y The second element
	 * @param z The third element
	 */
	public Triple(X x, Y y, Z z)
	{
		super(x, y);
		this.z = z;
	}
	
	/**
	 * Prints out a String representation of this Triple.
	 */
	public String toString()
	{
		return String.format("( %s, %s, %s )", x, y, z);
	}
}