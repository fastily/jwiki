package jwiki.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Class used to do GET/POST requests.
 * 
 * @author Fastily
 * 
 */
public final class Req
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
	 * The format string for the chunked upload headers.
	 */
	private static final String chunkheaderfmt = "Content-Disposition: form-data; name=\"%s\"\r\n"
			+ "Content-Type: %s; charset=UTF-8\r\n" + "Content-Transfer-Encoding: %s\r\n\r\n";

	/**
	 * Content encoding to use for URLEncoded forms.
	 */
	public static final String urlenc = "application/x-www-form-urlencoded";

	/**
	 * All static methods; no constructors allowed.
	 */
	private Req()
	{

	}

	/**
	 * Sets the cookies of a URLConnection using the specified cookiejar <b>PRECONDITION</b>: You must not have not yet
	 * called <code>connect()</code> on <code>c</code>, otherwise you'll get an error.
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
	 * Prepares a URLConnection with the given url and cookiejar. <br>
	 * Additional properties include:
	 * <ul>
	 * <li>Connection: keep-alive</li>
	 * <li>Accept-Encoding: gzip</li>
	 * <li>User-Agent: Whatever Settings.useragent is set to</li>
	 * <ul>
	 * 
	 * @param url The URL to use.
	 * @param cookiejar The cookiejar to use. This param is optional, specifiy null to disable.
	 * @return The URLConnection.
	 * @throws IOException Network error?
	 */
	private static URLConnection genericURLConnection(URL url, CookieManager cookiejar) throws IOException
	{
		URLConnection c = url.openConnection();
		c.setRequestProperty("User-Agent", Settings.userAgent); // required, or server will 403.
		c.setRequestProperty("Connection", "keep-alive");
		c.setRequestProperty("Accept-Encoding", "gzip");

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
	 * Checks if the response from the server is in gzip encoding. PRECONDITION: you must have already called connect()
	 * on the URLConnection and received a response from the server for this to work properly.
	 * 
	 * @param c The URLConnection to check.
	 * @return A gzip decoding inputstream or a normal inputstream if the response was not compressed.
	 * @throws IOException Network error
	 */
	private static InputStream resolveCompression(URLConnection c) throws IOException
	{
		String ce = c.getHeaderField("Content-Encoding");
		return ce != null && ce.toLowerCase().equals("gzip") ? new GZIPInputStream(c.getInputStream()) : c.getInputStream();
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

		return resolveCompression(c);
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
		return resolveCompression(c);
	}

	/**
	 * Does a GET request. Uses given cookiejar.
	 * 
	 * @param url The URL to query
	 * @param cookiejar The cookiejar to use
	 * @return The result of the GET request.
	 * @throws IOException Network error.
	 */
	protected static Reply get(URL url, CookieManager cookiejar) throws IOException
	{
		return new Reply(genericGET(url, cookiejar));
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
	protected static Reply post(URL url, String text, CookieManager cookiejar, String contenttype) throws IOException
	{
		return new Reply(genericPOST(url, cookiejar, contenttype, text));
	}

	/**
	 * Performs a multipart/form-data post. Primarily intended for use while uploading files. Basically we're trying to
	 * output something that looks like <a
	 * href="https://www.mediawiki.org/w/index.php?title=API:Upload&oldid=842387#Sample_Raw_Upload">this</a>. The primary
	 * goal for writing the upload code like this is for a minimal memory footprint; this means users on faster internet
	 * connections could feasibly use a multi-threaded bot to quickly upload files without hitting some OutOfMemoryError.
	 * 
	 * @param url The URL to upload to
	 * @param cookiejar the cookiejar to use
	 * @param args The argument list to pass in.
	 * @param filename The local name of the file we're uploading
	 * @param fc The FileChannel to read bytes from
	 * @return The reply from the server.
	 * @throws IOException Network error.
	 */
	protected static Reply chunkPost(URL url, CookieManager cookiejar, HashMap<String, String> args, String filename,
			FileChannel fc) throws IOException
	{
		String boundary = "-----Boundary-----";
		URLConnection c = makePost(url, cookiejar, "multipart/form-data; boundary=" + boundary);
		boundary = "--" + boundary + "\r\n";

		String temp = new String(boundary);
		for (Map.Entry<String, String> t : args.entrySet())
		{
			temp += String.format(chunkheaderfmt, t.getKey(), "text/plain", "8bit");
			temp += t.getValue();
			temp += "\r\n" + boundary;
		}

		temp += String.format(chunkheaderfmt, "chunk\"; filename=\"" + filename, "application/octet-stream", "binary"); // hacky

		OutputStream os = c.getOutputStream();
		os.write(temp.getBytes("UTF-8"));

		pipe(fc, os, WAction.chunksize);
		os.write(new String("\r\n" + boundary + "--\r\n\r\n").getBytes("UTF-8"));
		os.close();

		grabCookies(c, cookiejar);
		return new Reply(resolveCompression(c));
	}

	/**
	 * Reads bytes in from a file and redirects them to an outputstream. Useful for performing chunked uploads in a
	 * memory friendly way. CAVEAT: This method does not close streams.
	 * 
	 * @param fc The filechannel to read bytes from
	 * @param os The output stream to write bytes to.
	 * @param max The maximum number of bytes to transfer. This *must* be a multiple of <code>bf.capacity()</code> if you
	 *           want this feature to work properly.
	 * @throws IOException If I/O error.
	 */
	private static void pipe(FileChannel fc, OutputStream os, long max) throws IOException
	{
		byte[] uout = new byte[1024 * 512]; // set buffer size to 512kb
		ByteBuffer buf = ByteBuffer.wrap(uout);

		for (long i = 0; i < max;)
		{
			int read = fc.read(buf);
			if (read == -1)
				break;

			i += read;

			os.write(uout, 0, read);
			buf.clear();
		}
	}
}