package jwiki.util;

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
public class MapList<K, V>
{
	/**
	 * The backing structure for this MapList. This is public because a getter would just return a reference to this
	 * anyways.
	 */
	public final HashMap<K, ArrayList<V>> l = new HashMap<>();

	/**
	 * Constructor, creates an empty MapList.
	 */
	public MapList()
	{

	}

	/**
	 * Adds a key-value pair to this MapList.
	 * 
	 * @param k The key to add
	 * @param v The value to add
	 */
	public void put(K k, V v)
	{
		if (l.containsKey(k))
			l.get(k).add(v);
		else
		{
			ArrayList<V> temp = new ArrayList<>();
			temp.add(v);
			l.put(k, temp);
		}
	}

	/**
	 * Merges an ArrayList of V objects into the value set for a given key.
	 * 
	 * @param k The key to use
	 * @param vl The list of values to merge.
	 */
	public void put(K k, ArrayList<V> vl)
	{
		if (l.containsKey(k))
			l.get(k).addAll(vl);
		else
			l.put(k, vl);
	}
}