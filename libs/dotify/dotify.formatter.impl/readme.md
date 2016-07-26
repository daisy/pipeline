[![Build Status](https://travis-ci.org/brailleapps/dotify.formatter.impl.svg?branch=master)](https://travis-ci.org/brailleapps/dotify.formatter.impl)

# dotify.formatter.impl #
dotify.formatter.impl contains an implementation of the formatter interfaces of [dotify.api](https://github.com/brailleapps/dotify.api).

## Using ##
Download the [latest release](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.daisy.dotify%22%20%20a%3A%22dotify.formatter.impl%22) from maven central and add it to your runtime environment.

Access the implementations via the following APIs in [dotify.api](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.daisy.dotify%22%20%20a%3A%22dotify.api%22):
- `FormatterEngineMaker`
- `FormatterFactoryMaker`
- `ExpressionFactoryMaker`
- `PagedMediaWriterFactoryMaker`

 _or_ in an OSGi environment use:
- `FormatterEngineFactoryService`
- `FormatterFactory`
- `ExpressionFactory`
- `PagedMediaWriterFactoryMakerService`

## Building ##
Build with `gradlew build` (Windows) or `./gradlew build` (Mac/Linux)

## Testing ##

Tests are run with `gradlew test` (Windows) or `./gradlew test` (Mac/Linux).

### Adding tests ###

OBFL-to-PEF tests can be added by including lines such as the
following:

```java
testPEF("resource-files/foo-input.obfl", "resource-files/foo-expected.pef", false);
```

in a class that extends `AbstractFormatterEngineTest`. 

If you want the tests to be included in the overview page (linked to above)
the OBFL and PEF files need to be placed in
`test/org/daisy/dotify/engine/impl/resource-files` and named according
to the pattern `foo-input.obfl`/`foo-expected.pef`.

## Requirements & Compatibility ##
- Requires JDK 7
- Compatible with SPI and OSGi
