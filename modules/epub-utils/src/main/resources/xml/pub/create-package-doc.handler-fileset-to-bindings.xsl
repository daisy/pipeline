<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns="http://www.idpf.org/2007/opf"
                exclude-result-prefixes="#all">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl"/>

    <xsl:variable name="manifest" select="collection()[2]"/> <!-- hrefs normalized -->

    <!-- TODO : bindings is deprecated from 3.2 and may be removed in future specs -->

    <xsl:template match="/d:fileset">
        <xsl:variable name="bindings" as="element()*">
            <xsl:apply-templates select="d:file"/>
        </xsl:variable>
        <xsl:if test="exists($bindings)">
            <bindings>
                <xsl:sequence select="$bindings"/>
            </bindings>
        </xsl:if>
    </xsl:template>

    <xsl:template match="d:file">
        <xsl:message>
            <xsl:text>[WARNING] Bindings are deprecated and should not be used anymore. </xsl:text>
            <xsl:text>Please use HTML &lt;object/&gt; content fallback instead for </xsl:text>
            <xsl:value-of select="@media-type"/>
            <xsl:text>.</xsl:text>
        </xsl:message>
        <xsl:variable name="href" select="pf:normalize-uri(resolve-uri(@href,base-uri(.)))"/>
        <xsl:variable name="handler" as="element(d:file)?" select="$manifest//d:file[resolve-uri(@href,base-uri(.))=$href]"/>
        <xsl:if test="@media-type and $handler">
            <mediaType handler="{$handler/@id}" media-type="{@media-type}"/>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
