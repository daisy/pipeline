<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:pf="http://www.daisy.org/ns/pipeline/functions" exclude-result-prefixes="#all"
    version="2.0" xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions">

    <xsl:function name="pf:unescape-uri" as="xs:string">
        <xsl:param name="string" as="xs:string?"/>
        <xsl:sequence select="$string"/>
    </xsl:function>

</xsl:stylesheet>
