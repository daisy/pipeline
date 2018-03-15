<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="css:text[@target]">
        <xsl:if test="@css:white-space">
            <xsl:message select="concat('white-space:',@css:white-space,' could not be applied to target-text()')"/>
        </xsl:if>
        <xsl:variable name="target" select="@target"/>
        <css:box type="inline" css:anchor="{@target}">
            <xsl:value-of select="string(//*[@css:id=$target][1])"/>
        </css:box>
    </xsl:template>
    
    <!--
        Suppress warning messages "The source document is in no namespace, but the template rules
        all expect elements in a namespace" (see https://github.com/daisy/pipeline-mod-braille/issues/38)
    -->
    <xsl:template match="/phony">
        <xsl:next-match/>
    </xsl:template>
    
</xsl:stylesheet>
