<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:f="f"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/generate-id.xsl"/>
    <xsl:include href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
    
    <xsl:key name="url-1" match="*[@id]" use="pf:normalize-uri(resolve-uri(concat('#',encode-for-uri(@id)),base-uri(.)))"/>
    <xsl:key name="url-2" match="*[@xml:id]" use="pf:normalize-uri(resolve-uri(concat('#',@xml:id),base-uri(.)))"/>
    
    <xsl:function name="f:get-target" as="element()?">
        <xsl:param name="target" as="xs:string"/> <!-- resolved and normalized URI -->
        <xsl:variable name="target" as="element()*" select="(key('url-1',$target,collection()[1]),
                                                             key('url-2',$target,collection()[1]))"/>
        <xsl:sequence select="($target)[1]"/>
    </xsl:function>
    
    <xsl:variable name="referenced-elements" as="xs:string*">
        <xsl:variable name="targets" as="xs:string*">
            <xsl:for-each select="//css:text[@target]|
                                  //css:string[@name][@target]|
                                  //css:counter[@target]|
                                  //css:content[@target]">
                <xsl:variable name="target" as="xs:string" select="@target"/>
                <xsl:variable name="target" as="xs:anyURI" select="resolve-uri($target,base-uri(.))"/>
                <xsl:variable name="target" as="xs:string" select="pf:normalize-uri($target)"/>
                <xsl:sequence select="$target"/>
            </xsl:for-each>
        </xsl:variable>
        <xsl:sequence select="distinct-values($targets)"/>
    </xsl:variable>
    
    <xsl:template match="/*" priority="1">
        <xsl:variable name="referenced-elements" as="element()*">
            <xsl:for-each select="$referenced-elements">
                <xsl:sequence select="f:get-target(.)"/>
            </xsl:for-each>
        </xsl:variable>
        <xsl:call-template name="pf:next-match-with-generated-ids">
            <xsl:with-param name="prefix" select="'target_'"/>
            <xsl:with-param name="for-elements" select="$referenced-elements"/>
            <xsl:with-param name="in-use" select="//@css:id"/>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[@xml:id or @id][not(@css:id)]">
        <xsl:variable name="uri" as="xs:string*"
                      select="(@id/pf:normalize-uri(resolve-uri(concat('#',encode-for-uri(.)),base-uri(..))),
                               @xml:id/pf:normalize-uri(resolve-uri(concat('#',.),base-uri(..))))"/>
        <xsl:choose>
            <xsl:when test="$referenced-elements[.=$uri and f:get-target(.) is current()]">
                <xsl:copy>
                    <xsl:apply-templates select="@*"/>
                    <xsl:variable name="generated-id" as="xs:string">
                        <xsl:call-template name="pf:generate-id"/>
                    </xsl:variable>
                    <xsl:attribute name="css:_id_" select="$generated-id"/>
                    <xsl:apply-templates select="node()"/>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="css:text[@target]|
                         css:string[@name][@target]|
                         css:counter[@target]|
                         css:content[@target]">
        <xsl:variable name="uri" as="xs:string"
                      select="pf:normalize-uri(resolve-uri(if (contains(@target,'#')) then @target else concat('#',@target),base-uri(.)))"/>
        <xsl:variable name="target" as="element()?" select="f:get-target($uri)"/>
        <xsl:choose>
            <xsl:when test="exists($target)">
                <xsl:copy>
                    <xsl:sequence select="@* except @target"/>
                    <xsl:variable name="target" as="xs:string">
                        <xsl:choose>
                            <xsl:when test="$target/@css:id">
                                <xsl:sequence select="$target/@css:id"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:for-each select="$target">
                                    <xsl:call-template name="pf:generate-id"/>
                                </xsl:for-each>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:attribute name="target" select="$target"/>
                    <xsl:attribute name="original-target" select="@target"/>
                    <xsl:sequence select="node()"/>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message>
                    <xsl:text>target-</xsl:text>
                    <xsl:value-of select="local-name(.)"/>
                    <xsl:text>(</xsl:text>
                    <xsl:if test="self::css:string">
                        <xsl:value-of select="@name"/>
                        <xsl:text>, </xsl:text>
                    </xsl:if>
                    <xsl:value-of select="@target"/>
                    <xsl:text>) could not be resolved.</xsl:text>
                </xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
</xsl:stylesheet>
