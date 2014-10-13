JWIKI
=========

This is a MediaWiki API client-side library.  It can be used by developers to build bots/tools or perform analytics on a Wiki.  My goal was to create a simple, reliable, efficient, and low-overhead framework for anybody trying to make use of the MediaWiki API.

Caveat: This library is under active development so files/classes/functions may move, change, and/or disappear without warning.

##Features

*Edit pages, delete pages, upload files (via the chunked upload protocol)
*Query special pages, get category members, get links on a page, get template transclusions
*Supported MediaWiki extensions include CentralAuth and GlobalUsage.
*Includes a versatile, extensible multi-threaded bot framework to quickly perform mass changes or analytics.
*Basic crypto to save login credentials locally and hide them from prying eyes

##Dependencies
*JSON support provided by [JSON-java](https://github.com/douglascrockford/JSON-java) - bundled as a JAR archive in top of the repository’s directory structure; use a newer version at your own discretion.

##Requirements
*Minimum [JDK/JRE](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) version: **8u20**
*Officially supported for [MediaWiki](https://www.mediawiki.org/wiki/MediaWiki) **1.25+**

##Getting Started Resources
*Main class: [Wiki.java](https://github.com/fastily/jwiki/blob/master/src/jwiki/core/Wiki.java)
*[Javadocs](http://fastily.github.io/jwiki/docs/jwiki/)

###Sample Code
```java
import javax.security.auth.login.LoginException;
import jwiki.core.Wiki;

//This program will edit an article by replacing the article text with some text of your choosing.
public class JwikiExample
{
   public static void main(String[] args) throws LoginException
   {
     Wiki wiki = new Wiki("Your_Username", "Your_Password", "en.wikipedia.org"); // login
     wiki.edit("SomeArticle", "SomeRandomText", "EditSummary"); // perform action
   }
}
```

##Project Objectives
I created this framework with a few specific goals in mind:

* **Simple** - _Anybody_ with a beginner's knowledge of Java shall be able to use this framework.  I avoid horrible things like complex custom objects and convoluted calls; this project isn't for showing off my Java skills, it's designed to save time and effort.
* **Speed** - This framework shall emphasize performance.  Time is a precious resource so why waste it waiting for some dumb program :)
* **Succinct** - Changes and queries shall be easy to perform.  I designed this framework so API calls to a Wiki can be constructed in seconds with one line of code consisting of, for the most part, Java primitive types.  I believe one should spend less time coding and more time completing tasks.