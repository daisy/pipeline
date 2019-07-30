# texhyph-utils API

## Java API

- package <a href="java/org/daisy/pipeline/braille/tex/" class="apidoc"><code>org.daisy.pipeline.braille.tex</code></a>

### Transformers ([`org.daisy.pipeline.braille.common.TransformProvider`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/common/TransformProvider.html))

- [`(hyphenator:tex)`](java/org/daisy/pipeline/braille/tex/impl/TexHyphenatorSimpleImpl.java)
  
  Recognized features:
  
  - `hyphenator`: Will only match if the value is `tex` or `texhyph`.
  - `table`: A tex table is a URI that is either a file name, a file
      path relative to a registered tablepath, an absolute file URI,
      or a fully qualified table identifier. Only URIs that point to
      LaTeX pattern files (ending with ".tex") are matched. The
      `table` feature is not compatible with `locale`.
  - `locale`: Matches only hyphenators with that locale.
  
  No other features are allowed.
  
- [`(hyphenator:tex)`](java/org/daisy/pipeline/braille/tex/impl/TexHyphenatorDotifyImpl.java)
  
  Recognized features:
  
  - `hyphenator`: Will only match if the value is `tex` or `texhyph`.
  - `table`: A tex table is a URI that is be either a file name, a
      file path relative to a registered tablepath, an absolute file
      URI, or a fully qualified table identifier. A URI can either
      point to a LaTeX pattern file (".tex") or a Java properties file
      (".xml" or ".properties") that Dotify uses as the format for
      storing hyphenator configurations. The `table` feature is not
      compatible with `locale`.
  - `locale`: Matches only hyphenators with that locale.
  
  No other features are allowed.

## <a href="resources/META-INF/catalog.xml" class="source">catalog.xml</a>

- <a href="resources/xml/library.xsl" class="apidoc">`http://www.daisy.org/pipeline/modules/braille/texhyph-utils/library.xsl`</a>

## OSGi services

### Saxon functions (`net.sf.saxon.lib.ExtensionFunctionDefinition`)

- [`{http://code.google.com/p/texhyphj/}hyphenate`](java/org/daisy/pipeline/braille/tex/saxon/impl/HyphenateDefinition.java)


<link rev="dp2:doc" href="./"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/apidoc"/>
