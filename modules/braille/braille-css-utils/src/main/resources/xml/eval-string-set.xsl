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
    
    <xsl:template match="*[@css:string-set]">
        <xsl:copy>
            <xsl:if test="@css:string-set!='none'">
                <xsl:variable name="evaluated-string-set-pairs" as="element(css:string-set)*">
                    <xsl:apply-templates mode="string-set" select="css:parse-string-set(@css:string-set,.)">
                        <xsl:with-param name="context" select="." tunnel="yes"/>
                    </xsl:apply-templates>
                </xsl:variable>
                <xsl:attribute name="css:string-set"
                               select="css:serialize-string-set($evaluated-string-set-pairs)"/>
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
    
    <xsl:template mode="string-set" match="css:attr" as="xs:string">
        <xsl:param name="context" as="element()" tunnel="yes"/>
        <xsl:variable name="name" select="string(@name)"/>
        <xsl:value-of select="string($context/@*[name()=$name])"/>
    </xsl:template>
    
    <xsl:template mode="string-set" match="css:content[not(@target|@target-attribute)]" as="xs:string">
        <xsl:param name="context" as="element()" tunnel="yes"/>
        <xsl:sequence select="string($context)"/>
    </xsl:template>
    
    <xsl:template mode="string-set" match="css:string[@name][not(@target|@target-attribute)]">
        <xsl:message>string() function not supported in string-set property</xsl:message>
    </xsl:template>
    
    <xsl:template mode="string-set" match="css:counter[not(@target|@target-attribute)]">
        <xsl:message>counter() function not supported in string-set property</xsl:message>
    </xsl:template>
    
    <xsl:template mode="string-set" match="css:text[@target]">
        <xsl:message>target-text() function not supported in string-set property</xsl:message>
    </xsl:template>
    
    <xsl:template mode="string-set" match="css:string[@name][@target]">
        <xsl:message>target-string() function not supported in string-set property</xsl:message>
    </xsl:template>
    
    <xsl:template mode="string-set" match="css:counter[@target]">
        <xsl:message>target-counter() function not supported in string-set property</xsl:message>
    </xsl:template>
    
    <xsl:template mode="string-set" match="css:content[@target]">
        <xsl:message>target-content() function not supported in string-set property</xsl:message>
    </xsl:template>
    
    <xsl:template mode="string-set" match="css:leader">
        <xsl:message>leader() function not supported in string-set property</xsl:message>
    </xsl:template>
    
    <xsl:template mode="string-set" match="css:flow[@from]">
        <xsl:message>flow() function not supported in string-set property</xsl:message>
    </xsl:template>
    
    <xsl:template mode="string-set" match="css:custom-func">
        <xsl:message><xsl:value-of select="@name"/>() function not supported in string-set property</xsl:message>
    </xsl:template>
    
    <xsl:template mode="string-set"
                  match="css:text[@target-attribute]|
                         css:string[@name][@target-attribute]|
                         css:counter[@target-attribute]|
                         css:content[@target-attribute]">
        <xsl:message terminate="yes">Coding error: evaluation of attr() should already have been done</xsl:message>
    </xsl:template>
    
    <xsl:template mode="string-set" match="*">
        <xsl:message terminate="yes">Coding error</xsl:message>
    </xsl:template>
    
</xsl:stylesheet>
