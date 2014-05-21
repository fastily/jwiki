package sand;


import jwiki.commons.*;
import jwiki.core.*;
import jwiki.util.*;

public class T
{
	public static final Wiki fastily = WikiGen.generate("Fastily");
	
	public static final Wiki clone = WikiGen.generate("FastilyClone");
	
	public static final Commons com = new Commons(clone, fastily);
	
	public static void main(String[] args) throws Throwable
	{
	}
}