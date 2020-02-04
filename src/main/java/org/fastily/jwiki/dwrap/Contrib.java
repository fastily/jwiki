package org.fastily.jwiki.dwrap;

/**
 * Represents a contribution made by a user.
 * 
 * @author Fastily
 *
 */
public class Contrib extends DataEntry
{
	/**
	 * This contribution's revision id.
	 */
	public long revid;

	/**
	 * This contribution's parent ID
	 */
	public long parentid;

	/**
	 * Constructor, creates a Contrib with all null fields.
	 */
	protected Contrib()
	{

	}
}