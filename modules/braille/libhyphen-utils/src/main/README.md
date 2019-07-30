# libhyphen-utils API

## Java API

- package <a href="java/org/daisy/pipeline/braille/libhyphen/" class="apidoc"><code>org.daisy.pipeline.braille.libhyphen</code></a>

## <a href="resources/META-INF/catalog.xml" class="source">catalog.xml</a>

- <a href="resources/xml/library.xsl" class="apidoc">`http://www.daisy.org/pipeline/modules/braille/libhyphen-utils/library.xsl`</a>

## OSGi services

### Hyphen table paths ([`org.daisy.pipeline.braille.libhyphen.LibhyphenTablePath`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/libhyphen/LibhyphenTablePath.html))

- <a href="resources/hyphen/" class="apidoc">`http://www.libreoffice.org/dictionaries/hyphen/`</a>

  A standard collection of hyphenation tables used in LibreOffice

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

### Saxon functions (`net.sf.saxon.lib.ExtensionFunctionDefinition`)

- [`{http://hunspell.sourceforge.net/Hyphen}hyphenate`](java/org/daisy/pipeline/braille/libhyphen/saxon/impl/HyphenateDefinition.java)



<link rev="dp2:doc" href="./"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/apidoc"/>
