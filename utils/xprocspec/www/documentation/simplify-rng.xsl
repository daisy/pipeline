<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:x="http://www.daisy.org/ns/pipeline/xproc/test" xmlns:rng="http://relaxng.org/ns/structure/1.0" xmlns="http://www.w3.org/1999/xhtml"
    xmlns:a="http://relaxng.org/ns/compatibility/annotations/1.0" version="2.0">

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="rng:start"/>

    <xsl:template match="rng:ref" name="ref">
        <xsl:variable name="name" select="@name"/>
        <xsl:apply-templates select="//rng:define[@name=$name]"/>
    </xsl:template>

    <xsl:template match="rng:define">
        <xsl:apply-templates select="node()"/>
    </xsl:template>

    <xsl:template match="rng:element">
        <xsl:param name="ancestors" tunnel="yes" as="node()*"/>
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:choose>
                <xsl:when test="$ancestors/@name=@name or not(@name)">
                    <xsl:apply-templates select="a:documentation"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="a:documentation"/>
                    <xsl:apply-templates select="node()">
                        <xsl:with-param name="ancestors" select="($ancestors,.)" tunnel="yes" as="node()*"/>
                    </xsl:apply-templates>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="a:documentation">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:attribute name="xml:id" select="generate-id()"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
