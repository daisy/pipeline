<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
                xmlns:m="http://www.w3.org/1998/Math/MathML"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:svg="http://www.w3.org/2000/svg"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://www.w3.org/1999/xhtml"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all"
                version="2.0">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl"/>
    <!--    <xsl:import href="../../../../test/xspec/mock/uri-functions.xsl"/>-->
    <xsl:import href="update-epub-prefixes.xsl"/>

    <xsl:template match="@*|node()">
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/*">
        <wrapper>
            <xsl:attribute name="xml:base" select="base-uri(/*)"/>

            <xsl:variable name="base" select="base-uri(/*)"/>
            <xsl:variable name="head" select="/html/head"/>
            <xsl:for-each select="/html/body/*">
                <xsl:variable name="body" select="."/>

                <html>
                    <xsl:copy-of select="/*/@* | @xml:base" exclude-result-prefixes="#all"/>
                    <xsl:namespace name="epub" select="'http://www.idpf.org/2007/ops'"/>
                    <xsl:variable name="prefixes" select="f:prefixes($head, $body, ())"/>
                    <xsl:if test="count($prefixes)">
                        <xsl:attribute name="epub:prefix" select="string-join($prefixes, ' ')"/>
                    </xsl:if>
                    <xsl:text>
</xsl:text>
                    <head>
                        <xsl:namespace name="dc" select="'http://purl.org/dc/elements/1.1/'"/>
                        <xsl:namespace name="dcterms" select="'http://purl.org/dc/terms/'"/>
                        <xsl:copy-of select="/html/head/@*" exclude-result-prefixes="#all"/>
                        <xsl:for-each select="/html/head/*">
                            <xsl:choose>
                                <xsl:when test="self::link[@rel=('prev','next')]"/>
                                <xsl:otherwise>
                                    <xsl:copy-of select="." exclude-result-prefixes="#all"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:for-each>
                        <xsl:if test="position()&gt;1">
                            <link rel="prev" href="{pf:relativize-uri(base-uri(preceding-sibling::*[1]),base-uri(.))}"/>
                        </xsl:if>
                        <xsl:if test="position()&lt;last()">
                            <link rel="next" href="{pf:relativize-uri(base-uri(following-sibling::*[1]),base-uri(.))}"/>
                        </xsl:if>
                    </head>
                    <xsl:text>
</xsl:text>
                    <body>
                        <xsl:apply-templates select="$body/(@* except @xml:base)"/>
                        <xsl:if test="$body[self::header]">
                            <xsl:attribute name="epub:type" select="string-join(('frontmatter','titlepage',tokenize($body/@epub:type,'\s+')),' ')"/>
                        </xsl:if>
                        <xsl:text>
</xsl:text>
                        <xsl:apply-templates select="$body/*"/>
                        <xsl:text>
</xsl:text>
                    </body>
                    <xsl:text>
</xsl:text>
                </html>
                <xsl:text>
</xsl:text>
            </xsl:for-each>

        </wrapper>
    </xsl:template>

    <xsl:template match="@src | @href | @data[parent::object] | @xlink:href | @altimg | @longdesc">
        <xsl:variable name="original-uri" select="if (replace(.,'^(.*)#(.*)$','$1')='') then base-uri(/*) else resolve-uri(replace(.,'^(.*)#(.*)$','$1'), base-uri(/*))"/>
        <xsl:variable name="original-uri-relative" select="pf:relativize-uri($original-uri, base-uri(/*))"/>
        <xsl:choose>
            <xsl:when test="$original-uri-relative=replace(base-uri(/*),'^.*/([^/]*)$','$1')">
                <xsl:variable name="target-id" select="substring-after(.,'#')"/>
                <xsl:variable name="target" select="(//*[(@id,@xml:id)=$target-id])[1]"/>
                <xsl:choose>
                    <xsl:when test="$target">
                        <xsl:attribute name="{name()}" select="concat(pf:relativize-uri(base-uri($target),$original-uri),'#',$target-id)"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="{name()}" select="'#'"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="." exclude-result-prefixes="#all"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
