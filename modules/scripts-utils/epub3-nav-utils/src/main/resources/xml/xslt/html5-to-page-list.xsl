<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

    <xsl:output indent="yes"/>

    <xsl:param name="base-uri" select="base-uri(/*)"/>

    <xsl:variable name="base-ref">
        <xsl:variable name="base-dir" select="replace($base-uri,'/[^/]*(#.*)?$','/')"/>
        <xsl:choose>
            <xsl:when test="starts-with(base-uri(/*),$base-dir)">
                <xsl:value-of select="concat(substring-after(base-uri(/*),$base-dir),'#')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message
                    select="concat('Document base URI [',base-uri(/*),'] is not relative to the given base URI [',$base-uri,']')"/>
                <xsl:value-of select="concat(base-uri(/),'#')"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>

    <xsl:template match="/*">
        <ol>
            <xsl:apply-templates select="//*[@epub:type='pagebreak']"/>
        </ol>
    </xsl:template>
    <xsl:template match="*[@epub:type='pagebreak']">
        <xsl:variable name="content">
            <xsl:choose>
                <xsl:when test="@title">
                    <xsl:value-of select="@title"/>
                </xsl:when>
                <xsl:when test="@id and normalize-space()=''">
                    <xsl:message select="concat('WARNING page break with ID ',@id,' has no value')"/>
                    <xsl:value-of select="'-'"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="@id">
                <li>
                    <a href="{concat($base-ref,@id)}">
                        <xsl:copy-of select="$content"/>
                    </a>
                </li>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message select="concat('page ',$content,' has no ID')"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>


</xsl:stylesheet>
