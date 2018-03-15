<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xslt/internal"
    exclude-result-prefixes="#all"
    version="2.0">
    
    <xsl:template match="/*">
        <!-- first pass: move ids -->
        <xsl:variable name="move-ids" as="document-node()">
            <xsl:document>
                <xsl:call-template name="move-ids"/>
            </xsl:document>
        </xsl:variable>
        
        <!-- second pass: update refs -->
        <xsl:apply-templates select="$move-ids" mode="update-refs"/>
    </xsl:template>
    
    <xsl:template name="move-ids">
        <xsl:param name="forward-id" as="xs:string*"/>
        
        <!-- forward this id if the element is not a box, or one of its ancestors is a inline box -->
        <xsl:variable name="should-forward" select="@css:id and (not(self::css:box) and not(ancestor::css:box[@type='inline']))"/>
        <xsl:variable name="forward-id" select="($forward-id, if ($should-forward and @css:id) then string(@css:id) else ())" as="xs:string*"/>
        
        <!-- calculate id -->
        <xsl:variable name="id" select="if ($should-forward) then '' else if (self::css:box or ancestor::css:box[@type='inline']) then string((@css:id, $forward-id)[1]) else ''"/>
        <xsl:variable name="forward-id" select="$forward-id[not(.=$id)]" as="xs:string*"/>
        
        <!-- store ids that needs to have their references updated -->
        <xsl:variable name="moved-ids" select="if (self::css:box) then string-join($forward-id,' ') else ''"/>
        <xsl:variable name="forward-id" select="if (self::css:box) then () else $forward-id" as="xs:string*"/>
        
        <!-- recursively handle descendants -->
        <xsl:variable name="descendants" as="node()*">
            <xsl:choose>
                <xsl:when test="*">
                    <xsl:for-each select="*[1]">
                        <xsl:call-template name="move-ids">
                            <xsl:with-param name="forward-id" select="$forward-id"/>
                        </xsl:call-template>
                    </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:copy-of select="node()"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="forward-id" select="if (*) then $descendants[self::*][last()]/tokenize(@pxi:forward-ids,' ') else $forward-id"/>
        
        <!-- preceding non-element nodes -->
        <xsl:if test="not(preceding-sibling::*)">
            <xsl:copy-of select="preceding-sibling::node()"/>
        </xsl:if>
        
        <xsl:copy>
            <xsl:copy-of select="@* except @css:id"/>
            
            <!-- id -->
            <xsl:if test="$id">
                <xsl:attribute name="css:id" select="$id"/>
            </xsl:if>
            
            <!-- store old ids so that the new id can be found when updating references -->
            <xsl:if test="$moved-ids">
                <xsl:attribute name="pxi:moved-ids" select="$moved-ids"/>
            </xsl:if>
            
            <!-- if there are no more following siblings to forward ids to; bubble up the ids to the parent -->
            <xsl:if test="count($forward-id) and not(following-sibling::*)">
                <xsl:attribute name="pxi:forward-ids" select="string-join($forward-id,' ')"/>
            </xsl:if>
            
            <!-- descendant nodes -->
            <xsl:copy-of select="$descendants"/>
            
        </xsl:copy>
        
        <!-- trailing non-element nodes -->
        <xsl:copy-of select="following-sibling::node() intersect following-sibling::*[1]/preceding-sibling::node()"/>
        <xsl:if test="not(following-sibling::*)">
            <xsl:copy-of select="following-sibling::node()"/>
        </xsl:if>
        
        <!-- recursively handle following siblings -->
        <xsl:for-each select="following-sibling::*[1]">
            <xsl:call-template name="move-ids">
                <xsl:with-param name="forward-id" select="$forward-id"/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template match="@* | node()" mode="update-refs">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()" mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="css:counter[@name][@target]" mode="update-refs">
        <xsl:variable name="target" select="@target"/>
        <xsl:variable name="target" select="//*[tokenize(@pxi:moved-ids,' ') = $target][1]/@css:id"/>
        
        <xsl:copy>
            <xsl:apply-templates select="@*" mode="#current"/>
            <xsl:if test="$target">
                <xsl:attribute name="target" select="string($target)"/>
            </xsl:if>
            <xsl:apply-templates select="node()" mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="@css:anchor" mode="update-refs">
        <xsl:variable name="anchor" select="."/>
        <xsl:variable name="anchor" select="//*[tokenize(@pxi:moved-ids,' ') = $anchor][1]/@css:id"/>
        
        <xsl:choose>
            <xsl:when test="$anchor">
                <xsl:attribute name="css:anchor" select="string($anchor)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="@css:id[.='']" mode="update-refs"/>
    <xsl:template match="@css:id[not(parent::css:box) and not(ancestor::css:box[@type='inline'])]" mode="update-refs"/>
    <xsl:template match="@pxi:*" mode="update-refs"/>
    
</xsl:stylesheet>
