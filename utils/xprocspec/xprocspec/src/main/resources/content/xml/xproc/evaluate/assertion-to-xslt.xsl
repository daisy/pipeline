<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:x="http://www.daisy.org/ns/xprocspec" xmlns:c="http://www.w3.org/ns/xproc-step">

    <xsl:output indent="yes"/>

    <xsl:template match="/x:expect">
        <xsl:variable name="test" select="@test"/>
        <xsl:variable name="equals" select="@equals"/>
        <xsl:element name="xsl:stylesheet">
            <xsl:copy-of select="namespace::*"/>
            <xsl:attribute name="version" select="'2.0'"/>
            <xsl:element name="xsl:param">
                <xsl:attribute name="name" select="'temp-dir'"/>
                <xsl:attribute name="required" select="'yes'"/>
            </xsl:element>
            <xsl:element name="xsl:param">
                <xsl:attribute name="name" select="'test-base-uri'"/>
                <xsl:attribute name="required" select="'yes'"/>
            </xsl:element>
            <xsl:element name="xsl:template">
                <xsl:attribute name="match" select="'/'"/>
                <c:result>
                    <xsl:element name="xsl:choose">
                        <xsl:element name="xsl:when">
                            <xsl:choose>
                                <xsl:when test="$equals">
                                    <xsl:attribute name="test" select="concat('(',$test,') = (',$equals,')')"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="test" select="$test"/>
                                </xsl:otherwise>
                            </xsl:choose>
                            <xsl:element name="xsl:attribute">
                                <xsl:attribute name="name" select="'result'"/>
                                <xsl:attribute name="select" select="&quot;&apos;success&apos;&quot;"/>
                            </xsl:element>
                        </xsl:element>
                        <xsl:element name="xsl:otherwise">
                            <xsl:element name="xsl:attribute">
                                <xsl:attribute name="name" select="'result'"/>
                                <xsl:attribute name="select" select="&quot;&apos;failed&apos;&quot;"/>
                            </xsl:element>
                        </xsl:element>
                    </xsl:element>
                    <xsl:element name="xsl:attribute">
                        <xsl:attribute name="name" select="'test'"/>
                        <xsl:attribute name="select" select="$test"/>
                    </xsl:element>
                    <xsl:element name="xsl:attribute">
                        <xsl:attribute name="name" select="'equals'"/>
                        <xsl:attribute name="select" select="($equals,'true()')[1]"/>
                    </xsl:element>
                </c:result>
            </xsl:element>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
