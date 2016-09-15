#!/bin/bash

#: Generates Maven artifacts for jwiki. 
#: 
#: PRECONDITION: gradle build succeeded.  Minimum gradle version >= 3.0
#: 
#: Tested on OS X 10.11.6
#: Author: Fastily

version="1.2.2"
name="jwiki"
outputDir="build/artifacts"

cd "${0%/*}"
mkdir -p "$outputDir"

## Copy sources
jar cf "${outputDir}/${name}-${version}-sources.jar" -C "src/main/java/" .

## Copy javadocs
jar cf "${outputDir}/${name}-${version}-javadoc.jar" -C "build/docs/javadoc/" .

## Copy compiled files
cp build/libs/*.jar "$outputDir"

## Copy generated pom
cp "build/pom.xml" "${outputDir}/${name}-${version}.pom"

printf "Done!\n"