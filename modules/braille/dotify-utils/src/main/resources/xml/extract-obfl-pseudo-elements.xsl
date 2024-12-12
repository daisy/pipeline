<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:s="org.daisy.pipeline.braille.css.xpath.Style"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl" />
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[not(self::css:_)]" priority="1">
        <xsl:param name="style" as="item()?" tunnel="yes" select="()"/>
        <xsl:next-match>
            <xsl:with-param name="style" tunnel="yes" select="s:merge((
                                                                css:parse-stylesheet(@style,$style),
                                                                (@css:* except @css:page)/css:parse-stylesheet(.)))"/>
        </xsl:next-match>
    </xsl:template>
    
    <xsl:template match="*[css:_obfl-on-toc-start|
                           css:_obfl-on-collection-start|
                           css:_obfl-on-volume-start|
                           css:_obfl-on-volume-end|
                           css:_obfl-on-collection-end|
                           css:_obfl-on-toc-end]">
        <xsl:variable name="id" select="generate-id()"/>
        <xsl:copy>
            <xsl:sequence select="@*"/>
            <xsl:if test="css:_obfl-on-toc-start">
                <xsl:attribute name="css:_obfl-on-toc-start" select="$id"/>
            </xsl:if>
            <xsl:if test="css:_obfl-on-collection-start">
                <xsl:attribute name="css:_obfl-on-collection-start" select="$id"/>
            </xsl:if>
            <xsl:if test="css:_obfl-on-volume-start">
                <xsl:attribute name="css:_obfl-on-volume-start" select="$id"/>
            </xsl:if>
            <xsl:if test="css:_obfl-on-volume-end">
                <xsl:attribute name="css:_obfl-on-volume-end" select="$id"/>
            </xsl:if>
            <xsl:if test="css:_obfl-on-collection-end">
                <xsl:attribute name="css:_obfl-on-collection-end" select="$id"/>
            </xsl:if>
            <xsl:if test="css:_obfl-on-toc-end">
                <xsl:attribute name="css:_obfl-on-toc-end" select="$id"/>
            </xsl:if>
            <xsl:apply-templates select="node() except (css:_obfl-on-toc-start|
                                                        css:_obfl-on-collection-start|
                                                        css:_obfl-on-volume-start|
                                                        css:_obfl-on-volume-end|
                                                        css:_obfl-on-collection-end|
                                                        css:_obfl-on-toc-end)"/>
        </xsl:copy>
        <xsl:if test="css:_obfl-on-toc-start">
            <xsl:result-document href="-obfl-on-toc-start/{$id}">
                <css:_ css:flow="-obfl-on-toc-start/{$id}">
                    <xsl:apply-templates select="css:_obfl-on-toc-start"/>
                </css:_>
            </xsl:result-document>
        </xsl:if>
        <xsl:if test="css:_obfl-on-collection-start">
            <xsl:result-document href="-obfl-on-collection-start/{$id}">
                <css:_ css:flow="-obfl-on-collection-start/{$id}">
                    <xsl:apply-templates select="css:_obfl-on-collection-start"/>
                </css:_>
            </xsl:result-document>
        </xsl:if>
        <xsl:if test="css:_obfl-on-volume-start">
            <xsl:result-document href="-obfl-on-volume-start/{$id}">
                <css:_ css:flow="-obfl-on-volume-start/{$id}">
                    <xsl:apply-templates select="css:_obfl-on-volume-start"/>
                </css:_>
            </xsl:result-document>
        </xsl:if>
        <xsl:if test="css:_obfl-on-volume-end">
            <xsl:result-document href="-obfl-on-volume-end/{$id}">
                <css:_ css:flow="-obfl-on-volume-end/{$id}">
                    <xsl:apply-templates select="css:_obfl-on-volume-end"/>
                </css:_>
            </xsl:result-document>
        </xsl:if>
        <xsl:if test="css:_obfl-on-collection-end">
            <xsl:result-document href="-obfl-on-collection-end/{$id}">
                <css:_ css:flow="-obfl-on-collection-end/{$id}">
                    <xsl:apply-templates select="css:_obfl-on-collection-end"/>
                </css:_>
            </xsl:result-document>
        </xsl:if>
        <xsl:if test="css:_obfl-on-toc-end">
            <xsl:result-document href="-obfl-on-toc-end/{$id}">
                <css:_ css:flow="-obfl-on-toc-end/{$id}">
                    <xsl:apply-templates select="css:_obfl-on-toc-end"/>
                </css:_>
            </xsl:result-document>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="css:_obfl-on-toc-start|
                         css:_obfl-on-collection-start|
                         css:_obfl-on-volume-start|
                         css:_obfl-on-volume-end|
                         css:_obfl-on-collection-end|
                         css:_obfl-on-toc-end">
        <xsl:param name="style" as="item()?" tunnel="yes" select="()"/>
        <xsl:copy>
            <xsl:sequence select="@* except @style"/>
            <xsl:sequence select="css:style-attribute($style)"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
