package fbot.lib.core;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Class used to do GET & POST requests.
 * 
 * @author Fastily
 * 
 */
public class Request
{
	/**
	 * Connection timeout for URLConnections
	 */
	private static final int connectTimeout = 60000;
	
	/**
	 * Read timeout for URLConnections
	 */
	private static final int readTimeout = 360000;
	
	/**
	 * Represents the content encoding to use for URLEncoded forms.
	 */
	protected static final String urlenc = "application/x-www-form-urlencoded";
	
	/**
	 * Sets the cookies of a URLConnection using the specified cookiejar <b>PRECONDITION</b>: You must not have not yet
	 * called <tt>connect()</tt> on <tt>c</tt>, otherwise you'll get an error.
	 * 
	 * @param c The URLConnection to use.
	 * @param cookiejar The cookiejar to use
	 */
	private static void setCookies(URLConnection c, HashMap<String, String> cookiejar)
	{
		String cookie = "";
		for(Map.Entry<String, String> e : cookiejar.entrySet())
			cookie += String.format("%s=%s;", e.getKey(), e.getValue());
		
		System.out.println("COOKIE: " + cookie);
		
		c.setRequestProperty("Cookie", cookie);
		c.setRequestProperty("User-Agent", "toypowerf.01"); // required, or server will 403.
	}
	
	/**
	 * Grabs cookies from this URLConnection and assigns the to the specified cookiejar.
	 * 
	 * @param u The URLConnection to check
	 * @param cookiejar The cookiejar to assign cookies to.
	 */
	private static void grabCookies(URLConnection u, HashMap<String, String> cookiejar)
	{
		
		String ht;
		for (int i = 1; (ht = u.getHeaderFieldKey(i)) != null; i++)
		{
			System.out.println(String.format("%s: %s", ht, u.getHeaderField(i)));
			if(ht.equals("Set-Cookie"))
			{
				String c = u.getHeaderField(i);
				int ex = c.indexOf('=');
				cookiejar.put(c.substring(0, ex), c.substring(ex+1, c.indexOf(';')));
			}
		}
	}
	
	/**
	 * Does a post operation with the given text.
	 * 
	 * @param url The URL to post to
	 * @param text The text to post to the URL
	 * @param cookiejar The cookiejar to use
	 * @param contenttype Sets the content-type header. Set to null if you don't want to use it.
	 * @return A Reply object containing the result.
	 * @throws IOException Network error.
	 */
	protected static Reply post(URL url, String text, HashMap<String, String> cookiejar, String contenttype) throws IOException
	{
		URLConnection c = url.openConnection();
		c.setConnectTimeout(connectTimeout);
		c.setReadTimeout(readTimeout);
		setCookies(c, cookiejar);
		
		if (contenttype != null)
			c.setRequestProperty("Content-Type", contenttype);
		c.setDoOutput(true);
		c.connect();
		OutputStreamWriter out = new OutputStreamWriter(c.getOutputStream(), "UTF-8");
		out.write(text);
		out.close();
		grabCookies(c, cookiejar);
		
		return new Reply(c.getInputStream());
	}
	
	/**
	 * Performs a multipart/form-data post. Primarily intended for use while uploading files. Adapted from MER-C's <a
	 * href="https://code.google.com/p/wiki-java/">wiki-java</a>
	 * 
	 * @param url The URL to post to
	 * @param params The parameters to post with. Accepted values are: String & byte[] arrays.
	 * @param cookiejar The cookiejar to use
	 * @return A Reply from the server.
	 * @throws IOException Network error
	 */
	protected static Reply chunkPost(URL url, Map<String, ?> params, HashMap<String, String> cookiejar) throws IOException
	{
		String boundary = "-----Boundary-----";
		
		URLConnection c = url.openConnection();
		c.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		c.setConnectTimeout(connectTimeout);
		c.setReadTimeout(readTimeout);
		setCookies(c, cookiejar);
		c.setDoOutput(true);
		c.connect();
		
		boundary = "--" + boundary + "\r\n";
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bout);
		out.writeBytes(boundary);
		
		for (Map.Entry<String, ?> entry : params.entrySet())
		{
			String name = entry.getKey();
			Object value = entry.getValue();
			out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"\r\n");
			if (value instanceof String)
			{
				out.writeBytes("Content-Type: text/plain; charset=UTF-8\r\n\r\n");
				out.write(((String) value).getBytes("UTF-8"));
			}
			else if (value instanceof byte[])
			{
				out.writeBytes("Content-Type: application/octet-stream\r\n\r\n");
				out.write((byte[]) value);
			}
			else
				throw new UnsupportedOperationException("Unrecognized data type");
			
			out.writeBytes("\r\n" + boundary);
		}
		
		out.writeBytes("--\r\n");
		out.close();
		
		OutputStream uout = c.getOutputStream();
		uout.write(bout.toByteArray());
		uout.close();
		out.close();
		
		grabCookies(c, cookiejar);
		
		return new Reply(c.getInputStream());
		
	}
	
	/**
	 * Does a GET request. Uses given cookiejar.
	 * 
	 * @param url The URL to query
	 * @param cookiejar The cookiejar to use
	 * @return The result of the GET request.
	 * @throws IOException Network error.
	 */
	protected static Reply get(URL url, HashMap<String, String> cookiejar) throws IOException
	{
		return new Reply(getInputStream(url, cookiejar));
	}
	
	/**
	 * Makes an InputStream from the given URL.
	 * 
	 * @param url The URL to use.
	 * @param cookiejar The cookiejar to use. This is optional; specifiy null to disable.
	 * @return The InputStream made from the URL. Remember to close the InputStream when you're finished with it!
	 * @throws IOException Network error.
	 */
	protected static InputStream getInputStream(URL url, HashMap<String, String> cookiejar) throws IOException
	{
		URLConnection c = url.openConnection();
		c.setConnectTimeout(connectTimeout);
		c.setReadTimeout(readTimeout);
		
		if (cookiejar != null)
			setCookies(c, cookiejar);
		c.connect();
		
		return c.getInputStream();
	}
}