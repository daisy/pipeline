<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" version="2.0"
    xmlns:m="http://www.w3.org/1998/Math/MathML"
    xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
    exclude-result-prefixes="#all">

    <xsl:import  href="http://www.daisy.org/pipeline/modules/mediatype-utils/library.xsl"/>
    <xsl:import  href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
    

    <xsl:output indent="yes" method="xml"/>
    
    <xsl:template match="m:*/@*">
        <xsl:copy/>
    </xsl:template>
    <xsl:template match="m:*/@dtbook:*"/>
    
    
    <xsl:template match="m:*">
        <xsl:copy  copy-namespaces="no">
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="m:math">
        <m:math>
            <xsl:apply-templates select="@*"/>
            <xsl:choose>
                <xsl:when test="(@altimg or @alttext) and not(m:semantics)">
                    <m:semantics>
                        <xsl:apply-templates/>
                        <xsl:apply-templates select="@altimg|@alttext" mode="alt"/>
                    </m:semantics>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates/>
                </xsl:otherwise>
            </xsl:choose>
        </m:math>
    </xsl:template>
    <xsl:template match="m:math/m:semantics">
        <m:semantics>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates/>
            <xsl:apply-templates select="../@altimg|../@alttext" mode="alt"/>
        </m:semantics>
    </xsl:template>
    <xsl:template match="m:math/@altimg" mode="alt">
        <m:annotation src="{.}" encoding="{pf:mediatype-from-extension(pf:get-extension(.))}"/>
    </xsl:template>
    <xsl:template match="m:math/@alttext" mode="alt">
        <m:annotation encoding="text/plain"><xsl:value-of select="."/></m:annotation>
    </xsl:template>
</xsl:stylesheet>
