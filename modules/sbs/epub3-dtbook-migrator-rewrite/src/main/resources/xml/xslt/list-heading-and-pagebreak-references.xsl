<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns="http://www.w3.org/1999/xhtml" xpath-default-namespace="http://www.w3.org/1999/xhtml" exclude-result-prefixes="#all" xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:c="http://www.w3.org/ns/xproc-step">

    <xsl:output indent="yes"/>

    <xsl:template match="node()">
        <xsl:apply-templates select="node()"/>
    </xsl:template>

    <xsl:template match="/*">
        <c:result>
            <xsl:apply-templates/>
        </c:result>
    </xsl:template>

    <xsl:template match="header/h1[@epub:type='fulltitle']"/>
    <xsl:template match="body[header and not(*[not(self::header)])]"/>

    <xsl:template match="h1 | h2 | h3 | h4 | h5 | h6">
        <xsl:variable name="sectioning-element" select="parent::*"/>
        <c:result>
            <xsl:attribute name="xml:base" select="base-uri(.)"/>
            <xsl:attribute name="data-sectioning-element" select="$sectioning-element/name()"/>
            <xsl:if test="$sectioning-element/@id">
                <xsl:attribute name="data-sectioning-id" select="$sectioning-element/@id"/>
            </xsl:if>
            <xsl:attribute name="data-heading-element" select="name()"/>
            <xsl:if test="@id">
                <xsl:attribute name="data-heading-id" select="@id"/>
            </xsl:if>
            <xsl:attribute name="data-heading-depth" select="substring-after(local-name(),'h')"/>
            <xsl:attribute name="data-sectioning-depth" select="count(ancestor::body | ancestor::section | ancestor::article)"/>
            <xsl:value-of select="normalize-space(string-join(.//text() except .//*[tokenize(@epub:type,'\s+') = 'noteref']//text(),''))"/>
        </c:result>
    </xsl:template>

    <xsl:template match="body | section | article">
        <xsl:if test="not(h1 | h2 | h3 | h4 | h5 | h6)">
            <c:result>
                <xsl:attribute name="xml:base" select="base-uri(.)"/>
                <xsl:attribute name="data-sectioning-element" select="name()"/>
                <xsl:if test="@id">
                    <xsl:attribute name="data-sectioning-id" select="@id"/>
                </xsl:if>
                <xsl:attribute name="data-sectioning-depth" select="count(ancestor-or-self::body | ancestor-or-self::section | ancestor-or-self::article)"/>
            </c:result>
        </xsl:if>
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="*[@epub:type='pagebreak']">
        <c:result>
            <xsl:attribute name="xml:base" select="base-uri(.)"/>
            <xsl:attribute name="data-pagebreak-element" select="name()"/>
            <xsl:if test="@id">
                <xsl:attribute name="data-pagebreak-id" select="@id"/>
            </xsl:if>
            <xsl:value-of select="@title"/>
        </c:result>
    </xsl:template>

</xsl:stylesheet>
