package sand;

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
	}
}