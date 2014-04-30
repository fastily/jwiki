package jwiki.util;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * Create an encrypted user/password file so you don't have to re-enter credentials every time you run programs.
 * 
 * @author Fastily
 * 
 */
public class FLogin
{
	/**
	 * Random salt to make brute force attacks harder.
	 */
	private static final byte[] salt = intToByte(0xde, 0xad, 0xbe, 0xef, 0xde, 0xad, 0xbe, 0xef);
	
	/**
	 * Represents our encryption method
	 */
	private static final String enctype = "PBEWithMD5AndDES";
	
	/**
	 * Our secret key object
	 */
	private static final SecretKey key = makeSecretKey();
	
	/**
	 * Our cipher object
	 */
	private static final Cipher cipher = makeCipher();
	
	/**
	 * PBE Parameter. Generated with salt.
	 */
	private static final PBEParameterSpec pbeps = new PBEParameterSpec(salt, 20);
	
	/**
	 * The default .px.txt location.
	 */
	protected static final String pxloc1 = String.format("%s%s%s", FSystem.home, FSystem.psep, ".px.txt");
	
	/**
	 * The secondary .px.txt location
	 */
	protected static final String pxloc2 = ".px.txt";
	
	/**
	 * If main() is called, this will be initialized to the JVM's console.
	 */
	private static Console c;
	
	/**
	 * Internal method to create our cipher
	 * 
	 * @return This class's cipher.
	 */
	private static Cipher makeCipher()
	{
		try
		{
			return Cipher.getInstance(enctype);
		}
		catch (Throwable e)
		{
			FError.errAndExit(e);
		}
		return null; // dead code to shut up compiler
	}
	
	/**
	 * Internal method to create our secret key.
	 * 
	 * @return This class's secret key.
	 */
	private static SecretKey makeSecretKey()
	{
		try
		{
			return SecretKeyFactory.getInstance(enctype).generateSecret(new PBEKeySpec("aklsdjffdsafol".toCharArray()));
		}
		catch (Throwable e)
		{
			FError.errAndExit(e);
		}
		return null; // dead code to shut up compiler
	}
	
	/**
	 * Converts an array of ints to an array of bytes.
	 * 
	 * @param is The array of ints
	 * @return The equivalent array of bytes.
	 */
	private static byte[] intToByte(int... is)
	{
		byte[] t = new byte[is.length];
		for (int i = 0; i < is.length; i++)
			t[i] = (byte) is[i];
		return t;
	}
	
	/**
	 * Encrypts a String.
	 * 
	 * @param s The string to encrypt
	 * @return The encrypted string as bytes
	 */
	private static byte[] encode(String s)
	{
		try
		{
			cipher.init(Cipher.ENCRYPT_MODE, key, pbeps);
			return cipher.doFinal(s.getBytes("UTF-8"));
		}
		catch (Throwable e)
		{
			FError.errAndExit(e);
		}
		return null; // dead code to shut up compiler
	}
	
	/**
	 * Decrypts an encrypted set of bytes.
	 * 
	 * @param s The bytes to decrypt
	 * @return An unencrypted string.
	 */
	private static String decode(byte[] s)
	{
		try
		{
			cipher.init(Cipher.DECRYPT_MODE, key, pbeps);
			return new String(cipher.doFinal(s), "UTF-8");
		}
		catch (Throwable e)
		{
			FError.errAndExit(e);
		}
		return null; // dead code to shut up compiler
	}
	
	/**
	 * Generates a user-password pair via command line.
	 * 
	 * @return A tuple (username, password)
	 */
	private static Tuple<String, String> genPX()
	{
		String u = c.readLine("%nEnter a username: ").trim();
		c.printf("=== Characters hidden for security === %n");
		char[] p1 = c.readPassword("Enter password for %s: ", u);
		char[] p2 = c.readPassword("Confirm/Re-enter password for %s: ", u);
		
		return Arrays.equals(p1, p2) ? new Tuple<String, String>(u, new String(p1)) : null;
	}
	
	/**
	 * Main driver.
	 * 
	 * @param args Doesn't take args
	 * @throws Throwable If something went wrong.
	 */
	public static void main(String[] args) throws Throwable
	{
		c = System.console();
		if (c == null)
			FError.errAndExit("You need to be running in CLI mode");
		
		String x = "";
		c.printf("Welcome to FLogin!%nThis utility will encrypt & store your usernames/passwords%n(c) 2014 Fastily%n%n");
		
		while (true)
		{
			Tuple<String, String> t = genPX();
			if (t == null)
				continue;
			
			x += String.format("%s:%s%n", t.x, t.y);
			if (c.readLine("Continue? (Y/N): ").trim().toLowerCase().equals("n"))
				break;
		}
		
		c.printf("%nUser quit%n");
		
		if (!x.isEmpty())
		{
			try
			{
				FileOutputStream fos = new FileOutputStream(pxloc1);
				fos.write(encode(x));
				fos.close();
				
				FSystem.copyFile(pxloc1, pxloc2);
				c.printf("Written successfully to '%s' & '%s'%n", pxloc1, pxloc2);
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Generates the user/password list by decoding .px.txt files.
	 */
	protected static HashMap<String, String> genPXList()
	{
		HashMap<String, String> pxlist = new HashMap<String, String>();
		try
		{
			FileInputStream fis;
			if (new File(pxloc1).isFile())
				fis = new FileInputStream(pxloc1);
			else if (new File(pxloc2).isFile())
				fis = new FileInputStream(pxloc1);
			else
				throw new FileNotFoundException(".px.txt was not found");
			
			byte[] buf = new byte[fis.available()];
			fis.read(buf);
			fis.close();
			
			for (String s : FString.splitCombo(decode(buf)))
			{
				Tuple<String, String> t = FString.splitOn(s, ":");
				pxlist.put(t.x, t.y);
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			FError.errAndExit(e, ".px.txt doesn't seem to exist anywhere.  Run 'java FLogin' to create .px.txt");
		}
		
		return pxlist;
	}
}