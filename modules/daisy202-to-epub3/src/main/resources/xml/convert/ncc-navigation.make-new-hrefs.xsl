<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:pf="http://www.daisy.org/ns/pipeline/functions">
    <xsl:param name="uri" required="yes"/>
    <xsl:param name="base" required="yes"/>
    
    <xsl:include href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
    
    <xsl:template match="/*">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="href" select="replace(pf:relativize-uri($uri,$base),'^ncc.xhtml','')"/>
            <xsl:copy-of select="node()"/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
