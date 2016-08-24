<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/" exclude-result-prefixes="dtb" version="2.0">

    <!--Normalizes mixed block/inline content models.-->

    <xsl:output indent="yes" method="xml"/>
    
    <xsl:template match="/">
        <xsl:message>Normalize mixed section/block content</xsl:message>
        <xsl:apply-templates/>
    </xsl:template>
    
    
    <!-- identity template -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- purposely exclude level6 because it cannot contain child levels -->
    <xsl:template match="dtb:level | dtb:level1 | dtb:level2 | dtb:level3 | dtb:level4 | dtb:level5">
        <xsl:message>Normalize mixed section and block content model for <xsl:value-of select="local-name(.)"/></xsl:message>
        <xsl:choose>
            <xsl:when test="self::dtb:level">
                <xsl:call-template name="normalize-level">
                    <xsl:with-param name="child-level-name">level</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="self::dtb:level1">
                <xsl:call-template name="normalize-level">
                    <xsl:with-param name="child-level-name">level2</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="self::dtb:level2">
                <xsl:call-template name="normalize-level">
                    <xsl:with-param name="child-level-name">level3</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="self::dtb:level3">
                <xsl:call-template name="normalize-level">
                    <xsl:with-param name="child-level-name">level4</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="self::dtb:level4">
                <xsl:call-template name="normalize-level">
                    <xsl:with-param name="child-level-name">level5</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="self::dtb:level5">
                <xsl:call-template name="normalize-level">
                    <xsl:with-param name="child-level-name">level6</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <!-- zedai section elements must not be interleaved with non-section elements -->
    <xsl:template name="normalize-level">
        
        <xsl:param name="child-level-name" as="xs:string"/>
        
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:for-each-group group-adjacent="exists(self::dtb:*[local-name() = $child-level-name])" select="*">
                <xsl:choose>
                    <!-- the target element itself-->
                    <xsl:when test="current-grouping-key() or position()=1 or (every $e in current-group() satisfies $e/self::dtb:pagenum)">
                        <xsl:apply-templates select="current-group()"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:element name="{$child-level-name}"
                            namespace="http://www.daisy.org/z3986/2005/dtbook/">
                            <xsl:copy-of select="current-group()"/>
                        </xsl:element>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each-group>
        </xsl:copy>
    </xsl:template>


</xsl:stylesheet>
