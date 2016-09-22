<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="c"
    version="2.0">
    
    <xsl:template match="/c:directory | c:directory[not(ancestor::c:directory)]">
        <d:fileset>
            <xsl:copy-of select="@*[not(name()='name')]"/>
            <xsl:attribute name="xml:base" select="if (ends-with(base-uri(),'/')) then base-uri() else concat(base-uri(),'/')"/>
            <xsl:for-each select="*">
                <xsl:sort select="encode-for-uri(@name)"/>
                <xsl:apply-templates select=".">
                    <xsl:with-param name="base" select="encode-for-uri(@name)"/>
                </xsl:apply-templates>
            </xsl:for-each>
        </d:fileset>
    </xsl:template>
    
    <xsl:template match="c:other"/>
    
    <xsl:template match="c:directory">
        <xsl:param name="base" required="yes" as="xs:string*"/>
        <xsl:for-each select="*">
            <xsl:sort select="encode-for-uri(@name)"/>
            <xsl:apply-templates select=".">
                <xsl:with-param name="base" select="($base,encode-for-uri(@name))"/>
            </xsl:apply-templates>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template match="c:file">
        <xsl:param name="base" required="yes" as="xs:string*"/>
        <d:file href="{string-join($base,'/')}">
            <xsl:copy-of select="@*[not(name()='name')]"/>
            <xsl:copy-of select="node()"/>
        </d:file>
    </xsl:template>
    
</xsl:stylesheet>
