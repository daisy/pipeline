# css-utils API

## Java API

- package <a href="java/org/daisy/pipeline/braille/css/" class="apidoc"><code>org.daisy.pipeline.braille.css</code></a>

## <a href="resources/META-INF/catalog.xml" class="source">catalog.xml</a>

- <a href="resources/xml/library.xsl" class="apidoc">`http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl`</a>: Utility functions for CSS-parsing.
- <a href="resources/xml/library.xpl" class="apidoc">`http://www.daisy.org/pipeline/modules/braille/css-utils/library.xpl`</a>
- <a href="resources/xml/transform/abstract-block-translator.xsl" class="apidoc">`http://www.daisy.org/pipeline/modules/braille/css-utils/transform/abstract-block-translator.xsl`</a>
- <a href="resources/xml/transform/block-translator-from-text-transform.xsl" class="apidoc">`http://www.daisy.org/pipeline/modules/braille/css-utils/transform/block-translator-from-text-transform.xsl`</a>

## OSGi services

### Transformers ([`org.daisy.pipeline.braille.common.TransformProvider`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/common/TransformProvider.html))

- [`(input:css)(output:css)`](java/org/daisy/pipeline/braille/css/impl/CSSBlockTransform.java)
  
  Recognized features:
  
  - `id`: If present it must be the only feature. Will match a
      transformer with a unique ID.
  - `locale`: If present the value will be used instead of any
      xml:lang attributes.
  
  Other features are used for finding sub-transformers of type
  [BrailleTranslator](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/common/BrailleTranslator.html)
  that match `(input:text-css)`.
  
### Calabash steps (`org.daisy.common.xproc.calabash.XProcStepProvider`)

- [`{http://www.daisy.org/ns/pipeline/braille-css}inline`](java/org/daisy/pipeline/braille/css/calabash/impl/CssInlineStep.java)



<link rev="dp2:doc" href="./"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/apidoc"/>
