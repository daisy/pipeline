<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/" 
    xmlns="http://www.daisy.org/ns/z3998/authoring/"
    version="2.0" exclude-result-prefixes="dtb">
    
    <xsl:output indent="yes" method="xml"/>
    
    <xsl:template match="//dtb:head">
        <xsl:for-each select="dtb:meta">
            <xsl:choose>
                <xsl:when test="@name = 'dc:Title'">
                    <meta property="dcterms:title" content="{@content}"/>
                </xsl:when>
                <xsl:when test="@name = 'dc:Creator'">
                    <meta property="dcterms:creator" content="{@content}"/>
                </xsl:when>
                <xsl:when test="@name = 'dc:Date'">
                    <meta property="dcterms:date" content="{@content}" xml:id="meta-dcdate"/>
                </xsl:when>
                <xsl:when test="@name = 'dc:Publisher'">
                    <meta property="dcterms:publisher" content="{@content}"/>
                </xsl:when>
                <xsl:when test="@name = 'dc:Language'">
                    <meta property="dcterms:language" content="{@content}"/>
                </xsl:when>
                <xsl:when test="@name = 'dc:Subject'">
                    <meta property="dcterms:subject" content="{@content}"/>
                </xsl:when>
                <xsl:when test="@name = 'dc:Description'">
                    <meta property="dcterms:description" content="{@content}"/>
                </xsl:when>
                <xsl:when test="@name = 'dc:Contributor'">
                    <meta property="dcterms:contributor" content="{@content}"/>
                </xsl:when>
                <xsl:when test="@name = 'dc:Type'">
                    <meta property="dcterms:type" content="{@content}"/>
                </xsl:when>
                <xsl:when test="@name = 'dc:Format'">
                    <meta property="dcterms:format" content="{@content}"/>
                </xsl:when>
                <xsl:when test="@name = 'dc:Source'">
                    <meta property="dcterms:source" content="{@content}"/>
                </xsl:when>
                <xsl:when test="@name = 'dc:Relation'">
                    <meta property="dcterms:relation" content="{@content}"/>
                </xsl:when>
                <xsl:when test="@name = 'dc:Coverage'">
                    <meta property="dcterms:coverage" content="{@content}"/>
                </xsl:when>
                <xsl:when test="@name = 'dc:Rights'">
                    <meta property="dcterms:rights" content="{@content}"/>
                </xsl:when>
                
                <xsl:when test="@name = 'dtb:revisionDescription'">
                    <meta property="dcterms:description" content="{@content}"
                        about="#meta-dcdate"/>
                </xsl:when>
            </xsl:choose>
            
        </xsl:for-each>
    </xsl:template>
   
    <!-- identity template which discards everything -->
    <xsl:template match="@*|node()">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
</xsl:stylesheet>
