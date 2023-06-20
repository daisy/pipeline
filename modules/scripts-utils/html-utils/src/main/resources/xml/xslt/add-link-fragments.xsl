<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:svg="http://www.w3.org/2000/svg"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:m="http://www.w3.org/1998/Math/MathML"
                xmlns="http://www.w3.org/1999/xhtml"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all">
    
    <xsl:import href="library.xsl"/>

    <xsl:variable name="doc-base" select="pf:html-base-uri(/)"/>
    <xsl:variable name="file-to-body-id" as="element()">
        <d:fileset>
            <xsl:for-each select="collection()[position()&gt;1]/*">
                <d:file href="{base-uri(.)}" id="{.//body/@id}"/>
            </xsl:for-each>
        </d:fileset>
    </xsl:variable>

    <xsl:template match="@aria-describedat  |
                         @longdesc          |
                         link/@href         |
                         a/@href            |
                         area/@href         |
                         script/@scr        |
                         img/@src           |
                         iframe/@src        |
                         embed/@src         |
                         object/@data       |
                         audio/@src         |
                         video/@src         |
                         source/@src        |
                         track/@src         |
                         input/@src         |
                         input/@formaction  |
                         button/@formaction |
                         form/@action       |
                         blockquote/@cite   |
                         q/@cite            |
                         ins/@cite          |
                         del/@cite          |
                         head/@profile      |
                         svg:*/@xlink:href  |
                         svg:*/@href        |
                         m:math/@altimg     |
                         m:mglyph/@src      ">
        <xsl:choose>
            <xsl:when test="not(contains(.,'#')) or ends-with(.,'#')">
                <xsl:variable name="uri" select="resolve-uri(replace(.,'#$',''),$doc-base)"/>
                <xsl:choose>
                    <xsl:when test="$file-to-body-id/d:file[@href=$uri]">
                        <xsl:attribute name="{local-name(.)}" namespace="{namespace-uri(.)}"
                                       select="concat($uri,'#',$file-to-body-id/d:file[@href=$uri]/@id)"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:next-match/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
