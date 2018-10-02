<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs" version="2.0" xmlns:html="http://www.w3.org/1999/xhtml"
    xmlns:pf="http://www.daisy.org/ns/pipeline/functions" xmlns="" xpath-default-namespace="">

        <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
<!--    <xsl:import href="../../../../test/xspec/mock/uri-functions.xsl"/>-->

    <xsl:template match="/*">
        <xsl:variable name="base-uri" select="base-uri()"/>
        <xsl:copy>
            <xsl:apply-templates select="@*|html:*">
                <xsl:with-param name="base-uri" tunnel="yes" select="$base-uri"/>
                <xsl:with-param name="smil-href" tunnel="yes" select="pf:relativize-uri(base-uri(smil),$base-uri)"/>
                <xsl:with-param name="smil" tunnel="yes" select="smil"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="@*|node()[not(self::*)]">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="*">
        <xsl:param name="base-uri" as="xs:string" tunnel="yes"/>
        <xsl:param name="smil-href" as="xs:string" tunnel="yes"/>
        <xsl:param name="smil" as="element()" tunnel="yes"/>
        <xsl:variable name="id" select="@id"/>
        <xsl:variable name="par" select="if (not(@id)) then () else ($smil//par[text/resolve-uri(@src,base-uri()) = concat($base-uri,'#',$id)])[1]"/>

        <xsl:choose>
            <xsl:when test="$par">
                <xsl:copy>
                    <xsl:apply-templates select="@*"/>
                    <xsl:element name="a" namespace="http://www.w3.org/1999/xhtml">
                        <xsl:attribute name="href" select="concat($smil-href,'#',$par/@id)"/>
                        <xsl:apply-templates select="node()"/>
                    </xsl:element>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy>
                    <xsl:apply-templates select="@*|node()"/>
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

</xsl:stylesheet>
