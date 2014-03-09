package fbot.ft;

import java.io.File;
import java.net.URL;

import fbot.lib.core.FTask;
import fbot.lib.core.Namespace;
import fbot.lib.core.Request;
import fbot.lib.core.Tools;
import fbot.lib.core.W;
import fbot.lib.core.aux.Logger;

public class MTC
{
	
	private static W wiki;
	
	private static W enwp;
	
	private static String posttext = "language=en&image=%s&newname=%s&project=wikipedia&username=&commonsense=1&"
			+ "tusc_user=&tusc_password=&doit=Get+text&test=%%2F";
	
	public static void main(String[] args)
	{
		wiki = new W("FSV", "goodboy1", "commons.wikimedia.org");
		enwp = new W("FSV", "goodboy1", "en.wikipedia.org");
		
		for (String s : args)
			doJob(s);
	}
	
	private static boolean doJob(String title)
	{
		if (wiki.exists(title))
		{
			String text = enwp.getPageText(title);
			if (text != null && text.matches("(?si).*?\\{\\{(db\\-f8)\\}\\}.*?"))
				return enwp.appendText(title, "\n{{db-f8}}", "f8");
		}
		
		File f = downloadFile(title);
		return f != null ? wiki.upload(f, title, getDesc(title), String.format("from [[w:%s|%s]]", title, title))
					&& enwp.appendText(title, "\n{{db-f8}}", "f8") : false;
	}
	
	public static File downloadFile(String title)
	{
		String path = "./" + Namespace.nss(title);
		return FTask.downloadFile(title, path, enwp) ? new File(path) : null;
	}
	
	private static String getDesc(String title)
	{
		Logger.fyi("Generating description page for " + title);
		try
		{
			String tl = Tools.enc(Namespace.nss(title));
			String s = Tools.inputStreamToString(
					Request.genericPost(new URL("http://bots.wmflabs.org/~richs/commonshelper.php"), null, Request.urlenc,
							String.format(posttext, tl, tl)), true);
			return s.substring(s.indexOf("{{Info"), s.indexOf("</textarea>"));
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}
}