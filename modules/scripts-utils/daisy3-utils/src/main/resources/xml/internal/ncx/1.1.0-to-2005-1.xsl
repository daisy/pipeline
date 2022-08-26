<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

    <!-- put everything in "http://www.daisy.org/z3986/2005/ncx/" namespace -->
    <xsl:template match="*">
        <xsl:element namespace="http://www.daisy.org/z3986/2005/ncx/" name="{local-name(.)}">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
    </xsl:template>

    <!-- update version attribute -->
    <xsl:template match="/ncx/@version">
        <xsl:attribute name="version" select="'2005-1'"/>
    </xsl:template>

    <!-- add playOrder attributes -->
    <xsl:template match="navPoint|navTarget">
        <xsl:element namespace="http://www.daisy.org/z3986/2005/ncx/" name="{local-name(.)}">
            <xsl:attribute name="playOrder" select="1 + count(preceding::navPoint|preceding::navTarget|
                                                              ancestor::navPoint|ancestor::navTarget)"/>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
    </xsl:template>

    <!-- change navList with class "pagenum" to pageList -->
    <xsl:template match="/ncx/navList[@class='pagenum']">
        <xsl:element namespace="http://www.daisy.org/z3986/2005/ncx/" name="pageList">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="/ncx/navList[@class='pagenum']/navTarget">
        <xsl:element namespace="http://www.daisy.org/z3986/2005/ncx/" name="pageTarget">
            <xsl:attribute name="playOrder" select="1 + count(preceding::navPoint|preceding::navTarget|
                                                              ancestor::navPoint|ancestor::navTarget)"/>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@*|text()|comment()|processing-instruction()">
        <xsl:copy/>
    </xsl:template>

</xsl:stylesheet>
