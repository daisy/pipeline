<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns="http://www.w3.org/1998/Math/MathML"
    exclude-result-prefixes="xs"
    version="2.0">

    <!--<xsl:template match="*[following-sibling::*[1][self::tmpl and selector=('tmSUP','tmSUB','tmSUBSUP')]]" priority="2">
        <xsl:param name="keep" select="false()"/>
        <xsl:if test="$keep">
            <xsl:next-match/>
        </xsl:if>
    </xsl:template>
-->
            <!--<xsl:apply-templates select="preceding-sibling::*[1]">
                <xsl:with-param name="keep" select="true()"/>
            </xsl:apply-templates>-->

    <xsl:template match="tmpl[selector='tmSUP' and not(variation='tvSU_PRECEDES')]">
        <msup>
            <xsl:apply-templates select="slot[2] | pile[2]"/>
        </msup>
    </xsl:template>

    <xsl:template match="tmpl[selector='tmSUB' and not(variation='tvSU_PRECEDES')]">
        <msub>
            <xsl:apply-templates select="slot[1] | pile[1]"/>
        </msub>
    </xsl:template>

    <xsl:template match="tmpl[selector='tmSUBSUP' and not(variation='tvSU_PRECEDES')]">
        <msubsup>
            <xsl:apply-templates select="slot[1] | pile[1]"/>
            <xsl:apply-templates select="slot[2] | pile[2]"/>
        </msubsup>
    </xsl:template>

    <xsl:template match="tmpl[selector=('tmSUP','tmSUB','tmSUBSUP') and variation='tvSU_PRECEDES']">
        <mmultiscripts>
            <mprescripts/>
            <xsl:apply-templates select="slot[1] | pile[1]"/>
            <xsl:apply-templates select="slot[2] | pile[2]"/>
        </mmultiscripts>
    </xsl:template>

    <xsl:template match="msub|msup" mode="repair-subsup">
      <xsl:element name="{local-name()}" namespace="http://www.w3.org/1998/Math/MathML">
        <xsl:apply-templates select="preceding-sibling::*[1]" mode="#current">
          <xsl:with-param name="keep" select="true()"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="node()" mode="#current"/>
      </xsl:element>
    </xsl:template>

    <xsl:template match="*[following-sibling::*[1]/local-name() = 'msub']|*[following-sibling::*[1]/local-name() = 'msup']" mode="repair-subsup">
        <xsl:param name="keep" select="false()"/>
        <xsl:if test="$keep">
          <xsl:next-match/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="mmultiscripts" mode="repair-subsup">
        <mmultiscripts>
            <xsl:apply-templates select="following-sibling::*[1]" mode="#current">
                <xsl:with-param name="keep" select="true()"/>
            </xsl:apply-templates>
            <xsl:apply-templates select="node()" mode="#current"/>
        </mmultiscripts>
    </xsl:template>

    <xsl:template match="*[preceding-sibling::*[1]/local-name() = 'mmultiscripts']" mode="repair-subsup">
      <xsl:param name="keep" select="false()"/>
      <xsl:if test="$keep">
        <xsl:next-match/>
      </xsl:if>
    </xsl:template>

</xsl:stylesheet>
