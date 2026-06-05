<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:pls="http://www.w3.org/2005/01/pronunciation-lexicon"
    xmlns:ssml="http://www.w3.org/2001/10/synthesis"
    xmlns:xml="http://www.w3.org/XML/1998/namespace"
    xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp"
    exclude-result-prefixes="#all"
    version="2.0">

  <xsl:import href="get-lookahead.xsl"/>

  <xsl:variable name="lexicons" select="collection()[2]"/>

  <xsl:key name="pronunciation" match="pls:lexeme"
	   use="concat(ancestor::pls:lexicon/@xml:lang, lower-case(string-join(pls:grapheme/text(),'')))" />

  <!-- default behaviour: copy everything -->
  <xsl:template match="@*|node()" priority="1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="ssml:token" priority="2">
    <xsl:copy>
      <xsl:copy-of select="@*"/>

      <!-- get the corresponding lexeme if it exists, depending on the language -->
      <xsl:variable name="lang" select="ancestor-or-self::*[@xml:lang][1]/@xml:lang"/>
      <xsl:variable name="pr"
		    select="key('pronunciation',
			    concat($lang, lower-case(string-join(text(),''))), $lexicons)"/>
      <xsl:choose>
	<xsl:when test="not($pr)">
	  <xsl:apply-templates select="node()"/>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:variable name="ok-look-ahead" as="xs:boolean">
	    <xsl:choose>
	      <xsl:when test="not($pr/pls:grapheme/@positive-lookahead) and not($pr/pls:grapheme/@negative-lookahead)">
		<xsl:value-of select="xs:boolean('true')"/>
	      </xsl:when>
	      <xsl:otherwise>
		<xsl:variable name="look-ahead" select="string-join(ssml:get-lookahead(.),'')"/>
		<xsl:value-of select="( not($pr/pls:grapheme/@positive-lookahead) or
				        matches($look-ahead, $pr/pls:grapheme/@positive-lookahead)
                                      ) and
				      ( not($pr/pls:grapheme/@negative-lookahead) or
                                        not(matches($look-ahead, $pr/pls:grapheme/@negative-lookahead))
                                      )"/>
	      </xsl:otherwise>
	    </xsl:choose>
	  </xsl:variable>
	  <xsl:choose>
	    <xsl:when test="count($pr/pls:phoneme) = 1 and $ok-look-ahead">
	      <ssml:phoneme>
		<xsl:attribute name="ph">
		  <xsl:value-of select="$pr/pls:phoneme/text()"/>
		</xsl:attribute>
		<xsl:attribute name="alphabet">
		  <xsl:value-of select="$pr/ancestor::pls:lexicon/@alphabet"/>
		</xsl:attribute>
		<xsl:apply-templates select="node()"/>
	      </ssml:phoneme>
	    </xsl:when>
	    <xsl:when test="count($pr/pls:alias) = 1 and $ok-look-ahead">
	      <xsl:value-of select="$pr/pls:alias/text()"/>
	    </xsl:when>
	    <xsl:otherwise>
	      <xsl:apply-templates select="node()"/>
	    </xsl:otherwise>
	  </xsl:choose>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
