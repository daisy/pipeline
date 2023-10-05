<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:s="http://www.w3.org/2001/SMIL20/"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                exclude-result-prefixes="#all">

  <xsl:include href="http://www.daisy.org/pipeline/modules/smil-utils/clock-functions.xsl"/>

  <xsl:template name="main">
    <d:durations>
      <xsl:call-template name="accu"/>
    </d:durations>
  </xsl:template>

  <xsl:template name="accu">
    <xsl:param name="elapsed-time" as="xs:double" select="0"/>
    <xsl:param name="smil-number" as="xs:integer" select="1"/>
    <xsl:if test="$smil-number le count(collection()[/s:smil])">
      <xsl:variable name="smil" select="collection()[/s:smil][$smil-number]"/>
      <d:duration doc="{base-uri($smil)}" duration="{pf:smil-seconds-to-full-clock-value($elapsed-time)}"/>
      <xsl:variable name="time-in-this-smil" as="xs:double" select="pf:smil-total-seconds($smil/*)"/>
      <xsl:call-template name="accu">
        <xsl:with-param name="elapsed-time" select="$elapsed-time + $time-in-this-smil"/>
        <xsl:with-param name="smil-number" select="$smil-number + 1"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
