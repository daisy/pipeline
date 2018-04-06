# liblouis-formatter API

## OSGi services

### Transformers ([`org.daisy.pipeline.braille.common.TransformProvider`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/common/TransformProvider.html))

- [`(input:css)(output:pef)(formatter:liblouis)`](java/org/daisy/pipeline/braille/liblouis/impl/LiblouisCSSStyledDocumentTransform.java):
  Converts an XML document with inline Braille CSS to PEF using liblouisutdml.
  
  Recognized features:

  - `formatter`: Will only match if the value is `liblouis`.

  Other features are used for finding sub-transformers of type
  [BrailleTranslator](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/common/BrailleTranslator.html).



<link rev="dp2:doc" href="./"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/apidoc"/>
