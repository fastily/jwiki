package jwiki.core;

import java.net.HttpCookie;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import jwiki.commons.Commons;
import jwiki.util.FIO;
import jwiki.util.WikiGen;

public class XO
{
	/*
	public static final Wiki fastily = WikiGen.generate("Fastily");

	public static final Wiki clone = WikiGen.generate("FastilyClone");

	public static final Commons com = new Commons(clone, fastily);
*/
	public static final 		Wiki wiki = WikiGen.generate("FastilyClone");
	
	
	public static void main(String[] args) throws Throwable
	{
		//fastily.getWiki("en.wikipedia.org");
		//fastily.getWiki("en.wikinews.org");
		
		//for(HttpCookie c : fastily.cookiejar.getCookieStore().getCookies())
		//	System.out.println(c);
			//System.out.printf("%s:%s%n")
		

		System.out.println("===========ORIGINAL COOKIES============");
		for(HttpCookie c : wiki.cookiejar.getCookieStore().getCookies())
			System.out.println(c);
		System.out.println("===========END ORIGINAL COOKIES============\n\n");
		
		//String origin = "https://commons.wikimedia.org/";
		
		URL u = new URL("https://en.wikipedia.org/w/api.php?action=tokens&format=json&type=edit");
		URLConnection c = u.openConnection();
		loadCookies(c);
		
		//c.setRequestProperty("Origin", origin);
		c.setRequestProperty("User-Agent", Settings.useragent);
		//c.setRequestProperty(" Access-Control-Allow-Origin", origin);
	
		c.connect();
		System.out.println("SERVER REPLIED WITH: " + FIO.inputStreamToString(c.getInputStream(), true));
		
		for(Map.Entry<String, List<String>> e : c.getHeaderFields().entrySet())
		{
			System.out.printf("%n%s:%n", e.getKey());
			for(String s : e.getValue())
				System.out.println(s);
		}
		
		
		System.out.println("=======\n\n\n\n\n\n\n\n");
		for(HttpCookie hc : wiki.cookiejar.getCookieStore().get(new URI("https://commons.wikimedia.org/w/api.php?action=tokens&format=json&type=edit")))
			System.out.println(hc);
		
		
		
		
	}
	
	public static void loadCookies(URLConnection c)
	{
		String cookie = "";
		for (HttpCookie hc : wiki.cookiejar.getCookieStore().getCookies())
		{
			String temp = hc.toString();
			//if(temp.contains("centralauth"))
				//cookie += temp.replace("centralauth_", "enwiki") + ";";
				cookie += temp + ";";
		}
		System.out.println(cookie);
		c.setRequestProperty("Cookie", cookie);
	}
}