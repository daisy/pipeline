<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:html="http://www.w3.org/1999/xhtml" exclude-result-prefixes="#all" xmlns:xs="http://www.w3.org/2001/XMLSchema">

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="html:h1 | html:h2 | html:h3 | html:h4 | html:h5 | html:h6 | html:span[matches(@class,'(^|\s)page-(front|normal|special)(\s|$)')]">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:if test="not(@id)">
                <xsl:attribute name="id" select="generate-id(.)"/>
            </xsl:if>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
