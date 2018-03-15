<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:louis="http://liblouis.org/liblouis"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:output method="xml" encoding="UTF-8" indent="yes"/>
    <xsl:output method="xml" encoding="UTF-8" indent="yes" name="louis-styles"/>
    
    <!--
        css-utils [2.0.0,3.0.0)
    -->
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>
    
    <xsl:variable name="braille-page-format" select="pxi:get-page-layout-param(/*, 'louis:braille-page-format')"/>
    
    <xsl:variable name="root" select="/*"/>
    <xsl:key name="stylesheet" match="//*[@css:display]|//louis:toc-item" use="string(@style)"/>
    
    <xsl:function name="pxi:generate-style-name" as="xs:string">
        <xsl:param name="stylesheet" as="xs:string"/>
        <xsl:value-of select="generate-id($root/key('stylesheet', $stylesheet)[1])"/>
    </xsl:function>
    
    <xsl:template match="@style|@css:display"/>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:if test="self::*/@css:display or self::louis:toc-item">
                <xsl:attribute name="louis:style" select="concat('#', pxi:generate-style-name(string(@style)))"/>
            </xsl:if>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="/*">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
        <xsl:result-document href="louis-styles.xml" format="louis-styles">
            <louis:styles>
                <xsl:choose>
                    <xsl:when test="/louis:toc">
                        <xsl:variable name="href" select="@href"/>
                        <xsl:variable name="offset-left" select="
                            number(pxi:get-page-layout-param(collection()/*[base-uri(.)=$href], 'louis:page-width'))
                            - number(/louis:toc/@width)"/>
                        <xsl:for-each select="distinct-values(//louis:toc-item/string(@style))">
                            <xsl:variable name="i" select="position()"/>
                            <xsl:if test="$i &lt;= 10">
                                <xsl:call-template name="print-liblouis-style">
                                    <xsl:with-param name="style" select="."/>
                                    <xsl:with-param name="style-name" select="concat('contents', $i)"/>
                                    <xsl:with-param name="toc-item" select="true()"/>
                                    <xsl:with-param name="offset-left" select="$offset-left"/>
                                </xsl:call-template>
                            </xsl:if>
                        </xsl:for-each>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="print-liblouis-style">
                            <xsl:with-param name="style" select="string(@style)"/>
                            <xsl:with-param name="style-name" select="'root'"/>
                        </xsl:call-template>
                        <xsl:for-each select="distinct-values(//*[@css:display and not(self::louis:toc-item)]/string(@style))">
                            <xsl:call-template name="print-liblouis-style">
                                <xsl:with-param name="style" select="."/>
                                <xsl:with-param name="style-name" select="pxi:generate-style-name(.)"/>
                            </xsl:call-template>
                        </xsl:for-each>
                    </xsl:otherwise>
                </xsl:choose>
            </louis:styles>
        </xsl:result-document>
    </xsl:template>
    
    <xsl:template name="print-liblouis-style">
        <xsl:param name="style" as="xs:string"/>
        <xsl:param name="style-name" as="xs:string"/>
        <xsl:param name="toc-item" as="xs:boolean" select="false()"/>
        <xsl:param name="offset-left" select="0"/>
        
        <xsl:text># --------------------------------------------------------------------------------------------------&#xa;</xsl:text>
        <xsl:text># </xsl:text>
        <xsl:value-of select="if (starts-with($style-name, '#')) then $style else $style-name"/>
        <xsl:text>&#xa;</xsl:text>
        <xsl:text># --------------------------------------------------------------------------------------------------&#xa;</xsl:text>
        
        <xsl:text>style </xsl:text>
        <xsl:sequence select="$style-name"/>
        <xsl:text>&#xa;</xsl:text>
        
        <xsl:variable name="properties" as="element()*" select="css:parse-declaration-list($style)"/>
        <xsl:variable name="text-align" as="xs:string" select="string($properties[@name='text-align'][last()]/@value)"/>
        <xsl:variable name="left" as="xs:string" select="string($properties[@name='left'][last()]/@value)"/>
        <xsl:variable name="right" as="xs:string" select="string($properties[@name='right'][last()]/@value)"/>
        <xsl:variable name="margin-top" as="xs:string" select="string($properties[@name='margin-top'][last()]/@value)"/>
        <xsl:variable name="margin-bottom" as="xs:string" select="string($properties[@name='margin-bottom'][last()]/@value)"/>
        <xsl:variable name="text-indent" as="xs:string" select="string($properties[@name='text-indent'][last()]/@value)"/>
        <xsl:variable name="page-break-before" as="xs:string" select="string($properties[@name='page-break-before'][last()]/@value)"/>
        <xsl:variable name="page-break-after" as="xs:string" select="string($properties[@name='page-break-after'][last()]/@value)"/>
        <xsl:variable name="page-break-inside" as="xs:string" select="string($properties[@name='page-break-inside'][last()]/@value)"/>
        <xsl:variable name="orphans" as="xs:string" select="string($properties[@name='orphans'][last()]/@value)"/>
        
        <!-- format -->
        
        <xsl:variable name="format" as="xs:string?">
            <xsl:choose>
                <xsl:when test="$toc-item">
                    <xsl:value-of select="'contents'"/>
                </xsl:when>
                <xsl:when test="$text-align='left'">
                    <xsl:value-of select="'leftJustified'"/>
                </xsl:when>
                <xsl:when test="$text-align='right'">
                    <xsl:value-of select="'rightJustified'"/>
                </xsl:when>
                <xsl:when test="$text-align='center'">
                    <xsl:value-of select="'centered'"/>
                </xsl:when>
            </xsl:choose>
        </xsl:variable>
        <xsl:if test="$format">
            <xsl:value-of select="concat('   format ', $format, '&#xa;')"/>
        </xsl:if>
        
        <!-- leftMargin -->
        
        <xsl:choose>
            <xsl:when test="pxi:is-numeric($left)">
                <xsl:value-of select="concat('   leftMargin ', format-number(number($left) + $offset-left, '0'), '&#xa;')"/>
            </xsl:when>
            <xsl:when test="$offset-left > 0">
                <xsl:value-of select="concat('   leftMargin ', format-number($offset-left, '0'))"/>
            </xsl:when>
        </xsl:choose>
        
        <!-- rightMargin -->
        
        <xsl:if test="pxi:is-numeric($right)">
            <xsl:value-of select="concat('   rightMargin ', format-number(number($right), '0'), '&#xa;')"/>
        </xsl:if>
        
        <!-- linesBefore -->
        
        <xsl:if test="pxi:is-numeric($margin-top)">
            <xsl:value-of select="concat('   linesBefore ', format-number(number($margin-top), '0'), '&#xa;')"/>
        </xsl:if>
        
        <!-- linesAfter -->
        
        <xsl:if test="pxi:is-numeric($margin-bottom)">
            <xsl:value-of select="concat('   linesAfter ', format-number(number($margin-bottom), '0'), '&#xa;')"/>
        </xsl:if>
        
        <!-- firstLineIndent -->
        
        <xsl:if test="pxi:is-numeric($text-indent)">
            <xsl:value-of select="concat('   firstLineIndent ', format-number(number($text-indent), '0'), '&#xa;')"/>
        </xsl:if>
        
        <!-- newPageBefore -->
        
        <xsl:if test="$page-break-before='always'">
            <xsl:value-of select="concat('   newPageBefore ', 'yes', '&#xa;')"/>
        </xsl:if>
        
        <!-- newPageAfter -->
        
        <xsl:if test="$page-break-after='always'">
            <xsl:value-of select="concat('   newPageAfter ', 'yes', '&#xa;')"/>
        </xsl:if>
        
        <!-- rightHandPage -->
        
        <xsl:if test="$page-break-before='right'">
            <xsl:value-of select="concat('   rightHandPage ', 'yes', '&#xa;')"/>
        </xsl:if>
        
        <!-- dontSplit -->
        
        <xsl:if test="$page-break-inside='avoid'">
            <xsl:value-of select="concat('   dontSplit ', 'yes', '&#xa;')"/>
        </xsl:if>
        
        <!-- keepWithNext -->
        <!-- FIXME: 'avoid page-break after' not entirely the same as 'keep with next sibling' -->
        
        <xsl:if test="$page-break-after='avoid'">
            <xsl:value-of select="concat('   keepWithNext ', 'yes', '&#xa;')"/>
        </xsl:if>
        
        <!-- orphanControl -->
        
        <xsl:if test="pxi:is-numeric($orphans)">
            <xsl:value-of select="concat('   orphanControl ', format-number(number($orphans), '0'), '&#xa;')"/>
        </xsl:if>
        
        <!-- braillePageNumberFormat -->
        
        <xsl:if test="$style-name='root'">
            <xsl:variable name="braillePageNumberFormat">
                <xsl:choose>
                    <xsl:when test="$braille-page-format='decimal'">
                        <xsl:text>normal</xsl:text>
                    </xsl:when>
                    <xsl:when test="$braille-page-format='lower-roman'">
                        <xsl:text>roman</xsl:text>
                    </xsl:when>
                    <xsl:when test="$braille-page-format='prefix-p'">
                        <xsl:text>p</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>blank</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:value-of select="concat('   braillePageNumberFormat ', $braillePageNumberFormat, '&#xa;')"/>
        </xsl:if>
        
        <xsl:text>&#xa;</xsl:text>
        
    </xsl:template>
    
    <xsl:function name="pxi:is-numeric" as="xs:boolean">
        <xsl:param name="value"/>
        <xsl:sequence select="matches($value, '^(0|-?[1-9][0-9]*)(\.0*)?$')"/>
    </xsl:function>
    
    <xsl:function name="pxi:get-page-layout-param">
        <xsl:param name="document" as="element()"/>
        <xsl:param name="param-name"/>
        <xsl:sequence select="$document/louis:page-layout//c:param[@name=$param-name]/@value"/>
    </xsl:function>
    
</xsl:stylesheet>
