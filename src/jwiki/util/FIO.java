package jwiki.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * I/O related functions for jwiki.
 * 
 * @author Fastily
 * 
 */
public class FIO
{
	/**
	 * Reads contents of an InputStream to a String.
	 * 
	 * @param is The InputStream to read to File
	 * @param close Set to true to close the InputStream after we're done.
	 * @return The String we made from the InputStream, or the empty String if something went wrong.a
	 */
	public static String inputStreamToString(InputStream is, boolean close)
	{
		try
		{
			String x = "";
			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			
			String line;
			while ((line = in.readLine()) != null)
				x += line + "\n";
			
			if (close)
				is.close();
			
			return x.trim();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * Close an InputStream without generating exceptions.
	 * 
	 * @param is The InputStream to close.
	 * @return True if we closed it successfully.
	 */
	public static boolean closeInputStream(InputStream is)
	{
		try
		{
			is.close();
			return true;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return false;
		}
		
	}
	
}