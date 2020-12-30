<link rel="dp2:permalink" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Braille/Hyphenation/TeX/"/>
<link rev="dp2:doc" href="../src/main/java/org/daisy/pipeline/braille/tex/impl/TexHyphenatorSimpleImpl.java"/>
<link rev="dp2:doc" href="../src/main/java/org/daisy/pipeline/braille/tex/impl/TexHyphenatorDotifyImpl.java"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/userdoc"/>

# TeX based hyphenation

DAISY Pipeline can perform hyphenation using a reimplementation of
[Frank Liang's hyphenation algorithm][Liang] used in [TeX][]. TeX
[hyphenators](http://daisy.github.io/pipeline/Get-Help/User-Guide/Braille/#hyphenation)
can be selected using a query that contains
`(hyphenator:tex)`. Recognized features are:

`hyphenator`
: Will only match if the value is "tex" or "texhyph".

`table`
: A tex table is a URI that is either a file name, a file path
  relative to a registered tablepath, an absolute file URI, or a fully
  qualified table identifier. A URI can either point to a TeX
  hyphenation pattern file (".tex") or a Java properties file (".xml"
  or ".properties") that [Dotify][] uses as the format for storing
  hyphenator configurations. The `table` feature is not compatible
  with `locale`.

`locale`
: Matches only hyphenators with this locale.

No other features are allowed.


[TeX]: http://www.tug.org
[Liang]: http://tug.org/docs/liang
[Dotify]: https://github.com/mtmse/dotify.hyphenator.impl
