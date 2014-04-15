package jwiki.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import jwiki.util.FSystem;

/**
 * Writes text to a file.
 * 
 * @author Fastily
 *
 */
public class WriteFile
{
	/**
	 * The file to use.
	 */
	private File f;
	
	/**
	 * The text to write.
	 */
	private String text;
	
	/**
	 * Whether to overwrite the file or not.
	 */
	private boolean overwrite;
	
	/**
	 * Whether we're writing a raw byte to the file.
	 */
	private boolean writebyte = false;
	
	/**
	 * The byte to write if we're using writebyte.
	 */
	private int b;
	
	/**
	 * Constructor, may overwrite any <tt>f</tt> with specified text using default OS charset.
	 * @param f File to use
	 * @param text Text to write
	 */
	public WriteFile(File f, String text)
	{
		this(f, text, true);
	}
	
	/**
	 * Constructor, may overwrite any <tt>f</tt> with specified text using default OS charset. 
	 * Convenience method, takes a list of Strings and writes it to file.  Each String separated
	 * by the system default newline character.
	 * @param f The file to write to.
	 * @param list The list of Strings to use
	 */
	public WriteFile(File f, String[] list)
	{
		this(f, FString.listCombo(list));
	}
	
	/**
	 * Constructor, choose to overwrite f with specified text.
	 * @param f File to use
	 * @param text Text to write
	 * @param overwrite Whether to overwrite or not (assuming file even exists, otherwise this is n/a)
	 */
	public WriteFile(File f, String text, boolean overwrite)
	{
		this.f = f;
		this.text = text;
		this.overwrite = overwrite;
	}
	
	/**
	 * Constructor, choose to overwrite f with specified text.
	 * @param f File to use.  Use full path for best results.
	 * @param text Text to write
	 * @param overwrite Whether to overwrite or not (assuming file even exists, otherwise this is n/a)
	 */
	public WriteFile(String f, String text, boolean overwrite)
	{
		this(new File(f), text, overwrite);
	}
	
	/**
	 * Constructor, writes a byte to a file.  Does nothing if f is non-existent or if b
	 * is not a writeable byte.
	 * 
	 * @param f The file to use.
	 * @param b The byte to write
	 */
	public WriteFile(File f, int b)
	{
		this(f, null, false);
		this.b = b;
		writebyte = true;
	}
	
	
	/**
	 * Performs the actual write operation.
	 * 
	 * @throws IOException If we had an i/o error of some sort.
	 */
	public void write() throws IOException
	{
		if(writebyte)
		{
			if(!f.exists() || b <= 0) //senseless to write empty chars or to non-existent files
				return;
			
			FileOutputStream out = new FileOutputStream(f, true);
			out.write(b);
			out.close();
		}
		else if(overwrite)
		{
			PrintStream p = new PrintStream(f, FSystem.getDefaultCharset());
			p.print(text);
			p.close();
		}
		else
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(f, f.exists()));
			out.write(text);
			out.close();
		}
	}
}