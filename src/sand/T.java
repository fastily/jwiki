package sand;

import java.io.File;

import jwiki.commons.*;
import jwiki.core.*;
import jwiki.util.*;

public class T
{
	public static final Wiki fastily = WikiGen.generate("Fastily");

	public static final Wiki clone = WikiGen.generate("FastilyClone");

	public static final Commons com = new Commons(clone, fastily);

	public static void main(String[] args) throws Throwable
	{
		/*
		String x = "";
		for (String s : fastily.getCategoryMembers("Category:Pending fair use deletes", "File"))
		{
			clone.replaceText(
					s,
					CStrings.delregex,
					String.format("{{delete|reason=Originally flagged for speedy deletion as non-free/fair-use. "
							+ "Converted to DR.|subpage=%s|day=29|month=05|year=2014}}\n", s),
					"convert to DR");
			clone.edit("Commons:Deletion requests/" + s, String.format("===[[:%s]]===\nOriginally flagged for speedy deletion as non-free/fair-use. ~~~~", s), "start");
		   x += String.format("\n{{%s}}", "Commons:Deletion requests/" + s);
		}

		clone.addText("Commons:Deletion requests/2014/05/29", x, "++", false);*/
		
		//com.nuke("Page dependent on deleted or non-existent content", fastily.prefixIndex("Template", "WTFPL-1/"));
		//com.nuke("[[Commons:Deletion requests/File:Luftbild Grindelhochhäuser Hamburg.jpg]]", fastily.whatTranscludesHere("User:Heinz-Josef Lücking/Creative Commons by-sa-3.0 de"));
		//clone.upload(new File("/Users/Alec/Desktop/a.svg"), "File:Test.svg", new ReadFile("/Users/Alec/Desktop/a.txt").getTextAsBlock(), "Reset");
	
	Wiki wiki = clone.getWiki("en.wikipedia.org");
	for(String s : wiki.getCategoryMembers("Category:Wikipedia files with the same name on Wikimedia Commons as of unknown date", "File"))
		wiki.replaceText(s, "(?si)\\{\\{(nowcommons|now commons|db\\-f8).*?\\}\\}", "{{Subst:ncd}}", "+timestamp");
	
	}
}