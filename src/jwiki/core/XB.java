package jwiki.core;

import jwiki.util.WikiGen;

public class XB
{
	public static void main(String[] args)
	{
		//Settings.debug = true;
		Wiki wiki = WikiGen.generate("FastilyClone");
		System.out.println(wiki.token);
		Wiki wiki2 = wiki.getWiki("en.wikipedia.org");
		System.out.println(wiki2.token);
		
		wiki.edit("User:FastilyClone/AX", "Testing4", "t");
		wiki2.edit("User:FastilyClone/AX", "Testing4", "t");
		
	}
}