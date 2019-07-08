<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns="http://www.w3.org/1998/Math/MathML"
    exclude-result-prefixes="xs"
    version="1.0">

     <!-- Limits -->

    <xsl:template match="tmpl[selector='tmLIM']">
        <munderover>
            <xsl:apply-templates select="slot[1] | pile[1]"/>
            <xsl:apply-templates select="slot[2] | pile[2]"/>
            <xsl:apply-templates select="slot[3] | pile[3]"/>
        </munderover>
    </xsl:template>

</xsl:stylesheet>
