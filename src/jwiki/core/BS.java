package jwiki.core;

import java.util.ArrayList;

import jwiki.util.Tuple;

public class BS
{
	public static void main(String[] args) throws Throwable
	{
		System.out.println(Settings.useragent);
		//System.out.println(System.getProperty("java.version"));
		 Wiki wiki = new Wiki("FastilyClone", "");
		 wiki.edit("User:FastilyClone/ABC", "Blah", "test");


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