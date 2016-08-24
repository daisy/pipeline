<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/" exclude-result-prefixes="dtb" version="2.0">

    <!--Normalizes mixed block/inline content models.-->

    <xsl:output indent="yes" method="xml"/>


    <xsl:template match="/">
        <xsl:message>Normalize mixed block/inline content</xsl:message>
        <xsl:apply-templates/>
    </xsl:template>
    
    
    <!-- identity template -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>


    <!-- removed annotation from this list after the introduction of annotation-block and annotation-phrase -->
    <xsl:template
        match="dtb:div | dtb:prodnote | dtb:note | dtb:epigraph | dtb:li | 
        dtb:th | dtb:caption | dtb:sidebar |
        dtb:address | dtb:covertitle | dtb:samp | dtb:td | dtb:blockquote">

        <!--<xsl:message>Normalize mixed block and inline content models for <xsl:value-of select="local-name(.)"/></xsl:message>-->
        
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <!-- these tests represent a superset of inline and block elements of the elements that this template matches -->
            <xsl:variable name="inlines"
                select="tokenize('a,abbr,acronym,annoref,bdo,dfn,em,line,noteref,sent,span,strong,sub,sup,w,br',',')"/>
            <xsl:variable name="blocks"
                select="tokenize('code-block,samp,cite,img,imggroup,pagenum,prodnote,p,list,dl,div,linegroup,byline,dateline,epigraph,table,address,author,prodnote,sidebar,note,annotation-block,doctitle,docauthor, covertitle,bridgehead',',')"/>

            <xsl:variable name="has-inlines" select="not(empty(child::*[local-name() = $inlines]))"/>
            <xsl:variable name="has-blocks" select="not(empty(child::*[local-name() = $blocks]))"/>
            <xsl:variable name="is-text" select="not(empty(child::* = text()))"/>

            <!--<xsl:message>
                inlines? <xsl:value-of select="$has-inlines"/>
                blocks? <xsl:value-of select="$has-blocks"/>
                text? <xsl:value-of select="$is-text"/>
            </xsl:message>-->

            <xsl:choose>
                <!-- when there is a mix of block and inline children, we have to wrap the inlines in a block -->
                <xsl:when test="($has-inlines or child::* = text()) and $has-blocks">
                    <xsl:call-template name="blockize"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:copy>

    </xsl:template>


    <!-- zedai section elements must contain all block element children, so transform any inlines into blocks -->
    <xsl:template
        match="dtb:level | dtb:level1 | dtb:level2 | dtb:level3 | dtb:level4 | 
        dtb:level5 | dtb:level6">

        <xsl:copy>

            <xsl:apply-templates select="@*"/>

            <xsl:variable name="inlines"
                select="tokenize('a,abbr,acronym,annoref,bdo,dfn,em,line,noteref,sent,span,strong,sub,sup,w',',')"/>

            <xsl:choose>
                <xsl:when test="child::*/local-name() = $inlines">
                    <xsl:call-template name="blockize"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates/>
                </xsl:otherwise>
            </xsl:choose>

        </xsl:copy>

    </xsl:template>

    <!-- take the context element's children and wrap any inlines in p elements -->
    <xsl:template name="blockize">
        
        <xsl:variable name="inlines"
            select="tokenize('a,abbr,acronym,annoref,bdo,blockquote,br,dfn,em,line,noteref,sent,span,strong,sub,sup,w',',')"/>
        
        <xsl:variable name="parent" select="."/>
        <xsl:for-each-group group-adjacent="local-name() = $inlines or self::text()" select="node()[not(self::text()[normalize-space() = ''])]">
            <xsl:choose>
                <xsl:when test="current-grouping-key()">
                    <xsl:element name="p" namespace="http://www.daisy.org/z3986/2005/dtbook/">
                        <xsl:for-each select="current-group()">
                            <xsl:apply-templates select="."/>
                        </xsl:for-each>
                    </xsl:element>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:for-each select="current-group()">
                       <xsl:choose>
                           <!--<xsl:when test="self::text()">
                               <xsl:element name="p" namespace="http://www.daisy.org/z3986/2005/dtbook/">
                                   <xsl:copy/>
                               </xsl:element>
                           </xsl:when>-->
                           
                           <!-- discard whitespace -->
                           <xsl:when test="self::text() and string-length(self::text()[normalize-space()]) = 0">
                               
                           </xsl:when>
                           
                           <!-- all other elements must be block, so just copy them -->
                           <xsl:otherwise>
                               <xsl:copy>
                                   <xsl:apply-templates select="@*|node()"/>
                               </xsl:copy>
                           </xsl:otherwise>           
                       </xsl:choose> 
                    </xsl:for-each>
                </xsl:otherwise>
                
            </xsl:choose>
        </xsl:for-each-group>
        
    </xsl:template>
    
</xsl:stylesheet>
