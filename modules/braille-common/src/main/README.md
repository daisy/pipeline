# braille-common

## Java API

- package [`org.daisy.pipeline.braille.common`](java/org/daisy/pipeline/braille/common/)

## [catalog.xml](resources/META-INF/catalog.xml)

- [`http://www.daisy.org/pipeline/modules/braille/common-utils/library.xsl`](resources/xml/library.xsl)
- [`http://www.daisy.org/pipeline/modules/braille/common-utils/library.xpl`](resources/xml/library.xpl)
- [`http://www.daisy.org/pipeline/modules/braille/css-utils/transform/abstract-block-translator.xsl`](resources/xml/abstract-block-translator.xsl)
- [`http://www.daisy.org/pipeline/modules/braille/css-utils/transform/block-translator-from-text-transform.xsl`](resources/xml/block-translator-from-text-transform.xsl)

## Transformers ([`org.daisy.pipeline.braille.common.TransformProvider`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/common/TransformProvider.html))

- [`(input:css)(output:css)`](java/org/daisy/pipeline/braille/common/impl/CSSBlockTransform.java)

  Provides a
  [`BrailleTranslator`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/common/BrailleTranslator.html)
  that takes a CSS styled document and processes it
  [block](https://www.w3.org/TR/2007/WD-css3-box-20070809/#block-level)
  per block, transcribing text to braille if possible. The result is a
  copy of the input with some text nodes translated to braille, and
  some text-level CSS styles changed. Invisible control characters
  such as [soft
  hyphens](https://www.unicode.org/reports/tr14/tr14-39.html#SoftHyphen)
  may also have been inserted. Block-level CSS and text that can not
  be translated to braille for some reason is left unchanged.

  Recognized features of this transformer are:

  `id`
  : If present it must be the only feature. Will match a transformer with a unique ID.

  `locale`
  : If present the value will be used instead of any `xml:lang` attributes.

  The remaining features are used for selecting sub-transformers of
  type
  [`BrailleTranslator`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/common/BrailleTranslator.html)
  that match `(input:text-css)`.

## Saxon XPath functions ([`net.sf.saxon.lib.ExtensionFunctionDefinition`](https://www.saxonica.com/html/documentation9.8/javadoc/net/sf/saxon/lib/ExtensionFunctionDefinition.html))

- [`{http://www.daisy.org/ns/pipeline/functions}text-transform`](java/org/daisy/pipeline/braille/common/saxon/impl/TextTransformDefinition.java)

  Apply a text transformer to a string sequence, see [XSLT documentation](resources/xml/library.xsl)

## XMLCalabash XProc steps ([`org.daisy.common.xproc.calabash.XProcStepProvider`](http://daisy.github.io/pipeline/api/org/daisy/common/xproc/calabash/XProcStepProvider.html))

- [`{http://www.daisy.org/ns/pipeline/xproc}transform`](java/org/daisy/pipeline/braille/common/calabash/impl/PxTransformStep.java)

  Apply an XML transformer to a node, see [XProc documentation](resources/xml/library.xpl)

- [`{http://www.daisy.org/ns/pipeline/xproc}parse-query`](java/org/daisy/pipeline/braille/common/calabash/impl/PxParseQueryStep.java)

  Parse a query string and convert it to a `c:param-set` document, see [XProc documentation](resources/xml/library.xpl)


<link rev="dp2:doc" href="./"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/common/saxon/impl/TextTransformDefinition.java"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/common/calabash/impl/PxTransformStep.java"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/common/calabash/impl/PxParseQueryStep.java"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/common/impl/CSSBlockTransform.java"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/apidoc"/>
