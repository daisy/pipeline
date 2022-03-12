<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:include href="library.xsl"/>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="@css:padding-left|
                         @css:padding-right|
                         @css:padding-top|
                         @css:padding-bottom"/>
    
    <xsl:template match="css:box[@type='block'][@css:padding-left or
                                                @css:padding-right or
                                                @css:padding-top or
                                                @css:padding-bottom]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:element name="css:box">
                <xsl:attribute name="type" select="'block'"/>
                <xsl:attribute name="css:collapsing-margins" select="'no'"/>
                <xsl:apply-templates select="@css:padding-left|
                                             @css:padding-right|
                                             @css:padding-top|
                                             @css:padding-bottom" mode="padding-to-margin"/>
                <xsl:apply-templates>
                    <!--
                        because a box is added, inherit must be concretized on child boxes for
                        non-inherited properties (only one level of inheritance needed)
                    -->
                    <xsl:with-param name="concretize-inherit" select="true()" tunnel="yes"/>
                </xsl:apply-templates>
            </xsl:element>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="css:box">
        <xsl:copy>
            <xsl:apply-templates select="@style"/>
            <xsl:sequence select="@* except @style"/>
            <xsl:apply-templates>
                <xsl:with-param name="concretize-inherit" select="false()" tunnel="yes"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="@css:padding-left|
                         @css:padding-right|
                         @css:padding-top|
                         @css:padding-bottom" mode="padding-to-margin">
        <xsl:attribute name="css:{replace(local-name(),'padding','margin')}" select="."/>
    </xsl:template>
    
    <xsl:template match="css:box/@style">
        <xsl:param name="concretize-inherit" as="xs:boolean" select="false()" tunnel="yes"/>
        <xsl:choose>
            <xsl:when test="$concretize-inherit">
                <xsl:variable name="properties" as="element()*" select="css:parse-declaration-list(.)"/>
                <xsl:choose>
                    <xsl:when test="$properties[@value='inherit' and not(css:is-inherited(@name))]">
                        <xsl:variable name="parent-properties" as="element()*"
                                      select="css:parse-declaration-list(parent::*/ancestor::css:box[1]/@style)"/>
                        <xsl:sequence select="css:style-attribute(css:serialize-declaration-list(
                                                for $p in $properties return
                                                  if ($p/@value='inherit' and not(css:is-inherited($p/@name)))
                                                    then $parent-properties[@name=$p/@name][last()]
                                                    else $p))"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:next-match/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
</xsl:stylesheet>
