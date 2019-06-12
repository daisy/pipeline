<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
                xmlns="http://www.w3.org/1999/xhtml"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all"
                version="2.0">

    <xsl:param name="identifier" required="yes"/>
    <xsl:param name="title" required="yes"/>
    <xsl:param name="supplier" select="''"/>
    <xsl:param name="publisher" required="yes"/>
    <xsl:variable name="lang" select="string((/html/head/meta[@name='dc:language']/@content, /*/@xml:lang, /*/@lang)[1])"/>
    
    <xsl:include href="update-epub-prefixes.xsl"/>

    <xsl:template match="@*|node()">
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:apply-templates select="@*[not(name()=('lang','xml:lang'))] | node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/*">
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:namespace name="epub" select="'http://www.idpf.org/2007/ops'"/>
            <xsl:apply-templates select="@*[not(name()=('lang','xml:lang'))]"/>
            <xsl:attribute name="xml:lang" select="$lang"/>
            <xsl:attribute name="lang" select="$lang"/>
            <xsl:variable name="prefixes" select="f:prefixes(/*/head, /*/body, 'nordic')"/>
            <xsl:attribute name="epub:prefix" select="string-join($prefixes, ' ')"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="head">
        <head>
            <xsl:namespace name="dc" select="'http://purl.org/dc/elements/1.1/'"/>
            <xsl:namespace name="dcterms" select="'http://purl.org/dc/terms/'"/>
            <meta charset="UTF-8"/>
            <title>
                <xsl:value-of select="$title"/>
            </title>
            <meta name="dc:identifier" content="{$identifier}"/>
            <meta name="viewport" content="width=device-width"/>
            <meta name="nordic:guidelines" content="2015-1"/>
            <xsl:if test="$supplier!=''">
                <meta name="nordic:supplier" content="{$supplier}"/>
            </xsl:if>
            <meta name="dc:publisher" content="{$publisher}"/>
            <meta name="dc:language" content="{$lang}"/>
            <xsl:apply-templates select="*[not(self::meta | self::title)] | meta[not(@charset) and not(@name=('nordic:guidelines','nordic:supplier','dc:publisher','dc:language'))]"/>
            <style type="text/css" xml:space="preserve"><![CDATA[
                .initialism{
                    -epub-speak-as:spell-out;
                }
                .list-style-type-none{
                    list-style-type:none;
                }
                table[class ^= "table-rules-"],
                table[class *= " table-rules-"]{
                    border-width:thin;
                    border-style:hidden;
                }
                table[class ^= "table-rules-"]:not(.table-rules-none),
                table[class *= " table-rules-"]:not(.table-rules-none){
                    border-collapse:collapse;
                }
                table[class ^= "table-rules-"] td,
                table[class *= " table-rules-"] td{
                    border-width:thin;
                    border-style:none;
                }
                table[class ^= "table-rules-"] th,
                table[class *= " table-rules-"] th{
                    border-width:thin;
                    border-style:none;
                }
                table.table-rules-none td,
                table.table-rules-none th{
                    border-width:thin;
                    border-style:hidden;
                }
                table.table-rules-all td,
                table.table-rules-all th{
                    border-width:thin;
                    border-style:solid;
                }
                table.table-rules-cols td,
                table.table-rules-cols th{
                    border-left-width:thin;
                    border-right-width:thin;
                    border-left-style:solid;
                    border-right-style:solid;
                }
                table.table-rules-rows tr{
                    border-top-width:thin;
                    border-bottom-width:thin;
                    border-top-style:solid;
                    border-bottom-style:solid;
                }
                table.table-rules-groups colgroup{
                    border-left-width:thin;
                    border-right-width:thin;
                    border-left-style:solid;
                    border-right-style:solid;
                }
                table.table-rules-groups tfoot,
                table.table-rules-groups thead,
                table.table-rules-groups tbody{
                    border-top-width:thin;
                    border-bottom-width:thin;
                    border-top-style:solid;
                    border-bottom-style:solid;
                }
                table[class ^= "table-frame-"],
                table[class *= " table-frame-"]{
                    border:thin hidden;
                }
                table.table-frame-void{
                    border-style:hidden;
                }
                table.table-frame-above{
                    border-style:outset hidden hidden hidden;
                }
                table.table-frame-below{
                    border-style:hidden hidden outset hidden;
                }
                table.table-frame-lhs{
                    border-style:hidden hidden hidden outset;
                }
                table.table-frame-rhs{
                    border-style:hidden outset hidden hidden;
                }
                table.table-frame-hsides{
                    border-style:outset hidden;
                }
                table.table-frame-vsides{
                    border-style:hidden outset;
                }
                table.table-frame-box{
                    border-style:outset;
                }
                table.table-frame-border{
                    border-style:outset;
                }
            ]]></style>
        </head>
    </xsl:template>

    <!-- delete empty ol elements (this probably stems from a bug in epub3-nav-utils) -->
    <xsl:template match="ol[not(*)]"/>

</xsl:stylesheet>
