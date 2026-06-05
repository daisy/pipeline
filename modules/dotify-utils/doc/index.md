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
[hyphenator](http://daisy.github.io/pipeline/Get-Help/User-Guide/Braille/#hyphenation). By
default, the document locale is used to select the translator and
hyphenator.

# Dotify based braille transcription

Dotify does not only format but also transcribes to braille. Dotify
based [braille
translators](http://daisy.github.io/pipeline/Get-Help/User-Guide/Braille/#braille-transcription)
can be selected using a transformer query that contains
`(translator:dotify)`. Recognized features are:

`translator`
: Will only match if the value is "dotify"

`language`
: The primary target language of the Liblouis translator. Must be a
  [RFC 5646](https://tools.ietf.org/html/rfc5646) language tag. By
  default the document locale is used. In that case an automatic
  fallback mechanism is used: if nothing is found for
  language-COUNTRY-variant, then language-COUNTRY is searched, then
  language. If the document locale is of the form language-COUNTRY,
  only the language subtag is used.

`region`
: The region or community in which the braille code applies. Must be a
  [RFC 5646](https://tools.ietf.org/html/rfc5646) language tag. By
  default the document locale is used if it is of the form
  language-COUNTRY. (The same automatic fallback mechanism as
  described above is used.)

`locale`
: Shorthand for `language` and `region`.

`force-pre-translation`
: A value "true" will enable the pre-translation step. Disabled by default.

No other features are allowed.


[Dotify]: https://github.com/mtmse/dotify.formatter.impl
[OBFL]: https://mtmse.github.io/obfl/
