<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:s="org.daisy.pipeline.braille.css.xpath.Style"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <!-- ====== -->
    <!-- Syntax -->
    <!-- ====== -->
    
    <xsl:variable name="css:STRING_RE">'[^']*'|"[^"]*"</xsl:variable>
    <xsl:variable name="css:VENDOR_PRF_IDENT_RE" select="'-(\p{L}|_)+-(\p{L}|[0-9]|_)(\p{L}|[0-9]|_|-)*'"/>

    <!-- ======= -->
    <!-- Parsing -->
    <!-- ======= -->
    
    <doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
        <desc>
            <p>Parse a style sheet.</p>
            <p>The style sheet must be specified as a string or a node (or empty sequence). In case
            of a node the string value is taken. Additional information may be provided by
            specifying the style sheet as an attribute node. A `css:page` attribute is parsed as a
            @page rule. A `css:volume` attribute is parsed as a @volume rule. A `css:text-transform`
            attribute is parsed as a @text-transform rule. A `css:counter-style` attribute is parsed
            as a @counter-style rule. A `css:*` attribute with the name of a property is parsed as a
            property declaration. `attr()` values in `content` and `string-set` properties are
            evaluated.</p>
            <p>An optional parent style may be specified as a `Style` item().</p>
            <p>Return value is an optional `Style` item.</p>
        </desc>
    </doc>
    <xsl:function name="css:parse-stylesheet" as="item()?">
        <xsl:param name="stylesheet" as="item()?"/> <!-- xs:string|attribute() -->
        <xsl:sequence select="ParseStylesheet:parse($stylesheet)"
                      xmlns:ParseStylesheet="org.daisy.pipeline.braille.css.saxon.impl.ParseStylesheetDefinition$ParseStylesheet">
            <!--
                Implemented in ../../java/org/daisy/pipeline/braille/css/saxon/impl/ParseStylesheetDefinition.java
            -->
        </xsl:sequence>
    </xsl:function>
    <xsl:function name="css:parse-stylesheet" as="item()?">
        <xsl:param name="stylesheet" as="item()?"/> <!-- xs:string|attribute() -->
        <xsl:param name="parent" as="item()?"/>
        <xsl:sequence select="ParseStylesheet:parse($stylesheet,$parent)"
                      xmlns:ParseStylesheet="org.daisy.pipeline.braille.css.saxon.impl.ParseStylesheetDefinition$ParseStylesheet">
            <!--
                Implemented in ../../java/org/daisy/pipeline/braille/css/saxon/impl/ParseStylesheetDefinition.java
            -->
        </xsl:sequence>
    </xsl:function>
    
    <xsl:function name="css:parse-string" as="element()?">
        <xsl:param name="string" as="xs:string"/>
        <xsl:if test="matches($string,concat('^(',$css:STRING_RE,')$'))">
            <css:string value="{replace(replace(replace(
                                  substring($string, 2, string-length($string)-2),
                                  '\\A\s?','&#xA;'),
                                  '\\27\s?',''''),
                                  '\\22\s?','&quot;')}"/>
        </xsl:if>
    </xsl:function>
    
    <!-- =========== -->
    <!-- Serializing -->
    <!-- =========== -->
    
    <xsl:template match="css:rule" mode="css:serialize" as="xs:string">
        <xsl:param name="base" as="xs:string*" select="()"/>
        <xsl:param name="level" as="xs:integer" select="1"/>
        <xsl:param name="indent" as="xs:string?" select="()"/>
        <xsl:variable name="newline" as="xs:string"
                      select="if (exists($indent)) then string-join(('&#xa;',for $i in 2 to $level return $indent),'')
                              else ' '"/>
        <xsl:choose>
            <xsl:when test="not(@selector)">
                <xsl:sequence select="css:serialize-stylesheet(*,$base,$level,$indent)"/>
            </xsl:when>
            <xsl:when test="exists($base) and not(matches(@selector,'^&amp;'))">
                <xsl:sequence select="string-join((
                                        string-join($base,', '),' {',$newline,$indent,
                                        css:serialize-stylesheet(
                                          *,
                                          @selector,
                                          $level+1,
                                          $indent),
                                        $newline,'}'),'')"/>
            </xsl:when>
            <xsl:otherwise> <!-- matches(@selector,'^&amp;') -->
                <xsl:sequence select="css:serialize-stylesheet(
                                        *,
                                        if (exists($base))
                                          then for $s in @selector return
                                               for $b in $base return
                                                 for $bb in tokenize($b,'\s*,\s*') return
                                                   concat($bb,substring($s,2))
                                          else @selector,
                                        $level,
                                        $indent)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="css:property[@value]" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat(@name,': ',@value)"/>
    </xsl:template>
    
    <xsl:template match="css:property[@name='content' and not(@value)]|
                         css:property[@name=('counter-set','counter-reset','counter-increment') and not(@value)]"
                  mode="css:serialize" as="xs:string">
        <xsl:variable name="value" as="xs:string*">
            <xsl:apply-templates mode="#current" select="*"/>
        </xsl:variable>
        <xsl:variable name="property" as="element(css:property)">
            <xsl:copy>
                <xsl:sequence select="@name"/>
                <xsl:attribute name="value" select="if (exists($value)) then string-join($value,' ') else 'none'"/>
            </xsl:copy>
        </xsl:variable>
        <xsl:apply-templates mode="#current" select="$property"/>
    </xsl:template>
    
    <xsl:template match="css:property[@name='string-set' and not(@value)]" mode="css:serialize" as="xs:string">
        <xsl:variable name="value" as="xs:string*">
            <xsl:apply-templates mode="#current" select="*"/>
        </xsl:variable>
        <xsl:variable name="property" as="element(css:property)">
            <xsl:copy>
                <xsl:sequence select="@name"/>
                <xsl:attribute name="value" select="if (exists($value)) then string-join($value,', ') else 'none'"/>
            </xsl:copy>
        </xsl:variable>
        <xsl:apply-templates mode="#current" select="$property"/>
    </xsl:template>
    
    <xsl:template match="css:string-set" mode="css:serialize" as="xs:string">
        <xsl:variable name="value" as="xs:string*">
            <xsl:apply-templates mode="#current" select="*"/>
        </xsl:variable>
        <xsl:sequence select="concat(@name,' ',if (exists($value)) then string-join($value,' ') else '&quot;&quot;')"/>
    </xsl:template>
    
    <xsl:template match="css:counter-set" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat(@name,' ',@value)"/>
    </xsl:template>
    
    <xsl:template match="css:string[@value]" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat('&quot;',
                                     replace(replace(
                                       @value,
                                       '\n','\\A '),
                                       '&quot;','\\22 '),
                                     '&quot;')"/>
    </xsl:template>
    
    <xsl:template match="css:content[not(@target|@target-attribute)]" mode="css:serialize" as="xs:string">
        <xsl:sequence select="'content()'"/>
    </xsl:template>
    
    <xsl:template match="css:content[@target]" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat('target-content(url(&quot;',@target,'&quot;))')"/>
    </xsl:template>
    
    <xsl:template match="css:content[@target-attribute]" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat('target-content(attr(',@target-attribute,' url))')"/>
    </xsl:template>
    
    <xsl:template match="css:attr" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat('attr(',@name,')')"/>
    </xsl:template>
    
    <xsl:template match="css:string[@name][not(@target|@target-attribute)]" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat('string(',@name,if (@scope) then concat(', ', @scope) else '',')')"/>
    </xsl:template>
    
    <xsl:template match="css:counter" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat('counter(',@name,if (@style) then concat(', ', @style) else '',')')"/>
    </xsl:template>
    
    <xsl:template match="css:text[@target]" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat('target-text(url(&quot;',@target,'&quot;))')"/>
    </xsl:template>
    
    <xsl:template match="css:text[@target-attribute]" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat('target-text(attr(',@target-attribute,' url))')"/>
    </xsl:template>
    
    <xsl:template match="css:string[@name][@target]" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat('target-string(url(&quot;',@target,'&quot;), ',@name,')')"/>
    </xsl:template>
    
    <xsl:template match="css:string[@name][@target-attribute]" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat('target-string(attr(',@target-attribute,' url), ',@name,')')"/>
    </xsl:template>
    
    <xsl:template match="css:counter[@target]" mode="css:serialize" as="xs:string">
        <xsl:variable name="target" as="xs:string" select="(@original-target,@target)[1]"/>
        <xsl:sequence select="concat('target-counter(url(&quot;',$target,'&quot;), ',@name,if (@style) then concat(', ', @style) else '',')')"/>
    </xsl:template>
    
    <xsl:template match="css:counter[@target-attribute]" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat('target-counter(attr(',@target-attribute,' url), ',@name,if (@style) then concat(', ', @style) else '',')')"/>
    </xsl:template>
    
    <xsl:template match="css:leader" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat('leader(',string-join((concat('&quot;',@pattern,'&quot;'),@position,@alignment),', '),')')"/>
    </xsl:template>
    
    <xsl:template match="css:flow[@from]" mode="css:serialize" as="xs:string">
        <xsl:sequence select="string-join(('flow(',@from,if (@scope) then (', ',@scope) else (),')'),'')"/>
    </xsl:template>
    
    <xsl:template match="css:custom-func" mode="css:serialize" as="xs:string">
        <xsl:sequence select="concat(
                                @name,
                                '(',
                                string-join(for $i in 1 to 10 return @*[name()=concat('arg',$i)]/string(),', '),
                                ')')"/>
    </xsl:template>
    
    <xsl:function name="css:serialize-stylesheet" as="xs:string">
        <xsl:param name="rules" as="element()*"/> <!-- css:rule*|css:property* -->
        <xsl:sequence select="css:serialize-stylesheet($rules,())"/>
    </xsl:function>
    
    <xsl:function name="css:serialize-stylesheet" as="xs:string">
        <xsl:param name="rules" as="element()*"/> <!-- css:rule*|css:property* -->
        <xsl:param name="base" as="xs:string*"/>
        <xsl:sequence select="css:serialize-stylesheet($rules,$base,1)"/>
    </xsl:function>
    
    <xsl:function name="css:serialize-stylesheet" as="xs:string">
        <xsl:param name="rules" as="element()*"/> <!-- css:rule*|css:property* -->
        <xsl:param name="base" as="xs:string*"/>
        <xsl:param name="level" as="xs:integer"/>
        <xsl:sequence select="css:serialize-stylesheet($rules,$base,$level,())"/>
    </xsl:function>
    
    <xsl:function name="css:serialize-stylesheet" as="xs:string">
        <xsl:param name="rules" as="element()*"/> <!-- css:rule*|css:property* -->
        <xsl:param name="base" as="xs:string*"/>
        <xsl:param name="level" as="xs:integer"/>
        <xsl:param name="indent" as="xs:string?"/>
        <xsl:variable name="newline" as="xs:string"
                      select="if (exists($indent)) then string-join(('&#xa;',for $i in 2 to $level return $indent),'') else ' '"/>
        <xsl:variable name="serialized-pseudo-rules" as="xs:string*">
            <!-- also includes rules with relative selector -->
            <xsl:apply-templates select="$rules[self::css:rule and @selector[matches(.,'^&amp;')]]" mode="css:serialize">
                <xsl:with-param name="base" select="$base"/>
                <xsl:with-param name="level" select="$level"/>
                <xsl:with-param name="indent" select="$indent"/>
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:variable name="serialized-at-rules" as="xs:string*">
            <xsl:apply-templates select="$rules[self::css:rule and @selector[not(matches(.,'^&amp;'))]]" mode="css:serialize">
                <xsl:with-param name="level" select="if (exists($base)) then $level+1 else $level"/>
                <xsl:with-param name="indent" select="$indent"/>
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:variable name="serialized-declarations" as="xs:string*">
            <xsl:apply-templates mode="css:serialize"
                                 select="$rules[(self::css:rule and not(@selector)) or self::css:property]">
                <xsl:with-param name="level" select="if (exists($base)) then $level+1 else $level"/>
                <xsl:with-param name="indent" select="$indent"/>
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:variable name="serialized-rules" as="xs:string*">
            <xsl:choose>
                <xsl:when test="exists($base)">
                    <xsl:variable name="serialized-inner-rules" as="xs:string*">
                        <xsl:if test="exists($serialized-declarations)">
                            <xsl:sequence select="concat(
                                                    string-join($serialized-declarations,string-join((';',$newline,$indent),'')),
                                                    if (exists($serialized-at-rules))
                                                      then ';'
                                                      else '')"/>
                        </xsl:if>
                        <xsl:sequence select="$serialized-at-rules"/>
                    </xsl:variable>
                    <xsl:if test="exists($serialized-inner-rules)">
                        <xsl:sequence select="string-join((
                                                string-join($base,', '),' {',$newline,$indent,
                                                string-join($serialized-inner-rules,string-join(($newline,$indent),'')),
                                                $newline,'}'),'')"/>
                    </xsl:if>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:if test="exists($serialized-declarations)">
                        <xsl:sequence select="concat(
                                                string-join($serialized-declarations,string-join((';',$newline),'')),
                                                if (exists($serialized-at-rules) or exists($serialized-pseudo-rules))
                                                  then ';'
                                                  else '')"/>
                    </xsl:if>
                    <xsl:sequence select="$serialized-at-rules"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:sequence select="$serialized-pseudo-rules"/>
        </xsl:variable>
        <xsl:sequence select="string-join($serialized-rules,$newline)"/>
    </xsl:function>
    
    <xsl:function name="css:style-attribute" as="attribute(style)?">
        <xsl:param name="style" as="item()?"/>
        <xsl:if test="$style">
            <xsl:variable name="style" as="xs:string" select="string($style)"/>
            <xsl:if test="not($style='')">
                <xsl:attribute name="style" select="$style"/>
            </xsl:if>
        </xsl:if>
    </xsl:function>
    
    <!-- ======= -->
    <!-- Strings -->
    <!-- ======= -->
    
    <xsl:function name="css:string" as="element()*">
        <xsl:param name="name" as="xs:string"/>
        <xsl:param name="context" as="element()"/>
        <xsl:variable name="last-set" as="element()?"
                      select="$context/(self::*|preceding::*|ancestor::*)
                              [contains(@css:string-set,$name)]
                              [last()]"/>
        <xsl:choose>
            <xsl:when test="$context/ancestor::*/@css:flow[not(.='normal')]">
                <xsl:choose>
                    <xsl:when test="$last-set
                                    intersect $context/ancestor::*[@css:anchor][1]/descendant-or-self::*">
                        <xsl:variable name="value" as="element(css:string-set)?"
                                      select="s:toXml(css:parse-stylesheet($last-set/@css:string-set))/css:string-set
                                              [@name=$name][last()]"/>
                        <xsl:choose>
                            <xsl:when test="exists($value)">
                                <xsl:sequence select="$value/*"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:variable name="context" as="element()?"
                                              select="$last-set/(preceding::*|ancestor::*)[last()]
                                                      intersect $context/ancestor::*[@css:anchor][1]/descendant-or-self::*"/>
                                <xsl:if test="$context">
                                    <xsl:sequence select="css:string($name, $context)"/>
                                </xsl:if>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="anchor" as="xs:string" select="$context/ancestor::*/@css:anchor"/>
                        <xsl:variable name="context" as="element()?" select="collection()//*[@css:id=$anchor][1]"/>
                        <xsl:if test="$context">
                            <xsl:sequence select="css:string($name, $context)"/>
                        </xsl:if>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="$last-set">
                <xsl:variable name="value" as="element(css:string-set)?"
                              select="s:toXml(css:parse-stylesheet($last-set/@css:string-set))/css:string-set
                                      [@name=$name][last()]"/>
                <xsl:choose>
                    <xsl:when test="exists($value)">
                        <xsl:sequence select="$value/*"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="context" as="element()?" select="$last-set/(preceding::*|ancestor::*)[last()]"/>
                        <xsl:if test="$context">
                            <xsl:sequence select="css:string($name, $context)"/>
                        </xsl:if>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
        </xsl:choose>
    </xsl:function>
    
</xsl:stylesheet>
