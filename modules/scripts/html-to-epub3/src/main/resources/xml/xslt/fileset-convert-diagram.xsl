<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:diagram="http://www.daisy.org/ns/z3998/authoring/features/description/"
    xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
    xmlns:pf="http://www.daisy.org/ns/pipeline/functions" exclude-result-prefixes="#all"
    version="2.0">

    <!--
        
        This XSLT receives a resource fileset as the primary documenet and XML documents (possibly containing
        DIAGRAM descriptions) in the default collection.
        
        The primary result document is the fileset where DIAGRAM entries have been replaced by HTML entries.
        The secondary resultl documents contain the HTML documents created from the DIAGRAM descriptions.
        
    -->
    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
    <xsl:import href="diagram-to-html.xsl"/>

    <xsl:param name="content-dir" as="xs:string" required="yes"/>

    <xsl:output indent="yes"/>

    <xsl:template match="node() | @*" mode="fileset">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*" mode="fileset"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template
        match="d:file[ tokenize(@kind,'\s+') = 'description' 
                       and @media-type=('application/xml','application/z3998-auth-diagram+xml')]"
        mode="fileset">
        <xsl:variable name="doc"
            select="collection()[/diagram:description[base-uri()=current()/@original-href]]"
            as="document-node()?"/>
        <xsl:choose>
            <xsl:when test="exists($doc)">
                <xsl:variable name="new-href" select="replace(@href,'\.[^.]+','.xhtml')"/>
                <d:file href="{$new-href}" media-type="application/xhtml+xml">
                    <xsl:apply-templates
                        select="node()|@* except (@href|@media-type)" mode="fileset"
                    />
                </d:file>
                <xsl:result-document href="{resolve-uri($new-href,$content-dir)}">
                    <xsl:apply-templates select="$doc" mode="#default"/>
                </xsl:result-document>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
