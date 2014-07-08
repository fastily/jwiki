package jwiki.util;

import java.util.List;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Reads a text file in, line by line. CAVEAT: Only works with small plaintext files! This is meant for reading
 * configuration files, or short lists. If you're doing something extreme, use
 * <tt>newBufferedReader(Path path, Charset cs)</tt> in <tt>Files</tt>.
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
	 * Constructor, takes a pathname. Uses default charset for OS. e.g. Windows = unicode, nix = utf-8. Ignores
	 * blank/whitespace lines by default.
	 * 
	 * @param p The path to read from.
	 */
	public ReadFile(String p)
	{
		this(p, Charset.defaultCharset(), true);
	}

	/**
	 * Constructor, takes a filename and encoding. Sets the internal list to empty if we failed.
	 * 
	 * @param p The filename to read.
	 * @param enc The encoding to use
	 * @param ignoreBlanks Set to true to ignore blank lines.
	 * 
	 */
	public ReadFile(String p, Charset enc, boolean ignoreBlanks)
	{
		try
		{
			List<String> tl = Files.readAllLines(Paths.get(p), enc);
			if (!ignoreBlanks)
				l.addAll(tl);
			else
				for (String s : tl)
					if (!s.trim().isEmpty())
						l.add(s);

		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Returns the items in this ReadFile as an array of Strings, one item per newline.
	 * 
	 * @return The array of Strings as specified.
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
	 *           pair.
	 * @return The resulting hashmap.
	 */
	public HashMap<String, String> getSplitList(String delim)
	{
		HashMap<String, String> h = new HashMap<String, String>();
		for (String s : l)
		{
			int i = s.indexOf(delim);
			if (i > -1)
				h.put(s.substring(0, i), s.substring(i + 1));
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
		return FString.fenceMaker(FSystem.lsep, l.toArray(new String[0]));
	}
}