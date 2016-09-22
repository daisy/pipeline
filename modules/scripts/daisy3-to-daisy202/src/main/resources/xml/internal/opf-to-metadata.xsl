<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.w3.org/1999/xhtml" xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xpath-default-namespace="http://openebook.org/namespaces/oeb-package/1.0/"
    exclude-result-prefixes="#all" version="2.0">

    <xsl:output indent="yes"/>

    <xsl:template match="text()"/>

    <xsl:template match="metadata">
        <head>
            <xsl:apply-templates/>
        </head>
    </xsl:template>

    <xsl:template match="dc:Format|dc:Date"/>
    <xsl:template match="dc:*">
        <meta name="{lower-case(name())}" content="{normalize-space(.)}"/>
    </xsl:template>

    <xsl:template match="meta[@name='dtb:sourceDate']">
        <meta name="ncc:sourceDate" content="{@content}"/>
    </xsl:template>

    <xsl:template match="meta[@name='dtb:sourceEdition']">
        <meta name="ncc:sourceEdition" content="{@content}"/>
    </xsl:template>

    <xsl:template match="meta[@name='dtb:sourcePublisher']">
        <meta name="ncc:sourcePublisher" content="{@content}"/>
    </xsl:template>

    <xsl:template match="meta[@name='dtb:sourceRights']">
        <meta name="ncc:sourceRights" content="{@content}"/>
    </xsl:template>

    <xsl:template match="meta[@name='dtb:sourceTitle']">
        <meta name="ncc:sourceTitle" content="{@content}"/>
    </xsl:template>

    <xsl:template match="meta[@name='dtb:multimediaType']">
        <meta name="ncc:multimediaType"
            content="{
            if (@content='audioNCX') then 'audioNcc'
            else if (@content='textNCX') then 'textNcc'
            else @content
            }"
        />
    </xsl:template>

    <xsl:template match="meta[@name='dtb:multimediaContent']">
        <meta name="prod:multimediaContent" content="{@content}"/>
    </xsl:template>

    <xsl:template match="meta[@name='dtb:narrator']">
        <meta name="ncc:narrator" content="{@content}"/>
    </xsl:template>

    <xsl:template match="meta[@name='dtb:producer']">
        <meta name="ncc:producer" content="{@content}"/>
    </xsl:template>

    <!--will be generated-->
    <xsl:template match="meta[@name='dtb:producedDate']"/>

    <xsl:template match="meta[@name='dtb:revision']">
        <meta name="ncc:revision" content="{@content}"/>
    </xsl:template>

    <xsl:template match="meta[@name='dtb:revisionDate']">
        <meta name="ncc:revisionDate" content="{@content}"/>
    </xsl:template>

    <xsl:template match="meta[@name='dtb:revisionDescription']">
        <meta name="ncc:revisionDescription" content="{@content}"/>
    </xsl:template>

    <!--FIXME convert SMIL clock value-->
    <xsl:template match="meta[@name='dtb:totalTime']">
        <meta name="ncc:totalTime" content="{@content}"/>
    </xsl:template>

    <xsl:template match="meta[@name='dtb:audioFormat']">
        <meta name="prod:audioFormat" content="{@content}"/>
    </xsl:template>

</xsl:stylesheet>
