[![Build Status](https://travis-ci.org/brailleapps/dotify.hyphenator.impl.svg?branch=master)](https://travis-ci.org/brailleapps/dotify.hyphenator.impl)
[![Type](https://img.shields.io/badge/type-provider_bundle-blue.svg)](https://github.com/brailleapps/wiki/wiki/Types)
[![License: LGPL v2.1](https://img.shields.io/badge/License-LGPL%20v2%2E1%20%28or%20later%29-blue.svg)](https://www.gnu.org/licenses/lgpl-2.1)

# dotify.hyphenator.impl #
Provides hyphenators for many languages. This implementation is based on Franklin Mark Liang's hyphenation algorithm as used in TeX and contains patterns for about 50 languages.

## Techniques ##
  * Java, Java SPI, OSGi
  * Franklin Mark Liang's hyphenation patterns

## Functionality and features ##
  * Supports hyphenation for about 50 languages
  * Widely used format for hyphenation description

## Limitations ##
Franklin Mark Liang's hyphenation algorithm does not support non-standard hyphenation.

## Using ##
Download the [latest release](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.daisy.dotify%22%20%20a%3A%22dotify.hyphenator.impl%22) from maven central and add it to your runtime environment.
Access the implementations via the `HyphenatorFactoryMaker` API in [dotify.api](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.daisy.dotify%22%20%20a%3A%22dotify.api%22) _or_ in an OSGi environment use `HyphenatorFactoryMakerService`.

## Building ##
Build with `gradlew build` (Windows) or `./gradlew build` (Mac/Linux)

## Testing ##
Tests are run with `gradlew test` (Windows) or `./gradlew test` (Mac/Linux)

## Requirements & Compatibility ##
- Requires Java 8
- Compatible with SPI and OSGi

## More information ##
See the [common wiki](https://github.com/brailleapps/wiki/wiki) for more information.
