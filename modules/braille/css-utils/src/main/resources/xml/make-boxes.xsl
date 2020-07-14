<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:re="regex-utils"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:include href="library.xsl"/>
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xsl"/>
    
    <xsl:template match="css:_">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[@css:display]">
        <xsl:choose>
            <xsl:when test="@css:display='none'">
                <xsl:apply-templates select="." mode="display-none"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="css:box">
                    <xsl:attribute name="type" select="if (@css:display=('block','list-item')) then 'block' else 'inline'"/>
                    <xsl:attribute name="name" select="if (@name and (self::css:before or
                                                                      self::css:after or
                                                                      self::css:alternate or
                                                                      self::css:duplicate or
                                                                      self::css:footnote-call))
                                                       then @name
                                                       else name()"/>
                    <xsl:apply-templates select="@style|@css:*"/>
                    <xsl:if test="@css:display='list-item'">
                        <!--
                            implied by display: list-item
                        -->
                        <xsl:attribute name="css:counter-increment" select="'list-item'"/>
                        <xsl:variable name="list-style-type" as="xs:string"
                                      select="css:specified-properties('list-style-type', true(), true(), true(), .)/@value"/>
                        <xsl:if test="$list-style-type!='none'">
                            <css:box type="inline" name="css:marker">
                                <xsl:choose>
                                    <xsl:when test="matches($list-style-type,re:exact($css:BRAILLE_STRING_RE))">
                                        <xsl:attribute name="css:text-transform" select="'none'"/>
                                        <xsl:value-of select="substring($list-style-type,2,string-length($list-style-type)-2)"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <css:counter name="list-item" style="{$list-style-type}"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                                <xsl:text> </xsl:text>
                            </css:box>
                        </xsl:if>
                    </xsl:if>
                    <xsl:apply-templates/>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="*[@css:table]" priority="0.6">
        <css:box type="block">
            <xsl:attribute name="name" select="name()"/>
            <xsl:apply-templates select="@style|@css:*"/>
            <xsl:apply-templates select="css:before"/>
            <xsl:apply-templates select="*[@css:table-caption]" mode="display-table"/>
            <css:box type="table">
                <xsl:apply-templates select="node() except *[@css:table-caption]" mode="display-table"/>
            </css:box>
            <xsl:apply-templates select="css:after"/>
        </css:box>
    </xsl:template>
    
    <xsl:template match="*[@css:table-caption]" mode="display-table">
        <css:box type="block">
            <xsl:attribute name="name" select="name()"/>
            <xsl:apply-templates select="@style|@css:*"/>
            <xsl:apply-templates/>
        </css:box>
    </xsl:template>
    
    <xsl:template match="*[@css:table-cell]" mode="display-table">
        <css:box type="table-cell">
            <xsl:attribute name="name" select="name()"/>
            <xsl:apply-templates select="@style|@css:*"/>
            <xsl:apply-templates/>
        </css:box>
    </xsl:template>
    
    <xsl:template match="*">
        <xsl:element name="css:box">
            <xsl:attribute name="type" select="'inline'"/>
            <xsl:attribute name="name" select="if (@name and (self::css:before or
                                                              self::css:after or
                                                              self::css:alternate or
                                                              self::css:duplicate or
                                                              self::css:footnote-call))
                                               then @name
                                               else name()"/>
            <xsl:apply-templates select="@style|@css:*|node()"/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="*" mode="display-none display-table">
        <xsl:element name="css:_">
            <xsl:attribute name="name" select="name()"/>
            <xsl:apply-templates select="@*|node()" mode="#current"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@*|
                         text()|
                         css:white-space|
                         css:text|
                         css:content|
                         css:string|
                         css:counter|
                         css:flow[@from]|
                         css:leader|
                         css:custom-func">
        <xsl:sequence select="."/>
    </xsl:template>
    
    <xsl:template match="@css:display|
                         @css:list-style-type|
                         @css:table|
                         @css:table-caption|
                         @css:table-cell"/>
    
    <!--
        FIXME: display-table: warning when style != default, error when unexpected elements
    -->
    <xsl:template match="@*|
                         text()|
                         css:white-space|
                         css:text|
                         css:content|
                         css:string|
                         css:counter|
                         css:flow[@from]|
                         css:leader|
                         css:custom-func"
                  mode="display-none display-table"/>
    
    <xsl:template match="@css:id|
                         @css:counter-reset|
                         @css:counter-set|
                         @css:counter-increment|
                         @css:string-set|
                         @css:*[matches(local-name(),'^_')]"
                  mode="display-none display-table">
        <xsl:sequence select="."/>
    </xsl:template>
    
    <xsl:template match="*[not(descendant::*[@css:table-cell])]/@css:display" mode="display-table">
        <xsl:if test="not(.='none')">
            <xsl:call-template name="pf:warn">
                <xsl:with-param name="msg">"display" property on "{}" element within table must be "none".</xsl:with-param>
                <xsl:with-param name="args" select="name(parent::*)"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="@css:display" mode="display-table">
        <xsl:if test="not(.='inline')">
            <xsl:call-template name="pf:warn">
                <xsl:with-param name="msg">"display" property on "{}" element ignored.</xsl:with-param>
                <xsl:with-param name="args" select="name(parent::*)"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
    
</xsl:stylesheet>
