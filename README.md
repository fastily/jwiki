#JWIKI
jwiki is a simple Java client framework wrapping the [MediaWiki](https://www.mediawiki.org/wiki/MediaWiki) API.  It can be used by developers to create bots/tools, or to perform analytics on any Wiki with API access.

I strongly recommend that you download the latest [release](https://github.com/fastily/jwiki/releases), as that code is stable and pre-configured to integrate with most IDEs.  Of course, if you prefer the bleeding edge, feel free to build from the latest source code.

##Features
* Edit pages, delete pages, upload files (using chunked uploads)
* Query special pages, get category members, get links on a page, get template transclusions
* Supported MediaWiki extensions include [CentralAuth](https://www.mediawiki.org/wiki/Extension:CentralAuth) and [GlobalUsage](https://www.mediawiki.org/wiki/Extension:GlobalUsage).

##Getting Started
* [Quick Start Guide](https://github.com/fastily/jwiki/wiki/Quick-Start-Guide)
* [Javadocs](https://fastily.github.io/jwiki/docs/jwiki/)
* Main class: [Wiki.java](https://github.com/fastily/jwiki/blob/master/src/jwiki/core/Wiki.java)

###Example Code
```java
import jwiki.core.Wiki;

//Edit a Wikipedia page by replacing its text with text of your choosing.
public class JwikiExample
{
   public static void main(String[] args) throws Throwable
   {
     Wiki wiki = new Wiki("Username", "Password", "en.wikipedia.org"); // login
     wiki.edit("Wikipedia:Sandbox", "SomeText", "EditSummary"); // edit
   }
}
```

###Dependencies
JSON parsing provided by [JSON-java](https://github.com/stleary/JSON-java).  It is bundled as a JAR in the `lib/` folder; use a newer version at your own risk.

###Requirements
* [JDK/JRE](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html): **8+**
* [MediaWiki](https://www.mediawiki.org/wiki/MediaWiki): **1.25+**


##Project Objectives
My goal is to create a simple, reliable, and low-overhead framework for anybody seeking to make use of the MediaWiki API.  I will be focusing on:

* **Simplicity** - Complex objects and functions are abstracted into the background so that _anybody_, regardless of Java experience, will be able to use jwiki.
* **Speed** - Network calls, local computation, and memory usage are optimized and kept at a minimum, so as to enhance performance and reduce overhead.
* **Succinctness** - Most complex API actions can be performed in jwiki using one line of local code consisting of simple objects and primitive types.

##See Also
* [jwiki-extras](https://github.com/fastily/jwiki-extras) - Experimental extensions for jwiki
