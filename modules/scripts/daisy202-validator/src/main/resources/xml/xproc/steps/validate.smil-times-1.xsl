<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all" version="2.0"
    xmlns:pf="http://www.daisy.org/ns/pipeline/functions" xmlns:f="http://www.daisy.org/ns/pipeline/internal-function/daisy202-validator" xmlns:c="http://www.w3.org/ns/xproc-step"
    xpath-default-namespace="">

    <xsl:import href="http://www.daisy.org/pipeline/modules/mediaoverlay-utils/clock-functions.xsl"/>

    <xsl:template match="/*">
        <c:result>
            <xsl:apply-templates select="*"/>
        </c:result>
    </xsl:template>

    <!-- matching *:smil instead of smil to avoid namespace warning -->
    <xsl:template match="*:smil">
        <smil xml:base="{base-uri(.)}">
            <xsl:variable name="meta-duration" select="head/meta[@name='ncc:timeInThisSmil']/string(@content)"/>
            <xsl:variable name="meta-duration" select="if ($meta-duration) then pf:mediaoverlay-clock-value-to-seconds($meta-duration) else 0"/>
            <!-- NOTE: this assumes that if there are no clip-end attribute, only a clip-begin attribute, then the declared SMIL duration is the total duration of the audio file pointed to by that audio element -->
            <xsl:attribute name="calculated-duration"
                select="sum(for $audio in (body//audio[@clip-begin or @clip-end]) return (($audio[@clip-end]/pf:mediaoverlay-clock-value-to-seconds(@clip-end),$meta-duration)[1] - ($audio[@clip-begin]/pf:mediaoverlay-clock-value-to-seconds(@clip-begin),0)[1]))"/>
            <xsl:for-each select="body//audio[@clip-begin and not(@clip-end) or not(@clip-begin) and @clip-end]">
                <xsl:message select="concat('audio clip is missing a ',(if (@clip-begin) then 'clip-end' else 'clip-begin'),' attribute (par id: ',parent::par/@id,')')"/>
            </xsl:for-each>
            <xsl:attribute name="meta-duration" select="$meta-duration"/>
            <xsl:variable name="meta-totalTime" select="head/meta[@name='ncc:totalElapsedTime']/string(@content)"/>
            <xsl:attribute name="meta-totalTime" select="if ($meta-totalTime) then pf:mediaoverlay-clock-value-to-seconds($meta-totalTime) else 0"/>
        </smil>
    </xsl:template>

</xsl:stylesheet>
