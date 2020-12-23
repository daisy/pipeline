# css-utils API

- package <a href="java/org/daisy/pipeline/css/" class="apidoc"><code>org.daisy.pipeline.css</code></a>

## <a href="resources/META-INF/catalog.xml" class="source">catalog.xml</a>

- <a href="resources/xml/library.xpl" class="apidoc">`http://www.daisy.org/pipeline/modules/css-utils/library.xpl`</a>
- <a href="resources/xml/library.xsl" class="apidoc">`http://www.daisy.org/pipeline/modules/css-utils/library.xsl`</a>

## OSGi services

### Saxon functions (`net.sf.saxon.lib.ExtensionFunctionDefinition`)

- [`{http://www.daisy.org/ns/pipeline/functions}media-query-matches`](java/org/daisy/pipeline/css/saxon/impl/MediaQueryMatchesDefinition.java)

### Calabash steps ([`org.daisy.common.xproc.calabash.XProcStepProvider`](http://daisy.github.io/pipeline/api/org/daisy/common/xproc/calabash/XProcStepProvider.html))

- [`{http://www.daisy.org/ns/pipeline/xproc/internal}css-cascade`](java/org/daisy/pipeline/css/calabash/impl/CssCascadeStep.java)
