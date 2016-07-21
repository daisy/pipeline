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
    
    <xsl:template match="*[matches(string(@style), '::before|::after')]">
        <xsl:variable name="rules" as="element()*" select="css:parse-stylesheet(@style)"/>
        <xsl:variable name="before-style" as="xs:string?" select="$rules[@selector='::before']/@style"/>
        <xsl:variable name="after-style" as="xs:string?" select="$rules[@selector='::after']/@style"/>
        <xsl:choose>
            <xsl:when test="$before-style or $after-style">
                <xsl:copy>
                    <xsl:sequence select="@*[not(name()='style')]"/>
                    <xsl:sequence select="css:style-attribute(css:serialize-stylesheet($rules[not(@selector=('::before','::after'))]))"/>
                    <xsl:if test="$before-style">
                        <xsl:element name="css:before">
                            <xsl:attribute name="style" select="$before-style"/>
                        </xsl:element>
                    </xsl:if>
                    <xsl:apply-templates select="node()"/>
                    <xsl:if test="$after-style">
                        <xsl:element name="css:after">
                            <xsl:attribute name="style" select="$after-style"/>
                        </xsl:element>
                    </xsl:if>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
</xsl:stylesheet>
