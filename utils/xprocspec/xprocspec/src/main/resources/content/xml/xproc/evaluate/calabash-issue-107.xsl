<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:c="http://www.w3.org/ns/xproc-step">

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/c:errors">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:for-each select="c:error[contains(@code,':')]/@code">
                <xsl:variable name="prefix" select="tokenize(.,':')[1]"/>
                <xsl:if test="not(namespace-uri-for-prefix($prefix,/*))">
                    <xsl:namespace name="{$prefix}" select="'http://example.net/'"/>
                </xsl:if>
            </xsl:for-each>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
