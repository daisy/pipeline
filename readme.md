[![Build Status](https://travis-ci.org/brailleapps/dotify.hyphenator.impl.svg?branch=master)](https://travis-ci.org/brailleapps/dotify.hyphenator.impl)

# dotify.hyphenator.impl #
Provides hyphenators for many languages.  

## Using ##
Download the [latest release](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.daisy.dotify%22%20%20a%3A%22dotify.hyphenator.impl%22) from maven central and add it to your runtime environment.
Access the implementations via the `HyphenatorFactoryMaker` API in [dotify.api](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.daisy.dotify%22%20%20a%3A%22dotify.api%22) _or_ in an OSGi environment use `HyphenatorFactoryMakerService`.

## Building ##
Build with `gradlew build` (Windows) or `./gradlew build` (Mac/Linux)

## Requirements & Compatibility ##
- Requires JDK 7
- Compatible with SPI and OSGi