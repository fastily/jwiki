package fastily.jwiki.util;

import java.nio.file.FileSystems;

/**
 * Useful, frequently used system properties
 * 
 * @author Fastily
 * 
 */
public final class FSystem
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
	 * Hiding constructor from javadoc
	 */
	private FSystem()
	{
		
	}
}