<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:x="http://www.daisy.org/ns/xprocspec">

    <xsl:output indent="yes"/>

    <xsl:template match="/">
        <xsl:variable name="namespaces-in-use" select="distinct-values(//namespace::*/name())"/>
        <xsl:variable name="xsl-prefix" select="('xsl', for $i in (1 to count($namespaces-in-use)+1) return concat('xsl',$i))"/>
        <xsl:variable name="xsl-prefix" select="for $prefix in ($xsl-prefix) return (if ($prefix = $namespaces-in-use) then () else $prefix)"/>
        <xsl:variable name="xsl-prefix" select="$xsl-prefix[1]"/>

        <xsl:element name="{$xsl-prefix}:stylesheet" namespace="http://www.w3.org/1999/XSL/Transform">
            <xsl:attribute name="version" select="'2.0'"/>
            <xsl:attribute name="xpath-default-namespace" select="namespace-uri-for-prefix('',/*)"/>

            <xsl:copy-of select="//namespace::*"/>

            <xsl:element name="{$xsl-prefix}:output" namespace="http://www.w3.org/1999/XSL/Transform">
                <xsl:attribute name="indent" select="'yes'"/>
            </xsl:element>

            <xsl:element name="{$xsl-prefix}:template" namespace="http://www.w3.org/1999/XSL/Transform">
                <xsl:attribute name="match" select="'/'"/>
                <x:document>
                    <xsl:element name="{$xsl-prefix}:copy-of" namespace="http://www.w3.org/1999/XSL/Transform">
                        <xsl:attribute name="select" select="if (/*/@select) then /*/@select else '/*'"/>
                    </xsl:element>
                </x:document>
            </xsl:element>

        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
