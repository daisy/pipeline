<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:h="http://www.w3.org/1999/xhtml"
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
        <xsl:sequence select="if (exists($arg/root(.)/h:html/h:head/h:base[@href]))
                              then resolve-uri(
                                     normalize-space($arg/root(.)/h:html/h:head/h:base/@href[1]),
                                     $document-uri)
                              else $document-uri"/>
    </xsl:function>

    <!--
        Get encoding of HTML document as defined in metadata
    -->
    <xsl:function name="pf:html-encoding" as="xs:string">
        <xsl:param name="arg" as="document-node()"/>
        <xsl:sequence select="($arg/h:html/h:head/h:meta[lower-case(@name)='ncc:charset']/@content,
                               for $charset in $arg/h:html/h:head/h:meta[lower-case(@http-equiv)='content-type']/@content
                                                   /tokenize(.,'\s*;\s*')[starts-with(.,'charset=')]
                                 return substring-after($charset,'charset='),
                               'utf-8'
                               )[1]"/>
    </xsl:function>

</xsl:stylesheet>
