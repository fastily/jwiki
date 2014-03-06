package fbot.lib.core;

import java.util.Random;

import fbot.lib.commons.CStrings;
import fbot.lib.commons.Commons;
import fbot.lib.commons.WikiGen;
import fbot.lib.util.FGUI;


public class OXL
{	

	public static void main(String[] args) throws Throwable
	{
		
		Constants.debug = true;
		String text = "{{Sandbox}}\n<!-- Please edit only below this line. -->\n" + new Random().nextInt();
		W wiki = WikiGen.generate("FSV");
		wiki.edit("Commons:Sandbox", text, "Test");
		
		//W wiki = FGUI.login();
		//wiki.edit("User:Fastily/VX", "It worked!", " y");	

		//Commons.nukeContribs("COCAwu", CStrings.oos);
		
		
		//W wiki = WikiGen.generate("FSV");
		//System.out.println(wiki.whichNS("Fastily"));
		
		//for(String s : wiki.getUserUploads("Netherzone"))
		//	wiki.edit(s, wiki.getPageText(s) + "\n{{Subst:npd}}", "npd");
		
		
		//Commons.addText("npd", "{{Subst:npd}}", wiki.getUserUploads("Eduardo paredes ortega"));
	}
}