# braille-css-utils

## Java API

- package [`org.daisy.pipeline.braille.css`](java/org/daisy/pipeline/braille/css/)

## [catalog.xml](resources/META-INF/catalog.xml)

### XProc

- [`http://www.daisy.org/pipeline/modules/braille/css-utils/library.xpl`](resources/xml/library.xpl)

### XSLT

- [`http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl`](resources/xml/library.xsl): Utility functions for CSS-parsing.

## CSS cascaders [`org.daisy.pipeline.css.CssCascader`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/css/CssCascader.html)

- [`media="embossed"`](java/org/daisy/pipeline/braille/css/impl/BrailleCssCascader.java)

  [`CssCascader`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/css/CssCascader.html)
  that supports media "embossed" and "print". Only a very small subset
  of medium "print" is supported though, namely the properties color,
  font-style, font-weight, text-decoration.

## Saxon XPath functions ([`net.sf.saxon.lib.ExtensionFunctionDefinition`](https://www.saxonica.com/html/documentation9.8/javadoc/net/sf/saxon/lib/ExtensionFunctionDefinition.html))

- [`{http://www.daisy.org/ns/pipeline/braille-css}parse-stylesheet`](java/org/daisy/pipeline/braille/css/saxon/impl/ParseStylesheetDefinition.java)

  Parse a style sheet, see [XSLT documentation](resources/xml/library.xsl)

- [`{http://www.daisy.org/ns/pipeline/braille-css}render-table-by`](java/org/daisy/pipeline/braille/css/saxon/impl/RenderTableByDefinition.java)

  Render a table as a (nested) list, see [XSLT documentation](resources/xml/library.xsl)


<link rev="dp2:doc" href="./"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/css/impl/BrailleCssCascader.java"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/css/saxon/impl/ParseStylesheetDefinition.java"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/css/saxon/impl/RenderTableByDefinition.java"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/apidoc"/>
