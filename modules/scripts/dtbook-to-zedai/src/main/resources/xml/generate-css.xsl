<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    version="2.0"
    xmlns:rend="http://www.daisy.org/ns/z3998/authoring/features/rend/"
    xmlns:its="http://www.w3.org/2005/11/its" 
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:z="http://www.daisy.org/ns/z3998/authoring/"
    xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp"
    exclude-result-prefixes="xs rend its xlink z tmp">
    
    <xsl:output method="text"/>
    
    <xsl:template match="/">
        <xsl:message>Generating CSS</xsl:message>
        <xsl:apply-templates select="//z:object"/>
        <xsl:apply-templates select="//z:table"/>
        <xsl:apply-templates select="//z:col"/>
        <xsl:apply-templates select="//z:colgroup"/>
        <xsl:apply-templates select="//z:th"/>
        <xsl:apply-templates select="//z:td"/>
        <xsl:apply-templates select="//z:tr"/>
        <xsl:apply-templates select="//z:thead"/>
        <xsl:apply-templates select="//z:tbody"/>
        <xsl:apply-templates select="//z:tfoot"/>
    </xsl:template>
    
    <xsl:template match="z:object">
        <xsl:if test="@tmp:height or @tmp:width">
            #<xsl:value-of select="@xml:id"/>{
                <xsl:if test="@tmp:height">
                    height: <xsl:value-of select="@tmp:height"/>;
                </xsl:if>
                <xsl:if test="@tmp:width">
                    width: <xsl:value-of select="@tmp:height"/>;
                </xsl:if>
            }
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="z:table">
        <xsl:if test="@tmp:width or @tmp:border or @tmp:cellspacing or @tmp:cellpadding">
            #<xsl:value-of select="@xml:id"/>{
            <xsl:if test="@tmp:width">
                width: <xsl:value-of select="@tmp:width"/>;
            </xsl:if>
            <xsl:if test="@tmp:border">
                border: <xsl:value-of select="@tmp:border"/>;
            </xsl:if>
            <xsl:if test="@tmp:cellspacing">
                cellspacing: <xsl:value-of select="@tmp:cellspacing"/>;
            </xsl:if>
            <xsl:if test="@tmp:cellpadding">
                cellpadding: <xsl:value-of select="@tmp:cellpadding"/>;
            </xsl:if>
            }
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="z:col | z:colgroup">
        <xsl:if test="@tmp:width or @tmp:align or @tmp:valign">
            #<xsl:value-of select="@xml:id"/>{
            <xsl:if test="@tmp:width">
                width: <xsl:value-of select="@tmp:width"/>;
            </xsl:if>
            <xsl:if test="@tmp:align">
                align: <xsl:value-of select="@tmp:align"/>;
            </xsl:if>
            <xsl:if test="@tmp:valign">
                valign: <xsl:value-of select="@tmp:valign"/>;
            </xsl:if>
            }
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="z:th | z:td | z:tr | z:tbody | z:tfoot | z:thead">
        <xsl:if test="@tmp:align or @tmp:valign">
            #<xsl:value-of select="@xml:id"/>{
            <xsl:if test="@tmp:align">
                align: <xsl:value-of select="@tmp:align"/>;
            </xsl:if>
            <xsl:if test="@tmp:valign">
                valign: <xsl:value-of select="@tmp:valign"/>;
            </xsl:if>
            }
        </xsl:if>
    </xsl:template>
    
    
</xsl:stylesheet>
