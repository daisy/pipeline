<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[@css:_obfl-on-toc-start|
                           @css:_obfl-on-volume-start|
                           @css:_obfl-on-volume-end|
                           @css:_obfl-on-toc-end]">
        <xsl:copy>
            <xsl:sequence select="@* except (@css:_obfl-on-toc-start|
                                             @css:_obfl-on-volume-start|
                                             @css:_obfl-on-volume-end|
                                             @css:_obfl-on-toc-end)"/>
            <xsl:if test="@css:_obfl-on-toc-start">
                <css:_obfl-on-toc-start style="{@css:_obfl-on-toc-start}"/>
            </xsl:if>
            <xsl:if test="@css:_obfl-on-volume-start">
                <css:_obfl-on-volume-start style="{@css:_obfl-on-volume-start}"/>
            </xsl:if>
            <xsl:if test="@css:_obfl-on-volume-end">
                <css:_obfl-on-volume-end style="{@css:_obfl-on-volume-end}"/>
            </xsl:if>
            <xsl:if test="@css:_obfl-on-toc-end">
                <css:_obfl-on-toc-end style="{@css:_obfl-on-toc-end}"/>
            </xsl:if>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
