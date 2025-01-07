<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns="http://www.w3.org/1999/xhtml">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

    <xsl:output indent="yes"/>

    <xsl:param name="output-base-uri" required="yes"/>

    <xsl:variable name="base-ref" select="concat(pf:relativize-uri(base-uri(/*),$output-base-uri),'#')"/>

    <xsl:template match="/*">
        <ol>
            <xsl:apply-templates mode="pagebreak" select="//*[tokenize(@epub:type,'\s+')='pagebreak' or @role='doc-pagebreak']"/>
        </ol>
    </xsl:template>
    <xsl:template mode="pagebreak" match="*">
        <xsl:variable name="content">
            <xsl:choose>
                <xsl:when test="@aria-label|@title">
                    <xsl:value-of select="normalize-space((@aria-label,@title)[1])"/>
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
