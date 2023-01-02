<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:s="org.daisy.pipeline.braille.css.xpath.Style"
                xmlns:re="regex-utils"
                exclude-result-prefixes="#all">
    
    <xsl:import href="base.xsl"/>
    
    <!-- ==================== -->
    <!-- Property Definitions -->
    <!-- ==================== -->
    
    <xsl:variable name="css:properties" as="xs:string*"
        select="('display',
                 'flow',
                 'left',
                 'right',
                 'margin-left',
                 'margin-right',
                 'margin-top',
                 'margin-bottom',
                 'padding-left',
                 'padding-right',
                 'padding-bottom',
                 'padding-top',
                 'border-left-pattern',
                 'border-left-style',
                 'border-left-width',
                 'border-left-align',
                 'border-right-pattern',
                 'border-right-style',
                 'border-right-width',
                 'border-right-align',
                 'border-bottom-pattern',
                 'border-bottom-style',
                 'border-bottom-width',
                 'border-bottom-align',
                 'border-top-pattern',
                 'border-top-style',
                 'border-top-width',
                 'border-top-align',
                 'text-indent',
                 'list-style-type',
                 'text-align',
                 'page-break-before',
                 'page-break-after',
                 'page-break-inside',
                 'volume-break-before',
                 'volume-break-after',
                 'volume-break-inside',
                 'orphans',
                 'widows',
                 'page',
                 'string-set',
                 'counter-reset',
                 'counter-set',
                 'counter-increment',
                 'content',
                 'white-space',
                 'hyphens',
                 'hyphenate-character',
                 'size',
                 'max-height',
                 'min-length',
                 'max-length',
                 'text-transform',
                 'braille-charset',
                 'font-style',
                 'font-weight',
                 'text-decoration',
                 'color',
                 'line-height',
                 'letter-spacing',
                 'word-spacing',
                 'render-table-by',
                 'table-header-policy')"/>
    
    <xsl:variable name="css:applies-to" as="xs:string*"
        select="('.*',
                 '.*',
                 '^(block|table|list-item)$',
                 '^(block|table|list-item)$',
                 '^(block|table|table-cell|list-item)$',
                 '^(block|table|table-cell|list-item)$',
                 '^(block|table|table-cell|list-item)$',
                 '^(block|table|table-cell|list-item)$',
                 '^(block|table|table-cell|list-item)$',
                 '^(block|table|table-cell|list-item)$',
                 '^(block|table|table-cell|list-item)$',
                 '^(block|table|table-cell|list-item)$',
                 '^(block|table|table-cell|list-item)$',
                 '^(block|table|table-cell|list-item)$',
                 '^(block|table|table-cell|list-item)$',
                 '^(block|table|table-cell|list-item)$',
                 '^(block|table|table-cell|list-item)$',
                 '^(block|table|table-cell|list-item)$',
                 '^(block|table|table-cell|list-item)$',
                 '^(block|table|table-cell|list-item)$',
                 '^(block|table|table-cell|list-item)$',
                 '^(block|table|table-cell|list-item)$',
                 '^(block|table|table-cell|list-item)$',
                 '^(block|table|table-cell|list-item)$',
                 '^(block|table|table-cell|list-item)$',
                 '^(block|table|table-cell|list-item)$',
                 '^(block|table|table-cell|list-item)$',
                 '^(block|table|table-cell|list-item)$',
                 '^(block|table|table-cell|list-item)$',
                 '^(list-item)$',
                 '^(block|table|table-cell|list-item)$',
                 '^(block|table|list-item)$',
                 '^(block|table|list-item)$',
                 '^(block|table|list-item)$',
                 '^(block|table|list-item)$',
                 '^(block|table|list-item)$',
                 '^(block|table|list-item)$',
                 '^(block|list-item)$',
                 '^(block|list-item)$',
                 '.*',
                 '.*',
                 '.*',
                 '.*',
                 '.*',
                 '^(::before|::after|@top-left|@top-center|@top-right|@bottom-left|@bottom-center|@bottom-right)$',
                 '.*',
                 '.*',
                 '.*',
                 '^@page$',
                 '^@footnotes$',
                 '^@volume$',
                 '^@volume$',
                 '.*',
                 '.*',
                 '.*',
                 '.*',
                 '.*',
                 '.*',
                 '^(block|table|list-item)$',
                 '.*',
                 '.*',
                 '.*',
                 '.*',
                 '^(block|list-item)$')"/>
    
    <xsl:variable name="css:inherited-properties" as="xs:string*"
        select="('text-indent',
                 'list-style-type',
                 'text-align',
                 'page',
                 'white-space',
                 'hyphens',
                 'hyphenate-character',
                 'font-style',
                 'font-weight',
                 'text-decoration',
                 'color',
                 'line-height',
                 'letter-spacing',
                 'word-spacing',
                 'text-transform',
                 'braille-charset')"/>
    
    <xsl:function name="css:is-inherited" as="xs:boolean">
        <xsl:param name="property" as="xs:string"/>
        <xsl:sequence select="$property=$css:inherited-properties"/>
    </xsl:function>
    
    <xsl:function name="css:applies-to" as="xs:boolean">
        <xsl:param name="property" as="xs:string"/>
        <xsl:param name="display" as="xs:string"/>
        <xsl:variable name="index" select="index-of($css:properties, $property)"/>
        <xsl:sequence select="if ($index)
                              then matches($display, $css:applies-to[$index])
                              else matches($property, re:exact($css:VENDOR_PRF_IDENT_RE))"/> <!-- might apply -->
    </xsl:function>
    
    <!-- ============== -->
    <!-- Counter Styles -->
    <!-- ============== -->
    
    <xsl:function name="css:parse-counter-styles" as="map(xs:string,item())">
        <xsl:param name="stylesheet">
            <!--
                input is either:
                - a string, in which case it should be a regular style sheet consisting of @counter-style rules, or
                - a `css:counter-style' attribute, in which case it should have the form "& style1 { ... } & style2 { ... }"
                - an external object item
            -->
        </xsl:param>
        <xsl:map>
            <xsl:variable name="stylesheet" as="item()?">
                <xsl:choose>
                    <xsl:when test="not(exists($stylesheet))"/>
                    <xsl:when test="string($stylesheet)=''"/>
                    <xsl:when test="$stylesheet instance of xs:string or
                                    $stylesheet instance of attribute()">
                        <xsl:sequence select="s:get(css:parse-stylesheet($stylesheet),'@counter-style')"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:sequence select="$stylesheet"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:for-each select="for $s in $stylesheet return s:keys($s)">
                <xsl:map-entry key="replace(.,'^&amp; ','')">
                    <xsl:sequence select="s:get($stylesheet,.)"/>
                </xsl:map-entry>
            </xsl:for-each>
        </xsl:map>
    </xsl:function>
    
    <!--
        round to next .25
    -->
    <xsl:function name="css:round-line-height" as="xs:string">
        <xsl:param name="line-height" as="xs:string"/>
        <xsl:variable name="value" as="xs:double">
            <xsl:choose>
                <xsl:when test="ends-with($line-height,'%')">
                    <xsl:sequence select="number(substring($line-height,1,string-length($line-height)-1))
                                          div 100"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="number($line-height)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="value" as="xs:double" select="round($value * 4) div 4"/>
        <xsl:sequence select="format-number($value, '0.##')"/>
    </xsl:function>
    
</xsl:stylesheet>
