<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:s="http://www.w3.org/2001/10/synthesis"
                xmlns="http://www.w3.org/2001/10/synthesis"
                xpath-default-namespace="http://www.w3.org/2001/10/synthesis"
                exclude-result-prefixes="#all">

  <xsl:output indent="no" omit-xml-declaration="yes" exclude-result-prefixes="#all"/>

  <xsl:param name="speech-rate" as="xs:double" select="1.0"/>
  <xsl:param name="ending-mark" select="''"/>

  <xsl:template match="*">
    <speak version="1.0">
      <xsl:choose>
        <xsl:when test="$speech-rate!=1.0 and descendant::text()[normalize-space(.) and not(ancestor::prosody[@rate])]">
          <prosody>
            <xsl:attribute name="rate" select="format-number($speech-rate,'0%')"/>
            <xsl:apply-templates mode="copy" select="if (local-name()='speak') then node() else .">
              <xsl:with-param name="rate" tunnel="yes" select="$speech-rate"/>
            </xsl:apply-templates>
          </prosody>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates mode="copy" select="if (local-name()='speak') then node() else ."/>
        </xsl:otherwise>
      </xsl:choose>
      <break time="250ms"/>
      <xsl:if test="$ending-mark != ''">
        <mark name="{$ending-mark}"/>
      </xsl:if>
    </speak>
  </xsl:template>

  <xsl:template mode="copy" match="*">
    <xsl:element name="{local-name()}">
      <xsl:apply-templates mode="#current" select="@*|node()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template mode="copy" match="@*">
    <xsl:attribute name="{local-name()}" select="string(.)"/>
  </xsl:template>

  <xsl:template mode="copy" match="text()">
    <xsl:copy />
  </xsl:template>

  <xsl:template mode="copy" match="@xml:lang">
    <!-- not copied in order to prevent inconsistency with the current voice -->
  </xsl:template>

  <xsl:template mode="copy" match="token">
    <!-- tokens are unwrapped because they are not SSML1.0-compliant and not SAPI-compliant-->
    <xsl:apply-templates select="node()" mode="copy"/>
    <xsl:if test="following-sibling::*">
      <xsl:text> </xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="copy" match="prosody[@rate]">
    <xsl:param name="rate" as="xs:double" tunnel="yes" select="1.0"/>
    <xsl:variable name="parent-rate" as="xs:double" select="$rate"/>
    <xsl:variable name="rate" as="xs:double">
      <xsl:variable name="rate" as="xs:string" select="normalize-space(string(@rate))"/>
      <xsl:choose>
        <xsl:when test="matches($rate,'^[0-9]+$')">
          <xsl:sequence select="number($rate) div 200"/>
        </xsl:when>
        <xsl:when test="matches($rate,'^[0-9]+%$')">
          <xsl:sequence select="$speech-rate * (number(substring($rate,1,string-length($rate)-1)) div 100)"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:sequence select="$speech-rate * (
                                       if ($rate='x-slow')  then 0.4
                                  else if ($rate='slow')    then 0.6
                                  else if ($rate='fast')    then 1.5
                                  else if ($rate='x-fast')  then 2.5
                                  else                           1.0 (: medium, default, or illegal value :)
                                )"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="(@* except @rate) or $rate!=$parent-rate">
        <xsl:element name="{local-name(.)}">
          <xsl:if test="$rate!=$parent-rate">
            <xsl:attribute name="rate" select="format-number($rate,'0%')"/>
          </xsl:if>
          <xsl:apply-templates mode="#current" select="@* except @rate"/>
          <xsl:apply-templates mode="#current">
            <xsl:with-param name="rate" tunnel="yes" select="$rate"/>
          </xsl:apply-templates>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="#current"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
