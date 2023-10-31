<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:ssml="http://www.w3.org/2001/10/synthesis"
    xmlns:tts="http://www.daisy.org/ns/pipeline/tts"
    exclude-result-prefixes="#all"
    version="2.0">

  <!--======================================================================= -->
  <!-- Convert the CSS properties into SSML elements and discard any node -->
  <!-- which is not SSML. -->
  <!--======================================================================= -->

  <xsl:function name="tts:normlist">
    <xsl:param name="li"/>
    <xsl:value-of select="replace(translate($li, ' ',''), '[^-_0-9a-zA-Z]+', '|')"/>
  </xsl:function>

  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="ssml:s">
    <xsl:copy>
      <xsl:copy-of select="@xml:lang|@id"/>
      <xsl:if test="@tts:voice-family">
	<!-- voice-family has the format: attr1|attr2|attr3 where attr
	     is either age, gender, voice's vendor or voice's name. If
	     a voice-name is given, it should come after the mandatory
	     voice-vendor.-->
	<!-- Should we use the wildcard '*' to mean 'any vendor'? -->
	<xsl:variable name="voice-selectors">
	  <tts:selectors>
	    <xsl:analyze-string select="tts:normlist(@tts:voice-family)" regex="(\||^)([0-9]+)(\||$)">
	      <xsl:matching-substring>
		<tts:sel age="{regex-group(2)}"/>
	      </xsl:matching-substring>
	      <xsl:non-matching-substring>
		<xsl:analyze-string select="." regex="(\||^)((male)|(female)|(neutral))(\||$)">
		  <xsl:matching-substring>
		    <tts:sel gender="{regex-group(2)}"/>
		  </xsl:matching-substring>
		  <xsl:non-matching-substring>
		    <tts:sel other="{.}"/>
		  </xsl:non-matching-substring>
		</xsl:analyze-string>
	      </xsl:non-matching-substring>
	    </xsl:analyze-string>
	  </tts:selectors>
	</xsl:variable>
	<xsl:if test="$voice-selectors//@age">
	  <xsl:attribute name="voice-age">
	    <xsl:value-of select="$voice-selectors//@age"/>
	  </xsl:attribute>
	</xsl:if>
	<xsl:if test="$voice-selectors//@gender">
	  <xsl:attribute name="voice-gender">
	    <xsl:value-of select="$voice-selectors//@gender"/>
	  </xsl:attribute>
	</xsl:if>
	<xsl:for-each select="tokenize($voice-selectors//@other, '\|+')">
	  <xsl:attribute name="{concat('voice-selector', position())}">
	    <xsl:value-of select="."/>
	  </xsl:attribute>
	</xsl:for-each>
      </xsl:if>
      <xsl:apply-templates select="." mode="css1"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*" mode="css-child">
    <xsl:apply-templates select="." mode="css1"/>
  </xsl:template>

  <xsl:template match="text()" mode="css-child">
    <xsl:value-of select="."/>
  </xsl:template>

  <xsl:template match="ssml:*" mode="css-child">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates mode="#current"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*" mode="css1">
    <xsl:apply-templates select="." mode="css2"/>
  </xsl:template>
  <xsl:template match="*[@tts:speak = 'none']" mode="css1">
  </xsl:template>

  <xsl:template match="*" mode="css2">
    <xsl:apply-templates select="." mode="css3"/>
  </xsl:template>
  <xsl:template match="*[@tts:volume]" mode="css2">
    <ssml:prosody volume="{@tts:volume}">
      <xsl:apply-templates select="." mode="css3"/>
    </ssml:prosody>
  </xsl:template>

  <xsl:template match="*" mode="css3">
    <xsl:apply-templates select="." mode="css4"/>
  </xsl:template>
  <xsl:template match="*[@tts:pitch]" mode="css3">
    <ssml:prosody pitch="{@tts:pitch}">
      <xsl:apply-templates select="." mode="css4"/>
    </ssml:prosody>
  </xsl:template>

  <xsl:template match="*" mode="css4">
    <xsl:apply-templates select="." mode="css5"/>
  </xsl:template>
  <xsl:template match="*[@tts:speak = 'spell-out']" mode="css4">
    <ssml:say-as interpret-as="characters">
      <xsl:apply-templates select="." mode="css5"/>
    </ssml:say-as>
  </xsl:template>

  <xsl:template match="*" mode="css5">
    <xsl:apply-templates select="." mode="css6"/>
  </xsl:template>
  <xsl:template match="*[@tts:speech-rate]" mode="css5">
    <ssml:prosody rate="{@tts:speech-rate}">
      <xsl:apply-templates select="." mode="css6"/>
    </ssml:prosody>
  </xsl:template>

  <xsl:template match="*" mode="css6">
    <xsl:apply-templates select="." mode="css7"/>
  </xsl:template>
  <xsl:template match="*[@tts:pitch-range]" mode="css6">
    <ssml:prosody range="{@tts:pitch-range}">
      <xsl:apply-templates select="." mode="css7"/>
    </ssml:prosody>
  </xsl:template>

  <xsl:template match="*" mode="css7">
    <xsl:apply-templates select="." mode="css8"/>
  </xsl:template>
  <xsl:template match="*[@tts:speak-numeral = 'digits']" mode="css7">
    <ssml:say-as interpret-as="ordinal">
      <xsl:apply-templates select="." mode="css8"/>
    </ssml:say-as>
  </xsl:template>

  <xsl:template match="*" mode="css8">
    <xsl:apply-templates select="node()" mode="css-child"/>
  </xsl:template>
  <xsl:template match="*[@tts:speak-numeral = 'continuous']" mode="css8">
    <ssml:say-as interpret-as="cardinal">
      <xsl:apply-templates select="node()" mode="css-child"/>
    </ssml:say-as>
  </xsl:template>

</xsl:stylesheet>
