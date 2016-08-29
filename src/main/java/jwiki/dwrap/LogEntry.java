package jwiki.dwrap;

import java.time.Instant;

import jwiki.core.Reply;

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
	public LogEntry(Reply r)
	{
		super(r.getString("user"), r.getString("title"), r.getString("comment"), Instant.parse(r.getString("timestamp")));
		type = r.getString("type");
		action = r.getString("action");
	}
}