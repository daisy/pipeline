[![Build Status](https://travis-ci.org/brailleapps/dotify.translator.impl.svg?branch=master)](https://travis-ci.org/brailleapps/dotify.translator.impl)
[![Type](https://img.shields.io/badge/type-service_bundle-blue.svg)](https://github.com/brailleapps/wiki/wiki/Badges)

# Introduction #
dotify.translator.impl contains an implementation of the translator interfaces of [dotify.api](https://github.com/joeha480/dotify/tree/master/dotify.api). If you want to use it, you can get it [here](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22dotify.translator.impl%22).

## Using ##
Download the [latest release](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.daisy.dotify%22%20%20a%3A%22dotify.translator.impl%22) from maven central and add it to your runtime environment.

Access the implementations via the following APIs in [dotify.api](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.daisy.dotify%22%20%20a%3A%22dotify.api%22):
- `BrailleFilterFactoryMaker`
- `BrailleTranslatorFactoryMaker`
- `MarkerProcessorFactoryMaker`
- `TextBorderFactoryMaker`

 _or_ in an OSGi environment use:
- `BrailleFilterFactoryMakerService`
- `BrailleTranslatorFactoryMakerService`
- `MarkerProcessorFactoryMakerService`
- `TextBorderFactoryMakerService`

## Building ##
Build with `gradlew build` (Windows) or `./gradlew build` (Mac/Linux)

## Testing ##

Tests are run with `gradlew test` (Windows) or `./gradlew test` (Mac/Linux).

## Requirements & Compatibility ##
- Requires JDK 7
- Compatible with SPI and OSGi

## More information ##
See the [common wiki](https://github.com/brailleapps/wiki/wiki) for more information.
