<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:include href="library.xsl"/>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:key name="id" match="*[@css:id]" use="@css:id"/>
    
    <xsl:template match="*[@css:content]">
        <xsl:variable name="context" as="element()?">
            <xsl:choose>
                <xsl:when test="self::css:before or self::css:after">
                    <xsl:sequence select="(ancestor::*[not(self::css:before|self::css:after)])[last()]"/>
                </xsl:when>
                <xsl:when test="self::css:alternate">
                    <xsl:variable name="anchor" select="@css:anchor"/>
                    <xsl:sequence select="key('id',$anchor)"/>
                </xsl:when>
                <xsl:when test="self::css:footnote-call">
                    <xsl:sequence select="."/>
                </xsl:when>
                <xsl:when test="self::css:*[matches(local-name(),'^_.+')]">
                    <xsl:sequence select="."/>
                </xsl:when>
                <xsl:when test="@css:content='none'"/>
                <xsl:otherwise>
                    <xsl:message>'content' property only supported on ::before, ::after, ::alternate and ::footnote-call pseudo-elements</xsl:message>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:copy>
            <xsl:sequence select="@* except @css:content"/>
            <xsl:apply-templates select="css:before"/>
            <xsl:if test="exists($context)">
                <xsl:apply-templates select="css:parse-content-list(@css:content, $context)" mode="eval-content-list">
                    <xsl:with-param name="context" select="$context"/>
                    <xsl:with-param name="parent" select="."/>
                </xsl:apply-templates>
            </xsl:if>
            <xsl:apply-templates select="css:after"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="css:string[@value]" mode="eval-content-list">
        <xsl:variable name="string" as="xs:string">
            <xsl:apply-templates select="." mode="css:eval"/>
        </xsl:variable>
        <xsl:value-of select="$string"/>
    </xsl:template>
    
    <xsl:template match="css:attr" mode="eval-content-list">
        <xsl:param name="context" as="element()"/>
        <xsl:variable name="string" as="xs:string?">
            <xsl:apply-templates select="." mode="css:eval">
                <xsl:with-param name="context" select="$context"/>
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:if test="exists($string)">
            <xsl:value-of select="$string"/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="css:text[@target]|
                         css:string[@name]|
                         css:counter|
                         css:content[@target]|
                         css:leader|
                         css:custom-func"
                  mode="eval-content-list">
        <xsl:sequence select="."/>
    </xsl:template>
    
    <!--
        default scope within before and after pseudo-elements is 'backward'
    -->
    <xsl:template match="css:flow[@from and not(@scope)]" mode="eval-content-list">
        <xsl:param name="context" as="element()"/>
        <xsl:param name="parent" as="element()"/>
        <xsl:choose>
            <xsl:when test="$parent[self::css:before or self::css:after]">
                <xsl:sequence select="."/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message>flow() function not supported in content property of elements except for ::before or ::after pseudo-elements</xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="css:content[not(@target)]" mode="eval-content-list">
        <xsl:message>content() function not supported in content property of pseudo-elements</xsl:message>
    </xsl:template>
    
    <xsl:template match="css:flow[@from and @scope]" mode="eval-content-list">
        <xsl:message>flow() function with argument '<xsl:value-of select="@scope"/>' not supported in content property of pseudo-elements</xsl:message>
    </xsl:template>
    
    <xsl:template match="*" mode="eval-content-list">
        <xsl:message terminate="yes">Coding error</xsl:message>
    </xsl:template>
    
</xsl:stylesheet>
