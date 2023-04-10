<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://www.w3.org/2001/10/synthesis"
                exclude-result-prefixes="#all">

  <xsl:output indent="no" omit-xml-declaration="yes" exclude-result-prefixes="#all"/>

  <xsl:param name="voice" select="''"/>
  <xsl:param name="ending-mark" select="''"/>

  <xsl:template match="*">
    <speak version="1.0">
      <xsl:apply-templates select="if (local-name()='speak') then node() else ." mode="copy"/>
      <break time="250ms"/>
      <xsl:if test="$ending-mark != ''">
        <mark name="{$ending-mark}"/>
      </xsl:if>
    </speak>
  </xsl:template>

  <xsl:template match="*" mode="copy">
    <xsl:element name="{local-name()}">
      <xsl:apply-templates select="@* | node()" mode="copy"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="@*" mode="copy">
    <xsl:attribute name="{local-name()}">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="text()" mode="copy">
    <xsl:copy />
  </xsl:template>

  <xsl:template match="@xml:lang" mode="copy">
    <!-- not copied in order to prevent inconsistency with the current voice -->
  </xsl:template>

  <xsl:template match="*[contains(local-name(),'token')]" mode="copy">
    <!-- tokens are not copied because they are not SSML1.0-compliant and not SAPI-compliant-->
    <xsl:apply-templates select="node()" mode="copy"/>
    <xsl:if test="following-sibling::*">
      <xsl:text> </xsl:text>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
