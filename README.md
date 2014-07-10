JWIKI
=========

This is a MediaWiki API client-side library.  It can be used by developers to program bots/tools or perform analytics on a Wiki.  My goal is to create an extremely fast and efficient, yet simple framework, which introduces a minimal amount of overhead for anybody trying to make use of the MediaWiki API.
</br>
<span style="text-decoration:underline;color:red">Caveat</span>: This library is under active development and so files/classes/functions may move, change, and/or disappear without warning.

<h3>Sample Features</h3>
<ul>
<li>Edit pages, delete pages, upload files (via the chunked upload protocol)</li>
<li>Query special pages, get category members, get links on a page, get template transclusions</li>
<li>Supported MediaWiki extensions include CentralAuth and GlobalUsage.</li>
<li>Includes a versatile, extensible multi-threaded bot framework to quickly perform mass changes or analytics.</li>
<li>Basic crypto to save login credentials locally and hide them from prying eyes</li>
</ul>

<h3>Dependencies</h3>
The project relies on two freely licensed external libraries, namely:
<ul>
<li><a href="https://github.com/douglascrockford/JSON-java">JSON-java</a> (<a href="http://www.json.org/license.html">License</a>) - parse JSON formatted server responses </li> 
<li><a href="http://svn.apache.org/viewvc/commons/proper/cli/trunk/src/">Commons CLI 1.3 (beta version)</a> (<a href="http://www.apache.org/licenses/">License</a>) - Significantly decrease the argument parsing overhead in my CLI tools (in the <tt>ft</tt> package)</li> 
</ul>
These libraries are bundled as jars in top of the repo's directory structure.  You may also download the newest version(s) of these libraries from the links above, but I cannot guarantee those versions will work with this framework.

<h3>Documentation</h3>
<a href="http://fastily.github.io/jwiki/docs/jwiki/">Here!</a>

Quick start guide: check out <a href=https://github.com/fastily/jwiki/blob/master/src/jwiki/core/Wiki.java style="font-family:Lucida Console">Wiki.java</a>.  Everything you need in one nifty file!

<h3>System requirements</h3>
<ul>
<li>Client *must* be running a minimum Java version 8+ (1.8+)</li>
<li>For best results, use with <a href="https://www.mediawiki.org/wiki/MediaWiki">MediaWiki</a> version 1.23+</li>
</ul>