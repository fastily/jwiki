package jwiki.core;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import jwiki.util.FError;
import jwiki.util.FIO;
import jwiki.util.FString;

/**
 * Performs an action on a wiki. Will never throw an exception. Most methods return some sort of value indicating
 * whether we were successful or not in performing the requested action.
 * 
 * @author Fastily
 * 
 */
public class ClientAction
{
	/**
	 * The size of upload chunks. Default = 4Mb
	 */
	protected static final int chunksize = 1024 * 1024 * 4;

	/**
	 * Hiding from javadoc
	 */
	private ClientAction()
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
	protected static boolean edit(Wiki wiki, String title, String text, String reason)
	{
		ColorLog.info(wiki, "Editing " + title);
		URLBuilder ub = wiki.makeUB("edit");

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
	protected static boolean undo(Wiki wiki, String title, String reason)
	{
		ColorLog.fyi(wiki, "Undoing newest revision of " + title);
		try
		{
			ArrayList<Revision> rl = wiki.getRevisions(title, 2, false);
			return rl.size() < 2 ? FError.printErrorAndReturn("There are fewer than two revisions in " + title, false) : edit(
					wiki, title, rl.get(1).text, reason);
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
	protected static boolean purge(Wiki wiki, String title)
	{
		ColorLog.fyi(wiki, "Purging " + title);
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
	protected static boolean delete(Wiki wiki, String title, String reason)
	{
		ColorLog.info(wiki, "Deleting " + title);
		URLBuilder ub = wiki.makeUB("delete");

		String[] es = FString.massEnc(title, reason, wiki.token);
		String posttext = URLBuilder.chainParams("title", es[0], "reason", es[1], "token", es[2]);

		try
		{
			return !ClientRequest.post(ub.makeURL(), posttext, wiki.cookiejar, ClientRequest.urlenc).hasErrorIfIgnore(
					"missingtitle");
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Undelete a page. You must have admin rights on the wiki you are trying to perform this task on, otherwise it won't
	 * go through.
	 * 
	 * @param wiki The wiki object to use
	 * @param title The title to undelete
	 * @param reason The reason to use
	 * @return True if we successfully undeleted the page.
	 */
	protected static boolean undelete(Wiki wiki, String title, String reason)
	{
		ColorLog.info(wiki, "Restoring " + title);
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
	 * Upload a media file to this wiki. Uses the chunked uploads protocol.
	 * 
	 * @param wiki The wiki object to use
	 * @param p The path to the file we're uploading
	 * @param title The title to upload to. Should include the "File:" prefix
	 * @param text The text to use on the file's description page
	 * @param reason The edit summary
	 * @return True if we were successful.
	 */
	protected static boolean upload(Wiki wiki, Path p, String title, String text, String reason)
	{
		String uploadTo = wiki.convertIfNotInNS(title, "File");
		String filename = FIO.getFileName(p);
		String filekey = null;
		URLBuilder ub = wiki.makeUB("upload");

		try (FileChannel fc = FileChannel.open(p, StandardOpenOption.READ))
		{
			long filesize = Files.size(p);
			long chunks = filesize / chunksize + ((filesize % chunksize) > 0 ? 1 : 0);

			HashMap<String, String> args = FString.makeParamMap("filename", Namespace.nss(uploadTo), "token", wiki.token,
					"ignorewarnings", "true", "stash", "1", "filesize", "" + filesize);

			ColorLog.info(wiki, String.format("Uploading '%s' to '%s'", filename, title));

			for (long i = 0, offset = fc.position(), failcount = 0; i < chunks;)
			{
				ColorLog.log(wiki, String.format("(%s): Uploading chunk %d of %d", filename, i + 1, chunks), Level.INFO, ColorLog.PURPLE);
				
				args.put("offset", "" + offset);
				if (filekey != null)
					args.put("filekey", filekey);

				ServerReply r = ClientRequest.chunkPost(ub.makeURL(), wiki.cookiejar, args, filename, fc);

				if (r.hasError()) // allow 5x retries for failed chunks.
				{
					if (++failcount > 5)
						throw new IOException("Server is being difficult today - failed to upload " + filename);
					fc.position(offset);
					ColorLog.error(wiki, String.format("Failed on chunk %d/%d.  Attempt %d/5", i + 1, chunks, failcount));
				}
				else
				{
					filekey = r.getStringR("filekey");
					offset = fc.position();
					i++;
					failcount = 0;
				}
			}

			return filekey != null ? unstash(wiki, filekey, uploadTo, text, reason) : false;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			if (filekey != null)
				unstash(wiki, filekey, uploadTo, text, reason); // try unstashing anyways, b/c sometimes it succeeded
			return false;
		}
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
		ColorLog.info(wiki, String.format("Unstashing '%s' from temporary archive @ '%s'", title, filekey));
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