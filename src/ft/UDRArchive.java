package ft;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jwiki.core.Wiki;
import jwiki.util.WikiGen;

/**
 * A tiny COM:UD archiving bot.
 * 
 * @author Fastily
 * 
 */
public class UDRArchive
{
	/**
	 * Main driver.
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		Wiki wiki = WikiGen.generate("ArchiveBot");
		String target = "Commons:Undeletion requests/Current requests";
		String text = wiki.getPageText(target);
		Matcher m = Pattern.compile("(?si)\\s*?\\{\\{(udelh)\\}\\}.+?\\{\\{(udelf)\\}\\}\\s*?").matcher(text);
		
		String dump = "";
		int cnt = 0;
		while (m.find())
		{
			dump += text.substring(m.start(), m.end());
			cnt++;
		}
		
		String summary = "Archiving %d thread(s) %s [[%s]]";
		String archive = new SimpleDateFormat("'Commons:Undeletion requests/Archive/'yyyy-MM").format(new Date());
		
		if (wiki.exists(archive))
			dump = wiki.getPageText(archive) + dump;
		
		wiki.edit(archive, dump, String.format(summary, cnt, "from", target));
		wiki.edit(target, m.replaceAll(""), String.format(summary, cnt, "to", archive));
	}
}