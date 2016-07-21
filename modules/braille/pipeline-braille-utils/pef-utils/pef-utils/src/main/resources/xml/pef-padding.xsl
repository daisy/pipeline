<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="pef:page">
        <xsl:variable name="rows" as="xs:integer" select="xs:integer(number(ancestor::*[@rows][1]/@rows))"/>
        <xsl:variable name="cols" as="xs:integer" select="xs:integer(number(ancestor::*[@cols][1]/@cols))"/>
        <xsl:copy>
            <xsl:sequence select="@*"/>
            <xsl:for-each select="pef:row">
                <xsl:variable name="rowgap" as="xs:integer" select="xs:integer(number(ancestor-or-self::*[@rowgap][1]/@rowgap))"/>
                <xsl:copy>
                    <xsl:sequence select="@*"/>
                    <xsl:attribute name="rowgap" select="format-number($rowgap,'0')"/>
                    <xsl:sequence select="string-join((string(.), for $x in string-length(string(.)) + 1 to $cols return '⠀'), '')"/>
                </xsl:copy>
            </xsl:for-each>
            <xsl:for-each select="1 to (($rows * 4
                                         - sum(for $row in pef:row
                                               return 4 + xs:integer(number($row/ancestor-or-self::*[@rowgap][1]/@rowgap))))
                                         idiv 4)">
                <xsl:element name="row" namespace="http://www.daisy.org/ns/2008/pef">
                    <xsl:sequence select="string-join(for $x in 1 to $cols return '⠀', '')"/>
                </xsl:element>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
