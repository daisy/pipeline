# libhyphen-core API

## Java API

- package <a href="java/org/daisy/pipeline/braille/libhyphen/" class="apidoc"><code>org.daisy.pipeline.braille.libhyphen</code></a>

## OSGi services

### Transformers ([`org.daisy.pipeline.braille.common.TransformProvider`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/common/TransformProvider.html))

- [`(hyphenator:hyphen)`](java/org/daisy/pipeline/braille/libhyphen/impl/LibhyphenJnaImpl.java)
  
  Recognized features:
  
  - `id`: If present it must be the only feature. Matches a hyphenator
      with a unique ID.
  - `hyphenator`: Will only match if the value is `hyphen`, or if it's
      a hyphenator's ID.
  - `table` or `libhyphen-table`: A Hyphen table is a URI that can be
      either a file name, a file path relative to a registered table
      path, an absolute file URI, or a fully qualified table
      identifier. This feature is not compatible with other features
      except `hyphenator`.
  - `locale`: Matches only hyphenators with that locale.



<link rev="dp2:doc" href="./"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/apidoc"/>
