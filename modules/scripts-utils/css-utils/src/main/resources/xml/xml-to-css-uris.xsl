<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:d="http://www.daisy.org/ns/pipeline/data" xmlns:xs="http://www.w3.org/2001/XMLSchema"
    version="2.0" exclude-result-prefixes="#all">

    <xsl:param name="link-element" select="'true'" as="xs:string"/>

    <xsl:template match="/" name="stylesheets">
        <d:sheets>
            <xsl:apply-templates/>
        </d:sheets>
    </xsl:template>

    <xsl:template match="text()"/>

    <xsl:template match="processing-instruction('xml-stylesheet')">
        <xsl:if test="matches(., 'type=(&quot;\s*text/css\s*&quot;)|(''\s*text/css\s*'')')">
            <xsl:analyze-string select="." regex="href=\s*['&quot;]([^'&quot;\s]*)\s*['&quot;]">
                <xsl:matching-substring>
                    <d:sheet href="{regex-group(1)}"/>
                </xsl:matching-substring>
            </xsl:analyze-string>
        </xsl:if>
    </xsl:template>

    <xsl:template
        match="*:link[normalize-space(@href)][normalize-space(@rel) eq 'stylesheet' 
                             and (empty(@type) or normalize-space(@type) eq 'text/css')]">
        <xsl:if test="$link-element = 'true'">
            <d:sheet href="{normalize-space(@href)}"/>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
