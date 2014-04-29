package ft;

import java.util.ArrayList;

import jwiki.commons.CStrings;
import jwiki.commons.WikiGen;
import jwiki.core.Wiki;
import jwiki.mbot.WAction;
import jwiki.util.FString;
import static jwiki.commons.Commons.*;

public class RedirectClear
{
	public static void main(String[] args)
	{
		int process = 500;
		for (String s : args)
			if (s.matches("\\d*?"))
				process = Integer.parseInt(s);
		
		ArrayList<RItem> l = new ArrayList<RItem>();
		for (String s : fsv.allPages(null, true, process, "File"))
			l.add(new RItem(s));
		
		WikiGen.genM("Fastily", 6).start(l.toArray(new RItem[0]));
		
	}
	
	private static class RItem extends WAction
	{
		private RItem(String title)
		{
			super(title, null, null);
		}
		
		public boolean doJob(Wiki wiki)
		{
			if (!fsv.globalUsage(title).isEmpty())
				return true;
			
			//TODO: This really should be a replacement operation
			if(fsv.imageUsage(title).length > 0)
				return true;

			return fastily.delete(title, CStrings.house);
		}
	}
}