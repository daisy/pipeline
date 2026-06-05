<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:pls="http://www.w3.org/2005/01/pronunciation-lexicon"
    xmlns:ssml="http://www.w3.org/2001/10/synthesis"
    xmlns:xml="http://www.w3.org/XML/1998/namespace"
    xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp"
    exclude-result-prefixes="#all"
    version="2.0">

  <xsl:import href="get-lookahead.xsl"/>

  <xsl:template match="@*|node()" priority="1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="ssml:token" priority="2">
    <xsl:copy>
      <xsl:copy-of select="@*"/>

      <xsl:variable name="current-text" select="string-join(.//text(), '')"/>
      <xsl:variable name="lang" select="ancestor-or-self::*[@xml:lang][1]/@xml:lang"/>

      <xsl:variable name="candidates">
	<tmp:subs>
	  <xsl:if test="not(*[1])">
	     <!-- Alias-based substitutions are only performed when there is only one text node. -->
	    <xsl:for-each select="collection()[/tmp:lexicons]/tmp:lexicons/pls:lexicon[@xml:lang = $lang and @alias='true']//pls:lexeme">
	      <xsl:variable name="rep" select="replace($current-text, current()/pls:grapheme/text(), current()/pls:alias/text())"/>
	      <xsl:if test="$rep != $current-text">
		<tmp:sub rep="{$rep}">
		  <xsl:copy-of select="current()/pls:grapheme/@positive-lookahead|current()/pls:grapheme/@negative-lookahead"/>
		</tmp:sub>
	      </xsl:if>
	    </xsl:for-each>
	  </xsl:if>
	  <xsl:for-each select="collection()[/tmp:lexicons]/tmp:lexicons/pls:lexicon[@xml:lang = $lang and @alias='false']//pls:lexeme">
	    <xsl:if test="matches($current-text, current()/pls:grapheme/text())">
	      <tmp:sub ph="{current()/pls:phoneme/text()}">
	  	<xsl:copy-of select="current()/pls:grapheme/@positive-lookahead|current()/pls:grapheme/@negative-lookahead"/>
	      </tmp:sub>
	    </xsl:if>
	  </xsl:for-each>
	</tmp:subs>
      </xsl:variable>

      <xsl:choose>
	<xsl:when test="not($candidates//tmp:sub[1])">
	  <xsl:copy-of select="node()"/>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:variable name="best-candidate" select="$candidates//tmp:sub[not(@positive-lookahead) and not(@negative-lookahead)][1]"/>
	  <xsl:choose>
	    <xsl:when test="$best-candidate">
	      <!-- Side effect: the graphemes without lookahead constraints have priority over the ones which do. -->
	      <xsl:call-template name="substitute">
		<xsl:with-param name="sub" select="$best-candidate"/>
	      </xsl:call-template>
	    </xsl:when>
	    <xsl:otherwise>
	      <!-- The look-ahead is computed lazily. -->
	      <xsl:variable name="lookahead" select="string-join(ssml:get-lookahead(.),'')"/>
	      <xsl:variable name="matched" select="$candidates//tmp:sub[(not(@positive-lookahead) or matches($lookahead, @positive-lookahead))
						   and (not(@negative-lookahead) or not(matches($lookahead, @negative-lookahead)))][1]"/>
	      <xsl:choose>
		<xsl:when test="$matched">
		  <xsl:call-template name="substitute">
		    <xsl:with-param name="sub" select="$matched"/>
		  </xsl:call-template>
		</xsl:when>
		<xsl:otherwise>
		  <xsl:copy-of select="node()"/>
		</xsl:otherwise>
	      </xsl:choose>
	    </xsl:otherwise>
	  </xsl:choose>
	</xsl:otherwise>
      </xsl:choose>

    </xsl:copy>
  </xsl:template>

  <xsl:template name="substitute">
    <xsl:param name="sub"/>
    <xsl:choose>
      <xsl:when test="$sub/@rep">
	<xsl:value-of select="$sub/@rep"/>
      </xsl:when>
      <xsl:otherwise>
	<ssml:phoneme ph="{$sub/@ph}">
	  <xsl:copy-of select="node()"/>
	</ssml:phoneme>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>

