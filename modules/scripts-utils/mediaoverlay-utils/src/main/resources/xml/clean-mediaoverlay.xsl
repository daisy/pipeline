<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xpath-default-namespace="http://www.w3.org/ns/SMIL" exclude-result-prefixes="#all" version="2.0">
    
    <xsl:output indent="yes"/>
    
    <xsl:template match="seq[empty(descendant::par)]"/>
    
    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>
    

</xsl:stylesheet>
