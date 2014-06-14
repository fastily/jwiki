package jwiki.core;

import java.net.HttpCookie;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import jwiki.util.FIO;
import jwiki.util.WikiGen;

public class XF
{
	public static Wiki wiki = WikiGen.generate("FastilyClone");
	
	public static void main(String[] args) throws Throwable
	{

		String cauth = null; //FQuery.getToken(wiki, "centralauth");
		String origin = "https://commons.wikimedia.org/";
		
		URL u = new URL("https://en.wikipedia.org/w/api.php?action=tokens&format=json&type=edit&origin=" + origin + "&centralauthtoken=" + cauth);
		URLConnection c = u.openConnection();
		loadCookies(c);
		
		c.setRequestProperty("Origin", origin);
		c.setRequestProperty("User-Agent", Settings.useragent);
		c.setRequestProperty(" Access-Control-Allow-Origin", origin);
	
		c.connect();
		System.out.println(FIO.inputStreamToString(c.getInputStream(), true));
		
		for(Map.Entry<String, List<String>> e : c.getHeaderFields().entrySet())
		{
			System.out.printf("%n%s:%n", e.getKey());
			for(String s : e.getValue())
				System.out.println(s);
		}
	}
	
	public static void loadCookies(URLConnection c)
	{
		String cookie = "";
		for (HttpCookie hc : wiki.cookiejar.getCookieStore().getCookies())
			cookie += hc.toString() + ";";
		
		c.setRequestProperty("Cookie", cookie);
	}
}