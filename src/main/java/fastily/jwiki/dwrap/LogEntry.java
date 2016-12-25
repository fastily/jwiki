package fastily.jwiki.dwrap;

import java.time.Instant;

import com.google.gson.JsonObject;

import fastily.jwiki.util.GSONP;

/**
 * Represents a MediaWiki Log entry
 * 
 * @author Fastily
 *
 */
public class LogEntry extends DataEntry
{
	/**
	 * The log that this Log Entry belongs to. (e.g. 'delete', 'block')
	 */
	public final String type;

	/**
	 * The action that was performed in this log. (e.g. 'restore', 'revision')
	 */
	public final String action;

	/**
	 * Creates a new LogEntry
	 * 
	 * @param r The reply from the server, representing a LogEntry.
	 */
	public LogEntry(JsonObject r)
	{
		super(GSONP.gString(r, "user"), GSONP.gString(r, "title"), GSONP.gString(r, "comment"), Instant.parse(GSONP.gString(r, "timestamp")));
		type = GSONP.gString(r, "type");
		action = GSONP.gString(r, "action");
	}
}