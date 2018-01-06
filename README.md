# jwiki
[![Build Status](https://travis-ci.org/fastily/jwiki.svg?branch=master)](https://travis-ci.org/fastily/jwiki)
![JDK-1.8+](https://upload.wikimedia.org/wikipedia/commons/7/75/Blue_JDK_1.8%2B_Shield_Badge.svg)
[![MediaWiki 1.27+](https://upload.wikimedia.org/wikipedia/commons/2/2c/MediaWiki_1.27%2B_Blue_Badge.svg)](https://www.mediawiki.org/wiki/MediaWiki)
[![License: GPL v3](https://upload.wikimedia.org/wikipedia/commons/8/86/GPL_v3_Blue_Badge.svg)](https://www.gnu.org/licenses/gpl-3.0.en.html)

jwiki is a lightweight Java library that makes interacting with [MediaWiki](https://www.mediawiki.org/wiki/MediaWiki) effortless.

## Overview
The MediaWiki [API](https://www.mediawiki.org/wiki/API:Main_page) is complicated and difficult to use effectively.  Clients must handle  complexities such as pagination, credential management, JSON, and errors.  I created jwiki to hide these nasty bits in the background, while providing a simple interface to access all the powerful features of the MediaWiki API.

With jwiki, complex API actions/queries can be executed with _one_ simple function call using only simple objects and primitive types.  It's so easy that _anyone_ can write an application that works with MediaWiki.

## Getting Started
* [Examples](https://github.com/fastily/jwiki/wiki/Examples)
* [Javadocs](https://fastily.github.io/jwiki/docs/jwiki/)

## Download
jwiki is [available on jcenter](https://bintray.com/fastily/maven/jwiki).

#### Maven
```xml
<dependency>
  <groupId>fastily</groupId>
  <artifactId>jwiki</artifactId>
  <version>1.5.0</version>
  <type>pom</type>
</dependency>
```

#### Gradle
```groovy
compile 'fastily:jwiki:1.5.0'
```

## Build
Build and publish jwiki on your local machine with
```bash
./gradlew build publishToMavenLocal
```

## Feedback
Please use [issues](https://github.com/fastily/jwiki/issues) for bug reports and/or feature requests.