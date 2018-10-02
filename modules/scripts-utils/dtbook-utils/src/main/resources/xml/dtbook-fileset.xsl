<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:d="http://www.daisy.org/ns/pipeline/data" xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/" version="2.0"
    exclude-result-prefixes="#all" xmlns:pf="http://www.daisy.org/ns/pipeline/functions">
    
    <!-- Returns a fileset of all the resources references from a DTBook (i.e. images and CSS files) -->
    
    <!--
        Note: this file is currently not used
    -->
    
    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
    
    <xsl:template match="/">
        <xsl:variable name="base" select="base-uri(/*)"/>
        <d:fileset xml:base="{replace($base,'[^/]+$','')}">
            <d:file href="{replace($base,'^.*/([^/]+)$','$1')}" media-type="application/x-dtbook+xml" original-href="{$base}"/>
            <xsl:for-each select="processing-instruction('xml-stylesheet')">
                <xsl:variable name="href" select="replace(replace(.,'type=&quot;[^&quot;]*&quot;',''),'\s*href=&quot;(.*)&quot;\s*','$1')"/>
                <d:file href="{$href}" original-href="{resolve-uri($href,$base)}"/>
            </xsl:for-each>
            <xsl:for-each select="//dtbook:link/@href">
                <d:file href="{pf:relativize-uri(resolve-uri(.,base-uri(.)),$base)}" original-href="{resolve-uri(.,base-uri(.))}"/>
            </xsl:for-each>
            <xsl:for-each select="//dtbook:img/@src">
                <d:file href="{pf:relativize-uri(resolve-uri(.,base-uri(.)),$base)}" original-href="{resolve-uri(.,base-uri(.))}"/>
            </xsl:for-each>
        </d:fileset>
    </xsl:template>
</xsl:stylesheet>
