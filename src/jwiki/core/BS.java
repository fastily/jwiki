package jwiki.core;

import java.net.URL;

import jwiki.util.FString;

public class BS
{
	public static void main(String[] args) throws Throwable
	{
		//Settings.debug = true;
		URL u = new URL("https://commons.wikimedia.org/w/api.php?action=query&prop=categories&format=json&cllimit=3&titles=User%3AFastily&continue=");
		
		ServerReply r = ClientRequest.get(u, null);
		System.out.println(r.toString(2));
		
		System.out.println("----\n\n\n\n\n----");
		
		URL u2 = new URL("https://commons.wikimedia.org/w/api.php?action=query&prop=categories&format=json&cllimit=3&titles=User%3AFastily&continue=" + FString.enc("||") + "&clcontinue="  + FString.enc("25579206|User_en-N"));
		ServerReply r2 = ClientRequest.get(u2, null);
		System.out.println(r2.toString(2));
		
	}
}