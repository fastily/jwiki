package fbot.ft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fbot.lib.commons.Commons;
import fbot.lib.commons.WikiGen;
import fbot.lib.core.Tools;
import fbot.lib.core.Wiki;
import fbot.lib.mbot.QAction;

public class MobileWebFinder
{
	private static final String google = "[//www.google.com/searchbyimage?image_url=%s Google Images] ";
	
	private static final String tineye = "[http://tineye.com/search?url=%s Tineye] ";
	
	private static final String delete = "[https://commons.wikimedia.org/w/index.php?title=%s&action=delete Delete] ";
	
	private static final String edit = "[https://commons.wikimedia.org/w/index.php?title=%s&action=edit Edit] ";
	
	private static final String history = "[https://commons.wikimedia.org/w/index.php?title=%s&action=history History] ";
	
	public static void main(String[] args)
	{
		ArrayList<MWItem> mwl = new ArrayList<MWItem>();
		for(String s : Commons.fastily.getCategoryMembers("Category:Uploaded with Mobile/Web", "File"))
			mwl.add(new MWItem(s));
		
		WikiGen.genM("Fastily", 22).start(mwl.toArray(new MWItem[0]));
		
		HashMap<String, ArrayList<MWItem>> l = genRawList();
		for(MWItem m : mwl)
			if(m.valid)
			{
				String key = "" + m.getTitle().charAt(5);
				if(l.containsKey(key))
					l.get(key).add(m);
				else
					l.get("Other").add(m);
			}
		
		String index = "{{User:FSV/MUFooter}}\nUpdated at ~~~~~\n\n";
		for(Map.Entry<String, ArrayList<MWItem>> e : l.entrySet())
		{
			String page = "User:FSV/MobileUpload/" + e.getKey();
			index += String.format("*[[/%s]]\n", e.getKey());
			
			String text = "<gallery>\n";
			for(MWItem m : e.getValue())
				text += genCap(m);
			
			text += "</gallery>";
			
			Commons.fsv.edit(page, text, "Updating list for " + e.getKey());
		}
		
		Commons.fsv.edit("User:FSV/MobileUpload", index, "Updating index");
	}
	
	
	private static HashMap<String, ArrayList<MWItem>> genRawList()
	{
		HashMap<String, ArrayList<MWItem>> l = new HashMap<String, ArrayList<MWItem>>();
		for(char i = 'A'; i <= 'Z'; i++)
			l.put("" + i, new ArrayList<MWItem>());
		l.put("Other", new ArrayList<MWItem>());
		
		return l;
			
	}
	
	private static String genCap(MWItem m)
	{
		String enc = Tools.enc(m.getTitle());
		return String.format("%s|%s%s%s%s%s\n", m.getTitle(), String.format(google, m.url), String.format(tineye, m.url),
				String.format(edit, enc), String.format(delete, enc), String.format(history, enc));
	}
	
	private static class MWItem extends QAction
	{
		private boolean valid = false;
		
		private String url;
		
		protected MWItem(String title)
		{
			super(title);
		}
		
		@Override
		public boolean doJob(Wiki wiki)
		{
			String t = getTitle();
			if (wiki.globalUsage(t).size() > 0
					|| wiki.imageUsage(t).length > 0
					|| wiki.getPageText(t)
							.matches("(?si).*?\\{\\{(speedy|no permission|no license|no source|delete).*?\\}\\}.*?"))
				return true;
			
			return (url = wiki.getImageInfo(t).getURL()) != null ? (valid = true) : false; // hehe
		}
	}
}