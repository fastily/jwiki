JWIKI
=========
This is a MediaWiki [API](https://www.mediawiki.org/wiki/API:Main_page) client-side library.  It can be used by developers to build bots/tools and/or perform analytics on a Wiki.  My goal is to create a simple, reliable, efficient, and low-overhead framework for anybody seeking to make use of the MediaWiki API.

_NB_: This library is under active development so files/classes/functions may move, change, and/or disappear without warning.

##Features
* Edit and delete pages, upload files (via the chunked upload protocol)
* Query special pages, get category members, get links on a page, get template transclusions
* Supported MediaWiki extensions include [CentralAuth](https://www.mediawiki.org/wiki/Extension:CentralAuth) and [GlobalUsage](https://www.mediawiki.org/wiki/Extension:GlobalUsage).
* Bundled with a versatile, extensible multi-threaded bot framework to quickly perform changes or analytics.
* A flexible, extensible interface that allows advanced users to implement custom API queries.

##Dependencies
JSON support is provided by [JSON-java](https://github.com/douglascrockford/JSON-java).  This is bundled as a JAR archive at the top of the repository’s directory structure; use a newer version at your own discretion.

##Requirements
* Minimum [JDK/JRE](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) version: **8**
* Officially supported for [MediaWiki](https://www.mediawiki.org/wiki/MediaWiki): **1.25+**

##Getting Started
* [Quick Start Guide](https://github.com/fastily/jwiki/wiki/Quick-Start-Guide)
* [Javadocs](https://fastily.github.io/jwiki/docs/jwiki/)
* Main class: [Wiki.java](https://github.com/fastily/jwiki/blob/master/src/jwiki/core/Wiki.java)

####Sample Code
```java
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
```

##Project Goals
I designed jwiki with the following goals in mind:

* **Simple** - _Anybody_ with a beginner's knowledge of Java shall be able to use this framework.  I avoid horrible things like complex custom objects and convoluted calls; this project isn't for showing off my Java skills - it's designed to save my users time and effort.
* **Speed** - This framework shall emphasize performance.  Time is precious so why spend it waiting for some dumb program to finish!
* **Succinct** - Changes and queries shall be easy to perform.  I designed this framework so API calls can be constructed with one line of code consisting of simple objects and primitive types.
