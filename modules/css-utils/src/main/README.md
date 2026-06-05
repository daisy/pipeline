# css-utils

- package [`org.daisy.pipeline.css`](java/org/daisy/pipeline/css/)

## [catalog.xml](resources/META-INF/catalog.xml)

- [`http://www.daisy.org/pipeline/modules/css-utils/library.xpl`](resources/xml/library.xpl)

## XSLT packages

- [`http://www.daisy.org/pipeline/modules/css-utils/library.xsl`](resources/xml/library.xsl)

## Saxon XPath functions ([`net.sf.saxon.lib.ExtensionFunctionDefinition`](https://www.saxonica.com/html/documentation9.8/javadoc/net/sf/saxon/lib/ExtensionFunctionDefinition.html))

- [`{http://www.daisy.org/ns/pipeline/functions}media-query-matches`](java/org/daisy/pipeline/css/saxon/impl/CssMediaFunctionProvider.java)

  Test whether a media query matches a medium, see [XSLT documentation](resources/xml/library.xsl)

## XMLCalabash XProc steps ([`org.daisy.common.xproc.calabash.XProcStepProvider`](http://daisy.github.io/pipeline/api/org/daisy/common/xproc/calabash/XProcStepProvider.html))

- [`{http://www.daisy.org/ns/pipeline/xproc/internal}css-cascade`](java/org/daisy/pipeline/css/calabash/impl/CssCascadeStep.java)

  Used in [`{http://www.daisy.org/ns/pipeline/xproc}css-cascade`](resources/xml/css-cascade.xpl)


<link rev="dp2:doc" href="./"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/apidoc"/>
