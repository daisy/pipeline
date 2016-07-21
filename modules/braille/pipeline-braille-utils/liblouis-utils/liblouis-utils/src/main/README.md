# liblouis-utils API

## <a href="resources/META-INF/catalog.xml" class="source">catalog.xml</a>

- <a href="resources/xml/library.xsl" class="apidoc">`http://www.daisy.org/pipeline/modules/braille/liblouis-utils/library.xsl`</a>
  - `louis:translate`: Translate a text string to Braille using liblouis.
  - `louis:hyphenate`: Hyphenate a text string using liblouis.

- <a href="resources/xml/library.xpl" class="apidoc">`http://www.daisy.org/pipeline/modules/braille/liblouis-utils/library.xpl`</a>
  - `louis:format`: Convert an XML document with inline Braille CSS to
    PEF using liblouisutdml.
  - `louis:translate-mathml`: Translate a MathML document to Braille
    using liblouisutdml.

## OSGi services

### Transformers (`org.daisy.pipeline.braille.common.TransformProvider`)

- [`org.daisy.pipeline.braille.liblouis.impl.LiblouisCSSBlockTransform.Provider`](java/org/daisy/pipeline/braille/liblouis/impl/LiblouisCSSBlockTransform.java): `(input:css)(output:css)(translator:liblouis)`


<link rev="dp2:doc" href="./"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/apidoc"/>
