<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:s="org.daisy.pipeline.braille.css.xpath.Style"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:include href="library.xsl"/>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[@css:string-set]">
        <xsl:copy>
            <xsl:if test="@css:string-set!='none'">
                <xsl:variable name="evaluated-string-set" as="element(css:property)*">
                    <xsl:for-each select="s:toXml(css:parse-stylesheet(@css:string-set))">
                        <xsl:copy>
                            <xsl:sequence select="@*"/>
                            <xsl:apply-templates mode="string-set" select="css:string-set">
                            </xsl:apply-templates>
                        </xsl:copy>
                    </xsl:for-each>
                </xsl:variable>
                <xsl:apply-templates mode="css:property-as-attribute" select="$evaluated-string-set"/>
            </xsl:if>
            <xsl:sequence select="@* except @css:string-set"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template mode="string-set" match="css:string-set" as="element(css:string-set)">
        <xsl:copy>
            <xsl:sequence select="@name"/>
            <xsl:variable name="evaluated-content" as="xs:string*">
                <xsl:apply-templates mode="#current" select="*"/>
            </xsl:variable>
            <css:string value="{string-join($evaluated-content,'')}"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template mode="string-set" match="css:string[@value]" as="xs:string">
        <xsl:value-of select="string(@value)"/>
    </xsl:template>
    
    <xsl:template mode="string-set"
                  match="css:attr|
                         css:text[@target-attribute]|
                         css:string[@name][@target-attribute]|
                         css:counter[@target-attribute]|
                         css:content[@target-attribute]">
        <xsl:message terminate="yes">Coding error: evaluation of attr() should already have been done</xsl:message>
    </xsl:template>
    
    <xsl:template mode="string-set" match="css:content[not(@target|@target-attribute)]" as="xs:string">
        <xsl:message terminate="yes">Coding error: evaluation of content() should already have been done</xsl:message>
    </xsl:template>
    
    <xsl:template mode="string-set" match="*">
        <xsl:message terminate="yes">Coding error</xsl:message>
    </xsl:template>
    
</xsl:stylesheet>
