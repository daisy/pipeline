<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:template match="/pef:volume">
        <xsl:copy>
            <xsl:sequence select="@*|text()"/>
            <xsl:apply-templates select="*"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="pef:section">
        <xsl:copy>
            <xsl:sequence select="(@cols,parent::*/@cols)[1]"/>
            <xsl:sequence select="(@duplex,parent::*/@duplex)[1]"/>
            <xsl:sequence select="(@rowgap,parent::*/@rowgap)[1]"/>
            <xsl:sequence select="(@rows,parent::*/@rows)[1]"/>
            <xsl:sequence select="node()"/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
