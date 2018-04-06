# dotify-core API

## Java API

- package <a href="java/org/daisy/pipeline/braille/dotify/" class="apidoc"><code>org.daisy.pipeline.braille.dotify</code></a>

## OSGi services

### Transformers ([`org.daisy.pipeline.braille.common.TransformProvider`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/common/TransformProvider.html))

- [`(input:text-css)(output:braille)(translator:dotify)`](java/org/daisy/pipeline/braille/dotify/impl/DotifyTranslatorImpl.java)
  
  Recognized features:
  
  - `translator`: Will only match if the value is `dotify`.
  - `locale`: Required. Matches only Dotify translators for that
      locale. An automatic fallback mechanism is used: if nothing is
      found for language-COUNTRY-variant, then language-COUNTRY is
      searched, then language.
  - `hyphenator`: A value `none` will disable hyphenation. `auto` is
      the default and will match any Dotify translator, whether it
      supports hyphenation out-of-the-box, with the help of an
      external hyphenator, or not at all. A value not equal to `none`
      or `auto` will match every Dotify translator that uses an
      external hyphenator that matches this feature.
  
  No other features are allowed.
  
- [`(hyphenator:dotify)`](java/org/daisy/pipeline/braille/dotify/impl/DotifyHyphenatorImpl.java)
  
  Recognized features:
  
  - `hyphenator`: Will only match if the value is `dotify`.
  - `locale`: Required. Matches only Dotify translators for that
      locale. An automatic fallback mechanism is used: if nothing is
      found for language-COUNTRY-variant, then language-COUNTRY is
      searched, then language.
  
  No other features are allowed.
  


<link rev="dp2:doc" href="./"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/apidoc"/>
