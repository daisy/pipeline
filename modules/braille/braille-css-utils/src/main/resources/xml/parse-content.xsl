<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:s="org.daisy.pipeline.braille.css.xpath.Style"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[@css:content]">
        <xsl:choose>
            <xsl:when test="self::css:before or self::css:after"/>
            <xsl:when test="self::css:alternate"/>
            <xsl:when test="self::css:footnote-call"/>
            <xsl:when test="self::css:*[matches(local-name(),'^_.+')]"/>
            <xsl:when test="@css:content='none'"/>
            <xsl:otherwise>
                <xsl:message>'content' property only supported on ::before, ::after, ::alternate and ::footnote-call pseudo-elements</xsl:message>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:copy>
            <xsl:sequence select="@* except @css:content"/>
            <xsl:apply-templates select="css:before"/>
            <xsl:apply-templates mode="content-list" select="for $s in s:get(css:parse-stylesheet(@css:content),'content')
                                                             return s:toXml($s)"/>
            <xsl:apply-templates select="css:after"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template mode="content-list" match="css:string[@value]">
        <xsl:value-of select="string(@value)"/>
    </xsl:template>
    
    <xsl:template mode="content-list"
                  match="css:text[@target]|
                         css:string[@name][not(@target|@target-attribute)]|
                         css:string[@name][@target]|
                         css:counter[not(@target|@target-attribute)]|
                         css:counter[@target]|
                         css:content[@target]|
                         css:leader|
                         css:flow|
                         css:custom-func">
        <xsl:sequence select="."/>
    </xsl:template>
    
    <xsl:template match="css:attr|
                         css:text[@target-attribute]|
                         css:string[@name][@target-attribute]|
                         css:counter[@target-attribute]|
                         css:content[@target-attribute]">
        <xsl:message terminate="yes">Coding error: evaluation of attr() should already have been done</xsl:message>
    </xsl:template>
    
    <xsl:template mode="content-list" match="*">
        <xsl:message terminate="yes">Coding error</xsl:message>
    </xsl:template>
    
</xsl:stylesheet>
