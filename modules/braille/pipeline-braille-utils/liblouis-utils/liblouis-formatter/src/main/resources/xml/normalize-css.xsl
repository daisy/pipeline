<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:louis="http://liblouis.org/liblouis"
    xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
    exclude-result-prefixes="xs louis css"
    version="2.0">
    
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>
    
    <xsl:variable name="liblouis-properties" as="xs:string*"
        select="('left',
                 'right',
                 'margin-top',
                 'margin-bottom',
                 'text-align',
                 'text-indent',
                 'page-break-before',
                 'page-break-after',
                 'page-break-inside',
                 'orphans')"/>
    
    <xsl:variable name="liblouis-inherited-properties" as="xs:string*"
        select="('text-align',
                 'text-indent')"/>
    
    <xsl:variable name="liblouis-initial-values" as="xs:string*"
        select="('auto',
                 'auto',
                 '0',
                 '0',
                 'left',
                 '0',
                 'auto',
                 'auto',
                 'auto',
                 '0')"/>
    
    <xsl:template match="@*|text()|comment()|processing-instruction()">
        <xsl:copy/>
    </xsl:template>
    
    <xsl:template match="louis:border|
                         louis:line|
                         louis:print-page|
                         louis:running-header|
                         louis:running-footer|
                         louis:page-layout">
        <xsl:sequence select="."/>
    </xsl:template>
    
    <xsl:template match="*">
        <xsl:variable name="properties" as="element()*">
            <xsl:apply-templates select="css:specified-properties($liblouis-properties, false(), false(), true(), .)[@name=$liblouis-properties]">
                <xsl:with-param name="display" select="(@css:display,'inline')[1]" tunnel="yes"/>
                <xsl:with-param name="context" select="." tunnel="yes"/>
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:copy>
            <xsl:apply-templates select="@*[not(name()='style')]"/>
            <xsl:sequence select="css:style-attribute(css:serialize-declaration-list($properties))"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="css:property">
        <xsl:param name="display" as="xs:string" select="'inline'" tunnel="yes"/>
        <xsl:param name="context" as="element()" tunnel="yes"/>
        <xsl:variable name="name" select="string(@name)"/>
        <xsl:if test="css:applies-to($name, $display)">
            <xsl:variable name="property" as="element()">
                <xsl:choose>
                    <xsl:when test="not($name=$liblouis-inherited-properties)">
                        <xsl:apply-templates select="." mode="css:inherit">
                            <xsl:with-param name="validate" select="true()"/>
                            <xsl:with-param name="context" select="$context"/>
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:sequence select="."/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:variable name="property" as="element()">
                <xsl:apply-templates select="$property" mode="css:default"/>
            </xsl:variable>
            <xsl:if test="not($property/@value='inherit')">
                <xsl:if test="$name=$liblouis-inherited-properties
                              or not($property/@value=$liblouis-initial-values[index-of($liblouis-properties, $name)])">
                    <xsl:sequence select="$property"/>
                </xsl:if>
            </xsl:if>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="css:property" priority="0.6">
        <xsl:param name="context" as="element()" tunnel="yes"/>
        <xsl:choose>
            <xsl:when test="$context/self::louis:box">
                <xsl:variable name="name" select="string(@name)"/>
                <xsl:if test="css:applies-to($name, 'block')">
                    <xsl:variable name="property" as="element()">
                        <xsl:apply-templates select="." mode="css:inherit">
                            <xsl:with-param name="validate" select="true()"/>
                            <xsl:with-param name="context" select="$context"/>
                        </xsl:apply-templates>
                    </xsl:variable>
                    <xsl:variable name="property" as="element()">
                        <xsl:apply-templates select="$property" mode="css:default"/>
                    </xsl:variable>
                    <xsl:if test="not($property/@value=$liblouis-initial-values[index-of($liblouis-properties, $name)])">
                        <xsl:sequence select="$property"/>
                    </xsl:if>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="css:property[@name=$css:paged-media-properties]" priority="0.7">
        <xsl:param name="context" as="element()" tunnel="yes"/>
        <xsl:if test="not($context/ancestor-or-self::louis:box)">
            <xsl:next-match/>
        </xsl:if>
    </xsl:template>
    
</xsl:stylesheet>
