package jwiki.core;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;

import jwiki.dwrap.Revision;
import jwiki.util.FError;
import jwiki.util.FL;
import jwiki.util.FString;

/**
 * Performs an action on a wiki. Use of these functions is for advanced users who need more functionality than what is
 * available in the Wiki class. Most methods return some sort of value indicating whether we were successful or not in
 * performing the requested action.
 * 
 * @author Fastily
 * 
 */
public final class WAction
{
	/**
	 * The size of upload chunks. Default = 4Mb
	 */
	protected static final int chunksize = 1024 * 1024 * 4;

	/**
	 * Constructors disallowed
	 */
	private WAction()
	{

	}

	/**
	 * Performs an action on wiki requiring a simple POST request.
	 * 
	 * @param wiki The wiki object to use
	 * @param ub The URLBuilder to use
	 * @param params The parameters to post, in order of <code>[ param1, value1, param2, value2 ] </code>.
	 * @return A reply from the server or null if something went wrong.
	 */
	protected static Reply doAction(Wiki wiki, String action, HashMap<String, String> params)
	{
		try
		{
			return Req.post(wiki.makeUB(action).makeURL(), FString.makeURLParamString(FString.encValues(new HashMap<>(params))),
					wiki.cookiejar, Req.urlenc);
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
	 * @param wiki The wiki object to use.
	 * @param title The title to edit
	 * @param text The new page text to set <code>title</code> to.
	 * @param reason The edit summary to use
	 * @param agressive If true, program will try 10 times
	 * 
	 * @return True if the operation was successful.
	 */
	public static boolean edit(Wiki wiki, String title, String text, String reason, boolean agressive)
	{
		ColorLog.info(wiki, "Editing " + title);

		HashMap<String, String> pl = FL.pMap("title", title, "text", text, "summary", reason, "token", wiki.token);
		if (wiki.isBot)
			pl.put("bot", "");

		int attempt = agressive ? 5 : 1;
		for (int i = 0; i < attempt; i++)
		{
			if (agressive && i < 0)
				ColorLog.info(wiki, String.format("Editing '%s', trial: %d/%d", title, i, attempt));

			Reply r = doAction(wiki, "edit", pl);
			if (r != null && r.resultIs("Success"))
				return true;

			if (r.getErrorCode().equals("ratelimited"))
				try
				{
					ColorLog.warn(wiki, "Rate Limited! Sleeping 30 sec");
					Thread.sleep(30000);
				}
				catch (InterruptedException e)
				{

				}
		}
		return false;
	}

	/**
	 * Add text to a page by editing it. Use this instead of <code>edit()</code> if you're only planning on adding text
	 * to a page. If the page does not exist, then this function creates a new page with the text specified in
	 * <code>text</code>.
	 * 
	 * @param wiki The wiki object to use.
	 * @param title The title to edit
	 * @param text The new page text to set <code>title</code> to.
	 * @param reason The edit summary to use
	 * @param append Set to true to append text, else prepend
	 * @return True if the operation was successful.
	 */
	protected static boolean addText(Wiki wiki, String title, String text, String reason, boolean append)
	{
		ColorLog.info(wiki, "Adding text to " + title);

		HashMap<String, String> pl = FL.pMap("title", title, append ? "appendtext" : "prependtext", text, "summary", reason, "token",
				wiki.token);
		if (wiki.isBot)
			pl.put("bot", "");

		Reply r = doAction(wiki, "edit", pl);
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
		ColorLog.info(wiki, "Undoing top revision of " + title);
		ArrayList<Revision> rl = wiki.getRevisions(title, 2, false, null, null);
		return rl.size() < 2 ? FError.printErrAndRet("There are fewer than two revisions in " + title, false)
				: edit(wiki, title, rl.get(1).text, reason, true);
	}

	/**
	 * Deletes a page.
	 * 
	 * @param wiki The wiki object to use
	 * @param title The title to delete
	 * @param reason The edit summary to use
	 * @return True if we were successful.
	 */
	protected static boolean delete(Wiki wiki, String title, String reason)
	{
		ColorLog.info(wiki, "Deleting " + title);
		Reply r = doAction(wiki, "delete", FL.pMap("title", title, "reason", reason, "token", wiki.token));
		return r != null && !r.hasErrorIfIgnore("missingtitle");
	}

	/**
	 * Undelete a page
	 * 
	 * @param wiki The wiki object to use
	 * @param title The title to delete
	 * @param reason The edit summary to use
	 * @param agressive Set to true to attempt undelete maximum of 10 times in event of failure.
	 * @return True if we were successful.
	 */
	public static boolean undelete(Wiki wiki, String title, String reason, boolean agressive)
	{
		int attempt = agressive ? 10 : 1;
		for (int i = 0; i < attempt; i++)
		{
			if (agressive && i < 0)
				ColorLog.info(wiki, String.format("Undeleting '%s' trial: %d/%d", title, i, attempt));
			Reply r = doAction(wiki, "undelete", FL.pMap("title", title, "reason", reason, "token", wiki.token));
			if (r != null && !r.hasError())
				return true;
		}
		return false;
	}

	/**
	 * Purges page caches.
	 * 
	 * @param wiki The Wiki object to run the action against.
	 * @param titles The titles to purge
	 * @return A HashMap where each key is the title and each value indicates whether the title was successfully purged.
	 */
	public static HashMap<String, Boolean> purge(Wiki wiki, ArrayList<String> titles)
	{
		SQ sq = SQ.with(wiki, null, FL.pMap());
		sq.action = "purge";

		HashMap<String, Boolean> l = new HashMap<>();
		for (Reply r : sq.multiTitleQuery("titles", titles).getJAofJO("purge"))
			l.put(r.getString("title"), r.has("purged"));

		return l;
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
		String uploadTo = wiki.convertIfNotInNS(title, NS.FILE);
		String filename = p.getFileName().toString();
		String filekey = null;
		URLBuilder ub = wiki.makeUB("upload");

		try (FileChannel fc = FileChannel.open(p, StandardOpenOption.READ))
		{
			long filesize = Files.size(p);
			long chunks = filesize / chunksize + ((filesize % chunksize) > 0 ? 1 : 0);

			HashMap<String, String> args = FL.pMap("filename", wiki.nsl.nss(uploadTo), "token", wiki.token, "ignorewarnings", "true",
					"stash", "1", "filesize", "" + filesize);

			ColorLog.info(wiki, String.format("Uploading '%s' to '%s'", filename, title));

			for (long i = 0, offset = fc.position(), failcount = 0; i < chunks;)
			{
				ColorLog.log(wiki, String.format("(%s): Uploading chunk %d of %d", filename, i + 1, chunks), "INFO", ColorLog.PURPLE);

				args.put("offset", "" + offset);
				if (filekey != null)
					args.put("filekey", filekey);

				Reply r = Req.chunkPost(ub.makeURL(), wiki.cookiejar, args, filename, fc);

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
		Reply r = doAction(wiki, "upload", FL.pMap("filename", title, "text", text, "comment", reason, "token", wiki.token, "filekey",
				filekey, "ignorewarnings", "true"));
		return r != null && r.resultIs("Success");
	}
}