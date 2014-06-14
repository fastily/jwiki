package jwiki.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import jwiki.util.FError;
import jwiki.util.FIO;
import jwiki.util.FString;
import jwiki.util.FSystem;

/**
 * Performs an action on a wiki. Will never throw an exception. Most methods return some sort of value indicating
 * whether we were successful or not in performing the requested action.
 * 
 * @author Fastily
 * 
 */
public class FAction
{
	/**
	 * The size of upload chunks.
	 */
	private static final int chunksize = 1024 * 1024 * 4;
	
	/**
	 * Hiding from javadoc
	 */
	private FAction()
	{
		
	}
	
	/**
	 * Edit a page, and check if the request actually went through.
	 * 
	 * @param wiki The wiki to use.
	 * @param title The title to use
	 * @param text The text to use
	 * @param reason The edit summary to use
	 * 
	 * @return True if the operation was successful.
	 */
	public static boolean edit(Wiki wiki, String title, String text, String reason)
	{
		Logger.info(wiki, "Editing " + title);
		URLBuilder ub = wiki.makeUB();
		ub.setAction("edit");
		
		String[] es = FString.massEnc(title, text, reason, wiki.token);
		String posttext = URLBuilder.chainParams("title", es[0], "text", es[1], "summary", es[2], "token", es[3]);
		
		try
		{
			return ClientRequest.post(ub.makeURL(), posttext, wiki.cookiejar, ClientRequest.urlenc).resultIs("Success");
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Undo the top revision of a page. PRECONDITION: <tt>title</tt> must point to a valid page.
	 * 
	 * @param wiki The wiki object to use
	 * @param title The title to edit
	 * @param reason The reason to use
	 * @return True if we were successful.
	 */
	public static boolean undo(Wiki wiki, String title, String reason)
	{
		Logger.fyi(wiki, "Undoing newest revision of " + title);
		try
		{
			Revision[] rl = FQuery.getRevisions(wiki, title, 2, false);
			return rl.length < 2 ? FError.printErrorAndReturn("There are fewer than two revisions in " + title, false) : edit(
					wiki, title, rl[1].getText(), reason);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Purges the cache of a page.
	 * 
	 * @param wiki The wiki object to use
	 * @param title The title of the page to purge
	 * @return True if we were successful.
	 */
	public static boolean purge(Wiki wiki, String title)
	{
		Logger.fyi(wiki, "Purging " + title);
		URLBuilder ub = wiki.makeUB("purge", "titles", FString.enc(title));
		
		try
		{
			ServerReply r = ClientRequest.get(ub.makeURL(), wiki.cookiejar);
			return !r.hasError() && r.getJSONArray("purge").getJSONObject(0).has("purged");
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Deletes a page. You must have admin rights for this to work.
	 * 
	 * @param wiki The wiki object to use.
	 * @param title The title to use.
	 * @param reason The reason to use
	 * @return True if the operation was successful.
	 */
	public static boolean delete(Wiki wiki, String title, String reason)
	{
		Logger.info(wiki, "Deleting " + title);
		URLBuilder ub = wiki.makeUB("delete");
		
		String[] es = FString.massEnc(title, reason, wiki.token);
		String posttext = URLBuilder.chainParams("title", es[0], "reason", es[1], "token", es[2]);
		
		try
		{
			return !ClientRequest.post(ub.makeURL(), posttext, wiki.cookiejar, ClientRequest.urlenc).hasErrorIfIgnore("missingtitle");
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Undelete a page. You must have admin rights on the wiki you are trying to perform this task on, otherwise it
	 * won't go through.
	 * 
	 * @param wiki The wiki object to use
	 * @param title The title to undelete
	 * @param reason The reason to use
	 * @return True if we successfully undeleted the page.
	 */
	public static boolean undelete(Wiki wiki, String title, String reason)
	{
		Logger.info(wiki, "Restoring " + title);
		URLBuilder ub = wiki.makeUB("undelete");
		
		String[] es = FString.massEnc(title, reason, wiki.token);
		String posttext = URLBuilder.chainParams("title", es[0], "reason", es[1], "token", es[2]);
		
		try
		{
			return !ClientRequest.post(ub.makeURL(), posttext, wiki.cookiejar, ClientRequest.urlenc).hasError();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Upload a media file.
	 * 
	 * @param wiki The wiki object to use
	 * @param f The local file to upload
	 * @param title The title to upload to. Must include 'File:' prefix.
	 * @param text The text to upload with, which will appear on the file description page.
	 * @param reason The edit summary to use.
	 * @return True if we were successful.
	 */
	public static boolean upload(Wiki wiki, File f, String title, String text, String reason)
	{
		Logger.info(wiki, String.format("Uploading '%s' to '%s'", f.getName(), title));
		String uploadTo = wiki.convertIfNotInNS(title, "File");
		
		long filesize = f.length();
		if (filesize <= 0)
		{
			System.err.println(String.format("'%s' is an empty file.", f.getName()));
			return false;
		}
		
		long chunks = filesize / chunksize + ((filesize % chunksize) > 0 ? 1 : 0);
		
		URLBuilder ub = wiki.makeUB("upload");
		
		String filekey = null;
		FileInputStream in = null;
		String filename = Namespace.nss(uploadTo);
		
		HashMap<String, Object> l = FSystem.makeParamMap("filename", filename, "token", wiki.token, "ignorewarnings",
				"true", "stash", "1", "filesize", "" + filesize);
		
		try
		{
			in = new FileInputStream(f);
			for (int i = 0; i < chunks; i++)
			{
				Logger.log(wiki, String.format("(%s): Uploading chunk %d of %d", f.getName(), i + 1, chunks), "PURPLE");
				
				l.put("offset", "" + i * chunksize);
				if (filekey != null)
					l.put("filekey", filekey);
				
				if ((filekey = uploadChunk(l, wiki, ub, f, in, i + 1)) == null)
					throw new IOException("Server is being difficult today");
			}
			
			in.close();
			return filekey != null ? unstash(wiki, filekey, filename, text, reason) : false;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			if (filekey != null)
				unstash(wiki, filekey, filename, text, reason);
			FIO.closeInputStream(in);
			return false;
		}
	}
	
	/**
	 * Uploads a single chunk, read out of a file, based on chunksize. Attempts a maximum of 5 uploads before giving up.
	 * 
	 * @param l The HashMap containing our parameters to pass to the server.
	 * @param wiki The wiki object to use
	 * @param ub The pre-defined URLBuilder
	 * @param f The file we're uploading
	 * @param in The inputstream created from the file we're uploading
	 * @param id The chunk number this is.
	 * @return The filekey retrieved from the server, or null if something went wrong.
	 * @throws IOException Eh?
	 */
	private static String uploadChunk(HashMap<String, Object> l, Wiki wiki, URLBuilder ub, File f, FileInputStream in, int id)
			throws IOException
	{
		int remain = in.available();
		byte[] chunk = remain > chunksize ? new byte[chunksize] : new byte[remain];
		in.read(chunk);
		l.put("chunk\"; filename=\"" + f.getName(), chunk);
		
		ServerReply r = null;
		for (int i = 0; i < 5; i++)
		{
			try
			{
				r = ClientRequest.chunkPost(ub.makeURL(), l, wiki.cookiejar);
				break;
			}
			catch (Throwable e)
			{
				e.printStackTrace();
				Logger.error(wiki, String.format("(%s): Encountered error @ chunk %d.  Retrying...", f.getName(), id));
			}
		}
		return !r.hasError() ? r.getStringR("filekey") : null;
	}
	
	/**
	 * Try and unstash a file uploaded via chunked uploads.
	 * 
	 * @param wiki The wiki object to use.
	 * @param filekey The filekey to use
	 * @param title The title to unstash the file to.
	 * @param text The text to put on the file description page.
	 * @param reason The edit summary.
	 * @return True if we were successful.
	 */
	private static boolean unstash(Wiki wiki, String filekey, String title, String text, String reason)
	{
		Logger.info(wiki, String.format("Unstashing '%s' from temporary archive @ '%s'", title, filekey));
		URLBuilder ub = wiki.makeUB("upload");
		
		String[] es = FString.massEnc(title, text, reason, wiki.token, filekey);
		String posttext = URLBuilder.chainParams("filename", es[0], "text", es[1], "comment", es[2], "ignorewarnings", "true",
				"filekey", es[4], "token", es[3]);
		try
		{
			return ClientRequest.post(ub.makeURL(), posttext, wiki.cookiejar, ClientRequest.urlenc).resultIs("Success");
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}
}