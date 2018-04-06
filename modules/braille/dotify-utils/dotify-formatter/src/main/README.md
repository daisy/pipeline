# dotify-formatter API

## OSGi services

### Transformers ([`org.daisy.pipeline.braille.common.TransformProvider`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/common/TransformProvider.html))

- [`(input:css)(output:pef)(formatter:dotify)`](java/org/daisy/pipeline/braille/dotify/impl/DotifyCSSStyledDocumentTransform.java)
  
  Recognized features:
  
  - `formatter`: Will only match if the value is `dotify`.
  
  Other features are used for finding sub-transformers of type BrailleTranslator.
  


<link rev="dp2:doc" href="./"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/apidoc"/>
