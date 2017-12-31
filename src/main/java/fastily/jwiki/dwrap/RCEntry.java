package fastily.jwiki.dwrap;

/**
 * Represents a Recent Changes entry.
 * 
 * @author Fastily
 *
 */
public class RCEntry extends DataEntry
{
	/**
	 * The type of entry this RCEntry represents (ex: log, edit, new)
	 */
	public String type;

	/**
	 * Constructor, creates an RCEntry with all null fields.
	 */
	protected RCEntry()
	{

	}
}