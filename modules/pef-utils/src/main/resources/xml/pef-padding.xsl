<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <!--
        Add empty pages so that every section contains an even number of pages.
        Add empty rows so that the sum of the heights of the rows on a page is equal to the available height of the page.
        Add space characters (U+2800) so that every row is exactly as wide as the page allows.
    -->
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="pef:volume|
                         pef:section[@duplex]"
                  priority="0.9">
        <xsl:next-match>
            <xsl:with-param name="duplex" tunnel="yes" select="xs:boolean(@duplex)"/>
        </xsl:next-match>
    </xsl:template>
    
    <xsl:template match="pef:volume|
                         pef:section[@rows]"
                  priority="0.8">
        <xsl:next-match>
            <xsl:with-param name="rows" tunnel="yes" select="xs:integer(number(@rows))"/>
        </xsl:next-match>
    </xsl:template>
    
    <xsl:template match="pef:volume|
                         pef:section[@cols]"
                  priority="0.7">
        <xsl:next-match>
            <xsl:with-param name="cols" tunnel="yes" select="xs:integer(number(@cols))"/>
        </xsl:next-match>
    </xsl:template>
    
    <xsl:template match="pef:volume|
                         pef:section[@rowgap]|
                         pef:page[@rowgap]|
                         pef:row[@rowgap]"
                  priority="0.6">
        <xsl:next-match>
            <xsl:with-param name="rowgap" tunnel="yes" select="xs:integer(number(@rowgap))"/>
        </xsl:next-match>
    </xsl:template>
    
    <xsl:template match="pef:section">
        <xsl:param name="duplex" tunnel="yes" as="xs:boolean"/>
        <xsl:param name="rows" tunnel="yes" as="xs:integer"/>
        <xsl:param name="cols" tunnel="yes" as="xs:integer"/>
        <xsl:choose>
            <xsl:when test="$duplex and (count(pef:page) mod 2) != 0">
                <xsl:copy>
                    <xsl:apply-templates select="@*|node()"/>
                    <xsl:apply-templates select="$empty-page"/>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:variable name="empty-page" as="element()">
        <page xmlns="http://www.daisy.org/ns/2008/pef"/>
    </xsl:variable>
    
    <xsl:template match="pef:page">
        <xsl:param name="rows" tunnel="yes" as="xs:integer"/>
        <xsl:param name="cols" tunnel="yes" as="xs:integer"/>
        <xsl:param name="rowgap" tunnel="yes" as="xs:integer"/>
        <xsl:copy>
            <xsl:sequence select="@*"/>
            <xsl:apply-templates select="pef:row[following-sibling::pef:row]"/>
            <xsl:variable name="gap" as="xs:integer"
                          select="4 * $rows
                                  - sum(for $row in pef:row[following-sibling::pef:row]
                                        return 4 + ($row/@rowgap/xs:integer(number(.)),$rowgap)[1])"/>
            <xsl:for-each select="(pef:row[not(following-sibling::pef:row)],
                                   for $x in 1 + count(pef:row[not(following-sibling::pef:row)]) to $gap idiv (4 + $rowgap)
                                     return $empty-row)">
                <xsl:apply-templates select=".">
                    <xsl:with-param name="force-rowgap" tunnel="yes" select="if (position()=last())
                                                                             then $rowgap + ($gap mod (4 + $rowgap))
                                                                             else $rowgap"/>
                </xsl:apply-templates>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>
    
    <xsl:variable name="empty-row" as="element()">
        <row xmlns="http://www.daisy.org/ns/2008/pef"/>
    </xsl:variable>
    
    <xsl:template match="pef:row">
        <xsl:param name="rows" tunnel="yes" as="xs:integer"/>
        <xsl:param name="cols" tunnel="yes" as="xs:integer"/>
        <xsl:param name="rowgap" tunnel="yes" as="xs:integer"/>
        <xsl:param name="force-rowgap" tunnel="yes" as="xs:integer?" select="()"/>
        <xsl:copy>
            <xsl:sequence select="@* except @rowgap"/>
            <xsl:attribute name="rowgap" select="format-number(($force-rowgap,$rowgap)[1],'0')"/>
            <xsl:sequence select="string-join((string(.), for $x in string-length(string(.)) + 1 to $cols return '⠀'), '')"/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
