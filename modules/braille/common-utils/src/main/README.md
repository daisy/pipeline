# common-utils API

General purpose building blocks.

## <a href="resources/META-INF/catalog.xml" class="source">catalog.xml</a>

- <a href="resources/xml/library.xsl" class="apidoc">`http://www.daisy.org/pipeline/modules/braille/common-utils/library.xsl`</a>
  - `pf:transform`: Query a text transformer and apply it to a string.
  - `brl:unicode-braille-to-escape-sequence`
  - `brl:unicode-braille-to-nabcc`
  - `brl:nabcc-to-unicode-braille`

- <a href="resources/xml/library.xpl" class="apidoc">`http://www.daisy.org/pipeline/modules/braille/common-utils/library.xpl`</a>
  - `px:transform`: Query an XML transformer of a certain type and apply it to a node.
  - `px:mark-transitions`
  - `px:select-by-base`
  - `px:select-by-positions`
  - `px:validate-braille`
  - `px:xslt-for-each`

## Java API

- package <a href="java/org/daisy/pipeline/braille/common/" class="apidoc">`org.daisy.pipeline.braille.common`</a>

## OSGi services

### Saxon functions (`net.sf.saxon.lib.ExtensionFunctionDefinition`)

- [`{http://www.daisy.org/ns/pipeline/functions}text-transform`](java/org/daisy/pipeline/braille/common/saxon/impl/TextTransformDefinition.java)

### Calabash steps (`org.daisy.common.xproc.calabash.XProcStepProvider`)

- [`{http://www.daisy.org/ns/pipeline/xproc}transform`](java/org/daisy/pipeline/braille/common/calabash/impl/PxTransformStep.java)


<link rev="dp2:doc" href="./"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/apidoc"/>
