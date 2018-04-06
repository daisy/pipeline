# liblouis-utils API

## <a href="resources/META-INF/catalog.xml" class="source">catalog.xml</a>

- <a href="resources/xml/library.xsl" class="apidoc">`http://www.daisy.org/pipeline/modules/braille/liblouis-utils/library.xsl`</a>
- <a href="resources/xml/library.xpl" class="apidoc">`http://www.daisy.org/pipeline/modules/braille/liblouis-utils/library.xpl`</a>

## OSGi services

### Transformers ([`org.daisy.pipeline.braille.common.TransformProvider`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/common/TransformProvider.html))

- [`(input:css)(output:css)(translator:liblouis)`](java/org/daisy/pipeline/braille/liblouis/impl/LiblouisCSSBlockTransform.java)
  
  Recognized features:
  
  - `id`: If present it must be the only feature. Will match a
      transformer with a unique ID.
  - `translator`: Will only match if the value is `liblouis`.
  - `locale`: If present the value will be used instead of any
      xml:lang attributes.
  
  Other features are used for finding sub-transformers of type
  [LiblouisTranslator](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/liblouis/LiblouisTranslator.html).
  


<link rev="dp2:doc" href="./"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/apidoc"/>
