<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:pf="http://www.daisy.org/ns/pipeline/functions" exclude-result-prefixes="#all"
    version="2.0">

    <xsl:function name="pf:normalize-uri" as="xs:string">
        <xsl:param name="uri" as="xs:string?"/>
        <xsl:sequence select="if (contains($uri,'#')) then substring-before($uri,'#') else $uri"/>
    </xsl:function>
    
    <xsl:function name="pf:normalize-uri" as="xs:string">
        <xsl:param name="uri" as="xs:string?"/>
        <xsl:param name="fragment" as="xs:boolean?"/>
        <xsl:sequence select="if (contains($uri,'#')) then substring-before($uri,'#') else $uri"/>
    </xsl:function>

</xsl:stylesheet>
