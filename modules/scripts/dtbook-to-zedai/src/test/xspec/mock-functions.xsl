<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions">

    <xsl:function name="pf:get-extension" as="xs:string">
        <xsl:param name="uri" as="xs:string"/>
        <xsl:sequence select="lower-case(replace($uri,'^.*\.([^.]*)$','$1'))"/>
    </xsl:function>
    
    <xsl:function name="pf:mediatype-from-extension" as="xs:string">
        <xsl:param name="extension" as="xs:string"/>
        <xsl:sequence select="if ($extension='jpg') then 'image/jpeg'
                    else if ($extension='png') then 'image/png'
                    else ''"/>
    </xsl:function>
    
    <xsl:template name="pf:next-match-with-generated-ids">
        <xsl:param name="prefix"/>
        <xsl:param name="for-elements"/>
        <xsl:next-match/>
    </xsl:template>
    
    <xsl:template name="pf:generate-id">
        <xsl:attribute name="id" select="generate-id()"/>
    </xsl:template>
    
</xsl:stylesheet>
