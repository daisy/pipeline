# liblouis-utils

## Java API

- package [`org.daisy.pipeline.braille.liblouis`](java/org/daisy/pipeline/braille/liblouis/)

## [catalog.xml](resources/META-INF/catalog.xml)

- [`http://www.daisy.org/pipeline/modules/braille/liblouis-utils/library.xsl`](resources/xml/library.xsl)

## Liblouis table paths ([`org.daisy.pipeline.braille.liblouis.LiblouisTablePath`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/liblouis/LiblouisTablePath.html))

- [`http://www.liblouis.org/tables/`](resources/default-tables/)

  The default translation tables that come with Liblouis

## PEF tables ([`org.daisy.pipeline.braille.pef.TableProvider`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/pef/TableProvider.html))

- [`(liblouis-table:...)`](java/org/daisy/pipeline/braille/liblouis/pef/impl/LiblouisDisplayTableProvider.java)

  Provides [PEF
  tables](https://mtmse.github.io/dotify.api/latest/javadoc/org/daisy/dotify/api/table/Table.html)
  based on Liblouis display tables. Recognized features are:

  `id`
  : Matches Liblouis display tables by their fully qualified table
    identifier. Not compatible with other features.

  `liblouis-table`
  : A Liblouis table is a URI that can be either a file name, a file
    path relative to a registered tablepath, an absolute file URI, or
    a fully qualified table identifier.

  `locale`
  : Matches only Liblouis display tables with that locale.
  
## Transformers ([`org.daisy.pipeline.braille.common.TransformProvider`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/common/TransformProvider.html))

- [`(input:text-css)(output:braille)(translator:liblouis)`](java/org/daisy/pipeline/braille/liblouis/impl/LiblouisTranslatorJnaImplProvider.java)

  Liblouis based braille translator, see [user documentation](../../doc/)

<!--

- [`(hyphenator:liblouis)`](java/org/daisy/pipeline/braille/liblouis/impl/LiblouisHyphenatorJnaImplProvider.java)
  
  Liblouis based hyphenator. Recognized features are:

  `hyphenator`
  : Will only match if the value is "liblouis"
  
  `table`
  `liblouis-table`
  : A Liblouis table is a list of URIs that can be either a file name,
    a file path relative to a registered tablepath, an absolute file
    URI, or a fully qualified table identifier. The tablepath that
    contains the first "sub-table" in the list will be used as the
    base for resolving the subsequent sub-tables. This feature is not
    compatible with other features except `hyphenator` and `locale`.

  `locale`
  : Matches only hyphenators with that locale.
  
  The remaining features are passed on to
  [`lou_findTable`](http://liblouis.org/documentation/liblouis.html#lou_005ffindTable).

- [`(input:mathml)(output:braille)`](java/org/daisy/pipeline/braille/liblouis/impl/LiblouisMathMLTransform.java)
  
  Translates a MathML document to Braille using
  Liblouisutdml. Recognized features are:

  `locale`
  : If the locale is "en-GB" the math code used is "UK maths". If the
    locale is "en" the math code used is Nemeth. If the locale is "de"
    the math code used is Marburg. If the locale is "nl" the math code
    used is the Woluwe code.

## Saxon XPath functions ([`net.sf.saxon.lib.ExtensionFunctionDefinition`](https://www.saxonica.com/html/documentation9.8/javadoc/net/sf/saxon/lib/ExtensionFunctionDefinition.html))

- [`{http://liblouis.org/liblouis}hyphenate`](java/org/daisy/pipeline/braille/liblouis/saxon/impl/HyphenateDefinition.java)

  Hyphenate a text string using Liblouis, see [XSLT documentation](resources/xml/library.xsl)

## XMLCalabash XProc steps ([`org.daisy.common.xproc.calabash.XProcStepProvider`](http://daisy.github.io/pipeline/api/org/daisy/common/xproc/calabash/XProcStepProvider.html))

- [`{http://liblouis.org/liblouis}translate-file`](java/org/daisy/pipeline/braille/liblouis/calabash/impl/TranslateFileStep.java)

  See [XProc documentation](resources/xml/library.xpl)
-->


[Liblouis]: http://liblouis.org/

<link rev="dp2:doc" href="./"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/liblouis/pef/impl/LiblouisDisplayTableProvider.java"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/liblouis/impl/LiblouisTranslatorJnaImplProvider.java"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/liblouis/impl/LiblouisCSSStyledDocumentTransform.java"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/liblouis/impl/LiblouisHyphenatorJnaImplProvider.java"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/liblouis/impl/LiblouisMathMLTransform.java"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/liblouis/saxon/impl/HyphenateDefinition.java"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/liblouis/calabash/impl/TranslateFileStep.java"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/apidoc"/>
