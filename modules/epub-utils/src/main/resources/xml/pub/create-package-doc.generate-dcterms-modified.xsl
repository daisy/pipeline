<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.idpf.org/2007/opf" xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xpath-default-namespace="http://www.idpf.org/2007/opf" version="2.0"
    exclude-result-prefixes="#all">

    <!--generate a new dcterms:modified after the identifier-->
    <xsl:template match="dc:identifier">
        <xsl:copy-of select="."/>
        <meta property="dcterms:modified">
            <xsl:value-of
                select="format-dateTime(
                adjust-dateTime-to-timezone(current-dateTime(),xs:dayTimeDuration('PT0H')),
                '[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01][Z]')"
            />
        </meta>
    </xsl:template>

    <!--discard any existing date-->
    <xsl:template match="meta[@property='dcterms:modified']"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
