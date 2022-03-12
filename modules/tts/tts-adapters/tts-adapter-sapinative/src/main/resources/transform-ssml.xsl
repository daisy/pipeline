<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:ssml="http://www.w3.org/2001/10/synthesis"
    exclude-result-prefixes="#all"
    version="2.0">

  <xsl:output omit-xml-declaration="yes"/>

  <xsl:param name="voice" select="''"/>
  <xsl:param name="ending-mark" select="''"/>

  <xsl:template match="*">
    <ssml:speak version="1.0">
      <xsl:apply-templates select="if (local-name()='speak') then node() else ." mode="copy"/>
      <ssml:break time="250ms"/>
      <xsl:if test="$ending-mark != ''">
        <bookmark mark="{$ending-mark}"/>
      </xsl:if>
    </ssml:speak>
  </xsl:template>

  <xsl:template match="@*|node()" mode="copy">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="copy"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@xml:lang" mode="copy">
    <!-- not copied in order to prevent inconsistency with the current voice -->
  </xsl:template>

  <xsl:template match="ssml:token" mode="copy">
    <!-- tokens are not copied because they are not SSML1.0-compliant and not SAPI-compliant-->
    <xsl:apply-templates select="@*|node()" mode="copy"/>
  </xsl:template>

  <xsl:template match="ssml:mark" mode="copy">
     <bookmark mark="@name"/>
  </xsl:template>


</xsl:stylesheet>
