# dotify-utils

## Java API

- package [`org.daisy.pipeline.braille.dotify`](java/org/daisy/pipeline/braille/dotify/)

## [catalog.xml](resources/META-INF/catalog.xml)

- [`http://www.daisy.org/pipeline/modules/braille/dotify-utils/library.xpl`](resources/xml/library.xpl)

## Transformers ([`org.daisy.pipeline.braille.common.TransformProvider`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/common/TransformProvider.html))

- [`(input:css)(output:pef)(formatter:dotify)`](java/org/daisy/pipeline/braille/dotify/impl/DotifyCSSStyledDocumentTransform.java)

  Dotify based braille formatter, see [user documentation](../../doc/)
  
- [`(input:text-css)(output:braille)(translator:dotify)`](java/org/daisy/pipeline/braille/dotify/impl/DotifyTranslatorImpl.java)

  Dotify based braille translator, see [user documentation](../../doc/)

## XMLCalabash XProc steps ([`org.daisy.common.xproc.calabash.XProcStepProvider`](http://daisy.github.io/pipeline/api/org/daisy/common/xproc/calabash/XProcStepProvider.html))

- [`{http://www.daisy.org/ns/pipeline/xproc/internal}obfl-to-pef`](java/org/daisy/pipeline/braille/dotify/calabash/impl/OBFLToPEFStep.java)

  See [XProc documentation](resources/xml/library.xpl)

- [`{http://www.daisy.org/ns/pipeline/xproc/internal}shift-obfl-marker`](java/org/daisy/pipeline/braille/dotify/calabash/impl/ShiftObflMarkerStep.java)

  Used in [resources/xml/shift-obfl-marker.xpl](resources/xml/shift-obfl-marker.xpl)


[Dotify]: https://github.com/mtmse/dotify.formatter.impl

<link rev="dp2:doc" href="./"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/dotify/impl/DotifyCSSStyledDocumentTransform.java"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/dotify/impl/DotifyTranslatorImpl.java"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/dotify/impl/DotifyHyphenatorImpl.java"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/dotify/calabash/impl/OBFLToPEFStep.java"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/dotify/calabash/impl/FileToOBFLStep.java"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/apidoc"/>
