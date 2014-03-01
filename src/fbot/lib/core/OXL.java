package fbot.lib.core;

import fbot.lib.commons.Commons;


public class OXL
{	

	public static void main(String[] args) throws Throwable
	{
		
		Constants.debug = true;
		//FGUI.login();

		String[] l = Commons.com.getLinksOnPage("Commons:Deletion requests/Files uploaded by Wuerthag", "File");
		Commons.removeDelete("otrs recieved", l);

	}
}