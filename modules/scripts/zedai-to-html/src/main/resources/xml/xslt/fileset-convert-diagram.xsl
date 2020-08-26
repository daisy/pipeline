<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:diagram="http://www.daisy.org/ns/z3998/authoring/features/description/"
                exclude-result-prefixes="#all">
    
    <!--
        This XSLT receives a fileset with DIAGRAM entries as the primary document and XML documents
        containing DIAGRAM descriptions in the default collection.
        
        The primary result document is the fileset where the DIAGRAM entries have been replaced by
        HTML entries.  The secondary result documents contain the HTML documents created from the
        DIAGRAM descriptions.
    -->

    <xsl:import href="diagram-to-html.xsl"/>

    <xsl:template mode="fileset" match="node() | @*" >
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="node() | @*"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template mode="fileset" match="d:file">
        <xsl:variable name="doc" as="document-node()?"
                      select="collection()[/diagram:description[base-uri()=current()/resolve-uri(@href,base-uri(.))]]"/>
        <xsl:choose>
            <xsl:when test="exists($doc)">
                <xsl:variable name="new-href" select="replace(@href,'\.[^.]+','.xhtml')"/>
                <xsl:copy>
                    <xsl:attribute name="href-before-move" select="@href"/>
                    <xsl:attribute name="href" select="$new-href"/>
                    <xsl:attribute name="media-type" select="'application/xhtml+xml'"/>
                    <xsl:apply-templates mode="#current" select="node()|@* except (@href|@original-href|@media-type)"/>
                </xsl:copy>
                <xsl:result-document href="{resolve-uri($new-href,base-uri(.))}">
                    <xsl:apply-templates mode="#default" select="$doc"/>
                </xsl:result-document>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template mode="fileset" match="d:file/@href">
        <xsl:sequence select="."/>
        <xsl:attribute name="href-before-move" select="."/>
    </xsl:template>

</xsl:stylesheet>
