<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all" version="2.0"
    xmlns:pf="http://www.daisy.org/ns/pipeline/functions" xmlns:f="http://www.daisy.org/ns/pipeline/internal-function/daisy202-validator" xmlns:c="http://www.w3.org/ns/xproc-step"
    xpath-default-namespace="">

    <xsl:import href="http://www.daisy.org/pipeline/modules/mediaoverlay-utils/clock-functions.xsl"/>

    <xsl:param name="ncc-totalTime"/>

    <xsl:template match="/*">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="calculated-totalTime" select="sum(*/number(@calculated-duration))"/>
            <xsl:attribute name="ncc-meta-totalTime" select="pf:mediaoverlay-clock-value-to-seconds($ncc-totalTime)"/>
            <xsl:for-each select="*">
                <xsl:copy>
                    <xsl:copy-of select="@*"/>
                    <xsl:attribute name="calculated-totalTime" select="sum(preceding::*/number(@calculated-duration))"/>
                    <xsl:copy-of select="node()"/>
                </xsl:copy>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
