<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns="http://www.w3.org/1999/xhtml" xpath-default-namespace="http://www.w3.org/1999/xhtml" exclude-result-prefixes="#all" xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:pf="http://www.daisy.org/ns/pipeline/functions">

    <xsl:import href="http://www.daisy.org/pipeline/modules/common-utils/i18n.xsl"/>
    <!--    <xsl:import href="../../../../test/xspec/mock/i18n.xsl"/>-->

    <xsl:variable name="partition-types" select="('cover','frontmatter','bodymatter','backmatter')"/>
    <xsl:variable name="division-types"
        select="('abstract','acknowledgments','afterword','answers','appendix','assessment','assessments','bibliography','z3998:biographical-note','case-study','chapter','colophon','conclusion','contributors','copyright-page','credits','dedication','z3998:discography','division','z3998:editorial-note','epigraph','epilogue','errata','z3998:filmography','footnotes','foreword','glossary','z3998:grant-acknowledgment','halftitlepage','imprimatur','imprint','index','index-group','index-headnotes','index-legend','introduction','keywords','landmarks','loa','loi','lot','lov','notice','other-credits','page-list','part','practices','preamble','preface','prologue','z3998:promotional-copy','z3998:published-works','z3998:publisher-address','qna','rearnotes','revision-history','z3998:section','seriespage','subchapter','z3998:subsection','titlepage','toc','toc-brief','z3998:translator-note','volume')"/>
    <xsl:variable name="translations" select="document('../i18n/translations.xml')/*"/>

    <xsl:template match="@*|node()">
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="body[f:types(.)='cover'] | section[f:types(.)='cover']">
        <xsl:call-template name="section-with-default-title">
            <xsl:with-param name="title" select="'Cover'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="body[f:types(.)='cover']/section[f:classes(.)='frontcover'] | section[f:types(.)='cover']/section[f:classes(.)='frontcover']">
        <xsl:call-template name="section-with-default-title">
            <xsl:with-param name="title" select="'Front Cover'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="body[f:types(.)='cover']/section[f:classes(.)='rearcover'] | section[f:types(.)='cover']/section[f:classes(.)='rearcover']">
        <xsl:call-template name="section-with-default-title">
            <xsl:with-param name="title" select="'Rear Cover'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="body[f:types(.)='cover']/section[f:classes(.)='leftflap'] | section[f:types(.)='cover']/section[f:classes(.)='leftflap']">
        <xsl:call-template name="section-with-default-title">
            <xsl:with-param name="title" select="'Left Flap'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="body[f:types(.)='cover']/section[f:classes(.)='rightflap'] | section[f:types(.)='cover']/section[f:classes(.)='rightflap']">
        <xsl:call-template name="section-with-default-title">
            <xsl:with-param name="title" select="'Right Flap'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="body[f:types(.)='part'] | section[f:types(.)='part']">
        <xsl:call-template name="section-with-default-title">
            <xsl:with-param name="title" select="'Untitled Part'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="body[f:types(.)='chapter'] | section[f:types(.)='chapter']">
        <xsl:call-template name="section-with-default-title">
            <xsl:with-param name="title" select="'Untitled Chapter'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="body[not(header) and not(h1 | h2 | h3 | h4 | h5 | h6) and not(f:types(.)=('cover','part','chapter')) and not(f:classes(.)=('frontcover','rearcover','leftflap','rightflap'))]">
        <xsl:call-template name="section-with-default-title">
            <xsl:with-param name="title" select="'Untitled Section'"/>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="section[not(h1 | h2 | h3 | h4 | h5 | h6) and not(f:types(.)=('cover','part','chapter')) and not(f:classes(.)=('frontcover','rearcover','leftflap','rightflap'))]">
        <xsl:call-template name="section-with-default-title">
            <xsl:with-param name="title" select="'Untitled Section'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="section-with-default-title">
        <xsl:param name="title" required="yes"/>
        <xsl:message select="$title"/>
        <xsl:message select="('en',ancestor-or-self::*[@xml:lang|@lang]/(@xml:lang|@lang)[1])[last()]"/>
        <xsl:variable name="translated-title" select="pf:i18n-translate($title,('en',ancestor-or-self::*[@xml:lang|@lang]/(@xml:lang|@lang)[1])[last()],$translations)"/>
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:apply-templates select="@*"/>
            <xsl:if test="not(h1 | h2 | h3 | h4 | h5 | h6)">
                <!-- sectioning element is missing headline; let's generate one! -->
                <xsl:variable name="level" select="count(ancestor::body | ancestor::section | ancestor::article | ancestor::nav | ancestor::aside) + 1 + (if (ancestor::body/header) then -1 else 0)"/>
                <!--                <xsl:variable name="level" select="min((6, count(ancestor::section | ancestor::article | ancestor::nav | ancestor::aside) + 1)) + (if (ancestor::body/header) then 0 else 1)"/>-->
                <xsl:element name="h{$level}">
                    <xsl:value-of select="$translated-title"/>
                </xsl:element>
            </xsl:if>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:function name="f:types" as="xs:string*">
        <xsl:param name="element" as="element()"/>
        <xsl:sequence select="tokenize($element/@epub:type,'\s+')"/>
    </xsl:function>

    <xsl:function name="f:classes" as="xs:string*">
        <xsl:param name="element" as="element()"/>
        <xsl:sequence select="tokenize($element/@class,'\s+')"/>
    </xsl:function>

</xsl:stylesheet>
