JWIKI
=========

This is a MediaWiki API client-side library.  It can be used by developers to program bots/tools or perform analytics on a Wiki.  My goal is to create an extremely fast and efficient, yet simple framework, which introduces a minimal amount of overhead for anybody trying to make use of the MediaWiki API.


Caveat: This library is under active development and so files/classes/functions may move, change, and/or disappear without warning.

<h3>Sample Features</h3>
<ul>
<li>Edit pages, delete pages, upload files (via the chunked upload protocol)</li>
<li>Query special pages, get category members, get links on a page, get template transclusions</li>
<li>Supported MediaWiki extensions include CentralAuth and GlobalUsage.</li>
<li>Includes a versatile, extensible multi-threaded bot framework to quickly perform mass changes or analytics.</li>
<li>Basic crypto to save login credentials locally and hide them from prying eyes</li>
</ul>

<h3>Dependencies</h3>
The project relies on one external library for JSON support <a href="https://github.com/douglascrockford/JSON-java">JSON-java</a> (<a href="http://www.json.org/license.html">License</a>).  It is bundled as a .jar in top of the repo's directory structure; use newer version(s) at your own discretion.

<h3>Documentation</h3>
<li><a href="http://fastily.github.io/jwiki/docs/jwiki/">Javadocs</a></li>
<li>Quick start guide: check out <a href=https://github.com/fastily/jwiki/blob/master/src/jwiki/core/Wiki.java style="font-family:Lucida Console">Wiki.java</a>.  Everything you need in one nifty file!</li>
</ul>

<h3>System requirements</h3>
<ul>
<li>Client *must* be running a minimum Java version 8+ (1.8+)</li>
<li>For best results, use with <a href="https://www.mediawiki.org/wiki/MediaWiki">MediaWiki</a> version 1.23+</li>
</ul>

<h3>Project Objectives</h3>
I created this framework with a few specific goals in mind:
<ul>
<li>Simplicity - <b>Anybody</b> with a even beginner's knowledge of Java shall be able to use this framework.  I make it a point to avoid horrible things like complex objects and convoluted calls; this project isn't intended to show folks how amazing I am at the Java language, it's designed for the purpose of making their lives easy.</li>
<li>Speediness - This framework shall emphasize performance.  Time is a precious resource so why waste it waiting for some dumb program to finish :)</li>
<li>Shortness - Changes or Queries to a Wiki shall be easy to perform.  I designed this framework so that API calls to a Wiki can be constructed in seconds with one line of code consisting of, for the most part, Java primitive types.  I believe one should spend less time coding, and more time getting done what one initially set out to complete.</li>
</ul>