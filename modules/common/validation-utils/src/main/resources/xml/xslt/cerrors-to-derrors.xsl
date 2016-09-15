<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:svrl="http://purl.oclc.org/dsdl/svrl" 
    xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:d="http://www.daisy.org/ns/pipeline/data" 
    exclude-result-prefixes="#all">
    <xsl:output xml:space="default" indent="yes"/>
    
    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>
    
    <xsl:template match="c:errors">
        <d:errors>
            <xsl:apply-templates/>
        </d:errors>
    </xsl:template>
    
    <xsl:template match="c:error">
        <d:error>
            <d:desc><xsl:value-of select="text()"/></d:desc>
            <d:location>
                <xsl:attribute name="line"><xsl:value-of select="@line"/></xsl:attribute>
                <xsl:attribute name="column"><xsl:value-of select="@column"/></xsl:attribute>
            </d:location>
        </d:error>
    </xsl:template>
    
</xsl:stylesheet>
