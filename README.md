# jwiki
[![Build Status](https://travis-ci.org/fastily/jwiki.svg?branch=master)](https://travis-ci.org/fastily/jwiki)
![JDK-1.8+](https://upload.wikimedia.org/wikipedia/commons/7/75/Blue_JDK_1.8%2B_Shield_Badge.svg)
[![MediaWiki 1.27+](https://upload.wikimedia.org/wikipedia/commons/2/2c/MediaWiki_1.27%2B_Blue_Badge.svg)](https://www.mediawiki.org/wiki/MediaWiki)
[![License: GPL v3](https://upload.wikimedia.org/wikipedia/commons/8/86/GPL_v3_Blue_Badge.svg)](https://www.gnu.org/licenses/gpl-3.0.en.html)

jwiki is a simple Java client library wrapping the [MediaWiki](https://www.mediawiki.org/wiki/MediaWiki) Web [API](https://www.mediawiki.org/wiki/API:Main_page).  It can be used by developers to create bots and tools, or to perform analytics on just about any Wiki.

## Features
* Perform actions, such as edit, delete, and upload (using chunked uploads).
* Perform queries, such as getting category members, getting links on a page, and getting template transclusions.
* Support for popular MediaWiki extensions, including [CentralAuth](https://www.mediawiki.org/wiki/Extension:CentralAuth) and [GlobalUsage](https://www.mediawiki.org/wiki/Extension:GlobalUsage).

## Getting Started
* Main class: [Wiki.java](https://github.com/fastily/jwiki/blob/master/src/main/java/fastily/jwiki/core/Wiki.java)
* [Recipe Book/Cheatsheet](https://github.com/fastily/jwiki/wiki/Recipe-Book)
* [Javadocs](https://fastily.github.io/jwiki/docs/jwiki/)

### Download
jwiki is available via [bintray/jcenter](https://bintray.com/fastily/maven/jwiki)

Maven:
```xml
<dependency>
  <groupId>fastily</groupId>
  <artifactId>jwiki</artifactId>
  <version>1.3.0</version>
  <type>pom</type>
</dependency>
```

Gradle:
```groovy
compile 'fastily:jwiki:1.3.0'
```

## Project Objectives
jwiki is intended to be a simple, reliable, and low-overhead framework for anybody seeking to make use of the MediaWiki API.  Emphasis is placed on:

* **Simplicity** - Complex objects and functions are abstracted into the background so that _anybody_, regardless of Java experience, will be able to use jwiki.
* **Speed** - Network calls, local computation, and memory usage are optimized and kept at a minimum, so as to enhance performance and reduce overhead.
* **Succinctness** - Most complex API actions can be performed in jwiki using one line of local code consisting of simple objects and primitive types.