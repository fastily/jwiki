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

	public static final Wiki fastily = WikiGen.generate("Fastily");

	public static final Wiki clone = WikiGen.generate("FastilyClone");

	public static final Commons com = new Commons(clone, fastily);

	public static void main(String[] args)
	{

	}
}