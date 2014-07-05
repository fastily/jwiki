JWIKI
=========

This is a MediaWiki API client-side library.  It can be used to program bots/tools or perform analytics on a Wiki.  My goal is to create a fast and efficient, yet simple framework, which introduces a minimal amount of overhead for anybody trying to make a bot.
<ins>Caveat</ins>: This library is under active development and so files may move, change, and/or disappear without warning.

<h3>Sample Features</h3>
<ul>
<li>Edit pages, delete pages, upload files (via chunked upload protocol)</li>
<li>Query special pages, get category members, get links on a page, get template transclusions</li>
<li>Supported extensions include, but are not limited to CentralAuth, GlobalUsage.</li>
<li>Includes a versatile, extensible multi-threaded bot framework to quickly perform mass changes and/or analytics.</li>
<li>Basic crypto to save login credentials locally and hide them from prying eyes</li>
</ul>
tl;dr: Most common MediaWiki features are supported, as well as several extensions installed on WMF Wikis.

<h3>Dependencies</h3>
The project relies on two freely licensed external libraries, namely:
<ul>
<li><a href="https://github.com/douglascrockford/JSON-java">JSON-java</a> (<a href="http://www.json.org/license.html">License</a>) - parse JSON formatted server responses </li> 
<li><a href="http://svn.apache.org/viewvc/commons/proper/cli/trunk/src/">Commons CLI 1.3 (beta version)</a> (<a href="http://www.apache.org/licenses/">License</a>) - Significantly decrease the argument parsing overhead in my CLI tools (in the <tt>ft</tt> package)</li> 
</ul>
These libraries are bundled as jars in top of the repo's directory structure.  You may also download the newest version(s) of these libraries from the links above, but I will not guarantee they will work with this framework.

<h3>Documentation</h3>
<a href="https://dl.dropboxusercontent.com/u/76520097/jwiki/index.html">Here</a> (it's ghetto I know.  I'll get a github.io site for it eventually :P)

Quick start guide: check out <tt>jwiki/core/Wiki.java</tt>.  Everything you need in one nifty file :) 

<h3>System requirements</h3>
<ul>
<li>Client *must* be running a minimum Java version 8+ (1.8+)</li>
<li>For best results, use with MW 1.23+</li>
</ul>