<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
    xmlns:css="http://www.daisy.org/ns/pipeline/braille-css" exclude-result-prefixes="#all" version="2.0" xmlns:f="#">

    <xsl:template match="/*">
        <xsl:variable name="label-split-points" as="document-node()">
            <xsl:document>
                <xsl:apply-templates select="." mode="label-split-points"/>
            </xsl:document>
        </xsl:variable>
        
        <_>
            <xsl:variable name="parts" as="node()*">
                <xsl:for-each select="$label-split-points/*">
                    <xsl:call-template name="split">
                        <xsl:with-param name="forward" select="()"/>
                        <xsl:with-param name="siblings" select="()"/>
                    </xsl:call-template>
                </xsl:for-each>
            </xsl:variable>
            <xsl:sequence select="for $p in $parts return $p[self::pxi:part]/*[self::css:box or count(*)]"/>
        </_>
    </xsl:template>
    
    <!-- ========== Find Parts ========== -->
    
    <xsl:template name="split" as="node()*">
        <xsl:param name="forward" as="node()*"/>
        <xsl:param name="siblings" as="node()*" required="yes"/>
        
        <!-- split nodes before current node -->
        <xsl:if test="@pxi:split-before and count($forward)">
            <xsl:call-template name="create-part">
                <xsl:with-param name="nodes" select="$forward"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:variable name="forward" select="if (@pxi:split-before) then () else $forward"/>
        
        <!-- add current node to current part -->
        <xsl:variable name="forward" select="$forward | ."/>
        
        <!-- recursively handle first half of descendants -->
        <xsl:variable name="descendants-1" select="node()[position() &lt; round(last() div 2)]"/>
        <xsl:variable name="descendants-1" as="node()*">
            <xsl:choose>
                <xsl:when test="$descendants-1">
                    <xsl:for-each select="$descendants-1[1]">
                        <xsl:call-template name="split">
                            <xsl:with-param name="forward" select="$forward"/>
                            <xsl:with-param name="siblings" select="$descendants-1[position() &gt; 1]"/>
                        </xsl:call-template>
                    </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="$forward"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:sequence select="$descendants-1[self::pxi:part]"/>
        <xsl:variable name="forward" select="$descendants-1[not(self::pxi:part)]"/>
        
        <!-- recursively handle second half of descendants -->
        <xsl:variable name="descendants-2" select="node()[position() &gt;= round(last() div 2)]"/>
        <xsl:variable name="descendants-2" as="node()*">
            <xsl:choose>
                <xsl:when test="$descendants-2">
                    <xsl:for-each select="$descendants-2[1]">
                        <xsl:call-template name="split">
                            <xsl:with-param name="forward" select="$forward"/>
                            <xsl:with-param name="siblings" select="$descendants-2[position() &gt; 1]"/>
                        </xsl:call-template>
                    </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="$forward"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:sequence select="$descendants-2[self::pxi:part]"/>
        <xsl:variable name="forward" select="$descendants-2[not(self::pxi:part)]"/>
        
        <!-- split nodes after current node -->
        <xsl:if test="@pxi:split-after and count($forward)">
            <xsl:call-template name="create-part">
                <xsl:with-param name="nodes" select="$forward"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:variable name="forward" select="if (@pxi:split-after) then () else $forward"/>
        
        <!-- recursively handle first half of following siblings -->
        <xsl:variable name="siblings-1" select="$siblings[position() &lt; round(last() div 2)]"/>
        <xsl:variable name="siblings-2" select="$siblings[position() &gt;= round(last() div 2)]"/>
        <xsl:variable name="siblings-1" as="node()*">
            <xsl:choose>
                <xsl:when test="$siblings-1">
                    <xsl:for-each select="$siblings-1[1]">
                        <xsl:call-template name="split">
                            <xsl:with-param name="forward" select="$forward"/>
                            <xsl:with-param name="siblings" select="$siblings-1[position() &gt; 1]"/>
                        </xsl:call-template>
                    </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="$forward"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:sequence select="$siblings-1[self::pxi:part]"/>
        <xsl:variable name="forward" select="$siblings-1[not(self::pxi:part)]"/>
        
        <!-- recursively handle second half of following siblings -->
        <xsl:variable name="siblings-2" select="$siblings[position() &gt;= round(last() div 2)]"/>
        <xsl:variable name="siblings-2" as="node()*">
            <xsl:choose>
                <xsl:when test="$siblings-2">
                    <xsl:for-each select="$siblings-2[1]">
                        <xsl:call-template name="split">
                            <xsl:with-param name="forward" select="$forward"/>
                            <xsl:with-param name="siblings" select="$siblings-2[position() &gt; 1]"/>
                        </xsl:call-template>
                    </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="$forward"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:sequence select="$siblings-2[self::pxi:part]"/>
        <xsl:variable name="forward" select="$siblings-2[not(self::pxi:part)]"/>
        
        <!-- forward remaining nodes to parent or wrap as part if there's no parent (trailing nodes at end of document) -->
        <xsl:choose>
            <xsl:when test="parent::*">
                <xsl:sequence select="$forward"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="count($forward)">
                    <xsl:call-template name="create-part">
                        <xsl:with-param name="nodes" select="$forward"/>
                    </xsl:call-template>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
        
    </xsl:template>
    
    <!-- ========== Create Part ========== -->
    
    <xsl:template name="create-part">
        <xsl:param name="nodes" as="node()*"/>
        <pxi:part>
            <xsl:apply-templates select="/*" mode="create-part">
                <xsl:with-param name="include" select="$nodes"/>
                <xsl:with-param name="include-with-ancestors" select="$nodes/ancestor-or-self::node()"/>
            </xsl:apply-templates>
        </pxi:part>
    </xsl:template>
    
    <xsl:template match="node()" mode="create-part">
        <xsl:param name="include" required="yes" as="node()*"/>
        <xsl:param name="include-with-ancestors" required="yes" as="node()*"/>
        <xsl:copy>
            <xsl:if test=". intersect $include">
                <xsl:copy-of select="@*:id"/>
            </xsl:if>
            <xsl:choose>
                <xsl:when test="count(self::css:box) and count(descendant-or-self::node() except $include) and not(ancestor::css:box[descendant-or-self::node() except $include])">
                    <!-- only add @part if the css:box is being split, except if it's a descendant of another box being split -->
                    <xsl:variable name="part" select="if (. intersect $include) then 'first' else if (descendant::node()[last()] intersect $include) then 'last' else 'middle'"/>
                    <xsl:attribute name="part" select="$part"/>
                    <xsl:choose>
                        <xsl:when test="$part = 'first'">
                            <xsl:copy-of select="@* except (@*:id | @pxi:*)"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:copy-of select="@* except (@css:*[matches(local-name(),'^counter-(reset|set|increment).*$')] |
                                                            @css:string-entry |
                                                            @css:string-set |
                                                            @*:id | @pxi:*)"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:copy-of select="@* except (@*:id | @pxi:*)"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:apply-templates select="node()[. intersect $include-with-ancestors]" mode="#current">
                <xsl:with-param name="include" select="$include"/>
                <xsl:with-param name="include-with-ancestors" select="$include-with-ancestors"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>
    
    <!-- ========== Label Split Points ========== -->
    
    <xsl:template match="@* | node()" mode="label-split-points">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="." mode="split-before"/>
            <xsl:apply-templates select="." mode="split-after"/>
            <xsl:apply-templates select="node()" mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="node() | @*" mode="split-before split-after" priority="0.5"/>
    
    <xsl:template match="*[@css:page or @css:counter-set-page]" name="split-before" mode="split-before" priority="1">
        <xsl:attribute name="pxi:split-before" select="'true'"/>
    </xsl:template>
    
    <xsl:template match="*[@css:page]" name="split-after" mode="split-after" priority="1">
        <xsl:attribute name="pxi:split-after" select="'true'"/>
    </xsl:template>
    
    <!-- ========== Useful for debugging ========== -->

    <xsl:function name="f:path">
        <xsl:param name="node" as="node()"/>
        <xsl:value-of select="string-join(for $n in $node/(ancestor::* | .) return concat((if ($n intersect $n/../@*) then concat('@',$n/name()) else if ($n/self::*) then $n/name() else if ($n/self::text()) then 'text()' else 'node()'), '[', count($n/preceding-sibling::node()) + 1, ']'), '/')"/>
    </xsl:function>
    
</xsl:stylesheet>
