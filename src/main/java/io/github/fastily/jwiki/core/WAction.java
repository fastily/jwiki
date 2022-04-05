package io.github.fastily.jwiki.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.fastily.jwiki.util.FL;
import io.github.fastily.jwiki.util.GSONP;
import okhttp3.HttpUrl;
import okhttp3.Response;
import okio.BufferedSource;
import okio.Okio;

/**
 * Static methods to perform changes to a Wiki.
 * 
 * @author Fastily
 *
 */
class WAction
{
	/**
	 * All static methods, constructors disallowed.
	 */
	private WAction()
	{

	}

	/**
	 * {@code POST} an action
	 * 
	 * @param wiki The Wiki to work on.
	 * @param action The type of action to perform. This is the literal API action
	 * @param applyToken Set true to apply {@code wiki}'s edit token
	 * @param form The form data to post. This should not be URL-encoded
	 * @return True on success
	 */
	protected static ActionResult postAction(Wiki wiki, String action, boolean applyToken, HashMap<String, String> form)
	{
		HashMap<String, String> fl = FL.pMap("format", "json");
		if (applyToken)
			fl.put("token", wiki.conf.token);

		fl.putAll(form);

		try
		{
			JsonObject result = JsonParser.parseString(wiki.apiclient.basicPOST(FL.pMap("action", action), fl).body().string()).getAsJsonObject();
			if (wiki.debug)
				wiki.conf.log.debug(wiki, GSONP.gsonPP.toJson(result));

			return ActionResult.wrap(result, action);
		}
		catch (Throwable e)
		{
			return ActionResult.NONE;
		}
	}

	/**
	 * Adds text to a page.
	 * 
	 * @param wiki The Wiki to work on.
	 * @param title The title to edit.
	 * @param text The text to add.
	 * @param summary The edit summary to use
	 * @param append Set True to append text, or false to prepend text.
	 * @return True on success
	 */
	protected static boolean addText(Wiki wiki, String title, String text, String summary, boolean append)
	{
		wiki.conf.log.info(wiki, "Adding text to " + title);

		HashMap<String, String> pl = FL.pMap("title", title, append ? "appendtext" : "prependtext", text, "summary", summary);
		if (wiki.conf.isBot)
			pl.put("bot", "");

		return postAction(wiki, "edit", true, pl) == ActionResult.SUCCESS;
	}

	/**
	 * Edits a page.
	 * 
	 * @param wiki The Wiki to work on.
	 * @param title The title to edit
	 * @param text The text to replace the text of {@code title} with.
	 * @param summary The edit summary to use
	 * @return True on success.
	 */
	protected static boolean edit(Wiki wiki, String title, String text, String summary)
	{
		wiki.conf.log.info(wiki, "Editing " + title);

		HashMap<String, String> pl = FL.pMap("title", title, "text", text, "summary", summary);
		if (wiki.conf.isBot)
			pl.put("bot", "");

		for (int i = 0; i < 5; i++)
			switch (postAction(wiki, "edit", true, pl))
			{
				case SUCCESS:
					return true;
				case RATELIMITED:
					try
					{
						wiki.conf.log.fyi(wiki, "Ratelimited by server, sleeping 10 seconds");
						Thread.sleep(10000);
					}
					catch (Throwable e)
					{
						e.printStackTrace();
						return false;
					}
					break;
				case PROTECTED:
					wiki.conf.log.error(wiki, title + " is protected, cannot edit.");
					return false;

				default:
					wiki.conf.log.warn(wiki, "Got an error, retrying: " + i);
			}

		wiki.conf.log.error(wiki, String.format("Could not edit '%s', aborting.", title));
		return false;
	}

	/**
	 * Moves a page.
	 * 
	 * @param wiki The Wiki objec to use
	 * @param title The original title to move
	 * @param newTitle The new title to move the old page to
	 * @param moveTalk Flag indicating if {@code title}'s talk page (assuming it exists) should be moved. Optional, set false to disable.
	 * @param moveSubpages Flag indicating if {@code title}'s subpages should also be moved. Requires admin/pagemover rights, otherwise this does nothing. Optional, set false to disable.
	 * @param supressRedirect Flag indicating if a redirect to {@code newTitle} should be automatically generated at {@code title}. Requires admin/pagemover rights, otherwise this does nothing.
	 *           Optional, set false to disable.
	 * @param reason The edit summary to use
	 * @return True if the operation succeeded
	 */
	protected static boolean move(Wiki wiki, String title, String newTitle, boolean moveTalk, boolean moveSubpages, boolean supressRedirect, String reason)
	{
		wiki.conf.log.info(wiki, String.format("Moving %s to %s", title, newTitle));

		HashMap<String, String> pl = FL.pMap("from", title, "to", newTitle, "reason", reason);

		if (moveTalk)
			pl.put("movetalk", "1");
		if (moveSubpages)
			pl.put("movesubpages", "1");
		if (supressRedirect)
			pl.put("noredirect", "1");

		return postAction(wiki, "move", true, pl) == ActionResult.NONE;
	}

