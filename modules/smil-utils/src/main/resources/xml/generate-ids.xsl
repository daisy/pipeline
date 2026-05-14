<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xpath-default-namespace="http://www.w3.org/ns/SMIL"
    exclude-result-prefixes="#all" version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
    <xsl:param name="iteration-position" required="yes"/>
    
    <xsl:template match="@*|node()" mode="#all">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="body">
        <xsl:apply-templates select="." mode="add-id"/>
    </xsl:template>
    
    <xsl:template match="*" mode="add-id" priority="10">
        <xsl:param name="id"/>
        <xsl:variable name="id" select="
            if (self::body) then concat('mo',$iteration-position)
            else concat($id,
                if (self::seq and ancestor::seq) then '-'
                else concat('_',local-name()),
                if (self::text or self::audio) then '' else (count(preceding-sibling::*)+1))
            "></xsl:variable>
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:attribute name="id" select="$id"/>
            <xsl:apply-templates mode="add-id">
                <xsl:with-param name="id" select="$id"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
