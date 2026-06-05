<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:ssml="http://www.w3.org/2001/10/synthesis"
		exclude-result-prefixes="#all"
		version="2.0">


  <!-- It is common to use heterodox symbols or punctuation marks to
       emphasize titles, enumerate items or format dialogues. We don't
       want them to be pronounced aloud. Since it would be tedious to
       build a list of such symbols, this script discards all the
       characters between words and keeps only those which may change
       the prosody. -->

  <xsl:template match="@*|node()" priority="1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>


  <!-- Note: the NKFC normalization is available with Saxon but it
       might not be the case with every implementation. -->

  <xsl:template match="text()[not(ancestor::ssml:token[1])]" priority="2">
    <xsl:value-of select="replace(
			      replace(normalize-unicode(., 'NFKC'), '[\p{Z}]+', ' '),
    			      '[^ \p{Pi}\p{Pf}\p{N},!?:;.()¿؟¡%&quot;''&amp;™®©&#x055c;&#x07f9;&#x109f;&#x1944;&#x1363;&#xa1fe;&#x1808;&#x1802;&#x07f8;&#xa60d;&#x055d;&#xa6f5;&#x060c;&#x3001;&#x2026;&#x0eaf;&#x1801;&#x1362;&#x055c;&#x0df4;&#x3002;&#x0964;&#x06d4;&#x037e;&#x061b;&#x204f;&#x1363;&#xa6f6;&#x05c3;&#x003a;&#x0950;&#x0965;&#x0970;&#x061e;&#x061f;&#x06d4;&#x064f;]',
			      ' '
                          )"/>
  </xsl:template>

<!--
UTF8 punctuation marks to be kept:

x055c Armenian exclamation mark
x07f9 nko exclamation mark
x109f myanmar exclamation mark
x1944 limbu exclamation mark
x1363 ethiopic comma
xa1fe lisu comma
x1808 mongolian manchu comma
x1802 mongolian comma
x07f8 nko comma
xa60d vai comma
x055d armenian comma
xa6f5 bamum comma
x060c arabic comma
x3001 ideographic comma
x2026 ellipsis
x0eaf laotian ellipsis
x1801 thai elipsis
x1362 ethiopic full stop
x055c armenian EXCLAMATION MARK
x0df4 sinhalan full stop
x3002 ideographic full stop
x0964 devanagardi danda
x06d4 urdu full stop
x037e greek question mark
x061b arabic semicolon
x204f reversed semicolon
x1363 ethiopic semicolon
xa6f6 bamum semicolon
x05c3 hebrew puctuation
x003a hebrew colon
x0950 devanagari om
x0965 devanagari double danda
x0970 devanagari abbreviation sign
x061e Arabic Triple Dot Punctuation Mark
x061f Arabic Question Mark
x06d4 Arabic Full Stop
x064f ARABIC DAMMA
-->

</xsl:stylesheet>
