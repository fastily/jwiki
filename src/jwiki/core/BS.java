package jwiki.core;

import java.util.ArrayList;

import jwiki.util.Tuple;
import jwiki.util.WikiGen;

public class BS
{
	public static void main(String[] args) throws Throwable
	{
		System.out.println(Settings.useragent);
		
		 Wiki wiki = WikiGen.generate("FastilyClone");
		// wiki.edit("User:Fastily/OL", "hi", "test");
		// System.out.println(wiki.delete("User:Fastily/OL", "User requested deletion in own [[Commons:Userpage|userspace]]"));

		 //printTAL(MQuery.fileUsage(wiki, FString.toSAL("File:San Diego Zoo Safari Park 150 2014-08-29.JPG", "File:Antarctica 6400px from Blue Marble.jpg")));

		 System.out.println(wiki.getWiki("en.wikipedia.org").getPageText("Wikipedia:Sandbox"));
		 
		System.out.println( wiki.getWiki("en.wikipedia.org").getPageText("WP:AN") );
		 
		 System.out.println(wiki.whichNS("File:Fastily.jpg"));
		 
		 //printA(MQuery.exists(wiki, true, wiki.getLinksOnPage("User:FastilyClone/UC")));
	}

	public static void printTAL(ArrayList<Tuple<String, ArrayList<String>>> l)
	{
		for (Tuple<String, ArrayList<String>> t : l)
		{
			System.out.printf("******** %s *********%n%n", t.x);
			for (String s : t.y)
				System.out.println(s);
			System.out.println();
		}
	}

	public static void printA(ArrayList<String> l)
	{
		for (String s : l)
			System.out.println(s);
	}
}