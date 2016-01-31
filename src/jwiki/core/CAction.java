package jwiki.core;

import java.util.ArrayList;

import jwiki.util.FL;
import jwiki.util.PTask;
import jwiki.util.Tuple;

/**
 * Static methods for performing concurrent actions on a Wiki.
 * 
 * @author Fastily
 * 
 */
public final class CAction
{
	/**
	 * Constructors disallowed
	 */
	private CAction()
	{

	}

	/**
	 * Deletes pages concurrently. The account performing this task requires administrator rights.
	 * 
	 * @param wiki The Wiki object to use.
	 * @param reason The deletion log summary to use.
	 * @param titles The titles to delete.
	 * @return Pages which were not successfully deleted.
	 */
	public static ArrayList<String> delete(Wiki wiki, String reason, ArrayList<String> titles)
	{
		return PTask.execute(FL.toAL(titles.stream().map(t -> new Tuple<>(t, () -> WAction.delete(wiki, t, reason)))));
	}

	/**
	 * Undelete pages concurrently. The account performing this task requires administrator rights.
	 * 
	 * @param wiki The Wiki object to use
	 * @param agressive Set True to attempt restoration 10 times before giving up.
	 * @param reason The log reason to use
	 * @param titles The titles to restore.
	 * @return Pages which were not successfully restored.
	 */
	public static ArrayList<String> undelete(Wiki wiki, boolean agressive, String reason, ArrayList<String> titles)
	{
		return PTask.execute(FL.toAL(
				MQuery.exists(wiki, false, titles).stream().map(t -> new Tuple<>(t, () -> WAction.undelete(wiki, t, reason, agressive)))));
	}

	/**
	 * Edits pages to append or prepend some text concurrently.
	 * 
	 * @param wiki The Wiki object to use
	 * @param append Set True to append text or False to prepend text. WARNING: Newline characters will not be
	 *           automatically inserted.
	 * @param add The text to add
	 * @param reason The edit summary to use
	 * @param titles The pages to edit
	 * @return Pages which were not successfully edited.
	 */
	public static ArrayList<String> addText(Wiki wiki, boolean append, String add, String reason, ArrayList<String> titles)
	{
		return PTask.execute(FL.toAL(titles.stream().map(t -> new Tuple<>(t, () -> WAction.addText(wiki, t, add, reason, append)))));
	}

	/**
	 * Concurrently performs text replacement on pages.
	 * 
	 * @param wiki The Wiki object to use
	 * @param replace A regex matching the text on each page to replace.
	 * @param replacement The text to replace any text matching <code>replace</code>.
	 * @param reason The edit summary to use
	 * @param titles The pages to edit
	 * @return Pages which were not successfully edited.
	 */
	public static ArrayList<String> replace(Wiki wiki, String replace, String replacement, String reason, ArrayList<String> titles)
	{
		return PTask.execute(FL.toAL(titles.stream().map(t -> new Tuple<>(t, () -> wiki.replaceText(t, replace, replacement, reason)))));
	}
}