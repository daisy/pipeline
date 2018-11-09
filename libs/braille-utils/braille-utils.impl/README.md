[![Build Status](https://travis-ci.org/brailleapps/braille-utils.impl.svg?branch=master)](https://travis-ci.org/brailleapps/braille-utils.impl)
[![Type](https://img.shields.io/badge/type-provider_bundle-blue.svg)](https://github.com/brailleapps/wiki/wiki/Types)
[![License: LGPL v2.1](https://img.shields.io/badge/License-LGPL%20v2%2E1%20%28or%20later%29-blue.svg)](https://www.gnu.org/licenses/lgpl-2.1)

# braille-utils.imp #
Provides embosser, table and paper implementations for the purpose of communicating with embossers.

## Using ##
Download the [latest release](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22braille-utils.impl%22) from maven central and add it to your runtime environment.
Access the implementations via the following APIs in [braille-utils.api](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22braille-utils.api%22):
  - `EmbosserCatalog`
  - `TableCatalog`
  - `PaperCatalog`
  
_Or_, in an OSGi environment, use:
  - `EmbosserCatalogService`
  - `TableCatalogService`
  - `PaperCatalogService`

## Building ##
Build with `gradlew build` (Windows) or `./gradlew build` (Mac/Linux)

## Testing ##
Tests are run with `gradlew test` (Windows) or `./gradlew test` (Mac/Linux)

## Requirements & Compatibility ##
- Requires Java 8
- Compatible with SPI and OSGi

## More information ##
See the [common wiki](https://github.com/brailleapps/wiki/wiki) for more information.