	/**
	 * Deletes a page. Wiki must be logged in and have administrator permissions for this to succeed.
	 * 
	 * @param wiki The Wiki to work on.
	 * @param title The title to delete
	 * @param reason The log summary to use
	 * @return True on success
	 */
	protected static boolean delete(Wiki wiki, String title, String reason)
	{
		wiki.conf.log.info(wiki, "Deleting " + title);
		return postAction(wiki, "delete", true, FL.pMap("title", title, "reason", reason)) == ActionResult.NONE;
	}

	/**
	 * Undelete a page. Wiki must be logged in and have administrator permissions for this to succeed.
	 * 
	 * @param wiki The Wiki to work on.
	 * @param title The title to delete
	 * @param reason The log summary to use
	 * @return True on success
	 */
	protected static boolean undelete(Wiki wiki, String title, String reason)
	{
		wiki.conf.log.info(wiki, "Restoring " + title);

		for (int i = 0; i < 10; i++)
			if (postAction(wiki, "undelete", true, FL.pMap("title", title, "reason", reason)) == ActionResult.NONE)
				return true;

		return false;
	}

	/**
	 * Purges the cache of pages.
	 * 
	 * @param wiki The Wiki to work on.
	 * @param titles The title(s) to purge.
	 */
	protected static void purge(Wiki wiki, ArrayList<String> titles)
	{
		wiki.conf.log.info(wiki, "Purging:" + titles);

		HashMap<String, String> pl = FL.pMap("titles", FL.pipeFence(titles));
		postAction(wiki, "purge", false, pl);
	}

