<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" version="1.0">
    
    <xsl:output method="text"/>
    
    <xsl:template match="/*">
        <xsl:for-each select="/metadata/versioning/versions/version">
            <xsl:value-of select="normalize-space(.)"/>
            <xsl:text>
            </xsl:text>
        </xsl:for-each>
    </xsl:template>
    
</xsl:stylesheet>
