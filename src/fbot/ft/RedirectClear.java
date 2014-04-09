package fbot.ft;

import java.util.ArrayList;

import fbot.lib.commons.CStrings;
import fbot.lib.commons.WikiGen;
import fbot.lib.core.Wiki;
import fbot.lib.mbot.WAction;
import fbot.lib.util.FString;
import static fbot.lib.commons.Commons.*;

public class RedirectClear
{
	public static void main(String[] args)
	{
		int process = 400;
		for (String s : args)
			if (s.matches("\\d*?"))
				process = Integer.parseInt(s);
		
		ArrayList<RItem> l = new ArrayList<RItem>();
		for (String s : fsv.allPages(null, true, process, "File"))
			l.add(new RItem(s));
		
		WikiGen.genM("Fastily", 3).start(l.toArray(new RItem[0]));
		
	}
	
	private static class RItem extends WAction
	{
		private RItem(String title)
		{
			super(title, null, null);
		}
		
		public boolean doJob(Wiki wiki)
		{
			if (!fsv.globalUsage(getTitle()).isEmpty())
				return true;
			
			String[] ul = fsv.imageUsage(getTitle());
			
			
			//for (String x : ul)
			//	fsv.replaceText(x, FString.makePageTitleRegex(x), "update");
			
			return fastily.delete(getTitle(), CStrings.house);
		}
	}
}