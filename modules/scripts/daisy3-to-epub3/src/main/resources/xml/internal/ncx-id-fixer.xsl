<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns="http://www.w3.org/1999/xhtml"
    xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
    xpath-default-namespace="http://www.w3.org/1999/xhtml" exclude-result-prefixes="#all"
    version="2.0">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
    
    <xsl:param name="ncx-uri" select="''"/>
    
    <xsl:variable name="id-map" select="collection()[/d:idmap]"/>
    
    <xsl:template match="@href">
        <xsl:variable name="srcref" select="resolve-uri(substring-before(.,'#'),$ncx-uri)"/>
        <xsl:variable name="idref" select="substring-after(.,'#')"/>
        <xsl:variable name="mapping" select="$id-map/d:idmap/d:doc[@href=$srcref]/d:id[@old-id=$idref]"/>
        <!--TODO relativize mapping new src-->
        <!--TODO concatenate mapping new ID-->
        <xsl:attribute name="href" select="concat(pf:relativize-uri($mapping/@new-src,base-uri(.)),'#',$mapping/@new-id)"/>
    </xsl:template>


    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>
    

</xsl:stylesheet>
