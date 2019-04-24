<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:opf="http://www.idpf.org/2007/opf"
                xmlns="http://www.w3.org/1999/xhtml"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xpath-default-namespace=""
                exclude-result-prefixes="#all">

    <!-- <xsl:param name="modified" select="format-dateTime( -->
    <!--     adjust-dateTime-to-timezone(current-dateTime(),xs:dayTimeDuration('PT0H')), -->
    <!--     '[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01][Z]')"/> -->

    <xsl:import href="http://www.daisy.org/pipeline/modules/mediaoverlay-utils/clock-functions.xsl"/>

    <xsl:variable name="ncc.body" select="collection()[position()=2]/*"/>
    <xsl:variable name="smil" select="collection()[position()&gt;2]/*"/>

    <xsl:template match="text()"/>

    <xsl:template match="opf:metadata">
        <head>

            <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>

            <title>
                <xsl:for-each select="dc:title[1]">
                    <xsl:copy-of select="@scheme|@http-equiv|@lang|@dir"/>
                    <xsl:if test="@xml:lang">
                        <xsl:attribute name="lang" select="@xml:lang"/>
                    </xsl:if>
                    <xsl:copy-of select="normalize-space(.)"/>
                </xsl:for-each>
            </title>

            <!-- mandatory metadata -->
            <meta name="dc:format" content="Daisy 2.02"/>
            <!-- dc:title mandatory in EPUB -->
            <!-- dc:identifier mandatory in EPUB -->
            <!-- dc:language mandatory in EPUB -->
            <!-- dc:creator is not strictly mandatory in EPUB, but we can't include a sensible value if it is not provided -->
            <!-- dc:publisher is not strictly mandatory in EPUB, but we can't include a sensible value if it is not provided -->
            <!-- dc:date is not strictly mandatory in EPUB, but we can't include a sensible value if it is not provided -->

            <meta name="ncc:charset" content="utf-8"/>
            <meta name="ncc:tocItems" content="{count($ncc.body/*)}"/>
            <meta name="ncc:pageFront" content="{count($ncc.body/html:span['page-front'=tokenize(@class,'\s+')])}"/>
            <meta name="ncc:pageNormal" content="{count($ncc.body/html:span['page-normal'=tokenize(@class,'\s+')])}"/>
            <meta name="ncc:pageSpecial" content="{count($ncc.body/html:span['page-special'=tokenize(@class,'\s+')])}"/>
            <meta name="ncc:sidebars" content="0"/>
            <meta name="ncc:prodNotes" content="0"/>
            <meta name="ncc:footnotes" content="0"/>
            <meta name="ncc:totalTime" content="{pf:mediaoverlay-seconds-to-full-clock-value(
                                                   sum($smil/body/seq/@dur/xs:decimal(replace(.,'^(.+)s$','$1'))))}"/>

            <xsl:for-each select="*[not(self::opf:*) and not(self::dc:format)]">
                <xsl:call-template name="meta">
                    <xsl:with-param name="name" select="name()"/>
                </xsl:call-template>
            </xsl:for-each>

            <xsl:for-each select="opf:meta[@property and not(@refines)]">
                <!-- NOTE on fidelity loss: meta elements that refine other meta elements are lost -->
                <!-- NOTE on fidelity loss: the @role attribute on meta elements are lost -->
                <xsl:call-template name="meta">
                    <xsl:with-param name="name" select="@property"/>
                </xsl:call-template>
            </xsl:for-each>
            
            <!-- <meta name="viewport" content="width=device-width"/> -->
            <!-- <meta name="dcterms:modified" content="{$modified}"/> -->
        </head>
    </xsl:template>

    <xsl:template name="meta">
        <xsl:param name="name" required="yes"/>
        <xsl:choose>
            <xsl:when test="$name=$allowed-meta or starts-with($name,'prod:')">
                <meta name="{$name}" content="{normalize-space(.)}">
                    <xsl:copy-of select="@scheme|@http-equiv|@lang|@dir"/>
                    <xsl:if test="@xml:lang">
                        <xsl:attribute name="lang" select="@xml:lang"/>
                    </xsl:if>
                </meta>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message select="concat('meta element &quot;',$name,'&quot; was omitted')"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:variable name="allowed-meta" as="xs:string*"
                  select="('dc:contributor',
                           'dc:coverage',
                           'dc:creator',
                           'dc:date',
                           'dc:description',
                           'dc:format',
                           'dc:identifier',
                           'dc:language',
                           'dc:publisher',
                           'dc:relation',
                           'dc:rights',
                           'dc:source',
                           'dc:subject',
                           'dc:title',
                           'dc:type',
                           'ncc:charset',
                           'ncc:depth',
                           'ncc:files',
                           'ncc:footnotes',
                           'ncc:generator',
                           'ncc:kByteSize',
                           'ncc:maxPageNormal',
                           'ncc:multimediaType',
                           'ncc:narrator',
                           'ncc:pageFront',
                           'ncc:pageNormal',
                           'ncc:pageSpecial',
                           'ncc:prodNotes',
                           'ncc:producedDate',
                           'ncc:producer',
                           'ncc:revision',
                           'ncc:revisionDate',
                           'ncc:setInfo',
                           'ncc:sidebars',
                           'ncc:sourceDate',
                           'ncc:sourceEdition',
                           'ncc:sourcePublisher',
                           'ncc:sourceRights',
                           'ncc:sourceTitle',
                           'ncc:tocItems',
                           'ncc:totalTime'
                           )"/>
    
</xsl:stylesheet>
