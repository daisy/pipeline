<?xml version="1.0" encoding="utf-8"?>

<xsl:stylesheet version="2.0"
    xmlns="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:brl="http://www.daisy.org/z3986/2009/braille/"
    xmlns:my="http://my-functions"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="my brl xs">

    <xsl:param name="contraction" select="2"/>

    <xsl:output method="xml" encoding="utf-8" indent="no"/>

    <xsl:template match="@*|comment()|processing-instruction()" mode="#all">
        <xsl:sequence select="."/>
    </xsl:template>

    <xsl:template match="*|text()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="*[@xml:lang or @brl:grade]">
        <xsl:choose>
            <xsl:when test="my:get-grade(parent::*[1]) = 2 and (my:get-grade(.) &lt; 2)">
                <xsl:apply-templates mode="add-announcements" select=".">
                    <xsl:with-param name="top-element-id" select="generate-id(.)"/>
                    <xsl:with-param name="single-word"
                        select="count(tokenize(string(.), '(\s|/|-)+')[string(.) != '']) &lt; 2"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy>
                    <xsl:apply-templates select="@*|node()"/>
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*|text()" mode="add-announcements">
        <xsl:param name="top-element-id"/>
        <xsl:param name="single-word"/>
        <xsl:choose>
            <xsl:when test="(my:is-inline-element(.) or self::text()) and (normalize-space(string(.))!='')">
                <xsl:if test="not(preceding::text()[ancestor::*[generate-id()=$top-element-id]
                    and normalize-space(string())!=''])">
                    <!-- If it's a single word, insert an announcement for a single word grade change -->
                    <!-- If there are multiple words, insert an announcement for a multiple word grade change -->
                    <dtb:span><xsl:value-of select="if ($single-word) then '&#x2559;' else '&#x255A;'"/></dtb:span>
                </xsl:if>
                <xsl:sequence select="."/>
                <xsl:if test="not($single-word) and not(following::text()[ancestor::*[generate-id()=$top-element-id]
                    and normalize-space(string())!=''])">
                    <!-- There are multiple words. Insert an announcement for the end of grade change -->
                    <dtb:span><xsl:text>&#x255D;</xsl:text></dtb:span>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy>
                    <xsl:apply-templates mode="add-announcements" select="@*|node()">
                        <xsl:with-param name="top-element-id" select="$top-element-id"/>
                        <xsl:with-param name="single-word" select="$single-word"/>
                    </xsl:apply-templates>
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:function name="my:get-system">
        <xsl:param name="element"/>
        <xsl:sequence select="'de'"/>
    </xsl:function>

    <xsl:function name="my:get-grade">
        <xsl:param name="element"/>
        <xsl:variable name="brl-grade" select="my:get-brl-grade($element)"/>
        <xsl:sequence select="if ($brl-grade) then number($brl-grade) else
            (if ($element[lang('de')]) then $contraction else 0)"/>
    </xsl:function>

    <xsl:function name="my:get-brl-grade">
        <xsl:param name="element"/>
        <xsl:sequence select="$element/ancestor-or-self::*[@brl:grade][1]/@brl:grade"/>
    </xsl:function>

    <xsl:function name="my:is-inline-element">
        <xsl:param name="element"/>
        <xsl:sequence select="boolean($element[
            self::dtb:span or 
            self::dtb:abbr or 
            self::brl:num or 
            self::brl:emph or 
            self::dtb:em or 
            self::dtb:strong])"/>
    </xsl:function>

</xsl:stylesheet>
