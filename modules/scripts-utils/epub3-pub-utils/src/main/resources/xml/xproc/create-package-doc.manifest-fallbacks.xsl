<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:d="http://www.daisy.org/ns/pipeline/data" exclude-result-prefixes="#all">
    <xsl:output indent="yes"/>

    <xsl:template match="/*">
        <d:fileset>
            <xsl:attribute name="xml:base" select="base-uri(/*/d:fileset[1])"/>
            <xsl:for-each select="d:fileset[1]/d:file">
                <xsl:copy>
                    <xsl:copy-of select="@*"/>
                    <xsl:if test="/*/d:fileset[position() &gt; 1]/d:file[position() &lt; last()]/@href=@href">
                        <xsl:variable name="href" select="@href"/>
                        <xsl:attribute name="fallback" select="/*/d:fileset[1]/d:file[@href=/*/d:fileset[position() &gt; 1]/d:file[@href=$href]/following-sibling::*[1]/@href]/@id"/>
                    </xsl:if>
                </xsl:copy>
            </xsl:for-each>
        </d:fileset>
    </xsl:template>

</xsl:stylesheet>
