# pef-utils API

## <a href="resources/META-INF/catalog.xml" class="source">catalog.xml</a>

- <a href="resources/xml/library.xsl" class="apidoc">`http://www.daisy.org/pipeline/modules/braille/pef-utils/library.xsl`</a>
  - `pef:encode`: Re-encode a Braille string (Unicode Braille) using a
    specified character set.

- <a href="resources/xml/library.xpl" class="apidoc">`http://www.daisy.org/pipeline/modules/braille/pef-utils/library.xpl`</a>
  - `pef:pef2text`: Convert a PEF document into a textual
    (ASCII-based) format.
  - `pef:text2pef`: Convert an ASCII-based Braille format into PEF.
  - `pef:validate`: Validate a PEF document.
  - `pef:merge`: Merge PEF documents on volume- or section-level.
  - `pef:store`: Store a PEF document to disk, possibly in an
    ASCII-based format or with an HTML preview.
  - `pef:compare`: Compare two PEF documents.
  - `x:pef-compare`: Compare two PEF documents as a custom
    [XProcSpec][] assertion.


<link rev="dp2:doc" href="./"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/apidoc"/>
