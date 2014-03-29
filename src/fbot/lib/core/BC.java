package fbot.lib.core;

import static fbot.lib.commons.Commons.*;
import fbot.lib.commons.CStrings;
import fbot.lib.commons.Commons;
import fbot.lib.commons.WikiGen;
import fbot.lib.mbot.WAction;
import fbot.lib.util.ReadFile;

public class BC
{
	public static void main(String[] args)
	{
		//removeDelete("-", fsv.getLinksOnPage("Commons:Deletion requests/Files uploaded by NyRo", "File"));
		
		//{{OTRS permission}}
		//for(String s : fsv.getCategoryMembers("Items missing OTRS ticket ID"))
		//	fsv.replaceText(s, "(?si)\\{\\{(OTRS permission).*?\\}\\}", "{{OTRS permission|2011090410008681}}", "fix borked transfer");

		for(String s : fsv.getCategoryMembers(CStrings.osd, "File"))
			if(fsv.getRevisions(s, 1, false)[0].getUser().contains("Andy"))
				fastily.delete(s, CStrings.oos);
		//nuke(CStrings.uru, fsv.prefixIndex("User", "FSV/MobileUpload"));
	}
}