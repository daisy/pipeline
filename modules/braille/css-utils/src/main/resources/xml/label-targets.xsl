<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[@xml:id or @id]">
        <xsl:variable name="uri" as="xs:anyURI*" select="for $i in (@xml:id|@id) return resolve-uri(concat('#',$i),base-uri(.))"/>
        <xsl:variable name="referenced" as="element()*">
            <xsl:variable name="id" as="xs:string*" select="(@xml:id|@id)"/>
            <xsl:for-each select="//*[self::css:text[@target] or
                                      self::css:string[@name][@target] or
                                      self::css:counter[@target] or
                                      self::css:content[@target]]
                                     [replace(@target,'^.*#','')=$id]">
                <xsl:if test="resolve-uri(if (contains(@target,'#')) then @target else concat('#',@target),base-uri(.))=$uri">
                    <xsl:sequence select="."/>
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="exists($referenced)">
                <xsl:copy>
                    <xsl:apply-templates select="@*"/>
                    <xsl:if test="not(@css:id)">
                        <xsl:attribute name="css:_id_" select="generate-id(.)"/>
                    </xsl:if>
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
        <xsl:variable name="uri" as="xs:anyURI"
                      select="resolve-uri(if (contains(@target,'#')) then @target else concat('#',@target),base-uri(.))"/>
        <xsl:variable name="target" as="element()*">
            <xsl:variable name="id" as="xs:string" select="replace(@target,'^.*#','')"/>
            <xsl:for-each select="//*[@xml:id=$id or @id=$id]">
                <xsl:if test="resolve-uri(concat('#',$id),base-uri(.))=$uri">
                    <xsl:sequence select="."/>
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="target" as="element()?" select="$target[1]"/> <!-- first occurence wins -->
        <xsl:choose>
            <xsl:when test="exists($target)">
                <xsl:copy>
                    <xsl:sequence select="@* except @target"/>
                    <xsl:attribute name="target" select="($target/@css:id,generate-id($target))[1]"/>
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
    
    <!--
        Suppress warning messages "The source document is in no namespace, but the template rules
        all expect elements in a namespace" (see https://github.com/daisy/pipeline-mod-braille/issues/38)
    -->
    <xsl:template match="/phony">
        <xsl:next-match/>
    </xsl:template>
    
</xsl:stylesheet>
