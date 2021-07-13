<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:re="regex-utils"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <!-- ======== -->
    <!-- Counters -->
    <!-- ======== -->
    
    <!--
        <symbol> = <ident> | <string>
    -->
    <xsl:variable name="css:SYMBOL_RE" select="re:or(($css:IDENT_RE,$css:STRING_RE))"/>
    <xsl:variable name="css:SYMBOL_RE_ident" select="1"/>
    <xsl:variable name="css:SYMBOL_RE_string" select="$css:SYMBOL_RE_ident + $css:IDENT_RE_groups + 1"/>
    <xsl:variable name="css:SYMBOL_RE_groups" select="$css:SYMBOL_RE_string + $css:STRING_RE_groups"/>
    
    <!--
        symbols(<symbols-type>? <string>+)
    -->
    <xsl:variable name="css:SYMBOLS_FN_RE" select="concat('symbols\(\s*((',$css:IDENT_RE,')\s+)?((',$css:STRING_RE,')(\s+(',$css:STRING_RE,'))*)\s*\)')"/>
    <xsl:variable name="css:SYMBOLS_FN_RE_type" select="2"/>
    <xsl:variable name="css:SYMBOLS_FN_RE_symbols" select="$css:SYMBOLS_FN_RE_type + $css:IDENT_RE_groups + 1"/>
    <xsl:variable name="css:SYMBOLS_FN_RE_groups" select="$css:SYMBOLS_FN_RE_symbols + 1 + $css:STRING_RE_groups + 2 + $css:STRING_RE_groups"/>
    
    <!--
        <counter-style-name> | symbols()
    -->
    <xsl:variable name="css:COUNTER_STYLE_RE" select="concat('(',$css:IDENT_RE,')|(',$css:SYMBOLS_FN_RE,')')"/>
    <xsl:variable name="css:COUNTER_STYLE_RE_name" select="1"/>
    <xsl:variable name="css:COUNTER_STYLE_RE_symbols_type" select="$css:COUNTER_STYLE_RE_name + $css:IDENT_RE_groups + 1 + $css:SYMBOLS_FN_RE_type"/>
    <xsl:variable name="css:COUNTER_STYLE_RE_symbols" select="$css:COUNTER_STYLE_RE_name + $css:IDENT_RE_groups + 1 + $css:SYMBOLS_FN_RE_symbols"/>
    <xsl:variable name="css:COUNTER_STYLE_RE_groups" select="$css:COUNTER_STYLE_RE_name + $css:IDENT_RE_groups + 1 + $css:SYMBOLS_FN_RE_groups"/>
    
    <xsl:function name="css:parse-symbols" as="xs:string*">
        <xsl:param name="symbols" as="xs:string"/>
        <xsl:analyze-string select="$symbols" regex="{$css:SYMBOL_RE}">
            <xsl:matching-substring>
                <xsl:choose>
                    <!--
                        <ident>
                    -->
                    <xsl:when test="regex-group($css:SYMBOL_RE_ident)!=''">
                        <xsl:sequence select="regex-group($css:SYMBOL_RE_ident)"/>
                    </xsl:when>
                    <!--
                        <string>
                    -->
                    <xsl:otherwise>
                        <xsl:sequence select="substring(regex-group($css:SYMBOL_RE_string), 2, string-length(regex-group($css:SYMBOL_RE_string))-2)"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:matching-substring>
        </xsl:analyze-string>
    </xsl:function>
    
    <xsl:function name="css:counter-value" as="xs:integer?">
        <xsl:param name="name" as="xs:string"/>
        <xsl:param name="context" as="element()"/>
        <xsl:variable name="reset-x" as="xs:string" select="concat('counter-reset-',$name)"/>
        <xsl:variable name="set-x" as="xs:string" select="concat('counter-set-',$name)"/>
        <xsl:variable name="increment-x" as="xs:string" select="concat('counter-increment-',$name)"/>
        <xsl:variable name="creator" as="element()?">
            <xsl:variable name="candidates" as="element()*"
                          select="$context/ancestor-or-self::*/(self::*|preceding-sibling::*)"/>
            <xsl:sequence select="if ($candidates[@css:*[local-name()=$reset-x]])
                                  then ($candidates[@css:*[local-name()=$reset-x]])[last()]
                                  else ($candidates[@css:*[local-name()=($set-x,$increment-x)]])[1]"/>
        </xsl:variable>
        <xsl:if test="$creator">
            <xsl:variable name="scope" as="element()*"
                          select="$context/(preceding::*|ancestor-or-self::*)
                                  intersect $creator/(self::*|following-sibling::*)/descendant-or-self::*
                                  except $creator/(descendant::*|following::*)[@css:*[local-name()=$reset-x]]
                                                 /(self::*|following-sibling::*)/descendant-or-self::*"/>
            <xsl:variable name="last-set" as="element()" select="($creator|$scope[@css:*[local-name()=$set-x]])[last()]"/>
            <xsl:sequence select="sum(($last-set/@css:*[local-name()=($reset-x,$set-x,$increment-x)][1]
                                       |($scope
                                         intersect $context/(self::*|preceding::*|ancestor::*)
                                         intersect $last-set/(descendant::*|following::*))
                                        /@css:*[local-name()=$increment-x])
                                      /xs:integer(number(.)))"/>
        </xsl:if>
    </xsl:function>
    
    <xsl:function name="pxi:counter-representation-alphabetic" as="xs:string">
        <xsl:param name="number" as="xs:integer"/>
        <xsl:param name="symbols" as="xs:string*"/>
        <xsl:variable name="base" as="xs:integer" select="count($symbols)"/>
        <xsl:sequence select="if ($number &gt; $base)
                              then concat(pxi:counter-representation-alphabetic(($number - 1) idiv $base, $symbols),
                                          $symbols[1 + (($number - 1) mod $base)])
                              else $symbols[$number]"/>
    </xsl:function>
    
    <xsl:function name="pxi:counter-representation-numeric" as="xs:string">
        <xsl:param name="number" as="xs:integer"/>
        <xsl:param name="symbols" as="xs:string*"/>
        <xsl:variable name="base" as="xs:integer" select="count($symbols)"/>
        <xsl:sequence select="if ($number &gt;= $base)
                              then concat(pxi:counter-representation-numeric($number idiv $base, $symbols),
                                          $symbols[1 + ($number mod $base)])
                              else $symbols[1 + $number]"/>
    </xsl:function>
    
    <xsl:function name="pxi:counter-representation-additive" as="xs:string?">
        <xsl:param name="number" as="xs:integer"/>
        <xsl:param name="weights" as="xs:integer*"/>
        <xsl:param name="symbols" as="xs:string*"/>
        <xsl:choose>
            <xsl:when test="$number=0 and 0=$weights">
                <xsl:sequence select="$symbols[index-of($weights, 0)]"/>
            </xsl:when>
            <xsl:when test="count($weights) &gt; 0">
                <xsl:variable name="rest-number" as="xs:integer"
                              select="if ($weights[1]=0) then $number
                                      else $number mod $weights[1]"/>
                <xsl:variable name="rest" as="xs:string?"
                              select="if ($rest-number=0) then ''
                                      else pxi:counter-representation-additive(
                                             $rest-number,
                                             $weights[position()&gt;1],
                                             $symbols[position()&gt;1])"/>
                <xsl:if test="exists($rest)">
                    <xsl:sequence select="if ($weights[1]=0) then $rest
                                          else string-join((
                                                 for $x in 1 to ($number idiv $weights[1]) return $symbols[1],
                                                 $rest),'')"/>
                </xsl:if>
            </xsl:when>
        </xsl:choose>
    </xsl:function>
    
    <xsl:function name="css:counter-style" as="element()">
        <xsl:param name="name-or-inline" as="xs:string"/>
        <xsl:analyze-string select="$name-or-inline" regex="^({$css:COUNTER_STYLE_RE})$">
            <xsl:matching-substring>
                <xsl:choose>
                    <!--
                        <counter-style-name>>
                    -->
                    <xsl:when test="regex-group(1 + $css:COUNTER_STYLE_RE_name)!=''">
                        <xsl:sequence select="css:named-counter-style(regex-group(1 + $css:COUNTER_STYLE_RE_name))"/>
                    </xsl:when>
                    <!--
                        symbols()
                    -->
                    <xsl:otherwise>
                        <css:counter-style system="{if (not(regex-group(1 + $css:COUNTER_STYLE_RE_symbols_type)=''))
                                                    then regex-group(1 + $css:COUNTER_STYLE_RE_symbols_type)
                                                    else 'symbolic'}"
                                           symbols="{regex-group(1 + $css:COUNTER_STYLE_RE_symbols)}"
                                           prefix=""
                                           suffix=" "
                                           fallback="decimal"
                                           negative="-"
                                           text-transform="auto"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:matching-substring>
        </xsl:analyze-string>
    </xsl:function>
    
    <xsl:template name="css:counter-representation" as="xs:string*">
        <xsl:param name="value" as="xs:integer" required="yes"/>
        <xsl:param name="style" as="element()" required="yes"/>
        <xsl:param name="with-prefix-suffix" as="xs:boolean" select="false()"/>
        <xsl:variable name="system" as="xs:string" select="$style/@system"/>
        <xsl:variable name="symbols" as="xs:string*" select="css:parse-symbols($style/@symbols)"/>
        <xsl:variable name="formatted-value" as="xs:string?">
            <xsl:choose>
                <xsl:when test="$system='cyclic'">
                    <xsl:sequence select="$symbols[1 + (($value - 1) mod count($symbols))]"/>
                </xsl:when>
                <xsl:when test="$system='fixed'">
                    <xsl:if test="$value &gt;= 1 and $value &lt;= count($symbols)">
                        <xsl:sequence select="$symbols[$value]"/>
                    </xsl:if>
                </xsl:when>
                <xsl:when test="$system='symbolic'">
                    <xsl:if test="$value &gt;= 1">
                        <xsl:sequence select="string-join(
                                                for $x in 1 to 1 + (($value - 1) idiv count($symbols))
                                                return $symbols[1 + (($value - 1) mod count($symbols))],
                                              '')"/>
                    </xsl:if>
                </xsl:when>
                <xsl:when test="$system='alphabetic'">
                    <xsl:if test="$value &gt;= 1">
                        <xsl:sequence select="pxi:counter-representation-alphabetic($value, $symbols)"/>
                    </xsl:if>
                </xsl:when>
                <xsl:when test="$system='numeric'">
                    <xsl:sequence select="concat(
                                            if ($value &gt;= 0) then '' else $style/@negative,
                                            pxi:counter-representation-numeric(abs($value), $symbols))"/>
                </xsl:when>
                <xsl:when test="$system='additive'">
                    <xsl:if test="$value &gt;= 0">
                        <xsl:variable name="additive-symbols" as="xs:string*" select="tokenize($style/@additive-symbols,',')"/>
                        <xsl:sequence select="pxi:counter-representation-additive(
                                                $value,
                                                for $x in $additive-symbols return xs:integer(number(replace($x,'^\s*([1-9][0-9]*)\s.*$','$1'))),
                                                css:parse-symbols(string-join(
                                                  for $x in $additive-symbols return replace($x,'^\s*[1-9][0-9]*',' '),
                                                  '')))"/>
                    </xsl:if>
                </xsl:when>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$formatted-value">
                <xsl:variable name="formatted-value" as="xs:string"
                              select="if ($with-prefix-suffix)
                                      then concat($style/@prefix,$formatted-value,$style/@suffix)
                                      else $formatted-value"/>
                <xsl:variable name="text-transform" as="xs:string" select="$style/@text-transform"/>
                <xsl:sequence select="($formatted-value,$text-transform)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="css:counter-representation">
                    <xsl:with-param name="value" select="$value"/>
                    <xsl:with-param name="style" select="css:named-counter-style($style/@fallback)"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template name="css:counter"> <!-- as="(xs:string*|xs:integer)?" -->
        <xsl:param name="name" as="xs:string" required="yes"/>
        <xsl:param name="style" as="xs:string?" select="()"/>
        <xsl:param name="context" as="element()" select="."/>
        <xsl:param name="default-marker-contents" as="xs:boolean" select="false()"/>
        <xsl:variable name="value" as="xs:integer?" select="css:counter-value($name, $context)"/>
        <xsl:if test="exists($value)">
            <xsl:choose>
                <xsl:when test="$style and $style='none'">
                    <xsl:sequence select="('','none')"/>
                </xsl:when>
                <xsl:when test="$style">
                    <xsl:call-template name="css:counter-representation">
                        <xsl:with-param name="value" select="$value"/>
                        <xsl:with-param name="style" select="css:counter-style($style)"/>
                        <xsl:with-param name="with-prefix-suffix" select="$default-marker-contents"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="$value"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>
    
</xsl:stylesheet>
