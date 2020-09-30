<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:s="http://www.w3.org/2001/SMIL20/"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="#all">

  <xsl:include href="compute-time-in-smil.xsl"/>

  <xsl:template match="/">
    <d:durations>
      <xsl:call-template name="accu"/>
    </d:durations>
  </xsl:template>

  <xsl:template name="accu">
    <xsl:param name="elapsed-time" as="xs:dayTimeDuration" select="xs:dayTimeDuration('PT0S')"/>
    <xsl:param name="smil-number" as="xs:integer" select="1"/>
    <xsl:if test="$smil-number le count(collection()[/s:smil])">
      <xsl:variable name="smil" select="collection()[/s:smil][$smil-number]"/>
      <d:duration doc="{base-uri($smil)}" duration="{d:format-duration($elapsed-time)}"/>
      <xsl:variable name="time-in-this-smil" as="xs:dayTimeDuration">
        <xsl:call-template name="time-in-smil">
          <xsl:with-param name="smil" select="$smil"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:call-template name="accu">
        <xsl:with-param name="elapsed-time" select="$elapsed-time + $time-in-this-smil"/>
        <xsl:with-param name="smil-number" select="$smil-number + 1"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:function name="d:format-duration" as="xs:string">
    <xsl:param name="dur" as="xs:dayTimeDuration"/>
    <xsl:variable name="hours" select="24 * days-from-duration($dur) + hours-from-duration($dur)"/>
    <xsl:variable name="mins" select="d:two-digits(minutes-from-duration($dur))"/>
    <xsl:variable name="secs" select="d:two-digits(seconds-from-duration($dur))"/>
    <xsl:sequence select="concat($hours, ':', $mins, ':', $secs)"/>
  </xsl:function>

  <xsl:function name="d:two-digits">
    <xsl:param name="str"/>
    <xsl:value-of select="replace(xs:string($str),'^([0-9])(\.|:|$)','0$1$2')"/>
  </xsl:function>

</xsl:stylesheet>
