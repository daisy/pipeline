<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:x="http://www.daisy.org/ns/pipeline/xproc/test" xmlns:f="http://www.daisy.org/ns/pipeline/xproc/test/internal-functions" xmlns:rng="http://relaxng.org/ns/structure/1.0"
    xmlns:a="http://relaxng.org/ns/compatibility/annotations/1.0" version="2.0" xmlns="http://www.w3.org/1999/xhtml" xpath-default-namespace="http://www.w3.org/1999/xhtml">

    <!-- TODO: join documentation that is written across through multiple <attribute/>s with the same name -->

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="code[ancestor::p]">
        <xsl:variable name="following" select="following-sibling::node()[1][self::text()]"/>
        <xsl:choose>
            <xsl:when test="not(matches(.,'^\w+$'))">
                <xsl:copy-of select="."/>
            </xsl:when>
            <xsl:when test="following-sibling::text()[not(matches(.,'^\s*and\s*$','i') or matches(.,'^\s*[,;]\s*','i'))][1][matches(.,'^\s*element','i')]">
                <a href="#the-{.}-element"><![CDATA[]]><xsl:copy-of select="."/><![CDATA[]]></a>
            </xsl:when>
            <xsl:when test="following-sibling::text()[not(matches(.,'^\s*and\s*$','i') or matches(.,'^\s*[,;]\s*$','i'))][1][matches(.,'^\s*attribute','i')]">
                <xsl:variable name="element" select="if (matches(ancestor::section/@id,'the-\w*-element')) then replace(ancestor::section/@id,'the-(\w*)-element','$1') else ()"/>
                <xsl:choose>
                    <xsl:when test="$element">
                        <a href="#attr-{$element}-{.}"><![CDATA[]]><xsl:copy-of select="."/><![CDATA[]]></a>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:copy-of select="."/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
