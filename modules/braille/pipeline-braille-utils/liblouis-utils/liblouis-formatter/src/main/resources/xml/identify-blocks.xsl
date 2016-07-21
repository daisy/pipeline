<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:xs="http://www.w3.org/2001/XMLSchema"
        xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
        xmlns:louis="http://liblouis.org/liblouis">
    
    <xsl:template match="*">
        <xsl:variable name="this" as="element()" select="."/>
        <xsl:copy>
            <xsl:sequence select="@*"/>
            <xsl:for-each-group select="*|text()"
                                group-adjacent="boolean(descendant-or-self::*[@css:display or @css:page or @louis:braille-page-reset])">
                <xsl:choose>
                    <xsl:when test="current-grouping-key()">
                        <xsl:for-each select="current-group()">
                            <xsl:apply-templates select="."/>
                        </xsl:for-each>
                    </xsl:when>
                    <xsl:when test="matches(string-join(current-group()/string(.), ''), '^[\s&#x2800;]*$')
                                    and not(current-group()/descendant-or-self::louis:space or
                                            current-group()/descendant-or-self::css:string or
                                            current-group()/descendant-or-self::css:counter or
                                            current-group()/descendant-or-self::css:text or
                                            current-group()/descendant-or-self::css:leader)">
                        <xsl:sequence select="current-group()"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:element name="css:block">
                            <xsl:sequence select="current-group()"/>
                        </xsl:element>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each-group>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="text()|louis:space">
        <xsl:sequence select="."/>
    </xsl:template>
    
</xsl:stylesheet>
