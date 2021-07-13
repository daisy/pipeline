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
    
    <xsl:template match="css:content[@target]">
        <xsl:variable name="target" select="@target"/>
        <xsl:apply-templates mode="copy" select="//*[@css:id=$target][1]/child::node()">
            <xsl:with-param name="anchor" select="$target"/>
            <xsl:with-param name="parent-style"
                            select="css:computed-properties(($css:properties,'#all'), true(), true(), false(), .)"/>
        </xsl:apply-templates>
    </xsl:template>
    
    <xsl:template mode="copy" match="*|text()">
        <xsl:param name="anchor" as="xs:string" required="yes"/>
        <xsl:param name="parent-style" as="element(css:property)*" required="yes"/>
        <xsl:variable name="style" as="element(css:property)*">
            <xsl:for-each select="css:computed-properties(($css:properties,'#all'), true(), true(), false(), .)">
                <xsl:variable name="property" as="xs:string" select="@name"/>
                <xsl:choose>
                    <xsl:when test="css:is-inherited($property) and $parent-style[@name=$property]">
                        <xsl:if test="@value!=$parent-style[@name=$property]/@value">
                            <xsl:sequence select="."/>
                        </xsl:if>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:if test="@value!=css:initial-value($property)">
                            <xsl:sequence select="."/>
                        </xsl:if>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="style" as="element(css:rule)*">
            <xsl:if test="exists($style)">
                <css:rule>
                    <xsl:sequence select="$style"/>
                </css:rule>
            </xsl:if>
            <xsl:sequence select="css:parse-stylesheet(@style)[@selector]"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="self::*">
                <xsl:copy>
                    <xsl:sequence select="@* except @style"/>
                    <xsl:if test="exists($style)">
                        <xsl:sequence select="css:style-attribute(css:serialize-stylesheet($style))"/>
                    </xsl:if>
                    <xsl:if test="not(@css:anchor)">
                        <xsl:attribute name="css:anchor" select="$anchor"/>
                    </xsl:if>
                    <xsl:sequence select="node()"/>
                </xsl:copy>
            </xsl:when>
            <xsl:when test="exists($style)">
                <!-- create anonymous box for attaching style -->
                <css:box type="inline">
                    <xsl:sequence select="css:style-attribute(css:serialize-stylesheet($style))"/>
                    <xsl:sequence select="."/>
                </css:box>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template mode="copy"
                  match="css:after|
                         css:before|
                         css:duplicate|
                         css:alternate|
                         css:footnote-call"/>

    <!--
        prevent already parsed properties to be serialized again
    -->
    <xsl:template match="@css:*" mode="css:attribute-as-property"/>

</xsl:stylesheet>
