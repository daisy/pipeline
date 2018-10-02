<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all" version="2.0" xmlns:opf="http://www.idpf.org/2007/opf"
    xmlns:pf="http://www.daisy.org/ns/pipeline/functions" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns="http://www.w3.org/1999/xhtml">
    
    <!-- provided a sequence of all html documents as well as the opf document wrapped in a wrapper element, will sort the html documents in spine order, and remove the opf as well as any documents not in the spine. -->
    
    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
    <!--<xsl:import href="../../../../test/xspec/mock-functions.xsl"/>-->

    <xsl:template match="/*">
        <xsl:copy>
            <xsl:variable name="documents" as="element()*" select="*"/>
            <xsl:variable name="opf" select="$documents[self::opf:package]"/>
            
            <xsl:for-each select="$opf/opf:spine/opf:itemref/@idref">
                <xsl:variable name="href" select="pf:normalize-uri(string(resolve-uri($opf/opf:manifest/opf:item[@id=current()]/@href, base-uri($opf))))"/>
                <xsl:variable name="html" select="$documents[pf:normalize-uri(string(base-uri())) = $href]"/>
                <xsl:copy-of select="$html"/>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
