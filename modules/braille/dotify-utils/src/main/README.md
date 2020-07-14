# dotify-utils API

## Java API

- package <a href="java/org/daisy/pipeline/braille/dotify/" class="apidoc"><code>org.daisy.pipeline.braille.dotify</code></a>

## <a href="resources/META-INF/catalog.xml" class="source">catalog.xml</a>

- <a href="resources/xml/library.xsl" class="apidoc">`http://www.daisy.org/pipeline/modules/braille/dotify-utils/library.xsl`</a>
- <a href="resources/xml/library.xpl" class="apidoc">`http://www.daisy.org/pipeline/modules/braille/dotify-utils/library.xpl`</a>

## OSGi services

### Calabash steps (`org.daisy.common.xproc.calabash.XProcStepProvider`)

- [`{http://code.google.com/p/dotify/}obfl-to-pef`](java/org/daisy/pipeline/braille/dotify/calabash/impl/OBFLToPEFStep.java)
- [`{http://code.google.com/p/dotify/}file-to-obfl`](java/org/daisy/pipeline/braille/dotify/calabash/impl/FileToOBFLStep.java)


### Transformers ([`org.daisy.pipeline.braille.common.TransformProvider`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/common/TransformProvider.html))

- [`(input:text-css)(output:braille)(translator:dotify)`](java/org/daisy/pipeline/braille/dotify/impl/DotifyTranslatorImpl.java)
  
  Recognized features:
  
  - `translator`: Will only match if the value is `dotify`.
  - `locale`: Required. Matches only Dotify translators for that
      locale. An automatic fallback mechanism is used: if nothing is
      found for language-COUNTRY-variant, then language-COUNTRY is
      searched, then language.
  - `hyphenator`: A value `none` will disable hyphenation. `auto` is
      the default and will match any Dotify translator, whether it
      supports hyphenation out-of-the-box, with the help of an
      external hyphenator, or not at all. A value not equal to `none`
      or `auto` will match every Dotify translator that uses an
      external hyphenator that matches this feature.
  
  No other features are allowed.

- [`(hyphenator:dotify)`](java/org/daisy/pipeline/braille/dotify/impl/DotifyHyphenatorImpl.java)
  
  Recognized features:
  
  - `hyphenator`: Will only match if the value is `dotify`.
  - `locale`: Required. Matches only Dotify translators for that
      locale. An automatic fallback mechanism is used: if nothing is
      found for language-COUNTRY-variant, then language-COUNTRY is
      searched, then language.
  
  No other features are allowed.

- [`(input:css)(output:pef)(formatter:dotify)`](java/org/daisy/pipeline/braille/dotify/impl/DotifyCSSStyledDocumentTransform.java)
  
  Recognized features:
  
  - `formatter`: Will only match if the value is `dotify`.
  
  Other features are used for finding sub-transformers of type BrailleTranslator.



<link rev="dp2:doc" href="./"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/apidoc"/>
