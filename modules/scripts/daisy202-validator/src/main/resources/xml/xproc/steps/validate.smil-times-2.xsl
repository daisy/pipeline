<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns="" xpath-default-namespace=""
                exclude-result-prefixes="#all">

    <xsl:import href="http://www.daisy.org/pipeline/modules/smil-utils/clock-functions.xsl"/>

    <xsl:param name="ncc-totalTime"/>

    <xsl:template match="/*">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="calculated-totalTime" select="sum(*[not(@is-master)]/number(@calculated-duration))"/>
            <xsl:attribute name="ncc-meta-totalTime" select="pf:smil-clock-value-to-seconds($ncc-totalTime)"/>
            <xsl:for-each select="*">
                <xsl:copy>
                    <xsl:copy-of select="@*"/>
                    <xsl:attribute name="calculated-totalTime"
                                   select="sum(preceding-sibling::*[not(@is-master)]/number(@calculated-duration))"/>
                    <xsl:copy-of select="node()"/>
                </xsl:copy>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
