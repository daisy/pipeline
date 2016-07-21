<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:include href="library.xsl"/>
    
    <xsl:param name="property-names"/>
    <xsl:variable name="property-names-list" as="xs:string*" select="tokenize(normalize-space($property-names), ' ')"/>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="css:counter/@style">
        <xsl:sequence select="."/>
    </xsl:template>
    
    <xsl:template match="@style">
        <xsl:variable name="rules" as="element()*" select="css:parse-stylesheet(.)"/>
        <xsl:variable name="properties" as="element()*"
                      select="$rules[not(@selector)]/css:parse-declaration-list(@style)"/>
        <!--
            filter
        -->
        <xsl:variable name="rules" as="element()*">
            <xsl:sequence select="$rules[@selector]"/>
            <xsl:if test="not($property-names='#all')">
                <xsl:variable name="properties" as="element()*" select="$properties[not(@name=$property-names-list)]"/>
                <xsl:if test="exists($properties)">
                    <css:rule style="{css:serialize-declaration-list($properties)}"/>
                </xsl:if>
            </xsl:if>
        </xsl:variable>
        <xsl:if test="exists($rules)">
            <xsl:attribute name="style" select="css:serialize-stylesheet($rules)"/>
        </xsl:if>
        <xsl:variable name="properties" as="element()*"
                      select="for $n in distinct-values(if ($property-names='#all')
                                                        then $properties/@name
                                                        else $property-names-list)
                              return $properties[@name=$n][last()]"/>
        <!--
            validate
        -->
        <xsl:variable name="properties" as="element()*">
            <xsl:apply-templates select="$properties" mode="css:validate"/>
        </xsl:variable>
        <!--
            inherit
        -->
        <xsl:variable name="properties" as="element()*">
            <xsl:apply-templates select="$properties" mode="css:inherit">
                <xsl:with-param name="validate" select="true()"/>
                <xsl:with-param name="context" select="parent::*"/>
            </xsl:apply-templates>
        </xsl:variable>
        <!--
            default
        -->
        <xsl:variable name="properties" as="element()*">
            <xsl:apply-templates select="$properties" mode="css:default"/>
        </xsl:variable>
        <!--
            computed value
        -->
        <xsl:variable name="properties" as="element()*">
            <xsl:apply-templates select="$properties" mode="css:compute">
                <xsl:with-param name="concretize-inherit" select="true()"/>
                <xsl:with-param name="concretize-initial" select="true()"/>
                <xsl:with-param name="validate" select="true()"/>
                <xsl:with-param name="context" select="parent::*"/>
            </xsl:apply-templates>
        </xsl:variable>
        <!--
            make attributes
        -->
        <xsl:apply-templates select="$properties" mode="css:property-as-attribute"/>
    </xsl:template>
    
</xsl:stylesheet>
