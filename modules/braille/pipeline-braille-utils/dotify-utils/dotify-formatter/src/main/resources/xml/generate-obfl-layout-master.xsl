<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.daisy.org/ns/2011/obfl"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:obfl="http://www.daisy.org/ns/2011/obfl"
                xmlns:re="regex-utils"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>
    
    <xsl:variable name="empty-string" as="element()">
        <string value=""/>
    </xsl:variable>
    
    <xsl:variable name="empty-field" as="element()">
        <field>
            <xsl:sequence select="$empty-string"/>
        </field>
    </xsl:variable>
    
    <xsl:function name="obfl:generate-layout-master">
        <xsl:param name="page-stylesheet" as="xs:string"/>
        <xsl:param name="name" as="xs:string"/>
        <xsl:variable name="duplex" as="xs:boolean" select="true()"/>
        <xsl:sequence select="obfl:generate-layout-master($page-stylesheet, $name, $duplex)"/>
    </xsl:function>
    
    <xsl:function name="obfl:generate-layout-master">
        <xsl:param name="page-stylesheet" as="xs:string"/>
        <xsl:param name="name" as="xs:string"/>
        <xsl:param name="duplex" as="xs:boolean"/>
        <xsl:variable name="right-page-odd" as="xs:boolean" select="true()"/>
        <xsl:sequence select="obfl:generate-layout-master($page-stylesheet, $name, $duplex, $right-page-odd)"/>
    </xsl:function>
    
    <xsl:function name="obfl:generate-layout-master">
        <xsl:param name="page-stylesheet" as="xs:string"/>
        <xsl:param name="name" as="xs:string"/>
        <xsl:param name="duplex" as="xs:boolean"/>
        <xsl:param name="right-page-odd" as="xs:boolean"/>
        <xsl:variable name="page-stylesheet" as="element()*" select="css:parse-stylesheet($page-stylesheet)"/>
        <xsl:variable name="right-page-stylesheet" as="element()*" select="css:parse-stylesheet($page-stylesheet[@selector=':right']/@style)"/>
        <xsl:variable name="left-page-stylesheet" as="element()*" select="css:parse-stylesheet($page-stylesheet[@selector=':left']/@style)"/>
        <xsl:variable name="default-page-stylesheet" as="element()*">
            <xsl:choose>
                <xsl:when test="$right-page-stylesheet or $left-page-stylesheet">
                    <xsl:sequence select="css:parse-stylesheet($page-stylesheet[not(@selector)]/@style)"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="$page-stylesheet"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="default-page-properties" as="element()*"
                      select="css:parse-declaration-list($default-page-stylesheet[not(@selector)]/@style)"/>
        <xsl:variable name="size" as="xs:string"
                      select="($default-page-properties[@name='size'][css:is-valid(.)]/@value, css:initial-value('size'))[1]"/>
        <xsl:variable name="page-width" as="xs:integer" select="xs:integer(number(tokenize($size, '\s+')[1]))"/>
        <xsl:variable name="page-height" as="xs:integer" select="xs:integer(number(tokenize($size, '\s+')[2]))"/>
        <xsl:variable name="footnotes-properties" as="element()*"
                      select="css:parse-declaration-list($default-page-stylesheet[@selector='@footnotes'][1]/@style)"/>
        <xsl:variable name="footnotes-content" as="element()*"
                      select="css:parse-content-list($footnotes-properties[@name='content'][1]/@value,())"/>
        <layout-master name="{$name}" duplex="{$duplex}" page-number-variable="page"
                       page-width="{$page-width}" page-height="{$page-height}">
            <xsl:if test="$right-page-stylesheet">
                <template use-when="(= (% $page 2) {if ($right-page-odd) then 1 else 0})">
                    <xsl:call-template name="template">
                        <xsl:with-param name="stylesheet" select="$right-page-stylesheet"/>
                        <xsl:with-param name="page-side" tunnel="yes" select="'right'"/>
                    </xsl:call-template>
                </template>
            </xsl:if>
            <xsl:if test="$left-page-stylesheet">
                <template use-when="(= (% $page 2) {if ($right-page-odd) then 0 else 1})">
                    <xsl:call-template name="template">
                        <xsl:with-param name="stylesheet" select="$left-page-stylesheet"/>
                        <xsl:with-param name="page-side" tunnel="yes" select="'left'"/>
                    </xsl:call-template>
                </template>
            </xsl:if>
            <default-template>
                <xsl:call-template name="template">
                    <xsl:with-param name="stylesheet" select="$default-page-stylesheet"/>
                </xsl:call-template>
            </default-template>
            <xsl:if test="$footnotes-content[not(self::css:flow[@from])]">
                <xsl:message>only flow() function supported in footnotes area</xsl:message>
            </xsl:if>
            <xsl:if test="count($footnotes-content[self::css:flow[@from]]) > 1">
                <xsl:message>not more than one flow() function supported in footnotes area</xsl:message>
            </xsl:if>
             <!--
                 default scope within footnotes area is 'page'
             -->
            <xsl:if test="$footnotes-content[self::css:flow[@from and @scope[not(.='page')]]]">
                <xsl:message select="concat(@scope,' argument of flow() function not allowed within footnotes area')"/>
            </xsl:if>
            <xsl:for-each select="$footnotes-content[self::css:flow[@from]][1]">
                <xsl:variable name="footnotes-border-top" as="xs:string"
                              select="($footnotes-properties[@name='border-top'][1]/@value,css:initial-value('border-top'))[1]"/>
                <xsl:variable name="footnotes-max-height" as="xs:string"
                              select="($footnotes-properties[@name='max-height'][1]/@value,css:initial-value('max-height'))[1]"/>
                <xsl:variable name="footnotes-max-height" as="xs:integer"
                              select="if ($footnotes-max-height='none')
                                      then $page-height idiv 2
                                      else xs:integer(number($footnotes-max-height))"/>
                <xsl:variable name="footnotes-fallback-collection" as="xs:string?"
                              select="$footnotes-properties[@name=('-obfl-fallback-collection','-obfl-fallback-flow')][1]/@value"/>
                <xsl:if test="$footnotes-properties[@name='-obfl-fallback-flow']">
                    <xsl:message>Correct spelling of '-obfl-fallback-flow' is '-obfl-fallback-collection'</xsl:message>
                </xsl:if>
                <page-area align="bottom" max-height="{$footnotes-max-height}" collection="{@from}">
                    <xsl:if test="exists($footnotes-fallback-collection) and matches($footnotes-fallback-collection, re:exact($css:IDENT_RE))">
                        <fallback>
                            <rename collection="{@from}" to="{$footnotes-fallback-collection}"/>
                            <rename collection="meta/{@from}" to="meta/{$footnotes-fallback-collection}"/>
                            <xsl:variable name="footnotes-fallback-extra" as="xs:string?"
                                          select="$footnotes-properties[@name='-obfl-fallback-extra'][1]/@value"/>
                            <xsl:if test="exists($footnotes-fallback-extra)
                                          and matches($footnotes-fallback-extra, re:exact(re:comma-separated(concat($css:IDENT_RE,'\s+',$css:IDENT_RE))))">
                                <xsl:for-each select="tokenize($footnotes-fallback-extra,',')">
                                    <xsl:variable name="from-to" as="xs:string*" select="tokenize(normalize-space(.),'\s')"/>
                                    <rename collection="{$from-to[1]}" to="{$from-to[2]}"/>
                                </xsl:for-each>
                            </xsl:if>
                        </fallback>
                    </xsl:if>
                    <xsl:if test="$footnotes-border-top!='none'">
                        <before><leader pattern="{$footnotes-border-top}" position="100%" align="right"/></before>
                    </xsl:if>
                </page-area>
            </xsl:for-each>
        </layout-master>
    </xsl:function>
    
    <xsl:template name="template">
        <xsl:param name="stylesheet" as="element()*" required="yes"/>
        <xsl:variable name="top-left" as="element()*">
            <xsl:call-template name="fields">
                <xsl:with-param name="margin-stylesheet" select="$stylesheet[@selector='@top-left'][1]/@style"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="top-center" as="element()*">
            <xsl:call-template name="fields">
                <xsl:with-param name="margin-stylesheet" select="$stylesheet[@selector='@top-center'][1]/@style"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="top-right" as="element()*">
            <xsl:call-template name="fields">
                <xsl:with-param name="margin-stylesheet" select="$stylesheet[@selector='@top-right'][1]/@style"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="bottom-left" as="element()*">
            <xsl:call-template name="fields">
                <xsl:with-param name="margin-stylesheet" select="$stylesheet[@selector='@bottom-left'][1]/@style"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="bottom-center" as="element()*">
            <xsl:call-template name="fields">
                <xsl:with-param name="margin-stylesheet" select="$stylesheet[@selector='@bottom-center'][1]/@style"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="bottom-right" as="element()*">
            <xsl:call-template name="fields">
                <xsl:with-param name="margin-stylesheet" select="$stylesheet[@selector='@bottom-right'][1]/@style"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="properties" as="element()*"
                      select="css:parse-declaration-list($stylesheet[not(@selector)]/@style)"/>
        <xsl:variable name="margin-top" as="xs:integer"
                      select="max(($properties[@name='margin-top'][css:is-valid(.)]/xs:integer(@value),0))"/>
        <xsl:variable name="margin-bottom" as="xs:integer"
                      select="max(($properties[@name='margin-bottom'][css:is-valid(.)]/xs:integer(@value),0))"/>
        <xsl:variable name="margin-left" as="xs:integer"
                      select="max(($properties[@name='margin-left'][css:is-valid(.)]/xs:integer(@value),0))"/>
        <xsl:variable name="margin-right" as="xs:integer"
                      select="max(($properties[@name='margin-right'][css:is-valid(.)]/xs:integer(@value),0))"/>
        <xsl:choose>
            <xsl:when test="exists(($top-left, $top-center, $top-right)) or $margin-top &gt; 0">
                <xsl:call-template name="headers">
                    <xsl:with-param name="times" select="$margin-top"/>
                    <xsl:with-param name="left" select="$top-left"/>
                    <xsl:with-param name="center" select="$top-center"/>
                    <xsl:with-param name="right" select="$top-right"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <header/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:choose>
            <xsl:when test="exists(($bottom-left, $bottom-center, $bottom-right)) or $margin-bottom &gt; 0">
                <xsl:call-template name="footers">
                    <xsl:with-param name="times" select="$margin-bottom"/>
                    <xsl:with-param name="left" select="$bottom-left"/>
                    <xsl:with-param name="center" select="$bottom-center"/>
                    <xsl:with-param name="right" select="$bottom-right"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <footer/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:call-template name="margin-region">
            <xsl:with-param name="margin-stylesheet" select="$stylesheet[@selector='@left'][1]/@style"/>
            <xsl:with-param name="side" select="'left'"/>
            <xsl:with-param name="min-width" select="$margin-left"/>
        </xsl:call-template>
        <xsl:call-template name="margin-region">
            <xsl:with-param name="margin-stylesheet" select="$stylesheet[@selector='@right'][1]/@style"/>
            <xsl:with-param name="side" select="'right'"/>
            <xsl:with-param name="min-width" select="$margin-right"/>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template name="headers"> <!-- obfl:header* -->
        <xsl:param name="times" as="xs:integer" required="yes"/>
        <xsl:param name="left" as="element()*" required="yes"/> <!-- obfl:field* -->
        <xsl:param name="center" as="element()*" required="yes"/> <!-- obfl:field* -->
        <xsl:param name="right" as="element()*" required="yes"/> <!-- obfl:field* -->
        <xsl:if test="exists(($left, $center, $right)) or $times &gt; 0">
            <header>
                <xsl:sequence select="($left,$empty-field)[1]"/>
                <xsl:sequence select="($center,$empty-field)[1]"/>
                <xsl:sequence select="($right,$empty-field)[1]"/>
            </header>
            <xsl:call-template name="headers">
                <xsl:with-param name="times" select="$times - 1"/>
                <xsl:with-param name="left" select="$left[position()&gt;1]"/>
                <xsl:with-param name="center" select="$center[position()&gt;1]"/>
                <xsl:with-param name="right" select="$right[position()&gt;1]"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
    
    <xsl:template name="footers"> <!-- obfl:footer* -->
        <xsl:param name="times" as="xs:integer" required="yes"/>
        <xsl:param name="left" as="element()*" required="yes"/> <!-- obfl:field* -->
        <xsl:param name="center" as="element()*" required="yes"/> <!-- obfl:field* -->
        <xsl:param name="right" as="element()*" required="yes"/> <!-- obfl:field* -->
        <xsl:if test="exists(($left, $center, $right)) or $times &gt; 0">
            <xsl:call-template name="footers">
                <xsl:with-param name="times" select="$times - 1"/>
                <xsl:with-param name="left" select="$left[position()&lt;last()]"/>
                <xsl:with-param name="center" select="$center[position()&lt;last()]"/>
                <xsl:with-param name="right" select="$right[position()&lt;last()]"/>
            </xsl:call-template>
            <footer>
                <xsl:sequence select="($empty-field,$left)[last()]"/>
                <xsl:sequence select="($empty-field,$center)[last()]"/>
                <xsl:sequence select="($empty-field,$right)[last()]"/>
            </footer>
        </xsl:if>
    </xsl:template>
    
    <xsl:template name="fields" as="element()*"> <!-- obfl:field* -->
        <xsl:param name="margin-stylesheet" as="xs:string?"/>
        <xsl:variable name="properties" as="element()*" select="css:parse-declaration-list($margin-stylesheet)"/>
        <xsl:variable name="white-space" as="xs:string" select="($properties[@name='white-space']/@value,'normal')[1]"/>
        <xsl:variable name="text-transform" as="xs:string" select="($properties[@name='text-transform']/@value,'auto')[1]"/>
        <xsl:variable name="content" as="element()*">
            <xsl:apply-templates select="css:parse-content-list($properties[@name='content'][1]/@value,())" mode="eval-content-list-top-bottom">
                <xsl:with-param name="white-space" select="$white-space"/>
                <xsl:with-param name="text-transform" select="$text-transform"/>
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:for-each-group select="$content" group-ending-with="obfl:br">
            <field>
                <xsl:sequence select="if (current-group()[not(self::obfl:br)])
                                      then current-group()[not(self::obfl:br)]
                                      else $empty-string"/>
            </field>
        </xsl:for-each-group>
    </xsl:template>
    
    <xsl:template name="margin-region" as="element()?"> <!-- obfl:margin-region? -->
        <xsl:param name="margin-stylesheet" as="xs:string?"/>
        <xsl:param name="side" as="xs:string" required="yes"/>
        <xsl:param name="min-width" as="xs:integer" required="yes"/>
        <xsl:variable name="properties" as="element()*" select="css:parse-declaration-list($margin-stylesheet)"/>
        <xsl:variable name="indicators" as="element()*">
            <xsl:apply-templates select="css:parse-content-list($properties[@name='content'][1]/@value,())" mode="eval-content-list-left-right"/>
        </xsl:variable>
        <xsl:if test="exists($indicators) or $min-width &gt; 0">
            <margin-region align="{$side}" width="{max((count($indicators),$min-width))}">
                <indicators>
                    <xsl:sequence select="$indicators"/>
                </indicators>
            </margin-region>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="css:string[@value]" mode="eval-content-list-top-bottom">
        <xsl:param name="white-space" as="xs:string" select="'normal'"/>
        <xsl:param name="text-transform" as="xs:string" select="'auto'"/>
        <xsl:choose>
            <xsl:when test="$white-space=('pre-wrap','pre-line')">
                <!--
                    TODO: wrapping is not allowed, warn if content is clipped
                -->
                <xsl:analyze-string select="string(@value)" regex="\n">
                    <xsl:matching-substring>
                        <br/>
                    </xsl:matching-substring>
                    <xsl:non-matching-substring>
                        <xsl:choose>
                            <xsl:when test="$white-space='pre-wrap'">
                                <string value="{replace(.,'\s','&#x00A0;')}">
                                    <xsl:if test="not($text-transform=('none','auto'))">
                                        <xsl:attribute name="text-style" select="concat('text-transform:',$text-transform)"/>
                                    </xsl:if>
                                </string>
                            </xsl:when>
                            <xsl:otherwise>
                                <string value="{.}">
                                    <xsl:if test="not($text-transform=('none','auto'))">
                                        <xsl:attribute name="text-style" select="concat('text-transform:',$text-transform)"/>
                                    </xsl:if>
                                </string>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:non-matching-substring>
                </xsl:analyze-string>
            </xsl:when>
            <xsl:otherwise>
                <string value="{string(@value)}">
                    <xsl:if test="not($text-transform=('none','auto'))">
                        <xsl:attribute name="text-style" select="concat('text-transform:',$text-transform)"/>
                    </xsl:if>
                </string>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="css:counter[not(@target)][@name='page']" mode="eval-content-list-top-bottom">
        <xsl:param name="white-space" as="xs:string" select="'normal'"/>
        <xsl:param name="text-transform" as="xs:string" select="'auto'"/>
        <xsl:if test="$white-space!='normal'">
            <xsl:message select="concat('white-space:',$white-space,' could not be applied to target-counter(',@name,')')"/>
        </xsl:if>
        <current-page number-format="{if (@style=('roman', 'upper-roman', 'lower-roman', 'upper-alpha', 'lower-alpha'))
                                      then @style else 'default'}">
            <xsl:choose>
                <xsl:when test="matches(@style,re:exact($css:SYMBOLS_FN_RE))">
                    <xsl:choose>
                        <xsl:when test="not($text-transform=('none','auto'))">
                            <xsl:attribute name="text-style"
                                           select="concat('text-transform: -dotify-counter ',$text-transform,'; ',
                                                          '-dotify-counter-style: ',@style)"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="text-style" select="concat('text-transform: -dotify-counter; ',
                                                                            '-dotify-counter-style: ',@style)"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:when test="not($text-transform=('none','auto'))">
                    <xsl:attribute name="text-style" select="concat('text-transform: ',$text-transform)"/>
                </xsl:when>
            </xsl:choose>
        </current-page>
    </xsl:template>
    
    <xsl:template match="css:string[@name][not(@target)]" mode="eval-content-list-top-bottom">
        <xsl:param name="white-space" as="xs:string" tunnel="yes" select="'normal'"/>
        <xsl:param name="text-transform" as="xs:string" select="'auto'"/>
        <xsl:param name="page-side" as="xs:string" tunnel="yes" select="'both'"/>
        <xsl:if test="$white-space!='normal'">
            <xsl:message select="concat('white-space:',$white-space,' could not be applied to target-string(',@name,')')"/>
        </xsl:if>
        <xsl:variable name="scope" select="(@scope,'first')[1]"/>
        <xsl:if test="$page-side='both' and $scope=('spread-first','spread-start','spread-last','spread-last-except-start')">
            <!--
                FIXME: force creation of templates for left and right pages when margin
                content contains "string(foo, spread-last)"
            -->
            <xsl:message terminate="yes"
                         select="concat('string(',@name,', ',$scope,') on both left and right side currently not supported')"/>
        </xsl:if>
        <xsl:variable name="var-name" as="xs:string" select="concat('tmp_',generate-id(.))"/>
        <xsl:variable name="text-transform-decl" as="xs:string" select="if (not($text-transform=('none','auto')))
                                                                        then concat(' text-transform:',$text-transform)
                                                                        else ''"/>
        <xsl:choose>
            <xsl:when test="$scope=('first','page-first')">
                <marker-reference marker="{@name}" direction="forward" scope="page"
                                  text-style="-dotify-def:{$var-name};{$text-transform-decl}"/>
                <!--
                    FIXME: replace with scope="document" and remove second marker-reference
                -->
                <marker-reference marker="{@name}" direction="backward" scope="sequence"
                                  text-style="-dotify-defifndef:{$var-name};{$text-transform-decl}"/>
                <marker-reference marker="{@name}/entry" direction="backward" scope="sequence"
                                  text-style="-dotify-ifndef:{$var-name};{$text-transform-decl}"/>
            </xsl:when>
            <xsl:when test="$scope=('start','page-start')">
                <!--
                    Note that this behavior is not exactly according to the spec (see
                    https://github.com/snaekobbi/pipeline-mod-dedicon/issues/49)
                -->
                <!--
                    FIXME: scope="page-content" does not work as expected
                -->
                <marker-reference marker="{@name}/prev" direction="forward" scope="page-content"
                                  text-style="-dotify-def:{$var-name};{$text-transform-decl}"/>
                <!--
                    TODO: check that this does not match too much at the end of the page!
                    FIXME: replace with scope="document" and remove second marker-reference
                -->
                <marker-reference marker="{@name}" direction="backward" scope="sequence"
                                  text-style="-dotify-defifndef:{$var-name};{$text-transform-decl}"/>
                <marker-reference marker="{@name}/entry" direction="backward" scope="sequence"
                                  text-style="-dotify-ifndef:{$var-name};{$text-transform-decl}"/>
            </xsl:when>
            <xsl:when test="$scope=('start-except-last','page-start-except-last')">
                <!--
                    Note that this behavior is not exactly according to the spec (see
                    https://github.com/snaekobbi/pipeline-mod-dedicon/issues/49)
                -->
                <!--
                    FIXME: scope="page-content" does not work as expected
                -->
                <marker-reference marker="{@name}/prev" direction="forward" scope="page-content">
                    <xsl:if test="not($text-transform=('none','auto'))">
                        <xsl:attribute name="text-style" select="concat('text-transform:',$text-transform)"/>
                    </xsl:if>
                </marker-reference>
            </xsl:when>
            <xsl:when test="$scope=('last','page-last')">
                <!--
                    FIXME: replace with scope="document" and remove second marker-reference
                -->
                <marker-reference marker="{@name}" direction="backward" scope="sequence"
                                  text-style="-dotify-def:{$var-name};{$text-transform-decl}"/>
                <marker-reference marker="{@name}/entry" direction="backward" scope="sequence"
                                  text-style="-dotify-ifndef:{$var-name};{$text-transform-decl}"/>
            </xsl:when>
            <xsl:when test="$scope=('last-except-start','page-last-except-start')">
                <xsl:message terminate="yes"
                             select="concat('string(',@name,', ',$scope,') currently not supported. If you want to use it in combination with string(start), please consider using the combination start-except-last/last instead.')"/>
            </xsl:when>
            <xsl:when test="$scope='spread-first'">
                <marker-reference marker="{@name}" direction="forward" scope="spread"
                                  text-style="-dotify-def:{$var-name};{$text-transform-decl}">
                    <xsl:if test="$page-side='right'">
                        <xsl:attribute name="start-offset" select="'-1'"/>
                    </xsl:if>
                </marker-reference>
                <!--
                    FIXME: replace with scope="document" and remove second marker-reference
                -->
                <marker-reference marker="{@name}" direction="backward" scope="sequence"
                                  text-style="-dotify-defifndef:{$var-name};{$text-transform-decl}">
                    <xsl:if test="$page-side='right'">
                        <xsl:attribute name="start-offset" select="'-1'"/>
                    </xsl:if>
                </marker-reference>
                <marker-reference marker="{@name}/entry" direction="backward" scope="sequence"
                                  text-style="-dotify-ifndef:{$var-name};{$text-transform-decl}"/>
            </xsl:when>
            <xsl:when test="$scope='spread-start'">
                <!--
                    Note that this behavior is not exactly according to the spec (see
                    https://github.com/snaekobbi/pipeline-mod-dedicon/issues/49)
                -->
                <!--
                    FIXME: scope="spread-content"
                -->
                <marker-reference marker="{@name}/prev" direction="forward" scope="spread"
                                  text-style="-dotify-def:{$var-name};{$text-transform-decl}">
                    <xsl:if test="$page-side='right'">
                        <xsl:attribute name="start-offset" select="'-1'"/>
                    </xsl:if>
                </marker-reference>
                <!--
                    TODO: check that this does not match too much at the end of the page!
                    FIXME: replace with scope="document" and remove second marker-reference
                -->
                <marker-reference marker="{@name}" direction="backward" scope="sequence"
                                  text-style="-dotify-defifndef:{$var-name};{$text-transform-decl}">
                    <xsl:if test="$page-side='left'">
                        <xsl:attribute name="start-offset" select="'1'"/>
                    </xsl:if>
                </marker-reference>
                <marker-reference marker="{@name}/entry" direction="backward" scope="sequence"
                                  text-style="-dotify-ifndef:{$var-name};{$text-transform-decl}"/>
            </xsl:when>
            <xsl:when test="$scope='spread-start-except-last'">
                <!--
                    Note that this behavior is not exactly according to the spec (see
                    https://github.com/snaekobbi/pipeline-mod-dedicon/issues/49)
                -->
                <!--
                    FIXME: scope="spread-content"
                -->
                <marker-reference marker="{@name}/prev" direction="forward" scope="spread">
                    <xsl:if test="$page-side='right'">
                        <xsl:attribute name="start-offset" select="'-1'"/>
                    </xsl:if>
                    <xsl:if test="not($text-transform=('none','auto'))">
                        <xsl:attribute name="text-style" select="concat('text-transform:',$text-transform)"/>
                    </xsl:if>
                </marker-reference>
            </xsl:when>
            <xsl:when test="$scope='spread-last'">
                <!--
                    FIXME: replace with scope="document" and remove second marker-reference
                -->
                <marker-reference marker="{@name}" direction="backward" scope="sequence"
                                  text-style="-dotify-def:{$var-name};{$text-transform-decl}">
                    <xsl:if test="$page-side='left'">
                        <xsl:attribute name="start-offset" select="'1'"/>
                    </xsl:if>
                </marker-reference>
                <marker-reference marker="{@name}/entry" direction="backward" scope="sequence"
                                  text-style="-dotify-ifndef:{$var-name};{$text-transform-decl}"/>
            </xsl:when>
            <xsl:when test="$scope='spread-last-except-start'">
                <xsl:message terminate="yes"
                             select="concat('string(',@name,', ',$scope,') currently not supported. If you want to use it in combination with string(start), please consider using the combination start-except-last/last instead.')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message terminate="yes"
                             select="concat('in function string(',@name,', ',$scope,'): unknown keyword &quot;',$scope,'&quot;')"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="css:custom-func[@name='-obfl-marker-indicator'][matches(@arg2,$css:STRING_RE) and not(@arg3)]"
                  mode="eval-content-list-left-right" priority="1">
        <marker-indicator markers="indicator/{@arg1}" indicator="{substring(@arg2,2,string-length(@arg2)-2)}"/>
    </xsl:template>
    
    <xsl:template match="css:custom-func[@name='-obfl-marker-indicator']" mode="eval-content-list-left-right">
        <xsl:message>-obfl-marker-indicator() function requires exactly two arguments</xsl:message>
    </xsl:template>
    
    <xsl:template match="css:attr" mode="eval-content-list-top-bottom eval-content-list-left-right">
        <xsl:message>attr() function not supported in page margin</xsl:message>
    </xsl:template>
    
    <xsl:template match="css:content[not(@target)]" mode="eval-content-list-top-bottom eval-content-list-left-right">
        <xsl:message>content() function not supported in page margin</xsl:message>
    </xsl:template>
    
    <xsl:template match="css:content[@target]" mode="eval-content-list-top-bottom eval-content-list-left-right">
        <xsl:message>target-content() function not supported in page margin</xsl:message>
    </xsl:template>
    
    <xsl:template match="css:text[@target]" mode="eval-content-list-top-bottom eval-content-list-left-right">
        <xsl:message>target-text() function not supported in page margin</xsl:message>
    </xsl:template>
    
    <xsl:template match="css:string[@value]" mode="eval-content-list-left-right">
        <xsl:message>strings not supported in left and right page margin</xsl:message>
    </xsl:template>
    
    <xsl:template match="css:string[@name][not(@target)]" mode="eval-content-list-left-right">
        <xsl:message>string() function not supported in left and right page margin</xsl:message>
    </xsl:template>
    
    <xsl:template match="css:string[@name][@target]" mode="eval-content-list-top-bottom eval-content-list-left-right">
        <xsl:message>target-string() function not supported in page margin</xsl:message>
    </xsl:template>
    
    <xsl:template match="css:counter[@target]" mode="eval-content-list-top-bottom eval-content-list-left-right">
        <xsl:message>target-counter() function not supported in page margin</xsl:message>
    </xsl:template>
    
    <xsl:template match="css:counter[not(@target)]" mode="eval-content-list-left-right">
        <xsl:message>counter() function not supported in left and right page margin</xsl:message>
    </xsl:template>
    
    <xsl:template match="css:counter[not(@target)][not(@name='page')]" mode="eval-content-list-top-bottom">
        <xsl:message>counter() function not supported in page margin for other counters than 'page'</xsl:message>
    </xsl:template>
    
    <xsl:template match="css:leader" mode="eval-content-list-top-bottom eval-content-list-left-right">
        <xsl:message>leader() function not supported in page margin</xsl:message>
    </xsl:template>
    
    <xsl:template match="css:flow[@from]" mode="eval-content-list-top-bottom eval-content-list-left-right">
        <xsl:message>flow() function not supported in page margin</xsl:message>
    </xsl:template>
    
    <xsl:template match="css:custom-func[@name='-obfl-marker-indicator']" mode="eval-content-list-top-bottom">
        <xsl:message>-obfl-marker-indicator() function not supported in top and bottom page margin</xsl:message>
    </xsl:template>
    
    <xsl:template match="*" mode="eval-content-list-top-bottom eval-content-list-left-right">
        <xsl:message terminate="yes">Coding error</xsl:message>
    </xsl:template>
    
</xsl:stylesheet>
