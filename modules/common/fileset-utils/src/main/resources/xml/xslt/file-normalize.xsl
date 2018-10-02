<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pf="http://www.daisy.org/ns/pipeline/functions" version="2.0" exclude-result-prefixes="#all"
    xmlns:d="http://www.daisy.org/ns/pipeline/data">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

    <xsl:template match="/*">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:attribute name="href" select="if (@xml:base or not(matches(@href,'^\w+:'))) then pf:relativize-uri(resolve-uri(@href,@xml:base),@xml:base) else pf:normalize-uri(@href)"/>
            <xsl:if test="@original-href">
                <xsl:attribute name="original-href" select="if (@xml:base) then pf:normalize-uri(resolve-uri(@original-href,@xml:base)) else pf:normalize-uri(@original-href)"/>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
