<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:d="http://www.daisy.org/ns/pipeline/data"
		xmlns:xs="http://www.w3.org/2001/XMLSchema"
		xmlns:smil="http://www.w3.org/2001/SMIL20/"
		exclude-result-prefixes="#all" version="2.0">



  <xsl:function name="d:time" as="xs:time">
    <xsl:param name="str"/>
    <xsl:value-of select="xs:time(replace($str, '^([0-9]:)', '0$1'))"/>
  </xsl:function>

  <xsl:function name="d:two-digits">
    <xsl:param name="str"/>
    <xsl:value-of select="replace(xs:string($str), '^([0-9])(\.|:|$)', '0$1$2')"/>
  </xsl:function>

  <xsl:template match="/">
    <d:durations>
      <xsl:call-template name="accu"/>
    </d:durations>
  </xsl:template>

  <xsl:template name="accu">
    <xsl:param name="prev-duration"  select="xs:dayTimeDuration('PT0S')"/>
    <xsl:param name="smil-number" select="number('1')"/>

    <xsl:variable name="hours" select="24*days-from-duration($prev-duration)+hours-from-duration($prev-duration)"/>
    <xsl:variable name="mins" select="d:two-digits(minutes-from-duration($prev-duration))"/>
    <xsl:variable name="secs" select="d:two-digits(seconds-from-duration($prev-duration))"/>
    <xsl:variable name="formatted" select="concat($hours, ':', $mins, ':', $secs)"/>

    <xsl:choose>
      <xsl:when test="$smil-number le count(collection()[/smil:smil])">
	<xsl:variable name="root" select="collection()[/smil:smil][$smil-number]"/>
	<d:duration doc="{base-uri($root)}" duration="{$formatted}"/>
	<xsl:variable name="duration">
	  <xsl:apply-templates select="$root" mode="sum-durations">
	    <xsl:with-param name="duration" select="$prev-duration"/>
	  </xsl:apply-templates>
	</xsl:variable>
	<xsl:call-template name="accu">
	  <xsl:with-param name="prev-duration" select="$duration"/>
	  <xsl:with-param name="smil-number" select="$smil-number + 1"/>
	</xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
	<d:total duration="{$formatted}"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="*" mode="sum-durations">
    <xsl:param name="duration" as="xs:dayTimeDuration"/>
    <xsl:variable name="child" select="descendant::*[@clipBegin and @clipEnd][1]"/>
    <xsl:variable name="next" select="if ($child) then $child else following::*[@clipBegin and @clipEnd][1]"/>
    <xsl:choose>
      <xsl:when test="$next">
    	<xsl:apply-templates select="$next" mode="sum-durations">
	  <!-- @clipBegin and @clipEnd are no actual xs:time, but they should be compliant with xs:time format. -->
	  <xsl:with-param name="duration"
			  select="$duration + (d:time($next/@clipEnd) - d:time($next/@clipBegin))"/>
    	</xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
	<xsl:value-of select="$duration"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
