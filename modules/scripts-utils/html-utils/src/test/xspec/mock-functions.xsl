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

    <xsl:function name="pf:relativize-uri" as="xs:string">
        <xsl:param name="uri" as="xs:string?"/>
        <xsl:param name="base" as="xs:string?"/>
        <xsl:variable name="base-dir" select="replace($base,'^(.+?/?)([^/]*)?$','$1')"/>
        <xsl:sequence
            select="if (starts-with($uri,$base-dir)) then substring($uri,string-length($base-dir)+1) else $uri"
        />
    </xsl:function>

    <xsl:function name="pf:is-absolute" as="xs:boolean">
        <xsl:param name="uri" as="xs:string?"/>
        <xsl:sequence select="matches($uri,'([^:]+:)?/.*')"/>
    </xsl:function>
    
    <xsl:function name="pf:is-relative" as="xs:boolean">
        <xsl:param name="uri" as="xs:string?"/>
        <xsl:sequence select="not(pf:is-absolute($uri))"/>
    </xsl:function>

    <xsl:function name="pf:normalize-uri" as="xs:string">
        <xsl:param name="uri" as="xs:string?"/>
        <xsl:sequence select="$uri"/>
    </xsl:function>

    <xsl:function name="pf:normalize-uri" as="xs:string">
        <xsl:param name="uri" as="xs:string?"/>
        <xsl:param name="fragment" as="xs:boolean?"/>
        <xsl:sequence select="if (contains($uri,'#')) then substring-before($uri,'#') else $uri"/>
    </xsl:function>

</xsl:stylesheet>
