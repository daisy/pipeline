<?xml version="1.0" encoding="utf-8" standalone="yes"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:x="http://www.daisy.org/ns/xprocspec"
                xmlns:obfl="http://www.daisy.org/ns/2011/obfl"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:output method="xml" encoding="UTF-8" name="xml" indent="no" omit-xml-declaration="no"/>
    <xsl:output method="text" encoding="UTF-8" name="text"/>
    
    <xsl:template match="/">
        <xsl:for-each select="//x:scenario[not(child::x:scenario)]">
            <xsl:result-document href="{replace(
                                         resolve-uri(concat(@label,'-input.obfl'),base-uri(.)),
                                         '^http://code\.google\.com/p/dotify/','')}" format="xml">
                <xsl:text>&#x0a;</xsl:text>
                <xsl:apply-templates select="x:call/x:input[@port='source']/x:document[@type='inline']/obfl:obfl|
                                             x:expect/x:document[@type='inline']/obfl:obfl">
                    <xsl:with-param name="title" select="@label" tunnel="yes"/>
                    <xsl:with-param name="description" tunnel="yes"
                                    select="if (x:documentation) then normalize-space(x:documentation) else ()"/>
                    <xsl:with-param name="level" select="0" tunnel="yes"/>
                </xsl:apply-templates>
            </xsl:result-document>
            <xsl:result-document href="{replace(
                                         resolve-uri(concat(@label,'-expected.pef'),base-uri(.)),
                                         '^http://code\.google\.com/p/dotify/','')}" format="xml">
                <xsl:text>&#x0a;</xsl:text>
                <xsl:apply-templates select="x:expect/x:document[@type='inline']/pef:pef">
                    <xsl:with-param name="title" select="@label" tunnel="yes"/>
                    <xsl:with-param name="description" tunnel="yes"
                                    select="if (x:documentation) then normalize-space(x:documentation) else ()"/>
                    <xsl:with-param name="level" select="0" tunnel="yes"/>
                </xsl:apply-templates>
            </xsl:result-document>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template match="@*|node()">
        <xsl:copy copy-namespaces="no">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="obfl:obfl[not(child::obfl:meta)]">
        <xsl:param name="level" as="xs:integer" tunnel="yes"/>
        <xsl:copy copy-namespaces="no">
            <xsl:apply-templates select="@*"/>
            <xsl:text>&#x0a;</xsl:text>
            <xsl:value-of select="string-join(for $x in 1 to $level return '   ', '')"/>
            <meta xmlns="http://www.daisy.org/ns/2011/obfl">
                <xsl:call-template name="title-and-description">
                    <xsl:with-param name="level" select="$level + 1" tunnel="yes"/>
                </xsl:call-template>
                <xsl:text>&#x0a;</xsl:text>
                <xsl:value-of select="string-join(for $x in 1 to $level return '   ', '')"/>
            </meta>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="pef:meta">
        <xsl:copy copy-namespaces="no">
            <xsl:apply-templates select="@*"/>
            <xsl:call-template name="title-and-description"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template name="title-and-description">
        <xsl:param name="level" as="xs:integer" tunnel="yes"/>
        <xsl:param name="title" as="xs:string" tunnel="yes"/>
        <xsl:param name="description" as="xs:string?" tunnel="yes"/>
        <xsl:text>&#x0a;</xsl:text>
        <xsl:value-of select="string-join(for $x in 1 to $level return '   ', '')"/>
        <dc:title>
            <xsl:value-of select="$title"/>
        </dc:title>
        <xsl:text>&#x0a;</xsl:text>
        <xsl:value-of select="string-join(for $x in 1 to $level return '   ', '')"/>
        <dc:description>
            <xsl:value-of select="$description"/>
        </dc:description>
    </xsl:template>
    
    <!--
        indentation
    -->
    <xsl:template match="*" priority="1">
        <xsl:param name="level" as="xs:integer" tunnel="yes"/>
        <xsl:next-match>
            <xsl:with-param name="level" tunnel="yes" select="$level + 1"/>
        </xsl:next-match>
    </xsl:template>
    
    <xsl:template match="text()[matches(.,'^[ \t]*\n\s*$')]">
        <xsl:param name="level" as="xs:integer" tunnel="yes"/>
        <xsl:text>&#x0a;</xsl:text>
        <xsl:choose>
            <xsl:when test="following-sibling::*">
                <xsl:value-of select="string-join(for $x in 1 to $level return '   ', '')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="string-join(for $x in 1 to ($level - 1) return '   ', '')"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!--
        copied from src/main/resources/xml/obfl-normalize-space.xsl
    -->
    <xsl:template match="obfl:block/text()[not(preceding-sibling::node()[not(self::obfl:marker or self::text()[normalize-space(.)=''])])]"
                  priority="1">
        <xsl:sequence select="replace(., '^\s+', '')"/>
    </xsl:template>
    
</xsl:stylesheet>
