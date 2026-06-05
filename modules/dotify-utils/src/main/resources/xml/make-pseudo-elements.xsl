<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:f="local"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[@css:before or
                           @css:after or
                           @css:duplicate or
                           @css:footnote-call or
                           @css:alternate or
                           @css:*[matches(local-name(),'^alternate-[1-9][0-9]*$')]]">
        <xsl:variable name="this" as="element()" select="."/>
        <xsl:if test="@css:anchor and @css:id">
            <xsl:message terminate="yes">coding error</xsl:message>
        </xsl:if>
        <xsl:variable name="id" select="if (@css:id) then string(@css:id) else if (@css:anchor) then @css:anchor else generate-id(.)"/>
        <xsl:copy>
            <xsl:sequence select="@* except (@css:before|
                                             @css:after|
                                             @css:duplicate|
                                             @css:footnote-call|
                                             @css:alternate|
                                             @css:*[matches(local-name(),'^alternate-[1-9][0-9]*$')])"/>
            <xsl:if test="not(@css:id|@css:anchor)
                          and (@css:duplicate or
                               @css:alternate or
                               @css:*[matches(local-name(),'^alternate-[1-9][0-9]*$')] or
                               (@css:footnote-call and @css:flow='footnotes'))">
                <xsl:attribute name="css:id" select="$id"/>
            </xsl:if>
            <xsl:if test="@css:before">
                <css:before style="{@css:before}" name="{concat(f:name(.),'::before')}"/>
            </xsl:if>
            <xsl:apply-templates/>
            <xsl:if test="@css:after">
                <css:after style="{@css:after}" name="{concat(f:name(.),'::after')}"/>
            </xsl:if>
        </xsl:copy>
        <xsl:if test="@css:footnote-call and @css:flow='footnotes'">
            <css:footnote-call css:anchor="{$id}" style="{@css:footnote-call}" name="{concat(f:name(.),'::footnote-call')}"/>
        </xsl:if>
        <xsl:if test="@css:duplicate">
            <css:duplicate css:anchor="{$id}" style="{@css:duplicate}" name="{concat(f:name(.),'::duplicate')}">
                <xsl:sequence select="@* except (@style|@css:*)"/>
                <xsl:apply-templates/>
            </css:duplicate>
        </xsl:if>
        <xsl:if test="@css:alternate">
            <css:alternate css:anchor="{$id}" style="{@css:alternate}" name="{concat(f:name(.),'::alternate')}"/>
        </xsl:if>
        <xsl:for-each select="@css:*[matches(local-name(),'^alternate-[1-9][0-9]*$')]">
            <xsl:variable name="i" as="xs:integer" select="xs:integer(number(replace(local-name(.),'^alternate-([1-9][0-9]*)$','$1')))"/>
            <xsl:if test="not($i=1 and ../@css:alternate)">
                <css:alternate css:anchor="{$id}" style="{.}" name="{concat(f:name($this),'::alternate(',$i,')')}"/>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:function name="f:name" as="xs:string">
        <xsl:param name="element" as="element()"/>
        <xsl:choose>
            <xsl:when test="$element/@name and (
                              $element/self::css:before or
                              $element/self::css:after or
                              $element/self::css:alternate or
                              $element/self::css:duplicate or
                              $element/self::css:footnote-call )">
                <xsl:sequence select="$element/@name"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="name($element)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>
    
</xsl:stylesheet>
