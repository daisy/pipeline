<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:f="http://www.example.org/functions" version="2.0">
    
    <xsl:import href="http://www.example.org/functions.xsl"/>
    <xsl:template match="hello">
        <hello><xsl:value-of select="f:hello('World')"/></hello>
    </xsl:template>
</xsl:stylesheet>
