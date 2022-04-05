package io.github.fastily.jwiki.dwrap;

import java.time.Instant;

import com.google.gson.annotations.SerializedName;

/**
 * Structured data template class.
 * 
 * @author Fastily
 *
 */
public abstract class DataEntry
{
	/**
	 * The name of the user who made the contribution.
	 */
	public String user;

	/**
	 * Title and edit summary.
	 */
	public String title;

	/**
	 * The edit summary used in this contribution.
	 */
	@SerializedName("comment")
	public String summary;

	/**
	 * The date and time at which this edit was made.
	 */
	public Instant timestamp;

	/**
	 * Constructor, creates a DataEntry with all null fields.
	 */
	protected DataEntry()
	{

	}

	/**
	 * Gets a String representation of this DataEntry. Useful for debugging.
	 */
	public String toString()
	{
		return String.format("[ user : %s, title : %s, summary : %s, timestamp : %s ]", user, title, summary, timestamp);
	}
}