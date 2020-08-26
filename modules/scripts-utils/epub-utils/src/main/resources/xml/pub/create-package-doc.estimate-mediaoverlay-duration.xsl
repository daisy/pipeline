<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" exclude-result-prefixes="#all" xmlns:mo="http://www.w3.org/ns/SMIL" xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
    xmlns:opf="http://www.idpf.org/2007/opf" xmlns="http://www.idpf.org/2007/opf">
    <xsl:include href="http://www.daisy.org/pipeline/modules/smil-utils/clock-functions.xsl"/>
    <xsl:template match="/*">
        <meta property="media:duration">
            <xsl:value-of
                select="
                pf:mediaoverlay-seconds-to-full-clock-value(round-half-to-even(
                        sum(//mo:audio[@clipEnd]/pf:mediaoverlay-clock-value-to-seconds(@clipEnd))
                    -   sum(//mo:audio[@clipEnd and @clipBegin]/pf:mediaoverlay-clock-value-to-seconds(@clipBegin))
                , 4))
                "
            />
        </meta>
    </xsl:template>
</xsl:stylesheet>
