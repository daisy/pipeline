# common-utils

## Java API

- package [`org.daisy.pipeline.braille.common`](java/org/daisy/pipeline/braille/common/)

## [catalog.xml](resources/META-INF/catalog.xml)

- [`http://www.daisy.org/pipeline/modules/braille/common-utils/library.xsl`](resources/xml/library.xsl)
- [`http://www.daisy.org/pipeline/modules/braille/common-utils/library.xpl`](resources/xml/library.xpl)

## Saxon XPath functions ([`net.sf.saxon.lib.ExtensionFunctionDefinition`](https://www.saxonica.com/html/documentation9.8/javadoc/net/sf/saxon/lib/ExtensionFunctionDefinition.html))

- [`{http://www.daisy.org/ns/pipeline/functions}text-transform`](java/org/daisy/pipeline/braille/common/saxon/impl/TextTransformDefinition.java)

  Apply a text transformer to a string sequence, see [XSLT documentation](resources/xml/library.xsl)

- [`{http://www.daisy.org/ns/pipeline/functions}message`](java/org/daisy/pipeline/braille/common/saxon/impl/MessageDefinition.java)

  Create a log message, see [XSLT documentation](resources/xml/library.xsl)

- [`{http://www.daisy.org/ns/pipeline/functions}progress`](java/org/daisy/pipeline/braille/common/saxon/impl/ProgressDefinition.java)

  Update the progress, see [XSLT documentation](resources/xml/library.xsl)

## XMLCalabash XProc steps ([`org.daisy.common.xproc.calabash.XProcStepProvider`](http://daisy.github.io/pipeline/api/org/daisy/common/xproc/calabash/XProcStepProvider.html))

- [`{http://www.daisy.org/ns/pipeline/xproc}transform`](java/org/daisy/pipeline/braille/common/calabash/impl/PxTransformStep.java)

  Apply an XML transformer to a node, see [XProc documentation](resources/xml/library.xpl)

- [`{http://www.daisy.org/ns/pipeline/xproc}parse-query`](java/org/daisy/pipeline/braille/common/calabash/impl/PxParseQueryStep.java)

  Parse a query string and convert it to a `c:param-set` document, see [XProc documentation](resources/xml/library.xpl)


<link rev="dp2:doc" href="./"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/common/saxon/impl/TextTransformDefinition.java"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/common/saxon/impl/MessageDefinition.java"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/common/saxon/impl/ProgressDefinition.java"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/common/calabash/impl/PxTransformStep.java"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/common/calabash/impl/PxParseQueryStep.java"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/apidoc"/>
