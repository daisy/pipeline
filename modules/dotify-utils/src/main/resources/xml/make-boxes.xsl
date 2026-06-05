<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:s="org.daisy.pipeline.braille.css.xpath.Style"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:re="regex-utils"
                exclude-result-prefixes="#all"
                version="3.0">
    
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>
    
    <xsl:template match="css:_|css:box">
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
                    <xsl:apply-templates select="@xml:lang|@style|@css:*"/>
                    <xsl:if test="@css:display='list-item'">
                        <!--
                            implied by display: list-item
                        -->
                        <xsl:attribute name="css:counter-increment" select="'list-item'"/>
                        <xsl:variable name="list-style-type" as="xs:string?">
                            <xsl:iterate select="reverse(ancestor-or-self::*[not(self::css:_)])">
                                <xsl:variable name="style" as="item()?" select="s:get(css:parse-stylesheet(@style),'list-style-type')"/>
                                <xsl:choose>
                                    <xsl:when test="exists($style)">
                                        <xsl:break select="string($style)"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:next-iteration/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:iterate>
                        </xsl:variable>
                        <xsl:if test="$list-style-type[not(.=('none','inherit','initial'))]">
                            <css:box type="inline" name="css:marker">
                                <css:counter name="list-item" style="{$list-style-type}"/>
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
            <xsl:apply-templates select="@xml:lang|@style|@css:*"/>
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
            <xsl:apply-templates select="@xml:lang|@style|@css:*"/>
            <xsl:apply-templates/>
        </css:box>
    </xsl:template>
    
    <xsl:template match="*[@css:table-cell]" mode="display-table">
        <css:box type="table-cell">
            <xsl:attribute name="name" select="name()"/>
            <xsl:apply-templates select="@xml:lang|@style|@css:*"/>
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
            <xsl:apply-templates select="@xml:lang|@style|@css:*|node()"/>
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
    
    <xsl:template match="@style">
        <xsl:sequence select="css:style-attribute(s:remove(css:parse-stylesheet(.),'list-style-type'))"/>
    </xsl:template>
    
    <xsl:template match="@css:display|
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
            <xsl:message>
                <xsl:text>"display" property on "</xsl:text>
                <xsl:value-of select="name(parent::*)"/>
                <xsl:text>" element within table must be "none".</xsl:text>
            </xsl:message>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="@css:display" mode="display-table">
        <xsl:if test="not(.='inline')">
            <xsl:message>
                <xsl:text>"display" property on "</xsl:text>
                <xsl:value-of select="name(parent::*)"/>
                <xsl:text>" element ignored.</xsl:text>
            </xsl:message>
        </xsl:if>
    </xsl:template>
    
</xsl:stylesheet>
