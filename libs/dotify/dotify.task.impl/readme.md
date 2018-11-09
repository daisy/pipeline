[![Build Status](https://travis-ci.org/brailleapps/dotify.task.impl.svg?branch=master)](https://travis-ci.org/brailleapps/dotify.task.impl)
[![Type](https://img.shields.io/badge/type-provider_bundle-blue.svg)](https://github.com/brailleapps/wiki/wiki/Types)
[![License: LGPL v2.1](https://img.shields.io/badge/License-LGPL%20v2%2E1%20%28or%20later%29-blue.svg)](https://www.gnu.org/licenses/lgpl-2.1)

Note: This is a provider bundle, but it currently exports some classes that can be used by other bundles to extend the functionality
provided. These classes may be moved to a separate library bundle in the future.

# dotify.task.impl #
This component provides implementations for
* idenfiying XML formats with root element name and namespace
* validating OBFL and PEF files
* converting
  - DTBook, epub, html, xml and text to OBFL
  - OBFL to PEF and text

## Techniques ##
Java, StAX, XSLT, OBFL, Schematron, RelaxNG

## Using ##
Download the [latest release](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.daisy.dotify%22%20%20a%3A%22dotify.task.impl%22) from maven central and add it to your runtime environment.
Access the implementations via the `TaskSystemFactory` and `TaskGroupFactory` API in [streamline-api](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.daisy.streamline%22%20AND%20a%3A%22streamline-api%22) _or_ in an OSGi environment use `TaskSystemFactoryMakerService` and `TaskGroupFactoryMakerService`.

## Building ##
Build with `gradlew build` (Windows) or `./gradlew build` (Mac/Linux)

## Testing ##
Tests are run with `gradlew test` (Windows) or `./gradlew test` (Mac/Linux)

## Requirements & Compatibility ##
- Requires Java 8
- Compatible with SPI and OSGi

## More information ##
See the [common wiki](https://github.com/brailleapps/wiki/wiki) for more information.
