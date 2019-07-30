# pef-utils API

## Java API

- package <a href="java/org/daisy/pipeline/braille/pef/" class="apidoc">`org.daisy.pipeline.braille.pef`</a>

## <a href="resources/META-INF/catalog.xml" class="source">catalog.xml</a>

- <a href="resources/xml/library.xsl" class="apidoc">`http://www.daisy.org/pipeline/modules/braille/pef-utils/library.xsl`</a>
- <a href="resources/xml/library.xpl" class="apidoc">`http://www.daisy.org/pipeline/modules/braille/pef-utils/library.xpl`</a>

## OSGi services

### PEF tables (`org.daisy.pipeline.braille.pef.TableProvider`)

- [`(id:...)`](java/org/daisy/pipeline/braille/pef/impl/BrailleUtilsTableCatalog.java)
- [`(locale:...)`](java/org/daisy/pipeline/braille/pef/impl/LocaleBasedTableProvider.java)

### Saxon functions (`net.sf.saxon.lib.ExtensionFunctionDefinition`)

- [`{http://www.daisy.org/ns/2008/pef}encode`](java/org/daisy/pipeline/braille/pef/saxon/impl/EncodeDefinition.java)

### Calabash steps (`org.daisy.common.xproc.calabash.XProcStepProvider`)

- [`{http://www.daisy.org/ns/2008/pef}pef2text`](java/org/daisy/pipeline/braille/pef/calabash/impl/PEF2TextStep.java)
- [`{http://www.daisy.org/ns/2008/pef}text2pef`](java/org/daisy/pipeline/braille/pef/calabash/impl/Text2PEFStep.java)
- [`{http://www.daisy.org/ns/2008/pef}validate`](java/org/daisy/pipeline/braille/pef/calabash/impl/ValidateStep.java)



<link rev="dp2:doc" href="./"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/apidoc"/>
