package fastily.jwiki.dwrap;

import java.time.Instant;

import fastily.jwiki.core.Reply;

/**
 * Represents a Recent Changes entry.
 * 
 * @author Fastily
 *
 */
public class RCEntry extends DataEntry
{
	/**
	 * The type of entry this RCEntry represents (e.g. log, edit, new)
	 */
	public final String type;

	/**
	 * Constructor, creates a Recent Changes Entry based on a Reply from the server.
	 * 
	 * @param r The Reply object containing Recent
	 */
	public RCEntry(Reply r)
	{
		super(r.getStringR("user"), r.getStringR("title"), r.getStringR("comment"), Instant.parse(r.getStringR("timestamp")));
		type = r.getStringR("type");
	}
}