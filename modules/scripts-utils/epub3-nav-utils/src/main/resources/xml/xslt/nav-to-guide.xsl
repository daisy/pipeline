<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:opf="http://www.idpf.org/2007/opf" xmlns="http://www.idpf.org/2007/opf" xmlns:html="http://www.w3.org/1999/xhtml"
    xmlns:epub="http://www.idpf.org/2007/ops" xmlns:pf="http://www.daisy.org/ns/pipeline/functions" exclude-result-prefixes="#all">
    <xsl:include href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
    <xsl:output indent="yes"/>
    <xsl:param name="opf-base" required="yes"/>
    <xsl:template match="/*">
        <guide>
            <xsl:for-each select="//html:nav[@*[name()='epub:type']='landmarks']">
                <xsl:call-template name="landmarks-nav"/>
            </xsl:for-each>
        </guide>
    </xsl:template>
    <xsl:template name="landmarks-nav">
        <xsl:for-each select="descendant::html:a">
            <reference title="{.}" href="{pf:relativize-uri(resolve-uri(@href,base-uri(.)),$opf-base)}">
                <xsl:attribute name="type">
                    <xsl:choose>
                        <xsl:when test="@epub:type='titlepage'">
                            <xsl:value-of select="'title-page'"/>
                        </xsl:when>
                        <xsl:when test="@epub:type=('rearnotes','footnotes')">
                            <xsl:value-of select="'note'"/>
                        </xsl:when>
                        <xsl:when
                            test="@epub:type=('acknowledgements','bibliography','colophon','copyright-page','cover','dedication','epigraph','foreword','glossary','index','loi','lot','preface','toc')">
                            <xsl:value-of select="@epub:type"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="concat('other.',@epub:type)"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
            </reference>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
