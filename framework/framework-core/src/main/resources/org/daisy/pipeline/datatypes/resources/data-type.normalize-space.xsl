<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:a="http://relaxng.org/ns/compatibility/annotations/1.0">

    <!--
        normalize space inside documentation elements
    -->
    <xsl:template match="documentation[not(@xml:space='preserve')]|
                         a:documentation[not(@xml:space='preserve')]">
        <xsl:copy>
            <xsl:value-of select="normalize-space(string(.))"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
