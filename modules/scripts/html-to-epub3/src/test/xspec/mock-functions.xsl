<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:pf="http://www.daisy.org/ns/pipeline/functions" exclude-result-prefixes="#all"
    version="2.0">

    <xsl:function name="pf:get-extension" as="xs:string">
        <xsl:param name="uri" as="xs:string?"/>
        <xsl:sequence select="replace($uri,'^.*\.([^.]*)$','$1')"/>
    </xsl:function>

    <xsl:function name="pf:get-scheme" as="xs:string">
        <xsl:param name="uri" as="xs:string?"/>
        <xsl:sequence select="replace($uri,'^(([^:]*):)?.+','$2')"/>
    </xsl:function>

    <xsl:function name="pf:is-absolute" as="xs:boolean">
        <xsl:param name="uri" as="xs:string?"/>
        <xsl:sequence select="matches($uri,'([^:]+:)?/.*')"/>
    </xsl:function>

    <xsl:function name="pf:is-relative" as="xs:boolean">
        <xsl:param name="uri" as="xs:string?"/>
        <xsl:sequence select="not(matches($uri,'^[^/]+:'))"/>
    </xsl:function>

    
    <xsl:function name="pf:get-path" as="xs:string">
        <xsl:param name="uri" as="xs:string?"/>
        <xsl:sequence select="$uri"/>
    </xsl:function>
    
    <xsl:function name="pf:normalize-uri" as="xs:string">
        <xsl:param name="uri" as="xs:string?"/>
        <xsl:param name="fragment" as="xs:boolean?"/>
        <xsl:sequence select="if (contains($uri,'#')) then substring-before($uri,'#') else $uri"/>
    </xsl:function>

    <xsl:function name="pf:replace-path" as="xs:string">
        <xsl:param name="uri" as="xs:string?"/>
        <xsl:param name="path" as="xs:string?"/>
        <xsl:sequence select="if (starts-with($uri,'#')) then $uri else replace($uri,'^[^#]+',$path)"/>
    </xsl:function>

    <xsl:function name="pf:unescape-uri" as="xs:string">
        <xsl:param name="uri" as="xs:string?"/>
        <xsl:sequence select="replace($uri,' ','%20')"/>
    </xsl:function>

</xsl:stylesheet>
