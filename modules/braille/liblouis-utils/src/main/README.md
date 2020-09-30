# liblouis-utils

## API

### Java API

- package <a href="java/org/daisy/pipeline/braille/liblouis/" class="apidoc">org.daisy.pipeline.braille.liblouis</a>


## <a href="resources/META-INF/catalog.xml" class="source">catalog.xml</a>

- <a href="resources/xml/library.xsl" class="apidoc">`http://www.daisy.org/pipeline/modules/braille/liblouis-utils/library.xsl`</a>
- <a href="resources/xml/library.xpl" class="apidoc">`http://www.daisy.org/pipeline/modules/braille/liblouis-utils/library.xpl`</a>

## OSGi services

### Liblouis table paths ([`org.daisy.pipeline.braille.liblouis.LiblouisTablePath`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/liblouis/LiblouisTablePath.html))

- <a href="resources/tables/" class="apidoc">`http://www.liblouis.org/tables/`</a>

  The default translation tables that come with liblouis

### PEF tables ([`org.daisy.pipeline.braille.pef.TableProvider`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/pef/TableProvider.html))

- [`(liblouis-table:...)`](java/org/daisy/pipeline/braille/liblouis/pef/impl/LiblouisDisplayTableProvider.java)

  Recognized features:
  
  - `id`: Matches liblouis display tables by their fully qualified
      table identifier. Not compatible with other features.
  - `liblouis-table`: A liblouis table is a URI that can be either a
      file name, a file path relative to a registered tablepath, an
      absolute file URI, or a fully qualified table identifier.
  - `locale`: Matches only liblouis display tables with that locale.
  
  All matched tables must be of type "display table".

### Transformers ([`org.daisy.pipeline.braille.common.TransformProvider`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/common/TransformProvider.html))

- [`(input:css)(output:pef)(formatter:liblouis)`](java/org/daisy/pipeline/braille/liblouis/impl/LiblouisCSSStyledDocumentTransform.java)
  
  Converts an XML document with inline Braille CSS to PEF using liblouisutdml.
  
  Recognized features:

  - `formatter`: Will only match if the value is `liblouis`.

  Other features are used for finding sub-transformers of type
  [BrailleTranslator](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/common/BrailleTranslator.html).

- [`(input:text-css)(output:braille)(translator:liblouis)`](java/org/daisy/pipeline/braille/liblouis/impl/LiblouisTranslatorJnaImplProvider.java)
  
  Recognized features:
  
  - `id`: If present it must be the only feature. Will match a
      transformer with a unique ID.
  - `translator`: Will only match if the value is `liblouis`
  - `hyphenator`: A value `none` will disable hyphenation. `liblouis`
      will match only liblouis translators that support hyphenation
      out-of-the-box. `auto` is the default and will match any
      liblouis translator, whether it supports hyphenation
      out-of-the-box, with the help of an external hyphenator, or not
      at all. A value not equal to `none`, `liblouis` or `auto` will
      match every liblouis translator that uses an external hyphenator
      that matches this feature.
  - `table` or `liblouis-table`: A liblouis table is a list of URIs
      that can be either a file name, a file path relative to a
      registered tablepath, an absolute file URI, or a fully qualified
      table identifier. The tablepath that contains the first
      "sub-table" in the list will be used as the base for resolving
      the subsequent sub-tables. This feature is not compatible with
      other features except `translator`, `hyphenator` and `locale`.
  - `locale`: Matches only liblouis translators with that locale.
  - `handle-non-standard-hyphenation`: Specifies how non-standard
      hyphenation is handled in pre-translation mode. Can be `ignore`,
      `defer` or `fail`.
  - `dots-for-undefined-char`: The fixed dot pattern to insert for
    unknown characters. Must be a Unicode braille string.
  
  Other features are passed on to
  [`lou_findTable`](http://liblouis.org/documentation/liblouis.html#lou_005ffindTable). All
  matched tables must be of type "translation table".
  
  A translator will only use external hyphenators with the same locale as the translator itself.
  
- [`(hyphenator:liblouis)`](java/org/daisy/pipeline/braille/liblouis/impl/LiblouisHyphenatorJnaImplProvider.java)
  
  Recognized features:
  
  - `hyphenator`: Will only match if the value is `liblouis`
  - `table` or `liblouis-table`: A liblouis table is a list of URIs
      that can be either a file name, a file path relative to a
      registered tablepath, an absolute file URI, or a fully qualified
      table identifier. The tablepath that contains the first
      "sub-table" in the list will be used as the base for resolving
      the subsequent sub-tables. This feature is not compatible with
      other features except `hyphenator` and `locale`.
  - `locale`: Matches only hyphenators with that locale.
  
  Other features are passed on to [`lou_findTable`](http://liblouis.org/documentation/liblouis.html#lou_005ffindTable).

- [`(input:mathml)(output:braille)`](java/org/daisy/pipeline/braille/liblouis/impl/LiblouisMathMLTransform.java)
  
  Translates a MathML document to Braille using liblouisutdml.

### Saxon functions (`net.sf.saxon.lib.ExtensionFunctionDefinition`)

- [`{http://liblouis.org/liblouis}hyphenate`](java/org/daisy/pipeline/braille/liblouis/saxon/impl/HyphenateDefinition.java)

### Calabash steps (`org.daisy.common.xproc.calabash.XProcStepProvider`)

- [`{http://liblouis.org/liblouis}translate-file`](java/org/daisy/pipeline/braille/liblouis/calabash/impl/TranslateFileStep.java)



<link rev="dp2:doc" href="./"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/apidoc"/>
