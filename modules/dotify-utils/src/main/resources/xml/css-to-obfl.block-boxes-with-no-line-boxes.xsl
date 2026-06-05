<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all">

    <!-- this XSLT replaces the following XProc step:

        <p:delete match="css:box[@type='block']
                                [matches(string(.), '^[\s&#x2800;]*$')
                                 and not(descendant::css:white-space or
                                         descendant::css:string or
                                         descendant::css:counter or
                                         descendant::css:text or
                                         descendant::css:content or
                                         descendant::css:leader or
                                         descendant::css:custom-func)]
                                //text()">
    -->

    <xsl:template match="node()">
        <xsl:copy>
            <xsl:sequence select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="css:box[@type='block']">
        <xsl:copy>
            <xsl:sequence select="@*"/>
            <xsl:choose>
                <xsl:when test="descendant::*[self::css:white-space|
                                              self::css:string|
                                              self::css:counter|
                                              self::css:text|
                                              self::css:content|
                                              self::css:leader|
                                              self::css:custom-func]">
                    <xsl:apply-templates/>
                </xsl:when>
                <xsl:when test="not(matches(string(.),'^[\s&#x2800;]*$'))">
                    <xsl:apply-templates/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates mode="remove-text-nodes"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:copy>
    </xsl:template>

    <xsl:template mode="remove-text-nodes" match="text()"/>

    <xsl:template mode="remove-text-nodes" match="*">
        <xsl:copy>
            <xsl:sequence select="@*"/>
            <xsl:apply-templates mode="#current"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
