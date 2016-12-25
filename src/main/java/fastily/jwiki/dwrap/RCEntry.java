package fastily.jwiki.dwrap;

import java.time.Instant;

import com.google.gson.JsonObject;

import fastily.jwiki.util.GSONP;

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
	public RCEntry(JsonObject r)
	{
		super(GSONP.gString(r, "user"), GSONP.gString(r, "title"), GSONP.gString(r, "comment"), Instant.parse(GSONP.gString(r, "timestamp")));
		type = GSONP.gString(r, "type");
	}
}