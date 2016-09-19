<lexicon version="1.0"
      xmlns="http://www.w3.org/2005/01/pronunciation-lexicon"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://www.w3.org/2005/01/pronunciation-lexicon
        http://www.w3.org/TR/2007/CR-pronunciation-lexicon-20071212/pls.xsd"
      alphabet="ipa" xml:lang="en">

  <!-- regular substitutions -->
  <lexeme>
    <grapheme>grapheme1en</grapheme>
    <phoneme>phoneme1en</phoneme>
  </lexeme>
  <lexeme>
    <grapheme>grapheme2en</grapheme>
    <phoneme>phoneme2en</phoneme>
  </lexeme>
  <lexeme>
    <grapheme>grapheme3en</grapheme>
    <alias>alias3en</alias>
  </lexeme>

  <!-- substitutions with look-aheads -->
  <lexeme>
    <grapheme positive-lookahead="[ ]+test">before1</grapheme>
    <phoneme>before1phoneme</phoneme>
  </lexeme>
  <lexeme>
    <grapheme positive-lookahead="[ ]+test">before2</grapheme>
    <alias>before2alias</alias>
  </lexeme>

  <!-- regex without look-ahead -->
  <lexeme regex="true">
    <grapheme>([0-9]+)</grapheme>
    <alias>k$1</alias>
  </lexeme>

  <lexeme regex="true">
    <grapheme>([A-Z]+)</grapheme>
    <phoneme>regphoneme</phoneme>
  </lexeme>

  <!-- regex with look-aheads -->
  <lexeme regex="true">
    <grapheme positive-lookahead="[ ]+test">a</grapheme>
    <alias>b</alias>
  </lexeme>
</lexicon>