[![Build Status](https://travis-ci.org/brailleapps/dotify.task.impl.svg?branch=master)](https://travis-ci.org/brailleapps/dotify.task.impl)
[![Type](https://img.shields.io/badge/type-provider_bundle-blue.svg)](https://github.com/brailleapps/wiki/wiki/Types)

# dotify.task.impl #
Provides implementations for converting:
- DTBook, epub, html, xml and text to OBFL
- OBFL to PEF and text

## Using ##
Download the [latest release](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.daisy.dotify%22%20%20a%3A%22dotify.task.impl%22) from maven central and add it to your runtime environment.
Access the implementations via the `TaskSystemFactory` and `TaskGroupFactory` API in [dotify.task-api](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.daisy.dotify%22%20%20a%3A%22dotify.task-api%22) _or_ in an OSGi environment use `TaskSystemFactoryMakerService` and `TaskGroupFactoryMakerService`.

## Building ##
Build with `gradlew build` (Windows) or `./gradlew build` (Mac/Linux)

## Testing ##
Tests are run with `gradlew test` (Windows) or `./gradlew test` (Mac/Linux)

## Requirements & Compatibility ##
- Requires JDK 7
- Compatible with SPI and OSGi

## More information ##
See the [common wiki](https://github.com/brailleapps/wiki/wiki) for more information.
