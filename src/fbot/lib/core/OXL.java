package fbot.lib.core;

import fbot.lib.commons.WikiGen;
import fbot.lib.util.FGUI;


public class OXL
{	

	public static void main(String[] args) throws Throwable
	{
		
		Constants.debug = true;
		//W wiki = FGUI.login();
		//wiki.edit("User:Fastily/VX", "It worked!", " y");	
		
		W wiki = WikiGen.generate("FSV");
		System.out.println(wiki.getNS(-1));
		System.out.println(wiki.getNS(8));
		System.out.println(wiki.getNS("File"));
		System.out.println(wiki.getNS(7));
	}
}