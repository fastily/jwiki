# jwiki
[![Build Status](https://travis-ci.org/fastily/jwiki.svg?branch=master)](https://travis-ci.org/fastily/jwiki)
![JDK-1.8+](https://upload.wikimedia.org/wikipedia/commons/7/75/Blue_JDK_1.8%2B_Shield_Badge.svg)
[![MediaWiki 1.27+](https://upload.wikimedia.org/wikipedia/commons/2/2c/MediaWiki_1.27%2B_Blue_Badge.svg)](https://www.mediawiki.org/wiki/MediaWiki)
[![License: GPL v3](https://upload.wikimedia.org/wikipedia/commons/8/86/GPL_v3_Blue_Badge.svg)](https://www.gnu.org/licenses/gpl-3.0.en.html)

jwiki is a simple, lightweight Java framework for interacting with a [MediaWiki](https://www.mediawiki.org/wiki/MediaWiki) instance.

## Purpose
The MediaWiki [API](https://www.mediawiki.org/wiki/API:Main_page) is complicated and difficult to use effectively.  Clients are forced to deal with nasty details such as pagination, credential management, JSON, and unexpected errors.  I created jwiki to manage and hide away these nasty bits while providing effortless access to all the powerful features of the MediaWiki API.  jwiki allows complex API queries and actions to be executed with _one_ straightforward function call consisting of simple objects and/or primitive types.  It's so easy that _anyone_ (new developers included) can create an application that works with MediaWiki.

## Getting Started
* [Examples](https://github.com/fastily/jwiki/wiki/Examples)
* [Javadocs](https://fastily.github.io/jwiki/docs/jwiki/)

## Download
jwiki is [available](https://bintray.com/fastily/maven/jwiki) on [jcenter](https://bintray.com/bintray/jcenter)

#### Maven
```xml
<dependency>
  <groupId>fastily</groupId>
  <artifactId>jwiki</artifactId>
  <version>1.4.0</version>
  <type>pom</type>
</dependency>
```

#### Gradle
```groovy
compile 'fastily:jwiki:1.4.0'
```

## Build
Build and publish jwiki on your local machine with
```bash
./gradlew build publishToMavenLocal
```