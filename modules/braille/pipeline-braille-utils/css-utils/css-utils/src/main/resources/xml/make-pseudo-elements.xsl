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
    
    <xsl:template match="*[@css:before or @css:after or @css:duplicate or @css:alternate or @css:footnote-call]">
        <xsl:variable name="id" select="if (@css:id) then string(@css:id) else generate-id(.)"/>
        <xsl:copy>
            <xsl:sequence select="@* except (@css:before|@css:after|@css:duplicate|@css:alternate|@css:footnote-call)"/>
            <xsl:if test="@css:duplicate or @css:alternate or (@css:footnote-call and @css:flow='footnotes')">
                <xsl:attribute name="css:id" select="$id"/>
            </xsl:if>
            <xsl:if test="@css:before">
                <css:before style="{@css:before}"/>
            </xsl:if>
            <xsl:apply-templates/>
            <xsl:if test="@css:after">
                <css:after style="{@css:after}"/>
            </xsl:if>
        </xsl:copy>
        <xsl:if test="@css:footnote-call and @css:flow='footnotes'">
            <css:footnote-call css:anchor="{$id}" style="{@css:footnote-call}"/>
        </xsl:if>
        <xsl:if test="@css:duplicate">
            <css:duplicate css:anchor="{$id}" style="{@css:duplicate}">
                <xsl:sequence select="@* except (@style|@css:*)"/>
                <xsl:apply-templates/>
            </css:duplicate>
        </xsl:if>
        <xsl:if test="@css:alternate">
            <css:alternate css:anchor="{$id}" style="{@css:alternate}"/>
        </xsl:if>
    </xsl:template>
    
</xsl:stylesheet>
