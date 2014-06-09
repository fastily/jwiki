package jwiki.core;

import jwiki.commons.Commons;
import jwiki.util.ReadFile;
import jwiki.util.WikiGen;

import org.json.JSONObject;

public class XO
{
	public static final Wiki fastily = WikiGen.generate("Fastily");

	public static final Wiki clone = WikiGen.generate("FastilyClone");

	public static final Commons com = new Commons(clone, fastily);
	
	public static void main(String[] args) throws Throwable
	{
		
		
	}
}