	/**
	 * Uploads a file. Caution: overwrites files automatically.
	 * 
	 * @param wiki The Wiki to work on.
	 * @param title The title to upload the file to, excluding the {@code File:} prefix.
	 * @param desc The text to put on the newly uploaded file description page
	 * @param summary The edit summary to use when uploading a new file.
	 * @param file The Path to the file to upload.
	 * @return True on success.
	 */
	protected static boolean upload(Wiki wiki, String title, String desc, String summary, Path file)
	{
		wiki.conf.log.info(wiki, "Uploading " + file);

		try
		{
			ChunkManager cm = new ChunkManager(file);

			String filekey = null;
			String fn = file.getFileName().toString();

			Chunk c;
			while ((c = cm.nextChunk()) != null)
			{
				wiki.conf.log.fyi(wiki, String.format("Uploading chunk [%d of %d] of '%s'", cm.chunkCnt, cm.totalChunks, file));

				HashMap<String, String> pl = FL.pMap("format", "json", "filename", title, "token", wiki.conf.token, "ignorewarnings", "1", "stash", "1", "offset", "" + c.offset, "filesize",
						"" + c.filesize);
				if (filekey != null)
					pl.put("filekey", filekey);

				for (int i = 0; i < 5; i++)
					try
					{
						Response r = wiki.apiclient.multiPartFilePOST(FL.pMap("action", "upload"), pl, fn, c.bl);
						if (!r.isSuccessful())
						{
							wiki.conf.log.error(wiki, "Bad response from server: " + r.code());
							continue;
						}

						filekey = GSONP.getStr(JsonParser.parseString(r.body().string()).getAsJsonObject().getAsJsonObject("upload"), "filekey");
						if (filekey != null)
							break;
					}
					catch (Throwable e)
					{
						wiki.conf.log.error(wiki, "Encountered an error, retrying - " + i);
						e.printStackTrace();
					}
			}

			for (int i = 0; i < 3; i++)
			{
				wiki.conf.log.info(wiki, String.format("Unstashing '%s' as '%s'", filekey, title));

				if (postAction(wiki, "upload", true, FL.pMap("filename", title, "text", desc, "comment", summary, "filekey", filekey, "ignorewarnings", "true")) == ActionResult.SUCCESS)
					return true;

				wiki.conf.log.error(wiki, "Encountered an error while unstashing, retrying - " + i);
			}

			return false;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Upload a file by URL. The URL must be on the upload by url whitelist for the target Wiki or this method will automatically fail.
	 * 
	 * @param wiki The Wiki object to use
	 * @param url The URL the target file is located at.
	 * @param title The title to upload to.
	 * @param desc The text to put on the file description page
	 * @param summary The edit summary
	 * @return True if the upload was successful.
	 */
	protected static boolean uploadByUrl(Wiki wiki, HttpUrl url, String title, String desc, String summary)
	{
		wiki.conf.log.info(wiki, String.format("Uploading '%s' to '%s'", url, title));

		return postAction(wiki, "upload", true, FL.pMap("filename", title, "text", desc, "comment", summary, "ignorewarnings", "true", "url", url.toString())) == ActionResult.SUCCESS;
	}

	/**
	 * Represents the result of an action POSTed to a Wiki
	 * 
	 * @author Fastily
	 *
	 */
	protected static enum ActionResult
	{
		/**
		 * Used for success responses
		 */
		SUCCESS,

		/**
		 * Catch-all, used for unlisted/other errors
		 */
		ERROR,

		/**
		 * If no result could be determined.
		 */
		NONE,

		/**
		 * Error, if the request had an expired/invalid token.
		 */
		BADTOKEN,

		/**
		 * Error, if the request was missing a valid token.
		 */
		NOTOKEN,

		/**
		 * Error, if the user lacks permission to perform an action.
		 */
		PROTECTED,

		/**
		 * Error, if the action could not be completed due to being rate-limited by Wiki.
		 */
		RATELIMITED;

		/**
		 * Parses and wraps the response from a POST to the server in an ActionResult.
		 * 
		 * @param jo The json response from the server
		 * @param action The name of the action which produced this response. e.g. {@code edit}, {@code delete}
		 * @return An ActionResult representing the response result of the query.
		 */
		private static ActionResult wrap(JsonObject jo, String action)
		{
			try
			{
				if (jo.has(action))
					switch (GSONP.getStr(jo.getAsJsonObject(action), "result"))
					{
						case "Success":
							return SUCCESS;
						default:
							System.err.printf("Something isn't right.  Got back '%s', missing a 'result'?%n", GSONP.gson.toJson(jo));
					}
				else if (jo.has("error"))
					switch (GSONP.getStr(jo.getAsJsonObject("error"), "code"))
					{
						case "notoken":
							return NOTOKEN;
						case "badtoken":
							return BADTOKEN;
						case "cascadeprotected":
						case "protectedpage":
							return PROTECTED;
						default:
							return ERROR;
					}
			}
			catch (Throwable e)
			{

			}

			return NONE;
		}
	}

	/**
	 * Creates and manages Chunk Objects for {@link WAction#upload(Wiki, String, String, String, Path)}.
	 * 
	 * @author Fastily
	 *
	 */
	private final static class ChunkManager
	{
		/**
		 * The default chunk size is 4 Mb
		 */
		private static final int chunksize = 1024 * 1024 * 4;

		/**
		 * The source file stream
		 */
		private BufferedSource src;

		/**
		 * The current Chunk offset, in bytes
		 */
		private long offset = 0;

		/**
		 * The file size (in bytes) of the file being uploaded
		 */
		private final long filesize;

		/**
		 * The total number of Chunk objects to upload
		 */
		private final long totalChunks;

		/**
		 * Counts the number of chunks created so far.
		 */
		private int chunkCnt = 0;

		/**
		 * Creates a new Chunk Manager. Create a new ChunkManager for every upload.
		 * 
		 * @param fn The local file to upload
		 * @throws IOException I/O error.
		 */
		private ChunkManager(Path fn) throws IOException
		{
			filesize = Files.size(fn);
			src = Okio.buffer(Okio.source(fn, StandardOpenOption.READ));
			totalChunks = filesize / chunksize + ((filesize % chunksize) > 0 ? 1 : 0);
		}

		/**
		 * Determine if there are still Chunk objects to upload.
		 * 
		 * @return True if there are still Chunk objects to upload.
		 */
		private boolean has()
		{
			return offset < filesize;
		}

		/**
		 * Create and return the next sequential Chunk to upload.
		 * 
		 * @return The next sequential Chunk to upload, or null on error or if there are no more chunks to upload.
		 */
		private Chunk nextChunk()
		{
			if (!has())
				return null;

			try
			{
				Chunk c = new Chunk(offset, filesize, ++chunkCnt == totalChunks ? src.readByteArray() : src.readByteArray(chunksize));

				offset += chunksize;
				// chunkCnt++;

				if (!has())
					src.close();

				return c;

			}
			catch (Throwable e)
			{
				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * Represents an indidual chunk to upload
	 * 
	 * @author Fastily
	 *
	 */
	private final static class Chunk
	{
		/**
		 * The offset and filesize (both in bytes)
		 */
		protected final long offset, filesize;

		/**
		 * The raw binary data for this Chunk
		 */
		protected final byte[] bl;

		/**
		 * Creates a new Chunk to upload
		 * 
		 * @param offset The byte offset of this Chunk
		 * @param filesize The total file size of the file this Chunk belongs to
		 * @param bl The raw binary data contained by this chunk
		 */
		private Chunk(long offset, long filesize, byte[] bl)
		{
			this.offset = offset;
			this.filesize = filesize;

			this.bl = bl;
		}
	}
}