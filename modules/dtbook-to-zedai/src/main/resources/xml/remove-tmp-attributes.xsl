<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="2.0" xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp" exclude-result-prefixes="#all">
    
    <xsl:output indent="yes" method="xml" />
    
    
    
    <!-- discard tmp: attributes, which were used to hold css data prior to extraction -->
    <xsl:template match="@tmp:*"/>
    
    <!-- identity template -->
    <xsl:template match="@*|node()">
        <xsl:copy  copy-namespaces="no">
            <xsl:for-each select="namespace::* except namespace::tmp">
                <xsl:namespace name="{name(.)}"
                    select="string(.)"/>
            </xsl:for-each>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
