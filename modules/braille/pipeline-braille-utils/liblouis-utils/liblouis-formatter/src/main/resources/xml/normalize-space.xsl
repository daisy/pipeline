<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:louis="http://liblouis.org/liblouis">
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="louis:semantics|louis:styles|louis:page-layout">
        <xsl:sequence select="."/>
    </xsl:template>
    
    <!--
        flatten elements that are referenced in a toc-item
    -->
    <xsl:template match="*[@css:target]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:value-of select="string(.)"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="louis:space">
        <!--
            NOTE: liblouisutdml trims leading/trailing spaces
        -->
        <xsl:analyze-string select="string(.)" regex="\n">
            <xsl:matching-substring>
                <!--
                    (hack) because liblouisutdml doesn't want to start with a line-break
                -->
                <xsl:if test="position()=1">
                    <xsl:value-of select="'&#xA0;'"/>
                </xsl:if>
                <xsl:element name="louis:line-break"/>
            </xsl:matching-substring>
            <xsl:non-matching-substring>
                <xsl:value-of select="replace(., '^[\s&#x2800;]|[\s&#x2800;]$', '&#xA0;')"/>
            </xsl:non-matching-substring>
        </xsl:analyze-string>
    </xsl:template>
    
    <xsl:template match="text()">
        <xsl:choose>
            <xsl:when test="ancestor::css:block or
                            parent::louis:print-page or
                            parent::louis:running-header or
                            parent::louis:running-footer">
                <!--
                    squeeze text
                -->
                <xsl:value-of select="pxi:squeeze(string(.))"/>
            </xsl:when>
            <xsl:otherwise>
                <!--
                    if text is only whitespace for indentation, leave it
                -->
                <xsl:sequence select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:function name="pxi:squeeze" as="xs:string">
        <xsl:param name="string" as="xs:string"/>
        <xsl:sequence select="replace($string, '[\s&#x2800;]+', ' ')"/>
    </xsl:function>
    
</xsl:stylesheet>
