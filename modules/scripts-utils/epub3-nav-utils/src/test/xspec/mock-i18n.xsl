<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:pf="http://www.daisy.org/ns/pipeline/functions" exclude-result-prefixes="#all"
    version="2.0">

    <xsl:function name="pf:i18n-translate" as="xs:string">
        <xsl:param name="string" as="xs:string"/>
        <xsl:param name="language" as="xs:string"/>
        <xsl:param name="maps" as="element()*"/>
        <xsl:sequence select="$string"/>
    </xsl:function>        
</xsl:stylesheet>
