#!/bin/bash

#: Downloads and compiles the JSON-java library into a jar archive.
#: 
#: PRECONDITIONS: 
#:		1) Git and the JDK are installed
#: 
#: Tested on OS X 10.11.2
#: Author: Fastily

cd `dirname "$0"`

## Download JSON-java and compile
git clone "https://github.com/douglascrockford/JSON-java.git"
cd JSON-java

printf "Compiling JSON-java...\n"
mkdir -p org/json
mv *.java org/json/
cd org/json

javac *.java
cd ../..

## Create jar
printf "Making jar\n"
jar cf JSON-java.jar org/ README
mv JSON-java.jar ../

## Clean up
printf "Cleaning up...\n"
cd ..
rm -rf JSON-java

printf "Done!\n"