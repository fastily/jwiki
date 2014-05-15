package sand;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

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
		String x = "File:ZEH 1987 21 PLZ 1 Berlin 3.jpg";
		for(String s : fastily.getDuplicatesOf(x))
			fastily.delete("File:"+ s, "Exact or scaled-down duplicate: " + String.format("[[:%s]]", x));
	}
}