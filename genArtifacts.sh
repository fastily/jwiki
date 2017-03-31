#!/bin/bash

#: Generates Maven artifacts for jwiki. 
#: 
#: Tested on macOS 10.12.4
#: Author: Fastily

version="1.3.0"
name="jwiki"
outputDir="build/artifacts"

cd "${0%/*}" &> /dev/null

gradle clean build -x test

if ! gradle writeNewPom genJavadoc ; then
    printf "ERROR: Build Failed\n"
    exit 1
fi

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