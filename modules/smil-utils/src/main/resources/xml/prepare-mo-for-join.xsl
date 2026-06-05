<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet exclude-result-prefixes="#all" version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mo="http://www.w3.org/ns/SMIL"
    xmlns:epub="http://www.idpf.org/2007/ops">
    <xsl:param name="id-prefix" required="yes"/>
    <xsl:variable name="xml-base" select="replace(base-uri(/*),'^(.+/)[^/]*$','$1')"/>
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="mo:body">
        <xsl:choose>
            <xsl:when test="@epub:textref">
                <seq xmlns="http://www.w3.org/ns/SMIL">
                    <xsl:apply-templates select="@*"/>
                    <xsl:attribute name="xml:base" select="$xml-base"/>
                    <xsl:if test="@id">
                        <xsl:attribute name="id" select="concat($id-prefix,@id)"/>
                    </xsl:if>
                    <xsl:if test="@epub:textref">
                        <xsl:attribute name="epub:textref"
                            select="resolve-uri(@epub:textref,$xml-base)"/>
                    </xsl:if>
                    <xsl:apply-templates select="node()"/>
                </seq>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="node()"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="*[ancestor::mo:body]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:if test="@id">
                <xsl:attribute name="id" select="concat($id-prefix,@id)"/>
            </xsl:if>
            <xsl:if test="@src">
                <xsl:attribute name="src" select="resolve-uri(@src,$xml-base)"/>
            </xsl:if>
            <xsl:if test="@epub:textref">
                <xsl:attribute name="epub:textref" select="resolve-uri(@epub:textref,$xml-base)"/>
            </xsl:if>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
