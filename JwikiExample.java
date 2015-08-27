import jwiki.core.Wiki;

//Edit a Wikipedia page by replacing its text with text of your choosing.
public class JwikiExample
{
   public static void main(String[] args) throws Throwable
   {
     Wiki wiki = new Wiki("Username", "Password", "en.wikipedia.org"); // login
     wiki.edit("SomePage", "SomeText", "EditSummary"); // edit
   }
}