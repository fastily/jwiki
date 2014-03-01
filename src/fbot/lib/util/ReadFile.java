package fbot.lib.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Reads a file in, line by line.
 * 
 * @author Fastily
 */
public class ReadFile
{
	/**
	 * Data structure storing lines read in.
	 */
	private ArrayList<String> l = new ArrayList<String>();
	
	/**
	 * Constructor, takes a file. Uses default charset for OS. e.g. Windows = unicode, nix = utf-8.
	 * 
	 * @param f The file to read.
	 */
	public ReadFile(File f)
	{
		this(f, FSystem.getDefaultCharset());
	}
	
	/**
	 * Constructor, takes a pathname. Uses default charset for OS. e.g. Windows = unicode, nix = utf-8.
	 * 
	 * @param f The file to read.
	 */
	public ReadFile(String f)
	{
		this(new File(f));
	}
	
	/**
	 * Constructor, takes a file and encoding.
	 * 
	 * @param f The file to read.
	 * @param enc The encoding to use
	 */
	public ReadFile(File f, String enc)
	{
		Scanner m = null;
		try
		{
			m = new Scanner(f, enc);
		}
		catch (Throwable e)
		{
			FError.errAndExit(e);
		}
		
		while (m.hasNextLine())
			l.add(m.nextLine().trim());
	}
	
	/**
	 * Returns the objects in this ReadFile as an array of Strings.
	 * 
	 * @return This object's list of Strings.
	 */
	public String[] getList()
	{
		return l.toArray(new String[0]);
	}
	
	/**
	 * Takes each item in this object's list and splits it using the specified token, and places it into a hashmap. If
	 * the token does not exist, then the line is skipped. Useful for storing data.
	 * 
	 * @param delim The token to split on (first occurance only). The token is not included in the resulting key value
	 *            pair.
	 * @return The resulting hashmap.
	 */
	public HashMap<String, String> getSplitList(String delim)
	{
		HashMap<String, String> h = new HashMap<String, String>();
		for (String s : l)
		{
			int i = s.indexOf(delim);
			if(i > -1)
				h.put(s.substring(0, i), s.substring(i+1));
		}
		
		return h;
	}
	
	/**
	 * Returns the contents of the file as one continuous String.
	 * 
	 * @return The contents of the file as one continuous String.
	 */
	public String getTextAsBlock()
	{
		String x = "";
		for (String s : l)
			x += s + FSystem.lsep;
		return x;
	}
	
	/**
	 * Returns a String representation of this object. One item in this ReadFile per line.
	 * 
	 * @return A string representation of this object.
	 */
	public String toString()
	{
		String x = "";
		for (String s : l)
			x += s + "\n";
		
		return x;
	}
}