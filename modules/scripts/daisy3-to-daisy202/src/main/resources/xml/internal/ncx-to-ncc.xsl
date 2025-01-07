<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns="http://www.w3.org/1999/xhtml"
    xpath-default-namespace="http://www.daisy.org/z3986/2005/ncx/" exclude-result-prefixes="xs"
    version="2.0">

    <xsl:output indent="yes" method="xml"/>
    
    <xsl:param name="date"/>

    <xsl:variable name="metadata" select="collection()/*:head" as="element()?"/>

    <xsl:key name="pages" match="pageTarget" use="@type"/>
    <xsl:key name="meta" match="meta" use="@name"/>
    <xsl:key name="nav-targets" match="navTarget" use="@class"/>
    
    <xsl:template match="text()"/>
    
    <xsl:template match="ncx">
        <html>
            <head>
                <title>
                    <xsl:value-of select="$metadata/*[local-name()='meta'][@name='dc:title']/@content"/>
                </title>
                <xsl:copy-of select="$metadata/*"/>
                
                <meta http-equiv="Content-type" content="text/html; charset=utf-8"/>
                <meta name="ncc:charset" content="utf-8"/>
                <meta name="dc:format" content="Daisy 2.02"/>
                <meta name="dc:date" content="{if ($date[.!='']) then $date
                                               else format-date(current-date(),'[Y]-[M01]-[D01]')}"/>
                <meta name="ncc:generator" content="DAISY Pipeline 2"/>
                <meta name="ncc:depth"
                      content="{for $depth in key('meta','dtb:depth')/@content return
                                if(number($depth)>6) then '6' else $depth}"/>
                <meta name="ncc:pageFront" content="{count(key('pages','front'))}"/>
                <meta name="ncc:pageNormal" content="{count(key('pages','normal'))}"/>
                <meta name="ncc:pageSpecial" content="{count(key('pages','special'))}"/>
                <meta name="ncc:prodNotes"
                    content="{count(key('nav-targets',('prodnote','optional-prodnote')))}"/>
                <meta name="ncc:footnotes" content="{count(key('nav-targets',('note','noteref')))}"/>
                <meta name="ncc:sidebars" content="{count(key('nav-targets',('sidebar')))}"/>
                <xsl:if test="exists(key('pages','normal'))">
                    <meta name="ncc:maxPageNormal"
                          content="{max(key('pages','normal')/number(@value))}"/>
                </xsl:if>
                <meta name="ncc:producedDate"
                    content="{if ($date[.!='']) then $date
                              else format-date(current-date(),'[Y]-[M01]-[D01]')}"/>
                <meta name="ncc:tocItems" content="{count(//@playOrder)}"/>
            </head>
            <body>
                <xsl:apply-templates select="//*[@playOrder]">
                    <xsl:sort select="@playOrder" data-type="number"/>
                </xsl:apply-templates>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="navPoint" priority="2">
        <xsl:element
            name="h{
            for $count in count(ancestor::navPoint) return 
              if ($count le 5) then$count+1 else '6'}">
            <xsl:if test="number(@playOrder)=1">
                <xsl:attribute name="class" select="'title'"/>
            </xsl:if>
            <xsl:apply-templates select="." mode="content"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="pageTarget">
        <span class="page-{@type}">
            <xsl:apply-templates select="." mode="content"/>
        </span>
    </xsl:template>

    <xsl:template match="navTarget[@class=('note','noteref')]">
        <span class="noteref">
            <xsl:apply-templates select="." mode="content"/>
        </span>
    </xsl:template>
    <xsl:template match="navTarget[@class=('sidebar')]">
        <span class="sidebar">
            <xsl:apply-templates select="." mode="content"/>
        </span>
    </xsl:template>
    <xsl:template match="navTarget[@class=('optional-prodnote','prodnote')]">
        <span class="optional-prodnote">
            <xsl:apply-templates select="." mode="content"/>
        </span>
    </xsl:template>
    <xsl:template match="navTarget">
        <div class="group">
            <xsl:apply-templates select="." mode="content"/>
        </div>
    </xsl:template>

    <xsl:template match="*[@playOrder]" mode="content">
        <xsl:copy-of select="@id"/>
        <a href="{content/@src}">
            <xsl:sequence select="normalize-space(navLabel/text)"/>
        </a>
    </xsl:template>

</xsl:stylesheet>
