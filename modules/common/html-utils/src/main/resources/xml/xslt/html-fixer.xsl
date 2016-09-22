<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.w3.org/1999/xhtml"
    xpath-default-namespace="http://www.w3.org/1999/xhtml"
    xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="#all" version="2.0">


    <!--TODO add many more-->
    <!--TODO test-->
    
    <xsl:template match="dl">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:if test="*[1][self::dd]">
                <dt></dt>
            </xsl:if>
            <xsl:apply-templates select="node()"/>
            <xsl:if test="*[last()][self::dt]">
                <dd></dd>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="iframe">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
        </xsl:copy>
    </xsl:template>
        
    <xsl:template match="img/@width[not(matches(.,'^[0-9]+$'))]"/>
    <xsl:template match="img/@length[not(matches(.,'^[0-9]+$'))]"/>
    
        
    
    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
