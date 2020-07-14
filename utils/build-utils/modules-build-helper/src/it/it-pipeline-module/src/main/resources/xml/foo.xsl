<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                version="2.0">
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="text()">
        <xsl:value-of select="pf:java-function(.)"/>
    </xsl:template>
    
</xsl:stylesheet>
