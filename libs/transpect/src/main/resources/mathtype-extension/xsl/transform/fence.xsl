<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
  <!ENTITY unicode-fences "('0x0028', '0x0029', '0x005B', '0x005D', '0x007B', '0x007D', '0x2329', '0x232A',
                            '0x301A', '0x301B', '0xFE37', '0xFE38')">
  <!ENTITY non-unicode-fences "('0xEC07','0xEC08','0xEC09','0xEC0A','0xEC0C','0xEC0D', '0xF8EE', '0xF8F0',
                            '0xF8F9', '0xF8FB')">
]
>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:tr="http://transpect.io"
    xmlns="http://www.w3.org/1998/Math/MathML"
    exclude-result-prefixes="xs tr"
    version="2.0">

  <!-- Fences with one member -->
  <xsl:template match="tmpl[selector = ('tmPAREN', 'tmBRACK', 'tmBRACE', 'tmANGLE', 'tmBAR', 'tmDBAR', 'tmFLOOR', 'tmCEILING', 'tmINTERVAL', 'tmOBRACK')]">
    <mfenced>
      <xsl:attribute name="separators"/>
      <xsl:attribute name="open">
        <xsl:if test="some $text in variation/text() satisfies matches($text, '(_L|_..[LR])')">
          <xsl:apply-templates select="char[1]"/>
        </xsl:if>
      </xsl:attribute>
      <xsl:attribute name="close">
      <xsl:if test="some $text in variation/text() satisfies matches($text, '(_R|_..[LR])')">
        
          <xsl:choose>
            <xsl:when test="some $text in variation/text() satisfies matches($text, '(_L|_..[LR])')">
              <xsl:apply-templates select="char[2]"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates select="char[1]"/>
            </xsl:otherwise>
          </xsl:choose>
        
      </xsl:if>
      </xsl:attribute>
      <xsl:apply-templates select="*[local-name() = ('slot', 'pile')][1]"/>
    </mfenced>
  </xsl:template>
  <!-- Fences with two members -->
  <xsl:template match="tmpl[selector = 'tmDIRAC']">
    <mfenced>
      <xsl:attribute name="separators">
        <xsl:variable name="sep">
          <xsl:choose>
            <xsl:when test="variation = 'tvDI_LEFT'">
              <xsl:apply-templates select="char[2]"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates select="char[1]"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:value-of select="$sep"/>
      </xsl:attribute>
      <xsl:attribute name="open">
        <xsl:choose>
          <xsl:when test="variation = 'tvDI_LEFT'">
            <xsl:variable name="open">
              <xsl:apply-templates select="char[1]"/>
            </xsl:variable>
            <xsl:value-of select="$open"/>
          </xsl:when>
        </xsl:choose>
      </xsl:attribute>
      <xsl:attribute name="close">
        <xsl:if test="variation = 'tvDI_RIGHT'">
          <xsl:choose>
            <xsl:when test="variation = 'tvDI_LEFT'">
              <xsl:apply-templates select="char[3]"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates select="char[2]"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </xsl:attribute>
      <xsl:apply-templates select="*[local-name() = ('slot', 'pile')][1]"/>
      <xsl:apply-templates select="*[local-name() = ('slot', 'pile')][2]"/>
    </mfenced>
  </xsl:template>

  <!-- Fences underover -->
  <xsl:template match="tmpl[selector = ('tmHBRACE', 'tmHBRACK')]">
    <xsl:variable name="name" select="if (variation = 'tvHB_TOP') then 'mover' else 'munder'"/>
    <xsl:element name="{$name}" xmlns="http://www.w3.org/1998/Math/MathML">
      <xsl:element name="{$name}" xmlns="http://www.w3.org/1998/Math/MathML">
        <xsl:apply-templates select="*[local-name() = ('slot', 'pile')][1]"/>
        <xsl:variable name="mo">
          <xsl:apply-templates select="char[1]"/>
        </xsl:variable>
        <xsl:element name="{local-name($mo/*)}" xmlns="http://www.w3.org/1998/Math/MathML">
          <xsl:apply-templates select="$mo/*/@* except @stretchy"/>
          <xsl:attribute name="stretchy" select="'true'"/>
          <xsl:apply-templates select="$mo/*/node()"/>
        </xsl:element>
      </xsl:element>
      <xsl:apply-templates select="*[local-name() = ('slot', 'pile')][2]"/>
    </xsl:element>
  </xsl:template>

  <!-- fence characters -->

  <xsl:template match="char[mt_code_value = &non-unicode-fences; and typeface = '22']" priority="2">
    <mo>
      <xsl:choose>
        <xsl:when test="mt_code_value = ('0xEC07', '0xEC08')">|</xsl:when>
        <xsl:when test="mt_code_value = ('0xEC09', '0xEC0A')">‖</xsl:when>
        <xsl:when test="mt_code_value = '0xEC0C'">&#x23b5;</xsl:when>
        <xsl:when test="mt_code_value = '0xEC0D'">&#x23b4;</xsl:when>
        <xsl:when test="mt_code_value = '0xF8EE'">&#x2308;</xsl:when>
        <xsl:when test="mt_code_value = '0xF8F0'">&#x230a;</xsl:when>
        <xsl:when test="mt_code_value = '0xF8F9'">&#x2309;</xsl:when>
        <xsl:when test="mt_code_value = '0xF8FB'">&#x230b;</xsl:when>
        <xsl:otherwise>
          <xsl:if test="$debug">
            <xsl:message>Fence unknown: <xsl:value-of select="mt_code_value"/></xsl:message>
          </xsl:if>
          <xsl:call-template name="charhex"/>
        </xsl:otherwise>
      </xsl:choose>
    </mo>
  </xsl:template>

  <xsl:template match="char[mt_code_value = &unicode-fences; and typeface = '22']" priority="2">
    <mo>
      <xsl:call-template name="charhex"/>
    </mo>
  </xsl:template>

</xsl:stylesheet>
