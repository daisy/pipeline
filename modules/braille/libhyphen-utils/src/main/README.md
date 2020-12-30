# libhyphen-utils

## Java API

- package [`org.daisy.pipeline.braille.libhyphen`](java/org/daisy/pipeline/braille/libhyphen/)

## [catalog.xml](resources/META-INF/catalog.xml)

- [`http://www.daisy.org/pipeline/modules/braille/libhyphen-utils/library.xsl`](resources/xml/library.xsl)

## Hyphen table paths ([`org.daisy.pipeline.braille.libhyphen.LibhyphenTablePath`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/libhyphen/LibhyphenTablePath.html))

- [`http://www.libreoffice.org/dictionaries/hyphen/`](resources/hyphen/)

  A standard collection of hyphenation tables used in LibreOffice

## Transformers ([`org.daisy.pipeline.braille.common.TransformProvider`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/common/TransformProvider.html))

- [`(hyphenator:hyphen)`](java/org/daisy/pipeline/braille/libhyphen/impl/LibhyphenJnaImpl.java)

  Hyphen based hyphenator, see [user documentation](../../doc/)

## Saxon XPath functions ([`net.sf.saxon.lib.ExtensionFunctionDefinition`](https://www.saxonica.com/html/documentation9.8/javadoc/net/sf/saxon/lib/ExtensionFunctionDefinition.html))

- [`{http://hunspell.sourceforge.net/Hyphen}hyphenate`](java/org/daisy/pipeline/braille/libhyphen/saxon/impl/HyphenateDefinition.java)

  Hyphenate a text string using Hyphen, see [XSLT documentation](resources/xml/library.xsl)


[Hyphen]: http://hunspell.github.io/

<link rev="dp2:doc" href="./"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/libhyphen/saxon/impl/HyphenateDefinition.java"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/apidoc"/>
