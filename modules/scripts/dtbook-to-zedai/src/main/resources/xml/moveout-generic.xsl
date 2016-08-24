<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:f="http://www.daisy.org/ns/pipeline/internal-function"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all" version="2.0">
    
    <xsl:output indent="yes" method="xml"/>
    
    <xsl:template match="/*" priority="100">
        <xsl:apply-templates select="." mode="moveout"/>
    </xsl:template>
    
    <xsl:template match="*[not(f:is-valid-parent(.))]" mode="moveout">
        <xsl:variable name="this" select="."/>
        <!-- depth first processing -->
        <xsl:variable name="children" as="node()*">
            <xsl:apply-templates select="node()[not(self::text()) or normalize-space()]"
                mode="moveout"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="empty($children)">
                <xsl:copy-of select="."/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="first" select="$children[not(f:is-target(.))][1]"/>
                <xsl:for-each-group select="$children" group-adjacent="f:is-target(.)">
                    <xsl:choose>
                        <xsl:when test="current-grouping-key()">
                            <xsl:message>Moving out <xsl:value-of
                                    select="current-group()/concat(local-name(.),if (@id) then concat('#',@id) else '')"
                                /></xsl:message>
                            <xsl:apply-templates select="current-group()" mode="moveout"/>
                        </xsl:when>
                        <xsl:when
                            test="not(. is $first) and $this/(self::dtb:h1|self::dtb:h2|self::dtb:h3|self::dtb:h4|self::dtb:h5|self::dtb:h5|self::dtb:h6|self::dtb:h)">
                            <dtb:p>
                                <xsl:apply-templates select="$this/(@* except @id)"/>
                                <xsl:apply-templates select="current-group()" mode="moveout"/>
                            </dtb:p>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:element name="{name($this)}"
                                namespace="{namespace-uri($this)}">
                                <xsl:copy-of select="$this/namespace::*"/>
                                <xsl:apply-templates
                                    select="$this/(if (current() is $first) then @* else @* except @id)"/>
                                <xsl:apply-templates select="current-group()" mode="moveout"/>
                            </xsl:element>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each-group>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="node() | @*" mode="#all">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*" mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
    
    <!--<xsl:function name="f:is-target" as="xs:boolean">
        <xsl:param name="elem" as="item()*"/>
        <!-\-TO BE OVERRIDEN-\->
        <xsl:sequence select="false()"/>
    </xsl:function>-->
    <!--<xsl:function name="f:is-valid-parent" as="xs:boolean">
        <xsl:param name="elem" as="element()"/>
        <!-\-TO BE OVERRIDEN-\->
        <xsl:sequence select="true()"/>
    </xsl:function>-->
    
</xsl:stylesheet>
