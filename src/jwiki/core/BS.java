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
		System.out.println(wiki.getWiki("en.wikipedia.org").getPageText("Wikipedia:Sandbox"));

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