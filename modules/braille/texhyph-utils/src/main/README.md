# texhyph-utils

## Java API

- package [`org.daisy.pipeline.braille.tex`](java/org/daisy/pipeline/braille/tex/)

## [catalog.xml](resources/META-INF/catalog.xml)

- [`http://www.daisy.org/pipeline/modules/braille/texhyph-utils/library.xsl`](resources/xml/library.xsl)

## Transformers ([`org.daisy.pipeline.braille.common.TransformProvider`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/common/TransformProvider.html))

- [`(hyphenator:tex)`](java/org/daisy/pipeline/braille/tex/impl/TexHyphenatorSimpleImpl.java)
  
  TeX based hyphenator, see [user documentation](../../doc/)
  
- [`(hyphenator:tex)`](java/org/daisy/pipeline/braille/tex/impl/TexHyphenatorDotifyImpl.java)
  
  TeX based hyphenator with extended functionality, see [user documentation](../../doc/)

## Saxon XPath functions ([`net.sf.saxon.lib.ExtensionFunctionDefinition`](https://www.saxonica.com/html/documentation9.8/javadoc/net/sf/saxon/lib/ExtensionFunctionDefinition.html))

- [`{http://code.google.com/p/texhyphj/}hyphenate`](java/org/daisy/pipeline/braille/tex/saxon/impl/HyphenateDefinition.java)

  Hyphenate a text string using texhyphj, see [XSLT documentation](resources/xml/library.xsl)


<link rev="dp2:doc" href="./"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/tex/impl/TexHyphenatorSimpleImpl.java"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/tex/impl/TexHyphenatorDotifyImpl.java"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/tex/saxon/impl/HyphenateDefinition.java"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/apidoc"/>
