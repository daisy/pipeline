<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>
    
    <xsl:template match="/css:_">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="css:box[@type=('block','table')]">
        <xsl:param name="used-container-left-content-edge" as="xs:integer" select="0"/>
        <xsl:param name="actual-container-left-content-edge" as="xs:integer" select="0"/>
        <xsl:param name="used-container-right-content-edge" as="xs:integer" select="0"/>
        <xsl:param name="actual-container-right-content-edge" as="xs:integer" select="0"/>
        <xsl:param name="computed-container-text-indent" as="xs:integer" select="0"/>
        <xsl:param name="actual-container-text-indent" as="xs:integer" select="0"/>
        <xsl:variable name="computed-margin-left" as="xs:integer" select="(@css:margin-left/xs:integer(number(.)),0)[1]"/>
        <xsl:variable name="computed-border-left-width" as="xs:integer" select="if ((@css:border-left-pattern|@css:border-left-style)[not(.='none')])
                                                                                then 1 else 0"/>
        <xsl:variable name="computed-padding-left" as="xs:integer" select="(@css:padding-left/xs:integer(number(.)),0)[1]"/>
        <xsl:variable name="used-left-margin-edge" as="xs:integer" select="$used-container-left-content-edge"/>
        <xsl:variable name="used-left-border-edge" as="xs:integer" select="$used-left-margin-edge + $computed-margin-left"/>
        <xsl:variable name="used-left-padding-edge" as="xs:integer" select="$used-left-border-edge + $computed-border-left-width"/>
        <xsl:variable name="used-left-content-edge" as="xs:integer" select="$used-left-padding-edge + $computed-padding-left"/>
        <xsl:variable name="actual-left-margin-edge" as="xs:integer" select="$actual-container-left-content-edge"/>
        <xsl:variable name="actual-left-border-edge" as="xs:integer" select="if (not((@css:border-left-pattern|@css:border-left-style|
                                                                                      @css:border-top-pattern|@css:border-top-style|
                                                                                      @css:border-bottom-pattern|@css:border-bottom-style)[not(.='none')]))
                                                                             then $actual-left-margin-edge
                                                                             else max(($actual-left-margin-edge,$used-left-border-edge))"/>
        <xsl:variable name="actual-left-padding-edge" as="xs:integer" select="$actual-left-border-edge + $computed-border-left-width"/>
        <xsl:variable name="actual-left-content-edge" as="xs:integer" select="if (child::css:box[@type=('block','table')])
                                                                              then $actual-left-padding-edge
                                                                              else max(($actual-left-padding-edge,$used-left-content-edge))"/>
        <xsl:variable name="actual-margin-left" as="xs:integer" select="$actual-left-border-edge - $actual-left-margin-edge"/>
        <xsl:variable name="actual-padding-left" as="xs:integer" select="$actual-left-content-edge - $actual-left-padding-edge"/>
        <xsl:variable name="computed-text-indent" as="xs:integer" select="(@css:text-indent/xs:integer(number(.)),$computed-container-text-indent)[1]"/>
        <xsl:variable name="used-first-line-left-edge" as="xs:integer" select="$used-left-content-edge + $computed-text-indent"/>
        <xsl:variable name="actual-first-line-left-edge" as="xs:integer" select="if (child::css:box[@type=('block','table')])
                                                                                 then $actual-left-padding-edge
                                                                                 else max(($actual-left-padding-edge,$used-first-line-left-edge))"/>
        <xsl:variable name="actual-text-indent" as="xs:integer" select="$actual-first-line-left-edge - $actual-left-content-edge"/>
        <xsl:variable name="computed-margin-right" as="xs:integer" select="(@css:margin-right/xs:integer(number(.)),0)[1]"/>
        <xsl:variable name="computed-border-right-width" as="xs:integer" select="if ((@css:border-right-pattern|@css:border-right-style)[not(.='none')])
                                                                                 then 1 else 0"/>
        <xsl:variable name="computed-padding-right" as="xs:integer" select="(@css:padding-right/xs:integer(number(.)),0)[1]"/>
        <xsl:variable name="used-right-margin-edge" as="xs:integer" select="$used-container-right-content-edge"/>
        <xsl:variable name="used-right-border-edge" as="xs:integer" select="$used-right-margin-edge + $computed-margin-right"/>
        <xsl:variable name="used-right-padding-edge" as="xs:integer" select="$used-right-border-edge + $computed-border-right-width"/>
        <xsl:variable name="used-right-content-edge" as="xs:integer" select="$used-right-padding-edge + $computed-padding-right"/>
        <xsl:variable name="actual-right-margin-edge" as="xs:integer" select="$actual-container-right-content-edge"/>
        <xsl:variable name="actual-right-border-edge" as="xs:integer" select="if (not((@css:border-right-pattern|@css:border-right-style|
                                                                                       @css:border-top-pattern|@css:border-top-style|
                                                                                       @css:border-bottom-pattern|@css:border-bottom-style)[not(.='none')]))
                                                                              then $actual-right-margin-edge
                                                                              else max(($actual-right-margin-edge,$used-right-border-edge))"/>
        <xsl:variable name="actual-right-padding-edge" as="xs:integer" select="$actual-right-border-edge + $computed-border-right-width"/>
        <xsl:variable name="actual-right-content-edge" as="xs:integer" select="if (child::css:box[@type=('block','table')])
                                                                               then $actual-right-padding-edge
                                                                               else max(($actual-right-padding-edge,$used-right-content-edge))"/>
        <xsl:variable name="actual-margin-right" as="xs:integer" select="$actual-right-border-edge - $actual-right-margin-edge"/>
        <xsl:variable name="actual-padding-right" as="xs:integer" select="$actual-right-content-edge - $actual-right-padding-edge"/>
        <xsl:copy>
            <xsl:apply-templates select="@* except (@css:margin-left|@css:margin-right|
                                                    @css:padding-left|@css:padding-right|
                                                    @css:text-indent)"/>
            <xsl:if test="$actual-margin-left &gt; 0">
                <xsl:attribute name="css:margin-left" select="$actual-margin-left"/>
            </xsl:if>
            <xsl:if test="$actual-margin-right &gt; 0">
                <xsl:attribute name="css:margin-right" select="$actual-margin-right"/>
            </xsl:if>
            <xsl:if test="$actual-padding-left &gt; 0">
                <xsl:attribute name="css:padding-left" select="$actual-padding-left"/>
            </xsl:if>
            <xsl:if test="$actual-padding-right &gt; 0">
                <xsl:attribute name="css:padding-right" select="$actual-padding-right"/>
            </xsl:if>
            <xsl:if test="$actual-text-indent != $actual-container-text-indent">
                <xsl:attribute name="css:text-indent" select="$actual-text-indent"/>
            </xsl:if>
            <xsl:apply-templates>
                <xsl:with-param name="used-container-left-content-edge" select="$used-left-content-edge"/>
                <xsl:with-param name="actual-container-left-content-edge" select="$actual-left-content-edge"/>
                <xsl:with-param name="used-container-right-content-edge" select="$used-right-content-edge"/>
                <xsl:with-param name="actual-container-right-content-edge" select="$actual-right-content-edge"/>
                <xsl:with-param name="computed-container-text-indent" select="$computed-text-indent"/>
                <xsl:with-param name="actual-container-text-indent" select="$actual-text-indent"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="css:box[@type='table-cell']">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()">
                <xsl:with-param name="used-container-left-content-edge" select="0"/>
                <xsl:with-param name="actual-container-left-content-edge" select="0"/>
                <xsl:with-param name="used-container-right-content-edge" select="0"/>
                <xsl:with-param name="actual-container-right-content-edge" select="0"/>
                <xsl:with-param name="computed-container-text-indent" select="0"/>
                <xsl:with-param name="actual-container-text-indent" select="0"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>
    
    <!--
        ignore non-collapsing margins
    -->
    <xsl:template match="@css:collapsing-margins"/>
    
    <xsl:template match="@*|node()">
        <xsl:sequence select="."/>
    </xsl:template>
    
</xsl:stylesheet>
