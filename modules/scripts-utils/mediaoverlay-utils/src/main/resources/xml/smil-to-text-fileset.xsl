<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:d="http://www.daisy.org/ns/pipeline/data" exclude-result-prefixes="#all" version="2.0">
    <xsl:template match="/*">
        <d:fileset xml:base="{replace(base-uri(.),'^(.+/)[^/]*$','$1')}">
            <xsl:for-each select="distinct-values(//*[local-name()='text']/@src/tokenize(.,'#')[1])">
                <d:file href="{.}"/>
            </xsl:for-each>
        </d:fileset>
    </xsl:template>
</xsl:stylesheet>
