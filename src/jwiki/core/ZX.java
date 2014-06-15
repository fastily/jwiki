package jwiki.core;

import jwiki.commons.Commons;
import jwiki.util.Tuple;
import jwiki.util.WikiGen;

public class ZX
{	
	public static final Wiki fastily = WikiGen.generate("Fastily");

	public static final Wiki clone = WikiGen.generate("FastilyClone");

	public static final Commons com = new Commons(clone, fastily);
	
	public static void main(String[] args)
	{
	
	
		for(Tuple<String, Boolean> t : clone.exists("User:Fastily", "User talk:Fastily", "Aalksdfjlakjdf", "COM:AN"))
			System.out.println(t.x + " exists? " + t.y);
			
		
		clone.getWiki("en.wikipedia.org").edit("User:FastilyClone/AX", "{{Db-u1}}", "u1");
		
		clone.edit("User:FastilyClone/BCC", "Hello", "Testing");
	}
}