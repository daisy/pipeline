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
        <xsl:variable name="id" select="generate-id()"/>
        <xsl:copy>
            <xsl:sequence select="@* except (@css:_obfl-on-toc-start|
                                             @css:_obfl-on-volume-start|
                                             @css:_obfl-on-volume-end|
                                             @css:_obfl-on-toc-end)"/>
            <xsl:if test="@css:_obfl-on-toc-start">
                <xsl:attribute name="css:_obfl-on-toc-start-ref" select="$id"/>
            </xsl:if>
            <xsl:if test="@css:_obfl-on-volume-start">
                <xsl:attribute name="css:_obfl-on-volume-start-ref" select="$id"/>
            </xsl:if>
            <xsl:if test="@css:_obfl-on-volume-end">
                <xsl:attribute name="css:_obfl-on-volume-end-ref" select="$id"/>
            </xsl:if>
            <xsl:if test="@css:_obfl-on-toc-end">
                <xsl:attribute name="css:_obfl-on-toc-end-ref" select="$id"/>
            </xsl:if>
            <xsl:apply-templates/>
        </xsl:copy>
        <xsl:if test="@css:_obfl-on-toc-start">
            <xsl:result-document href="-obfl-on-toc-start/{$id}">
                <css:_ css:flow="-obfl-on-toc-start/{$id}">
                    <css:_obfl-on-toc-start style="{@css:_obfl-on-toc-start}"/>
                </css:_>
            </xsl:result-document>
        </xsl:if>
        <xsl:if test="@css:_obfl-on-volume-start">
            <xsl:result-document href="-obfl-on-volume-start/{$id}">
                <css:_ css:flow="-obfl-on-volume-start/{$id}">
                    <css:_obfl-on-volume-start style="{@css:_obfl-on-volume-start}"/>
                </css:_>
            </xsl:result-document>
        </xsl:if>
        <xsl:if test="@css:_obfl-on-volume-end">
            <xsl:result-document href="-obfl-on-volume-end/{$id}">
                <css:_ css:flow="-obfl-on-volume-end/{$id}">
                    <css:_obfl-on-volume-end style="{@css:_obfl-on-volume-end}"/>
                </css:_>
            </xsl:result-document>
        </xsl:if>
        <xsl:if test="@css:_obfl-on-toc-end">
            <xsl:result-document href="-obfl-on-toc-end/{$id}">
                <css:_ css:flow="-obfl-on-toc-end/{$id}">
                    <css:_obfl-on-toc-end style="{@css:_obfl-on-toc-end}"/>
                </css:_>
            </xsl:result-document>
        </xsl:if>
    </xsl:template>
    
</xsl:stylesheet>
