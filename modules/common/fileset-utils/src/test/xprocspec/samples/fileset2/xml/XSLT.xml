<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:h="http://www.w3.org/1999/xhtml" exclude-result-prefixes="#all" version="2.0">
    
    <xsl:param name="content-dir" required="yes"/>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="h:meta"/>

    <xsl:template match="h:a">
        <xsl:variable name="a-href" select="tokenize(@href,'#')[1]"/>
        <xsl:variable name="a-fragment" select="if (contains(@href,'#')) then tokenize(@href,'#')[last()] else ''"/>
        <xsl:variable name="self-id" select="ancestor-or-self::*/@id"/>
        <xsl:choose>
            <xsl:when test="starts-with(@href,'#') or not(matches($a-href,'^[^/]+:')) and resolve-uri(replace($a-href,'\.html$','.xhtml'),$content-dir) = /*/@xml:base">
                <!-- is link to the same document -->
                <xsl:choose>
                    <xsl:when test="$a-fragment = ('',$self-id)">
                        <!-- is link to the same part of the document (or no part of the document); replace the link with a span -->
                        <span xmlns="http://www.w3.org/1999/xhtml">
                            <xsl:apply-templates select="@*|node()"/>
                        </span>
                    </xsl:when>
                    <xsl:otherwise>
                        <!-- is link to another part of the document; only keep the fragment part of the href -->
                        <xsl:copy>
                            <xsl:apply-templates select="@*"/>
                            <xsl:attribute name="href" select="concat('#',$a-fragment)"/>
                            <xsl:apply-templates select="*|text()|processing-instruction()|comment()"/>
                        </xsl:copy>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <!-- links to another document; keep it as it is -->
                <xsl:copy>
                    <xsl:apply-templates select="@*|node()"/>
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
