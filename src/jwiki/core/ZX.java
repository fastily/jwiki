package jwiki.core;

import java.nio.file.Paths;

import jwiki.commons.Commons;
import jwiki.util.WikiGen;

public class ZX
{

	public static final Wiki fastily = WikiGen.generate("Fastily");

	public static final Wiki clone = WikiGen.generate("FastilyClone");

	public static final Commons com = new Commons(clone, fastily);

	public static void main(String[] args)
	{

	}
}