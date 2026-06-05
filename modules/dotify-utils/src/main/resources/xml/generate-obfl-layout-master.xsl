<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.daisy.org/ns/2011/obfl"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:s="org.daisy.pipeline.braille.css.xpath.Style"
                xmlns:obfl="http://www.daisy.org/ns/2011/obfl"
                xmlns:medium="org.daisy.pipeline.braille.dotify.saxon.impl.EmbossedMediumFunctions"
                exclude-result-prefixes="#all"
                version="3.0">
    
    <xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/library.xsl"/>
    <xsl:include href="http://www.daisy.org/pipeline/modules/css-utils/library.xsl"/>
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/pef-utils/library.xsl"/>
    <xsl:include href="marker-reference.xsl"/>
    
    <xsl:param name="medium" as="item()?" required="yes"/>
    <xsl:param name="braille-charset-table" as="xs:string" required="yes"/>
    <xsl:param name="page-and-volume-styles" as="item()*" required="no"/>
    <xsl:param name="counter-styles" as="item()?" required="no"/>
    
    <xsl:variable name="empty-page-style" as="item()" select="s:get(css:parse-stylesheet(()),'@page')"/>
    
    <xsl:variable name="page-styles" as="map(xs:string,item())">
        <xsl:iterate select="let $page-styles := for $s in $page-and-volume-styles return s:get($s,'@page'),
                                 $volume-styles := for $s in $page-and-volume-styles return s:get($s,'@volume'),
                                 $volume-styles := for $s in $volume-styles return ($s,for $k in s:keys($s)[matches(.,'^&amp;:')] return s:get($s,$k)),
                                 $volume-begin-styles := for $s in $volume-styles return s:get($s,'@begin'),
                                 $volume-end-styles := for $s in $volume-styles return s:get($s,'@end'),
                                 $volume-page-styles := for $s in ($volume-begin-styles,$volume-end-styles) return s:get($s,'@page')
                               return ($page-styles,$volume-page-styles,$empty-page-style)">
            <xsl:param name="map" as="map(xs:string,item())" select="map{}"/>
            <xsl:on-completion>
                <xsl:sequence select="$map"/>
            </xsl:on-completion>
            <xsl:variable name="serialized" as="xs:string" select="string(.)"/>
            <xsl:choose>
                <xsl:when test="map:contains($map,$serialized)">
                    <xsl:next-iteration/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:next-iteration>
                        <xsl:with-param name="map" select="map:put($map,$serialized,.)"/>
                    </xsl:next-iteration>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:iterate>
    </xsl:variable>
    <xsl:variable name="custom-counter-style-names" as="xs:string*"
                  select="map:keys(css:parse-counter-styles(s:get($counter-styles,'@counter-style')))"/>
    <xsl:variable name="obfl-variables" as="xs:string*" select="('-obfl-page',
                                                                 '-obfl-volume',
                                                                 '-obfl-volumes',
                                                                 '-obfl-sheets-in-document',
                                                                 '-obfl-sheets-in-volume',
                                                                 '-obfl-started-volume-number',
                                                                 '-obfl-started-page-number',
                                                                 '-obfl-started-volume-first-content-page'
                                                                 )"/>
    
    <xsl:function name="pxi:layout-master-name" as="xs:string">
        <xsl:param name="page-style" as="xs:string"/>
        <xsl:sequence select="concat('master_',
                                     index-of(map:keys($page-styles), $page-style)[1])"/>
    </xsl:function>
    
    <xsl:template match="/*">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates select="obfl:meta"/>
            <xsl:variable name="sequences" as="element()*" select="//obfl:sequence|//obfl:toc-sequence|//obfl:dynamic-sequence"/>
            <xsl:for-each select="distinct-values($sequences/@css:page)">
                <xsl:variable name="layout-master-name" select="pxi:layout-master-name(.)"/>
                <xsl:variable name="page-style" as="item()">
                    <xsl:if test="not(map:contains($page-styles,current()))">
                        <xsl:call-template name="pf:error">
                            <xsl:with-param name="msg">coding error</xsl:with-param>
                        </xsl:call-template>
                    </xsl:if>
                    <xsl:sequence select="map:get($page-styles,current())"/>
                </xsl:variable>
                <xsl:variable name="default-page-counter-names"
                              select="distinct-values(
                                        for $s in $sequences[@css:page=current()] return
                                          if ($s/parent::obfl:pre-content) then 'pre-page'
                                          else if ($s/parent::obfl:post-content) then 'post-page'
                                          else 'page')"/>
                <xsl:call-template name="obfl:generate-layout-master">
                    <xsl:with-param name="page-style" select="$page-style"/>
                    <xsl:with-param name="name" select="$layout-master-name"/>
                    <xsl:with-param name="default-page-counter-name" select="$default-page-counter-names[1]"/>
                </xsl:call-template>
                <!--
                    The result of calling obfl:generate-layout-master is the same regardless of the
                    default-page-counter-name passed. Therefore only the first result is
                    relevant. However, all calls need to be made anyway because the function checks
                    that there are no mismatches between the active page counter and counter() calls
                    in page margins.
                -->
                <xsl:for-each select="$default-page-counter-names[position()&gt;1]">
                    <xsl:variable name="_">
                        <xsl:call-template name="obfl:generate-layout-master">
                            <xsl:with-param name="page-style" select="$page-style"/>
                            <xsl:with-param name="name" select="$layout-master-name"/>
                            <xsl:with-param name="default-page-counter-name" select="."/>
                        </xsl:call-template>
                    </xsl:variable>
                </xsl:for-each>
            </xsl:for-each>
            <xsl:apply-templates select="* except obfl:meta"/>
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
    
    <xsl:template name="obfl:generate-layout-master">
        <xsl:param name="page-style" as="item()?" required="yes"/>
        <xsl:param name="name" as="xs:string"/>
        <xsl:param name="default-page-counter-name" as="xs:string"/> <!-- "page"|"pre-page"|"post-page" -->
        <xsl:variable name="page-style" as="item()" select="($page-style,$empty-page-style)[1]"/>
        <xsl:variable name="page-style" as="item()" select="medium:getPrintablePageStyle($medium,$page-style)"/>
        <xsl:variable name="right-page-style" as="item()?" select="s:get($page-style,'&amp;:right')"/>
        <xsl:variable name="left-page-style" as="item()?" select="s:get($page-style,'&amp;:left')"/>
        <xsl:variable name="size" as="xs:string"
                      select="string(s:get($page-style,'size')[not(string(.)='auto')])"/>
        <xsl:variable name="page-width" as="xs:integer" select="xs:integer(number(tokenize($size,'\s+')[1]))"/>
        <xsl:variable name="page-height" as="xs:integer" select="xs:integer(number(tokenize($size,'\s+')[2]))"/>
        <xsl:variable name="duplex" as="xs:boolean" select="if (exists($medium))
                                                            then pf:media-query-matches('(-daisy-duplex)',$medium)
                                                            else true()"/>
        <xsl:if test="exists(s:get($page-style,'counter-set')[not(string(.)='none')])">
            <xsl:message>
                <xsl:value-of select="string(s:get($page-style,'counter-set'))"/>
                <xsl:text> not supported inside @page</xsl:text>
            </xsl:message>
        </xsl:if>
        <xsl:variable name="page-counter-name" as="xs:string">
            <xsl:variable name="counter-increment" as="item()?" select="s:get($page-style,'counter-increment')"/>
            <xsl:variable name="counters" as="element(css:counter-set)*"
                          select="s:toXml($counter-increment)/self::css:counter-set"/>
            <xsl:choose>
                <xsl:when test="exists($counters)">
                    <xsl:if test="count($counters)&gt;1">
                        <xsl:message terminate="yes">
                            <xsl:value-of select="string($counter-increment)"/>
                            <xsl:text>: a page can only have one page counter</xsl:text>
                        </xsl:message>
                    </xsl:if>
                    <xsl:if test="not($counters/@value='1')">
                        <xsl:message terminate="yes">
                            <xsl:value-of select="string($counter-increment)"/>
                            <xsl:text>: a page counter can not be incremented by </xsl:text>
                            <xsl:value-of select="$counters/@value"/>
                        </xsl:message>
                    </xsl:if>
                    <xsl:sequence select="$counters/@name"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="$default-page-counter-name"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="footnotes-style" as="item()?" select="s:get($page-style,'@footnotes')"/>
        <xsl:variable name="footnotes-content" as="element()*" select="s:toXml(s:get($footnotes-style,'content'))"/>
        <layout-master name="{$name}" duplex="{$duplex}" page-width="{$page-width}" page-height="{$page-height}">
            <xsl:variable name="has-right-page-style" as="xs:boolean" select="exists(s:iterate($right-page-style))"/>
            <xsl:variable name="has-left-page-style" as="xs:boolean" select="exists(s:iterate($left-page-style))"/>
            <xsl:variable name="template" as="item()?"
                          select="if ($has-left-page-style) then $left-page-style
                                  else if ($has-right-page-style) then $right-page-style
                                  else ()"/>
            <xsl:variable name="template-side" as="xs:string?"
                          select="if ($has-left-page-style) then 'left'
                                  else if ($has-right-page-style) then 'right'
                                  else ()"/>
            <xsl:variable name="default-template" as="item()"
                          select="if ($has-right-page-style and $has-left-page-style)
                                  then $right-page-style
                                  else $page-style"/>
            <xsl:variable name="default-template-side" as="xs:string"
				          select="if ($has-right-page-style and $has-left-page-style)
                                  then 'right'
                                  else 'both'"/>
            <xsl:if test="$template[not(string(.)=string(s:remove($default-template,
                                                                  ('&amp;:left','&amp;:right'))))]">
                <!--
                    FIXME: need better way to determine whether we are on right or left page: https://github.com/mtmse/obfl/issues/22
                -->
                <template use-when="(= (% $page 2) {if ($template-side='left') then '0' else '1'})">
                    <xsl:call-template name="template">
                        <xsl:with-param name="style" select="$template"/>
                        <xsl:with-param name="page-side" tunnel="yes" select="$template-side"/>
                        <xsl:with-param name="page-counter-name" tunnel="yes" select="$page-counter-name"/>
                    </xsl:call-template>
                </template>
            </xsl:if>
            <default-template>
                <xsl:call-template name="template">
                    <xsl:with-param name="style" select="$default-template"/>
                    <xsl:with-param name="page-side" tunnel="yes" select="$default-template-side"/>
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
            <xsl:if test="$footnotes-content[self::css:flow[@from]][1][not(@scope)]">
                <xsl:call-template name="pf:warn">
                    <xsl:with-param name="msg">flow() function without scope argument not allowed within footnotes area</xsl:with-param>
                </xsl:call-template>
            </xsl:if>
            <xsl:if test="$footnotes-content[self::css:flow[@from]][1]/@scope[not(.='page')]">
                <xsl:call-template name="pf:warn">
                    <xsl:with-param name="msg">{} argument of flow() function not allowed within footnotes area</xsl:with-param>
                    <xsl:with-param name="args" select="$footnotes-content[self::css:flow[@from]][1]/@scope"/>
                </xsl:call-template>
            </xsl:if>
            <xsl:for-each select="$footnotes-content[self::css:flow[@from]][1]">
                <xsl:variable name="footnotes-border-top" as="xs:string"
                              select="string(s:getOrDefault($footnotes-style,'border-top-pattern'))"/>
                <xsl:variable name="footnotes-max-height" as="xs:string"
                              select="string(s:getOrDefault($footnotes-style,'max-height'))"/>
                <xsl:variable name="footnotes-max-height" as="xs:integer"
                              select="if ($footnotes-max-height='none')
                                      then $page-height idiv 2
                                      else xs:integer(number($footnotes-max-height))"/>
                <xsl:variable name="footnotes-fallback-collection" as="xs:string?"
                              select="s:getOrDefault($footnotes-style,'-obfl-fallback-collection')[not(.='normal')]"/>
                <page-area align="bottom" max-height="{$footnotes-max-height}" collection="{@from}">
                    <xsl:if test="exists($footnotes-fallback-collection)
                                  and not($footnotes-fallback-collection='normal')">
                        <fallback>
                            <rename collection="{@from}" to="{$footnotes-fallback-collection}"/>
                            <rename collection="meta/{@from}" to="meta/{$footnotes-fallback-collection}"/>
                        </fallback>
                    </xsl:if>
                    <xsl:if test="$footnotes-border-top!='none'">
                        <before>
                            <block translate="pre-translated-text-css">
                                <xsl:variable name="pattern"
                                              select="if ($braille-charset-table='')
                                                      then $footnotes-border-top
                                                      else pf:pef-encode(concat('(id:&quot;',$braille-charset-table,'&quot;)'),
                                                                      $footnotes-border-top)"/>
                                <leader pattern="{$pattern}" position="100%" align="right"/>
                                <!-- We add a single instance of the pattern in order to have some
                                     text after the leader and to make sure that the
                                     translate="pre-translated-text-css" has an effect on the leader
                                     (Dotify bug). -->
                                <xsl:value-of select="$pattern"/>
                            </block>
                        </before>
                    </xsl:if>
                </page-area>
            </xsl:for-each>
        </layout-master>
    </xsl:template>
    
    <xsl:template name="template">
        <xsl:param name="style" as="item()" required="yes"/>
        <xsl:variable name="top-left" as="element()*">
            <xsl:call-template name="fields">
                <xsl:with-param name="properties" select="s:get($style,'@top-left')"/>
                <xsl:with-param name="side" select="'top'"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="top-center" as="element()*">
            <xsl:call-template name="fields">
                <xsl:with-param name="properties" select="s:get($style,'@top-center')"/>
                <xsl:with-param name="side" select="'top'"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="top-right" as="element()*">
            <xsl:call-template name="fields">
                <xsl:with-param name="properties" select="s:get($style,'@top-right')"/>
                <xsl:with-param name="side" select="'top'"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="bottom-left" as="element()*">
            <xsl:call-template name="fields">
                <xsl:with-param name="properties" select="s:get($style,'@bottom-left')"/>
                <xsl:with-param name="side" select="'bottom'"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="bottom-center" as="element()*">
            <xsl:call-template name="fields">
                <xsl:with-param name="properties" select="s:get($style,'@bottom-center')"/>
                <xsl:with-param name="side" select="'bottom'"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="bottom-right" as="element()*">
            <xsl:call-template name="fields">
                <xsl:with-param name="properties" select="s:get($style,'@bottom-right')"/>
                <xsl:with-param name="side" select="'bottom'"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="margin-top" as="xs:integer"
                      select="max((xs:integer(string(s:getOrDefault($style,'margin-top'))),0))"/>
        <xsl:variable name="margin-bottom" as="xs:integer"
                      select="max((xs:integer(string(s:getOrDefault($style,'margin-bottom'))),0))"/>
        <xsl:variable name="margin-left" as="xs:integer"
                      select="max((xs:integer(string(s:getOrDefault($style,'margin-left'))),0))"/>
        <xsl:variable name="margin-right" as="xs:integer"
                      select="max((xs:integer(string(s:getOrDefault($style,'margin-right'))),0))"/>
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
            <xsl:with-param name="properties" select="s:get($style,'@left')"/>
            <xsl:with-param name="side" select="'left'"/>
            <xsl:with-param name="min-width" select="$margin-left"/>
        </xsl:call-template>
        <xsl:call-template name="margin-region">
            <xsl:with-param name="properties" select="s:get($style,'@right')"/>
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
        <xsl:param name="properties" as="item()?"/>
        <xsl:param name="side" as="xs:string" required="yes"/>
        <xsl:variable name="white-space" as="item()?" select="s:getOrDefault($properties,'white-space')"/>
        <xsl:variable name="text-transform" as="item()?" select="s:getOrDefault($properties,'text-transform')"/>
        <!-- assume that no other padding is specified (ensured by EmbossedMediumFunctions.java) -->
        <xsl:variable name="padding" as="item()?" select="s:getOrDefault($properties,concat('padding-',$side))"/>
        <xsl:variable name="content" as="element()*">
            <xsl:if test="exists($padding) and $side='top'">
                <xsl:for-each select="1 to xs:integer(number(string($padding)))">
                    <br/>
                </xsl:for-each>
            </xsl:if>
            <xsl:apply-templates select="s:toXml(s:get($properties,'content'))" mode="eval-content-list-top-bottom">
                <xsl:with-param name="white-space" select="$white-space"/>
                <xsl:with-param name="text-transform" select="$text-transform"/>
            </xsl:apply-templates>
            <xsl:if test="exists($padding) and $side='bottom'">
                <xsl:for-each select="1 to xs:integer(number(string($padding)))">
                    <br/>
                </xsl:for-each>
            </xsl:if>
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
        <xsl:param name="properties" as="item()?"/>
        <xsl:param name="side" as="xs:string" required="yes"/>
        <xsl:param name="min-width" as="xs:integer" required="yes"/>
        <xsl:variable name="white-space" as="item()?" select="s:getOrDefault($properties,'white-space')"/>
        <xsl:variable name="text-transform" as="item()?" select="s:getOrDefault($properties,'text-transform')"/>
        <!-- assume that no other padding is specified (ensured by EmbossedMediumFunctions.java) -->
        <xsl:variable name="padding" as="item()?" select="s:getOrDefault($properties,concat('padding-',$side))"/>
        <xsl:variable name="indicators" as="element()*">
            <xsl:apply-templates select="s:toXml(s:get($properties,'content'))" mode="eval-content-list-left-right">
                <xsl:with-param name="side" select="$side"/>
                <xsl:with-param name="white-space" select="$white-space"/>
                <xsl:with-param name="text-transform" select="$text-transform"/>
                <xsl:with-param name="padding" select="$padding"/>
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:if test="exists($indicators) or $min-width &gt; 0">
            <!--
                Note that left and right margin content, unlike top and bottom, will not extend into
                the page area if it doesn't fit in the margin, but will be truncated or result in an
                error (depending on the "allow-text-overflow-trimming" setting). We do a best effort
                here to set the minimum required width of the margin based on the indicator value,
                but it could be inaccurate (and even on overestimation). Ultimately we should
                require that the user sets the margin to a high enough value, and not not attempt to
                do an estimation of the required width.
            -->
            <margin-region align="{$side}" width="{max(($indicators/@indicator/string-length(.),$min-width))}">
                <indicators>
                    <xsl:sequence select="$indicators"/>
                </indicators>
            </margin-region>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="css:string[@value]" mode="eval-content-list-top-bottom">
        <xsl:param name="white-space" as="item()?"/>
        <xsl:param name="text-transform" as="item()?"/>
        <xsl:choose>
            <xsl:when test="$white-space[string(.)=('pre-wrap','pre-line')]">
                <!--
                    TODO: wrapping is not allowed, warn if content is clipped
                -->
                <xsl:analyze-string select="string(@value)" regex="\n">
                    <xsl:matching-substring>
                        <br/>
                    </xsl:matching-substring>
                    <xsl:non-matching-substring>
                        <xsl:choose>
                            <xsl:when test="string($white-space)='pre-wrap'">
                                <string value="{replace(.,'\s','&#x00A0;')}">
                                    <xsl:if test="$text-transform[not(string(.)='auto')]">
                                        <xsl:attribute name="text-style" select="concat('text-transform:',string($text-transform))"/>
                                    </xsl:if>
                                </string>
                            </xsl:when>
                            <xsl:otherwise>
                                <string value="{.}">
                                    <xsl:if test="$text-transform[not(string(.)='auto')]">
                                        <xsl:attribute name="text-style" select="concat('text-transform:',string($text-transform))"/>
                                    </xsl:if>
                                </string>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:non-matching-substring>
                </xsl:analyze-string>
            </xsl:when>
            <xsl:otherwise>
                <string value="{string(@value)}">
                    <xsl:if test="$text-transform[not(string(.)='auto')]">
                        <xsl:attribute name="text-style" select="concat('text-transform:',string($text-transform))"/>
                    </xsl:if>
                </string>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="css:counter[not(@target)]" priority="1" mode="eval-content-list-top-bottom">
        <xsl:param name="white-space" as="item()?"/>
        <xsl:param name="text-transform" as="item()?"/>
        <xsl:param name="page-counter-name" as="xs:string" tunnel="yes"/>
        <xsl:if test="not(@name=($page-counter-name,'volume',$obfl-variables))">
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
            <xsl:if test="@style=$custom-counter-style-names
                          or starts-with(@style,'symbols(')">
                <xsl:sequence select="'-dotify-counter'"/>
            </xsl:if>
            <!-- Note that '-dotify-counter' does not replace 'none', as would be the case with a
                 real text-transform. The text-transform property is merely used as a hack here. -->
            <xsl:sequence select="$text-transform[not(string(.)='auto')]"/>
        </xsl:variable>
        <xsl:variable name="text-style" as="xs:string*">
            <xsl:if test="exists($text-transform)">
                <xsl:sequence select="concat('text-transform: ',string-join($text-transform,' '))"/>
            </xsl:if>
            <xsl:if test="$white-space[not(string(.)='normal')]">
                <xsl:sequence select="concat('white-space: ',string($white-space))"/>
            </xsl:if>
            <xsl:if test="@style=$custom-counter-style-names
                          or starts-with(@style,'symbols(')">
                <xsl:sequence select="concat('-dotify-counter-style: ',@style)"/>
            </xsl:if>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="@name=($page-counter-name,'page')">
                <current-page number-format="{if (@style=('roman', 'upper-roman', 'lower-roman', 'upper-alpha', 'lower-alpha')
                                                  and not(@style=$custom-counter-style-names))
                                              then @style else 'default'}">
                    <xsl:if test="exists($text-style)">
                        <xsl:attribute name="text-style" select="string-join($text-style,'; ')"/>
                    </xsl:if>
                </current-page>
            </xsl:when>
            <xsl:otherwise>
                <evaluate expression="${replace(@name,'^-obfl-','')}">
                    <xsl:if test="exists($text-style)">
                        <xsl:attribute name="text-style" select="string-join($text-style,'; ')"/>
                    </xsl:if>
                </evaluate>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="css:string[@name][not(@target)]" mode="eval-content-list-top-bottom">
        <xsl:param name="white-space" as="item()?"/>
        <xsl:param name="text-transform" as="item()?"/>
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
    
    <xsl:template match="css:custom-func[@name='-obfl-marker-indicator']"
                  mode="eval-content-list-left-right" priority="1">
        <xsl:param name="side" as="xs:string"/>
        <xsl:param name="white-space" as="item()?"/>
        <xsl:param name="text-transform" as="item()?"/>
        <xsl:param name="padding" as="item()?"/>
        <xsl:variable name="text" as="xs:string" select="substring(@arg2,2,string-length(@arg2)-2)"/>
        <xsl:variable name="text" as="xs:string*">
            <xsl:if test="exists($padding) and $side='left'">
                <xsl:sequence select="string-join(for $x in 1 to xs:integer(number(string($padding))) return '&#x00A0;')"/>
            </xsl:if>
            <xsl:choose>
                <xsl:when test="$white-space[string(.)=('pre-wrap')]">
                    <xsl:sequence select="replace($text,'\s','&#x00A0;')"/>
                </xsl:when>
                <xsl:otherwise>
                    <!-- normalize white space here because Dotify does not do it -->
                    <xsl:sequence select="normalize-space($text)"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="exists($padding) and $side='left'">
                <xsl:sequence select="string-join(for $x in 1 to xs:integer(number(string($padding))) return '&#x00A0;')"/>
            </xsl:if>
        </xsl:variable>
        <xsl:variable name="text" as="xs:string" select="string-join($text)"/>
        <xsl:variable name="text-style" as="xs:string*">
            <xsl:if test="$text-transform[not(string(.)='auto')]">
                <xsl:sequence select="concat('text-transform: ',string($text-transform))"/>
            </xsl:if>
            <xsl:if test="$white-space[not(string(.)='normal')]">
                <xsl:sequence select="concat('white-space: ',string($white-space))"/>
            </xsl:if>
        </xsl:variable>
        <marker-indicator markers="indicator/{@arg1}" indicator="{$text}">
            <xsl:if test="exists($text-style)">
                <xsl:attribute name="text-style" select="string-join($text-style,'; ')"/>
            </xsl:if>
        </marker-indicator>
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
    
    <!-- we assume that white space only strings have been converted to padding in EmbossedMediumFunctions.java -->
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
    
</xsl:stylesheet>
