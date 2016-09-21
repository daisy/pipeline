<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:f="http://www.example.org/functions"
    version="2.0">

    <xsl:function name="f:hello" as="xs:string">
        <xsl:param name="hello" as="xs:string"/>
        <xsl:sequence select="concat('Hello, ',$hello,'!')"/>
    </xsl:function>
    
</xsl:stylesheet>
