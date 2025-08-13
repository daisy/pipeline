# pef-utils

## Java API

- package [`org.daisy.pipeline.braille.pef`](java/org/daisy/pipeline/braille/pef/)

## [catalog.xml](resources/META-INF/catalog.xml)

- [`http://www.daisy.org/pipeline/modules/braille/pef-utils/library.xsl`](resources/xml/library.xsl)
- [`http://www.daisy.org/pipeline/modules/braille/pef-utils/library.xpl`](resources/xml/library.xpl)

## PEF tables ([`org.daisy.pipeline.braille.pef.TableProvider`](java/org/daisy/pipeline/braille/pef/TableProvider.java))

- [`(id:...)`](java/org/daisy/pipeline/braille/pef/impl/BrailleUtilsTableCatalog.java)

  Provides [PEF
  tables](https://mtmse.github.io/dotify.api/latest/javadoc/org/daisy/dotify/api/table/Table.html)
  that are selectable via their unique ID (as defined by the
  `getIdentifier` method).

  The list of known table IDs is: <!-- see listAllTables() in BrailleUtilsFileFormatCatalogTest.java -->

  - `com_braillo.BrailloTableProvider.TableType.BRAILLO_6DOT_001_00`: Braillo USA 6 DOT 001.00
  - `com_braillo.BrailloTableProvider.TableType.BRAILLO_6DOT_031_01`: Braillo NETHERLANDS 6 DOT 031.01
  - `com_braillo.BrailloTableProvider.TableType.BRAILLO_6DOT_044_00`: Braillo ENGLAND 6 DOT 044.00
  - `com_braillo.BrailloTableProvider.TableType.BRAILLO_6DOT_046_01`: Braillo SWEDEN 6 DOT 046.01
  - `com_braillo.BrailloTableProvider.TableType.BRAILLO_6DOT_047_01`: Braillo NORWAY 6 DOT 047.01
  - `com_indexbraille.IndexTableProvider.TableType.INDEX_TRANSPARENT_6DOT`: Index transparent 6 dot
  - `com_viewplus.ViewPlusTableProvider.TableType.TIGER_INLINE_SUBSTITUTION_8DOT`: Tiger inline substitution 8-dot
  - `com_yourdolphin.SupernovaTableProvider.TableType.SV_SE_6DOT`: Swedish - Supernova 6 dot
  - `es_once_cidat.CidatTableProvider.TableType.IMPACTO_TRANSPARENT_6DOT`: Transparent mode
  - `es_once_cidat.CidatTableProvider.TableType.IMPACTO_TRANSPARENT_8DOT`: Transparent mode (8 dot)
  - `es_once_cidat.CidatTableProvider.TableType.PORTATHIEL_TRANSPARENT_6DOT`: Transparent mode
  - `org.daisy.braille.impl.table.DefaultTableProvider.TableType.EN_US`: US
  - `org_daisy.BrailleEditorsTableProvider.TableType.MICROBRAILLE`: MicroBraille BRL-file
  - `org_daisy.EmbosserTableProvider.TableType.CS_CZ`: Czech
  - `org_daisy.EmbosserTableProvider.TableType.DA_DK`: Danish
  - `org_daisy.EmbosserTableProvider.TableType.DE_DE`: German
  - `org_daisy.EmbosserTableProvider.TableType.EN_GB`: British
  - `org_daisy.EmbosserTableProvider.TableType.ES_ES_TABLE_2`: Spanish (modern)
  - `org_daisy.EmbosserTableProvider.TableType.ES_ES`: Spanish (classic)
  - `org_daisy.EmbosserTableProvider.TableType.IT_IT_FIRENZE`: Italian
  - `org_daisy.EmbosserTableProvider.TableType.MIT`: US (MIT)
  - `org_daisy.EmbosserTableProvider.TableType.NABCC_8DOT`: US (NABCC 8 dot)
  - `org_daisy.EmbosserTableProvider.TableType.NABCC`: US (NABCC)
  - `org_daisy.EmbosserTableProvider.TableType.UNICODE_BRAILLE`: Unicode braille
  - `se_tpb.CXTableProvider.TableType.SV_SE_CX`: Swedish CX

- [`(locale:...)`](java/org/daisy/pipeline/braille/pef/impl/LocaleBasedTableProvider.java)

  Provides PEF tables by locale. This is currently implemented using a
  fixed mapping from locale to ID:

  - en: `org.daisy.braille.impl.table.DefaultTableProvider.TableType.EN_US`
  - nl: `com_braillo.BrailloTableProvider.TableType.BRAILLO_6DOT_031_01`

## Saxon XPath functions ([`net.sf.saxon.lib.ExtensionFunctionDefinition`](https://www.saxonica.com/html/documentation9.8/javadoc/net/sf/saxon/lib/ExtensionFunctionDefinition.html))

- [`{http://www.daisy.org/ns/2008/pef}encode`](java/org/daisy/pipeline/braille/pef/saxon/impl/EncodeDefinition.java)

  Encode a (Unicode) braille string using the specified character set, see [XSLT documentation](resources/xml/library.xsl)

- [`{http://www.daisy.org/ns/2008/pef}decode`](java/org/daisy/pipeline/braille/pef/saxon/impl/DecodeDefinition.java)

  Decode a braille string in the specified character set (to Unicode braille), see [XSLT documentation](resources/xml/library.xsl)

- [`{http://www.daisy.org/ns/2008/pef}get-table-id`](java/org/daisy/pipeline/braille/pef/saxon/impl/GetTableIdDefinition.java)

## XMLCalabash XProc steps ([`org.daisy.common.xproc.calabash.XProcStepProvider`](http://daisy.github.io/pipeline/api/org/daisy/common/xproc/calabash/XProcStepProvider.html))

- [`{http://www.daisy.org/ns/pipeline/xproc}pef-validate`](java/org/daisy/pipeline/braille/pef/calabash/impl/ValidateStep.java)

  Validate a PEF document, see [XProc documentation](resources/xml/validate.xpl)


<link rev="dp2:doc" href="./"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/pef/calabash/impl/PEF2TextStep.java"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/pef/calabash/impl/Text2PEFStep.java"/>
<link rev="dp2:doc" href="java/org/daisy/pipeline/braille/pef/calabash/impl/ValidateStep.java"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/apidoc"/>
