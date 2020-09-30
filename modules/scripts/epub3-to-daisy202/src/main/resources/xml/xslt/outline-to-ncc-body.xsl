<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns="http://www.w3.org/1999/xhtml"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all">

    <xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/generate-id.xsl"/>
    <xsl:include href="http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl"/>

    <xsl:variable name="outline" select="collection()[1]" as="document-node()"/>
    <xsl:variable name="page-list" select="collection()[2]" as="document-node()"/>
    <xsl:variable name="content-docs" select="collection()[position()&gt;2]" as="document-node()*"/>

    <xsl:key name="referenced-element" match="*[@id]" use="@id"/>

    <xsl:template match="/*" priority="1">
        <xsl:variable name="body" as="document-node()">
            <xsl:document>
                <body>
                    <xsl:call-template name="pf:next-match-with-generated-ids">
                        <xsl:with-param name="prefix" select="'h_'"/>
                        <xsl:with-param name="for-elements" select="//li/a|
                                                                    //li/span"/>
                        <xsl:with-param name="in-use" select="()"/>
                    </xsl:call-template>
                </body>
            </xsl:document>
        </xsl:variable>
        <!--
            now add missing ids (to the page number spans)
        -->
        <xsl:apply-templates mode="add-ids" select="$body/*"/>
    </xsl:template>

    <xsl:template match="/*">
        <xsl:for-each select="ol">
            <xsl:variable name="i" select="position()"/>
            <xsl:variable name="content-doc" select="$content-docs[$i]"/>
            <xsl:variable name="doc-uri" as="xs:string" select="pf:normalize-uri(base-uri($content-doc/*))"/>
            <xsl:variable name="relative-uri" select="pf:relativize-uri($doc-uri,base-uri($outline/*))"/>
            <xsl:variable name="headings" as="element()*">
                <xsl:apply-templates mode="headings" select=".">
                    <xsl:with-param name="content-doc" tunnel="yes" select="$content-doc"/>
                </xsl:apply-templates>
            </xsl:variable>
            <xsl:variable name="page-numbers" as="element()*">
                <xsl:apply-templates mode="page-numbers"
                                     select="$page-list//d:file[pf:normalize-uri(resolve-uri(@href,base-uri(.)))=$doc-uri]/d:anchor">
                    <xsl:with-param name="content-doc" select="$content-doc"/>
                    <xsl:with-param name="relative-uri" select="$relative-uri"/>
                </xsl:apply-templates>
            </xsl:variable>
            <xsl:for-each select="($headings,$page-numbers)">
                <xsl:sort select="key('referenced-element',
                                      substring-after(a/@href,'#'),
                                      $content-doc)
                                  [1]/count(preceding::*|ancestor::*)"/>
                <xsl:sequence select="."/>
            </xsl:for-each>
        </xsl:for-each>
    </xsl:template>

    <xsl:template mode="headings" match="ol">
        <xsl:param name="depth" as="xs:integer" tunnel="yes" select="0"/>
        <xsl:apply-templates mode="#current" select="*">
            <xsl:with-param name="depth" tunnel="yes" select="$depth + 1"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="headings" match="li">
        <xsl:apply-templates mode="#current" select="*"/>
    </xsl:template>

    <xsl:template mode="headings" match="a">
        <xsl:param name="depth" as="xs:integer" tunnel="yes" required="yes"/>
        <xsl:element name="h{$depth}">
            <xsl:call-template name="pf:generate-id"/>
            <xsl:copy>
                <!--
                    Link will be fixed by px:html-update-links and create-linkbacks.xsl
                -->
                <xsl:sequence select="@href"/>
                <xsl:value-of select="normalize-space(string-join(.//text(),' '))"/>
            </xsl:copy>
        </xsl:element>
    </xsl:template>

    <!--
        If a content document has no headings, a title is generated and put in a span. It has no
        corresponding element to reference, so make it reference the first element in the document
        that has a SMIL linkback. It will not match the text of the generated title, but it is
        better than no link because that would make the document unreachable from the NCC.
    -->
    <xsl:template mode="headings" match="/body/ol[count(descendant::span|descendant::a)=1]/li/span">
        <xsl:param name="content-doc" tunnel="yes" as="document-node()" required="yes"/>
        <xsl:variable name="first-smil-link" as="element()?" select="($content-doc//a[matches(@href,'^.+\.smil#.+$')])[1]"/>
        <xsl:if test="exists($first-smil-link)">
            <h1>
                <xsl:call-template name="pf:generate-id"/>
                <a href="{pf:relativize-uri($first-smil-link/resolve-uri(@href,base-uri(.)),base-uri($outline/*))}">
                    <xsl:value-of select="normalize-space(string-join(.//text(),' '))"/>
                </a>
            </h1>
        </xsl:if>
    </xsl:template>

    <xsl:template mode="headings" match="*">
        <xsl:message terminate="yes">coding error</xsl:message>
    </xsl:template>

    <xsl:template mode="page-numbers" match="d:anchor">
        <xsl:param name="content-doc" required="yes"/>
        <xsl:param name="relative-uri" required="yes"/>
        <xsl:variable name="element" as="element()" select="key('referenced-element',@id,$content-doc)"/>
        <span>
            <xsl:variable name="value" select="if (@title) then @title
                                               else normalize-space(string-join($element//text(),' '))"/>
            <xsl:sequence select="@class"/>
            <xsl:if test="not(@class)">
                <xsl:attribute name="class">
                    <xsl:variable name="element-class" as="xs:string*" select="$element/@class/tokenize(.,'\s+')[not(.='')]"/>
                    <xsl:choose>
                        <xsl:when test="$element-class[.=('page-normal','page-front','page-special')]">
                            <xsl:sequence select="$element-class[.=('page-normal','page-front','page-special')][1]"/>
                        </xsl:when>
                        <xsl:when test="matches($value,'^[0-9]+$')">
                            <xsl:sequence select="'page-normal'"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:sequence select="'page-special'"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
            </xsl:if>
            <a href="{$relative-uri}#{@id}">
                <xsl:value-of select="$value"/>
            </a>
        </span>
    </xsl:template>

    <xsl:template mode="add-ids" match="/*" priority="1">
        <xsl:call-template name="pf:next-match-with-generated-ids">
            <xsl:with-param name="prefix" select="'p_'"/>
            <xsl:with-param name="for-elements" select="//span[not(@id)]"/>
            <xsl:with-param name="in-use" select="()"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template mode="add-ids" match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template mode="add-ids" match="span[not(@id)]">
        <xsl:copy>
            <xsl:sequence select="@*"/>
            <xsl:call-template name="pf:generate-id"/>
            <xsl:sequence select="node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
