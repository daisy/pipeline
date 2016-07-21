<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
    xmlns:louis="http://liblouis.org/liblouis"
    exclude-result-prefixes="#all"
    version="2.0">
    
    <!--
        css-utils [2.0.0,3.0.0)
    -->
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[@css:display or contains(string(@style), 'display')]">
        <xsl:variable name="properties"
            select="css:specified-properties('#all display', true(), true(), true(), .)"/>
        <xsl:variable name="display" as="xs:string" select="$properties[@name='display']/@value"/>
        <xsl:choose>
            <xsl:when test="$display=('none','page-break')">
                <xsl:sequence select=".//louis:print-page|
                                      .//louis:running-header|
                                      .//louis:running-footer"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy>
                    <xsl:sequence select="@* except (@style|@css:display)"/>
                    <xsl:sequence select="css:style-attribute(css:serialize-declaration-list(
                                            $properties[not(@name='display')]))"/>
                    <xsl:if test="$display=('block','list-item')">
                        <xsl:attribute name="css:display" select="'block'"/>
                    </xsl:if>
                    <xsl:apply-templates select="node()"/>
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
</xsl:stylesheet>
