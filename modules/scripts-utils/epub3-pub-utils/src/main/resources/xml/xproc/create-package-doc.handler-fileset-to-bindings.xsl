<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" exclude-result-prefixes="#all">
    <xsl:output indent="yes"/>
    <xsl:template match="/*">
        <bindings>
            <xsl:for-each select="*">
                <mediaType handler="{@handler}" media-type="{@media-type}"/>
            </xsl:for-each>
        </bindings>
    </xsl:template>
</xsl:stylesheet>
