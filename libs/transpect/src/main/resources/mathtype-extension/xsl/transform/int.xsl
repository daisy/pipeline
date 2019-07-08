<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns="http://www.w3.org/1998/Math/MathML"
    exclude-result-prefixes="xs"
    version="2.0">

    <!-- Integral operators -->
    <!-- The order is important because it would be very messy to address all
         conditions for each template, and so we rely on the fact that the
         last matching rule has precedence -->

  <xsl:template match="tmpl[selector = 'tmINTOP']">
    <msubsup>
      <mstyle mathsize="140%" displaystyle="true">
        <xsl:apply-templates select="(slot|pile|char)[4]"/>
      </mstyle>
      <xsl:apply-templates select="(slot|pile|char)[2]"/>
      <xsl:apply-templates select="(slot|pile|char)[3]"/>
    </msubsup>
    <xsl:apply-templates select="(slot|pile|char)[1]"/>
  </xsl:template>

  <!-- Integrals -->

  <xsl:template match="tmpl[selector = 'tmINTEG']">
    <mstyle displaystyle="true">
      <mrow>
        <xsl:element name="{if (variation='tvBO_SUM') then 'munderover' else 'msubsup'}">
          <xsl:variable name="operator">
            <xsl:apply-templates select="(slot|pile|char)[4]"/>
          </xsl:variable>
          <mo>
            <xsl:if test="variation = 'tvINT_EXPAND'">
              <xsl:attribute name="stretchy" select="'true'"/>
            </xsl:if>
            <xsl:copy-of select="$operator/*/@* except ($operator/*/(@stretchy | @font-position))"/>
            <xsl:variable name="char-code" as="xs:integer">
              <xsl:choose>
                <xsl:when test="(slot|pile|char)[4]/mt_code_value = '0xEE00'">
                  <xsl:value-of select="8755"/>
                </xsl:when>
                <xsl:when test="(slot|pile|char)[4]/mt_code_value = '0xEE01'">
                  <xsl:value-of select="8754"/>
                </xsl:when>
                <xsl:when test="variation = 'tvINT_LOOP'">
                  <xsl:value-of select="8750"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="string-to-codepoints($operator/*/text())"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <!-- tvINT3 is ambiguos in MTEF, so it produces all 3 variations of tvINT in xml -->
            <xsl:variable name="repetition_count" select="if (variation[matches(., 'tvINT_\d')]) then
                                                    if (variation = 'tvINT_3')
                                                    then 3
                                                    else number(
                                                      substring-after(variation/text()[matches(., 'tvINT_\d')], 'tvINT_')
                                                      )
                                                    else 1"/>
            <xsl:value-of select="codepoints-to-string(xs:integer($char-code + $repetition_count -1))"/>
          </mo>
          <xsl:apply-templates select="(slot|pile|char)[2]"/>
          <xsl:apply-templates select="(slot|pile|char)[3]"/>
        </xsl:element>
        <xsl:apply-templates select="(slot|pile|char)[1]"/>
      </mrow>
    </mstyle>
  </xsl:template>

</xsl:stylesheet>
