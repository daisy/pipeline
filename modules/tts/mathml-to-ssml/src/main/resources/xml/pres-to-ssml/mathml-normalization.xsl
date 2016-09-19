<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:m="http://www.w3.org/1998/Math/MathML"
		xmlns:xs="http://www.w3.org/2001/XMLSchema"
		exclude-result-prefixes="#all"
		version="2.0">

  <xsl:template match="text()" mode="normalize">
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>

  <xsl:template match="m:merror|m:style|m:action|m:aligngroup|m:alignmark|m:style|m:space|m:phantom"
		mode="normalize">
    <!-- erase -->
  </xsl:template>

  <xsl:template match="m:mfenced" mode="normalize">
    <!-- expand fences -->
    <xsl:variable name="open" select="if (@open) then @open else '('"/>
    <xsl:variable name="close" select="if (@close) then @close else ')'"/>
    <xsl:variable name="separators" select="if (@separators) then @separators else ','"/>
    <xsl:variable name="sep-array" select="string-to-codepoints($separators)"/>
    <xsl:variable name="last-sep" select="codepoints-to-string($sep-array[last()])"/>
    <m:mo><xsl:value-of select="$open"/></m:mo>
    <xsl:apply-templates select="*[1]" mode="normalize"/>
    <xsl:for-each select="* except *[1]">
      <xsl:variable name="index" select="position()"/>
      <m:mo>
	<xsl:value-of select="if ($index &gt; count($sep-array))
			      then $last-sep
			      else codepoints-to-string($sep-array[$index])"/>
      </m:mo>
      <xsl:apply-templates select="current()" mode="normalize"/>
    </xsl:for-each>
    <m:mo><xsl:value-of select="$close"/></m:mo>
  </xsl:template>

  <xsl:template match="m:mpadded" mode="normalize">
    <m:mrow>
      <xsl:apply-templates select="*|text()" mode="normalize"/>
    </m:mrow>
  </xsl:template>

  <xsl:template match="*" mode="normalize">
    <xsl:copy>
      <!-- TODO: copy some attributes -->
      <xsl:apply-templates select="*|text()" mode="normalize"/>
    </xsl:copy>
  </xsl:template>


  <!-- mmultiscripts don't play well with our regexp serialization. So
       we group together the subscripts to make it work with regexps. -->
  <xsl:template match="m:mmultiscripts" mode="normalize">
    <xsl:copy>
      <xsl:variable name="base-script" select="*[1]"/>
      <xsl:variable name="prescript" select="m:mprescripts"/>
      <xsl:apply-templates select="$base-script" mode="normalize"/>
      <xsl:choose>
  	<xsl:when test="not($prescript) or not($prescript/following-sibling::*[1])">
  	  <xsl:call-template name="gather">
  	    <xsl:with-param name="nodes" select="* except $base-script except $prescript"/>
  	  </xsl:call-template>
  	</xsl:when>
  	<xsl:otherwise>
  	  <xsl:call-template name="gather">
  	    <xsl:with-param name="nodes" select="$prescript/preceding-sibling::* except $base-script"/>
  	  </xsl:call-template>
  	  <xsl:copy-of select="$prescript"/>
  	  <xsl:call-template name="gather">
  	    <xsl:with-param name="nodes" select="$prescript/following-sibling::*"/>
  	  </xsl:call-template>
  	</xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="gather">
    <xsl:param name="nodes" as="node()*"/>
    <xsl:for-each-group select="$nodes" group-by="position() mod 2">
      <m:row>
  	<xsl:apply-templates select="current-group()" mode="normalize"/>
      </m:row>
    </xsl:for-each-group>
  </xsl:template>

</xsl:stylesheet>
