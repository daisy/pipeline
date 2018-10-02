<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:d="http://www.daisy.org/ns/pipeline/data" xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
    xmlns:pf="http://www.daisy.org/ns/pipeline/functions" exclude-result-prefixes="#all" version="2.0">

    <!--
        
       Resource fileset cleanup: 
        - remove entries that do not match existing files
        - replace @href by EPUB-safe URIs 
        
    -->

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

    <xsl:output indent="yes"/>

    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>


    <xsl:template match="d:file[not(pf:file-exists(pf:unescape-uri(pf:get-path(resolve-uri(@href,base-uri())))))]"
        use-when="function-available('pf:file-exists')"/>

    <xsl:template match="d:file">
        <d:file href="{f:safe-uri(@href)}">
            <xsl:apply-templates select="node()|@* except @href"/>
        </d:file>
    </xsl:template>

    <xsl:function name="f:safe-uri" as="xs:string">
        <xsl:param name="uri" as="xs:string?"/>
        <xsl:sequence
            select="pf:replace-path($uri,escape-html-uri(replace(pf:unescape-uri(pf:get-path($uri)),'[^\p{L}\p{N}\-/_.]','_')))"
        />
    </xsl:function>

</xsl:stylesheet>
