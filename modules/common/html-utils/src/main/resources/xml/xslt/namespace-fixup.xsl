<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    exclude-result-prefixes="#all" xmlns="http://www.w3.org/1999/xhtml">

    <xsl:template match="processing-instruction()|comment()" mode="#all">
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template match="/*">
        <xsl:apply-templates select="." mode="html"/>
    </xsl:template>

    <xsl:template match="*" mode="html">
        <xsl:element name="{local-name()}" namespace="http://www.w3.org/1999/xhtml">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="node()" mode="html"/>
        </xsl:element>
    </xsl:template>

    <!--===========================-->
    <!-- MathML                    -->
    <!--===========================-->

    <xsl:template match="*:math" mode="html">
        <xsl:apply-templates select="." mode="mathml"/>
    </xsl:template>

    <xsl:template match="*" mode="mathml">
        <xsl:element name="m:{local-name()}" namespace="http://www.w3.org/1998/Math/MathML">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="node()" mode="mathml"/>
        </xsl:element>
    </xsl:template>

    <xsl:template
        match="*:annotation-xml[lower-case(@encoding)=('text/html','application/xhtml+xml')]"
        mode="mathml">
        <xsl:element name="m:{local-name()}" namespace="http://www.w3.org/1998/Math/MathML">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="node()" mode="html"/>
        </xsl:element>
    </xsl:template>

    <!--===========================-->
    <!-- SVG                       -->
    <!--===========================-->

    <xsl:template match="*:svg" mode="html">
        <xsl:apply-templates select="." mode="svg"/>
    </xsl:template>

    <xsl:template match="*" mode="svg">
        <xsl:element name="svg:{local-name()}" namespace="http://www.w3.org/2000/svg">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="node()" mode="svg"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="*:foreignObject|*:desc|*:title" mode="svg">
        <xsl:element name="svg:{local-name()}" namespace="http://www.w3.org/2000/svg">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="node()" mode="html"/>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
