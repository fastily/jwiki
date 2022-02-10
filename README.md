# jwiki
[![Build Status](https://github.com/fastily/jwiki/workflows/build/badge.svg)](#)
[![JDK-11+](https://upload.wikimedia.org/wikipedia/commons/e/ef/Blue_JDK_11%2B_Shield_Badge.svg)](https://adoptium.net)
[![MediaWiki 1.31+](https://upload.wikimedia.org/wikipedia/commons/b/b2/Blue_MediaWiki_1.31%2B_Shield_Badge.svg)](https://www.mediawiki.org/wiki/MediaWiki)
[![License: GPL v3](https://upload.wikimedia.org/wikipedia/commons/8/86/GPL_v3_Blue_Badge.svg)](https://www.gnu.org/licenses/gpl-3.0.en.html)

Programmatically accessing [Wikipedia](https://en.wikipedia.org/wiki/Main_Page)/[MediaWiki](https://www.mediawiki.org/wiki/MediaWiki) via the [API](https://en.wikipedia.org/w/api.php) is hard ☹️.  I thought it didn't have to be, so I made it easy 😀.  jwiki lets you perform all sorts of crazy API calls with 1️⃣ line of Java.  

Yes, **one** line.  

It's so easy that _anyone_ (including your grandma 👵🏻) can write a program that works with MediaWiki.

Not convinced?  Try out the [examples](https://github.com/fastily/jwiki/wiki/Examples).

## Download
#### Maven
```xml
<dependency>
  <groupId>org.fastily</groupId>
  <artifactId>jwiki</artifactId>
  <version>1.9.0</version>
</dependency>
```

#### Gradle
```groovy
implementation 'org.fastily:jwiki:1.9.0'
```

⚠️ COORDINATES HAVE CHANGED (since 1.7.0): jwiki's new group id is `org.fastily`

## Build
Build and publish (install) jwiki on your computer with
```bash
./gradlew publishToMavenLocal -x signMavenJavaPublication
```

## Resources
* [Examples](https://github.com/fastily/jwiki/wiki/Examples)
* [Javadocs](https://www.javadoc.io/doc/org.fastily/jwiki/latest/)

Please create a new [issue](https://github.com/fastily/jwiki/issues) for bug reports and/or feature requests.

## Goals
* **Simplicity** - Complex objects and functions are abstracted into the background so that anybody, regardless of Java experience, will be able to use jwiki.
* **Speed** - Network calls, local computation, and memory usage are optimized and minimized, so as to enhance performance and reduce overhead.
* **Succinctness** - Complex API actions can be performed in jwiki using one line of local code consisting of simple objects and primitive types.