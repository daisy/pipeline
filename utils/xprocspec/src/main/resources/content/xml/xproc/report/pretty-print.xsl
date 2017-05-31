<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

    <xsl:template match="@*|comment()|processing-instruction()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="text()">
        <xsl:choose>
            <xsl:when test="(ancestor::*/@xml:space)[last()]='preserve'">
                <xsl:copy-of select="."/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="normalize-space()"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*">
        <xsl:param name="level" select="0" tunnel="yes"/>
        <xsl:choose>
            <xsl:when test="(ancestor-or-self::*/@xml:space)[last()]='preserve'">
                <xsl:copy-of select="."/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="$level &gt; 0">
                    <xsl:value-of select="concat('&#xA;', string-join(for $i in (0 to $level) return '    ', ''))"/>
                </xsl:if>
                <xsl:choose>
                    <xsl:when test="node()">
                        <xsl:copy>
                            <xsl:apply-templates select="@*|node()">
                                <xsl:with-param name="level" select="$level+1" tunnel="yes"/>
                            </xsl:apply-templates>
                            <xsl:value-of select="concat('&#xA;', string-join(for $i in (0 to $level) return '    ', ''))"/>
                        </xsl:copy>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:copy-of select="."/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
