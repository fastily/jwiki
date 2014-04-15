package jwiki.core;

import static jwiki.commons.Commons.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import jwiki.commons.CStrings;
import jwiki.commons.Commons;
import jwiki.commons.WikiGen;
import jwiki.mbot.WAction;
import jwiki.util.FString;
import jwiki.util.ReadFile;

public class BC
{
	public static void main(String[] args) throws FileNotFoundException
	{
		/*
		ArrayList<WAction> l  = new ArrayList<WAction>();		
		for(String s : fsv.prefixIndex("User", "FSV"))
			if(s.contains("/T"))
				l.add(new WAction(s, null, null) {
					@Override
					public boolean doJob(Wiki wiki)
					{
						try
						{
							System.out.println(Thread.currentThread().getName());
							Thread.sleep(1000);
						}
						catch(Throwable e)
						{
							e.printStackTrace();
						}
						return true;
					}
				});
		
		doAction("FSV", l.toArray(new WAction[0]));*/
		
		ArrayList<String> l  = new ArrayList<String>();		
		for(String s : fsv.prefixIndex("User", "FSV"))
			if(s.contains("/T"))
				l.add(s);
		
		nuke(CStrings.uru, l.toArray(new String[0]));
		
	}
}