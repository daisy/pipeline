<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:d="http://www.daisy.org/ns/pipeline/data"
        xmlns:manifest="urn:oasis:names:tc:opendocument:xmlns:manifest:1.0">
    
    <xsl:param name="base" required="yes"/>
    <xsl:param name="original-base" select="''"/>
    
    <xsl:template match="manifest:manifest">
        <xsl:element name="d:fileset">
            <xsl:attribute name="xml:base" select="$base"/>
            <xsl:apply-templates select="manifest:file-entry"/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="manifest:file-entry">
        <xsl:variable name="path" select="@manifest:full-path"/>
        <xsl:element name="d:file">
            <xsl:attribute name="href" select="$path"/>
            <xsl:if test="$original-base!=''">
                <xsl:attribute name="original-href" select="resolve-uri($path, $original-base)"/>
            </xsl:if>
            <xsl:if test="not(string(@manifest:media-type)='')">
                <xsl:attribute name="media-type"
                               select="if (@manifest:media-type='text/xml' and
                                           ends-with($path, '/content.xml') and
                                           //manifest:file-entry[@manifest:full-path=replace($path, '^(.*)content\.xml$', '$1') and
                                                                 @manifest:media-type='application/vnd.oasis.opendocument.formula'])
                                       then 'application/mathml+xml'
                                       else @manifest:media-type"/>
            </xsl:if>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="manifest:file-entry[ends-with(@manifest:full-path, '/')]" priority="0.6"/>
    
    <xsl:template match="manifest:file-entry[@manifest:full-path='/' and starts-with(@manifest:media-type, 'application/vnd.oasis.opendocument')]"
                  priority="0.7">
        <xsl:element name="d:file">
            <xsl:attribute name="href" select="'./'"/>
            <xsl:if test="not(string(@manifest:media-type)='')">
                <xsl:attribute name="media-type" select="@manifest:media-type"/>
            </xsl:if>
        </xsl:element>
    </xsl:template>
    
</xsl:stylesheet>
