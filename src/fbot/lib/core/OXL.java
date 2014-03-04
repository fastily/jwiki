package fbot.lib.core;

import fbot.lib.commons.CStrings;
import fbot.lib.commons.Commons;
import fbot.lib.commons.WikiGen;
import fbot.lib.util.FGUI;


public class OXL
{	

	public static void main(String[] args) throws Throwable
	{
		
		//Constants.debug = true;
		//W wiki = FGUI.login();
		//wiki.edit("User:Fastily/VX", "It worked!", " y");	

		//Commons.nukeContribs("COCAwu", CStrings.oos);
		
		
		W wiki = WikiGen.generate("FSV");
		System.out.println(wiki.whichNS("Fastily"));
		
		
		//Commons.addText("npd", "{{Subst:npd}}", wiki.getUserUploads("Eduardo paredes ortega"));
	}
}