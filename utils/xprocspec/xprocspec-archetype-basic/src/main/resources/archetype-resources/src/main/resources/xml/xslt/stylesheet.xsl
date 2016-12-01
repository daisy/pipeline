<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns="http://www.w3.org/1999/xhtml" xpath-default-namespace="http://www.w3.org/1999/xhtml">

    <xsl:param name="name" required="yes"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="title | h1">
        <xsl:copy>
            <xsl:value-of select="concat('Hello, ',$name,'!')"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
