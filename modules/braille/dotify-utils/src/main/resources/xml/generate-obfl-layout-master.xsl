<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.daisy.org/ns/2011/obfl"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:obfl="http://www.daisy.org/ns/2011/obfl"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:re="regex-utils"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xsl"/>
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>
    <xsl:include href="marker-reference.xsl"/>
    
    <xsl:param name="duplex" as="xs:string" required="yes"/>
    
    <xsl:variable name="page-stylesheets" as="element(css:rule)*" select="/*/css:rule[@selector='@page']"/>
    
    <xsl:function name="pxi:layout-master-name" as="xs:string">
        <xsl:param name="page-stylesheet" as="xs:string"/>
        <xsl:sequence select="concat('master_',
                                     index-of($page-stylesheets/@style, $page-stylesheet)[1])"/>
    </xsl:function>
    
    <xsl:template match="/*">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:variable name="sequences" as="element()*" select="//obfl:sequence|//obfl:toc-sequence|//obfl:dynamic-sequence"/>
            <xsl:for-each select="distinct-values($sequences/@css:page)">
                <xsl:variable name="layout-master-name" select="pxi:layout-master-name(.)"/>
                <xsl:variable name="page-stylesheet" as="element()" select="$page-stylesheets[@style=current()][1]"/>
                <xsl:variable name="default-page-counter-names"
                              select="distinct-values(
                                        for $s in $sequences[@css:page=current()] return
                                          if ($s/parent::obfl:pre-content) then 'pre-page'
                                          else if ($s/parent::obfl:post-content) then 'post-page'
                                          else 'page')"/>
                <xsl:apply-templates mode="obfl:generate-layout-master" select="$page-stylesheet">
                    <xsl:with-param name="name" tunnel="yes" select="$layout-master-name"/>
                    <xsl:with-param name="default-page-counter-name" tunnel="yes" select="$default-page-counter-names[1]"/>
                </xsl:apply-templates>
                <!--
                    The result of calling obfl:generate-layout-master is the same regardless of the
                    default-page-counter-name passed. Therefore only the first result is
                    relevant. However, all calls need to be made anyway because the function checks
                    that there are no mismatches between the active page counter and counter() calls
                    in page margins.
                -->
                <xsl:for-each select="$default-page-counter-names[position()&gt;1]">
                    <xsl:variable name="_">
                        <xsl:apply-templates mode="obfl:generate-layout-master" select="$page-stylesheet">
                            <xsl:with-param name="name" tunnel="yes" select="$layout-master-name"/>
                            <xsl:with-param name="default-page-counter-name" tunnel="yes" select="."/>
                        </xsl:apply-templates>
                    </xsl:variable>
                </xsl:for-each>
            </xsl:for-each>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="obfl:sequence/@css:page|
                         obfl:toc-sequence/@css:page|
                         obfl:dynamic-sequence/@css:page">
        <xsl:attribute name="master" select="pxi:layout-master-name(.)"/>
    </xsl:template>
    
    <xsl:template match="/*/css:rule[@selector='@page']"/>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <!-- ==================================== -->
    <!-- CSS page style to OBFL layout-master -->
    <!-- ==================================== -->
    
    <xsl:variable name="empty-string" as="element()">
        <string value=""/>
    </xsl:variable>
    
    <xsl:variable name="empty-field" as="element()">
        <field>
            <xsl:sequence select="$empty-string"/>
        </field>
    </xsl:variable>
    
    <xsl:variable name="text-flow-area" as="element()">
        <field allow-text-flow="true"/>
    </xsl:variable>
    
    <xsl:template mode="obfl:generate-layout-master" match="css:rule[@selector='@page']" priority="1">
        <xsl:call-template name="pf:next-match-with-generated-ids">
            <xsl:with-param name="prefix" select="'tmp_'"/>
            <xsl:with-param name="for-elements" select="descendant::css:string[@name][not(@target)]"/>
            <xsl:with-param name="in-use" select="()"/>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template mode="obfl:generate-layout-master" match="css:rule[@selector='@page']">
        <xsl:param name="name" tunnel="yes" as="xs:string"/>
        <xsl:param name="default-page-counter-name" tunnel="yes" as="xs:string"/> <!-- "page"|"pre-page"|"post-page" -->
        <xsl:variable name="page-stylesheet" as="element()*" select="*"/>
        <xsl:variable name="duplex" as="xs:boolean" select="$duplex='true'"/>
        <xsl:variable name="right-page-stylesheet" as="element()*" select="$page-stylesheet[@selector='&amp;:right']/*"/>
        <xsl:variable name="left-page-stylesheet" as="element()*" select="$page-stylesheet[@selector='&amp;:left']/*"/>
        <xsl:variable name="default-page-stylesheet" as="element()*" select="$page-stylesheet"/>
        <xsl:variable name="default-page-properties" as="element()*"
                      select="if ($default-page-stylesheet/self::css:property)
                              then $default-page-stylesheet/self::css:property
                              else $default-page-stylesheet[not(@selector)]/css:property"/>
        <xsl:variable name="size" as="xs:string"
                      select="($default-page-properties[@name='size'][css:is-valid(.)]/@value, css:initial-value('size'))[1]"/>
        <xsl:variable name="page-width" as="xs:integer" select="xs:integer(number(tokenize($size, '\s+')[1]))"/>
        <xsl:variable name="page-height" as="xs:integer" select="xs:integer(number(tokenize($size, '\s+')[2]))"/>
        <xsl:if test="$default-page-properties[@name='counter-set']">
            <xsl:message>
                <xsl:apply-templates mode="css:serialize" select="$default-page-properties[@name='counter-set'][1]"/>
                <xsl:text> not supported inside @page</xsl:text>
            </xsl:message>
        </xsl:if>
        <xsl:variable name="counter-increment" as="element()*"
                      select="css:parse-counter-set(
                                ($default-page-properties[@name='counter-increment']/@value,$default-page-counter-name)[1],
                                1)"/>
        <xsl:if test="count($counter-increment)&gt;1">
            <xsl:message terminate="yes">
                <xsl:value-of select="$default-page-properties[@name='counter-increment'][1]"/>
                <xsl:text>: a page can only have one page counter</xsl:text>
            </xsl:message>
        </xsl:if>
        <xsl:variable name="counter-increment" as="element()" select="$counter-increment[last()]"/>
        <xsl:if test="not($counter-increment/@value='1')">
            <xsl:message terminate="yes">
                <xsl:value-of select="$default-page-properties[@name='counter-increment'][1]"/>
                <xsl:text>: a page counter can not be incremented by </xsl:text>
                <xsl:value-of select="$counter-increment/@value"/>
            </xsl:message>
        </xsl:if>
        <xsl:variable name="page-counter-name" as="xs:string" select="$counter-increment/@name"/>
        <xsl:variable name="footnotes-properties" as="element()*"
                      select="$default-page-stylesheet[@selector='@footnotes'][1]/css:property"/>
        <xsl:variable name="footnotes-content" as="element()*" select="$footnotes-properties[@name='content'][1]/*"/>
        <layout-master name="{$name}" duplex="{$duplex}"
                       page-width="{$page-width}" page-height="{$page-height}">
            <xsl:if test="$right-page-stylesheet">
                <template use-when="(= (% $page 2) 1)">
                    <xsl:call-template name="template">
                        <xsl:with-param name="stylesheet" select="$right-page-stylesheet"/>
                        <xsl:with-param name="page-side" tunnel="yes" select="'right'"/>
                        <xsl:with-param name="page-counter-name" tunnel="yes" select="$page-counter-name"/>
                    </xsl:call-template>
                </template>
            </xsl:if>
            <xsl:if test="$left-page-stylesheet">
                <template use-when="(= (% $page 2) 0)">
                    <xsl:call-template name="template">
                        <xsl:with-param name="stylesheet" select="$left-page-stylesheet"/>
                        <xsl:with-param name="page-side" tunnel="yes" select="'left'"/>
                        <xsl:with-param name="page-counter-name" tunnel="yes" select="$page-counter-name"/>
                    </xsl:call-template>
                </template>
            </xsl:if>
            <default-template>
                <xsl:call-template name="template">
                    <xsl:with-param name="stylesheet" select="$default-page-stylesheet"/>
                    <xsl:with-param name="page-counter-name" tunnel="yes" select="$page-counter-name"/>
                </xsl:call-template>
            </default-template>
            <xsl:if test="$footnotes-content[not(self::css:flow[@from])]">
                <xsl:call-template name="pf:warn">
                    <xsl:with-param name="msg">only flow() function supported in footnotes area</xsl:with-param>
                </xsl:call-template>
            </xsl:if>
            <xsl:if test="count($footnotes-content[self::css:flow[@from]]) > 1">
                <xsl:call-template name="pf:warn">
                    <xsl:with-param name="msg">not more than one flow() function supported in footnotes area</xsl:with-param>
                </xsl:call-template>
            </xsl:if>
             <!--
                 default scope within footnotes area is 'page'
             -->
            <xsl:if test="$footnotes-content[self::css:flow[@from]][1]/@scope[not(.='page')]">
                <xsl:call-template name="pf:warn">
                    <xsl:with-param name="msg">{} argument of flow() function not allowed within footnotes area</xsl:with-param>
                    <xsl:with-param name="args" select="$footnotes-content[self::css:flow[@from]][1]/@scope"/>
                </xsl:call-template>
            </xsl:if>
            <xsl:for-each select="$footnotes-content[self::css:flow[@from]][1]">
                <xsl:variable name="footnotes-border-top" as="xs:string"
                              select="($footnotes-properties[@name='border-top-pattern'][1]/@value,css:initial-value('border-top-pattern'))[1]"/>
                <xsl:variable name="footnotes-max-height" as="xs:string"
                              select="($footnotes-properties[@name='max-height'][1]/@value,css:initial-value('max-height'))[1]"/>
                <xsl:variable name="footnotes-max-height" as="xs:integer"
                              select="if ($footnotes-max-height='none')
                                      then $page-height idiv 2
                                      else xs:integer(number($footnotes-max-height))"/>
                <xsl:variable name="footnotes-fallback-collection" as="xs:string?"
                              select="$footnotes-properties[@name=('-obfl-fallback-collection','-obfl-fallback-flow')][1]/@value"/>
                <xsl:if test="$footnotes-properties[@name='-obfl-fallback-flow']">
                    <xsl:call-template name="pf:warn">
                        <xsl:with-param name="msg">Correct spelling of '-obfl-fallback-flow' is '-obfl-fallback-collection'</xsl:with-param>
                    </xsl:call-template>
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
    </xsl:template>
    
    <xsl:template name="template">
        <xsl:param name="stylesheet" as="element()*" required="yes"/> <!-- css:rule*|css:property* -->
        <xsl:variable name="top-left" as="element()*">
            <xsl:call-template name="fields">
                <xsl:with-param name="properties" select="$stylesheet[@selector='@top-left'][1]/css:property"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="top-center" as="element()*">
            <xsl:call-template name="fields">
                <xsl:with-param name="properties" select="$stylesheet[@selector='@top-center'][1]/css:property"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="top-right" as="element()*">
            <xsl:call-template name="fields">
                <xsl:with-param name="properties" select="$stylesheet[@selector='@top-right'][1]/css:property"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="bottom-left" as="element()*">
            <xsl:call-template name="fields">
                <xsl:with-param name="properties" select="$stylesheet[@selector='@bottom-left'][1]/css:property"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="bottom-center" as="element()*">
            <xsl:call-template name="fields">
                <xsl:with-param name="properties" select="$stylesheet[@selector='@bottom-center'][1]/css:property"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="bottom-right" as="element()*">
            <xsl:call-template name="fields">
                <xsl:with-param name="properties" select="$stylesheet[@selector='@bottom-right'][1]/css:property"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="properties" as="element()*"
                      select="if ($stylesheet/self::css:property)
                              then $stylesheet/self::css:property
                              else $stylesheet[not(@selector)]/css:property"/>
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
            <xsl:with-param name="properties" select="$stylesheet[@selector='@left'][1]/css:property"/>
            <xsl:with-param name="side" select="'left'"/>
            <xsl:with-param name="min-width" select="$margin-left"/>
        </xsl:call-template>
        <xsl:call-template name="margin-region">
            <xsl:with-param name="properties" select="$stylesheet[@selector='@right'][1]/css:property"/>
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
                <xsl:sequence select="$center[1]"/>
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
                <xsl:sequence select="if ($times &lt;= 0 and not(exists($left)) and not(exists($center)) and exists($right))
                                      then $text-flow-area
                                      else ($empty-field,$left)[last()]"/>
                <xsl:sequence select="$center[last()]"/>
                <xsl:sequence select="($empty-field,$right)[last()]"/>
            </footer>
        </xsl:if>
    </xsl:template>
    
    <xsl:template name="fields" as="element()*"> <!-- obfl:field* -->
        <xsl:param name="properties" as="element()*"/> <!-- css:property* -->
        <xsl:variable name="white-space" as="xs:string" select="($properties[@name='white-space']/@value,'normal')[1]"/>
        <xsl:variable name="text-transform" as="xs:string" select="($properties[@name='text-transform']/@value,'auto')[1]"/>
        <xsl:variable name="content" as="element()*">
            <xsl:apply-templates select="$properties[@name='content'][1]/*" mode="eval-content-list-top-bottom">
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
        <xsl:param name="properties" as="element()*"/> <!-- css:property* -->
        <xsl:param name="side" as="xs:string" required="yes"/>
        <xsl:param name="min-width" as="xs:integer" required="yes"/>
        <xsl:variable name="indicators" as="element()*">
            <xsl:apply-templates select="$properties[@name='content'][1]/*" mode="eval-content-list-left-right"/>
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
    
    <xsl:template match="css:counter[not(@target)]" priority="1" mode="eval-content-list-top-bottom">
        <xsl:param name="white-space" as="xs:string" select="'normal'"/>
        <xsl:param name="text-transform" as="xs:string" select="'auto'"/>
        <xsl:param name="page-counter-name" as="xs:string" tunnel="yes"/>
        <xsl:if test="not(@name=$page-counter-name)">
            <xsl:choose>
                <xsl:when test="@name='page'">
                    <xsl:message>
                        <xsl:text>Should not use counter(page) in a page margin when the active page counter is </xsl:text>
                        <xsl:value-of select="$page-counter-name"/>
                        <xsl:text>. Assuming counter(</xsl:text>
                        <xsl:value-of select="$page-counter-name"/>
                        <xsl:text>) was meant.</xsl:text>
                    </xsl:message>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:message terminate="yes">
                        <xsl:text>Can not use counter(</xsl:text>
                        <xsl:value-of select="@name"/>
                        <xsl:text>) in a page margin when the active page counter is </xsl:text>
                        <xsl:value-of select="$page-counter-name"/>
                        <xsl:text>.</xsl:text>
                    </xsl:message>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
        <xsl:variable name="text-transform" as="xs:string*">
            <xsl:if test="matches(@style,re:exact($css:SYMBOLS_FN_RE))">
                <xsl:sequence select="'-dotify-counter'"/>
            </xsl:if>
            <xsl:sequence select="$text-transform[not(.=('none','auto'))]"/>
        </xsl:variable>
        <xsl:variable name="text-style" as="xs:string*">
            <xsl:if test="exists($text-transform)">
                <xsl:sequence select="concat('text-transform: ',string-join($text-transform,' '))"/>
            </xsl:if>
            <xsl:if test="not($white-space='normal')">
                <xsl:sequence select="concat('white-space: ',$white-space)"/>
            </xsl:if>
            <xsl:if test="matches(@style,re:exact($css:SYMBOLS_FN_RE))">
                <xsl:sequence select="concat('-dotify-counter-style: ',@style)"/>
            </xsl:if>
        </xsl:variable>
        <current-page number-format="{if (@style=('roman', 'upper-roman', 'lower-roman', 'upper-alpha', 'lower-alpha'))
                                      then @style else 'default'}">
            <xsl:if test="exists($text-style)">
                <xsl:attribute name="text-style" select="string-join($text-style,'; ')"/>
            </xsl:if>
        </current-page>
    </xsl:template>
    
    <xsl:template match="css:string[@name][not(@target)]" mode="eval-content-list-top-bottom">
        <xsl:param name="white-space" as="xs:string" select="'normal'"/>
        <xsl:param name="text-transform" as="xs:string" select="'auto'"/>
        <xsl:param name="page-side" as="xs:string" tunnel="yes" select="'both'"/> <!-- right|left|both -->
        <xsl:if test="$page-side='both' and @scope=('spread-first','spread-start','spread-last','spread-last-except-start')">
            <!--
                FIXME: force creation of templates for left and right pages when margin
                content contains "string(foo, spread-last)"
            -->
            <xsl:call-template name="pf:error">
                <xsl:with-param name="msg">string({}, {}) on both left and right side currently not supported</xsl:with-param>
                <xsl:with-param name="args" select="(@name,
                                                     @scope)"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:apply-templates mode="marker-reference" select=".">
            <xsl:with-param name="white-space" tunnel="yes" select="$white-space"/>
            <xsl:with-param name="text-transform" tunnel="yes" select="$text-transform"/>
        </xsl:apply-templates>
    </xsl:template>
    
    <xsl:template match="css:custom-func[@name='-obfl-marker-indicator'][matches(@arg2,$css:STRING_RE) and not(@arg3)]"
                  mode="eval-content-list-left-right" priority="1">
        <marker-indicator markers="indicator/{@arg1}" indicator="{substring(@arg2,2,string-length(@arg2)-2)}"/>
    </xsl:template>
    
    <xsl:template match="css:custom-func[@name='-obfl-marker-indicator']" mode="eval-content-list-left-right">
        <xsl:call-template name="pf:warn">
            <xsl:with-param name="msg">-obfl-marker-indicator() function requires exactly two arguments</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template match="css:attr" mode="eval-content-list-top-bottom eval-content-list-left-right">
        <xsl:call-template name="pf:warn">
            <xsl:with-param name="msg">attr() function not supported in page margin</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template match="css:content[not(@target)]" mode="eval-content-list-top-bottom eval-content-list-left-right">
        <xsl:call-template name="pf:warn">
            <xsl:with-param name="msg">content() function not supported in page margin</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template match="css:content[@target]" mode="eval-content-list-top-bottom eval-content-list-left-right">
        <xsl:call-template name="pf:warn">
            <xsl:with-param name="msg">target-content() function not supported in page margin</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template match="css:text[@target]" mode="eval-content-list-top-bottom eval-content-list-left-right">
        <xsl:call-template name="pf:warn">
            <xsl:with-param name="msg">target-text() function not supported in page margin</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template match="css:string[@value]" mode="eval-content-list-left-right">
        <xsl:call-template name="pf:warn">
            <xsl:with-param name="msg">strings not supported in left and right page margin</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template match="css:string[@name][not(@target)]" mode="eval-content-list-left-right">
        <xsl:call-template name="pf:warn">
            <xsl:with-param name="msg">string() function not supported in left and right page margin</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template match="css:string[@name][@target]" mode="eval-content-list-top-bottom eval-content-list-left-right">
        <xsl:call-template name="pf:warn">
            <xsl:with-param name="msg">target-string() function not supported in page margin</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template match="css:counter[@target]" mode="eval-content-list-top-bottom eval-content-list-left-right">
        <xsl:call-template name="pf:warn">
            <xsl:with-param name="msg">target-counter() function not supported in page margin</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template match="css:counter[not(@target)]" mode="eval-content-list-left-right">
        <xsl:call-template name="pf:warn">
            <xsl:with-param name="msg">counter() function not supported in left and right page margin</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template match="css:leader" mode="eval-content-list-top-bottom eval-content-list-left-right">
        <xsl:call-template name="pf:warn">
            <xsl:with-param name="msg">leader() function not supported in page margin</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template match="css:flow[@from]" mode="eval-content-list-top-bottom eval-content-list-left-right">
        <xsl:call-template name="pf:warn">
            <xsl:with-param name="msg">flow() function not supported in page margin</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template match="css:custom-func[@name='-obfl-marker-indicator']" mode="eval-content-list-top-bottom">
        <xsl:call-template name="pf:warn">
            <xsl:with-param name="msg">-obfl-marker-indicator() function not supported in top and bottom page margin</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template match="*" mode="eval-content-list-top-bottom eval-content-list-left-right">
        <xsl:call-template name="pf:error">
            <xsl:with-param name="msg">Coding error</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    
    <!-- for XSpec -->
    <xsl:template name="obfl:generate-layout-master">
        <xsl:param name="page-stylesheet" as="element(css:rule)"/>
        <xsl:param name="name" as="xs:string"/>
        <xsl:param name="default-page-counter-name" as="xs:string"/>
        <xsl:apply-templates mode="obfl:generate-layout-master" select="$page-stylesheet">
            <xsl:with-param name="name" tunnel="yes" select="$name"/>
            <xsl:with-param name="default-page-counter-name" tunnel="yes" select="$default-page-counter-name"/>
        </xsl:apply-templates>
    </xsl:template>
    
</xsl:stylesheet>
