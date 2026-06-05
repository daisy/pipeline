<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xpath-default-namespace="http://www.daisy.org/z3986/2005/dtbook/"
                exclude-result-prefixes="#all">

    <xsl:import href="http://www.daisy.org/pipeline/modules/common-utils/i18n.xsl"/>

    <!--
        Normalize lang attributes
    -->

    <xsl:template match="@xml:lang">
        <xsl:attribute name="xml:lang" select="pf:normalize-lang(.)"/>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
