<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

    <!-- put everything in "http://www.daisy.org/z3986/2005/ncx/" namespace -->
    <xsl:template match="*">
        <xsl:element namespace="http://www.w3.org/2001/SMIL20/" name="{local-name(.)}">
            <xsl:apply-templates select="@*|*|text()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="/smil">
        <xsl:element namespace="http://www.w3.org/2001/SMIL20/" name="{local-name(.)}">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@*|text()|comment()|processing-instruction()">
        <xsl:copy/>
    </xsl:template>

</xsl:stylesheet>
