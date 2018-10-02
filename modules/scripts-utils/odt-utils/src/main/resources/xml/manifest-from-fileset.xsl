<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:d="http://www.daisy.org/ns/pipeline/data"
        xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
        xmlns:manifest="urn:oasis:names:tc:opendocument:xmlns:manifest:1.0">
    
    <xsl:variable name="base" select="//d:file[starts-with(@media-type,'application/vnd.oasis.opendocument')]
                                      /resolve-uri(@href, base-uri(.))"/>
    
    <xsl:include href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
    
    <xsl:template match="d:fileset">
        <xsl:if test="not($base)">
            <xsl:message terminate="yes">
                <xsl:text>[odt-utils] ERROR: manifest could not be created from fileset, no entry with media-type application/vnd.oasis.opendocument*</xsl:text>
            </xsl:message>
        </xsl:if>
        <xsl:element name="manifest:manifest">
            <xsl:attribute name="manifest:version" select="'1.2'"/>
            <xsl:attribute name="xml:base" select="resolve-uri('META-INF/manifest.xml', $base)"/>
            <xsl:apply-templates select="d:file"/>
            <xsl:if test="d:file[starts-with(pf:relativize-uri(resolve-uri(@href, base-uri(.)), $base), 'Configurations2/')]">
                <xsl:element name="manifest:file-entry">
                    <xsl:attribute name="manifest:full-path" select="'Configurations2/'"/>
                    <xsl:attribute name="manifest:media-type" select="'application/vnd.sun.xml.ui.configuration'"/>
                </xsl:element>
            </xsl:if>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="d:file[resolve-uri(@href,base-uri(.))=$base]">
        <xsl:element name="manifest:file-entry">
            <xsl:attribute name="manifest:full-path" select="'/'"/>
            <xsl:attribute name="manifest:version" select="'1.2'"/>
            <xsl:attribute name="manifest:media-type" select="@media-type"/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="d:file">
        <xsl:variable name="absolute-uri" select="resolve-uri(@href,base-uri(.))"/>
        <xsl:variable name="relative-uri" select="pf:relativize-uri($absolute-uri, $base)"/>
        <xsl:choose>
            <xsl:when test="not(pf:is-absolute($relative-uri))">
                <xsl:choose>
                    <xsl:when test="@media-type='application/mathml+xml' and ends-with($relative-uri, '/content.xml')">
                        <xsl:element name="manifest:file-entry">
                            <xsl:attribute name="manifest:full-path" select="$relative-uri"/>
                            <xsl:attribute name="manifest:media-type" select="'text/xml'"/>
                        </xsl:element>
                        <xsl:element name="manifest:file-entry">
                            <xsl:attribute name="manifest:full-path" select="replace($relative-uri, '^(.*)content\.xml$', '$1')"/>
                            <xsl:attribute name="manifest:media-type" select="'application/vnd.oasis.opendocument.formula'"/>
                        </xsl:element>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:element name="manifest:file-entry">
                            <xsl:attribute name="manifest:full-path" select="$relative-uri"/>
                            <xsl:attribute name="manifest:media-type" select="@media-type"/>
                        </xsl:element>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message>
                    <xsl:text>[odt-utils] WARNING: the file </xsl:text>
                    <xsl:value-of select="$absolute-uri"/>
                    <xsl:text> will not be included in the manifest because it falls outside of the base directory </xsl:text>
                    <xsl:value-of select="$base"/>
                </xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
</xsl:stylesheet>
