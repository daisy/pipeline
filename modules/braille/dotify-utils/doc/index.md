<link rel="dp2:permalink" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Braille/Dotify/">
<link rev="dp2:doc" href="../src/main/java/org/daisy/pipeline/braille/dotify/impl/DotifyCSSStyledDocumentTransform.java"/>
<link rev="dp2:doc" href="../src/main/java/org/daisy/pipeline/braille/dotify/impl/DotifyTranslatorImpl"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/userdoc"/>

# Dotify based braille formatting

The [Dotify][] braille formatting engine is an implementation of the
Open Braille Formatting Language, [OBFL][].

DAISY Pipeline provides a Dotify based
[formatter](http://daisy.github.io/pipeline/Get-Help/User-Guide/Braille/#braille-formatting)
that internally converts the input document (styled with CSS) into the
OBFL format. Scripts provide the option to retreive this intermediary
OBFL document.

The formatting is preceded by an optional "pre-translation" step in
which text is transcribed to braille if possible,
[block](https://www.w3.org/TR/2007/WD-css3-box-20070809/#block-level)
per block. The result of this step is a copy of the input document
with some text nodes translated to braille, and some text-level CSS
styles changed. Invisible control characters such as [soft
hyphens](https://www.unicode.org/reports/tr14/tr14-39.html#SoftHyphen)
may also have been inserted. Any text that could not be pre-translated
(or that does not exist yet because it is generated during formatting)
is translated during the formatting phase.

The Dotify based formatter can be selected using a transformer query
that contains `(formatter:dotify)`. Recognized features are:

`formatter`
: Will only match if the value is "dotify".

The remaining features are used for selecting the [braille
translator](http://daisy.github.io/pipeline/Get-Help/User-Guide/Braille/#braille-transcription)
and
[hyphenator](http://daisy.github.io/pipeline/Get-Help/User-Guide/Braille/#hyphenation).

# Dotify based braille transcription

Dotify does not only format but also transcribes to braille. Dotify
based [braille
translators](http://daisy.github.io/pipeline/Get-Help/User-Guide/Braille/#braille-transcription)
can be selected using a transformer query that contains
`(translator:dotify)`. Recognized features are:

`translator`
: Will only match if the value is "dotify"

`locale`
: Required.
: Matches only Dotify translators for that locale. An automatic
  fallback mechanism is used: if nothing is found for
  language-COUNTRY-variant, then language-COUNTRY is searched, then
  language.

`hyphenator`
: A value "none" will disable hyphenation. "auto" is the default and
  will match any Dotify translator, whether it supports hyphenation
  [out-of-the-box](#dotify-based-hyphenation), with the help of an
  external
  [hyphenator](http://daisy.github.io/pipeline/Get-Help/User-Guide/Braille/#hyphenation),
  or not at all. A value not equal to "none" or "auto" will match
  every Dotify translator that uses an external hyphenator that
  matches this feature.

`force-pre-translation`
: A value "true" will enable the pre-translation step. Disabled by default.

No other features are allowed.

# Dotify based hyphenation

Dotify based
[hyphenators](http://daisy.github.io/pipeline/Get-Help/User-Guide/Braille/#hyphenation)
can be selected using a transformer query that contains
`(hyphenator:dotify)`. Recognized features are:

`hyphenator`
: Will only match if the value is "dotify".

`locale`
: Required.
: Matches only Dotify hyphenators for that locale. An automatic
  fallback mechanism is used: if nothing is found for
  language-COUNTRY-variant, then language-COUNTRY is searched, then
  language.

No other features are allowed.


[Dotify]: https://github.com/mtmse/dotify.formatter.impl
[OBFL]: https://mtmse.github.io/obfl/
