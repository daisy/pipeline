<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
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
    
    <xsl:template match="*[contains(string(@style), 'content')]">
        <xsl:variable name="properties" as="element()*" select="css:parse-declaration-list(@style)"/>
        <xsl:variable name="content-list" as="xs:string?" select="$properties[@name='content'][1]/@value"/>
        <xsl:choose>
            <xsl:when test="$content-list">
                <xsl:copy>
                    <xsl:sequence select="@*[not(name()='style')]"/>
                    <xsl:sequence select="css:style-attribute(css:serialize-declaration-list(
                                            $properties[not(@name='content')]))"/>
                    <xsl:variable name="context" select="if (self::css:before or self::css:after) then parent::* else ."/>
                    <xsl:apply-templates select="css:parse-content-list($content-list, $context)" mode="eval-content-list">
                        <xsl:with-param name="context" select="$context"/>
                    </xsl:apply-templates>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="css:string[@value]" mode="eval-content-list">
        <xsl:value-of select="string(@value)"/>
    </xsl:template>
    
    <xsl:template match="css:attr" mode="eval-content-list">
        <xsl:param name="context" as="element()"/>
        <xsl:variable name="name" select="string(@name)"/>
        <xsl:value-of select="string($context/@*[name()=$name])"/>
    </xsl:template>
    
    <xsl:template match="css:text[@target]|css:string[@name][@target]|css:counter[@target]|css:leader"
                  mode="eval-content-list">
        <xsl:sequence select="."/>
    </xsl:template>
    
</xsl:stylesheet>
