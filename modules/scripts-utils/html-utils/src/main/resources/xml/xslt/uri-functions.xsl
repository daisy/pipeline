<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xhtml="http://www.w3.org/1999/xhtml"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
    
    <!--
        Document base URL according to the XHTML rules: https://html.spec.whatwg.org/#document-base-url
    -->
    <xsl:function name="pf:html-base-uri" as="xs:anyURI?">
        <xsl:param name="arg" as="node()?"/>
        <xsl:sequence select="pf:html-base-uri($arg,pf:document-uri($arg))"/>
    </xsl:function>
    <xsl:function name="pf:html-base-uri" as="xs:anyURI?">
        <xsl:param name="arg" as="node()?"/>
        <xsl:param name="document-uri" as="xs:anyURI?"/>
        <xsl:sequence select="if (exists($arg/root(.)/xhtml:html/xhtml:head/xhtml:base[@href]))
                              then resolve-uri(
                                     normalize-space($arg/root(.)/xhtml:html/xhtml:head/xhtml:base/@href[1]),
                                     $document-uri)
                              else $document-uri"/>
    </xsl:function>
    
</xsl:stylesheet>
