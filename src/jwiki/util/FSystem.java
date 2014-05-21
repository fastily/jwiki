package jwiki.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * System related methods.
 * 
 * @author Fastily
 * 
 */
public class FSystem
{
	/**
	 * The default line separator for text files by OS. For Windows it's '\r\n' and for Mac/Unix it's just '\n'.
	 */
	public static final String lsep = System.getProperty("line.separator");
	
	/**
	 * The default separator for pathnames by OS. For Windows it is '\' for Mac/Unix it is '/'
	 */
	public static final String psep = File.separator;
	
	/**
	 * The user's home directory.
	 */
	public static final String home = System.getProperty("user.home");
	
	
	public static final boolean isWindows = System.getProperty("os.name").contains("Windows");
	
	/**
	 * Hiding constructor from javadoc
	 */
	private FSystem()
	{
		
	}
	
	/**
	 * Gets the default character sets for file read-ins/writes by os. e.g. Windows = "Unicode" , unix = "UTF-8"
	 * 
	 * @return The charset defined for this os.
	 */
	public static String getDefaultCharset()
	{
		return isWindows ? "US-ASCII" : "UTF-8";
	}
	
	/**
	 * Returns the header of a batch/bash script, depending on OS.
	 * 
	 * @return The header to the script by OS.
	 */
	public static String getScriptHeader()
	{
		return (isWindows ? "@echo off" : "#!/bin/bash\n") + lsep;
	}
	
	/**
	 * Copies a file on disk.
	 * 
	 * @param src The path of the source file
	 * @param dest The location to copy the file to.
	 * 
	 * @throws IOException If we encountered some sort of read/write error
	 */
	
	public static void copyFile(String src, String dest) throws IOException
	{
		FileInputStream in = new FileInputStream(new File(src));
		FileOutputStream out = new FileOutputStream(new File(dest));
		
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0)
			out.write(buf, 0, len);
		
		in.close();
		out.close();
	}
	
	/**
	 * Creates a HashMap with keys as String, and Objects as values. Pass in each pair and value (in that order) into
	 * <tt>ol</tt>. This will be one pair entered into resulting HashMap.
	 * 
	 * @param ol The list of elements to turn into a HashMap.
	 * @return The resulting HashMap, or null if you specified an odd number of elements.
	 */
	public static HashMap<String, Object> makeParamMap(Object... ol)
	{
		HashMap<String, Object> l = new HashMap<String, Object>();
		
		if (ol.length % 2 == 1)
			return null;
		
		for (int i = 0; i < ol.length; i += 2)
			if (ol[i] instanceof String)
				l.put((String) ol[i], ol[i + 1]);
		
		return l;
	}
	
}