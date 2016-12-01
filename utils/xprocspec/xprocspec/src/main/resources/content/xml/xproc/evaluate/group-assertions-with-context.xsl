<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:x="http://www.daisy.org/ns/xprocspec">

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="x:scenario">
        <xsl:copy>
            <xsl:copy-of select="x:call"/>
            <xsl:for-each-group select="x:context|x:expect" group-starting-with="x:context">
                <x:context-group>
                    <xsl:for-each select="current-group()">
                        <xsl:copy-of select="."/>
                    </xsl:for-each>
                </x:context-group>
            </xsl:for-each-group>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
