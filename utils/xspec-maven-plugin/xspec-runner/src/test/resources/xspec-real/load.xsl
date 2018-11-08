<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema">
    
    <xsl:template name="load">
        <xsl:param name="href" as="xs:anyURI"/>
        <xsl:sequence select="doc($href)"/>
    </xsl:template>
    
</xsl:stylesheet>
