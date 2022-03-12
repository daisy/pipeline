<link rel="dp2:permalink" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Braille/Hyphenation/Hyphen/"/>
<link rev="dp2:doc" href="../src/main/java/org/daisy/pipeline/braille/libhyphen/impl/LibhyphenJnaImpl.java"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/userdoc"/>

# Hyphen based hyphenation

[Hyphen][] based
[hyphenators](http://daisy.github.io/pipeline/Get-Help/User-Guide/Braille/#hyphenation)
can be selected using a query that contains
`(hyphenator:hyphen)(table:...)` or `(hyphen-table:...)`. Recognized
features are:

<!-- id:  If present it must be the only feature. Matches a hyphenator with a unique ID. -->

`hyphenator`
: Will only match if the value is "hyphen" or "libhyphen", or if the
  value is a Hyphen table URI (see `libhyphen-table` feature
  below). <!-- or if it's a hyphenator's ID -->

`table`
`hyphen-table`
`libhyphen-table`
: A Hyphen table is a URI that can be either a file name, a file path
  relative to a registered table path, an absolute file URI, or a
  fully qualified table identifier. A collection of tables used in
  LibreOffice is available under the path
  [`http://www.libreoffice.org/dictionaries/hyphen/`](../src/main/resources/tables/). This
  feature is not compatible with other features except `hyphenator`.

`locale`
: Matches only hyphenators with that locale.


[Hyphen]: http://hunspell.github.io/
