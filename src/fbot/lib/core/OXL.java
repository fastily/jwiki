package fbot.lib.core;

import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fbot.lib.commons.CStrings;
import fbot.lib.commons.Commons;
import static fbot.lib.commons.Commons.*;
import fbot.lib.commons.WikiGen;
import fbot.lib.util.FString;
import fbot.lib.util.ReadFile;

public class OXL
{
	public static void main(String[] args) throws Throwable
	{
		String text = "language=en&project=wikipedia&image=14c+Nurnberg+thimble.jpg&newname=&username=&commonsense=1&tusc_user=&tusc_password=&doit=Get+text&test=1";
		URL u = new URL("http://tools.wmflabs.org/commonshelper/?");
		
		
		URLConnection c = u.openConnection();
		c.setDoOutput(true);
		//c.setRequestProperty("Host", "tools.wmflabs.org");
		c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		//c.setRequestProperty("Referer", "http://tools.wmflabs.org/commonshelper/?");
		//c.setRequestProperty("Content-Length", "133");
		c.setRequestProperty("User-Agent", "fpowertoys");
		//c.connect();
		//c.setRequestProperty("Accept-Encoding", "gzip, deflate");
		for (Map.Entry<String, List<String>> e : c.getRequestProperties().entrySet())
		{
			System.out.println("KEY: " + e.getKey());
			for (String s : e.getValue())
				System.out.println("\tVALUE: " + s);
			System.out.println();
		}
		
		OutputStreamWriter out = new OutputStreamWriter(c.getOutputStream(), "UTF-8");
		out.write(text);
		out.close();
		
		
		System.out.println(Tools.inputStreamToString(c.getInputStream(), true));
		System.out.println(Tools.inputStreamToString(Request.genericPost(u, null, Request.urlenc, text), true));
		
	}
	
}