<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
    xmlns:d="http://www.daisy.org/ns/pipeline/data" exclude-result-prefixes="#all" version="2.0">

    <!-- Note: files without @media-type are all assumed to be binary files. -->

    <xsl:function name="pf:is-xml" as="xs:boolean">
        <xsl:param name="media-type" as="xs:string"/>
        <xsl:sequence
            select="ends-with($media-type,'+xml') or ends-with($media-type,'/xml') or $media-type=('application/smil')"
        />
    </xsl:function>

    <xsl:function name="pf:is-html" as="xs:boolean">
        <xsl:param name="media-type" as="xs:string"/>
        <xsl:sequence select="$media-type=('text/html','application/xhtml+xml')"/>
    </xsl:function>

    <xsl:function name="pf:is-text" as="xs:boolean">
        <xsl:param name="media-type" as="xs:string"/>
        <xsl:sequence select="not(pf:is-xml($media-type)) and starts-with($media-type,'text/')"/>
    </xsl:function>

    <xsl:function name="pf:is-binary" as="xs:boolean">
        <xsl:param name="media-type" as="xs:string"/>
        <xsl:sequence
            select="not(pf:is-xml($media-type) or pf:is-text($media-type) or pf:is-html($media-type))"
        />
    </xsl:function>

    <xsl:function name="pf:mediatype-from-extension" as="xs:string">
        <xsl:param name="extension" as="xs:string"/>
        <xsl:value-of
            select="(doc('../maps/ext-to-mediatype.xml')//entry[@key=$extension]/@value, 'application/octet-stream')[1]"
        />
    </xsl:function>
    
    <xsl:function name="pf:mediatype-from-namespace" as="xs:string">
        <xsl:param name="extension" as="xs:string"/>
        <xsl:value-of
            select="(doc('../maps/ns-to-mediatype.xml')//entry[@key=$extension]/@value, 'application/xml')[1]"
        />
    </xsl:function>

    <!-- makes it easy to run the functions on a d:file from XProc -->
    <xsl:template match="/d:file">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="is-xml" select="pf:is-xml(@media-type)"/>
            <xsl:attribute name="is-html" select="pf:is-html(@media-type)"/>
            <xsl:attribute name="is-text" select="pf:is-text(@media-type)"/>
            <xsl:attribute name="is-binary" select="pf:is-binary(@media-type)"/>
            <xsl:copy-of select="node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
