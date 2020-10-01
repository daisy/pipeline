<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:ssml="http://www.w3.org/2001/10/synthesis">

  <!--
      Wrap the SSML content in a ssml:voice if needed and add a ssml:break. Then serialize the
      SSML. Namespaces are dropped.
  -->

  <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>

  <xsl:param name="voice" select="''"/>
  <xsl:param name="ending-mark">
      <!-- not used -->
  </xsl:param>

  <xsl:template match="*">
    <xsl:variable name="content" as="node()*" select="if (self::ssml:speak) then node() else ."/>
    <xsl:variable name="content" as="node()*">
      <xsl:choose>
        <xsl:when test="$voice!=''">
          <ssml:voice name="{$voice}">
            <xsl:sequence select="$content"/>
          </ssml:voice>
        </xsl:when>
        <xsl:otherwise>
          <xsl:sequence select="$content"/>
        </xsl:otherwise>
      </xsl:choose>
      <ssml:break time="250ms"/>
    </xsl:variable>
    <xsl:apply-templates mode="serialize" select="$content"/>
  </xsl:template>

  <xsl:template mode="serialize" match="ssml:*" priority="1">
    <xsl:value-of select="concat('&lt;',local-name())"/>
    <xsl:apply-templates mode="#current" select="@*"/>
    <xsl:value-of select="'>'"/>
    <xsl:apply-templates mode="#current"/>
    <xsl:value-of select="concat('&lt;/',local-name(),'>')"/>
  </xsl:template>

  <xsl:template mode="serialize" match="ssml:token" priority="2">
    <xsl:apply-templates mode="#current"/>
  </xsl:template>

  <xsl:template mode="serialize" match="ssml:mark" priority="2"/>

  <xsl:template mode="serialize" match="*">
    <xsl:apply-templates mode="#current"/>
  </xsl:template>

  <xsl:template mode="serialize" match="text()">
    <xsl:value-of select="."/>
  </xsl:template>

  <xsl:template mode="serialize" match="@*">
    <xsl:value-of select="concat(' ',local-name(),'=&quot;',replace(replace(.,'&amp;','&amp;amp;'),'&quot;','&amp;quot;'),'&quot;')"/>
  </xsl:template>

  <xsl:template mode="serialize" match="ssml:s/@*"/>

</xsl:stylesheet>
