<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:d="http://www.daisy.org/ns/pipeline/data" xmlns:xs="http://www.w3.org/2001/XMLSchema" version="2.0">

  <xsl:variable name="all-clips">
    <d:audio-clips>
      <xsl:apply-templates select="/*"/>
    </d:audio-clips>
  </xsl:variable>

  <xsl:template match="*[@id and not(descendant::*[@id])]">
    <d:clip idref="{@id}"/>
    <xsl:apply-templates select="*"/>
  </xsl:template>

  <xsl:template match="node()">
    <xsl:apply-templates select="*"/>
  </xsl:template>

  <xsl:template match="/">
    <xsl:variable name="count" select="count($all-clips/*/*)"/>
    <d:audio-clips>
      <xsl:for-each select="$all-clips/*/*">
	<xsl:variable name="duration" select="30.049 div $count"/> <!-- 30 seconds -->
	<xsl:variable name="start" select="(position()-1)*$duration"/>
	<xsl:variable name="end" select="$start + $duration"/>
	<xsl:copy>
	  <xsl:copy-of select="@*"/>
	  <xsl:attribute name="clipBegin"><xsl:value-of select="d:format-time($start)"/></xsl:attribute>
	  <xsl:attribute name="clipEnd"><xsl:value-of select="d:format-time($end)"/></xsl:attribute>
	  <!-- MP3_PATH must be replaced with an actual path -->
	  <xsl:attribute name="src"><xsl:value-of select="'file://%MP3_PATH%'"/></xsl:attribute>
	</xsl:copy>
      </xsl:for-each>
    </d:audio-clips>
    <xsl:apply-templates select="$all-clips" mode="fill-clip"/>
  </xsl:template>

  <xsl:function name="d:format-time">
    <xsl:param name="seconds"/>
    <xsl:variable name="sec" select="substring-before(xs:string($seconds), '.')"/>
    <xsl:variable name="ms" select="substring-after(xs:string($seconds), '.')"/>
    <xsl:text>00:00:</xsl:text>
    <xsl:choose>
      <xsl:when test="string-length($ms) = 0">
	<xsl:text>00</xsl:text>
      </xsl:when>
      <xsl:otherwise>
	<xsl:value-of
	    select="concat(if (string-length($sec) = 1) then concat('0', $sec) else $sec,'.',substring($ms, 1, 3))"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

</xsl:stylesheet>
