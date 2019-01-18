<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                version="2.0" exclude-result-prefixes="#all">

    <xsl:param name="include-links" select="'true'" as="xs:string"/>

    <xsl:template match="/">
        <d:fileset>
            <!--
                no need to set xml:base explicitly because it should be automatically carried over
                <xsl:attribute name="xml:base" select="base-uri(/)"/>
            -->
            <xsl:apply-templates select="/processing-instruction('xml-stylesheet')"/>
            <xsl:apply-templates select="*">
                <xsl:with-param name="fileset-base" select="base-uri(/)"/>
            </xsl:apply-templates>
        </d:fileset>
    </xsl:template>

    <xsl:template match="/processing-instruction('xml-stylesheet')">
        <xsl:if test="matches(., 'type=(&quot;\s*text/css\s*&quot;)|(''\s*text/css\s*'')')">
            <xsl:analyze-string select="." regex="href=\s*['&quot;]([^'&quot;\s]*)\s*['&quot;]">
                <xsl:matching-substring>
                    <d:file href="{regex-group(1)}" media-type="text/css"/>
                </xsl:matching-substring>
            </xsl:analyze-string>
        </xsl:if>
    </xsl:template>

    <xsl:template match="*:link[normalize-space(@href)]
                               [normalize-space(@rel)='stylesheet'
                                and (empty(@type) or normalize-space(@type)='text/css')]">
        <xsl:param name="fileset-base" tunnel="yes" select="''"/>
        <xsl:if test="$include-links='true'">
            <xsl:variable name="href" select="normalize-space(@href)"/>
            <!--
                $fileset-base='' when run from XSpec
            -->
            <xsl:variable name="href" select="if ($fileset-base='' or base-uri(.)=$fileset-base)
                                              then $href
                                              else resolve-uri($href,base-uri(.))"/>
            <d:file href="{$href}" media-type="text/css"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="*">
        <xsl:apply-templates select="*"/>
    </xsl:template>

</xsl:stylesheet>
