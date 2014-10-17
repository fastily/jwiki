JWIKI
=========
This is a MediaWiki [API](https://www.mediawiki.org/wiki/API:Main_page) client-side library.  It can be used by developers to build bots/tools or perform analytics on a Wiki.  My goal is to create a simple, reliable, efficient, and low-overhead framework for anybody seeking to make use of the MediaWiki API.

NB: This library is under active development so files/classes/functions may move, change, and/or disappear without warning.

##Features
* Edit and delete pages, upload files (via the chunked upload protocol)
* Query special pages, get category members, get links on a page, get template transclusions
* Supported MediaWiki extensions include [CentralAuth](https://www.mediawiki.org/wiki/Extension:CentralAuth) and [GlobalUsage](https://www.mediawiki.org/wiki/Extension:GlobalUsage).
* Bundled with a versatile, extensible multi-threaded bot framework to quickly perform changes or analytics.
* Bundled with basic crypto to save login credentials locally and hide them from prying eyes

##Dependencies
JSON support is provided by [JSON-java](https://github.com/douglascrockford/JSON-java) which is bundled as a JAR archive in top of the repository’s directory structure; use a newer version at your own discretion.

##Requirements
* Minimum [JDK/JRE](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) version: **8u20**
* Officially supported for [MediaWiki](https://www.mediawiki.org/wiki/MediaWiki) **1.25+**

##Getting Started
* [Javadocs](http://fastily.github.io/jwiki/docs/jwiki/)
* Main class: [Wiki.java](https://github.com/fastily/jwiki/blob/master/src/jwiki/core/Wiki.java)

####Sample Code
```java
import javax.security.auth.login.LoginException;
import jwiki.core.Wiki;

//This program edits a page by replacing its text with text of your choosing.
public class JwikiExample
{
   public static void main(String[] args) throws LoginException
   {
     Wiki wiki = new Wiki("Username", “Password”, "en.wikipedia.org"); // login
     wiki.edit("SomePage", "SomeText", "EditSummary"); // perform action
   }
}
```

##Project Objectives
I designed this framework the following goals in mind:

* **Simple** - _Anybody_ with a beginner's knowledge of Java shall be able to use this framework.  I avoid horrible things like complex custom objects and convoluted calls; this project isn't for showing off my Java skills, it's designed to save time and effort.
* **Speed** - This framework shall emphasize performance.  Time is a precious resource so why waste it waiting for some dumb program :)
* **Succinct** - Changes and queries shall be easy to perform.  I designed this framework so API calls to a Wiki can be constructed in seconds with one line of code consisting of, for the most part, Java primitive types.  I believe one should spend less time coding and more time completing tasks.