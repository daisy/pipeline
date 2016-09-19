<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:xs="http://www.w3.org/2001/XMLSchema"
		xmlns:ssml="http://www.w3.org/2001/10/synthesis"
		version="2.0">

  <!--  the SSML needs to be serialized because eSpeak doesn't work well with namespaces -->
  <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>

  <xsl:param name="voice" select="''"/>
  <xsl:param name="ending-mark"/>

  <xsl:template match="*">
    <xsl:variable name="content" select="if (local-name() = 'speak') then node() else ."/>
    <xsl:variable name="to-be-serialized">
      <xsl:choose>
	<xsl:when test="$voice != ''">
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
    <xsl:apply-templates mode="serialize" select="$to-be-serialized"/>
  </xsl:template>


  <!--  TODO: use common functions with Acapela for serialization -->

  <xsl:template match="text()" mode="serialize">
    <xsl:value-of select="."/>
  </xsl:template>

  <xsl:template match="ssml:token" mode="serialize" priority="3">
    <xsl:apply-templates mode="serialize" select="node()"/>
  </xsl:template>

  <xsl:template match="ssml:mark" mode="serialize" priority="3">
    <!--  ignore -->
  </xsl:template>

  <xsl:template match="ssml:*" mode="serialize" priority="2">
    <xsl:value-of select="concat('&lt;', local-name())"/>
    <xsl:if test="local-name() != 's'">
      <xsl:apply-templates select="@*" mode="serialize"/>
    </xsl:if>
    <xsl:value-of select="'>'"/>
    <xsl:apply-templates mode="serialize" select="node()"/>
    <xsl:value-of select="concat('&lt;/', local-name(), '>')"/>
  </xsl:template>

  <xsl:template match="*" mode="serialize" priority="1">
    <xsl:apply-templates mode="serialize" select="node()"/>
  </xsl:template>

  <xsl:template match="@*" mode="serialize">
    <xsl:value-of select="concat(' ', local-name(), '=&quot;', ., '&quot;')"/>
  </xsl:template>

</xsl:stylesheet>