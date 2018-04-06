# dotify-utils API

## <a href="resources/META-INF/catalog.xml" class="source">catalog.xml</a>

- <a href="resources/xml/library.xsl" class="apidoc">`http://www.daisy.org/pipeline/modules/braille/dotify-utils/library.xsl`</a>
- <a href="resources/xml/library.xpl" class="apidoc">`http://www.daisy.org/pipeline/modules/braille/dotify-utils/library.xpl`</a>

## OSGi services

### Transformers ([`org.daisy.pipeline.braille.common.TransformProvider`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/common/TransformProvider.html))

- [`(input:css)(output:css)(translator:dotify)`](java/org/daisy/pipeline/braille/dotify/impl/DotifyCSSBlockTransform.java)
  
  Recognized features:
  
  - `translator`: Will only match if the value is `dotify`.
  - `locale`: If present the value will be used instead of any xml:lang attributes.
  
  Other features are used for finding sub-transformers of type DotifyTranslator.
  


<link rev="dp2:doc" href="./"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/apidoc"/>
