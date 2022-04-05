package io.github.fastily.jwiki.dwrap;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a single revision in the history of a page.
 * 
 * @author Fastily
 * 
 */
public class Revision extends DataEntry
{
	/**
	 * The unique id associated with this revision.
	 */
	public long revid;

	/**
	 * The text of this revision
	 */
	@SerializedName("*")
	public String text;

	/**
	 * Constructor, creates a Revision with all null fields.
	 */
	protected Revision()
	{

	}
}