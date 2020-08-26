<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns="http://www.idpf.org/2007/opf"
                xpath-default-namespace="http://www.idpf.org/2007/opf"
                exclude-result-prefixes="#all">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl"/>
    <xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/generate-id.xsl"/>

    <xsl:param name="nav-doc-uri" required="yes"/>

    <xsl:template match="manifest">
        <xsl:variable name="manifest-base" select="pf:base-uri(.)"/>
        <xsl:variable name="manifest-with-nav">
            <xsl:copy>
                <xsl:apply-templates select="@*"/>
                <xsl:choose>
                    <xsl:when test="item/resolve-uri(@href,base-uri(.))=$nav-doc-uri">
                        <xsl:apply-templates mode="set-nav-property" select="item"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:sequence select="item"/>
                        <item href="{pf:relativize-uri($nav-doc-uri,$manifest-base)}"
                              media-type="application/xhtml+xml" properties="nav"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:copy>
        </xsl:variable>
        <xsl:apply-templates mode="add-ids" select="$manifest-with-nav"/>
    </xsl:template>

    <xsl:template mode="add-ids" match="manifest">
        <xsl:call-template name="pf:next-match-with-generated-ids">
            <xsl:with-param name="prefix" select="'item_'"/>
            <xsl:with-param name="for-elements" select="item[not(@id)]"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template mode="add-ids" match="item[not(@id)]">
        <xsl:copy>
            <xsl:call-template name="pf:generate-id"/>
            <xsl:apply-templates mode="#current" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template mode="set-nav-property" match="item[resolve-uri(@href,base-uri(.))=$nav-doc-uri]">
        <xsl:copy>
            <xsl:sequence select="@*"/>
            <xsl:attribute name="properties" select="string-join((tokenize(@properties,'\s+')[not(.='')],'nav'),' ')"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template mode="#default add-ids set-nav-property" match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
