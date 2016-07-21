<?xml version="1.1" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:brl="http://www.daisy.org/ns/pipeline/braille"
    xmlns:louis="http://liblouis.org/liblouis"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
    exclude-result-prefixes="#all"
    version="2.0">
    
    <xsl:param name="width"/>
    <xsl:param name="crop-left" select="0"/>
    <xsl:param name="crop-top" select="0"/>
    <xsl:param name="crop-bottom" select="0"/>
    <xsl:param name="border-left" select="'none'"/>
    <xsl:param name="border-right" select="'none'"/>
    <xsl:param name="border-top" select="'none'"/>
    <xsl:param name="border-bottom" select="'none'"/>
    <xsl:param name="keep-empty-trailing-lines" select="'false'"/>
    <xsl:param name="keep-page-structure" select="'false'"/>
    
    <xsl:output method="xml" encoding="UTF-8" indent="yes"/>
    
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xsl" />
    
    <xsl:template match="/*">
        <louis:result>
            <xsl:if test="$border-top!='none'">
                <louis:line>
                    <xsl:sequence select="pxi:repeat-char($border-top,
                        xs:integer(number($width)) + (if ($border-left='none') then 0 else 1) + (if ($border-right='none') then 0 else 1))"/>
                </louis:line>
            </xsl:if>
            <xsl:for-each select="tokenize(pxi:right-trim-formfeeds(string(.)), '&#x0C;')">
                <xsl:call-template name="page"/>
            </xsl:for-each>
            <xsl:if test="$border-bottom!='none'">
                <louis:line>
                    <xsl:sequence select="pxi:repeat-char($border-bottom,
                        xs:integer(number($width)) + (if ($border-left='none') then 0 else 1) + (if ($border-right='none') then 0 else 1))"/>
                </louis:line>
            </xsl:if>
        </louis:result>
    </xsl:template>
    
    <xsl:template name="page">
        <xsl:variable name="page-content">
            <xsl:choose>
                <xsl:when test="$keep-empty-trailing-lines='true'">
                    <xsl:for-each select="tokenize(string(.), '\n')">
                        <xsl:if test="position() &gt; number($crop-top) and
                            position() + number($crop-bottom) &lt; last()">
                            <xsl:call-template name="line"/>
                        </xsl:if>
                    </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:for-each select="tokenize(pxi:right-trim-newlines(string(.)), '\n')">
                        <xsl:if test="position() &gt; number($crop-top)">
                            <xsl:call-template name="line"/>
                        </xsl:if>
                    </xsl:for-each>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$keep-page-structure='true'">
                <louis:page>
                    <xsl:sequence select="$page-content"/>
                </louis:page>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="$page-content"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template name="line">
        <xsl:variable name="line" select="brl:nabcc-to-unicode-braille(pxi:space-to-nbsp(concat(
            pxi:repeat-char(' ', - xs:integer(number($crop-left))),
            substring(., number($crop-left) + 1, number($width)))))"/>
        <louis:line>
            <xsl:if test="$border-left!='none'">
                <xsl:value-of select="$border-left"/>
            </xsl:if>
            <xsl:value-of select="$line"/>
            <xsl:choose>
                <xsl:when test="$border-right!='none'">
                    <xsl:value-of select="pxi:repeat-char('&#xA0;', xs:integer(number($width)) - string-length($line))"/>
                    <xsl:value-of select="$border-right"/>
                </xsl:when>
                <xsl:when test="$border-left='none' and string-length($line)=0">
                    <xsl:text>&#xA0;</xsl:text>
                </xsl:when>
            </xsl:choose>
        </louis:line>
    </xsl:template>
    
    <xsl:function name="pxi:right-trim-newlines" as="xs:string">
        <xsl:param name="string" as="xs:string"/>
        <xsl:sequence select="replace($string, '\n+$','')"/>
    </xsl:function>
    
    <xsl:function name="pxi:right-trim-formfeeds" as="xs:string">
        <xsl:param name="string" as="xs:string"/>
        <xsl:sequence select="replace($string, '&#x0C;+$','')"/>
    </xsl:function>
    
    <xsl:function name="pxi:space-to-nbsp" as="xs:string">
        <xsl:param name="string" as="xs:string"/>
        <xsl:sequence select="translate($string, ' ', '&#xA0;')"/>
    </xsl:function>
    
    <xsl:function name="pxi:repeat-char" as="xs:string">
        <xsl:param name="char" as="xs:string"/>
        <xsl:param name="times" as="xs:integer"/>
        <xsl:sequence select="string-join(for $x in 1 to $times return $char, '')"/>
    </xsl:function>
    
</xsl:stylesheet>
