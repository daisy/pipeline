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
            <xsl:if test="string(@cols)!=string(parent::*/@cols)">
                <xsl:sequence select="@cols"/>
            </xsl:if>
            <xsl:if test="string(@duplex)!=string(parent::*/@duplex)">
                <xsl:sequence select="@duplex"/>
            </xsl:if>
            <xsl:if test="string(@rowgap)!=string(parent::*/@rowgap)">
                <xsl:sequence select="@rowgap"/>
            </xsl:if>
            <xsl:if test="string(@rows)!=string(parent::*/@rows)">
                <xsl:sequence select="@rows"/>
            </xsl:if>
            <xsl:sequence select="node()"/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
