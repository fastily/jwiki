package jwiki.util;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
	public static final String psep = FileSystems.getDefault().getSeparator();
	
	/**
	 * The user's home directory.
	 */
	public static final String home = System.getProperty("user.home");
	
	/**
	 * Is the system we're using Windows?  If so, this is true.
	 */
	public static final boolean isWindows = System.getProperty("os.name").contains("Windows");
	
	/**
	 * The default script header based on OS.
	 */
	public static final String scriptHeader = isWindows ? "@echo off" : "#!/bin/bash\n" + lsep;
	
	/**
	 * Hiding constructor from javadoc
	 */
	private FSystem()
	{
		
	}
	
	/**
	 * Copies a file on disk.
	 * 
	 * @param src The path of the source file
	 * @param dest The location to copy the file to.
	 * 
	 * @return True if we were successful.
	 */
	
	public static boolean copyFile(String src, String dest)
	{
		try
		{
			Files.copy(Paths.get(src), Paths.get(dest), StandardCopyOption.REPLACE_EXISTING);
			return true;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return false;
		}
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