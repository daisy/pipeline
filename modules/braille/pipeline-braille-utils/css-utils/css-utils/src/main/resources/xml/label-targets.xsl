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
    
    <xsl:template match="*[@xml:id or @id]">
        <xsl:variable name="name" as="xs:string*" select="(@xml:id|@id)"/>
        <xsl:choose>
            <xsl:when test="//*[self::css:text[@target] or
                                self::css:string[@name][@target] or
                                self::css:counter[@target] or
                                self::css:content[@target]]
                               [replace(@target,'^#','')=$name]">
                <xsl:copy>
                    <xsl:apply-templates select="@*"/>
                    <xsl:if test="not(@css:id)">
                        <xsl:attribute name="css:_id_" select="generate-id(.)"/>
                    </xsl:if>
                    <xsl:apply-templates select="node()"/>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="css:text[@target]|
                         css:string[@name][@target]|
                         css:counter[@target]|
                         css:content[@target]">
        <xsl:variable name="name" as="xs:string" select="replace(@target,'^#','')"/>
        <xsl:variable name="target" as="element()?" select="//*[@xml:id=$name or @id=$name][1]"/>
        <xsl:if test="$target">
            <xsl:copy>
                <xsl:sequence select="@* except @target"/>
                <xsl:attribute name="target" select="($target/@css:id,generate-id($target))[1]"/>
                <xsl:sequence select="node()"/>
            </xsl:copy>
        </xsl:if>
    </xsl:template>
    
</xsl:stylesheet>
