<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all" version="2.0">

    <!--
        
        Serialization can't currently be done from XSLT as XProc discards the serialization information.
        XProc does not support character maps. However Calabash supports us-ascii.
        
    <xsl:output encoding="us-ascii" use-character-maps="character-map"/>
    <xsl:character-map name="character-map">
        <xsl:output-character character="&apos;" string="&amp;#x27;"/>
        <xsl:output-character character="&quot;" string="&amp;#x22;"/>
        <xsl:output-character character="&amp;" string="&amp;#x26;"/>
        <xsl:output-character character="&lt;" string="&amp;#x3c;"/>
        <xsl:output-character character="&gt;" string="&amp;#x3e;"/>
        <xsl:output-character character="-" string="&amp;#x2d;"/>
    </xsl:character-map>
    -->

    <xsl:param name="preserve-empty-whitespace" select="'true'"/>

    <xsl:template match="@*|node()">
        <xsl:choose>
            <xsl:when test="ancestor::*[matches(local-name(),'^h[d\d]$')]">
                <xsl:copy exclude-result-prefixes="#all">
                    <xsl:apply-templates select="@*|node()"/>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="self::* and (parent::*/text() or $preserve-empty-whitespace='false') and normalize-space(string-join(parent::*/text(),''))=''">
                    <xsl:text>
</xsl:text>
                    <xsl:for-each select="1 to count(ancestor::*)">
                        <xsl:text>    </xsl:text>
                    </xsl:for-each>
                </xsl:if>
                <xsl:copy exclude-result-prefixes="#all">
                    <xsl:apply-templates select="@*|node()"/>
                    <xsl:if test="not(self::*[matches(local-name(),'^h[d\d]$')]) and self::* and * and (text() or $preserve-empty-whitespace='false') and normalize-space(string-join(text(),''))=''">
                        <xsl:text>
</xsl:text>
                        <xsl:for-each select="1 to count(ancestor::*)">
                            <xsl:text>    </xsl:text>
                        </xsl:for-each>
                    </xsl:if>
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="/*">
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:apply-templates select="@*|node()"/>
            <xsl:if test="normalize-space(string-join(text(),''))=''">
                <xsl:text>
</xsl:text>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="text()[not(ancestor::*[matches(local-name(),'^h[d\d]$')]) and not(.='') and normalize-space(.)='']">
        <xsl:text> </xsl:text>
    </xsl:template>

</xsl:stylesheet>
