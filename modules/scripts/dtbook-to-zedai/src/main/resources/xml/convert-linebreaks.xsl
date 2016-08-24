<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" 
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp"
    exclude-result-prefixes="xs tmp dtb" version="2.0">

    <xsl:output indent="yes" method="xml"/>

    <xsl:template match="/">
        <xsl:message>Convert br to lines</xsl:message>
        <xsl:apply-templates/>
    </xsl:template>

    <!-- instead of br line delimiters, wrap lines with the ln element.  these elements' zedai content models are the same or a subset of
        zedai:ln's own content model, so it's safe to wrap their contents in ln.
        we use tmp:ln (a made-up element) and later convert to zedai:ln
    -->
    <xsl:template match="dtb:a[dtb:br] | dtb:author[dtb:br] | dtb:bdo[dtb:br] | dtb:bridgehead[dtb:br] | dtb:byline[dtb:br] | 
        dtb:cite[dtb:br] | dtb:dateline[dtb:br] | dtb:dd[dtb:br] | dtb:dfn[dtb:br] | dtb:docauthor[dtb:br] | dtb:doctitle[dtb:br] | 
        dtb:dt[dtb:br] | dtb:em[dtb:br] | dtb:h1[dtb:br] | dtb:h2[dtb:br] | dtb:h3[dtb:br] | dtb:h4[dtb:br] | dtb:h5[dtb:br] | 
        dtb:h6[dtb:br] | dtb:hd[dtb:br] | dtb:p[dtb:br] | dtb:sent[dtb:br] | dtb:span[dtb:br] | dtb:strong[dtb:br] | dtb:title[dtb:br] | 
        dtb:line[dtb:br]">
        <xsl:copy>
            <xsl:copy-of select="@*"/>

            <xsl:for-each-group select="node()" group-ending-with="dtb:br">
                <xsl:if test="not(empty(current-group()[not(self::dtb:br)][normalize-space()]))">
                    <xsl:element name="tmp:ln">
                        <xsl:for-each select="current-group()[not(self::dtb:br)]">
                            <xsl:choose>
                                <xsl:when test="self::text() and position()=1 and position()=last()">
                                    <xsl:value-of select="normalize-space()"/>
                                </xsl:when>
                                <xsl:when test="self::text() and position()=1">
                                    <xsl:value-of select="replace(.,'^\s+','')"/>
                                </xsl:when>
                                <xsl:when test="self::text() and position()=last()">
                                    <xsl:value-of select="replace(.,'\s+$','')"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:apply-templates select="."/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:for-each>
                    </xsl:element>
                    
                </xsl:if>

            </xsl:for-each-group>

        </xsl:copy>
    </xsl:template>

    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>


</xsl:stylesheet>
