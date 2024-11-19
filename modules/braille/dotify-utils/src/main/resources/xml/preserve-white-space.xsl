<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:template match="*">
        <xsl:copy>
            <xsl:sequence select="@*"/>
            <xsl:call-template name="apply-templates"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[@css:white-space]">
        <xsl:copy>
            <xsl:sequence select="@* except @css:white-space"/>
            <xsl:call-template name="apply-templates">
                <xsl:with-param name="white-space" select="@css:white-space" tunnel="yes"/>
            </xsl:call-template>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="css:string|css:text|css:counter|css:content|css:custom-func" priority="0.6">
        <xsl:param name="white-space" as="xs:string" select="'normal'" tunnel="yes"/>
        <xsl:variable name="white-space" as="xs:string" select="(@css:white-space/string(),$white-space)[1]"/>
        <xsl:copy>
            <xsl:sequence select="@* except @css:white-space"/>
            <xsl:if test="$white-space!='normal'">
                <xsl:attribute name="css:white-space" select="$white-space"/>
            </xsl:if>
            <xsl:sequence select="node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template name="apply-templates">
        <xsl:param name="white-space" as="xs:string" select="'normal'" tunnel="yes"/>
        <xsl:choose>
            <xsl:when test="$white-space=('pre-wrap','pre-line')">
                <xsl:for-each-group select="*|text()" group-adjacent="boolean(self::*)">
                    <xsl:choose>
                        <xsl:when test="current-grouping-key()">
                            <xsl:for-each select="current-group()">
                                <xsl:apply-templates select="."/>
                            </xsl:for-each>
                        </xsl:when>
                        <xsl:when test="string-join(current-group()/string(.),'')=''"/>
                        <xsl:when test="$white-space='pre-wrap'">
                            <xsl:element name="css:white-space">
                                <xsl:sequence select="current-group()"/>
                            </xsl:element>
                        </xsl:when>
                        <xsl:when test="$white-space='pre-line'">
                            <xsl:analyze-string select="string-join(current-group()/string(.),'')" regex="\n+">
                                <xsl:matching-substring>
                                    <xsl:element name="css:white-space">
                                        <xsl:value-of select="."/>
                                    </xsl:element>
                                </xsl:matching-substring>
                                <xsl:non-matching-substring>
                                    <xsl:value-of select="."/>
                                </xsl:non-matching-substring>
                            </xsl:analyze-string>
                        </xsl:when>
                    </xsl:choose>
                </xsl:for-each-group>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="text()">
        <xsl:sequence select="."/>
    </xsl:template>
    
</xsl:stylesheet>
