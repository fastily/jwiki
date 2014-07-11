package jwiki.core;

import jwiki.util.WikiGen;

public class Test
{
	public static void main(String[] args)
	{
		Wiki wiki = WikiGen.generate("FastilyClone");
		
		for(String s : wiki.getUserUploads("Rashaad (Entertainer)"))
			wiki.addText(s, "\n{{Subst:Npd}}", "p", false);
		
		//Settings.debug = true;
		//System.out.println(wiki.getPageText("User:Fastily"));
	}
}