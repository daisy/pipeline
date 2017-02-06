<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:f="#"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:template match="/*">
        <!-- first pass: move markers -->
        <xsl:variable name="move-markers" as="document-node()">
            <xsl:document>
                <xsl:call-template name="move-markers">
                    <xsl:with-param name="forward" select="()" as="node()*"/>
                    <xsl:with-param name="siblings" select="()" as="element()*"/>
                </xsl:call-template>
            </xsl:document>
        </xsl:variable>
        
        <!-- second pass: remove @pxi:forward attributes -->
        <xsl:apply-templates select="$move-markers" mode="remove-pxi"/>
    </xsl:template>
    
    <xsl:template match="@* | node()" mode="remove-pxi">
        <xsl:copy>
            <xsl:apply-templates select="@* except @pxi:*" mode="#current"/>
            <xsl:apply-templates select="node()" mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template name="move-markers" as="node()*">
        <xsl:param name="forward" as="node()*"/>
        <xsl:param name="siblings" as="element()*" required="yes"/>
        
        <!-- determine if we should forward or keep the marker(s) -->
        <xsl:variable name="should-forward" select="not(ancestor-or-self::css:box/@type='inline')"/>
        <xsl:variable name="forward" select="if ($should-forward) then ($forward, .) else $forward" as="node()*"/>
        
        <!-- determine if we should create/update a marker attribute -->
        <xsl:variable name="marker" select="if (self::css:box[@type='inline']) then string-join(for $f in $forward return $f/(@css:_obfl-marker | @pxi:forward)/tokenize(., '\s+'),' ') else ''"/>
        <xsl:variable name="forward" select="if ($marker) then () else $forward" as="node()*"/>
        
        <!-- recursively handle first half of descendants -->
        <xsl:variable name="descendants-1" select="*[position() &lt; last() div 2]" as="element()*"/>
        <xsl:variable name="descendants-1" as="node()*">
            <xsl:choose>
                <xsl:when test="$descendants-1">
                    <xsl:for-each select="$descendants-1[1]">
                        <xsl:call-template name="move-markers">
                            <xsl:with-param name="forward" select="$forward" as="node()*"/>
                            <xsl:with-param name="siblings" select="$descendants-1[position() &gt; 1]" as="element()*"/>
                        </xsl:call-template>
                    </xsl:for-each>
                </xsl:when>
                <xsl:when test="not(*)">
                    <xsl:copy-of select="node()"/>
                </xsl:when>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="forward" select="if ($descendants-1[self::*]) then $descendants-1[self::*][last()] else $forward" as="node()*"/>
        
        <!-- recursively handle second half of descendants -->
        <xsl:variable name="descendants-2" select="*[position() &gt;= last() div 2]"/>
        <xsl:variable name="descendants-2" as="node()*">
            <xsl:if test="$descendants-2">
                <xsl:for-each select="$descendants-2[1]">
                    <xsl:call-template name="move-markers">
                        <xsl:with-param name="forward" select="$forward" as="node()*"/>
                        <xsl:with-param name="siblings" select="$descendants-2[position() &gt; 1]" as="element()*"/>
                    </xsl:call-template>
                </xsl:for-each>
            </xsl:if>
        </xsl:variable>
        <xsl:variable name="forward" select="if ($descendants-2[self::*]) then $descendants-2[self::*][last()] else $forward" as="node()*"/>
        
        <!-- recursively handle first half of following siblings -->
        <xsl:variable name="siblings-1" select="$siblings[position() &lt; round(last() div 2)]"/>
        <xsl:variable name="siblings-1" as="node()*">
            <xsl:if test="$siblings-1">
                <xsl:for-each select="$siblings-1[1]">
                    <xsl:call-template name="move-markers">
                        <xsl:with-param name="forward" select="$forward" as="node()*"/>
                        <xsl:with-param name="siblings" select="$siblings-1[position() &gt; 1]" as="element()*"/>
                    </xsl:call-template>
                </xsl:for-each>
            </xsl:if>
        </xsl:variable>
        <xsl:variable name="forward" select="if ($siblings-1[self::*]) then $siblings-1[self::*][last()] else $forward" as="node()*"/>
        
        <!-- recursively handle second half of following siblings -->
        <xsl:variable name="siblings-2" select="$siblings[position() &gt;= round(last() div 2)]"/>
        <xsl:variable name="siblings-2" as="node()*">
            <xsl:if test="$siblings-2">
                <xsl:for-each select="$siblings-2[1]">
                    <xsl:call-template name="move-markers">
                        <xsl:with-param name="forward" select="$forward" as="node()*"/>
                        <xsl:with-param name="siblings" select="$siblings-2[position() &gt; 1]" as="element()*"/>
                    </xsl:call-template>
                </xsl:for-each>
            </xsl:if>
        </xsl:variable>
        <xsl:variable name="forward" select="if ($siblings-2[self::*]) then $siblings-2[self::*][last()] else $forward" as="node()*"/>
        
        <!-- preceding non-element nodes -->
        <xsl:if test="not(preceding-sibling::*)">
            <xsl:copy-of select="preceding-sibling::node()"/>
        </xsl:if>
        
        <xsl:copy>
            <xsl:copy-of select="@* except @css:_obfl-marker"/>
            
            <!-- marker -->
            <xsl:choose>
                <xsl:when test="$marker">
                    <xsl:attribute name="css:_obfl-marker" select="$marker"/>
                </xsl:when>
                <xsl:when test="not($should-forward)">
                    <xsl:copy-of select="@css:_obfl-marker"/>
                </xsl:when>
            </xsl:choose>
            
            <!-- forward markers to the parent or next sibling -->
            <xsl:if test="count($forward)">
                <xsl:attribute name="pxi:forward" select="string-join(for $f in $forward return $f/(@css:_obfl-marker | @pxi:forward)/tokenize(., '\s+'),' ')"/>
            </xsl:if>
            
            <!-- descendant nodes -->
            <xsl:copy-of select="$descendants-1"/>
            <xsl:copy-of select="$descendants-2"/>
            
        </xsl:copy>
        
        <!-- trailing non-element nodes -->
        <xsl:choose>
            <xsl:when test="following-sibling::*">
                <xsl:copy-of select="following-sibling::node() intersect following-sibling::*[1]/preceding-sibling::node()"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="following-sibling::node()"/>
            </xsl:otherwise>
        </xsl:choose>
        
        <!-- sibling nodes -->
        <xsl:copy-of select="$siblings-1"/>
        <xsl:copy-of select="$siblings-2"/>
        
    </xsl:template>
    
    <!-- ========== Useful for debugging ========== -->
    
    <xsl:function name="f:path">
        <xsl:param name="node" as="node()"/>
        <xsl:value-of select="string-join(for $n in $node/(ancestor::* | .) return concat((if ($n intersect $n/../@*) then concat('@',$n/name()) else if ($n/self::*) then $n/name() else if ($n/self::text()) then 'text()' else 'node()'), '[', count($n/preceding-sibling::node()) + 1, ']'), '/')"/>
    </xsl:function>
    
</xsl:stylesheet>
