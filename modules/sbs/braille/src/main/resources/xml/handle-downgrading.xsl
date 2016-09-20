<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0"
                xmlns="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns:brl="http://www.daisy.org/z3986/2009/braille/"
                xmlns:my="http://my-functions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="my brl xs">
    
    <xsl:import href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>
    
    <xsl:param name="contraction" select="2"/>
    
    <xsl:variable name="text-transform-defs" as="element()*">
        <!--
            Note: requires that the @text-transform rules are defined in the style attribute of the
            root element!
        -->
        <xsl:for-each select="css:parse-stylesheet(/*/@style)[matches(@selector,'^@text-transform')]">
            <xsl:variable name="name" as="xs:string" select="replace(@selector,'^@text-transform\s+(.+)$','$1')"/>
            <xsl:variable name="props" as="element()*" select="css:parse-declaration-list(@style)"/>
            <xsl:if test="$props[@name='system' and @value='-sbs-indicators']">
                <css:text-transform name="{$name}">
                    <xsl:if test="not($props[@name='single-word'])">
                        <xsl:message terminate="yes"
                                     select="concat('@text-transform &quot;',$name,'&quot; does not define &quot;single-word&quot;')"/>
                    </xsl:if>
                    <!--
                        TODO: check if string
                    -->
                    <xsl:attribute name="single-word" select="$props[@name='single-word']/@value/substring(., 2, string-length(.)-2)"/>
                    <xsl:if test="not($props[@name='open'])">
                        <xsl:message terminate="yes"
                                     select="concat('@text-transform &quot;',$name,'&quot; does not define &quot;open&quot;')"/>
                    </xsl:if>
                    <!--
                        TODO: check if string
                    -->
                    <xsl:attribute name="open" select="$props[@name='open']/@value/substring(., 2, string-length(.)-2)"/>
                    <xsl:if test="not($props[@name='open'])">
                        <xsl:message terminate="yes"
                                     select="concat('@text-transform &quot;',$name,'&quot; does not define &quot;close&quot;')"/>
                    </xsl:if>
                    <!--
                        TODO: check if string
                    -->
                    <xsl:attribute name="close" select="$props[@name='close']/@value/substring(., 2, string-length(.)-2)"/>
                </css:text-transform>
            </xsl:if>
        </xsl:for-each>
    </xsl:variable>
    
    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="exists($text-transform-defs)">
                <xsl:apply-templates select="*">
                    <xsl:with-param name="parent-text-transform" tunnel="yes" select="'auto'"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="*"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="comment()|processing-instruction()" mode="#default add-indicators">
        <xsl:sequence select="."/>
    </xsl:template>
    
    <xsl:template match="*|text()">
        <xsl:copy>
            <xsl:sequence select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[@css:text-transform]">
        <xsl:param name="parent-text-transform" as="xs:string" tunnel="yes" required="yes"/>
        <xsl:variable name="text-transform" as="xs:string"
                      select="css:computed-properties('text-transform', false(), .)/@value"/>
        <xsl:variable name="text-transforms" as="xs:string*"
                      select="tokenize(normalize-space($text-transform), ' ')"/>
        <xsl:variable name="parent-text-transforms" as="xs:string*"
                      select="tokenize(normalize-space($parent-text-transform), ' ')"/>
        <xsl:variable name="text-transforms-to-apply" as="xs:string*"
                      select="$text-transforms[.=$text-transform-defs/@name and not(.=$parent-text-transforms)]"/>
        <xsl:choose>
            <xsl:when test="exists($text-transforms-to-apply)">
                <xsl:apply-templates mode="add-indicators" select=".">
                    <xsl:with-param name="element" select="."/>
                    <xsl:with-param name="text-transforms-to-apply" select="$text-transforms-to-apply"/>
                    <xsl:with-param name="deferred-text-transforms" select="$text-transforms[not(.=$text-transform-defs/@name)]"/>
                    <xsl:with-param name="single-word"
                                    select="count(tokenize(string(.), '(\s|/|-)+')[string(.) != '']) &lt; 2"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy>
                    <xsl:sequence select="@*"/>
                    <xsl:apply-templates>
                        <xsl:with-param name="parent-text-transform" tunnel="yes" select="$text-transform"/>
                    </xsl:apply-templates>
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*|text()" mode="add-indicators">
        <xsl:param name="element" as="element()" required="yes"/>
        <xsl:param name="text-transforms-to-apply" as="xs:string*" required="yes"/>
        <xsl:param name="deferred-text-transforms" as="xs:string*" required="no" select="()"/>
        <xsl:param name="single-word" as="xs:boolean" required="yes"/>
        <xsl:choose>
            <xsl:when test="(my:is-inline-element(.) or self::text()) and not(normalize-space(string(.))='')">
                <xsl:if test="not((preceding::text() intersect $element/descendant::node())[not(normalize-space(string())='')])">
                    <!--
                        If it's a single word, insert an announcement for a single word grade
                        change. If there are multiple words, insert an announcement for a multiple
                        word grade change
                    -->
                    <xsl:for-each select="$text-transforms-to-apply">
                        <xsl:variable name="name" as="xs:string" select="."/>
                        <xsl:variable name="def" as="element()" select="$text-transform-defs[@name=$name]"/>
                        <brl:literal>
                            <xsl:value-of select="if ($single-word) then $def/@single-word else $def/@open"/>
                        </brl:literal>
                    </xsl:for-each>
                </xsl:if>
                <xsl:copy>
                    <xsl:if test=". intersect $element and exists($deferred-text-transforms)">
                        <xsl:attribute name="css:text-transform" select="string-join($deferred-text-transforms, ' ')"/>
                    </xsl:if>
                    <xsl:sequence select="@* except @css:text-transform"/>
                    <xsl:sequence select="node()"/>
                </xsl:copy>
                <xsl:if test="not($single-word)
                              and not((following::text() intersect $element/descendant::node())[not(normalize-space(string())='')])">
                    <!--
                        There are multiple words. Insert an announcement for the end of grade change
                    -->
                    <xsl:for-each select="$text-transforms-to-apply">
                        <xsl:variable name="name" as="xs:string" select="."/>
                        <xsl:variable name="def" as="element()" select="$text-transform-defs[@name=$name]"/>
                        <brl:literal>
                            <xsl:value-of select="$def/@close"/>
                        </brl:literal>
                    </xsl:for-each>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy>
                    <xsl:if test=". intersect $element and exists($deferred-text-transforms)">
                        <xsl:attribute name="css:text-transform" select="string-join($deferred-text-transforms, ' ')"/>
                    </xsl:if>
                    <xsl:sequence select="@* except @css:text-transform"/>
                    <xsl:apply-templates mode="add-indicators">
                        <xsl:with-param name="element" select="$element"/>
                        <xsl:with-param name="text-transforms-to-apply" select="$text-transforms-to-apply"/>
                        <xsl:with-param name="single-word" select="$single-word"/>
                    </xsl:apply-templates>
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:function name="my:is-inline-element">
        <xsl:param name="element"/>
        <xsl:sequence select="boolean($element[self::dtb:span or
                                               self::dtb:abbr or
                                               self::brl:num or
                                               self::brl:emph or
                                               self::dtb:em or
                                               self::dtb:strong])"/>
    </xsl:function>
    
</xsl:stylesheet>
