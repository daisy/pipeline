<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:f="http://www.daisy.org/pipeline/modules/nordic-epub3-dtbook-migrator/epub3-to-dtbook.xsl" xmlns:pf="http://www.daisy.org/ns/pipeline/functions" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns="http://www.daisy.org/z3986/2005/dtbook/" xpath-default-namespace="http://www.daisy.org/z3986/2005/dtbook/" exclude-result-prefixes="#all" xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:html="http://www.w3.org/1999/xhtml" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:math="http://www.w3.org/1998/Math/MathML">

    <xsl:import href="http://www.daisy.org/pipeline/modules/common-utils/numeral-conversion.xsl"/>
    <!--<xsl:import href="../../../../test/xspec/mock/numeral-conversion.xsl"/>-->
    <xsl:import href="epub3-vocab.xsl"/>

    <xsl:param name="allow-links" select="false()"/>

    <xsl:output indent="yes" exclude-result-prefixes="#all" doctype-public="-//NISO//DTD dtbook 2005-3//EN" doctype-system="http://www.daisy.org/z3986/2005/dtbook-2005-3.dtd"/>

    <!--
    <xsl:variable name="special-classes"
        select="('part','cover','colophon','nonstandardpagination','jacketcopy','frontcover','rearcover','leftflap','rightflap','precedingemptyline','precedingseparator','indented','asciimath','byline','dateline','address','definition','keyboard','initialism','truncation','cite','bdo','quote','exercisenumber','exercisepart','answer','answer_1','box')"/>
    -->

    <xsl:template match="text()|comment()">
        <xsl:copy-of select="." exclude-result-prefixes="#all"/>
    </xsl:template>

    <xsl:template match="*">
        <xsl:comment select="concat('No template for element: &lt;',name(),string-join(for $a in (@*) return concat(' ',$a/name(),'=&quot;',$a,'&quot;'),''),'&gt;')"/>
    </xsl:template>

    <xsl:template match="html:style"/>

    <xsl:template match="html:script"/>

    <xsl:template name="f:coreattrs">
        <xsl:param name="except" tunnel="yes"/>

        <xsl:copy-of select="(@id|@title|@xml:space)[not(name()=$except)]" exclude-result-prefixes="#all"/>
        <xsl:call-template name="f:classes-and-types"/>
    </xsl:template>

    <xsl:template name="f:i18n">
        <xsl:param name="except" tunnel="yes"/>

        <xsl:copy-of select="(@dir)[not(name()=$except)]" exclude-result-prefixes="#all"/>
        <xsl:if test="(@xml:lang|@lang) and not(('xml:lang','lang')=$except)">
            <xsl:attribute name="xml:lang" select="(@xml:lang|@lang)[1]"/>
        </xsl:if>
    </xsl:template>

    <xsl:template name="f:classes-and-types">
        <xsl:param name="classes" select="()" tunnel="yes"/>
        <xsl:param name="except" tunnel="yes" select="()"/>
        <xsl:param name="except-classes" tunnel="yes" select="()"/>

        <xsl:if test="not($except-classes='*')">

            <xsl:variable name="old-classes" select="f:classes(.)"/>

            <xsl:variable name="showin" select="replace($old-classes[matches(.,'^showin-...$')][1],'showin-','')"/>
            <xsl:if test="$showin and not('_showin'=$except)">
                <xsl:attribute name="showin" select="$showin"/>
            </xsl:if>
            
            <!-- remove classes used for the conversion -->
            <xsl:variable name="old-classes" select="$old-classes[not(starts-with(.,'showin-')) and not(.=('list-style-type-none'))]"/>

            <xsl:if test="not('_class'=$except)">
                <xsl:variable name="epub-type-classes">
                    <xsl:for-each select="f:types(.)[not(matches(.,'(^|:)(front|body|back)matter'))]">
                        <xsl:choose>
                            <xsl:when test=".='cover'">
                                <xsl:sequence select="'jacketcopy'"/>
                            </xsl:when>
                            <xsl:when test=".='z3998:halftitle-page'">
                                <xsl:sequence select="'halftitlepage'"/>
                            </xsl:when>
                            <xsl:when test=".='z3998:title-page'">
                                <xsl:sequence select="'titlepage'"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:sequence select="tokenize(.,':')[last()]"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                </xsl:variable>

                <xsl:variable name="class-string"
                    select="string-join(distinct-values(($classes, if (preceding-sibling::*[1] intersect preceding-sibling::html:hr[1]) then (if (preceding-sibling::html:hr[1]/tokenize(@class,'\s')='separator') then 'precedingseparator' else 'precedingemptyline') else (), $old-classes, $epub-type-classes)[not(.='') and not(.=$except-classes)]),' ')"/>
                <xsl:if test="not($class-string='')">
                    <xsl:attribute name="class" select="$class-string"/>
                </xsl:if>
            </xsl:if>

        </xsl:if>
    </xsl:template>

    <xsl:template name="f:attrs">
        <xsl:call-template name="f:coreattrs"/>
        <xsl:call-template name="f:i18n"/>
    </xsl:template>

    <xsl:template name="f:attrsrqd">
        <xsl:param name="except" tunnel="yes"/>

        <xsl:copy-of select="(@id|@title|@xml:space)[not(name()=$except)]" exclude-result-prefixes="#all"/>
        <xsl:call-template name="f:classes-and-types"/>
        <xsl:call-template name="f:i18n"/>
    </xsl:template>

    <xsl:template match="html:html">
        <dtbook version="2005-3">
            <xsl:call-template name="f:attlist.dtbook"/>
            <xsl:apply-templates select="node()">
                <xsl:with-param name="all-ids" select=".//@id" tunnel="yes"/>
            </xsl:apply-templates>
        </dtbook>
    </xsl:template>

    <xsl:template name="f:attlist.dtbook">
        <xsl:call-template name="f:i18n"/>
    </xsl:template>

    <xsl:template match="html:head">
        <head>
            <xsl:call-template name="f:attlist.head"/>
            <meta name="dtb:uid" content="{(html:meta[lower-case(@name)=('dtb:uid','dc:identifier')])[1]/@content}"/>
            <xsl:apply-templates select="node()"/>
        </head>
    </xsl:template>

    <xsl:template match="html:title">
        <meta name="dc:Title" content="{normalize-space(.)}">
            <xsl:call-template name="f:i18n"/>
        </meta>
    </xsl:template>

    <xsl:template name="f:attlist.head">
        <xsl:call-template name="f:i18n"/>
        <xsl:if test="html:link[@rel='profile' and @href]">
            <xsl:attribute name="profile" select="(html:link[@rel='profile'][1])/@href"/>
        </xsl:if>
    </xsl:template>

    <!-- link is disallowed in nordic DTBook -->
    <xsl:template match="html:link">
        <!--<link>
            <xsl:call-template name="f:attlist.link"/>
        </link>-->
    </xsl:template>

    <xsl:template name="f:attlist.link">
        <xsl:call-template name="f:attrs"/>
        <xsl:copy-of select="@href|@hreflang|@type|@rel|@media" exclude-result-prefixes="#all"/>
        <!-- @sizes are dropped -->
    </xsl:template>

    <xsl:template match="html:meta">
        <xsl:if test="not(@http-equiv='Content-Type') and not(@charset) and not(@name='viewport')">
            <xsl:message
                select="concat('removed meta element because it did not contain a name attribute, a content attribute, or for some other reason (',string-join(for $a in (@*) return concat($a/name(),'=&quot;',$a,'&quot;'),' '),')')"
            />
        </xsl:if>
    </xsl:template>

    <xsl:template match="html:meta[@name and @content and not(lower-case(@name)=('viewport','dc:title'))]">
        <meta>
            <xsl:call-template name="f:attlist.meta"/>
        </meta>
    </xsl:template>

    <xsl:template name="f:attlist.meta">
        <xsl:call-template name="f:i18n"/>
        <xsl:copy-of select="@http-equiv" exclude-result-prefixes="#all"/>
        <xsl:choose>
            <xsl:when test="@name='nordic:guidelines'">
                <xsl:attribute name="name" select="'track:Guidelines'"/>
                <xsl:attribute name="content" select="'2015-1'"/>
            </xsl:when>
            <xsl:when test="@name='nordic:supplier'">
                <xsl:attribute name="name" select="'track:Supplier'"/>
                <xsl:attribute name="content" select="@content"/>
            </xsl:when>
            <xsl:when test="lower-case(@name)='dc:format'">
                <xsl:attribute name="name" select="'dc:Format'"/>
                <xsl:attribute name="content" select="'DTBook'"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:attribute name="name" select="if (starts-with(@name,'dc:')) then concat('dc:',upper-case(substring(@name,4,1)),lower-case(substring(@name,5))) else @name"/>
                <xsl:attribute name="content" select="@content"/>
            </xsl:otherwise>
        </xsl:choose>
        <!-- @charset is dropped -->
    </xsl:template>

    <xsl:template match="html:body">
        <book>
            <xsl:if test="(html:section | html:article)[f:types(.)=('cover','frontmatter')] or *[not(self::html:section)]">
                <xsl:call-template name="f:frontmatter"/>
            </xsl:if>
            <xsl:if test="(html:section | html:article)[f:types(.)=('bodymatter') or not(f:types(.)=('cover','frontmatter','bodymatter','backmatter'))]">
                <xsl:call-template name="f:bodymatter"/>
            </xsl:if>
            <xsl:if test="(html:section | html:article)[f:types(.)=('backmatter')]">
                <xsl:call-template name="f:rearmatter"/>
            </xsl:if>
            <xsl:apply-templates select="*[last()]/following-sibling::node()"/>
        </book>
    </xsl:template>

    <xsl:template name="f:frontmatter">
        <xsl:call-template name="f:copy-preceding-comments"/>
        <frontmatter>
            <xsl:for-each select="html:header">
                <xsl:call-template name="f:copy-preceding-comments"/>
                <xsl:apply-templates select="node()"/>
            </xsl:for-each>
            <xsl:apply-templates select="(html:section | html:article)[f:types(.)=('cover','frontmatter')]"/>
        </frontmatter>
    </xsl:template>

    <xsl:template name="f:bodymatter">
        <bodymatter>
            <xsl:apply-templates select="(html:section | html:article)[not(f:types(.)=('cover','frontmatter','backmatter'))]"/>
        </bodymatter>
    </xsl:template>

    <xsl:template name="f:rearmatter">
        <rearmatter>
            <xsl:apply-templates select="(html:section | html:article)[f:types(.)=('backmatter')]"/>
        </rearmatter>
    </xsl:template>

    <xsl:template match="html:section | html:article">
        <xsl:call-template name="f:copy-preceding-comments"/>
        <xsl:variable name="level" select="f:level(.)"/>
        <xsl:element name="level{f:level(.)}">
            <xsl:call-template name="f:attlist.level">
                <xsl:with-param name="classes" select="if (self::html:article) then 'article' else ()" tunnel="yes"/>
                <!--<xsl:with-param name="level-classes"
                    select="if ($level &gt; 1) then () else (if (f:types(.)='cover') then 'jacketcopy' else (), for $class in (tokenize(@class,'\s')) return if ($class = ('part','jacketcopy','colophon','nonstandardpagination')) then $class else ())"
                />-->
            </xsl:call-template>

            <xsl:variable name="headline" select="(html:*[matches(local-name(),'^h\d$')])[1]"/>

            <xsl:choose>
                <xsl:when test="not($headline/preceding-sibling::*[1][f:types(.)='pagebreak']) and $headline/following-sibling::*[1][f:types(.)='pagebreak']">
                    <!-- [tpb126] pagenum must not occur directly after hx unless the hx is preceded by a pagenum -->
                    <xsl:variable name="initial-pagebreak" select="$headline/following-sibling::*[1][f:types(.)='pagebreak']"/>
                    <xsl:apply-templates select="$initial-pagebreak"/>
                    <xsl:apply-templates select="$headline"/>
                    <xsl:apply-templates select="node()[not(. intersect $initial-pagebreak) and not(. intersect $headline)]"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="node()"/>
                </xsl:otherwise>
            </xsl:choose>

        </xsl:element>
    </xsl:template>

    <xsl:template name="f:attlist.level">
        <!--        <xsl:param name="level-classes"/>-->
        <xsl:call-template name="f:attrs">
            <!--            <xsl:with-param name="except-classes" select="'*'" tunnel="yes"/>-->
        </xsl:call-template>
        <!--<xsl:if test="count($level-classes) &gt; 0">
            <xsl:attribute name="class" select="string-join($level-classes,' ')"/>
        </xsl:if>-->
    </xsl:template>

    <xsl:template match="html:br">
        <br>
            <xsl:call-template name="f:attlist.br"/>
        </br>
    </xsl:template>

    <xsl:template name="f:attlist.br">
        <xsl:call-template name="f:coreattrs"/>
    </xsl:template>

    <xsl:template match="html:p[f:classes(.)='line']">
        <line>
            <xsl:call-template name="f:attlist.line"/>
            <xsl:apply-templates select="node()"/>
        </line>
    </xsl:template>

    <xsl:template name="f:attlist.line">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="except-classes" select="'line'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:span[f:classes(.)='linenum']">
        <linenum>
            <xsl:call-template name="f:attlist.linenum"/>
            <xsl:apply-templates select="node()"/>
        </linenum>
    </xsl:template>

    <xsl:template name="f:attlist.linenum">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="except-classes" select="'linenum'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <!-- <address> is not allowed in nordic DTBook. Replacing with p. -->
    <xsl:template match="html:address">
        <xsl:message select="'&lt;address&gt; is not allowed in nordic DTBook. Replacing with p and a &quot;address&quot; class.'"/>
        <p>
            <xsl:call-template name="f:attlist.address"/>
            <xsl:apply-templates select="node()"/>
        </p>
    </xsl:template>

    <!-- <address> is not allowed in nordic DTBook. Replacing with p and a "address" class. -->
    <xsl:template name="f:attlist.address">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="classes" select="'address'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:div">
        <div>
            <xsl:call-template name="f:attlist.div"/>
            <xsl:apply-templates select="node()"/>
        </div>
    </xsl:template>

    <xsl:template name="f:attlist.div">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <xsl:template match="html:*[f:classes(.)='title' and not(parent::html:header[parent::html:body]) and ancestor::html:*/f:types(.)=('z3998:poem','z3998:verse')]">
        <title>
            <xsl:call-template name="f:attlist.title"/>
            <xsl:apply-templates select="node()"/>
        </title>
    </xsl:template>

    <xsl:template name="f:attlist.title">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="except-classes" select="'title'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:*[f:types(.)='z3998:author' and not(parent::html:header[parent::html:body]) and ancestor::html:*/f:types(.)=('z3998:poem','z3998:verse')]">
        <author>
            <xsl:call-template name="f:attlist.author"/>
            <xsl:apply-templates select="node()"/>
        </author>
    </xsl:template>

    <xsl:template name="f:attlist.author">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="except-classes" select="'author'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:aside[f:types(.)='z3998:production'] | html:section[f:classes(.)=('frontcover','rearcover','leftflap','rightflap') and parent::*/f:types(.)='cover']">
        <xsl:choose>
            <xsl:when test="ancestor::html:section[f:classes(.)=('frontcover','rearcover','leftflap','rightflap') and parent::*/f:types(.)='cover']">
                <div>
                    <xsl:call-template name="f:attlist.div">
                        <xsl:with-param name="classes" select="if (f:classes(.) = ('render-required','render-optional')) then () else ('render-optional')" tunnel="yes"/>
                    </xsl:call-template>
                    <xsl:apply-templates select="node()"/>
                </div>
            </xsl:when>
            <xsl:otherwise>
                <prodnote>
                    <xsl:call-template name="f:attlist.prodnote"/>
                    <xsl:apply-templates select="node()"/>
                </prodnote>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="f:attlist.prodnote">
        <xsl:param name="all-ids" tunnel="yes" select=".//@id"/>
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="except-classes" select="('production','render-required','render-optional')" tunnel="yes"/>
        </xsl:call-template>
        <xsl:choose>
            <xsl:when test="f:classes(.)='render-required'">
                <xsl:attribute name="render" select="'required'"/>
            </xsl:when>
            <xsl:when test="f:classes(.)='render-optional'">
                <xsl:attribute name="render" select="'optional'"/>
            </xsl:when>
            <xsl:otherwise>
                <!-- let's make "optional" the default -->
                <xsl:attribute name="render" select="'optional'"/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="@id">
            <xsl:variable name="id" select="@id"/>
            <xsl:variable name="img" select="//html:img[replace(@longdesc,'^#','')=$id]"/>
            <xsl:if test="$img">
                <xsl:attribute name="imgref" select="string-join($img/((@id,f:generate-pretty-id(.,$all-ids))[1]),' ')"/>
            </xsl:if>
        </xsl:if>
    </xsl:template>

    <xsl:template match="html:aside[f:types(.)='sidebar'] | html:figure[f:types(.)='sidebar']">
        <sidebar>
            <xsl:call-template name="f:attlist.sidebar"/>
            <xsl:apply-templates select="node()"/>
        </sidebar>
    </xsl:template>

    <xsl:template name="f:attlist.sidebar">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="except-classes" select="'sidebar'" tunnel="yes"/>
        </xsl:call-template>
        <xsl:choose>
            <xsl:when test="self::html:figure">
                <xsl:attribute name="render" select="'required'"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:attribute name="render" select="'optional'"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="html:aside[f:types(.)='note'] | html:li[f:types(.)=('rearnote','footnote')]">
        <note>
            <xsl:call-template name="f:attlist.note"/>
            <xsl:choose>
                <xsl:when test="(text()[normalize-space()] | *)/f:is-inline(.) = true()">
                    <p>
                        <xsl:apply-templates select="node()"/>
                    </p>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="node()"/>
                </xsl:otherwise>
            </xsl:choose>
        </note>
    </xsl:template>

    <xsl:template name="f:attlist.note">
        <xsl:call-template name="f:attrsrqd">
            <xsl:with-param name="except-classes" select="'*'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <!-- <annotation> is not allowed in nordic DTBook. Replacing with p. -->
    <xsl:template match="html:aside[f:types(.)='annotation']">
        <xsl:message select="'&lt;annotation&gt; is not allowed in nordic DTBook. Replacing with p and a &quot;annotation&quot; class.'"/>
        <p>
            <xsl:call-template name="f:attlist.annotation"/>
            <xsl:apply-templates select="node()"/>
        </p>
    </xsl:template>

    <!-- <annotation> is not allowed in nordic DTBook. Replacing with p and a "annotation" class. -->
    <xsl:template name="f:attlist.annotation">
        <xsl:call-template name="f:attrsrqd">
            <xsl:with-param name="classes" select="'annotation'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <!-- <epigraph> is not allowed in nordic DTBook. Using p instead. -->
    <xsl:template match="html:*[f:types(.)='epigraph']">
        <xsl:message select="'&lt;epigraph&gt; is not allowed in nordic DTBook. Prefer using p with a epigraph class when possible.'"/>
        <xsl:choose>
            <xsl:when test="exists(self::html:aside | self::html:section) and exists(html:h1 | html:h2 | html:h3 | html:h4 | html:h5 | html:h6 | html:aside | html:section)">
                <xsl:element name="level{f:level(.)}">
                    <xsl:call-template name="f:attlist.epigraph"/>
                    <xsl:apply-templates select="node()"/>
                </xsl:element>
            </xsl:when>
            <xsl:when test="exists(*[not(f:is-inline(.))])">
                <epigraph>
                    <xsl:call-template name="f:attlist.epigraph"/>
                    <xsl:apply-templates select="node()"/>
                </epigraph>
            </xsl:when>
            <xsl:otherwise>
                <p>
                    <xsl:call-template name="f:attlist.epigraph"/>
                    <xsl:apply-templates select="node()"/>
                </p>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- <epigraph> is not allowed in nordic DTBook. Using p instead with a epigraph class. -->
    <xsl:template name="f:attlist.epigraph">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="classes" select="'epigraph'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <!-- <byline> is not allowed in nordic DTBook. Using span instead. -->
    <xsl:template match="html:span[f:classes(.)='byline']">
        <xsl:message select="'&lt;byline&gt; is not allowed in nordic DTBook. Using span instead with a byline class.'"/>
        <span>
            <xsl:call-template name="f:attlist.byline"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <!-- <byline> is not allowed in nordic DTBook. Using span instead with a byline class. -->
    <xsl:template name="f:attlist.byline">
        <xsl:call-template name="f:attrs">
            <!--            <xsl:with-param name="except-classes" select="'byline'" tunnel="yes"/>-->
        </xsl:call-template>
    </xsl:template>

    <!-- <dateline> is not allowed in nordic DTBook. Using span instead. -->
    <xsl:template match="html:span[f:classes(.)='dateline']">
        <xsl:message select="'&lt;dateline&gt; is not allowed in nordic DTBook. Using span instead with a dateline class.'"/>
        <span>
            <xsl:call-template name="f:attlist.dateline"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <!-- <dateline> is not allowed in nordic DTBook. Using span instead with a dateline class. -->
    <xsl:template name="f:attlist.dateline">
        <xsl:call-template name="f:attrs">
            <!--            <xsl:with-param name="except-classes" select="'dateline'" tunnel="yes"/>-->
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:*[f:classes(.)='linegroup']">
        <linegroup>
            <xsl:call-template name="f:attlist.linegroup"/>
            <xsl:apply-templates select="node()"/>
        </linegroup>
    </xsl:template>

    <xsl:template name="f:attlist.linegroup">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="except-classes" select="'linegroup'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:*[f:types(.)='z3998:poem'] | html:*[f:types(.)='z3998:verse' and not(ancestor::html:*/f:types(.)='z3998:poem')]">
        <poem>
            <xsl:call-template name="f:attlist.poem"/>
            <xsl:apply-templates select="node()"/>
        </poem>
    </xsl:template>

    <xsl:template name="f:attlist.poem">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="except-classes" select="('poem','verse')" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <!-- <a> is not allowed in nordic DTBook. Replacing with span. -->
    <xsl:template match="html:a">
        <xsl:choose>
            <xsl:when test="html:span[f:classes(.) = 'lic']">
                <xsl:apply-templates select="node()"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="f:a"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="f:a">
        <xsl:param name="children" select="node()"/>
        <xsl:choose>
            <xsl:when test="$allow-links">
                <a>
                    <xsl:call-template name="f:attlist.a"/>
                    <xsl:apply-templates select="$children"/>
                </a>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message select="'&lt;a&gt; is not allowed in nordic DTBook. Replacing with span and a &quot;a&quot; class.'"/>
                <span>
                    <xsl:call-template name="f:attlist.a"/>
                    <xsl:apply-templates select="$children"/>
                </span>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- <a> is not allowed in nordic DTBook. Replacing with span and a "a" class. -->
    <xsl:template name="f:attlist.a">

        <xsl:call-template name="f:attrs">
            <!--<xsl:with-param name="classes" select="'a'" tunnel="yes"/>-->

            <!--
            <!-\- Preserve @target as class attribute. Assumes that only characters that are valid for class names are used. -\->
            <xsl:with-param name="classes" select="('a', if (@target) then concat('target-',replace(@target,'_','-')) else ())" tunnel="yes"/>
            <xsl:with-param name="except-classes" select="for $rev in (f:classes(.)[matches(.,'^rev-')]) return $rev" tunnel="yes"/>
            -->
        </xsl:call-template>

        <xsl:if test="$allow-links">
            <xsl:copy-of select="@type|@href|@hreflang|@rel|@accesskey|@tabindex" exclude-result-prefixes="#all"/>
            <!-- @download and @media is dropped - they don't have a good equivalent in DTBook -->

            <xsl:choose>
                <xsl:when test="@target='_blank' or matches(@href,'^(\w+:|/)')">
                    <xsl:attribute name="external" select="'true'"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="external" select="'false'"/>
                </xsl:otherwise>
            </xsl:choose>

            <!--<xsl:if test="f:classes(.)[matches(.,'^rev-')]">
                <xsl:attribute name="rev" select="replace((f:classes(.)[matches(.,'^rev-')])[1],'^rev-','')"/>
            </xsl:if>-->
        </xsl:if>

    </xsl:template>

    <xsl:template match="html:em">
        <em>
            <xsl:call-template name="f:attlist.em"/>
            <xsl:apply-templates select="node()"/>
        </em>
    </xsl:template>

    <xsl:template name="f:attlist.em">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <xsl:template match="html:strong">
        <strong>
            <xsl:call-template name="f:attlist.strong"/>
            <xsl:apply-templates select="node()"/>
        </strong>
    </xsl:template>

    <xsl:template name="f:attlist.strong">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <!-- <dfn> is not allowed in nordic DTBook. Replacing with span. -->
    <xsl:template match="html:dfn">
        <xsl:message select="'&lt;dfn&gt; is not allowed in nordic DTBook. Replacing with span and a &quot;definition&quot; class.'"/>
        <span>
            <xsl:call-template name="f:attlist.dfn"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <!-- <dfn> is not allowed in nordic DTBook. Replacing with span and a "definition" class. -->
    <xsl:template name="f:attlist.dfn">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="classes" select="'definition'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <!-- <kbd> is not allowed in nordic DTBook. Replacing with code. -->
    <xsl:template match="html:kbd">
        <xsl:message select="'&lt;kbd&gt; is not allowed in Nordic DTBook. Replacing with &lt;code&gt; and a &quot;keyboard&quot; class.'"/>
        <code>
            <xsl:call-template name="f:attlist.kbd"/>
            <xsl:apply-templates select="node()"/>
        </code>
    </xsl:template>

    <!-- <kbd> is not allowed in nordic DTBook. Replacing with code and a "keyboard" class. -->
    <xsl:template name="f:attlist.kbd">
        <xsl:attribute name="xml:space" select="'preserve'"/>
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="classes" select="'keyboard'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:code">
        <code>
            <xsl:call-template name="f:attlist.code"/>
            <xsl:apply-templates select="node()"/>
        </code>
    </xsl:template>

    <xsl:template name="f:attlist.code">
        <xsl:attribute name="xml:space" select="'preserve'"/>
        <xsl:call-template name="f:attrs"/>
        <xsl:call-template name="f:i18n"/>
    </xsl:template>

    <xsl:template match="html:pre">
        <p>
            <xsl:call-template name="f:attlist.pre"/>
            <xsl:apply-templates select="node()"/>
        </p>
    </xsl:template>

    <xsl:template name="f:attlist.pre">
        <xsl:call-template name="f:attrs"/>
        <xsl:call-template name="f:i18n"/>
        <xsl:attribute name="xml:space" select="'preserve'"/>
    </xsl:template>

    <!-- <samp> is not allowed in nordic DTBook. Replacing with code. -->
    <xsl:template match="html:samp">
        <xsl:message select="'&lt;samp&gt; is not allowed in nordic DTBook. Replacing with code and a &quot;example&quot; class.'"/>
        <code>
            <xsl:call-template name="f:attlist.samp"/>
            <xsl:apply-templates select="node()"/>
        </code>
    </xsl:template>

    <!-- <samp> is not allowed in nordic DTBook. Replacing with code and a "example" class. -->
    <xsl:template name="f:attlist.samp">
        <xsl:attribute name="xml:space" select="'preserve'"/>
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="classes" select="'example'" tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="f:i18n"/>
    </xsl:template>

    <!-- <cite> is not allowed in nordic DTBook. Using span instead. -->
    <xsl:template match="html:cite">
        <xsl:message select="'&lt;cite&gt; is not allowed in nordic DTBook. Using span instead with a cite class.'"/>
        <span>
            <xsl:call-template name="f:attlist.cite"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <!-- <cite> is not allowed in nordic DTBook. Using span instead with a cite class. -->
    <xsl:template name="f:attlist.cite">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="classes" select="'cite'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <!-- abbr is disallowed in nordic dtbooks, using span instead -->
    <xsl:template match="html:abbr[f:types(.)='z3998:truncation']">
        <span>
            <xsl:call-template name="f:attlist.abbr"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <xsl:template name="f:attlist.abbr">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <!-- acronym is disallowed in nordic dtbooks, using span instead -->
    <xsl:template match="html:abbr">
        <span>
            <xsl:call-template name="f:attlist.acronym"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <!-- acronym is disallowed in nordic dtbooks, using span instead and thus not setting the pronounce attribute -->
    <xsl:template name="f:attlist.acronym">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="classes" select="if (not(f:types(.)='z3998:initialism')) then 'acronym' else ()" tunnel="yes"/>
        </xsl:call-template>
        <!--<xsl:if test="">
            <xsl:attribute name="pronounce" select="if (f:types(.)='z3998:initialism') then 'yes' else 'no'"/>
        </xsl:if>-->
    </xsl:template>

    <xsl:template match="html:sub">
        <sub>
            <xsl:call-template name="f:attlist.sub"/>
            <xsl:apply-templates select="node()"/>
        </sub>
    </xsl:template>

    <xsl:template name="f:attlist.sub">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <xsl:template match="html:sup">
        <sup>
            <xsl:call-template name="f:attlist.sup"/>
            <xsl:apply-templates select="node()"/>
        </sup>
    </xsl:template>

    <xsl:template name="f:attlist.sup">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <xsl:template match="html:span">
        <span>
            <xsl:call-template name="f:attlist.span"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <xsl:template name="f:attlist.span">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <!-- <bdo> is not allowed in nordic DTBook. Replacing with span. -->
    <xsl:template match="html:bdo">
        <xsl:message select="'&lt;bdo&gt; is not allowed in nordic DTBook. Replacing with span and a &quot;bdo-dir-{@dir}&quot; class.'"/>
        <span>
            <xsl:call-template name="f:attlist.bdo"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <!-- <bdo> is not allowed in nordic DTBook. Replacing with span and a "bdo-dir-{@dir}" class. -->
    <xsl:template name="f:attlist.bdo">
        <xsl:call-template name="f:coreattrs">
            <xsl:with-param name="classes" select="('bdo', if (@dir and not(@dir='')) then concat('bdo-dir-',@dir) else ())" tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="f:i18n"/>
    </xsl:template>

    <!-- <sent> not allowed in nordic guidelines, using <span> instead -->
    <xsl:template match="html:span[f:types(.)='z3998:sentence']">
        <span>
            <xsl:call-template name="f:attlist.sent"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <!-- <sent> not allowed in nordic guidelines, using <span> instead and including the 'sentence' type as a class -->
    <!--            <xsl:with-param name="except-classes" select="'sentence'" tunnel="yes"/>-->
    <xsl:template name="f:attlist.sent">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <!-- <w> is not allowed in nordic DTBook. Using span instead. -->
    <xsl:template match="html:span[f:types(.)='z3998:word' and not(f:types(.)='z3998:sentence')]">
        <xsl:message select="'&lt;w&gt; is not allowed in nordic DTBook. Using span instead with a &quot;word&quot; class.'"/>
        <span>
            <xsl:call-template name="f:attlist.w"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <!-- <w> is not allowed in nordic DTBook. Using span instead with a "word" class. -->
    <xsl:template name="f:attlist.w">
        <xsl:call-template name="f:attrs">
            <!--            <xsl:with-param name="except-classes" select="'word'" tunnel="yes"/>-->
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:span[f:types(.)='pagebreak'] | html:div[f:types(.)='pagebreak']">
        <xsl:choose>
            <xsl:when test="ancestor::html:td">
                <xsl:message select="'Moving pagenum in table cell before current row for DTBook conformance.'"/>
            </xsl:when>
            <xsl:otherwise>
                <pagenum>
                    <xsl:call-template name="f:attlist.pagenum"/>
                    <xsl:value-of select="@title"/>
                </pagenum>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="f:attlist.pagenum">
        <xsl:call-template name="f:attrsrqd">
            <xsl:with-param name="except" select="'title'" tunnel="yes"/>
            <xsl:with-param name="except-classes" select="('page-front','page-normal','page-special','pagebreak')" tunnel="yes"/>
        </xsl:call-template>
        <xsl:attribute name="page" select="replace((f:classes(.)[starts-with(.,'page-')],'page-normal')[1], '^page-', '')"/>
    </xsl:template>

    <xsl:template match="html:a[f:types(.)='noteref']">
        <noteref>
            <xsl:call-template name="f:attlist.noteref"/>
            <xsl:apply-templates select="node()"/>
        </noteref>
    </xsl:template>

    <xsl:template name="f:attlist.noteref">
        <xsl:if test="f:classes(.)[not(.='noteref')] or f:types(.)[not(.='noteref')]">
            <xsl:message select="'the class attribute on a noteref was dropped since it is not allowed in Nordic DTBook.'"/>
        </xsl:if>
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="except-classes" select="'*'" tunnel="yes"/>
        </xsl:call-template>
        <xsl:attribute name="idref" select="@href"/>
        <xsl:copy-of select="@type" exclude-result-prefixes="#all"/>
    </xsl:template>

    <!-- <annoref> is not allowed in nordic DTBook. Replacing with span. -->
    <xsl:template match="html:a[f:types(.)='annoref']">
        <xsl:message select="'&lt;annoref&gt; is not allowed in nordic DTBook. Replacing with span and a &quot;annoref&quot; class.'"/>
        <span>
            <xsl:call-template name="f:attlist.annoref"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <!-- <annoref> is not allowed in nordic DTBook. Replacing with span and a "annoref" class. -->
    <xsl:template name="f:attlist.annoref">
        <xsl:call-template name="f:attrs">
            <!--            <xsl:with-param name="except-classes" select="'annoref'" tunnel="yes"/>-->
        </xsl:call-template>
        <!--<xsl:attribute name="idref" select="@href"/>
        <xsl:copy-of select="@type" exclude-result-prefixes="#all"/>-->
    </xsl:template>

    <!-- <q> is not allowed in nordic DTBook. Replacing with span. -->
    <xsl:template match="html:q">
        <xsl:message select="'&lt;q&gt; is not allowed in nordic DTBook. Replacing with span and a &quot;quote&quot; class.'"/>
        <span>
            <xsl:call-template name="f:attlist.q"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <!-- <q> is not allowed in nordic DTBook. Replacing with span and a "quote" class. -->
    <xsl:template name="f:attlist.q">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="classes" select="'quote'" tunnel="yes"/>
        </xsl:call-template>
        <!--        <xsl:copy-of select="@cite" exclude-result-prefixes="#all"/>-->
    </xsl:template>

    <xsl:template match="html:img">
        <img>
            <xsl:call-template name="f:attlist.img"/>
            <xsl:apply-templates select="node()"/>
        </img>
    </xsl:template>

    <xsl:template name="f:attlist.img">
        <xsl:param name="all-ids" tunnel="yes" select=".//@id"/>
        <xsl:call-template name="f:attrs"/>
        <xsl:attribute name="src" select="replace(@src,'^images/','')"/>
        <xsl:choose>
            <xsl:when test="@alt='image' and (ancestor-or-self::*/(@xml:lang|@lang))[last()]='sv'">
                <xsl:attribute name="alt" select="'illustration'"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="@alt" exclude-result-prefixes="#all"/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:copy-of select="@longdesc|@height|@width" exclude-result-prefixes="#all"/>
        <xsl:if test="not(@id)">
            <xsl:attribute name="id" select="f:generate-pretty-id(.,$all-ids)"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="html:figure[f:classes(.)='image']">
        <xsl:choose>
            <xsl:when test="parent::html:figure[f:classes(.)='image-series']">
                <xsl:apply-templates select="html:img"/>
                <xsl:apply-templates select="html:figcaption"/>
                <xsl:apply-templates select="node()[not(self::html:img or self::html:figcaption)]"/>
            </xsl:when>
            <xsl:otherwise>
                <imggroup>
                    <xsl:call-template name="f:attlist.imggroup"/>
                    <xsl:apply-templates select="html:img"/>
                    <xsl:apply-templates select="html:figcaption"/>
                    <xsl:apply-templates select="node()[not(self::html:img or self::html:figcaption)]"/>
                </imggroup>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="html:figure[f:classes(.)='image-series']">
        <imggroup>
            <xsl:call-template name="f:attlist.imggroup"/>
            <xsl:apply-templates select="node()"/>
        </imggroup>
    </xsl:template>

    <xsl:template name="f:attlist.imggroup">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="except-classes" select="('image-series','image')" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:p">
        <xsl:variable name="precedinghr" select="preceding-sibling::*[1] intersect preceding-sibling::html:hr[1]"/>
        <p>
            <xsl:call-template name="f:attlist.p">
                <xsl:with-param name="classes" select="if ($precedinghr) then (if ($precedinghr/tokenize(@class,'\s')='separator') then 'precedingseparator' else 'precedingemptyline') else ()"
                    tunnel="yes"/>
            </xsl:call-template>
            <xsl:apply-templates select="node()"/>
        </p>
    </xsl:template>

    <xsl:template name="f:attlist.p">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <xsl:template match="html:hr"/>

    <xsl:template match="html:h1[f:types(.)='fulltitle' and parent::html:header[parent::html:body]]">
        <doctitle>
            <xsl:call-template name="f:attlist.doctitle"/>
            <xsl:apply-templates select="node()"/>
        </doctitle>
    </xsl:template>

    <xsl:template name="f:attlist.doctitle">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="except-classes" select="('fulltitle','title')" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:*[f:types(.)='z3998:author' and parent::html:header[parent::html:body]]">
        <docauthor>
            <xsl:call-template name="f:attlist.docauthor"/>
            <xsl:apply-templates select="node()"/>
        </docauthor>
    </xsl:template>

    <xsl:template name="f:attlist.docauthor">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="except-classes" select="('author','docauthor')" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <!-- <covertitle> is not allowed in nordic DTBook. Using p instead. -->
    <xsl:template match="html:*[f:types(.)='covertitle' and parent::html:header[parent::html:body]]">
        <xsl:message select="'&lt;covertitle&gt; is not allowed in nordic DTBook, dropping it...'"/>
    </xsl:template>

    <!-- <covertitle> is not allowed in nordic DTBook. Using p instead with a "covertitle" class. -->
    <xsl:template name="f:attlist.covertitle">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="classes" select="'covertitle'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:h1 | html:h2 | html:h3 | html:h4 | html:h5 | html:h6">
        <xsl:element name="h{if (parent::*/f:types(.)=('sidebar','z3998:poem','z3998:verse') or parent::*/f:classes(.)='linegroup') then 'd' else f:level(.)}">
            <xsl:call-template name="f:attlist.h"/>
            <xsl:apply-templates select="node()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template name="f:attlist.h">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <xsl:template match="text()[ancestor::html:h1 | ancestor::html:h2 | ancestor::html:h3 | ancestor::html:h4 | ancestor::html:h5 | ancestor::html:h6]">
        <!-- normalize space in headlines -->
        <xsl:choose>
            <xsl:when
                test="normalize-space()='' and count((ancestor::*[matches(name(),'h\d')][1]//text() intersect preceding::text())[normalize-space()]) and count((ancestor::*[matches(name(),'h\d')][1]//text() intersect following::text())[normalize-space()])">
                <xsl:text> </xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="normalize-space(.)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- <bridgehead> is not allowed in nordic DTBook. Using p instead. -->
    <xsl:template match="html:p[f:types(.)='bridgehead']">
        <xsl:message select="'&lt;bridgehead&gt; is not allowed in nordic DTBook. Using p instead with a bridgehead class.'"/>
        <p>
            <xsl:call-template name="f:attlist.bridgehead"/>
            <xsl:apply-templates select="node()"/>
        </p>
    </xsl:template>

    <!-- <bridgehead> is not allowed in nordic DTBook. Using p instead with a bridgehead class. -->
    <xsl:template name="f:attlist.bridgehead">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="classes" select="'bridgehead'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:blockquote">
        <blockquote>
            <xsl:call-template name="f:attlist.blockquote"/>
            <xsl:apply-templates select="node()"/>
        </blockquote>
    </xsl:template>

    <xsl:template name="f:attlist.blockquote">
        <xsl:call-template name="f:attrs"/>
        <xsl:copy-of select="@cite" exclude-result-prefixes="#all"/>
    </xsl:template>

    <xsl:template match="html:dl">
        <dl>
            <xsl:call-template name="f:attlist.dl"/>
            <xsl:apply-templates select="node()"/>
        </dl>
    </xsl:template>

    <xsl:template name="f:attlist.dl">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <xsl:template match="html:dt">
        <dt>
            <xsl:call-template name="f:attlist.dt"/>
            <xsl:apply-templates select="node()"/>
        </dt>
    </xsl:template>

    <xsl:template name="f:attlist.dt">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <xsl:template match="html:dd">
        <dd>
            <xsl:call-template name="f:attlist.dd"/>
            <xsl:apply-templates select="node()"/>
        </dd>
    </xsl:template>

    <xsl:template name="f:attlist.dd">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <xsl:template match="html:ol | html:ul">
        <xsl:choose>
            <xsl:when test="parent::html:section[f:types(.)='toc' and not(ancestor::html:section | ancestor::html:article)]">
                <list>
                    <xsl:call-template name="f:attlist.list">
                        <xsl:with-param name="classes" select="'toc'" tunnel="yes"/>
                    </xsl:call-template>
                    <xsl:apply-templates select="node()"/>
                </list>
            </xsl:when>
            <xsl:when test="f:types(.)=('rearnotes','footnotes') or html:li/f:types(.)=('rearnote','footnote')">
                <xsl:apply-templates select="node()"/>
            </xsl:when>
            <xsl:otherwise>
                <list>
                    <xsl:call-template name="f:attlist.list"/>
                    <xsl:apply-templates select="node()"/>
                </list>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="f:attlist.list">
        <!-- Only 'pl' is allowed in nordic DTBook, markers will be inlined. A generic script would set type to ul or ol (i.e. select="local-name()"). -->
        <xsl:attribute name="type" select="'pl'"/>
        <xsl:call-template name="f:attrs"/>
        <!--
        list is always preformatted so enum and start is not included in the result
        <xsl:copy-of select="@start" exclude-result-prefixes="#all"/>
        <xsl:if test="@type">
            <xsl:attribute name="enum" select="@type"/>
        </xsl:if>
        -->
        <xsl:attribute name="depth" select="count(ancestor::html:li[not(f:types(.)=('rearnote','footnote'))])+1"/>
    </xsl:template>

    <!-- Only 'pl' is allowed in nordic DTBook; prepend markers ("• " for ul, "1. " for numbered, etc) to all list items. -->
    <xsl:template match="html:li">
        <li>
            <xsl:call-template name="f:attlist.li"/>

            <xsl:variable name="is-block-except-list" select="if ((html:p | html:dl | html:div | html:blockquote | html:table | html:address | html:section | html:aside)) then true() else false()"/>

            <xsl:variable name="marker">
                <xsl:choose>
                    <xsl:when test="parent::html:*/f:classes(.)='list-style-type-none'"/>
                    <xsl:when test="(ancestor::html:section[1], ancestor::html:body)[1][f:types(.)='toc']"/>
                    <xsl:when test="parent::html:ul">
                        <xsl:value-of select="'• '"/>
                    </xsl:when>
                    <xsl:when test="parent::html:ol[not(@type) or @type='1']">
                        <xsl:variable name="value" select="f:li-value(.)"/>
                        <xsl:value-of select="concat($value,'. ')"/>
                    </xsl:when>
                    <xsl:when test="parent::html:ol[@type=('a','A')]">
                        <xsl:variable name="value" select="f:li-value(.)"/>
                        <xsl:variable name="alpha" select="f:numeric-decimal-to-alpha($value)"/>
                        <xsl:value-of select="concat(if (parent::html:ol/@type='a') then lower-case($alpha) else upper-case($alpha),'. ')"/>
                    </xsl:when>
                    <xsl:when test="parent::html:ol[@type=('i','I')]">
                        <xsl:variable name="value" select="f:li-value(.)"/>
                        <xsl:variable name="roman" select="pf:numeric-decimal-to-roman($value)"/>
                        <xsl:value-of select="concat(if (parent::html:ol/@type='i') then lower-case($roman) else upper-case($roman),'. ')"/>
                    </xsl:when>
                </xsl:choose>
            </xsl:variable>

            <xsl:choose>
                <xsl:when test="$is-block-except-list and string-length($marker)">
                    <xsl:choose>
                        <xsl:when test="*[1] intersect html:p">
                            <xsl:for-each select="*[1]">
                                <p>
                                    <xsl:call-template name="f:attlist.p"/>
                                    <xsl:value-of select="$marker"/>
                                    <xsl:apply-templates select="node()"/>
                                </p>
                            </xsl:for-each>
                            <xsl:apply-templates select="node() except *[1]"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <p>
                                <xsl:value-of select="$marker"/>
                            </p>
                            <xsl:apply-templates select="node()"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:when test="$is-block-except-list">
                    <xsl:apply-templates select="node()"/>
                </xsl:when>
                <xsl:when test="(html:ol or html:ul)">
                    <xsl:for-each-group select="node() except text()[not(normalize-space())]"
                        group-adjacent="not(self::html:ol or self::html:ul or self::html:a[html:span[f:classes(.)='lic']] or self::html:span[f:classes(.)='lic'])">
                        <xsl:choose>
                            <xsl:when test="current-grouping-key()">
                                <lic>
                                    <xsl:if test="position()=1 and string-length($marker)">
                                        <xsl:value-of select="$marker"/>
                                    </xsl:if>
                                    <xsl:apply-templates select="current-group()"/>
                                </lic>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:if test="position()=1 and string-length($marker)">
                                    <lic>
                                        <xsl:value-of select="$marker"/>
                                    </lic>
                                </xsl:if>
                                <xsl:apply-templates select="current-group()"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each-group>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$marker"/>
                    <xsl:apply-templates select="node()"/>
                </xsl:otherwise>
            </xsl:choose>
        </li>
    </xsl:template>

    <xsl:template name="f:attlist.li">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <xsl:template match="html:span[f:classes(.)='lic']">
        <xsl:variable name="position" select="count(preceding-sibling::*) + 1"/>
        <xsl:variable name="children" select="node()"/>
        <lic>
            <xsl:call-template name="f:attlist.lic"/>
            <xsl:choose>
                <xsl:when test="parent::html:a">
                    <xsl:variable name="a">
                        <xsl:for-each select="parent::*">
                            <xsl:call-template name="f:a">
                                <xsl:with-param name="children" select="$children"/>
                            </xsl:call-template>
                        </xsl:for-each>
                    </xsl:variable>
                    <xsl:for-each select="$a/node()">
                        <xsl:choose>
                            <xsl:when test="count(@* except @id) or ($position = 1 and @id)">
                                <xsl:copy exclude-result-prefixes="#all">
                                    <xsl:copy-of select="@* except @id" exclude-result-prefixes="#all"/>
                                    <xsl:if test="$position = 1">
                                        <xsl:copy-of select="@id" exclude-result-prefixes="#all"/>
                                    </xsl:if>
                                    <xsl:copy-of select="node()" exclude-result-prefixes="#all"/>
                                </xsl:copy>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:copy-of select="node()" exclude-result-prefixes="#all"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="node()"/>
                </xsl:otherwise>
            </xsl:choose>
        </lic>
    </xsl:template>

    <xsl:template name="f:attlist.lic">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="except-classes" select="'lic'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="f:cellhvalign">
        <!--
            the @cellhalign and @cellvalign attributes could potentially be inferred from the CSS here,
            but it's probably not worth it so they are ignored for now.
        -->
    </xsl:template>

    <xsl:template match="html:table">
        <table>
            <xsl:call-template name="f:attlist.table"/>
            <xsl:for-each select="html:caption">
                <xsl:call-template name="f:caption.table"/>
            </xsl:for-each>
            <xsl:apply-templates select="html:colgroup"/>

            <xsl:apply-templates select="html:thead/html:tr"/>
            <xsl:apply-templates select="html:tbody/html:tr | html:tr"/>
            <xsl:apply-templates select="html:tfoot/html:tr"/>

            <!--<xsl:apply-templates select="html:thead"/>
            <xsl:apply-templates select="html:tfoot"/>
            <xsl:apply-templates select="html:tbody | html:tr"/>-->
        </table>
    </xsl:template>

    <xsl:template name="f:attlist.table">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="except-classes" select="for $class in (f:classes(.)) return if (starts-with($class,'table-rules') or starts-with($class,'table-frame-')) then $class else ()"
                tunnel="yes"/>
        </xsl:call-template>
        <xsl:if test="html:caption/html:p[f:classes(.)='table-summary']">
            <xsl:attribute name="summary" select="normalize-space(string-join(html:caption/html:p[f:classes(.)='table-summary']//text(),' '))"/>
        </xsl:if>
        <xsl:if test="count(f:classes(.)[matches(.,'^table-rules-')])">
            <xsl:attribute name="rules" select="replace(f:classes(.)[matches(.,'^table-rules-')][1],'^table-rules-','')"/>
        </xsl:if>
        <xsl:if test="count(f:classes(.)[matches(.,'^table-frame-')])">
            <xsl:attribute name="frame" select="replace(f:classes(.)[matches(.,'^table-frame-')][1],'^table-frame-','')"/>
        </xsl:if>
        <!--
            @cellspacing, @cellpadding and @width could potentially be inferred from the CSS,
            but it's probably not worth it so they are ignored for now
        -->
    </xsl:template>

    <xsl:template name="f:caption.table">
        <xsl:variable name="content" select="node()[not(self::html:p[f:classes(.)='table-summary'])]"/>
        <xsl:if test="$content">
            <caption>
                <xsl:call-template name="f:attlist.caption"/>
                <xsl:apply-templates select="$content"/>
            </caption>
        </xsl:if>
    </xsl:template>

    <xsl:template match="html:figcaption">
        <caption>
            <xsl:call-template name="f:attlist.caption"/>
            <xsl:apply-templates select="node()"/>
        </caption>
    </xsl:template>

    <xsl:template name="f:attlist.caption">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <xsl:template match="html:thead">
        <thead>
            <xsl:call-template name="f:attlist.thead"/>
            <xsl:apply-templates select="node()"/>
        </thead>
    </xsl:template>

    <xsl:template name="f:attlist.thead">
        <xsl:call-template name="f:attrs"/>
        <xsl:call-template name="f:cellhvalign"/>
    </xsl:template>

    <xsl:template match="html:tfoot">
        <tfoot>
            <xsl:call-template name="f:attlist.tfoot"/>
            <xsl:apply-templates select="node()"/>
        </tfoot>
    </xsl:template>

    <xsl:template name="f:attlist.tfoot">
        <xsl:call-template name="f:attrs"/>
        <xsl:call-template name="f:cellhvalign"/>
    </xsl:template>

    <xsl:template match="html:tbody">
        <tbody>
            <xsl:call-template name="f:attlist.tbody"/>
            <xsl:apply-templates select="node()"/>
        </tbody>
    </xsl:template>

    <xsl:template name="f:attlist.tbody">
        <xsl:call-template name="f:attrs"/>
        <xsl:call-template name="f:cellhvalign"/>
    </xsl:template>

    <!-- <colgroup> is not allowed in nordic DTBook. -->
    <xsl:template match="html:colgroup">
        <xsl:message select="'&lt;colgroup&gt; is not allowed in nordic DTBook.'"/>
        <!--<colgroup>
            <xsl:call-template name="f:attlist.colgroup"/>
            <xsl:apply-templates select="node()"/>
        </colgroup>-->
    </xsl:template>

    <xsl:template name="f:attlist.colgroup">
        <xsl:call-template name="f:attrs"/>
        <xsl:copy-of select="@span" exclude-result-prefixes="#all"/>
        <xsl:call-template name="f:cellhvalign"/>
        <!--
            @width could potentially be inferred from the CSS,
            but it's probably not worth it so they are ignored for now
        -->
    </xsl:template>

    <!-- <col> is not allowed in nordic DTBook. -->
    <xsl:template match="html:col">
        <xsl:message select="'&lt;col&gt; is not allowed in nordic DTBook.'"/>
        <!--<col>
            <xsl:call-template name="f:attlist.col"/>
            <xsl:apply-templates select="node()"/>
        </col>-->
    </xsl:template>

    <xsl:template name="f:attlist.col">
        <xsl:call-template name="f:attrs"/>
        <xsl:copy-of select="@span" exclude-result-prefixes="#all"/>
        <xsl:call-template name="f:cellhvalign"/>
        <!--
            @width could potentially be inferred from the CSS,
            but it's probably not worth it so they are ignored for now
        -->
    </xsl:template>

    <xsl:template match="html:tr">
        <xsl:choose>
            <xsl:when test="not(html:td//*[self::html:span[f:types(.)='pagebreak']])">
                <tr>
                    <xsl:call-template name="f:attlist.tr"/>
                    <xsl:apply-templates select="node()"/>
                </tr>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="content" select="html:td//*[self::html:span[f:types(.)='pagebreak']]"/>
                <xsl:for-each select="$content">
                    <pagenum>
                        <xsl:call-template name="f:attlist.pagenum"/>
                        <xsl:value-of select="@title"/>
                    </pagenum>
                </xsl:for-each>
                <tr>
                    <xsl:call-template name="f:attlist.tr"/>
                    <xsl:apply-templates select="node()"/>
                </tr>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="f:attlist.tr">
        <xsl:call-template name="f:attrs"/>
        <xsl:call-template name="f:cellhvalign"/>
        <!--
            @width could potentially be inferred from the CSS,
            but it's probably not worth it so they are ignored for now
        -->
    </xsl:template>

    <xsl:template match="html:th">
        <th>
            <xsl:call-template name="f:attlist.th"/>
            <xsl:apply-templates select="node()"/>
        </th>
    </xsl:template>

    <xsl:template name="f:attlist.th">
        <xsl:call-template name="f:attrs"/>
        <xsl:copy-of select="@headers|@scope|@rowspan|@colspan" exclude-result-prefixes="#all"/>
        <xsl:call-template name="f:cellhvalign"/>
    </xsl:template>

    <xsl:template match="html:td">
        <td>
            <xsl:call-template name="f:attlist.td"/>
            <xsl:apply-templates select="node()"/>
        </td>
    </xsl:template>

    <xsl:template name="f:attlist.td">
        <xsl:call-template name="f:attrs"/>
        <xsl:copy-of select="@headers|@scope|@rowspan|@colspan" exclude-result-prefixes="#all"/>
        <xsl:call-template name="f:cellhvalign"/>
    </xsl:template>
    
    <xsl:template match="math:*">
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="math:*/@*">
        <xsl:choose>
            <xsl:when test="local-name() = 'altimg'">
                <xsl:attribute name="altimg" select="replace(.,'^images/','')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="." exclude-result-prefixes="#all"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="f:copy-preceding-comments">
        <xsl:variable name="this" select="."/>
        <xsl:apply-templates select="preceding-sibling::comment()[not($this/preceding-sibling::*) or preceding-sibling::*[1] is $this/preceding-sibling::*[1]]"/>
    </xsl:template>

    <xsl:function name="f:types" as="xs:string*">
        <xsl:param name="element" as="element()"/>
        <xsl:sequence select="tokenize($element/@epub:type,'\s+')"/>
    </xsl:function>

    <xsl:function name="f:classes" as="xs:string*">
        <xsl:param name="element" as="element()"/>
        <xsl:sequence select="tokenize($element/@class,'\s+')"/>
    </xsl:function>

    <xsl:function name="f:level" as="xs:integer">
        <xsl:param name="element" as="element()"/>
        <xsl:variable name="level" select="($element/ancestor-or-self::html:*[self::html:section or self::html:article or self::html:aside or self::html:nav or self::html:body])[last()]"/>
        <xsl:variable name="level-nodes" select="f:level-nodes($level)"/>
        <xsl:variable name="h-in-section" select="$level-nodes[self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4 or self::html:h5 or self::html:h6]"/>
        <xsl:variable name="h" select="$h-in-section[1]"/>
        <xsl:variable name="sections" select="$level/ancestor-or-self::*[self::html:section or self::html:article or self::html:aside or self::html:nav]"/>
        <xsl:variable name="explicit-level" select="count($sections)-1"/>
        <xsl:variable name="h-in-level-numbers" select="if ($h-in-section) then reverse($h-in-section/xs:integer(number(replace(local-name(),'^h','')))) else 1"/>
        <xsl:variable name="implicit-level" select="if ($h-in-level-numbers[1] = 6) then 6 else ()"/>
        <xsl:variable name="h-in-level-numbers" select="$h-in-level-numbers[not(.=6)]"/>
        <xsl:variable name="implicit-level" select="($implicit-level, if ($h-in-level-numbers[1] = 5) then 5 else ())"/>
        <xsl:variable name="h-in-level-numbers" select="$h-in-level-numbers[not(.=5)]"/>
        <xsl:variable name="implicit-level" select="($implicit-level, if ($h-in-level-numbers[1] = 4) then 4 else ())"/>
        <xsl:variable name="h-in-level-numbers" select="$h-in-level-numbers[not(.=4)]"/>
        <xsl:variable name="implicit-level" select="($implicit-level, if ($h-in-level-numbers[1] = 3) then 3 else ())"/>
        <xsl:variable name="h-in-level-numbers" select="$h-in-level-numbers[not(.=3)]"/>
        <xsl:variable name="implicit-level" select="($implicit-level, if ($h-in-level-numbers[1] = 2) then 2 else ())"/>
        <xsl:variable name="implicit-level" select="($implicit-level, if ($h-in-level-numbers = 1) then 1 else ())"/>
        <xsl:variable name="implicit-level" select="count($implicit-level)"/>

        <xsl:variable name="level" select="$explicit-level + $implicit-level"/>
        <xsl:sequence select="max((1,min(($level, 6))))"/>
        <!--
            NOTE: DTBook only supports 6 levels when using the explicit level1-level6 / h1-h6 elements,
            so min(($level, 6)) is used to flatten deeper structures.
            The implicit level / hd elements could be used in cases where the structures are deeper.
            However, our tools would have to support those elements.
        -->
    </xsl:function>

    <xsl:function name="f:level-nodes" as="node()*">
        <xsl:param name="level" as="element()"/>
        <xsl:variable name="level-levels"
            select="$level//html:*[(self::html:section or self::html:article or self::html:aside or self::html:nav or self::html:body) and ((ancestor::html:*[self::html:section or self::html:article or self::html:aside or self::html:nav or self::html:body])[last()] intersect $level)]"/>
        <xsl:variable name="level-nodes" select="$level//node()[not(ancestor-or-self::* intersect $level-levels)]"/>
        <xsl:sequence select="$level-nodes"/>
    </xsl:function>

    <xsl:function name="f:generate-pretty-id" as="xs:string">
        <xsl:param name="element" as="element()"/>
        <xsl:param name="all-ids" as="xs:string*"/>
        <xsl:variable name="id">
            <xsl:choose>
                <xsl:when test="$element[self::html:blockquote or self::html:q]">
                    <xsl:sequence select="concat('quote_',count($element/(ancestor::*|preceding::*)[self::html:blockquote or self::html:q])+1)"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:variable name="element-name" select="local-name($element)"/>
                    <xsl:sequence select="concat($element-name,'_',count($element/(ancestor::*|preceding::*)[local-name()=$element-name])+1)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:sequence select="if ($all-ids=$id) then generate-id($element) else $id"/>
    </xsl:function>

    <xsl:function name="f:li-value" as="xs:integer">
        <xsl:param name="li" as="element()"/>
        <xsl:choose>
            <xsl:when test="not($li)">
                <xsl:value-of select="1"/>
            </xsl:when>
            <xsl:when test="$li/@value">
                <xsl:value-of select="$li/@value"/>
            </xsl:when>
            <xsl:when test="not($li/preceding-sibling::*)">
                <xsl:value-of select="if ($li/parent::*/@start) then $li/parent::*/@start else if ($li/parent::*/@reversed) then count($li/parent::*/*) else 1"/>
            </xsl:when>
            <xsl:when test="$li/parent::*/@reversed">
                <xsl:value-of
                    select="if ($li/preceding-sibling::*[@value]) then f:li-value(($li/preceding-sibling::*[@value])[last()]) - 1 - count($li/preceding-sibling::* intersect ($li/preceding-sibling::*[@value])[last()]/following-sibling::*) else ($li/parent::*/@start/number(.), count($li/parent::*/*))[1] - count($li/preceding-sibling::*)"
                />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of
                    select="if ($li/preceding-sibling::*[@value]) then f:li-value(($li/preceding-sibling::*[@value])[last()]) + 1 + count($li/preceding-sibling::* intersect ($li/preceding-sibling::*[@value])[last()]/following-sibling::*) else ($li/parent::*/@start/number(.), 1)[1] + count($li/preceding-sibling::*)"
                />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="f:numeric-decimal-to-alpha">
        <!-- TODO: move this to numeral-conversion.xsl in DP2 common-utils -->
        <xsl:param name="decimal" as="xs:integer"/>
        <xsl:value-of select="string-join(f:numeric-decimal-to-alpha-part($decimal,()),'')"/>
    </xsl:function>

    <xsl:function name="f:numeric-decimal-to-alpha-part">
        <xsl:param name="remainder" as="xs:integer"/>
        <xsl:param name="result" as="xs:string*"/>
        <xsl:choose>
            <xsl:when test="$remainder &lt;= 0">
                <xsl:sequence select="$result"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="f:numeric-decimal-to-alpha-part(xs:integer(floor($remainder div 26)), (codepoints-to-string(($remainder mod 26) + 96), $result))"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="f:is-inline" as="xs:boolean">
        <xsl:param name="element" as="node()"/>
        <xsl:sequence select="$element[self::text()] or $element[self::* and local-name() = ('a','abbr','bdo','br','code','dfn','em','img','kbd','q','samp','span','strong','sub','sup')]"/>
    </xsl:function>

</xsl:stylesheet>
