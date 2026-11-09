<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns="http://www.w3.org/1999/xhtml"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all">

    <xsl:mode on-no-match="shallow-copy"/>
    <xsl:variable name="outline" select="collection()[2]"/>
    <xsl:variable name="matter-ids" select="collection()[3]/html/body/section[
        tokenize(normalize-space(@epub:type),'\s+')=('frontmatter','bodymatter','backmatter')]/@id"/>
    <xsl:key name="heading" match="d:section[@heading]" use="@heading"/>

    <xsl:template match="h1|h2|h3|h4|h5|h6">
        <xsl:variable name="section" select="key('heading',@id,$outline)[1]"/>
        <xsl:variable name="matter" select="$section/ancestor-or-self::d:section[@owner=$matter-ids][1]"/>
        <xsl:choose>
            <xsl:when test="$matter">
                <!-- The matter wrapper becomes the chunk body. Keep any heading it owns
                     and retain the original hierarchy even in continuation chunks. -->
                <xsl:variable name="depth" select="count($section/ancestor-or-self::d:section
                                                         intersect $matter/descendant::d:section)
                                                  + count($matter/@heading)"/>
                <xsl:element name="h{min((6,max((1,$depth))))}">
                    <xsl:apply-templates select="@*|node()"/>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise><xsl:next-match/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
