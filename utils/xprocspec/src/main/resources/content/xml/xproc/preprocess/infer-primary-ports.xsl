<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" exclude-result-prefixes="#all" xmlns:p="http://www.w3.org/ns/xproc">
    <xsl:template match="/p:declare-step">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:for-each select="p:input">
                <xsl:variable name="kind" select="@kind"/>
                <xsl:copy>
                    <xsl:copy-of select="@*"/>
                    <xsl:if test="not(@primary)">
                        <xsl:choose>
                            <xsl:when test="count(/*/p:input[@kind=$kind])=1">
                                <xsl:attribute name="primary" select="'true'"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:attribute name="primary" select="'false'"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>
                    <xsl:copy-of select="*"/>
                </xsl:copy>
            </xsl:for-each>
            <xsl:for-each select="p:output">
                <xsl:variable name="kind" select="@kind"/>
                <xsl:copy>
                    <xsl:copy-of select="@*"/>
                    <xsl:if test="not(@primary)">
                        <xsl:choose>
                            <xsl:when test="count(/*/p:output[@kind=$kind])=1">
                                <xsl:attribute name="primary" select="'true'"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:attribute name="primary" select="'false'"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>
                    <xsl:copy-of select="*"/>
                </xsl:copy>
            </xsl:for-each>
            <xsl:copy-of select="*[not(self::p:input) and not(self::p:output)]"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
