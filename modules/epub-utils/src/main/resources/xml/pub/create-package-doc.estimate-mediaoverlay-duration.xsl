<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:mo="http://www.w3.org/ns/SMIL"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns="http://www.idpf.org/2007/opf"
                exclude-result-prefixes="#all">
    <xsl:include href="http://www.daisy.org/pipeline/modules/smil-utils/clock-functions.xsl"/>
    <xsl:template match="/*">
        <meta property="media:duration">
            <xsl:value-of
                select="pf:smil-seconds-to-full-clock-value(round-half-to-even(pf:smil-total-seconds(/*), 4))"
            />
        </meta>
    </xsl:template>
</xsl:stylesheet>
