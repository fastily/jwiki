package jwiki.core;

import static jwiki.core.MBot.Task;

import java.util.ArrayList;

import jwiki.util.FError;

/**
 * Contains methods to perform concurrent actions on a wiki. Most methods return a set of values indicating whether we
 * were successful or not in performing the requested action(s).
 * 
 * @author Fastily
 * 
 */
public class CAction
{

	/**
	 * Hiding from javadoc
	 */
	private CAction()
	{

	}

	/**
	 * Deletes pages. Maximum concurrent threads = 20.
	 * 
	 * @param wiki The wiki object to use
	 * @param reason The log summary to use
	 * @param titles The page(s) to delete.
	 * @return A list of pages we didn't delete.
	 */
	public static ArrayList<String> delete(Wiki wiki, String reason, ArrayList<String> titles)
	{
		ColorLog.fyi(wiki, "Preparing to delete pages");
		ArrayList<Task> tl = new ArrayList<>();
		for (String s : titles)
			tl.add(new Task(s, null, reason) {
				public boolean doJob(Wiki wiki)
				{
					return WAction.delete(wiki, title, reason);
				}
			});
		return Task.toString(wiki.submit(tl, 20));
	}

	/**
	 * Undelete pages. Maximum concurrent threads = 5.
	 * 
	 * @param wiki The wiki object to use
	 * @param agressive Set to true to attempt restoration 10 times before giving up. MediaWiki isn't very good at
	 *           restoration.
	 * @param reason The log reason to use
	 * @param titles The page(s) to delete
	 * @return A list of pages we didn't delete.
	 */
	public static ArrayList<String> undelete(Wiki wiki, boolean agressive, String reason, ArrayList<String> titles)
	{
		ColorLog.fyi(wiki, "Preparing to restore pages");
		ArrayList<Task> tl = new ArrayList<>();
		for (String s : titles)
			tl.add(new Task(s, null, reason) {
				public boolean doJob(Wiki wiki)
				{
					return WAction.undelete(wiki, title, reason, agressive);
				}
			});
		return Task.toString(wiki.submit(tl, 5));
	}

	/**
	 * Adds text to a file
	 * 
	 * @param wiki The wiki object to use
	 * @param append Set to true to append text, set to false to prepend text. Newlines not automatically inserted.
	 * @param add The text to add
	 * @param reason The edit summary to use
	 * @param titles Pages to edit
	 * @return A list of titles we couldn't process.
	 */
	public static ArrayList<String> addText(Wiki wiki, boolean append, String add, String reason, ArrayList<String> titles)
	{
		return edit(wiki, append, reason, add, null, null, titles);
	}

	/**
	 * Replaces text on a page.
	 * 
	 * @param wiki The wiki object to use
	 * @param replace The text to replace, as a regex.
	 * @param replacement The replacement text for anything matching <code>replace</code>.
	 * @param reason The edit summary to use
	 * @param titles Pages to edit
	 * @return A list of titles we couldn't process.
	 */
	public static ArrayList<String> replace(Wiki wiki, String replace, String replacement, String reason,
			ArrayList<String> titles)
	{
		return edit(wiki, false, reason, null, replace, replacement, titles);
	}

	/**
	 * Edits a title. Allows for both add and/or replacement. Disable add and/or replace with null; you cannot disable
	 * both - you will get an error.
	 * 
	 * @param wiki The wiki object to use
	 * @param append Set to true to append text, set to false to prepend text. Newlines not automatically inserted. Param
	 *           is ignored if <code>add</code> is null.
	 * @param reason The edit summary to use
	 * @param add The text to add. Optional param: set to null to disable.
	 * @param replace The text to replace, as a regex. Optional param: set to null to disable
	 * @param replacement The replacement text for anything matching <code>replace</code>. Ignored if replace is null.
	 * @param titles Pages to edit
	 * @return A list of titles we couldn't process.
	 */
	public static ArrayList<String> edit(Wiki wiki, boolean append, String reason, String add, String replace,
			String replacement, ArrayList<String> titles)
	{
		ColorLog.fyi(wiki, "Preparing edit pages");
		ArrayList<Task> tl = new ArrayList<>();
		for (String s : titles)
			tl.add(new Task(s, null, reason) {
				public boolean doJob(Wiki wiki)
				{
					if (replace == null && add == null)
						return FError.printErrAndRet(String.format("CAction: Add and replace in '%s' are null!. Skip.", title),
								false);
					else if (replace == null)
						return WAction.addText(wiki, title, add, reason, append);
					else
					{
						if (replacement == null)
							return FError.printErrAndRet(
									String.format("CAction: Replace OP requested but replacement in '%s' is null!. Skip.", title),
									false);
						text = wiki.getPageText(title).replaceAll(replace, replacement);
						if (add != null)
							text = append ? text + add : add + text;
						return WAction.edit(wiki, title, text, reason, false);
					}
				}
			});
		return Task.toString(wiki.submit(tl, 3));
	}
}