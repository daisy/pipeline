<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" exclude-result-prefixes="#all">
    <xsl:output indent="yes"/>
    <xsl:template match="/*">
        <bindings> <!-- TODO : bindings is deprecated from 3.2 and may be removed in futur specs -->
            <xsl:for-each select="*">
                <xsl:message>[WARNING] Bindings are deprecated and should not be used anymore. Please use HTML <object/> content fallback instead for <xsl:value-of select="@media-type"/>.</xsl:message>
                <mediaType handler="{@handler}" media-type="{@media-type}"/>
            </xsl:for-each>
        </bindings>
    </xsl:template>
</xsl:stylesheet>
