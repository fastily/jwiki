package fastily.jwiki.dwrap;

import java.time.Instant;

import com.google.gson.JsonObject;

import fastily.jwiki.util.GSONP;

/**
 * Represents an entry obtained from the <code>protectedtitles</code> API module.
 * 
 * @author Fastily
 *
 */
public final class ProtectedTitleEntry extends DataEntry
{
	/**
	 * The protection level
	 */
	public final Level level;

	/**
	 * Creates a new ProtectedTitleEntry entry
	 * 
	 * @param r A Reply Object to parse into a ProtectedTitleEntry.
	 */
	public ProtectedTitleEntry(JsonObject r)
	{
		super(GSONP.gString(r, "user"), GSONP.gString(r, "title"), GSONP.gString(r, "comment"), Instant.parse(GSONP.gString(r, "timestamp")));

		switch (GSONP.gString(r, "level"))
		{
			case "sysop":
				level = Level.SYSOP;
				break;
			case "autoconfirmed":
				level = Level.AUTOCONFIRMED;
				break;
			case "extendedconfirmed":
				level = Level.EXTENDEDCONFIRMED;
				break;
			case "templateeditor":
				level = Level.TEMPLATEEDITOR;
				break;
			default:
				level = Level.EVERYONE;
				break;
		}
	}

	/**
	 * Represents a protection level for a title.
	 * 
	 * @author Fastily
	 *
	 */
	public static enum Level
	{
		/**
		 * Represents a page which may only be edited by administrators.
		 */
		SYSOP,

		/**
		 * Represents a page which may only be edited by <code>autoconfirmed</code> (or more privileged) users.
		 */
		AUTOCONFIRMED,

		/**
		 * Represents a page which may only be edited by <code>extendedconfirmed</code> (or more privileged) users.
		 */
		EXTENDEDCONFIRMED,

		/**
		 * Represents a page which may only be edited by users with the <code>templateeditor</code> right or admins.
		 */
		TEMPLATEEDITOR,

		/**
		 * Represents a page which may be edited by all users.
		 */
		EVERYONE;
	}
}