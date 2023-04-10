<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="#all">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

    <xsl:template match="/*">
        <xsl:next-match>
            <xsl:with-param name="base" tunnel="yes" select="base-uri(.)"/>
        </xsl:next-match>
    </xsl:template>

    <xsl:template match="d:file/@href">
        <xsl:param name="base" tunnel="yes" required="yes"/>
        <xsl:variable name="absolute-href" select="resolve-uri(.,base-uri(.))"/>
        <xsl:attribute name="href" select="f:safe-uri(pf:relativize-uri($absolute-href,$base))"/>
        <xsl:attribute name="absolute-href-before-move" select="$absolute-href"/>
    </xsl:template>

    <xsl:template match="d:file/@original-href">
        <xsl:attribute name="original-href" select="resolve-uri(.,base-uri(.))"/>
    </xsl:template>

    <xsl:function name="f:safe-uri" as="xs:string">
        <xsl:param name="uri" as="xs:string"/>
        <xsl:sequence select="pf:replace-path(
                                $uri,
                                escape-html-uri(
                                  replace(
                                    pf:unescape-uri(pf:get-path($uri)),
                                    '[^\p{L}\p{N}\-/_.]','_')))"/>
    </xsl:function>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
