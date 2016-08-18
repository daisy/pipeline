<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.daisy.org/ns/2011/obfl"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:obfl="http://www.daisy.org/ns/2011/obfl"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:re="regex-utils"
                exclude-result-prefixes="#all"
                version="2.0" >
    
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl" />
    <xsl:include href="generate-obfl-layout-master.xsl"/>
    
    <!-- ========== -->
    <!-- Parameters -->
    <!-- ========== -->
    
    <xsl:param name="braille-translator-query" as="xs:string" required="yes"/> <!-- unused -->
    <xsl:param name="duplex" as="xs:string" required="yes"/>
    
    <!-- ====================== -->
    <!-- Page and volume styles -->
    <!-- ====================== -->
    
    <xsl:variable name="volume-stylesheets" as="xs:string*"
                  select="distinct-values(collection()/*[not(@css:flow)]/string(@css:volume))"/>
    
    <xsl:variable name="page-stylesheets-right-odd" as="xs:string*">
        <!--
            page style to use in @begin and @end areas when no page property specified
        -->
        <xsl:variable name="default-page-style" as="xs:string" select="(collection()/*[not(@css:flow)])[1]/string(@css:page)"/>
        <xsl:variable name="page-stylesheets" as="xs:string*">
            <xsl:for-each-group select="collection()/*[not(@css:flow)]" group-starting-with="*[@css:counter-set-page]">
                <!--
                    TODO: optimisation: also check whether :left or :right style present. if not, no
                    need to differentiate.
                -->
                <xsl:if test="not(current-group()[1]/@css:counter-set-page[(xs:integer(.) mod 2)=0])">
                    <xsl:sequence select="current-group()/string(@css:page)"/>
                </xsl:if>
            </xsl:for-each-group>
            <xsl:for-each select="$volume-stylesheets">
                <xsl:variable name="volume-style" as="element()*" select="css:parse-stylesheet(.)"/>
                <xsl:variable name="volume-area-styles" as="element()*"
                              select="(if ($volume-style[matches(@selector,'^:')])
                                       then $volume-style/@style/css:parse-stylesheet(.)
                                       else $volume-style)
                                       [@selector=('@begin','@end')]"/>
                <xsl:for-each select="distinct-values($volume-area-styles/@style)">
                    <xsl:variable name="volume-area-style" as="element()*" select="css:parse-stylesheet(.)"/>
                    <xsl:variable name="volume-area-page-style" as="xs:string?"
                                  select="$volume-area-style[@selector='@page']/@style"/>
                    <xsl:variable name="volume-area-style" as="element()*"
                                  select="css:parse-declaration-list($volume-area-style[not(@selector)]/@style)"/>
                    <xsl:for-each select="distinct-values(
                                          css:parse-content-list($volume-area-style[@name='content'][1]/@value,())
                                          /self::css:flow[@from][(@scope,'document')[1]='document']/@from)">
                        <xsl:variable name="flow" as="xs:string" select="."/>
                        <xsl:for-each-group select="collection()/*[@css:flow=$flow]" group-starting-with="*[@css:counter-set-page]">
                            <xsl:if test="not(current-group()[1]/@css:counter-set-page[(xs:integer(.) mod 2)=0])">
                                <xsl:for-each select="current-group()">
                                    <xsl:sequence select="(@css:page/string(),$volume-area-page-style,$default-page-style)[1]"/>
                                </xsl:for-each>
                            </xsl:if>
                        </xsl:for-each-group>
                    </xsl:for-each>
                </xsl:for-each>
            </xsl:for-each>
        </xsl:variable>
        <xsl:sequence select="distinct-values($page-stylesheets)"/>
    </xsl:variable>
    
    <xsl:variable name="page-stylesheets-right-even" as="xs:string*">
        <!--
            page style to use in @begin and @end areas when no page property specified
        -->
        <xsl:variable name="default-page-style" as="xs:string" select="(collection()/*[not(@css:flow)])[1]/string(@css:page)"/>
        <xsl:variable name="page-stylesheets" as="xs:string*">
            <xsl:for-each-group select="collection()/*[not(@css:flow)]" group-starting-with="*[@css:counter-set-page]">
                <xsl:if test="current-group()[1]/@css:counter-set-page[(xs:integer(.) mod 2)=0]">
                    <xsl:sequence select="current-group()/string(@css:page)"/>
                </xsl:if>
            </xsl:for-each-group>
            <xsl:for-each select="$volume-stylesheets">
                <xsl:variable name="volume-style" as="element()*" select="css:parse-stylesheet(.)"/>
                <xsl:variable name="volume-area-styles" as="element()*"
                              select="(if ($volume-style[matches(@selector,'^:')])
                                       then $volume-style/@style/css:parse-stylesheet(.)
                                       else $volume-style)
                                       [@selector=('@begin','@end')]"/>
                <xsl:for-each select="distinct-values($volume-area-styles/@style)">
                    <xsl:variable name="volume-area-style" as="element()*" select="css:parse-stylesheet(.)"/>
                    <xsl:variable name="volume-area-page-style" as="xs:string?"
                                  select="$volume-area-style[@selector='@page']/@style"/>
                    <xsl:variable name="volume-area-style" as="element()*"
                                  select="css:parse-declaration-list($volume-area-style[not(@selector)]/@style)"/>
                    <xsl:for-each select="distinct-values(
                                          css:parse-content-list($volume-area-style[@name='content'][1]/@value,())
                                          /self::css:flow[@from][(@scope,'document')[1]='document']/@from)">
                        <xsl:variable name="flow" as="xs:string" select="."/>
                        <xsl:for-each-group select="collection()/*[@css:flow=$flow]" group-starting-with="*[@css:counter-set-page]">
                            <xsl:if test="current-group()[1]/@css:counter-set-page[(xs:integer(.) mod 2)=0]">
                                <xsl:for-each select="current-group()">
                                    <xsl:sequence select="(@css:page/string(),$volume-area-page-style,$default-page-style)[1]"/>
                                </xsl:for-each>
                            </xsl:if>
                        </xsl:for-each-group>
                    </xsl:for-each>
                </xsl:for-each>
            </xsl:for-each>
        </xsl:variable>
        <xsl:sequence select="distinct-values($page-stylesheets)"/>
    </xsl:variable>
    
    <xsl:function name="pxi:layout-master-name" as="xs:string">
        <xsl:param name="page-stylesheet" as="xs:string"/>
        <xsl:variable name="right-page-odd" as="xs:boolean" select="true()"/>
        <xsl:sequence select="pxi:layout-master-name($page-stylesheet, $right-page-odd)"/>
    </xsl:function>
    
    <xsl:function name="pxi:layout-master-name" as="xs:string">
        <xsl:param name="page-stylesheet" as="xs:string"/>
        <xsl:param name="right-page-odd" as="xs:boolean"/>
        <xsl:sequence select="concat('master_',
                                     if ($right-page-odd)
                                       then index-of($page-stylesheets-right-odd, $page-stylesheet)
                                       else (count($page-stylesheets-right-odd)
                                             + index-of($page-stylesheets-right-even, $page-stylesheet)))"/>
    </xsl:function>
    
    <xsl:function name="pxi:layout-master" as="xs:string">
        <xsl:param name="page-stylesheet" as="xs:string"/>
        <xsl:variable name="right-page-odd" as="xs:boolean" select="true()"/>
        <xsl:sequence select="pxi:layout-master($page-stylesheet, $right-page-odd)"/>
    </xsl:function>
    
    <xsl:function name="pxi:layout-master" as="element()">
        <xsl:param name="page-stylesheet" as="xs:string"/>
        <xsl:param name="right-page-odd" as="xs:boolean"/>
        <xsl:sequence select="obfl:generate-layout-master(
                                $page-stylesheet,
                                pxi:layout-master-name($page-stylesheet, $right-page-odd),
                                $duplex='true',
                                $right-page-odd)"/>
    </xsl:function>
    
    <!--
        Based on a sequence of @volume rules, return a sequence of "use-when" expressions for which
        each volume is quaranteed to match exactly one of them. Should in theory not be needed
        because volume templates are matched in the order they appear.
    -->
    <xsl:function name="obfl:volume-stylesheets-use-when" as="xs:string*">
        <xsl:param name="stylesheets" as="element()*"/>
        <xsl:for-each select="$stylesheets">
            <xsl:variable name="i" select="position()"/>
            <xsl:choose>
                <xsl:when test="not(@selector)">
                    <xsl:sequence select="obfl:not(obfl:or($stylesheets[position()&lt;$i or @selector]/obfl:volume-stylesheets-use-when(.)))"/>
                </xsl:when>
                <xsl:when test="@selector=':first'">
                    <xsl:sequence select="obfl:and((
                                            '(= $volume 1)',
                                            obfl:not(obfl:or($stylesheets[position()&lt;$i and @selector]/obfl:volume-stylesheets-use-when(.)))))"/>
                </xsl:when>
                <xsl:when test="@selector=':last'">
                    <xsl:sequence select="obfl:and((
                                            '(= $volume $volumes)',
                                            obfl:not(obfl:or($stylesheets[position()&lt;$i and @selector]/obfl:volume-stylesheets-use-when(.)))))"/>
                </xsl:when>
                <xsl:when test="matches(@selector,'^:nth\([1-9][0-9]*\)$')">
                    <xsl:sequence select="obfl:and((
                                            concat('(= $volume ',substring(@selector,6)),
                                            obfl:not(obfl:or($stylesheets[position()&lt;$i and @selector]/obfl:volume-stylesheets-use-when(.)))))"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="'nil'"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    </xsl:function>
    
    <!-- ===== -->
    <!-- Flows -->
    <!-- ===== -->
    
    <xsl:variable name="footnote-and-volume-range-flows" as="xs:string*">
        <!--
            FIXME: code duplication! (use css:deep-parse-stylesheet)
        -->
        <xsl:for-each select="distinct-values(($page-stylesheets-right-odd,$page-stylesheets-right-even))">
            <xsl:variable name="page-style" as="xs:string" select="."/>
            <xsl:variable name="page-style" as="element()*" select="css:parse-stylesheet($page-style)"/>
            <xsl:variable name="page-style" as="element()*" select="if ($page-style[matches(@selector,'^:')])
                                                                    then css:parse-stylesheet($page-style[not(@selector)]/@style)
                                                                    else $page-style"/>
            <xsl:variable name="footnotes-style" as="element()*"
                          select="css:parse-declaration-list($page-style[@selector='@footnotes'][1]/@style)"/>
            <xsl:sequence select="css:parse-content-list($footnotes-style[@name='content'][1]/@value,())
                                  /self::css:flow[@from and (not(@scope) or @scope='page')]/@from"/>
        </xsl:for-each>
        <xsl:for-each select="$volume-stylesheets">
            <xsl:variable name="volume-style" as="element()*" select="css:parse-stylesheet(.)"/>
            <xsl:variable name="volume-area-styles" as="element()*"
                          select="(if ($volume-style[matches(@selector,'^:')])
                                   then $volume-style/@style/css:parse-stylesheet(.)
                                   else $volume-style)
                                   [@selector=('@begin','@end')]"/>
            <xsl:for-each select="distinct-values($volume-area-styles/@style)">
                <xsl:variable name="volume-area-style" as="element()*" select="css:parse-stylesheet(.)"/>
                <xsl:variable name="volume-area-style" as="element()*"
                              select="css:parse-declaration-list($volume-area-style[not(@selector)]/@style)"/>
                <xsl:sequence select="css:parse-content-list($volume-area-style[@name='content'][1]/@value,())
                                      /self::css:flow[@from and @scope='volume']/@from"/>
            </xsl:for-each>
        </xsl:for-each>
    </xsl:variable>
    
    <!-- ===== -->
    <!-- Start -->
    <!-- ===== -->
    
    <xsl:variable name="initial-text-transform" as="xs:string" select="'none'"/>
    <xsl:variable name="initial-hyphens" as="xs:string" select="'manual'"/>
    <xsl:variable name="initial-word-spacing" as="xs:integer" select="1"/>
    
    <xsl:template name="start">
        <obfl version="2011-1" xml:lang="und">
            <xsl:variable name="translate" as="xs:string" select="if ($initial-text-transform='none') then 'pre-translated-text-css' else ''"/>
            <xsl:variable name="hyphenate" as="xs:string" select="string($initial-hyphens='auto')"/>
            <xsl:attribute name="hyphenate" select="$hyphenate"/>
            <xsl:if test="$translate!=''">
                <xsl:attribute name="translate" select="$translate"/>
            </xsl:if>
            <xsl:call-template name="_start">
                <xsl:with-param name="text-transform" tunnel="yes" select="$initial-text-transform"/>
                <xsl:with-param name="hyphens" tunnel="yes" select="$initial-hyphens"/>
                <xsl:with-param name="word-spacing" tunnel="yes" select="$initial-word-spacing"/>
            </xsl:call-template>
        </obfl>
    </xsl:template>
    
    <xsl:template name="_start">
            <xsl:for-each select="$page-stylesheets-right-odd">
                <xsl:sequence select="pxi:layout-master(., true())"/>
            </xsl:for-each>
            <xsl:for-each select="$page-stylesheets-right-even">
                <xsl:sequence select="pxi:layout-master(., false())"/>
            </xsl:for-each>
            <xsl:if test="count($volume-stylesheets)&gt;1">
                <xsl:message terminate="yes">Documents with more than one volume style are not supported.</xsl:message>
            </xsl:if>
            <xsl:if test="not(exists($volume-stylesheets))">
                <xsl:message>Document does not have an associated volume style.</xsl:message>
            </xsl:if>
            <xsl:variable name="volume-stylesheet" as="xs:string" select="($volume-stylesheets,'')[1]"/>
            <xsl:if test="$volume-stylesheet!=''">
                <xsl:variable name="volume-stylesheets" as="element()*" select="css:parse-stylesheet($volume-stylesheet)"/>
                <xsl:variable name="volume-stylesheets" as="element()*">
                    <xsl:choose>
                        <xsl:when test="$volume-stylesheets[matches(@selector,'^:')]">
                            <xsl:sequence select="$volume-stylesheets"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <css:rule style="{$volume-stylesheet}"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="volume-stylesheets-use-when" as="xs:string*" select="obfl:volume-stylesheets-use-when($volume-stylesheets)"/>
                <xsl:if test="not(obfl:or($volume-stylesheets-use-when)='nil')">
                    <xsl:variable name="no-upper-limit" select="'1000'"/>
                    <xsl:for-each select="$volume-stylesheets">
                        <xsl:variable name="i" select="position()"/>
                        <xsl:variable name="use-when" as="xs:string" select="$volume-stylesheets-use-when[$i]"/>
                        <xsl:if test="not($use-when='nil')">
                            <xsl:variable name="stylesheet" as="element()*" select="css:parse-stylesheet(@style)"/>
                            <xsl:variable name="properties" as="element()*"
                                          select="css:parse-declaration-list($stylesheet[not(@selector)]/@style)"/>
                            <xsl:variable name="volume-area-rules" as="element()*" select="$stylesheet[@selector=('@begin','@end')]"/>
                            <!--
                                page style to use in @begin and @end areas when no page property specified
                            -->
                            <xsl:variable name="default-page-style" as="xs:string" select="(collection()/*[not(@css:flow)])[1]/string(@css:page)"/>
                            <volume-template sheets-in-volume-max="{($properties[@name='max-length' and css:is-valid(.)]/string(@value),$no-upper-limit)[1]}">
                                <xsl:if test="not($use-when='t')">
                                    <xsl:attribute name="use-when" select="$use-when"/>
                                </xsl:if>
                                <xsl:for-each select="('@begin','@end')">
                                    <xsl:variable name="volume-area" select="."/>
                                    <xsl:variable name="volume-area-style" as="element()*"
                                                  select="css:parse-stylesheet($volume-area-rules[@selector=$volume-area][1]/@style)"/>
                                    <xsl:variable name="volume-area-page-style" as="xs:string?"
                                                  select="$volume-area-style[@selector='@page']/@style"/>
                                    <xsl:variable name="volume-area-style" as="element()*"
                                                  select="css:parse-declaration-list($volume-area-style[not(@selector)]/@style)"/>
                                    <xsl:variable name="volume-area-content" as="element()*"> <!-- css:_|obfl:list-of-references -->
                                        <xsl:apply-templates mode="css:eval-volume-area-content-list"
                                                             select="css:parse-content-list($volume-area-style[@name='content'][1]/@value,())">
                                            <xsl:with-param name="white-space"
                                                            select="($volume-area-style[@name='white-space']/@value,'normal')[1]"/>
                                            <xsl:with-param name="text-transform"
                                                            select="($volume-area-style[@name='text-transform']/@value,'auto')[1]"/>
                                            <xsl:with-param name="hyphens"
                                                            select="($volume-area-style[@name='hyphens']/@value,'manual')[1]"/>
                                            <xsl:with-param name="word-spacing"
                                                            select="($volume-area-style[@name='word-spacing']/@value,1)[1]"/>
                                        </xsl:apply-templates>
                                    </xsl:variable>
                                    <xsl:if test="$volume-area-content">
                                        <xsl:element name="{('pre','post')[index-of(('@begin','@end'),$volume-area)]}-content">
                                            <xsl:variable name="default-page-style" as="xs:string" select="($volume-area-page-style,$default-page-style)[1]"/>
                                            <xsl:for-each-group select="$volume-area-content" group-starting-with="css:_[@css:counter-set-page]">
                                                <xsl:variable name="right-page-odd" as="xs:boolean"
                                                              select="not(current-group()[1]/@css:counter-set-page[(xs:integer(.) mod 2)=0])"/>
                                                <xsl:for-each-group select="current-group()" group-adjacent="(self::css:_/@css:page/string(),$default-page-style)[1]">
                                                    <xsl:variable name="master" select="pxi:layout-master-name(current-grouping-key(), $right-page-odd)"/>
                                                    <xsl:for-each-group select="current-group()" group-ending-with="css:_[*/@css:page-break-after='right']">
                                                        <xsl:for-each-group select="current-group()" group-starting-with="css:_[*/@css:page-break-before='right']">
                                                            <xsl:variable name="counter-set-page" as="attribute()?" select="current-group()[1]/@css:counter-set-page"/>
                                                            <xsl:apply-templates mode="assert-nil-attr"
                                                                                 select="current-group()/self::css:_/(@* except (@css:flow|@css:page|@css:counter-set-page))"/>
                                                            <xsl:variable name="current-group" as="element()*"
                                                                          select="for $e in current-group() return if ($e/self::css:_) then $e/* else $e"/>
                                                            <xsl:variable name="first-toc" as="element()?"
                                                                          select="($current-group/self::css:box[@type='block' and @css:_obfl-toc])[1]"/>
                                                            <xsl:choose>
                                                                <xsl:when test="not($first-toc)">
                                                                    <xsl:variable name="sequence" as="element()*">
                                                                        <xsl:call-template name="apply-templates-within-post-or-pre-content-sequence">
                                                                            <xsl:with-param name="select" select="$current-group"/>
                                                                        </xsl:call-template>
                                                                    </xsl:variable>
                                                                    <xsl:element name="{if ($sequence/self::obfl:list-of-references) then 'dynamic-sequence' else 'sequence'}">
                                                                        <xsl:attribute name="master" select="$master"/>
                                                                        <xsl:apply-templates mode="sequence-attr" select="$counter-set-page"/>
                                                                        <xsl:sequence select="$sequence"/>
                                                                    </xsl:element>
                                                                </xsl:when>
                                                                <xsl:otherwise>
                                                                    <xsl:variable name="before-first-toc" as="element()*">
                                                                        <xsl:for-each-group select="$current-group"
                                                                                            group-starting-with="css:box[@type='block' and @css:_obfl-toc]">
                                                                            <xsl:if test="position()=1
                                                                                          and not(current-group()/self::css:box[@type='block' and @css:_obfl-toc])">
                                                                                <xsl:call-template name="apply-templates-within-post-or-pre-content-sequence">
                                                                                    <xsl:with-param name="select" select="current-group()"/>
                                                                                </xsl:call-template>
                                                                            </xsl:if>
                                                                        </xsl:for-each-group>
                                                                    </xsl:variable>
                                                                    <xsl:for-each-group select="$current-group"
                                                                                        group-starting-with="css:box[@type='block' and @css:_obfl-toc]">
                                                                        <xsl:variable name="toc" as="element()?"
                                                                                      select="current-group()/self::css:box[@type='block' and @css:_obfl-toc]"/>
                                                                        <xsl:if test="exists($toc)">
                                                                            <xsl:variable name="toc-name" select="generate-id($toc)"/>
                                                                            <xsl:variable name="toc-range" as="xs:string"
                                                                                          select="($toc/@css:_obfl-toc-range,'document')[1]"/>
                                                                            <xsl:variable name="on-toc-start" as="element()*"
                                                                                          select="collection()/*[@css:flow=concat('-obfl-on-toc-start/',
                                                                                                                                  $toc/@css:_obfl-on-toc-start)]/*"/>
                                                                            <xsl:variable name="on-volume-start" as="element()*"
                                                                                          select="if ($toc-range='document' and $toc/@css:_obfl-on-volume-start)
                                                                                                  then collection()/*[@css:flow=concat('-obfl-on-volume-start/',
                                                                                                                                       $toc/@css:_obfl-on-volume-start)]/*
                                                                                                  else ()"/>
                                                                            <xsl:variable name="on-volume-end" as="element()*"
                                                                                          select="if ($toc-range='document' and $toc/@css:_obfl-on-volume-end)
                                                                                                  then collection()/*[@css:flow=concat('-obfl-on-volume-end/',
                                                                                                                                       $toc/@css:_obfl-on-volume-end)]/*
                                                                                                  else ()"/>
                                                                            <xsl:variable name="on-toc-end" as="element()*"
                                                                                          select="(collection()/*[@css:flow=concat('-obfl-on-toc-end/',
                                                                                                                                   $toc/@css:_obfl-on-toc-end)]/*)"/>
                                                                            <xsl:variable name="before-toc" as="element()*" select="if (position()=2) then $before-first-toc else ()"/>
                                                                            <xsl:variable name="after-toc" as="element()*">
                                                                                <xsl:call-template name="apply-templates-within-post-or-pre-content-sequence">
                                                                                    <xsl:with-param name="select" select="current-group()[not(self::css:box[@type='block' and @css:_obfl-toc])]"/>
                                                                                </xsl:call-template>
                                                                            </xsl:variable>
                                                                            <xsl:if test="exists($before-toc) and not($toc-range='document')
                                                                                          or $before-toc/self::obfl:list-of-references">
                                                                                <xsl:element name="{if ($before-toc/self::obfl:list-of-references) then 'dynamic-sequence' else 'sequence'}">
                                                                                    <xsl:attribute name="master" select="$master"/>
                                                                                    <xsl:apply-templates mode="sequence-attr" select="$counter-set-page"/>
                                                                                    <xsl:sequence select="$before-toc"/>
                                                                                </xsl:element>
                                                                            </xsl:if>
                                                                            <toc-sequence master="{$master}" range="{$toc-range}" toc="{$toc-name}">
                                                                                <xsl:if test="position()=1
                                                                                              or (exists($before-toc) and $toc-range='document' and not($before-toc/self::obfl:list-of-references))">
                                                                                    <xsl:apply-templates mode="sequence-attr" select="$counter-set-page"/>
                                                                                </xsl:if>
                                                                                <!--
                                                                                    Inserting table-of-contents here as child of toc-sequence. Will be moved to the
                                                                                    right place (child of obfl) later.
                                                                                -->
                                                                                <table-of-contents name="{$toc-name}">
                                                                                    <xsl:apply-templates mode="table-of-contents" select="$toc"/>
                                                                                </table-of-contents>
                                                                                <xsl:if test="(exists($before-toc) and $toc-range='document' and not($before-toc/self::obfl:list-of-references))
                                                                                              or exists($on-toc-start)
                                                                                              or $toc/@css:page-break-before='always'">
                                                                                    <on-toc-start>
                                                                                        <xsl:if test="$toc-range='document' and not($before-toc/self::obfl:list-of-references)">
                                                                                            <xsl:sequence select="$before-toc"/>
                                                                                        </xsl:if>
                                                                                        <xsl:if test="$toc/@css:page-break-before='always'">
                                                                                            <block break-before="page"/>
                                                                                        </xsl:if>
                                                                                        <xsl:apply-templates mode="sequence" select="$on-toc-start"/>
                                                                                    </on-toc-start>
                                                                                </xsl:if>
                                                                                <xsl:if test="exists($on-volume-start)">
                                                                                    <on-volume-start>
                                                                                        <xsl:apply-templates mode="sequence" select="$on-volume-start"/>
                                                                                    </on-volume-start>
                                                                                </xsl:if>
                                                                                <xsl:if test="exists($on-volume-end)">
                                                                                    <on-volume-end>
                                                                                        <xsl:apply-templates mode="sequence" select="$on-volume-end"/>
                                                                                    </on-volume-end>
                                                                                </xsl:if>
                                                                                <xsl:if test="exists($on-toc-end) or (exists($after-toc) and not($after-toc/self::obfl:list-of-references))">
                                                                                    <on-toc-end>
                                                                                        <xsl:apply-templates mode="sequence" select="$on-toc-end"/>
                                                                                        <xsl:if test="not($after-toc/self::obfl:list-of-references)">
                                                                                            <xsl:sequence select="$after-toc"/>
                                                                                        </xsl:if>
                                                                                    </on-toc-end>
                                                                                </xsl:if>
                                                                            </toc-sequence>
                                                                            <xsl:if test="$after-toc/self::obfl:list-of-references">
                                                                                <xsl:element name="{if ($after-toc/self::obfl:list-of-references) then 'dynamic-sequence' else 'sequence'}">
                                                                                    <xsl:attribute name="master" select="$master"/>
                                                                                    <xsl:sequence select="$after-toc"/>
                                                                                </xsl:element>
                                                                            </xsl:if>
                                                                        </xsl:if>
                                                                    </xsl:for-each-group>
                                                                </xsl:otherwise>
                                                            </xsl:choose>
                                                        </xsl:for-each-group>
                                                    </xsl:for-each-group>
                                                </xsl:for-each-group>
                                            </xsl:for-each-group>
                                        </xsl:element>
                                    </xsl:if>
                                </xsl:for-each>
                            </volume-template>
                        </xsl:if>
                    </xsl:for-each>
                </xsl:if>
            </xsl:if>
            <xsl:apply-templates mode="assert-nil" select="collection()/*[not(self::css:_)]"/>
            <xsl:for-each select="collection()/css:_[@css:flow=$footnote-and-volume-range-flows]">
                <xsl:variable name="flow" as="xs:string" select="@css:flow"/>
                <collection name="{$flow}">
                    <xsl:for-each select="*">
                        <xsl:if test="@css:anchor='NULL'">
                            <xsl:message terminate="yes">Flowed element does not have anchor in normal flow</xsl:message>
                        </xsl:if>
                        <!--
                            We don't explicitly check that two items do not end up having the same
                            ID, which would trigger a "Identifier is not unique" error in
                            Dotify. Until this happens in practice I just assume that it can not
                            happen.
                        -->
                        <item id="{@css:anchor}">
                            <xsl:apply-templates mode="item" select="."/>
                        </item>
                    </xsl:for-each>
                </collection>
                <xsl:if test="collection()/css:_[@css:flow[not(.=$footnote-and-volume-range-flows)]]/*/@css:_obfl-use-when-collection-not-empty=$flow">
                    <collection name="meta/{$flow}">
                        <xsl:for-each select="*[1]">
                            <!--
                                giving this dummy item the same ID as the first item of the real collection seems to work
                            -->
                            <item id="{@css:anchor}">
                                <block/>
                            </item>
                        </xsl:for-each>
                    </collection>
                </xsl:if>
            </xsl:for-each>
            <xsl:for-each-group select="collection()/css:_[not(@css:flow)]" group-starting-with="*[@css:counter-set-page]">
                <xsl:variable name="right-page-odd" as="xs:boolean"
                              select="not(current-group()[1]/@css:counter-set-page[(xs:integer(.) mod 2)=0])"/>
                <xsl:for-each-group select="current-group()" group-adjacent="string(@css:page)">
                    <xsl:variable name="layout-master" select="pxi:layout-master-name(current-grouping-key(), $right-page-odd)"/>
                    <xsl:for-each-group select="current-group()" group-starting-with="css:_[*/@css:page-break-before='right']">
                        <xsl:for-each-group select="current-group()" group-ending-with="css:_[*/@css:page-break-after='right']">
                            <xsl:for-each-group select="current-group()" group-starting-with="css:_[*/@css:volume-break-before='always']">
                                <sequence master="{$layout-master}">
                                    <xsl:apply-templates mode="sequence-attr"
                                                         select="current-group()[1]/(@* except (@css:page|@css:volume|@css:string-entry))"/>
                                    <xsl:apply-templates mode="sequence-attr"
                                                         select="current-group()[1]/*/@css:volume-break-before[.='always']"/>
                                    <xsl:apply-templates mode="sequence"
                                                         select="current-group()[1]/(@css:string-entry|*)"/>
                                    <xsl:apply-templates mode="assert-nil-attr"
                                                         select="current-group()[position()&gt;1]/(@* except (@css:page|@css:volume|@css:string-entry))"/>
                                    <xsl:apply-templates mode="sequence"
                                                         select="current-group()[position()&gt;1]/*"/>
                                </sequence>
                            </xsl:for-each-group>
                        </xsl:for-each-group>
                    </xsl:for-each-group>
                </xsl:for-each-group>
            </xsl:for-each-group>
    </xsl:template>
    
    <xsl:template name="apply-templates-within-post-or-pre-content-sequence">
        <xsl:param name="select" as="element()*" required="yes"/> <!-- (css:box|obfl:list-of-references)* -->
        <xsl:for-each-group select="$select" group-adjacent="boolean(self::obfl:list-of-references)">
            <xsl:choose>
                <xsl:when test="current-grouping-key()">
                    <xsl:sequence select="current-group()"/>
                </xsl:when>
                <xsl:otherwise> <!-- css:box -->
                    <xsl:for-each-group select="current-group()" group-adjacent="(@css:_obfl-use-when-collection-not-empty,'normal')[1]">
                        <xsl:variable name="flow" as="xs:string" select="current-grouping-key()"/>
                        <xsl:choose>
                            <xsl:when test="not($flow='normal')">
                                <xsl:if test="$flow=$footnote-and-volume-range-flows">
                                    <list-of-references collection="meta/{$flow}" range="document">
                                        <on-collection-start>
                                            <xsl:apply-templates mode="sequence" select="current-group()"/>
                                        </on-collection-start>
                                    </list-of-references>
                                </xsl:if>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:apply-templates mode="sequence" select="current-group()"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each-group>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each-group>
    </xsl:template>
    
    <!-- ======== -->
    <!-- Sequence -->
    <!-- ======== -->
    
    <xsl:template mode="sequence-attr"
                  match="/css:_/@css:counter-set-page">
        <xsl:attribute name="initial-page-number" select="."/>
    </xsl:template>
    
    <xsl:template mode="sequence"
                  match="/css:_/@css:string-entry">
        <block>
            <xsl:apply-templates mode="css:parse-string-entry" select="css:parse-string-set(.)"/>
        </block>
    </xsl:template>
    
    <xsl:template mode="css:parse-string-entry"
                  match="css:string-set">
        <xsl:variable name="value" as="xs:string*">
            <xsl:apply-templates mode="css:eval-string-set" select="css:parse-content-list(@value, ())"/>
        </xsl:variable>
        <marker class="{@name}/entry" value="{replace(string-join($value,''),'^\s+|\s+$','')}"/>
    </xsl:template>
    
    <!-- ===== -->
    <!-- Boxes -->
    <!-- ===== -->
    
    <xsl:template mode="block-attr span-attr td-attr table-attr assert-nil-attr"
                  match="css:box/@part"/>
    
    <xsl:template mode="block-attr span-attr td-attr table-attr toc-entry-attr assert-nil-attr"
                  match="css:box/@name|
                         css:box/css:_/@name"/>
    
    <!-- =========== -->
    <!-- Block boxes -->
    <!-- =========== -->
    
    <xsl:template mode="sequence item td"
                  match="css:box[@type='block']">
        <xsl:apply-templates mode="block" select="."/>
    </xsl:template>
    
    <xsl:template mode="table-of-contents"
                  match="css:box[@type='block']">
        <xsl:apply-templates mode="toc-entry" select="."/>
    </xsl:template>
    
    <!--
        block or toc-entry element depending on context
    -->
    <xsl:template priority="0.8"
                  mode="block"
                  match="css:box[@type='block']">
        <block>
            <xsl:next-match/>
        </block>
    </xsl:template>
    <xsl:template priority="0.8"
                  mode="toc-entry"
                  match="css:box[@type='block']">
        <!--
            Automatically compute the toc-entry's ref-id by searching for target-counter(),
            target-text() and target-string() values within the current block, descendant blocks,
            following blocks or preceding blocks (in that order). It is currently not possible to
            define the ref-id directly in CSS which means a table-of-contents can not be constructed
            if no references are used for rendering content (such as braille page numbers or print
            page numbers).
        -->
        <!--
            TODO: warning when not all references in a block point to the same element
            TODO: warning when a block has no references or descendant blocks with references
        -->
        <xsl:variable name="descendant-refs" as="attribute()*"
                      select="((descendant::css:box)/@css:anchor
                               |(descendant::css:string)/@target
                               |(descendant::css:counter)/@target)"/>
        <xsl:variable name="following-refs" as="attribute()*"
                      select="((following::css:box)/@css:anchor
                               |(following::css:string)/@target
                               |(following::css:counter)/@target)"/>
        <xsl:variable name="preceding-refs" as="attribute()*"
                      select="(preceding::css:box/@css:anchor
                               |preceding::css:string/@target
                               |preceding::css:counter/@target)"/>
        <xsl:choose>
            <xsl:when test="exists($descendant-refs[some $id in string(.) satisfies collection()/*[not(@css:flow)]//*[@css:id=$id]])">
                <xsl:variable name="ref-id" as="xs:string"
                              select="$descendant-refs[some $id in string(.) satisfies collection()/*[not(@css:flow)]//*[@css:id=$id]][1]"/>
                <toc-entry ref-id="{$ref-id}">
                    <xsl:next-match>
                        <xsl:with-param name="toc-entry-ref-id" select="$ref-id" tunnel="yes"/>
                    </xsl:next-match>
                </toc-entry>
            </xsl:when>
            <xsl:when test="exists($descendant-refs)">
                <!--
                    if the entry references an element in a named flow, we assume that element is
                    part of the volume begin or end area, and is therefore omitted from the table of
                    contents
                -->
            </xsl:when>
            <xsl:when test="exists($following-refs[some $id in string(.) satisfies collection()/*[not(@css:flow)]//*[@css:id=$id]])">
                <xsl:variable name="ref-id" as="xs:string"
                              select="$following-refs[some $id in string(.) satisfies collection()/*[not(@css:flow)]//*[@css:id=$id]][1]"/>
                <toc-entry ref-id="{$ref-id}">
                    <xsl:next-match>
                        <xsl:with-param name="toc-entry-ref-id" select="$ref-id" tunnel="yes"/>
                    </xsl:next-match>
                </toc-entry>
            </xsl:when>
            <xsl:when test="exists($preceding-refs[some $id in string(.) satisfies collection()/*[not(@css:flow)]//*[@css:id=$id]])">
                <xsl:variable name="ref-id" as="xs:string"
                              select="$preceding-refs[some $id in string(.) satisfies collection()/*[not(@css:flow)]//*[@css:id=$id]][last()]"/>
                <toc-entry ref-id="{$ref-id}">
                    <xsl:next-match>
                        <xsl:with-param name="toc-entry-ref-id" select="$ref-id" tunnel="yes"/>
                    </xsl:next-match>
                </toc-entry>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message select="concat(
                                       'An element with display: -obfl-toc must have at least one descendant ',
                                       'target-counter(), target-string() or target-text() value (that references ',
                                       'an element that does not participate in a named flow).')">
                </xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!--
        attributes that apply on outer block: translate, hyphenate
    -->
    <xsl:template priority="0.71"
                  mode="block toc-entry"
                  match="css:box[@type='block']"
                  name="insert-text-attributes-and-next-match">
        <xsl:param name="text-transform" as="xs:string" tunnel="yes"/>
        <xsl:param name="hyphens" as="xs:string" tunnel="yes"/>
        <xsl:variable name="new-text-transform" as="xs:string?">
            <xsl:apply-templates mode="css:text-transform" select="."/>
        </xsl:variable>
        <xsl:variable name="new-hyphens" as="xs:string?">
            <xsl:apply-templates mode="css:hyphens" select="."/>
        </xsl:variable>
        <xsl:call-template name="obfl:translate">
            <xsl:with-param name="new-text-transform" select="$new-text-transform"/>
        </xsl:call-template>
        <xsl:call-template name="obfl:hyphenate">
            <xsl:with-param name="new-hyphens" select="$new-hyphens"/>
        </xsl:call-template>
        <xsl:next-match>
            <xsl:with-param name="text-transform" tunnel="yes" select="($new-text-transform,$text-transform)[1]"/>
            <xsl:with-param name="hyphens" tunnel="yes" select="($new-hyphens,$hyphens)[1]"/>
        </xsl:next-match>
    </xsl:template>
    
    <!--
        other attributes that apply on outer block
    -->
    <xsl:template priority="0.7"
                  mode="block toc-entry"
                  match="css:box[@type='block']">
        <xsl:apply-templates mode="block-attr"
                             select="@* except (@type|
                                                @css:text-transform|@css:hyphens|
                                                @css:string-set|@css:_obfl-marker|
                                                @css:line-height|@css:text-align|@css:text-indent|@page-break-inside)"/>
        <xsl:next-match/>
    </xsl:template>
    
    <!--
        not wrapped in additional block
    -->
    <xsl:template priority="0.6"
                  mode="block toc-entry"
                  match="css:box[@type='block']
                                [not(@css:line-height
                                     and (@css:margin-top or @css:margin-bottom or
                                          @css:border-top or @css:border-bottom))]">
        <xsl:apply-templates mode="block-attr"
                             select="@css:line-height|@css:text-align|@css:text-indent|@page-break-inside"/>
        <xsl:apply-templates mode="#current"
                             select="@css:string-set|@css:_obfl-marker"/>
        <xsl:apply-templates mode="#current"/>
        <xsl:apply-templates mode="css:anchor" select="@css:id"/>
    </xsl:template>
    
    <!--
        wrap content in additional block or toc-entry element when line-height > 1 is combined with
        top/bottom margin or border
    -->
    <xsl:template priority="0.63"
                  mode="block toc-entry"
                  match="css:box[@type='block']
                                [@css:line-height
                                 and (@css:margin-top or @css:margin-bottom or
                                      @css:border-top or @css:border-bottom)]">
        <xsl:apply-templates mode="#current" select="@css:string-set|@css:_obfl-marker"/>
        <xsl:next-match/>
    </xsl:template>
    
    <!--
        block or toc-entry element depending on context
    -->
    <xsl:template priority="0.62"
                  mode="block"
                  match="css:box[@type='block']
                                [@css:line-height
                                 and (@css:margin-top or @css:margin-bottom or
                                      @css:border-top or @css:border-bottom)]">
        <block>
            <xsl:next-match/>
        </block>
    </xsl:template>
    <xsl:template priority="0.61"
                  mode="toc-entry"
                  match="css:box[@type='block']
                                [@css:line-height
                                 and (@css:margin-top or @css:margin-bottom or
                                      @css:border-top or @css:border-bottom)]">
        <xsl:param name="toc-entry-ref-id" as="xs:string" tunnel="yes"/>
        <toc-entry ref-id="{$toc-entry-ref-id}">
            <xsl:next-match/>
        </toc-entry>
    </xsl:template>
    
    <!--
        attributes that apply on inner block
    -->
    <xsl:template priority="0.6"
                  mode="block toc-entry"
                  match="css:box[@type='block']
                                [@css:line-height
                                 and (@css:margin-top or @css:margin-bottom or
                                      @css:border-top or @css:border-bottom)]">
        <xsl:apply-templates mode="block-attr"
                             select="@css:line-height|@css:text-align|@css:text-indent|@page-break-inside"/>
        <!--
            repeat orphans/widows (why?)
        -->
        <xsl:apply-templates mode="block-attr" select="@css:orphans|@css:widows"/>
        <xsl:apply-templates mode="#current"/>
        <xsl:apply-templates mode="css:anchor" select="@css:id"/>
    </xsl:template>
    
    <!-- ====== -->
    <!-- Tables -->
    <!-- ====== -->
    
    <xsl:template priority="0.8"
                  mode="sequence"
                  match="css:box[@type='table']|
                         css:box[@type='block'][descendant::css:box[@type='table']]">
        <table>
            <xsl:apply-templates mode="table" select="."/>
        </table>
    </xsl:template>
    
    <xsl:template priority="0.6"
                  mode="table"
                  match="css:box[@type=('block','table')]">
        <xsl:call-template name="insert-text-attributes-and-next-match"/>
    </xsl:template>
    
    <xsl:template mode="table"
                  match="css:box[@type='block']">
        <xsl:apply-templates mode="table-attr" select="@* except (@type|@css:text-transform|@css:hyphens)"/>
        <xsl:apply-templates mode="#current"/>
    </xsl:template>
    
    <xsl:template mode="table"
                  match="css:box[@type='table']">
        <xsl:apply-templates mode="table-attr" select="@* except (@type|@css:render-table-by|
                                                                  @css:text-transform|@css:hyphens)"/>
        <xsl:if test="@css:render-table-by and not(@css:render-table-by='column')">
            <xsl:message>'render-table-by' property with a value other than 'column' is not supported on elements with 'display: table'.</xsl:message>
        </xsl:if>
        <xsl:choose>
            <xsl:when test="@css:render-table-by='column'">
                <xsl:for-each-group select="css:box[@type='table-cell']" group-by="@css:table-column">
                    <xsl:sort select="xs:integer(current-grouping-key())"/>
                    <tr>
                        <xsl:for-each select="current-group()">
                            <xsl:sort select="if (@css:table-header-group) then 1 else
                                              if (@css:table-row-group) then 2 else
                                              if (@css:table-footer-group) then 3 else ()"/>
                            <xsl:sort select="xs:integer((@css:table-header-group,@css:table-row-group,@css:table-footer-group)[1])"/>
                            <xsl:sort select="xs:integer(@css:table-row)"/>
                            <xsl:apply-templates mode="tr" select="."/>
                        </xsl:for-each>
                    </tr>
                </xsl:for-each-group>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="header-cells" as="element()*" select="css:box[@type='table-cell' and @css:table-header-group]"/>
                <xsl:variable name="body-cells" as="element()*" select="css:box[@type='table-cell' and @css:table-row-group]"/>
                <xsl:variable name="footer-cells" as="element()*" select="css:box[@type='table-cell' and @css:table-footer-group]"/>
                <xsl:variable name="header" as="element()*">
                    <xsl:for-each-group select="$header-cells" group-by="@css:table-row">
                        <xsl:sort select="xs:integer(current-grouping-key())"/>
                        <tr>
                            <xsl:for-each select="current-group()">
                                <xsl:sort select="xs:integer(@css:table-column)"/>
                                <xsl:apply-templates mode="tr" select="."/>
                            </xsl:for-each>
                        </tr>
                    </xsl:for-each-group>
                </xsl:variable>
                <xsl:variable name="body" as="element()*">
                    <xsl:for-each-group select="$body-cells" group-by="@css:table-row-group">
                        <xsl:sort select="xs:integer(current-grouping-key())"/>
                        <xsl:for-each-group select="current-group()" group-by="@css:table-row">
                            <xsl:sort select="xs:integer(current-grouping-key())"/>
                            <tr>
                                <xsl:for-each select="current-group()">
                                    <xsl:sort select="xs:integer(@css:table-column)"/>
                                    <xsl:apply-templates mode="tr" select="."/>
                                </xsl:for-each>
                            </tr>
                        </xsl:for-each-group>
                    </xsl:for-each-group>
                </xsl:variable>
                <xsl:variable name="footer" as="element()*">
                    <xsl:for-each-group select="$footer-cells" group-by="@css:table-row">
                        <xsl:sort select="xs:integer(current-grouping-key())"/>
                        <tr>
                            <xsl:for-each select="current-group()">
                                <xsl:sort select="xs:integer(@css:table-column)"/>
                                <xsl:apply-templates mode="tr" select="."/>
                            </xsl:for-each>
                        </tr>
                    </xsl:for-each-group>
                </xsl:variable>
                <xsl:apply-templates mode="table" select="node() except ($header-cells|$body-cells|$footer-cells)"/>
                <xsl:choose>
                    <xsl:when test="exists($header)">
                        <thead>
                            <xsl:sequence select="$header"/>
                        </thead>
                        <tbody>
                            <xsl:sequence select="$body"/>
                            <xsl:sequence select="$footer"/>
                        </tbody>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:sequence select="$body"/>
                        <xsl:sequence select="$footer"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template priority="0.6"
                  mode="tr"
                  match="css:box[@type='table-cell']">
        <td>
            <xsl:call-template name="insert-text-attributes-and-next-match"/>
        </td>
    </xsl:template>
    
    <xsl:template mode="tr"
                  match="css:box[@type='table-cell']">
        <xsl:if test="@css:table-row-span">
            <xsl:attribute name="{if (parent::*/@css:render-table-by='column') then 'col-span' else 'row-span'}"
                           select="@css:table-row-span"/>
        </xsl:if>
        <xsl:if test="@css:table-column-span">
            <xsl:attribute name="{if (parent::*/@css:render-table-by='column') then 'row-span' else 'col-span'}"
                           select="@css:table-column-span"/>
        </xsl:if>
        <xsl:apply-templates mode="td-attr"
                             select="@* except (@type|
                                                @css:text-transform|@css:hyphens|
                                                @css:string-set|@css:_obfl-marker|
                                                @css:table-header-group|
                                                @css:table-row-group|
                                                @css:table-footer-group|
                                                @css:table-row|
                                                @css:table-column|
                                                @css:table-row-span|
                                                @css:table-column-span)"/>
        <xsl:apply-templates mode="td"
                             select="@css:string-set|@css:_obfl-marker"/>
        <xsl:apply-templates mode="td"/>
    </xsl:template>
    
    <xsl:template mode="td"
                  match="css:box[@type='table']">
        <xsl:message terminate="yes">Nested tables not supported.</xsl:message>
    </xsl:template>
    
    <!-- ============ -->
    <!-- Inline boxes -->
    <!-- ============ -->
    
    <xsl:template mode="block span td toc-entry"
                  match="css:box[@type='inline']">
        <xsl:param name="text-transform" as="xs:string" tunnel="yes"/>
        <xsl:param name="hyphens" as="xs:string" tunnel="yes"/>
        <xsl:param name="pending-text-transform" as="xs:string?" tunnel="yes" select="()"/>
        <xsl:param name="pending-hyphens" as="xs:string?" tunnel="yes" select="()"/>
        <xsl:variable name="pending-text-transform" as="xs:string?"
                      select="(@css:text-transform/string(),$pending-text-transform)[1]"/>
        <xsl:variable name="pending-hyphens" as="xs:string?"
                      select="(@css:hyphens/string(),$pending-hyphens)[1]"/>
        <xsl:apply-templates mode="#current" select="@css:string-set|@css:_obfl-marker"/>
        <xsl:apply-templates mode="assert-nil-attr"
                             select="@* except (@type|
                                                @css:string-set|
                                                @css:_obfl-marker|
                                                @css:text-transform|@css:hyphens)"/>
        <xsl:for-each-group select="node()" group-adjacent="boolean(
                                                              self::css:box[@type='inline'] or
                                                              self::css:custom-func[@name='-obfl-evaluate'] or
                                                              self::css:counter[@target][@name='page'] or
                                                              self::css:leader)">
            <xsl:choose>
                <xsl:when test="current-grouping-key()">
                    <xsl:apply-templates mode="#current" select="current-group()">
                        <xsl:with-param name="pending-text-transform" tunnel="yes" select="$pending-text-transform"/>
                        <xsl:with-param name="pending-hyphens" tunnel="yes" select="$pending-hyphens"/>
                    </xsl:apply-templates>
                </xsl:when>
                <xsl:when test="every $n in current-group() satisfies
                                $n/self::text() and matches(string($n),'^[ \t\n\r&#x2800;&#x00AD;&#x200B;]*$')">
                    <xsl:value-of select="."/>
                </xsl:when>
                <!--
                    FIXME: this is a hack to avoid ending up with spans around <br/> elements, but
                    this should be fixed either in the pre-translator or in the Dotify white space
                    normalizer.
                -->
                <xsl:when test="every $n in current-group() satisfies
                                matches(string($n),'^[\s&#x2800;]*$')
                                and not($n/descendant-or-self::css:string|
                                        $n/descendant-or-self::css:counter|
                                        $n/descendant-or-self::css:leader|
                                        $n/descendant-or-self::css:custom-func)">
                    <xsl:apply-templates mode="#current" select="current-group()"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:variable name="new-text-transform" as="xs:string" select="($pending-text-transform,$text-transform)[1]"/>
                    <xsl:variable name="new-hyphens" as="xs:string" select="($pending-hyphens,$hyphens)[1]"/>
                    <xsl:variable name="attrs" as="attribute()*">
                        <xsl:call-template name="obfl:translate">
                            <xsl:with-param name="new-text-transform" select="$new-text-transform"/>
                        </xsl:call-template>
                        <xsl:call-template name="obfl:hyphenate">
                            <xsl:with-param name="new-hyphens" select="$new-hyphens"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:choose>
                        <xsl:when test="exists($attrs)">
                            <span>
                                <xsl:sequence select="$attrs"/>
                                <xsl:apply-templates mode="span" select="current-group()">
                                    <xsl:with-param name="pending-text-transform" tunnel="yes" select="()"/>
                                    <xsl:with-param name="pending-hyphens" tunnel="yes" select="()"/>
                                    <xsl:with-param name="text-transform" tunnel="yes" select="$new-text-transform"/>
                                    <xsl:with-param name="hyphens" tunnel="yes" select="$new-hyphens"/>
                                </xsl:apply-templates>
                            </span>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:apply-templates mode="#current" select="current-group()">
                                <xsl:with-param name="pending-text-transform" tunnel="yes" select="()"/>
                                <xsl:with-param name="pending-hyphens" tunnel="yes" select="()"/>
                                <xsl:with-param name="text-transform" tunnel="yes" select="$new-text-transform"/>
                                <xsl:with-param name="hyphens" tunnel="yes" select="$new-hyphens"/>
                            </xsl:apply-templates>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each-group>
        <xsl:apply-templates mode="css:anchor" select="@css:id"/>
    </xsl:template>
    
    <!-- ===================== -->
    <!-- Other inline elements -->
    <!-- ===================== -->
    
    <xsl:template mode="block span td table toc-entry"
                  match="css:box/css:_">
        <xsl:apply-templates mode="assert-nil-attr" select="@* except (@css:string-set|@css:_obfl-marker)"/>
        <xsl:apply-templates mode="#current" select="@css:string-set|@css:_obfl-marker"/>
        <xsl:apply-templates mode="#current"/>
    </xsl:template>
    
    <xsl:template priority="2"
                  mode="block span toc-entry"
                  match="css:box/css:_/node()">
        <xsl:call-template name="coding-error"/>
    </xsl:template>
    
    <!-- =============== -->
    <!-- Text attributes -->
    <!-- =============== -->
    
    <xsl:template mode="css:text-transform" match="css:box" as="xs:string?">
        <xsl:sequence select="@css:text-transform/string()"/>
    </xsl:template>
    
    <xsl:template mode="css:hyphens" match="css:box" as="xs:string?">
        <xsl:sequence select="@css:hyphens/string()"/>
    </xsl:template>
    
    <xsl:template name="obfl:hyphenate" as="attribute()?"> <!-- @hyphenate? -->
        <xsl:param name="hyphens" as="xs:string" tunnel="yes"/>
        <xsl:param name="new-hyphens" as="xs:string?"/>
        <!--
            'hyphens:auto' corresponds with 'hyphenate="true"'. 'hyphens:manual' corresponds with
            'hyphenate="false"'. For 'hyphens:none' all SHY and ZWSP characters are removed from the
            text.
        -->
        <xsl:choose>
            <xsl:when test="not(exists($new-hyphens))"/>
            <xsl:when test="$new-hyphens='auto' and not($hyphens='auto')">
                <xsl:attribute name="hyphenate" select="'true'"/>
            </xsl:when>
            <xsl:when test="not($new-hyphens='auto') and $hyphens='auto'">
                <xsl:attribute name="hyphenate" select="'false'"/>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template name="obfl:translate" as="attribute()?"> <!-- @translate? -->
        <xsl:param name="text-transform" as="xs:string" tunnel="yes"/>
        <xsl:param name="new-text-transform" as="xs:string?"/>
        <!--
            'text-transform:auto' corresponds with 'translate=""'. 'text-transform:none' corresponds
            with 'translate="pre-translated-text-css"'. Other values of text-transform are handled
            through style elements and text-style attributes.
        -->
        <xsl:choose>
            <xsl:when test="not(exists($new-text-transform))"/>
            <xsl:when test="$new-text-transform='none' and not($text-transform='none')">
                <xsl:attribute name="translate" select="'pre-translated-text-css'"/>
            </xsl:when>
            <xsl:when test="not($new-text-transform='none') and $text-transform='none'">
                <xsl:attribute name="translate" select="''"/>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template priority="1"
                  mode="block span td table toc-entry"
                  match="css:box[@css:word-spacing]">
        <xsl:next-match>
            <xsl:with-param name="word-spacing" tunnel="yes" select="xs:integer(@css:word-spacing)"/>
        </xsl:next-match>
    </xsl:template>
    
    <xsl:template mode="block-attr span-attr td-attr table-attr toc-entry-attr assert-nil-attr"
                  match="css:box/@css:word-spacing"/>
    
    <!-- ================ -->
    <!-- Other attributes -->
    <!-- ================ -->
    
    <xsl:template priority="0.6"
                  mode="block-attr toc-entry-attr"
                  match="/css:_/*/@css:_obfl-toc|
                         /css:_/*[@css:_obfl-toc]/@css:page-break-before[.='always']|
                         /css:_/*[@css:_obfl-toc]/@css:_obfl-toc-range|
                         /css:_/*[@css:_obfl-toc]/@css:_obfl-on-toc-start|
                         /css:_/*[@css:_obfl-toc]/@css:_obfl-on-volume-start|
                         /css:_/*[@css:_obfl-toc]/@css:_obfl-on-volume-end|
                         /css:_/*[@css:_obfl-toc]/@css:_obfl-on-toc-end"/>
    
    <xsl:template mode="block-attr table-attr toc-entry-attr"
                  match="css:box[@type=('block','table')]/@css:margin-left|
                         css:box[@type=('block','table')]/@css:margin-right|
                         css:box[@type=('block','table')]/@css:margin-top|
                         css:box[@type=('block','table')]/@css:margin-bottom|
                         css:box[@type=('block','table')]/@css:padding-left|
                         css:box[@type=('block','table')]/@css:padding-right|
                         css:box[@type=('block','table')]/@css:padding-top|
                         css:box[@type=('block','table')]/@css:padding-bottom">
        <xsl:attribute name="{local-name()}" select="format-number(xs:integer(number(.)), '0')"/>
    </xsl:template>
    
    <!--
        combine margin and padding on table cells
    -->
    <xsl:template mode="td-attr"
                  match="css:box[@type='table-cell' and not(@css:padding-left)]/@css:margin-left|
                         css:box[@type='table-cell' and not(@css:padding-right)]/@css:margin-right|
                         css:box[@type='table-cell' and not(@css:padding-top)]/@css:margin-top|
                         css:box[@type='table-cell' and not(@css:padding-bottom)]/@css:margin-bottom">
        <xsl:attribute name="{replace(local-name(),'margin','padding')}"
                       select="format-number(xs:integer(number(.)), '0')"/>
    </xsl:template>
    
    <xsl:template mode="td-attr"
                  match="css:box[@type='table-cell']/@css:padding-left|
                         css:box[@type='table-cell']/@css:padding-right|
                         css:box[@type='table-cell']/@css:padding-top|
                         css:box[@type='table-cell']/@css:padding-bottom">
        <xsl:variable name="padding" as="xs:integer" select="xs:integer(number(.))"/>
        <xsl:variable name="margin-name" as="xs:string" select="replace(local-name(),'padding','margin')"/>
        <xsl:variable name="margin" as="xs:integer" select="xs:integer(number((parent::*/@css:*[local-name()=$margin-name],0)[1]))"/>
        <xsl:attribute name="{local-name()}" select="format-number($padding + $margin, '0')"/>
    </xsl:template>
    
    <xsl:template mode="block-attr table-attr toc-entry-attr"
                  match="css:box[@type=('block','table')]/@css:line-height">
        <xsl:attribute name="row-spacing" select="format-number(xs:integer(number(.)), '0.0')"/>
    </xsl:template>
    
    <!--
        handle negative text-indent
    -->
    <xsl:template priority="0.6"
                  mode="block-attr table-attr td-attr toc-entry-attr"
                  match="css:box[@type=('block','table-cell')
                                 and not(child::css:box[@type='block'])
                                 and not(@css:border-top|@css:border-bottom|@css:border-left)
                                 and @css:text-indent]
                         /@css:margin-left"/>
    
    <xsl:template priority="0.6"
                  mode="block-attr table-attr td-attr toc-entry-attr"
                  match="css:box[@type=('block','table-cell')
                                 and not(child::css:box[@type='block'])
                                 and @css:text-indent]
                         /@css:padding-left"/>
    
    <xsl:template priority="0.6"
                  mode="block-attr table-attr td-attr toc-entry-attr"
                  match="css:box[@type=('block','table-cell')
                                 and not(child::css:box[@type='block'])]
                         /@css:text-indent">
        <xsl:variable name="text-indent" as="xs:integer" select="xs:integer(number(.))"/>
        <xsl:variable name="padding-left" as="xs:integer" select="(parent::*/@css:padding-left/xs:integer(number(.)),0)[1]"/>
        <xsl:choose>
            <xsl:when test="parent::*/(@css:border-top|@css:border-bottom|@css:border-left)">
                <xsl:if test="parent::*[@name or not(preceding-sibling::css:box)]">
                    <xsl:attribute name="first-line-indent" select="format-number($padding-left + $text-indent, '0')"/>
                </xsl:if>
                <xsl:if test="$padding-left &gt; 0">
                    <xsl:attribute name="text-indent" select="format-number($padding-left, '0')"/>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="margin-left" as="xs:integer" select="(parent::*/@css:margin-left/xs:integer(number(.)),0)[1]"/>
                <xsl:if test="parent::*[@name or not(preceding-sibling::css:box)]">
                    <xsl:attribute name="first-line-indent" select="format-number($margin-left + $padding-left + $text-indent, '0')"/>
                </xsl:if>
                <xsl:if test="$margin-left + $padding-left &gt; 0">
                    <xsl:attribute name="text-indent" select="format-number($margin-left + $padding-left, '0')"/>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template mode="block-attr table-attr td-attr toc-entry-attr"
                  match="css:box[@type=('block','table-cell')]/@css:text-indent"/>
    
    <xsl:template mode="block-attr table-attr td-attr toc-entry-attr"
                  match="css:box[@type=('block','table-cell')]/@css:text-align">
        <xsl:attribute name="align" select="."/>
    </xsl:template>
    
    <xsl:template mode="block-attr"
                  match="css:box[@type='block']/@css:_obfl-vertical-position">
        <xsl:attribute name="vertical-position" select="."/>
    </xsl:template>
    
    <xsl:template mode="block-attr"
                  match="css:box[@type='block']/@css:_obfl-vertical-align">
        <xsl:attribute name="vertical-align" select="."/>
    </xsl:template>
    
    <xsl:template mode="block-attr"
                  match="css:box[@type='block']/@css:page-break-before[.='always']">
        <xsl:attribute name="break-before" select="'page'"/>
    </xsl:template>
    
    <!--
        FIXME: 'left' not supported, treating as 'always'
    -->
    <xsl:template mode="block-attr"
                  match="css:box[@type='block']/@css:page-break-before[.='left']">
        <xsl:message select="concat(local-name(),':',.,' not supported yet. Treating like &quot;always&quot;.')"/>
        <xsl:attribute name="break-before" select="'page'"/>
    </xsl:template>
    
    <xsl:template mode="block-attr"
                  match="css:box[@type='block']/@css:page-break-after[.='avoid']">
        <xsl:attribute name="keep-with-next" select="'1'"/>
        <!--
            keep-with-next="1" requires that keep="page". This gives it a slighly different meaning
            than "page-break-after: avoid", but it will do.
        -->
        <xsl:if test="not(parent::*/@css:page-break-inside[.='avoid'])">
            <xsl:attribute name="keep" select="'page'"/>
        </xsl:if>
    </xsl:template>
    
    <!--
        page-break-after:always becomes break-before="page" on next block unless there is no next block
    -->
    <xsl:template priority="1"
                  mode="block"
                  match="css:box[@type='block'][not(parent::css:box) and not(following-sibling::*)][@css:page-break-after[.='always']]">
        <xsl:next-match/>
        <block break-before="page"/>
    </xsl:template>
    <xsl:template mode="block-attr"
                  match="css:box[@type='block'][not(parent::css:box) and not(following-sibling::*)]/@css:page-break-after[.='always']"/>
    
    <!--
        'right' is handled by starting new sequences
    -->
    <xsl:template mode="block-attr"
                  match="css:box[@type='block'][not(parent::css:box) and not(preceding-sibling::*)]/@css:page-break-before[.='right']|
                         css:box[@type='block'][not(parent::css:box) and not(following-sibling::*)]/@css:page-break-after[.='right']"/>
    
    <!--
        FIXME: 'left' not supported
    -->
    <xsl:template mode="block-attr"
                  match="css:box[@type='block'][not(parent::css:box) and not(preceding-sibling::*)]/@css:page-break-before[.='left']|
                         css:box[@type='block'][not(parent::css:box) and not(following-sibling::*)]/@css:page-break-after[.='left']"/>
    
    <xsl:template mode="block-attr"
                  match="css:box[@type='block']/@css:page-break-inside[.='avoid']">
        <xsl:attribute name="keep" select="'page'"/>
    </xsl:template>
    
    <xsl:template mode="block-attr"
                  match="css:box[@type='block']/@css:orphans|
                         css:box[@type='block']/@css:widows">
        <xsl:attribute name="{local-name()}" select="."/>
    </xsl:template>
    
    <xsl:template mode="sequence-attr"
                  match="css:box[@type='block'][not(parent::css:box) and not(preceding-sibling::*)]/@css:volume-break-before[.='always']">
        <xsl:attribute name="break-before" select="'volume'"/>
    </xsl:template>
    
    <xsl:template mode="block-attr"
                  match="css:box[@type='block'][not(parent::css:box) and not(preceding-sibling::*)]/@css:volume-break-before[.='always']"/>
    
    <xsl:variable name="_OBFL_KEEP_FN_RE">-obfl-keep\(\s*([1-9])\s*\)</xsl:variable>
    <xsl:variable name="_OBFL_KEEP_FN_RE_priority" select="1"/>
    
    <xsl:template mode="block-attr"
                  match="css:box[@type='block']/@css:volume-break-inside">
        <xsl:analyze-string select="." regex="^{$_OBFL_KEEP_FN_RE}$">
            <xsl:matching-substring>
                <xsl:attribute name="volume-keep-priority" select="regex-group($_OBFL_KEEP_FN_RE_priority)"/>
            </xsl:matching-substring>
            <xsl:non-matching-substring>
                <xsl:next-match/>
            </xsl:non-matching-substring>
        </xsl:analyze-string>
    </xsl:template>
    
    <xsl:template mode="block-attr"
                  match="css:box[@type='block']/@css:_obfl-keep-with-previous-sheets|
                         css:box[@type='block']/@css:_obfl-keep-with-next-sheets">
        <xsl:attribute name="{replace(local-name(),'^_obfl-','')}" select="format-number(xs:integer(.), '0')"/>
    </xsl:template>
    
    <xsl:template mode="toc-entry-attr"
                  match="css:box[@type='block']/@css:_obfl-vertical-position|
                         css:box[@type='block']/@css:_obfl-vertical-align|
                         css:box[@type='block' and not(@css:_obfl-toc)]/@css:page-break-before|
                         css:box[@type='block']/@css:page-break-after|
                         css:box[@type='block']/@css:page-break-inside|
                         css:box[@type='block']/@css:orphans|
                         css:box[@type='block']/@css:widows">
        <xsl:message select="concat('Property ',replace(local-name(),'^_','-'),' not supported inside an element with display: -obfl-toc')"/>
    </xsl:template>
    
    <xsl:template priority="1.1"
                  mode="block-attr"
                  match="css:box[@type='table-cell']//css:box[@type='block']/@css:_obfl-vertical-position|
                         css:box[@type='table-cell']//css:box[@type='block']/@css:_obfl-vertical-align|
                         css:box[@type='table-cell']//css:box[@type='block']/@css:page-break-before|
                         css:box[@type='table-cell']//css:box[@type='block']/@css:page-break-after|
                         css:box[@type='table-cell']//css:box[@type='block']/@css:page-break-inside|
                         css:box[@type='table-cell']//css:box[@type='block']/@css:orphans|
                         css:box[@type='table-cell']//css:box[@type='block']/@css:widows">
        <xsl:message select="concat('Property ',replace(local-name(),'^_','-'),' not supported inside table cell elements')"/>
    </xsl:template>
    
    <xsl:template mode="block-attr table-attr td-attr toc-entry-attr"
                  match="css:box[@type=('block','table','table-cell')]/@css:border-left|
                         css:box[@type=('block','table','table-cell')]/@css:border-right">
        <xsl:choose>
            <xsl:when test=".='none'">
                <xsl:attribute name="{local-name()}-style" select="'none'"/>
            </xsl:when>
            <xsl:when test=".=('⠇','⠿','⠸')">
                <xsl:attribute name="{local-name()}-style" select="'solid'"/>
                <xsl:choose>
                    <xsl:when test=".='⠿'">
                        <xsl:attribute name="{local-name()}-width" select="'2'"/>
                    </xsl:when>
                    <xsl:when test=".='⠇'">
                        <xsl:attribute name="{local-name()}-align"
                                       select="if (local-name()='border-left') then 'outer' else 'inner'"/>
                    </xsl:when>
                    <xsl:when test=".='⠸'">
                        <xsl:attribute name="{local-name()}-align"
                                       select="if (local-name()='border-right') then 'outer' else 'inner'"/>
                    </xsl:when>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message select="concat(local-name(),':',.,' not supported yet')"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template mode="block-attr table-attr td-attr toc-entry-attr"
                  match="css:box[@type=('block','table','table-cell')]/@css:border-top|
                         css:box[@type=('block','table','table-cell')]/@css:border-bottom|
                         css:box[@type='block']/@css:_obfl-underline">
        <xsl:variable name="name" select="replace(local-name(),'^_obfl-','')"/>
        <xsl:choose>
            <xsl:when test=".='none'">
                <xsl:attribute name="{$name}-style" select="'none'"/>
            </xsl:when>
            <xsl:when test=".=('⠉','⠛','⠒','⠿','⠶','⠤')">
                <xsl:attribute name="{$name}-style" select="'solid'"/>
                <xsl:choose>
                    <xsl:when test=".=('⠛','⠶')">
                        <xsl:attribute name="{$name}-width" select="'2'"/>
                    </xsl:when>
                    <xsl:when test=".='⠿'">
                        <xsl:attribute name="{$name}-width" select="'3'"/>
                    </xsl:when>
                </xsl:choose>
                <xsl:choose>
                    <xsl:when test=".=('⠉','⠛')">
                        <xsl:attribute name="{$name}-align"
                                       select="if ($name='border-top') then 'outer' else 'inner'"/>
                    </xsl:when>
                    <xsl:when test=".=('⠶','⠤')">
                        <xsl:attribute name="{$name}-align"
                                       select="if ($name='border-top') then 'inner' else 'outer'"/>
                    </xsl:when>
                    <xsl:when test=".='⠒'">
                        <xsl:attribute name="{$name}-align"
                                       select="'center'"/>
                    </xsl:when>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message select="concat(replace(local-name(),'^_','-'),':',.,' not supported yet')"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template mode="block-attr assert-nil-attr"
                  match="@css:_obfl-use-when-collection-not-empty[.='normal']"/>
    
    <xsl:template mode="block-attr assert-nil-attr"
                  match="/css:_[@css:flow[not(.=$footnote-and-volume-range-flows)]]/*/@css:_obfl-use-when-collection-not-empty"/>
    
    <!-- ==================== -->
    <!-- More inline elements -->
    <!-- ==================== -->
    
    <!--
        string()
    -->
    <xsl:template mode="block span toc-entry"
                  match="css:string[@name]">
        <xsl:if test="@scope">
            <xsl:message select="concat('string(',@name,', ',@scope,'): second argument not supported')"/>
        </xsl:if>
        <xsl:if test="@css:white-space">
            <xsl:message select="concat('white-space:',@css:white-space,' could not be applied to ',
                                        (if (@target) then 'target-string' else 'string'),'(',@name,')')"/>
        </xsl:if>
        <xsl:variable name="target" as="xs:string?" select="if (@target) then @target else ()"/>
        <xsl:variable name="target" as="element()?" select="if ($target) then collection()//*[@css:id=$target][1] else ."/>
        <xsl:if test="$target">
            <xsl:apply-templates mode="css:eval-string" select="css:string(@name, $target)"/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template mode="css:eval-string"
                  match="css:string[@value]">
        <xsl:call-template name="text">
            <xsl:with-param name="text" select="string(@value)"/>
        </xsl:call-template>
    </xsl:template>
    
    <!--
        target-counter(page)
    -->
    <xsl:template mode="block toc-entry"
                  match="css:counter[@target][@name='page']">
        <xsl:param name="text-transform" as="xs:string" tunnel="yes"/>
        <xsl:param name="hyphens" as="xs:string" tunnel="yes"/>
        <xsl:param name="pending-text-transform" as="xs:string?" tunnel="yes"/>
        <xsl:param name="pending-hyphens" as="xs:string?" tunnel="yes"/>
        <!--
            Dotify always uses default mode for page-number (bug?), so effective value of
            text-transform is 'auto'
        -->
        <xsl:variable name="pending-text-transform" as="xs:string" select="($pending-text-transform,$text-transform)[1]"/>
        <xsl:variable name="style" as="xs:string*">
            <xsl:variable name="text-transform" as="xs:string*">
                <xsl:if test="matches(@style,re:exact($css:SYMBOLS_FN_RE))">
                    <xsl:sequence select="'-dotify-counter'"/>
                </xsl:if>
                <xsl:if test="not($pending-text-transform=('auto','none'))">
                    <xsl:sequence select="$pending-text-transform"/>
                </xsl:if>
            </xsl:variable>
            <xsl:if test="exists($text-transform)">
                <xsl:sequence select="concat('text-transform: ',string-join($text-transform,' '))"/>
            </xsl:if>
            <xsl:if test="exists($pending-hyphens) and not($pending-hyphens=$hyphens)">
                <xsl:sequence select="concat('hyphens: ',$pending-hyphens)"/>
            </xsl:if>
            <xsl:if test="@css:white-space">
                <xsl:sequence select="concat('white-space:',@css:white-space)"/>
            </xsl:if>
            <xsl:if test="matches(@style,re:exact($css:SYMBOLS_FN_RE))">
                <xsl:sequence select="concat('-dotify-counter-style: ',@style)"/>
            </xsl:if>
        </xsl:variable>
        <xsl:variable name="page-number" as="element()">
            <page-number ref-id="{@target}"
                         number-format="{if (@style=('roman', 'upper-roman', 'lower-roman', 'upper-alpha', 'lower-alpha'))
                                         then @style else 'default'}"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="exists($style)">
                <style name="{string-join($style,'; ')}">
                    <xsl:sequence select="$page-number"/>
                </style>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="$page-number"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!--
        set text-transform to "auto" on block with descendant -target-counter(page) with
        text-transform ≠ "auto" (because page-number can not be contained within span)
    -->
    <xsl:template priority="1"
                  mode="css:text-transform"
                  as="xs:string?"
                  match="css:box[@type='block']
                           [css:box[@type='inline']
                              //css:counter[@target][@name='page']
                                  /ancestor::css:box[@type='inline']
                                  /@css:text-transform
                                     [last()]
                                     [not(.='none')]]">
        <xsl:param name="specified-value" as="xs:boolean" select="false()"/>
        <xsl:choose>
            <xsl:when test="$specified-value">
                <xsl:next-match/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="'auto'"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template priority="1"
                  mode="block"
                  match="css:box[@type='block']
                           [css:box[@type='inline']
                              //css:counter[@target][@name='page']
                                  /ancestor::css:box[@type='inline']
                                  /@css:text-transform
                                     [last()]
                                     [not(.='none')]]">
        <xsl:param name="text-transform" as="xs:string" tunnel="yes"/>
        <xsl:variable name="specified-text-transform" as="xs:string?">
            <xsl:apply-templates mode="css:text-transform" select=".">
                <xsl:with-param name="specified-value" select="true()"/>
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:next-match>
            <!--
                for child css:box[@type='inline'] matcher
            -->
            <xsl:with-param name="pending-text-transform" tunnel="yes" select="($specified-text-transform,$text-transform)[1]"/>
        </xsl:next-match>
    </xsl:template>
    
    <!--
        leader()
    -->
    <xsl:template mode="block td toc-entry"
                  match="css:leader">
        <leader pattern="{@pattern}" position="100%" align="right"/>
    </xsl:template>
    
    <!--
        -obfl-evaluate
    -->
    <xsl:template priority="1"
                  mode="block"
                  match="css:custom-func[@name='-obfl-evaluate'][matches(@arg1,$css:STRING_RE) and not (@arg2)]">
        <xsl:param name="text-transform" as="xs:string" tunnel="yes"/>
        <xsl:param name="hyphens" as="xs:string" tunnel="yes"/>
        <xsl:param name="pending-text-transform" as="xs:string?" tunnel="yes"/>
        <xsl:param name="pending-hyphens" as="xs:string?" tunnel="yes"/>
        <xsl:variable name="style" as="xs:string*">
            <xsl:if test="exists($pending-text-transform) and not($pending-text-transform=($text-transform,'none'))">
                <xsl:sequence select="concat('text-transform: ',$pending-text-transform)"/>
            </xsl:if>
            <xsl:if test="exists($pending-hyphens) and not($pending-hyphens=$hyphens)">
                <xsl:sequence select="concat('hyphens: ',$pending-hyphens)"/>
            </xsl:if>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="exists($style)">
                <style name="{string-join($style,'; ')}">
                    <evaluate expression="{substring(@arg1,2,string-length(@arg1)-2)}"/>
                </style>
            </xsl:when>
            <xsl:otherwise>
                <evaluate expression="{substring(@arg1,2,string-length(@arg1)-2)}"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template mode="block span"
                  match="css:custom-func[@name='-obfl-evaluate'][@arg2]">
        <xsl:message>-obfl-evaluate() function requires exactly one string argument</xsl:message>
    </xsl:template>
    
    <!--
        set text-transform to "auto" on block with descendant -obfl-evaluate() with text-transform ≠
        "auto" (because evaluate can not be contained within span)
    -->
    <xsl:template priority="1"
                  mode="css:text-transform"
                  as="xs:string?"
                  match="css:box[@type='block']
                           [css:box[@type='inline']
                              //css:custom-func
                                  [@name='-obfl-evaluate']
                                  [matches(@arg1,$css:STRING_RE) and not (@arg2)]
                                  /ancestor::css:box[@type='inline']
                                  /@css:text-transform
                                     [last()]
                                     [not(.='none')]]">
        <xsl:param name="specified-value" as="xs:boolean" select="false()"/>
        <xsl:choose>
            <xsl:when test="$specified-value">
                <xsl:next-match/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="'auto'"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template priority="1"
                  mode="block"
                  match="css:box[@type='block']
                           [css:box[@type='inline']
                              //css:custom-func
                                  [@name='-obfl-evaluate']
                                  [matches(@arg1,$css:STRING_RE) and not (@arg2)]
                                  /ancestor::css:box[@type='inline']
                                  /@css:text-transform
                                     [last()]
                                     [not(.='none')]]">
        <xsl:param name="text-transform" as="xs:string" tunnel="yes"/>
        <xsl:variable name="specified-text-transform" as="xs:string?">
            <xsl:apply-templates mode="css:text-transform" select=".">
                <xsl:with-param name="specified-value" select="true()"/>
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:next-match>
            <!--
                for child css:box[@type='inline'] matcher
            -->
            <xsl:with-param name="pending-text-transform" tunnel="yes" select="($specified-text-transform,$text-transform)[1]"/>
        </xsl:next-match>
    </xsl:template>
    
    <!-- =============== -->
    <!-- IDs and anchors -->
    <!-- =============== -->
    
    <!--
        FIXME: don't add id attribute if block not referenced by any toc-entry or page-number
    -->
    <xsl:template mode="block-attr toc-entry-attr"
                  match="css:box[@type='block']/@css:id">
        <xsl:variable name="id" as="xs:string" select="."/>
        <xsl:if test="not(ancestor::*/@css:flow[not(.='normal')])">
            <xsl:attribute name="id" select="$id"/>
        </xsl:if>
    </xsl:template>
    
    <!--
        FIXME: id attribute not supported on a span
    -->
    <xsl:template mode="block-attr assert-nil-attr"
                  match="css:box[@type='inline']/@css:id">
        <xsl:variable name="id" as="xs:string" select="."/>
        <!--
            FIXME: what about css:string[@target] and css:box[@css:anchor] ?
        -->
        <xsl:if test="collection()//css:counter[@name='page'][@target=$id]">
            <xsl:message terminate="yes">target-counter(page) referencing inline elements not supported.</xsl:message>
        </xsl:if>
    </xsl:template>
    
    <xsl:template mode="css:anchor"
                  match="css:box/@css:id">
        <xsl:variable name="id" as="xs:string" select="."/>
        <xsl:if test="collection()/*[@css:flow=$footnote-and-volume-range-flows]/*/@css:anchor=$id">
            <anchor item="{$id}"/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template mode="block-attr span-attr toc-entry-attr assert-nil-attr"
                  match="css:box/@css:anchor"/>
    
    <!-- ======= -->
    <!-- Markers -->
    <!-- ======= -->
    
    <xsl:template mode="block span td toc-entry"
                  match="css:box/@css:string-set|
                         css:box/css:_/@css:string-set">
        <xsl:apply-templates mode="css:parse-string-set" select="css:parse-string-set(.)"/>
    </xsl:template>
    
    <xsl:template mode="css:parse-string-set"
                  match="css:string-set">
        <xsl:variable name="value" as="xs:string*">
            <xsl:apply-templates mode="css:eval-string-set" select="css:parse-content-list(@value, ())"/>
        </xsl:variable>
        <marker class="{@name}" value="{replace(string-join($value,''),'^\s+|\s+$','')}"/>
    </xsl:template>
    
    <xsl:template mode="css:eval-string-set"
                  as="xs:string"
                  match="css:string[@value]">
        <xsl:sequence select="string(@value)"/>
    </xsl:template>
    
    <xsl:template mode="block span toc-entry"
                  match="css:box/@css:_obfl-marker|
                         css:box/css:_/@css:_obfl-marker">
        <xsl:for-each select="tokenize(.,' ')">
            <marker class="indicator/{.}" value="x"/>
        </xsl:for-each>
    </xsl:template>
    
    <!-- ========================== -->
    <!-- -obfl- prefixed attributes -->
    <!-- ========================== -->
    
    <xsl:template mode="table-attr"
                  match="css:box[@type='table']/@css:_obfl-table-col-spacing|
                         css:box[@type='table']/@css:_obfl-table-row-spacing|
                         css:box[@type='table']/@css:_obfl-preferred-empty-space">
        <xsl:attribute name="{replace(local-name(),'^_obfl-','')}" select="format-number(xs:integer(.), '0')"/>
    </xsl:template>
    
    <!-- ==== -->
    <!-- Text -->
    <!-- ==== -->
    
    <xsl:template mode="block span td toc-entry"
                  match="text()">
        <xsl:call-template name="text">
            <xsl:with-param name="text" select="."/>
        </xsl:call-template>
    </xsl:template>
    
    <!--
        FIXME: only if within block and no sibling blocks
    -->
    <xsl:template name="text">
        <xsl:param name="text" as="xs:string" required="yes"/>
        <xsl:param name="text-transform" as="xs:string" tunnel="yes"/>
        <xsl:param name="hyphens" as="xs:string" tunnel="yes"/>
        <xsl:param name="word-spacing" as="xs:integer" tunnel="yes"/>
        <xsl:variable name="text" as="xs:string" select="translate($text,'&#x2800;',' ')"/>
        <xsl:variable name="text" as="xs:string">
            <xsl:choose>
                <!--
                    For 'hyphens:none' all SHY and ZWSP characters are removed from the text in advance.
                -->
                <xsl:when test="$hyphens='none'">
                    <xsl:sequence select="replace($text,'[&#x00AD;&#x200B;]','')"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="$text"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="text" as="xs:string">
            <xsl:choose>
                <xsl:when test="$word-spacing=1">
                    <xsl:sequence select="$text"/>
                </xsl:when>
                <!--
                    FIXME: style elements are currently processed in a step before line breaking (in
                    MarkerProcessorFactoryServiceImpl) so that they can't be used for passing
                    word-spacing to a "LineBreakingFromStyledText". Performing word spacing in XSLT
                    instead. Alternatively this could be implemented in a "FromStyledTextToBraille".
                -->
                <xsl:otherwise>
                    <xsl:variable name="words" as="xs:string*">
                        <xsl:analyze-string select="$text" regex="[&#x00AD;&#x200B;]*[ \t\n\r][&#x00AD;&#x200B; \t\n\r]*">
                            <xsl:matching-substring/>
                            <xsl:non-matching-substring>
                                <xsl:sequence select="."/>
                            </xsl:non-matching-substring>
                        </xsl:analyze-string>
                    </xsl:variable>
                    <xsl:variable name="spacing" as="xs:string" select="concat(string-join(for $x in 1 to $word-spacing return '&#x00A0;',''),'&#x200B;')"/>
                    <xsl:sequence select="string-join($words, $spacing)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <!--
                text-transform values 'none' and 'auto' are handled through the translate attribute.
            -->
            <xsl:when test="$text-transform=('none','auto')">
                <xsl:value-of select="$text"/>
            </xsl:when>
            <!--
                Other values are handled through the style element
            -->
            <xsl:otherwise>
                <style name="text-transform:{$text-transform}">
                    <xsl:value-of select="$text"/>
                </style>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template mode="block span td toc-entry"
                  match="css:white-space">
        <xsl:apply-templates mode="#current"/>
    </xsl:template>
    
    <xsl:template mode="block span td toc-entry"
                  match="css:white-space/text()">
        <xsl:analyze-string select="." regex="\n">
            <xsl:matching-substring>
                <br/>
            </xsl:matching-substring>
            <xsl:non-matching-substring>
                <xsl:analyze-string select="." regex="[ \t\n\r&#x2800;]+">
                    <xsl:matching-substring>
                        <xsl:value-of select="concat(replace(.,'.','&#x00A0;'),'&#x200B;')"/>
                    </xsl:matching-substring>
                    <xsl:non-matching-substring>
                        <xsl:value-of select="."/>
                    </xsl:non-matching-substring>
                </xsl:analyze-string>
            </xsl:non-matching-substring>
        </xsl:analyze-string>
    </xsl:template>
    
    <!-- === -->
    <!-- ... -->
    <!-- === -->
    
    <xsl:template priority="0.1"
                  mode="block-attr toc-entry-attr"
                  match="css:box[@type='block']/@css:_obfl-toc">
        <xsl:message>display: -obfl-toc only allowed on elements that are flowed into @begin or @end area.</xsl:message>
    </xsl:template>
    
    <xsl:template mode="block-attr span-attr"
                  match="@css:_obfl-on-toc-start|
                         @css:_obfl-on-volume-start|
                         @css:_obfl-on-volume-end|
                         @css:_obfl-on-toc-end">
        <xsl:message select="concat('::',replace(local-name(),'^_','-'),' pseudo-element only allowed on elements with display: -obfl-toc.')"/>
    </xsl:template>
    
    <xsl:template mode="#default sequence item table-of-contents block span table tr td toc-entry assert-nil
                        sequence-attr item-attr table-of-contents-attr block-attr span-attr
                        table-attr tr-attr td-attr toc-entry-attr assert-nil-attr"
                  match="@*|*" >
        <xsl:call-template name="coding-error"/>
    </xsl:template>
    
    <xsl:template name="coding-error">
        <xsl:message terminate="yes">Coding error: unexpected <xsl:value-of select="pxi:get-path(.)"/> (mode was <xsl:apply-templates select="$pxi:print-mode" mode="#current"/>)</xsl:message>
    </xsl:template>
    
    <xsl:function name="pxi:get-path" as="xs:string">
        <xsl:param name="x"/> <!-- element()|attribute() -->
        <xsl:variable name="name" as="xs:string"
                      select="if ($x/self::css:box[@name]) then $x/@name else name($x)"/>
        <xsl:sequence select="if ($x/self::attribute())
                              then concat(pxi:get-path($x/parent::*),'/@',$name)
                              else if ($x/parent::*)
                              then concat(pxi:get-path($x/parent::*),'/',$name,'[',(count($x/preceding-sibling::*)+1),']')
                              else concat('/',$name)"/>
    </xsl:function>
    
    <xsl:variable name="pxi:print-mode"><pxi:print-mode/></xsl:variable>
    <xsl:template match="pxi:print-mode">#default</xsl:template>
    <xsl:template match="pxi:print-mode" mode="sequence">sequence</xsl:template>
    <xsl:template match="pxi:print-mode" mode="item">item</xsl:template>
    <xsl:template match="pxi:print-mode" mode="table-of-contents">table-of-contents</xsl:template>
    <xsl:template match="pxi:print-mode" mode="block">block</xsl:template>
    <xsl:template match="pxi:print-mode" mode="span">span</xsl:template>
    <xsl:template match="pxi:print-mode" mode="table">table</xsl:template>
    <xsl:template match="pxi:print-mode" mode="tr">tr</xsl:template>
    <xsl:template match="pxi:print-mode" mode="td">td</xsl:template>
    <xsl:template match="pxi:print-mode" mode="toc-entry">toc-entry</xsl:template>
    <xsl:template match="pxi:print-mode" mode="assert-nil">assert-nil</xsl:template>
    <xsl:template match="pxi:print-mode" mode="sequence-attr">sequence-attr</xsl:template>
    <xsl:template match="pxi:print-mode" mode="item-attr">item-attr</xsl:template>
    <xsl:template match="pxi:print-mode" mode="table-of-contents-attr">table-of-contents-attr</xsl:template>
    <xsl:template match="pxi:print-mode" mode="block-attr">block-attr</xsl:template>
    <xsl:template match="pxi:print-mode" mode="span-attr">span-attr</xsl:template>
    <xsl:template match="pxi:print-mode" mode="table-attr">table-attr</xsl:template>
    <xsl:template match="pxi:print-mode" mode="tr-attr">tr-attr</xsl:template>
    <xsl:template match="pxi:print-mode" mode="td-attr">td-attr</xsl:template>
    <xsl:template match="pxi:print-mode" mode="toc-entry-attr">toc-entry-attr</xsl:template>
    <xsl:template match="pxi:print-mode" mode="assert-nil-attr">assert-nil-attr</xsl:template>
    
    <!-- =========== -->
    <!-- Volume area -->
    <!-- =========== -->
    
    <xsl:template mode="css:eval-volume-area-content-list"
                  match="css:string[@value]">
        <css:_>
            <css:box type="inline">
                <xsl:value-of select="@value"/>
            </css:box>
        </css:_>
    </xsl:template>
    
    <!--
        default scope within volume area is 'document'
    -->
    <xsl:template mode="css:eval-volume-area-content-list"
                  match="css:flow[@from and (not(@scope) or @scope='document')]">
        <xsl:variable name="flow" as="xs:string" select="@from"/>
        <xsl:sequence select="collection()/*[@css:flow=$flow]"/>
    </xsl:template>
    
    <xsl:template mode="css:eval-volume-area-content-list"
                  match="css:flow[@from=$footnote-and-volume-range-flows and @scope='volume']">
        <list-of-references collection="{@from}" range="volume"/>
    </xsl:template>
    
    <xsl:template mode="css:eval-volume-area-content-list"
                  match="css:attr|
                         css:content[@target]|
                         css:content[not(@target)]|
                         css:string[@name][not(@target)]|
                         css:counter[not(@target)]|
                         css:text[@target]|
                         css:string[@name][@target]|
                         css:counter[@target]|
                         css:leader">
        <xsl:message select="concat(
                               if (@target) then 'target-' else '',
                               local-name(),
                               '() function not supported in volume area')"/>
    </xsl:template>
    
    <xsl:template mode="css:eval-volume-area-content-list"
                  match="css:custom-func[@name='-obfl-evaluate']">
        <xsl:message>-obfl-evaluate() function not supported in volume area</xsl:message>
    </xsl:template>
    
    <xsl:template mode="css:eval-volume-area-content-list"
                  match="*">
        <xsl:message terminate="yes">Coding error</xsl:message>
    </xsl:template>
    
    <!-- ======================== -->
    <!-- OBFL evaluation language -->
    <!-- ======================== -->
    
    <xsl:function name="obfl:not" as="xs:string">
        <xsl:param name="sexpr" as="xs:string"/>
        <xsl:sequence select="if ($sexpr='nil') then 't'
                              else if ($sexpr='t') then 'nil'
                              else concat('(! ',$sexpr,')')"/>
    </xsl:function>
    
    <xsl:function name="obfl:and" as="xs:string">
        <xsl:param name="sexprs" as="xs:string*"/>
        <xsl:variable name="sexprs2" as="xs:string*" select="distinct-values($sexprs)[not(.='t')]"/>
        <xsl:sequence select="if (not(exists($sexprs2))) then 't'
                              else if ('nil'=$sexprs2) then 'nil'
                              else if (count($sexprs2)=1) then $sexprs2[1]
                              else concat('(&amp; ',string-join($sexprs2,' '),')')"/>
    </xsl:function>
    
    <xsl:function name="obfl:or" as="xs:string">
        <xsl:param name="sexprs" as="xs:string*"/>
        <xsl:variable name="sexprs2" as="xs:string*" select="distinct-values($sexprs)[not(.='nil')]"/>
        <xsl:sequence select="if (not(exists($sexprs2))) then 'nil'
                              else if ('t'=$sexprs2) then 't'
                              else if (count($sexprs2)=1) then $sexprs2[1]
                              else concat('(| ',string-join($sexprs2,' '),')')"/>
    </xsl:function>

</xsl:stylesheet>
