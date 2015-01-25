package jwiki.core;

import java.util.ArrayList;
import java.util.Arrays;

import jwiki.dwrap.ImageInfo;
import jwiki.dwrap.Revision;
import jwiki.util.Tuple;

public class A
{
	public static void main(String[] args) throws Throwable
	{
		
		final Wiki wiki = new Wiki("Fastily", "");
		
		ArrayList<String> l = new ArrayList<>();
		for(int i = 0; i < 80; i++)
			l.add(String.format("User:FastilyClone/BXC%d", i));
		
		
		CAction.delete(wiki, "User requested deletion in own [[Commons:User pages|userspace]]", l);
		/*
		for(String s : l)
		  new Thread(() -> { 
			  for(int x = 0; x < 1000; x++)
			    wiki.addText(s, "\n" + x, "" + x, true);
		  }).start();
		
		*/
	//	for(Tuple<String, ImageInfo> ix : MQuery.getImageInfo(wiki, -1, -1, new ArrayList<>(Arrays.asList("File:Disneyland 19 2015-01-11.JPG", "File:Target Balboa 1 2015-01-11.JPG"))))
		
//			System.out.println(ix.x);
	
		for(Revision r : wiki.getRevisions("User:Fastily", 5, false))
			System.out.println(r.summary);
		
		
			
	//	System.out.println(x.redirectsTo);
	//	System.out.println(x.size);
		
		
		
		
		
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