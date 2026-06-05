<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:pls="http://www.w3.org/2005/01/pronunciation-lexicon"
    xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp"
    exclude-result-prefixes="#all"
    version="2.0">

  <!-- Reorganize the lexicons according to: -->
  <!-- # the language; -->
  <!-- # whether or not the phoneme is a regex; -->
  <!-- # whether or not it includes a phoneme or an alias; -->
  <!-- # alphabet; -->
  <!-- It will speed up the next steps. -->

  <xsl:template match="/" priority="2">
    <!-- primary result -->
    <xsl:result-document method="xml">
      <tmp:lexicons>
	<xsl:apply-templates select="collection()" mode="copy-subset">
	  <xsl:with-param name="regex" select="'false'"/>
	</xsl:apply-templates>
      </tmp:lexicons>
    </xsl:result-document>
    <xsl:result-document method="xml" href="regex-lexicons.xml">
      <tmp:lexicons>
	<xsl:apply-templates select="collection()" mode="copy-subset">
	  <xsl:with-param name="regex" select="'true'"/>
	</xsl:apply-templates>
      </tmp:lexicons>
    </xsl:result-document>
  </xsl:template>

  <xsl:template match="node()" priority="1">
    <xsl:apply-templates select="node()"/>
  </xsl:template>

  <xsl:template match="node()" mode="copy-subset">
    <xsl:param name="regex"/>
    <xsl:for-each-group select="pls:lexeme[(not(@regex) and $regex = 'false') or $regex = @regex]"
  			group-by="concat(ancestor-or-self::*[@xml:lang][1]/@xml:lang,
  				  ancestor-or-self::*[@alphabet][1]/@alphabet,
  				  exists(pls:alias))">
      <xsl:variable name="alphabet" select="ancestor-or-self::*[@alphabet][1]/@alphabet"/>
      <pls:lexicon xml:lang="{ancestor-or-self::*[@xml:lang][1]/@xml:lang}"
  		   alphabet="{if ($alphabet) then $alphabet else 'ipa'}"
  		   alias="{exists(pls:alias)}">
  	<xsl:apply-templates select="current-group()" mode="copy"/>
      </pls:lexicon>
    </xsl:for-each-group>
  </xsl:template>

  <xsl:template match="@*|node()" mode="copy" priority="1">
    <xsl:copy>
      <xsl:apply-templates mode="copy" select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@positive-lookahead|@negative-lookahead" mode="copy" priority="2">
    <xsl:attribute name="{local-name()}">
      <xsl:value-of select="if (substring(., 1, 1) = '^') then . else concat('^', .)"/>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="@regex|@alphabet|@xml:lang" mode="copy" priority="2">
    <!-- already copied in <lexicon> -->
  </xsl:template>

</xsl:stylesheet>
