<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns="http://www.daisy.org/ns/z3998/authoring/"
                version="2.0" exclude-result-prefixes="dtb">

    <xsl:output indent="yes" method="xml"/>

    <xsl:template match="/">
         <head>
             <xsl:apply-templates/>
         </head>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <xsl:template match="//dtb:head">
        <xsl:for-each select="dtb:meta">
            <xsl:choose>
                <xsl:when test="@name='dc:Date'">
                    <meta property="{lower-case(@name)}" content="{@content}" xml:id="meta-dcdate"/>
                </xsl:when>
                <xsl:when test="@name=('dc:Title',
                                       'dc:Creator',
                                       'dc:Publisher',
                                       'dc:Language',
                                       'dc:Subject',
                                       'dc:Description',
                                       'dc:Contributor',
                                       'dc:Type',
                                       'dc:Format',
                                       'dc:Source',
                                       'dc:Relation',
                                       'dc:Coverage',
                                       'dc:Rights'
                                       )">
                    <meta property="{lower-case(@name)}" content="{@content}"/>
                </xsl:when>
                <xsl:when test="@name='dtb:revisionDescription'">
                    <meta property="dc:description" content="{@content}"
                          about="#meta-dcdate"/>
                </xsl:when>
                <xsl:when test="contains(@name,':')">
                    <!-- Allow custom metadata if the vocabulary can be guessed from the
                         prefix. Unknown metadata is discarded. (See the px:epub3-merge-prefix that
                         follows px:dtbook-to-zedai-meta.) -->
                    <meta property="{@name}" content="{@content}"/>
                </xsl:when>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
