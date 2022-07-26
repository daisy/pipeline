<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all">
    
    <xsl:include href="library.xsl"/>
    
    <xsl:param name="counter-names"/>
    <xsl:param name="exclude-counter-names"/>
    <xsl:param name="counter-styles" as="map(xs:string,element(css:counter-style))"/>
    
    <xsl:variable name="counter-names-list" as="xs:string*" select="tokenize(normalize-space($counter-names), ' ')"/>
    <xsl:variable name="exclude-counter-names-list" as="xs:string*" select="tokenize(normalize-space($exclude-counter-names), ' ')"/>
    
    <xsl:variable name="context" select="collection()[2]"/>
    
    <!--
        override function defined in braille-css.xsl
    -->
    <xsl:function name="css:custom-counter-style" as="element(css:counter-style)?">
        <xsl:param name="name" as="xs:string"/>
        <xsl:sequence select="map:get($counter-styles,$name)"/>
    </xsl:function>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="css:counter">
        <xsl:if test="@css:white-space">
            <xsl:message select="concat('white-space:',@css:white-space,' could not be applied to ',
                                        (if (@target) then 'target-counter' else 'counter'),'(',@name,')')"/>
        </xsl:if>
        <xsl:choose>
            <xsl:when test="if ($counter-names='#all')
                            then not(@name=$exclude-counter-names-list)
                            else @name=$counter-names-list">
                <xsl:variable name="style" as="xs:string" select="(@style,'decimal')[1]"/>
                <xsl:variable name="text-with-text-transform" as="xs:string*">
                    <xsl:variable name="target" as="element()?">
                        <xsl:choose>
                            <xsl:when test="@target">
                                <xsl:variable name="id" as="xs:string" select="@target"/>
                                <xsl:sequence select="$context//*[@css:id=$id]"/>
                            </xsl:when>
                            <xsl:when test="/*/@css:flow[not(.='normal')]">
                                <xsl:variable name="id" as="xs:string" select="ancestor::*/@css:anchor"/>
                                <xsl:sequence select="$context//*[@css:id=$id]"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:variable name="id" as="xs:string" select="@xml:id"/>
                                <xsl:sequence select="$context//*[@xml:id=$id]"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:if test="$target">
                        <xsl:call-template name="css:counter">
                            <xsl:with-param name="name" select="@name"/>
                            <xsl:with-param name="style" select="$style"/>
                            <xsl:with-param name="context" select="$target"/>
                            <xsl:with-param name="default-marker-contents"
                                            select="exists($target/ancestor::*[self::css:marker or @name='css:marker'])"/>
                        </xsl:call-template>
                    </xsl:if>
                </xsl:variable>
                <xsl:if test="exists($text-with-text-transform)">
                    <xsl:variable name="text" as="xs:string" select="$text-with-text-transform[1]"/>
                    <xsl:variable name="text-transform" as="xs:string" select="($text-with-text-transform[2],'auto')[1]"/>
                    <css:box type="inline" css:text-transform="{$text-transform}">
                        <xsl:if test="@target">
                            <xsl:attribute name="css:anchor" select="@target"/>
                        </xsl:if>
                        <xsl:value-of select="$text"/>
                    </css:box>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="@css:*[matches(local-name(),'^counter-(set|reset|increment)-.*$')]">
        <xsl:variable name="name" as="xs:string"
                      select="replace(local-name(),'^counter-(set|reset|increment)-(.*)$','$2')"/>
        <xsl:if test="if ($counter-names='#all')
                      then $name=$exclude-counter-names-list
                      else not($name=$counter-names-list)">
            <xsl:next-match/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="@xml:id[starts-with(.,'__temp__')]"/>
    
</xsl:stylesheet>
