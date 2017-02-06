<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:css="http://www.daisy.org/ns/pipeline/braille-css" exclude-result-prefixes="#all"
    version="2.0">

    <!-- xslt replaces this:
        <p:delete match="css:box[@type='block']
                [matches(string(.), '^[\s&#x2800;]*$') and
                not(descendant::css:white-space or
                descendant::css:string or
                descendant::css:counter or
                descendant::css:text or
                descendant::css:content or
                descendant::css:leader or
                descendant::css:custom-func)]
                //text()">
    -->

    <xsl:template match="@* | node()" mode="#all">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="node()" mode="#current"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="css:box[@type='block']">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:choose>

                <xsl:when test="descendant::*[self::css:white-space | self::css:string | self::css:counter | self::css:text | self::css:content | self::css:leader | self::css:custom-func]">
                    <xsl:apply-templates select="node()" mode="#current"/>
                </xsl:when>

                <xsl:when test="not(matches(string(.), '^[\s&#x2800;]*$'))">
                    <xsl:apply-templates select="node()" mode="#current"/>
                </xsl:when>

                <xsl:otherwise>
                    <xsl:apply-templates select="node()" mode="remove-text-nodes"/>
                </xsl:otherwise>

            </xsl:choose>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="text()" mode="remove-text-nodes"/>

</xsl:stylesheet>
