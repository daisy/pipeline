[![Build Status](https://travis-ci.org/brailleapps/dotify.api.svg?branch=master)](https://travis-ci.org/brailleapps/dotify.api)

# Core Dotify API #
Provides the core Dotify API.

## Using ##
To implement the API, download the latest release from maven central using the following information: `org.daisy.dotify:dotify.api:2.5.0`

To use the API, you need to download at least one implementation as well. Here's a list of known implementations:
 - dotify.formatter.impl
 - dotify.translator.impl
 - dotify.text.impl
 - dotify.hyphenator.impl

## Building ##
Build with `gradlew build`

## Requirements & Compatibility ##
- Requires JDK 7
- Implementations can be provided with SPI and/or OSGi

# Javadoc #
Javadoc for the latest Dotify API development is available [here](http://brailleapps.github.io/dotify.api/latest/javadoc/).
