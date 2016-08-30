#JWIKI
jwiki is a simple Java client library wrapping the [MediaWiki](https://www.mediawiki.org/wiki/MediaWiki) Web [API](https://www.mediawiki.org/wiki/API:Main_page).  It can be used by developers to create bots and tools, or to perform analytics on just about any Wiki.

##Features
* Perform actions, such as edit, delete, and upload (using chunked uploads).
* Perform queries, such as getting category members, getting links on a page, and getting template transclusions.
* Support for popular MediaWiki extensions, including [CentralAuth](https://www.mediawiki.org/wiki/Extension:CentralAuth) and [GlobalUsage](https://www.mediawiki.org/wiki/Extension:GlobalUsage).

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
* [JSON-java](https://github.com/stleary/JSON-java)

###Requirements
* [JDK](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html): **8+**
* [MediaWiki](https://www.mediawiki.org/wiki/MediaWiki): **1.25+**

###License
jwiki is dual licensed under the [cc-by-sa-4.0](https://creativecommons.org/licenses/by-sa/4.0/) and the [GPLv3](https://www.gnu.org/licenses/gpl-3.0.en.html).  You may select the license of your choice.

##Project Objectives
jwiki is intended to be a simple, reliable, and low-overhead framework for anybody seeking to make use of the MediaWiki API.  Emphasis is placed on:

* **Simplicity** - Complex objects and functions are abstracted into the background so that _anybody_, regardless of Java experience, will be able to use jwiki.
* **Speed** - Network calls, local computation, and memory usage are optimized and kept at a minimum, so as to enhance performance and reduce overhead.
* **Succinctness** - Most complex API actions can be performed in jwiki using one line of local code consisting of simple objects and primitive types.

##See Also
* [jwiki-extras](https://github.com/fastily/jwiki-extras) - Experimental extensions for jwiki
