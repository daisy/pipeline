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
                <xsl:variable name="evaluated-string-set-pairs" as="element()*">
                    <xsl:apply-templates select="css:parse-string-set(@css:string-set)" mode="eval-string-set-pair">
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
    
    <xsl:template match="css:string-set" mode="eval-string-set-pair" as="element()">
        <xsl:copy>
            <xsl:sequence select="@name"/>
            <xsl:variable name="evaluated-content" as="xs:string*">
                <xsl:apply-templates select="css:parse-content-list(@value, ())" mode="eval-content-list"/>
            </xsl:variable>
            <xsl:attribute name="value"
                           select="concat('&quot;',
                                          replace(replace(
                                            string-join($evaluated-content,''),
                                            '\n','\\A '),
                                            '&quot;','\\22 '),
                                          '&quot;')"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="css:string[@value]" mode="eval-content-list" as="xs:string">
        <xsl:value-of select="string(@value)"/>
    </xsl:template>
    
    <xsl:template match="css:attr" mode="eval-content-list" as="xs:string">
        <xsl:param name="context" as="element()" tunnel="yes"/>
        <xsl:variable name="name" select="string(@name)"/>
        <xsl:value-of select="string($context/@*[name()=$name])"/>
    </xsl:template>
    
    <xsl:template match="css:content[not(@target)]" mode="eval-content-list" as="xs:string">
        <xsl:param name="context" as="element()" tunnel="yes"/>
        <xsl:sequence select="string($context)"/>
    </xsl:template>
    
    <xsl:template match="css:string[@name][not(@target)]" mode="eval-content-list">
        <xsl:message>string() function not supported in string-set property</xsl:message>
    </xsl:template>
    
    <xsl:template match="css:counter[not(@target)]" mode="eval-content-list">
        <xsl:message>counter() function not supported in string-set property</xsl:message>
    </xsl:template>
    
    <xsl:template match="css:text[@target]" mode="eval-content-list">
        <xsl:message>target-text() function not supported in string-set property</xsl:message>
    </xsl:template>
    
    <xsl:template match="css:string[@name][@target]" mode="eval-content-list">
        <xsl:message>target-string() function not supported in string-set property</xsl:message>
    </xsl:template>
    
    <xsl:template match="css:counter[@target]" mode="eval-content-list">
        <xsl:message>target-counter() function not supported in string-set property</xsl:message>
    </xsl:template>
    
    <xsl:template match="css:content[@target]" mode="eval-content-list">
        <xsl:message>target-content() function not supported in string-set property</xsl:message>
    </xsl:template>
    
    <xsl:template match="css:leader" mode="eval-content-list">
        <xsl:message>leader() function not supported in string-set property</xsl:message>
    </xsl:template>
    
    <xsl:template match="css:flow[@from]" mode="eval-content-list">
        <xsl:message>flow() function not supported in string-set property</xsl:message>
    </xsl:template>
    
    <xsl:template match="css:custom-func" mode="eval-content-list">
        <xsl:message><xsl:value-of select="@name"/>() function not supported in string-set property</xsl:message>
    </xsl:template>
    
    <xsl:template match="*" mode="eval-content-list">
        <xsl:message terminate="yes">Coding error</xsl:message>
    </xsl:template>
    
</xsl:stylesheet>
