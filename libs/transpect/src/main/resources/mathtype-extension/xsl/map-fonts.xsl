<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
	 xmlns:xs="http://www.w3.org/2001/XMLSchema"
	 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	 xmlns:mml="http://www.w3.org/1998/Math/MathML"
	 xmlns:tr="http://transpect.io"
	 version="2.0">
  <xsl:import href="identity.xsl"/>
  <xsl:import href="util/symbol-map-base-uri-to-name.xsl"/>

  <xsl:variable as="document-node(element(symbols))*" name="font-maps" select="collection()[symbols]"/>
  <xsl:variable as="xs:decimal*" name="text-code-points" select="192 to 214, 217 to 246, 249 to 509">
    <!-- these chars are likely to be meant as text.
         Math fonts often dont support italic/bold for them -->
  </xsl:variable>

  <xsl:key name="symbol-by-number" match="symbol" use="lower-case(@number)"/>

  <xsl:template match="mml:*[text()]" mode="map-fonts">
    <xsl:variable as="xs:string?" name="symbol">
      <xsl:variable name="font-position" select="lower-case(@font-position)"/>
      <xsl:variable name="fontfamily" select="@fontfamily" as="xs:string?"/>
      <xsl:if test="$fontfamily">
        <xsl:variable name="selected-map" as="document-node(element(symbols))?"
          select="@font-position/($font-maps[tr:symbol-map-base-uri-to-name(.) = $fontfamily])[last()]"/>
        <xsl:sequence
          select="
            if (exists($selected-map)) then
              key('symbol-by-number', $font-position, $selected-map)/@char
            else
              ()"
        />
      </xsl:if>
    </xsl:variable>
    <xsl:variable name="is-text-char" select="string-to-codepoints(($symbol, .)[1]) = $text-code-points" as="xs:boolean"/>
    <xsl:element name="{('mtext'[$is-text-char], name())[1]}" namespace="http://www.w3.org/1998/Math/MathML">
      <xsl:apply-templates select="@* except @font-position" mode="#current"/>
      <xsl:if test="$is-text-char">
        <xsl:attribute name="mathvariant">
          <xsl:choose>
            <xsl:when test="@mathvariant">
              <xsl:sequence select="@mathvariant"/>
            </xsl:when>
            <xsl:when test="self::mml:mi and string-length(text()) eq 1">italic</xsl:when>
            <xsl:otherwise>normal</xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
      </xsl:if>
      <xsl:choose>
        <xsl:when test="$symbol">
          <xsl:value-of select="$symbol"/>
        </xsl:when>
        <xsl:when test="@font-position">
          <xsl:value-of select="."/>
          <xsl:comment>No font-map available, font-position: <xsl:value-of select="@font-position"/></xsl:comment>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:element>
  </xsl:template>

  <xsl:template match="@fontfamily[. = ('Times New Roman', 'Symbol', 'Courier New', 'MT Extra')]" mode="map-fonts"/>
  <xsl:template match="mml:*[@default-font]/@fontfamily | @default-font" mode="map-fonts" priority="2"/>
  
</xsl:stylesheet>
