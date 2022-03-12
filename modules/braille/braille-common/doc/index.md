<link rel="dp2:permalink" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Braille/">
<link rev="dp2:doc" href="../src/main/java/org/daisy/pipeline/braille/common/BrailleTranslator.java"/>
<link rev="dp2:doc" href="../src/main/java/org/daisy/pipeline/braille/common/Hyphenator.java"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/userdoc"/>

# Braille transformation

DAISY Pipeline can convert an XML document to a paginated braille
document. Both the author of the document and the user of DAISY
Pipeline can influence the rendering through [CSS][].

The user can further control different aspects of the transformation,
with a "[transformer
query](../src/main/resources/xml/data-types/transform-query.xml)".

## Braille formatting

Braille formatting or layout includes line breaking, page breaking,
volume breaking, indentation, spacing, page layout, etc. The layout is
primarily controlled by the CSS, but different formatter
implementations may support a different subset or superset of CSS, or
may have implementation specific parameters. The known formatter
implementations are:

- [`(formatter:dotify)`](http://daisy.github.io/pipeline/Get-Help/User-Guide/Braille/Dotify/)
<!-- `(formatter:liblouis)`: hidden from the user because the implementation is very incomplete -->

## Braille transcription

Braille transcription or translation is the transformation of normal
text into braille script. It is locale dependent and can be further
configured by the user with parameters such as the contraction
grade. These parameters can be provided through CSS and/or through the
transformer query. Braille transciption is influenced only by
text-level CSS styles, such as `@text-transform` rules and
[`text-transform`](http://braillespecs.github.io/braille-css/#h3_the-text-transform-property)
properties. The known translator implementations are:

- [`(translator:liblouis)`](http://daisy.github.io/pipeline/Get-Help/User-Guide/Braille/Liblouis/)
- [`(translator:dotify)`](http://daisy.github.io/pipeline/Get-Help/User-Guide/Braille/Dotify/#dotify-based-braille-transcription)

## Hyphenation

Hypenation, or line breaking within words, can be allowed or
disallowed using the
[`hyphens`](http://braillespecs.github.io/braille-css/#h3_breaking-within-words)
CSS property. Hyphenation rules depend on the locale and also on the
braille system. The known hyphenator implementations are:

- [`(hyphenator:hyphen)`](http://daisy.github.io/pipeline/Get-Help/User-Guide/Braille/Hyphenation/Hyphen/)
- [`(hyphenator:tex)`](http://daisy.github.io/pipeline/Get-Help/User-Guide/Braille/Hyphenation/TeX/)
- [`(hyphenator:dotify)`](http://daisy.github.io/pipeline/Get-Help/User-Guide/Braille/Dotify/#dotify-based-hyphenation)
<!-- `(hyphenator:liblouis)`: hidden from the user because I'd like to remove the ability to do hyphenation from Liblouis -->


[CSS]: http://braillespecs.github.io/braille-css
