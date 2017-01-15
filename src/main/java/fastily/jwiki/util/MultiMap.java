package fastily.jwiki.util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A HashMap which allows multiple values for each key. Duplicate values are permitted.
 * 
 * @author Fastily
 *
 * @param <K> The type of the key
 * @param <V> The type of the values, which will be stored in an ArrayList.
 */
public class MultiMap<K, V>
{
	/**
	 * The backing structure for this MapList. This is public because a getter would just return a reference to this
	 * anyways.
	 */
	public final HashMap<K, ArrayList<V>> l = new HashMap<>();

	/**
	 * Constructor, creates an empty MapList.
	 */
	public MultiMap()
	{

	}

	/**
	 * Creates a new empty ArrayList for {@code k} in this MapList if it did not exist already.  Does nothing otherwise.
	 * @param k The key to create a new entry for, if applicable.
	 */
	public void touch(K k)
	{
		if(!l.containsKey(k))
			l.put(k, new ArrayList<>());
	}
	
	/**
	 * Adds a key-value pair to this MapList.
	 * 
	 * @param k The key to add
	 * @param v The value to add
	 */
	public void put(K k, V v)
	{
		touch(k);
		l.get(k).add(v);
	}

	/**
	 * Merges an ArrayList of V objects into the value set for a given key.
	 * 
	 * @param k The key to use
	 * @param vl The list of values to merge.
	 */
	public void put(K k, ArrayList<V> vl)
	{
		touch(k);
		l.get(k).addAll(vl);
	}
}