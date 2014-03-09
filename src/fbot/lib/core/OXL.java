package fbot.lib.core;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Random;

import fbot.ft.Relinker;
import fbot.lib.commons.CStrings;
import fbot.lib.commons.Commons;
import fbot.lib.commons.WikiGen;
import fbot.lib.mbot.WAction;
import fbot.lib.util.FGUI;
import fbot.lib.util.ReadFile;


public class OXL
{	
	public static void main(String[] args) throws Throwable
	{
	
		//Constants.debug = true;
		
	    //Commons.emptyCatDel(Commons.com.exists(Commons.com.getLinksOnPage("User:Dschwen/emptycats", "Category"), true));
		
		//Commons.removeDelete("-", new ReadFile("/Users/Alec/Desktop/a.txt").getList());
		
		W wiki = WikiGen.generate("Fastily");
		for(String s : Commons.com.getCategoryMembers(CStrings.osd, "User"))
			if(s.contains("Biopics"))
				wiki.delete(s, CStrings.uru);
		
		//Relinker.main(Commons.com.getLinksOnPage("Commons:Deletion requests/Files uploaded by Mlucan", "File"));
		//System.out.println(Commons.com.getToken());
		
		//Commons.com.upload(new File("/Users/Alec/Desktop/3.jpg"), "File:Basgen-scorpion-compared.JPG", new ReadFile("/Users/Alec/Desktop/3.txt").getTextAsBlock(), "");
		
		//Commons.com.edit("User:FSV/A38", "1", "-");
		//WikiGen.generate("Fastily").delete("User:FSV/A38", CStrings.uru);
		
		/*
		URL url = new URL("http://bots.wmflabs.org/~richs/commonshelper.php");
		String s = Tools.inputStreamToString(Request.genericPost(url, null, Request.urlenc, "language=en&image=Violet_flower.jpg&newname=Violet_flower.jpg&project=wikipedia&username=&commonsense=1&tusc_user=&tusc_password=&doit=Get+text&test=%2F"), true);
		String x = s.substring(s.indexOf("{{Info"), s.indexOf("</textarea>"));
		System.out.println(x);
		*/
		
	//	System.out.println(URLDecoder.decode("language=en&image=Violet_flower.jpg&newname=Violet_flower.jpg&project=wikipedia&username=&commonsense=1&tusc_user=&tusc_password=&doit=Get+text&test=%2F", "UTF-8"));
		
		
		/*
		ArrayList<WAction> l = new ArrayList<WAction>();
		for(String s : Commons.com.exists(Commons.com.getLinksOnPage("Commons:Deletion requests/Files uploaded by Mlucan", "File"), false))
			l.add(new WAction(s, null, null) {
				@Override
				public boolean doJob(W wiki)
				{
					return wiki.undelete(getTitle(), "x");
				}
			});
		
		Commons.doAction("Fastily", l.toArray(new WAction[0]));		
		*/
		
		//Constants.debug = true;
/*		
		W wiki = WikiGen.generate("Fastily");
		for(int i = 61; i < 69; i++)
			wiki.undelete(String.format("File:Siene River %d 2012-07-01.jpg", i), "-");
		*/
		//wiki.delete("User talk:Fastily/2013", CStrings.uru);
		
		//wiki.edit("Commons:Sandbox", text, "Test");
		
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