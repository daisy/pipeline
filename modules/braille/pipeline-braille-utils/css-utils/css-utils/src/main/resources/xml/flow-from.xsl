<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:include href="library.xsl"/>
    
    <!--
        default scope within before and after pseudo-elements is 'backward'
    -->
    <xsl:template match="css:flow[@from and not(@scope)]">
        <xsl:variable name="flow" as="xs:string" select="@from"/>
        <xsl:variable name="this" as="element()" select="."/>
        <xsl:variable name="preceding-consumer" as="element()?" select="preceding::css:flow[@from=$flow and not(@scope)][1]"/>
        <xsl:for-each select="collection()/*[@css:flow=$flow]/*">
            <xsl:variable name="anchor" select="@css:anchor"/>
            <xsl:variable name="anchor" as="element()" select="collection()[1]//*[@css:id=$anchor]"/>
            <xsl:if test="($preceding-consumer
                           and $anchor
                               intersect $this/(preceding::*|ancestor::*)
                               intersect $preceding-consumer/following::*)
                          or (not($preceding-consumer)
                              and $anchor
                                  intersect $this/(preceding::*|ancestor::*))">
                <xsl:sequence select="."/>
            </xsl:if>
        </xsl:for-each>

    </xsl:template>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
