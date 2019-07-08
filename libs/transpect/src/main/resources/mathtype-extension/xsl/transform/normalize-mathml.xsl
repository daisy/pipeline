<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="2.0"
  xmlns:xsl		= "http://www.w3.org/1999/XSL/Transform"
  xmlns:fn              = "http://www.w3.org/2005/xpath-functions"
  xmlns:xs		= "http://www.w3.org/2001/XMLSchema"
  xmlns:saxon		= "http://saxon.sf.net/"
  xmlns:letex		= "http://www.le-tex.de/namespace"
  exclude-result-prefixes = "xs saxon letex fn"
  xmlns="http://www.w3.org/1998/Math/MathML"
  xpath-default-namespace="http://www.w3.org/1998/Math/MathML">

  <xsl:include href="identity.xsl"/>

    <xsl:template match="/*:root">
       <xsl:apply-templates select="node()"/>
    </xsl:template>

    <xsl:template match="mrow">
        <xsl:choose>
            <xsl:when test="count(node()) eq count(child::mn)">
                <mn>
                    <xsl:value-of select=".//mn/text()"/>
                </mn>
            </xsl:when>
            <xsl:when test="count(node()) eq count(child::mstyle[@mathsize='normal' and @mathvariant='bold'])">
                <mstyle mathsize="normal" mathvariant="bold">
                    <xsl:apply-templates select="mstyle/node()"/>
                </mstyle>
            </xsl:when>
            <xsl:otherwise>
                <mrow>
                    <xsl:variable name="mtext">
                        <xsl:for-each-group select="node()" group-adjacent="if(self::mtext) then true() else false()">
                            <xsl:choose>
                                <xsl:when test="current-grouping-key()">
                                    <xsl:element name="mtext" namespace="http://www.w3.org/1998/Math/MathML">
                                        <xsl:apply-templates select="current-group()/node()"/>
                                    </xsl:element>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:apply-templates select="current-group()"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:for-each-group>
                    </xsl:variable>
                    <xsl:variable name="mstyle">
                        <xsl:for-each-group select="$mtext/node()" group-adjacent="if(self::mstyle and self::*/@mathsize='normal' and self::*/@mathvariant='bold') then true() else false()">
                            <xsl:choose>
                                <xsl:when test="current-grouping-key()">
                                    <mstyle mathsize="normal" mathvariant="bold">
                                        <xsl:apply-templates select="current-group()/node()"/>
                                    </mstyle>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:apply-templates select="current-group()"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:for-each-group>
                    </xsl:variable>
                    <xsl:for-each-group select="$mstyle/node()" group-adjacent="if(self::mstyle and self::*/@mathsize='normal' and self::*/@mathvariant='bold') then true() else false()">
                        <xsl:choose>
                            <xsl:when test="current-grouping-key()">
                                <mstyle mathsize="normal" mathvariant="bold">
                                    <xsl:apply-templates select="current-group()/node()"/>
                                </mstyle>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:apply-templates select="current-group()"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each-group>
                </mrow>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="mtext[preceding-sibling::*[1][self::mtext]]|mtext[following-sibling::*[1][self::mtext]]">
        <xsl:value-of select="."/>
    </xsl:template>
    
<!-- normalize functions -->
    <xsl:template match="*[@start-function]">
        <mi>
            <xsl:value-of select="."/>
            <xsl:value-of select="./following-sibling::*[1]"/>
            <xsl:value-of select="./following-sibling::*[2]"/>
        </mi>
    </xsl:template>
    <xsl:template match="*[preceding-sibling::*[1][@start-function] or preceding-sibling::*[2][@start-function]]"/>
    
<!-- do not set default attributes explicitly -->
    <xsl:template match="mtr/@columnalign[.='center']|mtd/@columnalign[.='center']|mtable/@columnalign[.='center']"/>
</xsl:stylesheet>
