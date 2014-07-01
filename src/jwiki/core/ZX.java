package jwiki.core;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import jwiki.commons.Commons;
import jwiki.util.FIO;
import jwiki.util.FSystem;
import jwiki.util.ReadFile;
import jwiki.util.Tuple;
import jwiki.util.WikiGen;

public class ZX
{	
	/*
	public static final Wiki fastily = WikiGen.generate("Fastily");

	public static final Wiki clone = WikiGen.generate("FastilyClone");

	public static final Commons com = new Commons(clone, fastily);
	*/
	public static void main(String[] args)
	{
		//Settings.debug = true;
		/*
		Wiki wiki = WikiGen.generate("FastilyClone");
		System.out.println(wiki.token);
		Wiki wiki2 = wiki.getWiki("en.wikipedia.org");
		System.out.println(wiki2.token);
		
		wiki.edit("User:FastilyClone/AX", "Testing4", "t");
		wiki2.edit("User:FastilyClone/AX", "Testing4", "t");*/
		
		ArrayList<Path> pl =  FIO.findFiles(Paths.get("/Users/Alec/Desktop/DUMP"));
		for(Path p :pl)
			System.out.println(p);
		
		System.out.println("\n\n" + pl.size());
	}
}