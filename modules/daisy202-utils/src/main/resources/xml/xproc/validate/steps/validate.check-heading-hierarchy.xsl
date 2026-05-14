<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs" version="2.0" xmlns:html="http://www.w3.org/1999/xhtml"
    xpath-default-namespace="http://www.w3.org/1999/xhtml" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:d="http://www.daisy.org/ns/pipeline/data">

    <xsl:template match="/*">
        <d:messages>
            <xsl:apply-templates select="//(h1|h2|h3|h4|h5|h6)"/>
        </d:messages>
    </xsl:template>
    
    <xsl:template match="h1|h2|h3|h4|h5|h6">
        <xsl:variable name="base-uri" select="base-uri(/*)"/>
        <xsl:variable name="level" select="number(substring(local-name(),2))"/>
        <xsl:variable name="preceding-level"
            select="number(substring((preceding::h1|preceding::h2|preceding::h3|preceding::h4|preceding::h5|preceding::h6)[last()]/local-name(),2))"/>
        <xsl:if test="$level - 1 &gt; $preceding-level">
            <d:message severity="error">
                <d:desc>incorrect heading hierarchy at <xsl:value-of select="concat('/',string-join(for $e in (ancestor-or-self::*) return concat($e/name(),'[',(count($e/preceding-sibling::*[name()=$e/name()])+1),']'),'/'))"
                /></d:desc>
                <d:file>
                    <xsl:value-of select="$base-uri"/>
                </d:file>
                <d:was>h<xsl:value-of select="$level"/></d:was>
                <d:expected>h<xsl:value-of select="$preceding-level+1"/> or less</d:expected>
            </d:message>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="text()"/>

</xsl:stylesheet>
