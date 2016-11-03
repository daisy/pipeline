[![Build Status](https://travis-ci.org/brailleapps/dotify.api.svg?branch=master)](https://travis-ci.org/brailleapps/dotify.api)
[![Type](https://img.shields.io/badge/type-api-blue.svg)](https://github.com/brailleapps/wiki/wiki/Types)

# Core Dotify API #
Provides the core Dotify API.

## Using ##
To implement the API, download the [latest release](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.daisy.dotify%22%20%20a%3A%22dotify.api%22) from maven central.

To use the API, you need to download at least one implementation as well. Here's a list of known implementations:
 - dotify.formatter.impl
 - dotify.translator.impl
 - dotify.text.impl
 - dotify.hyphenator.impl

## Building ##
Build with `gradlew build` (Windows) or `./gradlew build` (Mac/Linux)

## Testing ##
Tests are run with `gradlew test` (Windows) or `./gradlew test` (Mac/Linux)

## Requirements & Compatibility ##
- Requires Java 8
- Implementations can be provided with SPI and/or OSGi

# Javadoc #
Javadoc for the latest Dotify API development is available [here](http://brailleapps.github.io/dotify.api/latest/javadoc/).

## More information ##
See the [common wiki](https://github.com/brailleapps/wiki/wiki) for more information.
