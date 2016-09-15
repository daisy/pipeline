<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet exclude-result-prefixes="#all" version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
    xmlns:d="http://www.daisy.org/ns/pipeline/data" xmlns:xs="http://www.w3.org/2001/XMLSchema">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl"/>

    <!-- Joint fileset base: longest common URI of all fileset bases -->
    <xsl:variable name="base"
        select="pf:longest-common-uri(distinct-values(collection()/d:fileset[@xml:base]/pf:normalize-uri(@xml:base)))"
        as="xs:string"/>


    <xsl:template name="join">
        <d:fileset>
            <xsl:if test="$base">
                <xsl:attribute name="xml:base" select="$base"/>
            </xsl:if>
            <xsl:for-each-group select="collection()/d:fileset/d:file"
                group-by="
                if ($base) then pf:normalize-uri(pf:relativize-uri(resolve-uri(@href,base-uri(.)),$base))
                else if (not(matches(@href,'^\w+:'))) then pf:normalize-uri(resolve-uri(@href,base-uri(.)))
                else pf:normalize-uri(@href)">
                <d:file href="{current-grouping-key()}">
                    <xsl:apply-templates
                        select="current-group()/(@* except @href) | current-group()/*"/>
                </d:file>
            </xsl:for-each-group>
        </d:fileset>
    </xsl:template>

    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
