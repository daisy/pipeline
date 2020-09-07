<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:re="regex-utils"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:import href="base.xsl"/>
    
    <!-- ==================== -->
    <!-- Property Definitions -->
    <!-- ==================== -->
    
    <!--
        FIXME: needs to contain the OBFL-specific -obfl-right-text-indent because inheritance
        wouldn't work otherwise
    -->
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
                 'size',
                 'max-height',
                 'min-length',
                 'max-length',
                 'text-transform',
                 'font-style',
                 'font-weight',
                 'text-decoration',
                 'color',
                 'line-height',
                 'letter-spacing',
                 'word-spacing',
                 'render-table-by',
                 'table-header-policy',
                 '-obfl-right-text-indent')"/>
    
    <xsl:variable name="css:values" as="xs:string*"
        select="(re:exact(re:or(('block','inline','list-item','none','table',$css:VENDOR_PRF_IDENT_RE))),
                 re:exact($css:IDENT_RE),
                 re:exact(re:or(($css:NON_NEGATIVE_INTEGER_RE,'auto'))),
                 re:exact(re:or(($css:NON_NEGATIVE_INTEGER_RE,'auto'))),
                 re:exact($css:INTEGER_RE),
                 re:exact($css:INTEGER_RE),
                 re:exact($css:NON_NEGATIVE_INTEGER_RE),
                 re:exact($css:NON_NEGATIVE_INTEGER_RE),
                 re:exact($css:NON_NEGATIVE_INTEGER_RE),
                 re:exact($css:NON_NEGATIVE_INTEGER_RE),
                 re:exact($css:NON_NEGATIVE_INTEGER_RE),
                 re:exact($css:NON_NEGATIVE_INTEGER_RE),
                 re:exact(re:or(($css:BRAILLE_CHAR_RE,'none'))),
                 re:exact(re:or(('none','solid'))),
                 re:exact(re:or(($css:NON_NEGATIVE_INTEGER_RE,'thin','medium','thick'))),
                 re:exact(re:or(('inner','center','outer'))),
                 re:exact(re:or(($css:BRAILLE_CHAR_RE,'none'))),
                 re:exact(re:or(('none','solid'))),
                 re:exact(re:or(($css:NON_NEGATIVE_INTEGER_RE,'thin','medium','thick'))),
                 re:exact(re:or(('inner','middle','outer'))),
                 re:exact(re:or(($css:BRAILLE_CHAR_RE,'none'))),
                 re:exact(re:or(('none','solid'))),
                 re:exact(re:or(($css:NON_NEGATIVE_INTEGER_RE,'thin','medium','thick'))),
                 re:exact(re:or(('inner','middle','outer'))),
                 re:exact(re:or(($css:BRAILLE_CHAR_RE,'none'))),
                 re:exact(re:or(('none','solid'))),
                 re:exact(re:or(($css:NON_NEGATIVE_INTEGER_RE,'thin','medium','thick'))),
                 re:exact(re:or(('inner','middle','outer'))),
                 re:exact($css:INTEGER_RE),
                 re:exact(re:or(($css:IDENT_RE,$css:BRAILLE_STRING_RE,$css:SYMBOLS_FN_RE))),
                 re:exact(re:or(('center','left','right'))),
                 re:exact(re:or(('always','auto','avoid','left','right'))),
                 re:exact(re:or(('always','auto','avoid','left','right'))),
                 re:exact(re:or(('auto','avoid'))),
                 re:exact(re:or(('always','auto','avoid','prefer'))),
                 re:exact(re:or(('always','auto','avoid','prefer'))),
                 re:exact(re:or(('auto','avoid',$css:VENDOR_PRF_FN_RE))),
                 re:exact($css:INTEGER_RE),
                 re:exact($css:INTEGER_RE),
                 re:exact(re:or(($css:IDENT_RE,'auto'))),
                 re:exact(re:or(('none',re:comma-separated($css:STRING_SET_PAIR_RE)))),
                 re:exact(re:or(('none',re:space-separated($css:COUNTER_SET_PAIR_RE)))),
                 re:exact(re:or(('none',re:space-separated($css:COUNTER_SET_PAIR_RE)))),
                 re:exact(re:or(('none',re:space-separated($css:COUNTER_SET_PAIR_RE)))),
                 re:exact(re:or(('none',$css:CONTENT_LIST_RE))),
                 re:exact(re:or(('normal','pre-wrap','pre-line'))),
                 re:exact(re:or(('auto','manual','none'))),
                 re:exact(concat('(',$css:NON_NEGATIVE_INTEGER_RE,')\s+(',$css:NON_NEGATIVE_INTEGER_RE,')')),
                 re:exact(re:or(('none',$css:NON_NEGATIVE_INTEGER_RE))),
                 re:exact(re:or(($css:NON_NEGATIVE_INTEGER_RE,'auto'))),
                 re:exact(re:or(($css:NON_NEGATIVE_INTEGER_RE,'auto'))),
                 re:exact(re:or(($css:IDENT_LIST_RE,'auto','none'))),
                 re:exact(re:or(('normal','italic','oblique'))),
                 re:exact(re:or(('normal','bold','100','200','300','400','500','600','700','800','900'))),
                 re:exact(re:or(('none','underline','overline','line-through','blink'))),
                 re:exact($css:COLOR_RE),
                 re:exact(re:or(($css:POSITIVE_NUMBER_RE,$css:POSITIVE_PERCENTAGE_RE))),
                 re:exact($css:NON_NEGATIVE_INTEGER_RE),
                 re:exact($css:NON_NEGATIVE_INTEGER_RE),
                 re:exact(re:or(('auto',re:comma-separated($css:IDENT_RE)))),
                 re:exact(re:or(('once','always','front'))),
                 re:exact($css:NON_NEGATIVE_INTEGER_RE))"/>
    
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
                 '^@page$',
                 '^@footnotes$',
                 '^@volume$',
                 '^@volume$',
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
    
    <xsl:variable name="css:initial-values" as="xs:string*"
        select="('inline',
                 'normal',
                 'auto',
                 'auto',
                 '0',
                 '0',
                 '0',
                 '0',
                 '0',
                 '0',
                 '0',
                 '0',
                 'none',
                 'none',
                 '1',
                 'center',
                 'none',
                 'none',
                 '1',
                 'center',
                 'none',
                 'none',
                 '1',
                 'center',
                 'none',
                 'none',
                 '1',
                 'center',
                 '0',
                 'none',
                 'left',
                 'auto',
                 'auto',
                 'auto',
                 'auto',
                 'auto',
                 'auto',
                 '0',
                 '0',
                 'auto',
                 'none',
                 'none',
                 'none',
                 'none',
                 'none',
                 'normal',
                 'manual',
                 '40 25',
                 'none',
                 'auto',
                 'auto',
                 'auto',
                 'normal',
                 'normal',
                 'none',
                 '#000000',
                 '1',
                 '0',
                 '1',
                 'auto',
                 'once',
                 '0')"/>
    
    <xsl:variable name="css:media" as="xs:string*"
        select="('embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'print',
                 'print',
                 'print',
                 'print',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed',
                 'embossed')"/>
    
    <xsl:variable name="css:inherited-properties" as="xs:string*"
        select="('text-indent',
                 'list-style-type',
                 'text-align',
                 'page',
                 'white-space',
                 'hyphens',
                 'font-style',
                 'font-weight',
                 'text-decoration',
                 'color',
                 'line-height',
                 'letter-spacing',
                 'word-spacing',
                 'text-transform',
                 '-obfl-right-text-indent')"/>
    
    <xsl:variable name="css:paged-media-properties" as="xs:string*"
        select="('page-break-before',
                 'page-break-after',
                 'page-break-inside',
                 'orphans',
                 'widows')"/>
    
    <xsl:function name="css:is-valid" as="xs:boolean">
        <xsl:param name="css:property" as="element()"/>
        <xsl:variable name="index" select="index-of($css:properties, $css:property/@name)"/>
        <xsl:sequence select="if ($index)
                              then $css:property/@value=('inherit', 'initial') or matches($css:property/@value, $css:values[$index], 'x')
                              else matches($css:property/@name, re:exact($css:VENDOR_PRF_IDENT_RE))"/> <!-- might be valid -->
    </xsl:function>
    
    <xsl:function name="css:initial-value" as="xs:string?">
        <xsl:param name="property" as="xs:string"/>
        <xsl:variable name="index" select="index-of($css:properties, $property)"/>
        <xsl:if test="$index">
            <xsl:sequence select="$css:initial-values[$index]"/>
        </xsl:if>
    </xsl:function>
    
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
    
    <!-- ================== -->
    <!-- Special inheriting -->
    <!-- ================== -->
    
    <xsl:template match="css:property[@name='text-transform']" mode="css:compute">
        <xsl:param name="concretize-inherit" as="xs:boolean"/>
        <xsl:param name="concretize-initial" as="xs:boolean"/>
        <xsl:param name="validate" as="xs:boolean"/>
        <xsl:param name="context" as="element()"/>
        <xsl:choose>
            <xsl:when test="@value='inherit'">
                <xsl:sequence select="."/>
            </xsl:when>
            <xsl:when test="@value='none'">
                <xsl:sequence select="."/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="parent-computed" as="element()">
                    <xsl:call-template name="css:parent-property">
                        <xsl:with-param name="property" select="@name"/>
                        <xsl:with-param name="compute" select="true()"/>
                        <xsl:with-param name="concretize-inherit" select="true()"/>
                        <xsl:with-param name="concretize-initial" select="$concretize-initial"/>
                        <xsl:with-param name="validate" select="$validate"/>
                        <xsl:with-param name="context" select="$context"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="@value=('initial','auto') and $parent-computed/@value='none'">
                        <xsl:sequence select="."/>
                    </xsl:when>
                    <xsl:when test="@value=('initial','auto')">
                        <xsl:sequence select="$parent-computed"/>
                    </xsl:when>
                    <xsl:when test="$parent-computed/@value=('auto','none','initial')">
                        <xsl:sequence select="."/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:sequence select="css:property(@name, string-join(
                                                distinct-values((
                                                  tokenize(normalize-space(@value), ' '),
                                                  tokenize(normalize-space($parent-computed/@value), ' '))),
                                                ' '))"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- ============== -->
    <!-- Counter Styles -->
    <!-- ============== -->
    
    <xsl:function name="css:named-counter-style" as="element()">
        <xsl:param name="name" as="xs:string"/>
        <xsl:variable name="style" as="element()?"
                      select="(css:custom-counter-style($name),$css:predefined-counter-styles[@name=$name])[1]"/>
        <xsl:choose>
            <xsl:when test="$style
                            and ((($style/@system=('symbolic','alphabetic','numeric','cyclic','fixed')
                                   or not($style/@system))
                                  and $style/@symbols)
                                 or ($style/@system='additive'
                                     and $style/@additive-symbols))">
                <xsl:element name="css:counter-style">
                    <xsl:attribute name="system" select="($style/@system,'symbolic')[1]"/>
                    <xsl:choose>
                        <xsl:when test="$style/@system='additive'">
                            <xsl:sequence select="$style/@additive-symbols"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:sequence select="$style/@symbols"/>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:if test="$style/@system=('symbolic','alphabetic','numeric','additive') or not($style/@system)">
                        <xsl:attribute name="negative" select="($style/@negative,'-')[1]"/>
                    </xsl:if>
                    <xsl:attribute name="prefix" select="($style/@prefix,'')[1]"/>
                    <xsl:attribute name="suffix" select="($style/@suffix,'. ')[1]"/>
                    <xsl:attribute name="fallback" select="($style/@fallback,'. ')[1]"/>
                    <xsl:attribute name="text-transform" select="($style/@text-transform,'auto')[1]"/>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <css:counter-style system="numeric"
                                   symbols="'0' '1' '2' '3' '4' '5' '6' '7' '8' '9'"
                                   negative="-"
                                   prefix=""
                                   suffix=". "
                                   fallback="decimal"
                                   text-transform="auto"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>
    
    <xsl:variable name="css:predefined-counter-styles" as="element()*">
        <css:counter-style name="decimal"
                           system="numeric"
                           symbols="'0' '1' '2' '3' '4' '5' '6' '7' '8' '9'"
                           negative="-"/>
        <css:counter-style name="lower-alpha"
                           system="alphabetic"
                           symbols="'a' 'b' 'c' 'd' 'e' 'f' 'g' 'h' 'i' 'j' 'k' 'l' 'm' 'n' 'o' 'p' 'q' 'r' 's' 't' 'u' 'v' 'w' 'x' 'y' 'z'"/>
        <css:counter-style name="upper-alpha"
                           system="alphabetic"
                           symbols="'A' 'B' 'C' 'D' 'E' 'F' 'G' 'H' 'I' 'J' 'K' 'L' 'M' 'N' 'O' 'P' 'Q' 'R' 'S' 'T' 'U' 'V' 'W' 'X' 'Y' 'Z'"/>
        <css:counter-style name="lower-roman"
                           system="additive"
                           range="1 3999"
                           additive-symbols="1000 'm', 900 'cm', 500 'd', 400 'cd', 100 'c', 90 'xc', 50 'l', 40 'xl', 10 'x', 9 'ix', 5 'v', 4 'iv', 1 'i'"/>
        <css:counter-style name="upper-roman"
                           system="additive"
                           range="1 3999"
                           additive-symbols="1000 'M', 900 'CM', 500 'D', 400 'CD', 100 'C', 90 'XC', 50 'L', 40 'XL', 10 'X', 9 'IX', 5 'V', 4 'IV', 1 'I'"/>
    </xsl:variable>
    
    <xsl:function name="css:custom-counter-style" as="element()?">
        <xsl:param name="name" as="xs:string"/>
    </xsl:function>
    
    <!--
        round to next .25
    -->
    <xsl:function name="css:round-line-height" as="xs:string">
        <xsl:param name="line-height" as="xs:string"/>
        <xsl:analyze-string select="$line-height"
                            regex="^(({$css:POSITIVE_NUMBER_RE})|({$css:POSITIVE_PERCENTAGE_RE}))$">
            <xsl:matching-substring>
                <xsl:variable name="value" as="xs:double">
                    <xsl:choose>
                        <xsl:when test="regex-group(2)!=''">
                            <xsl:sequence select="number(regex-group(2))"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:sequence select="number(regex-group(2 + $css:POSITIVE_NUMBER_RE_groups + 1 + $css:POSITIVE_PERCENTAGE_RE_number))
                                                  div 100"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="value" as="xs:double" select="round($value * 4) div 4"/>
                <xsl:sequence select="format-number($value, '0.##')"/>
            </xsl:matching-substring>
            <xsl:non-matching-substring>
                <xsl:message terminate="yes" select="concat('Not a valid line-height: ',$line-height)"/>
            </xsl:non-matching-substring>
        </xsl:analyze-string>
    </xsl:function>
    
</xsl:stylesheet>
