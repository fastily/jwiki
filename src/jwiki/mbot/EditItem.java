package jwiki.mbot;

import jwiki.core.Logger;
import jwiki.core.Wiki;

/**
 * Simple implementation of an item to edit, using MBot.
 * 
 * @author Fastily
 * 
 */
public class EditItem extends WAction
{
	/**
	 * The text to add
	 */
	private String add;

	/**
	 * The text to replace, as a regex
	 */
	private String replace;

	/**
	 * The replacement text. Only used if <tt>replace</tt> is != null
	 */
	private String replacement;

	/**
	 * Constructor, sets params
	 * 
	 * @param title The title to use
	 * @param reason The edit summary
	 * @param add The text to add. Optional -- set to null to exclude
	 * @param replace The text to replace, in regex form. Optional -- set to null to exclude
	 * @param replacement The replacement text. Optional -- depends on <tt>replace</tt> being != null.
	 */
	public EditItem(String title, String reason, String add, String replace, String replacement)
	{
		super(title, null, reason);
		this.add = add;
		this.replace = replace;
		this.replacement = replacement;
	}

	/**
	 * Does the edit, as specified by WAction.
	 * 
	 * @param wiki The wiki object to use.
	 * @return True if we were successful.
	 */
	public boolean doJob(Wiki wiki)
	{
		text = wiki.getPageText(title);
		if (text == null)
			return false;

		if (replace == null && add != null) // simple append text
			return wiki.edit(title, text + add, summary);
		else if (replace != null && replacement != null && add != null) // replace & append
			return wiki.edit(title, text.replaceAll(replace, replacement) + add, summary);
		else if (replace != null && replacement != null) // replace only
			return wiki.edit(title, text.replaceAll(replace, replacement), summary);
		else
			// all null, or replace != null && replacement == null
			Logger.error(String.format("For '%s', why is everything null?", title));

		return false;
	}
}