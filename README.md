JWIKI
=========

This is a MediaWiki API client-side library.  It can be used to program bots/tools or perform analytics on a Wiki.  For best results, use with MW 1.23+

<ins>Caveat</ins>: This library is under active development and so files may move, change, and/or disappear without warning.

<h2>Sample Features</h2>
<ul>
<li>Edit pages, delete pages, upload files (via chunked upload protocol)</li>
<li>Query special pages, get category members, get links on a page, get template transclusions</li>
<li>Supported extensions include, but are not limited to CentralAuth, GlobalUsage.</li>
<li>Includes a versatile, extensible multi-threaded bot framework to quickly perform mass changes and/or analytics.</li>
<li>Basic crypto to save login credentials locally and hide them from prying eyes</li>
</ul>
Most common MediaWiki features are supported, as well as several extensions installed on WMF Wikis.

<h2>Dependencies</h2>
The project relies on two freely licensed external libraries, namely:
<ul>
<li><a href="https://github.com/douglascrockford/JSON-java">JSON-java</a> (<a href="http://www.json.org/license.html">License</a>)</li> - to parsing of JSON formatted server responses
<li><a href="http://svn.apache.org/viewvc/commons/proper/cli/trunk/src/">Commons CLI 1.3 (beta version)</a> (<a href="http://www.apache.org/licenses/">License</a>)</li> - Significantly decrease the argument parsing overhead in my CLI tools.
</ul>
These libraries are bundled as jars in top of the repo's directory structure.  You may also download the newest version(s) of these libraries from the links above, but I will not guarantee they will work with this framework.

<h2>System requirements</h2>
<ul>
<li>Client *must* be running a minimum Java version 8+ (1.8+)</li>
</ul>

<h2>ToDo List</h2>
<p>==High Priority==
*Create MassClientQuery class to perform multiple queries at once in a single query.  Obvious performance boost for the end user.
*Convert GlobalReplace to CLI and wrap it with GUI.
**GlobalReplace with 20+ images hits apihighlimit restriction.  Perhaps we should add a delay...</li>
*Timestamp parsing for time field (should be a date object, not a string) in Revision/Contrib is really messed up sometimes.  Is this broken server side?

*FLogin crypto is crap.  Keeps out non-CS people, but this is a joke to reverse engineer... 
** Salt should be secure random generated.
** Use MD5/MD6 hashes?
** Should I be using base64 encoding to output files?  This isn't a built-in and I'm lazy...
** Keys *should* be randomly generated - based on OS names, serial number, etc, perhaps?  We can save these locally.

== Low Priority ==
* Commons New Image Auditor
* Legit JUnit Tests
* Generators?
* Apparently addtext in edit already exists. </p>