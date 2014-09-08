package jwiki.core;

import java.util.ArrayList;

import jwiki.util.FString;
import jwiki.util.Tuple;

public class BS
{
	public static void main(String[] args) throws Throwable
	{
		//Settings.debug = true;
		/*URL u = new URL("https://commons.wikimedia.org/w/api.php?action=query&prop=categories&format=json&cllimit=3&titles=User%3AFastily&continue=");
		
		ServerReply r = ClientRequest.get(u, null);
		System.out.println(r.toString(2));
		
		System.out.println("----\n\n\n\n\n----");
		
		URL u2 = new URL("https://commons.wikimedia.org/w/api.php?action=query&prop=categories&format=json&cllimit=3&titles=User%3AFastily&continue=" + FString.enc("||") + "&clcontinue="  + FString.enc("25579206|User_en-N"));
		ServerReply r2 = ClientRequest.get(u2, null);
		System.out.println(r2.toString(2));*/
		
		Wiki wiki = new Wiki("FastilyClone", "");
		/*URLBuilder ub = wiki.makeUB("query", "list", "categorymembers", "cmlimit", "max", "cmtitle", FString.enc("Category:Copyright violations"));
		
		for(String s : QueryTools.doListQueryAndParse(wiki, ub, "categorymembers", "title"))
			System.out.println(s);
		
		*/
		/*
		URLBuilder ub = wiki.makeUB("query", "list", "allimages", "aiuser", "Fastily", "aisort", "timestamp");
		
		int i = 0;
		for(String s : QueryTools.limitedQueryAndStringParse(wiki, ub, "ailimit", 200, "allimages", "title"))
			System.out.println("" + ++i + ":" + s);*/
		/*
		URLBuilder ub = wiki.makeUB("query", "prop", "pageprops");
		for(ServerReply r : QueryTools.doGroupQuery(wiki, ub, "User talk:Fastily", "User:Fastily", "COM:AN"))
			System.out.println(r.toString(2));*/
		
		Settings.debug = true;
		URLBuilder ub = wiki.makeUB("query", "prop", "links", "titles", FString.enc("User talk:Fastily|User:Fastily"));
		for(Tuple<String, ArrayList<String>> t : QueryTools.multiItemQueryStringParse(wiki, ub, "pllimit", "links", "title", "title"))
		{
			int i = 0;
			System.out.println("\n\n== DOING : " + t.x + " ==");
			for(String s : t.y)
				System.out.println("" + ++i + ") " + s);
		}
		
	}
}