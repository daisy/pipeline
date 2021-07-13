<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:s="http://www.w3.org/ns/SMIL"
                exclude-result-prefixes="#all">

    <!--
        FIXME:
          - update metadata according to SMIL 1.0
    -->

    <xsl:include href="clock-functions.xsl"/>
    <xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/generate-id.xsl"/>

    <xsl:template match="/">
        <xsl:variable name="smil" as="document-node()">
            <xsl:document>
                <xsl:apply-templates select="/*"/>
            </xsl:document>
        </xsl:variable>
        <xsl:apply-templates mode="dur" select="$smil/*">
            <xsl:with-param name="total-time" tunnel="yes" select="pf:smil-total-seconds($smil/*)"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="/*" priority="1">
        <xsl:call-template name="pf:next-match-with-generated-ids">
            <xsl:with-param name="prefix" select="'id_'"/>
            <xsl:with-param name="for-elements"
                            select="//s:text[not(@id)]|
                                    //s:audio[not(@id)]"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="/s:smil">
        <xsl:element name="{local-name()}" namespace="">
            <xsl:element name="head" namespace="">
                <xsl:apply-templates select="s:head/s:meta[not(starts-with(@name,'dtb:'))]"/>
                <xsl:element name="layout" namespace="">
                    <xsl:element name="region" namespace="">
                        <xsl:attribute name="id" select="'txtView'"/>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
            <xsl:apply-templates select="s:body"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@*|node()[not(self::*)]">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="*">
        <xsl:element name="{local-name()}" namespace="">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="*[not(namespace-uri()='http://www.w3.org/ns/SMIL')]"/>
    <xsl:template match="@*[not(namespace-uri()=('','http://www.w3.org/ns/SMIL'))]"/>

    <xsl:template match="@clipBegin">
        <xsl:attribute name="clip-begin" select="concat(
                                                   'npt=',
                                                   pf:smil-seconds-to-timecount(
                                                     pf:smil-clock-value-to-seconds(.),
                                                     's'))"/>
    </xsl:template>

    <xsl:template match="@clipEnd">
        <xsl:attribute name="clip-end" select="concat(
                                                 'npt=',
                                                 pf:smil-seconds-to-timecount(
                                                   pf:smil-clock-value-to-seconds(.),
                                                   's'))"/>
    </xsl:template>

    <xsl:template match="@systemRequired">
        <xsl:attribute name="system-required" select="string(.)"/>
    </xsl:template>

    <xsl:template match="/*/@version"/>

    <!-- ensure single top-level seq -->
    <xsl:template match="/*/s:body">
        <xsl:element name="{local-name()}" namespace="">
            <xsl:apply-templates select="@*"/>
            <xsl:element name="seq" namespace="">
                <xsl:attribute name="dur" select="''"/> <!-- will be filled later -->
                <xsl:apply-templates/>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <!-- unwrap seq -->
    <xsl:template match="s:seq">
        <xsl:apply-templates/>
    </xsl:template>

    <!-- endsync is required in DAISY 2.02 -->
    <xsl:template match="s:par[not(@endsync)]">
        <xsl:element name="{local-name()}" namespace="">
            <xsl:attribute name="endsync" select="'last'"/>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
    </xsl:template>

    <!-- id attribute is required on text/audio in DAISY 2.02 -->
    <xsl:template match="s:text[not(@id)]|s:audio[not(@id)]">
        <xsl:element name="{local-name()}" namespace="">
            <xsl:call-template name="pf:generate-id"/>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template mode="dur" match="/smil/body/seq/@dur">
        <xsl:param name="total-time" as="xs:double" tunnel="yes" required="yes"/>
        <xsl:attribute name="{name()}" select="pf:smil-seconds-to-timecount($total-time,'s')"/>
    </xsl:template>

    <xsl:template mode="dur" match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
