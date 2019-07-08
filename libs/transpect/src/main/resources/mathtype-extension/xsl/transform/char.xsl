<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
<!ENTITY no-main-tmpl "('tmLIM','tmARROW','tmSUB','tmSUP','tmSUBSUP')">
<!ENTITY one-main-tmpl "('tmBOX','tmSTRIKE','tmJSTATUS','tmARC','tmHAT','tmTILDE','tmVEC','tmHBRACK','tmHBRACE','tmSUMOP','tmINTOP','tmINTER','tmUNION','tmCOPROD','tmPROD','tmSUM','tmINTEG','tmOBAR','tmUBAR','tmROOT','tmINTERVAL','tmOBRACK','tmCEILING','tmFLOOR','tmDBAR','tmBAR','tmBRACK','tmBRACE','tmPAREN','tmANGLE')">
<!ENTITY two-main-tmpl "('tmDIRAC','tmLDIV','tmFRACT')">
]>
<xsl:stylesheet 
  exclude-result-prefixes="xs tr mml" version="2.0"
  xmlns:tr="http://transpect.io"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mml="http://www.w3.org/1998/Math/MathML"
  xmlns="http://www.w3.org/1998/Math/MathML">
  
  <xsl:import href="../util/hexToDec.xsl"/>
  <xsl:import href="../util/decToHex.xsl"/>
  <xsl:import href="../util/symbol-map-base-uri-to-name.xsl"/>
  
  <xsl:variable name="lsize">
    <xsl:variable name="sizes" as="element(mml:size)+">
      <xsl:variable name="unit-map">
        <unit id="0" unit="in"/>
        <unit id="1" unit="cm"/>
        <unit id="2" unit="pt"/>
        <unit id="3" unit="pc"/>
        <unit id="4" unit="%"/>
      </xsl:variable>
      <xsl:variable as="xs:double" name="full-size"
        select="number(string-join(//eqn_prefs/sizes[1]/nibbles[position() lt last()]/text(), ''))"/>
      <size>
        <xsl:choose>
          <xsl:when test="//mtef_version = 3"/>
          <xsl:when test="//eqn_prefs/sizes[1]/unit = 2">
            <xsl:text>12pt</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:if test="$debug">
              <xsl:message terminate="no">
                <xsl:text>Equation preferences '</xsl:text>
                <xsl:text>12</xsl:text>
                <xsl:value-of select="$unit-map/unit[//eqn_prefs/sizes[1]/unit = @id]/@unit"/>
                <xsl:text>' not supported, using default</xsl:text>
              </xsl:message>
            </xsl:if>
          </xsl:otherwise>
        </xsl:choose>
      </size>
      <xsl:for-each select="//eqn_prefs/sizes[position() = (2 to 7)]">
        <xsl:variable name="cur" select="current()"/>
        <size>
          <xsl:choose>
            <xsl:when test="$cur/unit = 2">
              <xsl:value-of select="100 * number(string-join($cur/nibbles[position() lt last()], '')) div $full-size"/>
              <xsl:value-of select="'%'"/>
            </xsl:when>
            <xsl:when test="$cur/unit = 4">
              <xsl:value-of select="string-join($cur/nibbles[position() lt last()], '')"/>
              <xsl:value-of select="'%'"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:if test="$debug">
                <xsl:message terminate="no">
                  <xsl:text>Equation preferences '</xsl:text>
                  <xsl:value-of select="$cur/nibbles[position() lt last()]"/>
                  <xsl:value-of select="$unit-map/unit[$cur/unit = @id]/@unit"/>
                  <xsl:text>' not supported, using default</xsl:text>
                </xsl:message>
              </xsl:if>
            </xsl:otherwise>
          </xsl:choose>
        </size>
      </xsl:for-each>
    </xsl:variable>
    <full size="{if (normalize-space($sizes[1])) then $sizes[1] else '12pt'}"/>
    <sub size="{if (normalize-space($sizes[2])) then $sizes[2] else '58%'}"/>
    <sub2 size="{if (normalize-space($sizes[3])) then $sizes[3] else '42%'}"/>
    <sym size="{if (normalize-space($sizes[4])) then $sizes[4] else '150%'}"/>
    <subsym size="{if (normalize-space($sizes[5])) then $sizes[5] else '100%'}"/>
    <user1 size="{if (normalize-space($sizes[6])) then $sizes[6] else '75%'}"/>
    <user2 size="{if (normalize-space($sizes[7])) then $sizes[7] else '150%'}"/>
  </xsl:variable>
  
