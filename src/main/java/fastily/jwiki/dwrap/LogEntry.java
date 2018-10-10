package fastily.jwiki.dwrap;

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
	public String type;

	/**
	 * The action that was performed in this log. (e.g. 'restore', 'revision')
	 */
	public String action;

	/**
	 * Constructor, creates a LogEntry with all null fields.
	 */
	protected LogEntry()
	{

	}
}