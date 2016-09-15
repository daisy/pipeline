<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet exclude-result-prefixes="#all" version="2.0" xmlns:mo="http://www.w3.org/ns/SMIL"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:param name="src" required="yes"/>
    <xsl:param name="fragment" required="yes"/>
    <xsl:template match="/*">
        <xsl:for-each select="(//*[@fragment=$fragment and @src=$src]/parent::*/child::audio)[1]"
            xpath-default-namespace="http://www.w3.org/ns/SMIL">
            <audio xmlns="http://www.w3.org/ns/SMIL" clipBegin="{@clipBegin}" clipEnd="{@clipEnd}"
                src="{resolve-uri(@src,base-uri(.))}"/>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
