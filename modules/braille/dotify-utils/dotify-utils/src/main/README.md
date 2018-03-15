# dotify-utils API

## <a href="resources/META-INF/catalog.xml" class="source">catalog.xml</a>

- <a href="resources/xml/library.xsl" class="apidoc">`http://www.daisy.org/pipeline/modules/braille/dotify-utils/library.xsl`</a>
  - `dotify:translate`: Translate a text string to Braille with Dotify.

- <a href="resources/xml/library.xpl" class="apidoc">`http://www.daisy.org/pipeline/modules/braille/dotify-utils/library.xpl`</a>
  - `dotify:format`

## OSGi services

### Transformers (`org.daisy.pipeline.braille.common.TransformProvider`)

- [`org.daisy.pipeline.braille.dotify.impl.DotifyCSSBlockTransform.Provider`](java/org/daisy/pipeline/braille/dotify/impl/DotifyCSSBlockTransform.java): `(input:css)(output:css)(translator:dotify)`


<link rev="dp2:doc" href="./"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/apidoc"/>
