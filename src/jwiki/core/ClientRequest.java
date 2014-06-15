package jwiki.core;

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
import java.util.Map;

/**
 * Class used to do GET & POST requests.
 * 
 * @author Fastily
 * 
 */
public class ClientRequest
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
	 * Content encoding to use for URLEncoded forms.
	 */
	public static final String urlenc = "application/x-www-form-urlencoded";

	/**
	 * All static methods; no constructors allowed.
	 */
	private ClientRequest()
	{

	}

	/**
	 * Sets the cookies of a URLConnection using the specified cookiejar <b>PRECONDITION</b>: You must not have not yet
	 * called <tt>connect()</tt> on <tt>c</tt>, otherwise you'll get an error.
	 * 
	 * @param c The URLConnection to use.
	 * @param cookiejar The cookiejar to use
	 */
	private static void setCookies(URLConnection c, CookieManager cookiejar)
	{
		String cookie = "";
		try
		{
			for (HttpCookie hc : cookiejar.getCookieStore().get(c.getURL().toURI()))
				cookie += String.format("%s=%s;", hc.getName(), hc.getValue());
			//System.out.println(cookie);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
		c.setRequestProperty("Cookie", cookie);
	}

	/**
	 * Grabs cookies from this URLConnection and adds them to a cookiejar.
	 * 
	 * @param u The URLConnection to read https cookie headers from
	 * @param cookiejar The cookiejar to add cookies to.
	 */
	private static void grabCookies(URLConnection u, CookieManager cookiejar)
	{
		try
		{
			cookiejar.put(u.getURL().toURI(), u.getHeaderFields());
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Prepares a URLConnection with the given url and cookiejar.
	 * 
	 * @param url The URL to use.
	 * @param cookiejar The cookiejar to use. This param is optional, specifiy null to disable.
	 * @return The URLConnection.
	 * @throws IOException Network error?
	 */
	private static URLConnection genericURLConnection(URL url, CookieManager cookiejar) throws IOException
	{
		URLConnection c = url.openConnection();
		c.setRequestProperty("User-Agent", Settings.useragent); // required, or server will 403.

		c.setConnectTimeout(connectTimeout);
		c.setReadTimeout(readTimeout);
		if (cookiejar != null)
			setCookies(c, cookiejar);
		return c;
	}

	/**
	 * Creates a URLConnection to be used for a POST request.
	 * 
	 * @param url The URL to use
	 * @param cookiejar The cookiejar to use. This is optional, specify to null to disable.
	 * @param contenttype The optional content-type. This is optional, specify to null to disable.
	 * @return The URLConnection.
	 * @throws IOException Network error.
	 */
	private static URLConnection makePost(URL url, CookieManager cookiejar, String contenttype) throws IOException
	{
		URLConnection c = genericURLConnection(url, cookiejar);
		if (contenttype != null)
			c.setRequestProperty("Content-Type", contenttype);
		c.setDoOutput(true);
		c.connect();
		return c;
	}

	/**
	 * Performs a generic POST request.
	 * 
	 * @param url The URL to use
	 * @param cookiejar The cookiejar to use. Optional param, specify null to disable.
	 * @param contenttype The content type to use. Optional param, specify null to disable.
	 * @param text The text to post
	 * @return The InputStream returned by the server. Be sure to call close() on this when you're finished.
	 * @throws IOException Network error.
	 */
	public static InputStream genericPOST(URL url, CookieManager cookiejar, String contenttype, String text)
			throws IOException
	{
		URLConnection c = makePost(url, cookiejar, contenttype);
		OutputStreamWriter out = new OutputStreamWriter(c.getOutputStream(), "UTF-8");
		out.write(text);
		out.close();
		if (cookiejar != null)
			grabCookies(c, cookiejar);

		return c.getInputStream();
	}

	/**
	 * Performs a generic GET request.
	 * 
	 * @param url The URL to use.
	 * @param cookiejar The cookiejar to use. This is optional; specify null to disable.
	 * @return The InputStream made from the URL. Remember to close the InputStream when you're finished with it!
	 * @throws IOException Network error.
	 */
	public static InputStream genericGET(URL url, CookieManager cookiejar) throws IOException
	{
		URLConnection c = genericURLConnection(url, cookiejar);
		c.connect();
		if (cookiejar != null)
			grabCookies(c, cookiejar);
		return c.getInputStream();
	}

	/**
	 * Does a GET request. Uses given cookiejar.
	 * 
	 * @param url The URL to query
	 * @param cookiejar The cookiejar to use
	 * @return The result of the GET request.
	 * @throws IOException Network error.
	 */
	protected static ServerReply get(URL url, CookieManager cookiejar) throws IOException
	{
		return new ServerReply(genericGET(url, cookiejar));
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
	protected static ServerReply post(URL url, String text, CookieManager cookiejar, String contenttype) throws IOException
	{
		return new ServerReply(genericPOST(url, cookiejar, contenttype, text));
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
	protected static ServerReply chunkPost(URL url, Map<String, ?> params, CookieManager cookiejar) throws IOException
	{
		String boundary = "-----Boundary-----";
		URLConnection c = makePost(url, cookiejar, "multipart/form-data; boundary=" + boundary);
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

		return new ServerReply(c.getInputStream());
	}
}