<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml" xpath-default-namespace="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops" version="2.0" exclude-result-prefixes="#all">

    <xsl:output indent="yes" include-content-type="no"/>

    <xsl:template match="/*">
        <nav epub:type="landmarks" id="landmarks">
            <ol>
                <xsl:apply-templates select="*"/>
            </ol>
        </nav>
    </xsl:template>

    <xsl:template match="head"/>

    <xsl:template match="body">
        <xsl:for-each select="*[self::div or self::span[@class=('group','sidebar','optional-prodnote','noteref')]]">
            <li>
                <a href="{a[1]/@href}">
                    <xsl:if test="@id and not(@id=('toc','page-list','landmarks'))">
                        <xsl:attribute name="id" select="@id"/>
                    </xsl:if>
                    <xsl:choose>
                        <xsl:when test="@class='sidebar'">
                            <xsl:attribute name="epub:type" select="'sidebar'"/>
                        </xsl:when>
                        <xsl:when test="@class='optional-prodnote'">
                            <xsl:attribute name="epub:type" select="'annotation'"/>
                        </xsl:when>
                        <xsl:when test="@class='noteref'">
                            <xsl:attribute name="epub:type" select="'noteref'"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="epub:type" select="'division'"/>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:value-of select="a[1]"/>
                </a>
            </li>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