<xsl:variable name="mtcode-fontmap" as="element(symbols)"
    select="collection()[position() gt 1][tr:symbol-map-base-uri-to-name(.) = 'MathType MTCode']/symbols"/>
  
  <xsl:variable name="code-range" select="$mtcode-fontmap//symbol/@number" as="attribute(number)*"/>

  <xsl:template match="mi | mo | mn | mtext">
    <xsl:copy-of select="."/>
  </xsl:template>
  
  <xsl:template name="charhex">
    <xsl:if test="lower-case(replace(mt_code_value, '^0x', '')) = (for $i in $code-range return lower-case($i))">
      <xsl:attribute name="font-position" select="replace(mt_code_value, '^0x', '')"/>
      <xsl:attribute name="fontfamily" select="'MathType MTCode'"/>
      <xsl:attribute name="default-font"/>
    </xsl:if>
    <xsl:value-of select="codepoints-to-string(tr:hexToDec(mt_code_value))"/>
  </xsl:template>
  
  <xsl:template match="char/options[floor(. div 2) mod 2 = 1]">
    <xsl:attribute name="start-function"/>
  </xsl:template>
  
  <xsl:template match="char/font_position">
    <xsl:variable name="font-position-hex" select="tr:decToHex(text())"/>
    <xsl:variable name="pad" select="string-join(for $i in (1 to 4 - string-length($font-position-hex)) return '0','')"/>
    <xsl:attribute name="font-position" select="concat($pad, $font-position-hex)"/>
  </xsl:template>
  
  <xsl:template name="mathsize">
    <xsl:variable name="tmpl-present" select="boolean(parent::tmpl or parent::*/parent::tmpl)" as="xs:boolean"/>
    <xsl:variable name="tmpl-subsup" select="parent::*/preceding-sibling::selector = &no-main-tmpl;" as="xs:boolean"/>
    <xsl:variable name="tmpl-one-main" select="((parent::*/preceding-sibling::selector = &one-main-tmpl;) and not(parent::*/preceding-sibling::*[self::slot | self::pile]))" as="xs:boolean"/>
    <xsl:variable name="tmpl-two-main" select="((parent::*/preceding-sibling::selector = &two-main-tmpl;) and not(parent::*/preceding-sibling::*[self::slot | self::pile][2]))" as="xs:boolean"/>
    <xsl:variable name="sizename" select="preceding::*[local-name() = ('full', 'sub', 'sub2', 'sym', 'subsym', 'size')][1]/local-name()"/>
    <xsl:variable name="size" as="xs:string">
      <!-- TODO: MTEF5 user-defined sizes (equation_options) -->
      <xsl:choose>
        <xsl:when test="$sizename = 'sub'">
          <xsl:value-of select="$lsize/*[2]/@size"/>
        </xsl:when>
        <xsl:when test="$sizename = 'sub2'">
          <xsl:value-of select="$lsize/*[3]/@size"/>
        </xsl:when>
        <xsl:when test="$sizename = 'sym'">
          <xsl:value-of select="$lsize/*[4]/@size"/>
        </xsl:when>
        <xsl:when test="$sizename = 'size'">
          <xsl:variable name="size" select="preceding::size[1]"/>
          <xsl:choose>
            <xsl:when test="$size/point_size">
              <xsl:variable name="size" select="-1 * (number($size/point_size) div 32)"/>
              <xsl:variable name="fullsize" select="number(replace($lsize/*[1]/@size, 'pt', ''))"/>
              <xsl:value-of select="concat(floor($size * 100 div $fullsize), '%')"/>
            </xsl:when>
            <xsl:when test="$size/dsize">
              <xsl:variable name="lsize-selector" select="$size/lsize/text() + 1"/>
              <xsl:variable name="full-size" select="number(replace($lsize/*[1]/@size, 'pt', ''))"/>
              <xsl:variable name="rel-lsize" select="if (not($size/lsize = 0)) then number(replace($lsize/*[position() = $size/lsize]/@size, '%', '')) else 100"/>
              <xsl:variable name="abs-lsize" select="($full-size * $rel-lsize) div 100"/>
              <xsl:variable name="abs-size" select="$abs-lsize + $size/dsize"/>
              <xsl:variable name="rel-size" select="floor(($abs-size * 100) div $full-size)"/>
              <xsl:value-of select="concat($rel-size, '%')"/>
            </xsl:when>
            <xsl:otherwise>
              <!-- TODO: lsize - dsize -->
              <xsl:text>100%</xsl:text>
              <xsl:if test="$debug">
                <xsl:message>
                  <xsl:text>default size match: </xsl:text>
                  <xsl:value-of select="$size/dsize"/>
                </xsl:message>
              </xsl:if>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>100%</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="boolean($size) and (not($tmpl-present) or (not($tmpl-subsup) and ($tmpl-one-main or $tmpl-two-main)))">
      <xsl:attribute name="mathsize" select="$size"/>
    </xsl:if>
  </xsl:template>
  
  <!-- Default char translation for mathmode -->
  <xsl:template match="char[not(variation) or variation != 'textmode']" priority="-0.1">
    <mi>
      <xsl:apply-templates select="options"/>
      <xsl:apply-templates select="font_position"/>
      <xsl:call-template name="mathsize"/>
      <xsl:call-template name="charhex"/>
      <xsl:if test="$debug">
        <xsl:message terminate="no">
          <xsl:text>default character match: </xsl:text>
          <xsl:value-of select="mt_code_value/text()"/>
        </xsl:message>
      </xsl:if>
    </mi>
  </xsl:template>
  
  <!-- Default char translation for textmode -->
  <xsl:template match="char[variation = 'textmode']" priority="-0.1">
    <mtext>
      <xsl:apply-templates select="options"/>
      <xsl:apply-templates select="font_position"/>
      <xsl:call-template name="mathsize"/>
      <xsl:call-template name="charhex"/>
      <xsl:if test="$debug">
        <xsl:message terminate="no">
          <xsl:text>default character match: </xsl:text>
          <xsl:value-of select="mt_code_value/text()"/>
        </xsl:message>
      </xsl:if>
    </mtext>
  </xsl:template>

  <xsl:template match="char[mt_code_value eq '0xEF00' and typeface eq '23']">
    <malignmark edge="left"/>
  </xsl:template>
  
  <xsl:template match="char[typeface = '23' and mt_code_value = '0x0009']">
    <mtext>
      <xsl:apply-templates select="options"/>
      <xsl:apply-templates select="font_position"/>
      <xsl:call-template name="mathsize"/>
      <xsl:value-of select="'&#x2003;'"/>
    </mtext>
  </xsl:template>

  <xsl:template match="char[mt_code_value eq '0xEF00' and typeface eq '23']">
    <malignmark edge="left"/>
  </xsl:template>
  
  <xsl:template match="char[typeface = '24']">
    <mtext>
      <xsl:apply-templates select="options"/>
      <xsl:apply-templates select="font_position"/>
      <xsl:call-template name="mathsize"/>
      <xsl:call-template name="charhex"/>
    </mtext>
  </xsl:template>
  
  <xsl:template match="char[//mtef/mtef_version = '5' and (128 - number(typeface)) lt 1]">
    <xsl:variable name="font_index" select="256 - number(typeface)"/>
    <xsl:variable name="font" select="(//font_style_def)[position() = $font_index]"/>
    <xsl:variable name="font-name" select="(//font_def[font_name])[position() = $font/font_def_index]/font_name"/>
    <mi>
      <xsl:apply-templates select="options"/>
      <xsl:apply-templates select="font_position"/>
      <xsl:call-template name="mathsize"/>
      <xsl:if test="$font/char_style = 0">
        <xsl:attribute name="mathvariant">normal</xsl:attribute>
      </xsl:if>
      <xsl:if test="$font/char_style = 1">
        <xsl:attribute name="mathvariant">bold</xsl:attribute>
      </xsl:if>
      <xsl:if test="$font/char_style = 3">
        <xsl:attribute name="mathvariant">bold-italic</xsl:attribute>
      </xsl:if>
      <xsl:attribute name="fontfamily" select="$font-name"/>
      <xsl:call-template name="charhex"/>
    </mi>
  </xsl:template>
  
  <xsl:template match="char[//mtef/mtef_version = '3' and (128 - number(typeface)) lt 1]">
    <xsl:variable name="font">
      <xsl:variable name="typeface" select="number(typeface/text()) mod 256"/>
      <!-- (128 minus typeface) mod 256 can be negative, so add 256 before to always be positive -->
      <xsl:sequence select="//font[((256 + 128 - typeface) mod 256) = $typeface]/node()"/>
    </xsl:variable>
    <mi>
      <xsl:apply-templates select="options"/>
      <xsl:apply-templates select="font_position"/>
      <xsl:call-template name="mathsize"/>
      <xsl:if test="$font/style = 0">
        <xsl:attribute name="mathvariant">normal</xsl:attribute>
      </xsl:if>
      <xsl:if test="$font/style = 1">
        <xsl:attribute name="mathvariant">bold</xsl:attribute>
      </xsl:if>
      <!-- spec states 1 for italic and/or 2 for bold, but seems 1 = bold, 2 = italic, 3 = bold-italic (see MTEF5 spec) -->
      <xsl:if test="$font/style = 3">
        <xsl:attribute name="mathvariant">bold-italic</xsl:attribute>
      </xsl:if>
      <xsl:attribute name="fontfamily" select="$font/name"/>
      <xsl:call-template name="charhex"/>
    </mi>
  </xsl:template>
  
  <xsl:template match="char[//mtef/mtef_version = '5' and typeface = (1 to 12)]">
    <xsl:variable name="char">
      <xsl:call-template name="charhex"/>
    </xsl:variable>
    <xsl:variable name="element-name">
      <xsl:choose>
        <xsl:when test="variation='textmode'">mtext</xsl:when>
        <xsl:when test="typeface = ('6')">mo</xsl:when>
        <xsl:when test="typeface = ('2', '8')">mn</xsl:when>
        <xsl:when test="typeface = ('1', '11', '12')">mtext</xsl:when>
        <xsl:otherwise>mi</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="typeface" select="number(typeface/text())"/>
    <xsl:variable name="font" select="//styles[position() = $typeface]"/>
    <xsl:variable name="mathvariant">
      <xsl:choose>
        <xsl:when test="$font/font_style = '0'">normal</xsl:when>
        <xsl:when test="$font/font_style = '1'">bold</xsl:when>
        <xsl:when test="$font/font_style = '2'">italic</xsl:when>
        <xsl:when test="$font/font_style = '3'">bold-italic</xsl:when>
      </xsl:choose>
    </xsl:variable>
    <xsl:element name="{$element-name}" namespace="http://www.w3.org/1998/Math/MathML">
      <xsl:attribute name="mathvariant" select="$mathvariant"/>
      <xsl:apply-templates select="options"/>
      <xsl:apply-templates select="font_position"/>
      <xsl:attribute name="fontfamily" select="//font_def[position() = $font/font_def]/font_name"/>
      <xsl:attribute name="default-font"/>
      <xsl:call-template name="mathsize"/>
      <xsl:value-of select="$char"/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="char[//mtef/mtef_version = '3' and typeface = (1 to 12)]">
    <xsl:variable name="char">
      <xsl:call-template name="charhex"/>
    </xsl:variable>
    <xsl:variable name="element-name">
      <xsl:choose>
        <xsl:when test="variation='textmode'">mtext</xsl:when>
        <xsl:when test="typeface = ('6')">mo</xsl:when>
        <xsl:when test="typeface = ('2', '8')">mn</xsl:when>
        <xsl:when test="typeface = ('1', '11', '12')">mtext</xsl:when>
        <xsl:otherwise>mi</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="mathvariant">
      <xsl:choose>
        <xsl:when test="typeface = ('3', '4')">italic</xsl:when>
        <xsl:when test="typeface = ('7')">bold</xsl:when>
        <xsl:otherwise>normal</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:element name="{$element-name}" namespace="http://www.w3.org/1998/Math/MathML">
      <xsl:apply-templates select="options"/>
      <xsl:apply-templates select="font_position"/>
      <xsl:attribute name="fontfamily" select="if (typeface = (4, 5, 6)) then 'Symbol' else ''"/>
      <xsl:attribute name="default-font"/>
      <xsl:attribute name="mathvariant" select="$mathvariant"/>
      <xsl:if test="not(ancestor::tmpl[selector = ('tmSUB', 'tmSUP', 'tmSUBSUP')])">
        <xsl:call-template name="mathsize"/>
      </xsl:if>
      <xsl:call-template name="charhex"/>
    </xsl:element>
  </xsl:template>  

  <!-- BULLET -->
  <xsl:template match="char[mt_code_value = '0xE98F' and typeface = '11']" priority="2">
    <mo>&#x2022;</mo>
  </xsl:template>
  
</xsl:stylesheet>
