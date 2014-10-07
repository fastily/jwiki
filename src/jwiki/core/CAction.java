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
public class CAction
{
	/**
	 * The size of upload chunks. Default = 4Mb
	 */
	protected static final int chunksize = 1024 * 1024 * 4;

	/**
	 * Hiding from javadoc
	 */
	private CAction()
	{

	}

	/**
	 * Performs an action on wiki requiring a simple POST request.
	 * 
	 * @param wiki The wiki object to use
	 * @param ub The URLBuilder to use
	 * @param params The parameters to post, in order of param, value, param, value...
	 * @return A reply from the server or null if something went wrong.
	 */
	protected static Reply doAction(Wiki wiki, URLBuilder ub, String... params)
	{
		try
		{
			return CRequest
					.post(ub.makeURL(), URLBuilder.chainParams(FString.massEnc(params)), wiki.cookiejar, CRequest.urlenc);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Edit a page
	 * 
	 * @param wiki The object wiki to use.
	 * @param title The title to edit
	 * @param text The new page text to set <code>title</code> to.
	 * @param reason The edit summary to use
	 * 
	 * @return True if the operation was successful.
	 */
	protected static boolean edit(Wiki wiki, String title, String text, String reason)
	{
		ColorLog.info(wiki, "Editing " + title);
		Reply r = doAction(wiki, wiki.makeUB("edit"), "title", title, "text", text, "summary", reason, "token", wiki.token);
		return r != null && r.resultIs("Success");
	}

	/**
	 * Undo the top revision of a page. PRECONDITION: <code>title</code> must point to an existing page.
	 * 
	 * @param wiki The wiki object to use
	 * @param title The title to edit
	 * @param reason The reason to use
	 * @return True if the operation was successful.
	 */
	protected static boolean undo(Wiki wiki, String title, String reason)
	{
		ColorLog.fyi(wiki, "Undoing top revision of " + title);
		ArrayList<Revision> rl = wiki.getRevisions(title, 2, false);
		return rl.size() < 2 ? FError.printErrorAndReturn("There are fewer than two revisions in " + title, false) : edit(
				wiki, title, rl.get(1).text, reason);
	}

	// TODO: Purge module can actually take multiple titles
	/**
	 * Purges the cache of a page.
	 * 
	 * @param wiki The wiki object to use
	 * @param title The title to purge.
	 * @return True if we were successful.
	 */
	protected static boolean purge(Wiki wiki, String title)
	{
		ColorLog.fyi(wiki, "Purging " + title);
		Reply r = QueryTools.doSingleQuery(wiki, wiki.makeUB("purge", "titles", FString.enc(title)));
		return r != null && !r.hasError() && r.getJSONArray("purge").getJSONObject(0).has("purged");
	}

	/**
	 * Deletes a page. Requires sysop privileges on wiki.
	 * 
	 * @param wiki The wiki object to use.
	 * @param title The title to use.
	 * @param reason The reason to use
	 * @return True if the operation was successful.
	 */
	protected static boolean delete(Wiki wiki, String title, String reason)
	{
		ColorLog.info(wiki, "Deleting " + title);
		Reply r = doAction(wiki, wiki.makeUB("delete"), "title", title, "reason", reason, "token", wiki.token);
		return r != null && !r.hasErrorIfIgnore("missingtitle");
	}

	/**
	 * Undelete a page. Requires sysop privileges on wiki.
	 * 
	 * @param wiki The wiki object to use
	 * @param title The title to undelete
	 * @param reason The reason to use
	 * @return True if we successfully undeleted the page.
	 */
	protected static boolean undelete(Wiki wiki, String title, String reason)
	{
		ColorLog.info(wiki, "Restoring " + title);
		Reply r = doAction(wiki, wiki.makeUB("undelete"), "title", title, "reason", reason, "token", wiki.token);
		return r != null && !r.hasError();
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
				ColorLog.log(wiki, String.format("(%s): Uploading chunk %d of %d", filename, i + 1, chunks), Level.INFO,
						ColorLog.PURPLE);

				args.put("offset", "" + offset);
				if (filekey != null)
					args.put("filekey", filekey);

				Reply r = CRequest.chunkPost(ub.makeURL(), wiki.cookiejar, args, filename, fc);

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
	 * Unstash a file uploaded via chunked uploads.
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
		Reply r = doAction(wiki, wiki.makeUB("upload"), "filename", title, "text", text, "comment", reason, "token",
				wiki.token, "filekey", filekey, "ignorewarnings", "true");
		return r != null && r.resultIs("Success");
	}
}