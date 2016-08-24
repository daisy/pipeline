[![Build Status](https://travis-ci.org/brailleapps/braille-utils.pef-tools.svg?branch=master)](https://travis-ci.org/brailleapps/braille-utils.pef-tools)
[![Type](https://img.shields.io/badge/type-consumer_bundle-blue.svg)](https://github.com/brailleapps/wiki/wiki/Types)

## braille-utils.pef-tools ##
This contains pef related tools, based on the braille utils api.

## Using ##
To use the bundle, download the [latest release](http://search.maven.org/#search|ga|1|g%3A%22org.daisy.braille%22%20AND%20a%3A%22braille-utils.pef-tools%22) from maven central. In addtion, at least one bundle which implements the Braille Utils APIs must be added to the runtime, for example
[braille-utils.impl](http://search.maven.org/#search|ga|1|g%3A%22org.daisy.braille%22%20AND%20a%3A%22braille-utils.impl%22)

## Building ##
Build with `gradlew build` (Windows) or `./gradlew build` (Mac/Linux)

## Testing ##
Tests are run with `gradlew test` (Windows) or `./gradlew test` (Mac/Linux)

## Requirements & Compatibility ##
- Requires JDK 7
- Compatible with SPI and OSGi

## Javadoc ##
Javadoc for the latest PEF tools is available [here](http://brailleapps.github.io/braille-utils.pef-tools/latest/javadoc/).

## More information ##
See the [common wiki](https://github.com/brailleapps/wiki/wiki) for more information.
