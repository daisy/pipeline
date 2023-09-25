<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema" version="2.0"
                xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns:rend="http://www.daisy.org/ns/z3998/authoring/features/rend/"
                xmlns:its="http://www.w3.org/2005/11/its"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp"
                xmlns:d="http://www.daisy.org/ns/z3998/authoring/features/description/"
                xmlns:m="http://www.w3.org/1998/Math/MathML"
                xmlns:tts="http://www.daisy.org/ns/pipeline/tts"
                xmlns="http://www.daisy.org/ns/z3998/authoring/"
                exclude-result-prefixes="#all">

    <xsl:import href="translate-mathml-to-zedai.xsl"/>
    <xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/generate-id.xsl"/>

    <doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
        <desc>Direct translation element and attribute names from DTBook to ZedAI. Most of the work
            regarding content model normalization has already been done.</desc>
    </doc>

    <xsl:param name="css-filename"/>

    <xsl:key name="ids" match="*" use="@id"/>

    <xsl:template match="/">
        <!-- just for testing: insert the oxygen schema reference -->
        <!--
            <xsl:processing-instruction name="oxygen">
            <xsl:text>RNGSchema="/Users/marisa/Projects/pipeline2/daisy-pipeline-modules/schemas/zedai/z3998-book-1.0/z3998-book.rng" type="xml"</xsl:text>
        </xsl:processing-instruction>
        -->
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="dtb:dtbook" priority="1">
        <xsl:call-template name="pf:next-match-with-generated-ids">
            <xsl:with-param name="prefix" select="'id_'"/>
            <xsl:with-param name="for-elements" select="//dtb:frontmatter|
                                                        //dtb:level[not(@id)]|
                                                        //dtb:level1[not(@id)]|
                                                        //dtb:level2[not(@id)]|
                                                        //dtb:level3[not(@id)]|
                                                        //dtb:level4[not(@id)]|
                                                        //dtb:level5[not(@id)]|
                                                        //dtb:level6[not(@id)]|
                                                        //dtb:img[not(@id)]|
                                                        //dtb:blockquote[not(@id)]|
                                                        //dtb:q[not(@id)]|
                                                        //dtb:table[not(@id)]|
                                                        //dtb:col[not(@id)]|
                                                        //dtb:colgroup[not(@id)]|
                                                        //dtb:thead[not(@id)]|
                                                        //dtb:tfoot[not(@id)]|
                                                        //dtb:tbody[not(@id)]|
                                                        //dtb:tr[not(@id)]|
                                                        //dtb:th[not(@id)]|
                                                        //dtb:td[not(@id)]|
                                                        //dtb:author[not(@id)]|
                                                        //*[not(self::dtb:q)]/dtb:cite[not(@id)]|
                                                        //dtb:poem[not(@id)]
                                                        "/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="generate-id">
        <xsl:param name="f:generated-ids" tunnel="yes" select="()"/>
        <xsl:choose>
            <xsl:when test="@id">
                <xsl:attribute name="xml:id" select="@id"/>
            </xsl:when>
            <xsl:otherwise>
                <!-- rename id to xml:id -->
                <xsl:variable name="id" as="xs:string">
                    <xsl:call-template name="pf:generate-id"/>
                </xsl:variable>
                <xsl:attribute name="xml:id" select="$id"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="comment() | text()">
        <xsl:copy/>
    </xsl:template>

    <!-- a common set of attributes -->
    <xsl:template name="attrs">
        <xsl:if test="@id">
            <xsl:attribute name="xml:id" select="@id"/>
        </xsl:if>
        <xsl:copy-of select="@xml:space"/>
        <xsl:copy-of select="@class"/>
        <xsl:copy-of select="@xml:lang"/>
	<xsl:copy-of select="@tts:*"/>
        <xsl:if test="@dir">
            <xsl:attribute name="its:dir" select="@dir"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="dtb:dtbook">
        <document>
            <!-- make sure xml:lang is set - if not, try to infer from:
              1. a dc:language metadata
              2. an xml:lang attribute on the book element
              3. the default value 'en' -->
            <xsl:if test="empty(@xml:lang)">
                <xsl:attribute name="xml:lang"
                    select="(dtb:head/dtb:meta[lower-case(@name)='dc:language'][1]/@content,dtb:book/@xml:lang,'en')[1]"
                />
            </xsl:if>
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </document>
    </xsl:template>

    <xsl:template match="dtb:head">
        <head>
            <xsl:call-template name="attrs"/>
            <!-- hard-coding the zedai 'book' profile for dtbook transformation -->
            <meta rel="z3998:profile"
                resource="http://www.daisy.org/z3998/2012/auth/profiles/book/1.0/">
                <meta property="z3998:name" content="book"/>
                <meta property="z3998:version" content="1.0"/>
            </meta>

            <xsl:if test="exists(//m:math)">
                <meta rel="z3998:feature"
                    resource="http://www.daisy.org/z3998/2012/auth/features/mathml/1.0/">
                    <meta property="z3998:name" content="mathml"/>
                    <meta property="z3998:version" content="1.0"/>
                </meta>
            </xsl:if>

            <meta rel="z3998:rdfa-context"
                resource="http://www.daisy.org/z3998/2012/vocab/context/default/"/>

            <!-- this dummy identifier value will be filled in by an external step -->
            <meta property="dc:identifier" content="@@"/>

            <!--
                note that the translation of existing dtbook to zedai metadata,
                including using a pre-existing value for dc:publisher,
                happens in a different step.
                This just ensures a value for dc:publisher if there is none present in the source doc
            -->
            <xsl:if
                test="string-length(normalize-space(dtb:meta[lower-case(@name)='dc:publisher'][1]/@content)) = 0">
                <meta property="dc:publisher" content="Anonymous"/>
            </xsl:if>

            <!-- use the current date -->
            <meta property="dc:date">
                <xsl:attribute name="content"
                    select="format-dateTime(
                    adjust-dateTime-to-timezone(current-dateTime(),xs:dayTimeDuration('PT0H')),
                    '[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01][Z]')"
                />
            </meta>
        </head>

    </xsl:template>

    <xsl:template match="dtb:book">
        <body>
            <xsl:call-template name="attrs"/>
            <!-- insert frontmatter if there is none -->
            <xsl:if test="not(dtb:frontmatter)">
                <frontmatter>
                    <section>
                        <h role="title">
                            <xsl:value-of
                                select="preceding-sibling::dtb:head/dtb:meta[@name='dc:Title']/@content"
                            />
                        </h>
                    </section>
                </frontmatter>
            </xsl:if>
            <xsl:apply-templates/>
        </body>
    </xsl:template>

    <xsl:template match="dtb:frontmatter">
        <frontmatter>
            <xsl:call-template name="attrs"/>
            <!-- all sections must have IDs in case we need to anchor floating annotations to them -->
            <section>
                <xsl:call-template name="generate-id"/>
                <xsl:apply-templates select="dtb:doctitle"/>
                <xsl:apply-templates select="dtb:covertitle"/>
                <xsl:apply-templates select="dtb:docauthor"/>
            </section>
            <xsl:apply-templates select="dtb:level"/>
            <xsl:apply-templates select="dtb:level1"/>
        </frontmatter>
    </xsl:template>

    <xsl:template match="dtb:docauthor">
        <p role="author">
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </p>
    </xsl:template>

    <xsl:template match="dtb:frontmatter/dtb:doctitle">
        <h role="title">
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </h>
    </xsl:template>
    <xsl:template match="dtb:doctitle">
        <p role="title">
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </p>
    </xsl:template>

    <xsl:template
        match="dtb:level1|dtb:level2|dtb:level3|dtb:level4|dtb:level5|dtb:level6|dtb:level">
        <section>
            <xsl:call-template name="attrs"/>
            <!-- all sections must have IDs in case we need to anchor floating annotations to them -->
            <xsl:if test="not(@id)">
                <xsl:call-template name="generate-id"/>
            </xsl:if>
            <xsl:apply-templates/>
        </section>
    </xsl:template>


    <xsl:template match="dtb:h1|dtb:h2|dtb:h3|dtb:h4|dtb:h5|dtb:h6">
        <h>
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </h>
    </xsl:template>

    <xsl:template match="dtb:bridgehead|dtb:hd">
        <hd>
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </hd>
    </xsl:template>

    <xsl:template match="dtb:em|dtb:strong">
        <emph>
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </emph>
    </xsl:template>

    <xsl:template match="dtb:list">
        <list>
            <xsl:call-template name="attrs"/>

            <xsl:copy-of select="@depth"/>

            <!-- convert @start to a numeric value when needed -->
            <xsl:choose>
                <xsl:when test="@start and boolean(number(@start)+1)">
                    <xsl:copy-of select="@start"/>
                </xsl:when>
                <xsl:when test="@start and @enum=('i','I')">
                    <xsl:attribute name="start" select="f:roman-to-decimal(@start)"/>
                </xsl:when>
                <xsl:when test="@start and @enum=('a','A')">
                    <xsl:attribute name="start" select="f:alpha-to-decimal(@start)"/>
                </xsl:when>
                <xsl:when test="@start">
                    <xsl:message>Unparsable start attribute '<xsl:value-of select="@start"/>'</xsl:message>
                </xsl:when>
            </xsl:choose>

            <xsl:if test="@enum = '1'">
                <xsl:attribute name="rend:prefix">decimal</xsl:attribute>
            </xsl:if>
            <xsl:if test="@enum = 'a'">
                <xsl:attribute name="rend:prefix">lower-alpha</xsl:attribute>
            </xsl:if>
            <xsl:if test="@enum = 'A'">
                <xsl:attribute name="rend:prefix">upper-alpha</xsl:attribute>
            </xsl:if>
            <xsl:if test="@enum = 'i'">
                <xsl:attribute name="rend:prefix">lower-roman</xsl:attribute>
            </xsl:if>
            <xsl:if test="@enum = 'I'">
                <xsl:attribute name="rend:prefix">upper-roman</xsl:attribute>
            </xsl:if>


            <xsl:if test="@type = 'ul'">
                <xsl:attribute name="type">unordered</xsl:attribute>
            </xsl:if>
            <xsl:if test="@type = 'ol'">
                <xsl:attribute name="type">ordered</xsl:attribute>
            </xsl:if>
            <xsl:if test="@type = 'pl'">
                <!-- no attributes added for type='pl' -->
            </xsl:if>

            <xsl:apply-templates/>
        </list>
    </xsl:template>


    <xsl:template match="dtb:list/dtb:hd">
        <item>
            <hd>
                <xsl:call-template name="attrs"/>
                <xsl:apply-templates/>
            </hd>
        </item>
    </xsl:template>

    <xsl:template match="dtb:li">
        <item>
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </item>
    </xsl:template>

    <xsl:template match="dtb:img">

        <!--TODO add @media-type-->
        <object>
            <xsl:call-template name="attrs"/>
            <xsl:copy-of select="@src"/>

            <!-- height and width get put into CSS-->
            <xsl:if test="@height">
                <xsl:attribute name="tmp:height" select="@height"/>
            </xsl:if>
            <xsl:if test="@width">
                <xsl:attribute name="tmp:width" select="@width"/>
            </xsl:if>

            <xsl:if test="not(@id)">
                <xsl:call-template name="generate-id"/>
            </xsl:if>
            <xsl:choose>
                <!-- when we're using longdesc, just point to it with @desc.
                    the value in @alt will be copied as the diagram markup is created.-->
                <xsl:when test="@longdesc">
                    <xsl:attribute name="desc" select="if (starts-with(@longdesc,'#')) then substring(@longdesc, 2) else @longdesc"/>
                </xsl:when>
                <!-- if there's no longdesc, then use zedai's description element for the alt text -->
                <xsl:otherwise>
                    <xsl:variable name="id" as="xs:string">
                        <xsl:call-template name="generate-id"/>
                    </xsl:variable>
                    <description ref="{$id}">
                        <xsl:value-of select="@alt"/>
                    </description>
                </xsl:otherwise>
            </xsl:choose>
        </object>
    </xsl:template>

    <xsl:template match="dtb:imggroup">
        <block>
            <xsl:if test="dtb:caption">
                <xsl:attribute name="role" select="'figure'"/>
            </xsl:if>
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </block>
    </xsl:template>

    <!-- use modes! -->
    <xsl:template match="dtb:caption" mode="table-caption">
        <xsl:param name="refValue"/>
        <caption>
            <xsl:if test="$refValue">
                <xsl:attribute name="ref" select="$refValue"/>
            </xsl:if>
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates mode="#default"/>
        </caption>
    </xsl:template>

    <xsl:template match="dtb:caption" mode="#default">
        <xsl:param name="refValue"/>
        <caption>
            <xsl:choose>
                <xsl:when test="@imgref">
                    <xsl:attribute name="ref" select="replace(@imgref, '#', '')"/>
                </xsl:when>
                <xsl:when test="parent::dtb:imggroup">
                    <!-- get the id of the image in the imggroup and use it as a ref -->
                    <!-- we know that images with no IDs had them generated in the img template, so re-use that ID. -->
                    <xsl:variable name="img-id" as="xs:string">
                        <xsl:for-each select="../dtb:img">
                            <xsl:call-template name="generate-id"/>
                        </xsl:for-each>
                    </xsl:variable>
                    <xsl:attribute name="ref" select="$img-id"/>
                </xsl:when>
            </xsl:choose>
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </caption>
    </xsl:template>

    <xsl:template name="createAnnotation">
        <xsl:param name="byValue"/>
        <xsl:param name="refValue"/>
        <annotation>
            <xsl:call-template name="attrs"/>

            <xsl:if test="$byValue">
                <xsl:attribute name="by" select="$byValue"/>
            </xsl:if>

            <xsl:if test="$refValue">
                <xsl:attribute name="ref" select="$refValue"/>
            </xsl:if>

            <!-- at this point, annotations could still be "floating', i.e. not anchored to anything.  this will get fixed in another step.  -->


            <xsl:apply-templates/>
        </annotation>
    </xsl:template>

    <!-- these elements were dtb:annotation elements but they were sorted into block and phrase variants in a separate step. -->
    <xsl:template match="tmp:annotation-block | tmp:annotation-phrase">
        <xsl:call-template name="createAnnotation"/>
    </xsl:template>

    <xsl:template match="dtb:prodnote">
        <xsl:variable name="prodnoteIDRef" select="concat('#', @id)"/>
        <xsl:choose>
            <!-- when the prodnote is a longdesc, create a DIAGRAM longdesc element -->
            <xsl:when test="//dtb:img[@longdesc=$prodnoteIDRef]">
                <d:description xml:id="{@id}">
                    <d:body>
                        <d:summary>
                            <xsl:value-of select="//dtb:img[@longdesc=$prodnoteIDRef]/@alt"/>
                        </d:summary>
                        <d:longdesc>
                            <xsl:attribute name="by" select="'republisher'"/>
                            <xsl:apply-templates/>
                        </d:longdesc>
                    </d:body>
                </d:description>
            </xsl:when>

            <!-- when the prodnote is not a longdesc (perhaps more common), create an annotation -->
            <xsl:when test="@imgref">
                <xsl:call-template name="createAnnotation">
                    <xsl:with-param name="byValue" select="'republisher'"/>
                    <xsl:with-param name="refValue" select="replace(@imgref, '#', '')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="parent::dtb:imggroup">
                <!-- get the id of the image in the imggroup and use it as a ref -->
                <xsl:call-template name="createAnnotation">
                    <xsl:with-param name="byValue" select="'republisher'"/>
                    <xsl:with-param name="refValue" select="../dtb:img/@id"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="createAnnotation">
                    <xsl:with-param name="byValue" select="'republisher'"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template match="dtb:sidebar">
        <aside role="sidebar">
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </aside>
    </xsl:template>

    <xsl:template match="dtb:note">

        <note>
            <xsl:call-template name="attrs"/>
            <xsl:if test="@class=('footnote','endnote')">
                <xsl:attribute name="role" select="@class"/>
            </xsl:if>
            <xsl:apply-templates/>
        </note>
    </xsl:template>


    <xsl:template match="dtb:div">
        <block>
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </block>
    </xsl:template>

    <xsl:template match="dtb:pagenum">
        <pagebreak value="{.}">
            <xsl:call-template name="attrs"/>
        </pagebreak>
    </xsl:template>

    <xsl:template match="dtb:noteref">
        <xsl:variable name="ref" select="substring-after(@idref,'#')"/>
        <xsl:choose>
            <xsl:when test="exists(key('ids',$ref))">
                <noteref ref="{$ref}">
                    <xsl:call-template name="attrs"/>
                    <xsl:value-of select="."/>
                </noteref>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message>WARNING Noteref '<xsl:value-of select="."/>' to missing ID '<xsl:value-of select="@idref"/>'</xsl:message>
                <xsl:comment>FIXME Noteref '<xsl:value-of select="."/>' to missing ID '<xsl:value-of select="@idref"/>'</xsl:comment>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="dtb:annoref">
        <xsl:variable name="ref" select="substring-after(@idref,'#')"/>
        <xsl:choose>
            <xsl:when test="exists(key('ids',$ref))">
                <annoref ref="{$ref}">
                    <xsl:call-template name="attrs"/>
                    <xsl:value-of select="."/>
                </annoref>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message>WARNING Annoref '<xsl:value-of select="."/>' to missing ID '<xsl:value-of select="@idref"/>'</xsl:message>
                <xsl:comment>FIXME Annoref '<xsl:value-of select="."/>' to missing ID '<xsl:value-of select="@idref"/>'</xsl:comment>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="dtb:blockquote|dtb:q">
        <quote>
            <xsl:call-template name="attrs"/>

            <!-- if no ID, then give a new ID if needed to anchor the citation -->
            <xsl:if test="not(@id) and (dtb:cite or dtb:author or dtb:title)">
                <xsl:call-template name="generate-id"/>
            </xsl:if>
            <xsl:choose>
                <!-- for internal references, use @ref; otherwise use @xlink:ref -->
                <xsl:when test="starts-with(@cite, '#')">
                    <xsl:attribute name="ref" select="substring(@cite, 2)"/>
                </xsl:when>
                <xsl:when test="@cite">
                    <xsl:attribute name="xlink:href" select="@cite"/>
                </xsl:when>
            </xsl:choose>
            <xsl:choose>
                <xsl:when test="self::dtb:blockquote">
                    <!--group adjacent author and title in a single citation-->
                    <xsl:for-each-group select="*" group-adjacent="boolean(self::dtb:author|self::dtb:title)">
                        <xsl:choose>
                            <xsl:when test="current-grouping-key()">
                                <xsl:variable name="blockquote-id" as="xs:string">
                                    <xsl:for-each select="..">
                                        <xsl:call-template name="generate-id"/>
                                    </xsl:for-each>
                                </xsl:variable>
                                <citation about="#{$blockquote-id}">
                                    <xsl:apply-templates select="current-group()"/>
                                </citation>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:apply-templates select="current-group()"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each-group>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates/>
                </xsl:otherwise>
            </xsl:choose>
        </quote>
    </xsl:template>
    <xsl:template match="dtb:blockquote/dtb:title">
        <span property="title">
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </span>
    </xsl:template>
    <xsl:template match="dtb:blockquote/dtb:author">
            <name property="author">
                <xsl:call-template name="attrs"/>
                <xsl:apply-templates/>
            </name>
    </xsl:template>
    <xsl:template match="dtb:q/dtb:title">
        <xsl:variable name="quote-id" as="xs:string">
            <xsl:for-each select="..">
                <xsl:call-template name="generate-id"/>
            </xsl:for-each>
        </xsl:variable>
        <span property="title" about="#{$quote-id}">
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </span>
    </xsl:template>
    <xsl:template match="dtb:q/dtb:author">
        <xsl:variable name="quote-id" as="xs:string">
            <xsl:for-each select="..">
                <xsl:call-template name="generate-id"/>
            </xsl:for-each>
        </xsl:variable>
        <name property="author" about="#{$quote-id}">
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </name>
    </xsl:template>

    <xsl:template match="dtb:rearmatter">
        <backmatter>
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </backmatter>
    </xsl:template>

    <xsl:template match="dtb:table">

        <!-- in ZedAI, captions don't live inside tables as in DTBook -->
        <xsl:if test="dtb:caption">
            <xsl:variable name="id" as="xs:string">
                <xsl:call-template name="generate-id"/>
            </xsl:variable>
            <xsl:apply-templates mode="table-caption" select="dtb:caption">
                <xsl:with-param name="refValue" select="$id"/>
            </xsl:apply-templates>
        </xsl:if>

        <!-- move @summary into an annotation -->
        <xsl:if test="@summary">
            <description xml:id="{generate-id(@summary)}">
                <xsl:value-of select="@summary"/>
            </description>
        </xsl:if>
        <table>
            <xsl:if test="@summary">
                <xsl:attribute name="desc" select="generate-id(@summary)"/>
            </xsl:if>
            <!-- These will be put into CSS by a future XSL step -->
            <xsl:if test="@width">
                <xsl:attribute name="tmp:width" select="@width"/>
            </xsl:if>
            <xsl:if test="@border">
                <xsl:attribute name="tmp:border" select="@border"/>
            </xsl:if>
            <xsl:if test="@cellspacing">
                <xsl:attribute name="tmp:cellspacing" select="@cellspacing"/>
            </xsl:if>
            <xsl:if test="@cellpadding">
                <xsl:attribute name="tmp:cellpadding" select="@cellpadding"/>
            </xsl:if>

            <xsl:call-template name="attrs"/>

            <!-- generate an ID for use by CSS -->
            <xsl:if test="not(@id)">
                <xsl:call-template name="generate-id"/>
            </xsl:if>

            <!--
                Translation of the table inner content model:
                 - group any adjacent 'col' in 'colgroup'
                 - if there is a thead or tfoot, then wrap tr|pagenum in a tbody
                 - if there is a tfoot, it is re-ordered after the tbody
            -->
            <xsl:if test="dtb:col">
                <colgroup>
                    <xsl:apply-templates select="dtb:col"/>
                </colgroup>
            </xsl:if>
            <xsl:apply-templates select="dtb:colgroup"/>
            <xsl:apply-templates select="dtb:thead"/>
            <xsl:choose>
                <xsl:when test="(dtb:thead or dtb:tfoot) and (dtb:tr or dtb:pagenum)">
                    <tbody>
                        <xsl:apply-templates select="dtb:tr|dtb:pagenum"/>
                    </tbody>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="dtb:tbody|dtb:tr|dtb:pagenum"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:apply-templates select="dtb:tfoot"/>

        </table>
    </xsl:template>

    <xsl:template match="dtb:col">
        <col>
            <xsl:call-template name="attrs"/>
            <xsl:copy-of select="@span"/>

            <!-- these will be put into a CSS file by a future XSL step -->
            <xsl:if test="@width">
                <xsl:attribute name="tmp:width" select="@width"/>
            </xsl:if>
            <xsl:if test="@align">
                <xsl:attribute name="tmp:align" select="@align"/>
            </xsl:if>
            <xsl:if test="@valign">
                <xsl:attribute name="tmp:valign" select="@valign"/>
            </xsl:if>

            <!-- generate an ID for use by CSS -->
            <xsl:if test="not(@id)">
                <xsl:call-template name="generate-id"/>
            </xsl:if>
            <xsl:apply-templates/>
        </col>
    </xsl:template>

    <xsl:template match="dtb:colgroup">
        <colgroup>
            <xsl:call-template name="attrs"/>

            <!-- ignore @span if there are any col children (this is what the DTBook DTD states, and it also maps nicely to ZedAI) -->
            <xsl:if test="not(./dtb:col)">
                <xsl:copy-of select="@span"/>
            </xsl:if>

            <!-- these will be put into a CSS file by a future XSL step -->
            <xsl:if test="@width">
                <xsl:attribute name="tmp:width" select="@width"/>
            </xsl:if>
            <xsl:if test="@align">
                <xsl:attribute name="tmp:align" select="@align"/>
            </xsl:if>
            <xsl:if test="@valign">
                <xsl:attribute name="tmp:valign" select="@valign"/>
            </xsl:if>
            <!-- generate an ID for use by CSS -->
            <xsl:if test="not(@id)">
                <xsl:call-template name="generate-id"/>
            </xsl:if>
            <xsl:apply-templates/>
        </colgroup>
    </xsl:template>

    <xsl:template match="dtb:thead">
        <thead>
            <xsl:call-template name="attrs"/>

            <!-- these will be put into a CSS file by a future XSL step -->
            <xsl:if test="@align">
                <xsl:attribute name="tmp:align" select="@align"/>
            </xsl:if>
            <xsl:if test="@valign">
                <xsl:attribute name="tmp:valign" select="@valign"/>
            </xsl:if>
            <!-- generate an ID for use by CSS -->
            <xsl:if test="not(@id)">
                <xsl:call-template name="generate-id"/>
            </xsl:if>
            <xsl:apply-templates/>
        </thead>
    </xsl:template>

    <xsl:template match="dtb:tfoot">
        <tfoot>
            <xsl:call-template name="attrs"/>

            <!-- these will be put into a CSS file by a future XSL step -->
            <xsl:if test="@align">
                <xsl:attribute name="tmp:align" select="@align"/>
            </xsl:if>
            <xsl:if test="@valign">
                <xsl:attribute name="tmp:valign" select="@valign"/>
            </xsl:if>
            <!-- generate an ID for use by CSS -->
            <xsl:if test="not(@id)">
                <xsl:call-template name="generate-id"/>
            </xsl:if>
            <xsl:apply-templates/>
        </tfoot>
    </xsl:template>

    <xsl:template match="dtb:tbody">
        <tbody>
            <xsl:call-template name="attrs"/>

            <!-- these will be put into a CSS file by a future XSL step -->
            <xsl:if test="@align">
                <xsl:attribute name="tmp:align" select="@align"/>
            </xsl:if>
            <xsl:if test="@valign">
                <xsl:attribute name="tmp:valign" select="@valign"/>
            </xsl:if>
            <!-- generate an ID for use by CSS -->
            <xsl:if test="not(@id)">
                <xsl:call-template name="generate-id"/>
            </xsl:if>
            <xsl:apply-templates/>
        </tbody>
    </xsl:template>

    <xsl:template match="dtb:tr">
        <tr>
            <xsl:call-template name="attrs"/>

            <!-- these will be put into a CSS file by a future XSL step -->
            <xsl:if test="@align">
                <xsl:attribute name="tmp:align" select="@align"/>
            </xsl:if>
            <xsl:if test="@valign">
                <xsl:attribute name="tmp:valign" select="@valign"/>
            </xsl:if>
            <!-- generate an ID for use by CSS -->
            <xsl:if test="not(@id)">
                <xsl:call-template name="generate-id"/>
            </xsl:if>
            <xsl:apply-templates/>
        </tr>
    </xsl:template>

    <xsl:template match="dtb:th">
        <th>
            <xsl:call-template name="attrs"/>
            <xsl:copy-of select="@abbr"/>
            <xsl:copy-of select="@headers"/>
            <xsl:copy-of select="@colspan"/>
            <xsl:copy-of select="@rowspan"/>
            <xsl:copy-of select="@scope"/>

            <!-- these will be put into a CSS file by a future XSL step -->
            <xsl:if test="@align">
                <xsl:attribute name="tmp:align" select="@align"/>
            </xsl:if>
            <xsl:if test="@valign">
                <xsl:attribute name="tmp:valign" select="@valign"/>
            </xsl:if>
            <!-- generate an ID for use by CSS -->
            <xsl:if test="not(@id)">
                <xsl:call-template name="generate-id"/>
            </xsl:if>

            <xsl:apply-templates/>
        </th>
    </xsl:template>

    <xsl:template match="dtb:td">
        <td>
            <xsl:call-template name="attrs"/>

            <xsl:copy-of select="@headers"/>
            <xsl:copy-of select="@colspan"/>
            <xsl:copy-of select="@rowspan"/>
            <xsl:copy-of select="@scope"/>

            <!-- these will be put into a CSS file by a future XSL step -->
            <xsl:if test="@align">
                <xsl:attribute name="tmp:align" select="@align"/>
            </xsl:if>
            <xsl:if test="@valign">
                <xsl:attribute name="tmp:valign" select="@valign"/>
            </xsl:if>
            <!-- generate an ID for use by CSS -->
            <xsl:if test="not(@id)">
                <xsl:call-template name="generate-id"/>
            </xsl:if>
            <xsl:apply-templates/>
        </td>
    </xsl:template>

    <xsl:template match="dtb:byline">
        <!-- for most book (non-article) use cases, byline can be citation. the exception would be anthologies, for which we can call upon the periodicals vocab
            and actually use "role = byline".  for now, we will use just 1 vocabulary in this converter -->
        <citation>
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </citation>
    </xsl:template>

    <xsl:template match="dtb:sent">

        <s>
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </s>
    </xsl:template>

    <xsl:template match="dtb:address">
        <address>
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </address>
    </xsl:template>

    <xsl:template match="dtb:epigraph">
        <block role="epigraph">
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </block>
    </xsl:template>

    <xsl:template match="dtb:dateline">
        <p role="time">
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </p>
    </xsl:template>

    <xsl:template match="tmp:ln">
        <ln>
            <xsl:apply-templates/>
        </ln>
    </xsl:template>

    <xsl:template match="dtb:br">
        <!-- discard any br elements left after running convert-br-to-ln.xsl -->
    </xsl:template>


    <xsl:template match="dtb:author">
        <xsl:variable name="id" as="xs:string">
            <xsl:call-template name="generate-id"/>
        </xsl:variable>
        <!--if standalone author, wrap it in a citation.-->
        <citation xml:id="{$id}">
            <xsl:call-template name="attrs"/>
            <name property="author" about="{$id}">
                <xsl:apply-templates/>
            </name>
        </citation>
    </xsl:template>
    <xsl:template match="dtb:cite">
        <citation>
            <xsl:call-template name="attrs"/>

            <!-- if no ID and one is needed to anchor title|author properties, then give a new ID -->
            <xsl:if test="not(parent::dtb:q or @id)">
                <xsl:call-template name="generate-id"/>
            </xsl:if>

            <xsl:apply-templates/>
        </citation>
    </xsl:template>
    <xsl:template match="dtb:cite/dtb:title">
        <xsl:variable name="about" as="xs:string">
            <xsl:choose>
                <xsl:when test="../parent::dtb:q">
                    <xsl:for-each select="../..">
                        <xsl:call-template name="generate-id"/>
                    </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:for-each select="..">
                        <xsl:call-template name="generate-id"/>
                    </xsl:for-each>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <!-- Anchor to the ancestor quote if it exists, else to the parent cite -->
        <span property="title" about="#{$about}">
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </span>
    </xsl:template>
    <xsl:template match="dtb:cite/dtb:author">
        <xsl:variable name="about" as="xs:string">
            <xsl:choose>
                <xsl:when test="../parent::dtb:q">
                    <xsl:for-each select="../..">
                        <xsl:call-template name="generate-id"/>
                    </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:for-each select="..">
                        <xsl:call-template name="generate-id"/>
                    </xsl:for-each>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <!-- Anchor to the ancestor quote if it exists, else to the parent cite -->
        <name property="author" about="#{$about}">
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </name>
    </xsl:template>


    <xsl:template match="dtb:covertitle">
        <block role="covertitle">
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </block>
    </xsl:template>



    <xsl:template match="dtb:acronym">
        <!-- making an assumption: @pronounce has a default value of 'no' -->
        <abbr role="{if (@pronounce = 'yes') then 'acronym' else 'initialism'}">
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </abbr>
    </xsl:template>

    <!-- link elements live in the head of dtbook documents; there seems to be no zedai equivalent (chances are, whatever they reference is not relevant in a zedai world anyway) -->
    <xsl:template match="dtb:link"/>

    <xsl:template match="dtb:bodymatter">
        <bodymatter>
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </bodymatter>
    </xsl:template>

    <xsl:template match="dtb:p">
        <p>
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </p>
    </xsl:template>


    <xsl:template match="dtb:abbr">

        <abbr role="truncation">
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </abbr>
    </xsl:template>

    <xsl:template match="dtb:sup">
        <sup>
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </sup>
    </xsl:template>

    <xsl:template match="dtb:sub">
        <sub>
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </sub>
    </xsl:template>

    <xsl:template match="dtb:span">
        <span>
            <xsl:call-template name="attrs"/>
            <!-- normalization steps sometimes put role='example' on some spans, so be sure to copy it -->
            <xsl:copy-of select="@role"/>
            <xsl:apply-templates/>
        </span>
    </xsl:template>

    <xsl:template match="dtb:w">
        <w>
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </w>
    </xsl:template>

    <xsl:template
        match="tmp:annotation-block/dtb:linegroup | dtb:caption/dtb:linegroup | dtb:level/dtb:linegroup |
        dtb:level1/dtb:linegroup | dtb:level2/dtb:linegroup | dtb:level3/dtb:linegroup | dtb:level4/dtb:linegroup |
        dtb:level5/dtb:linegroup | dtb:level6/dtb:linegroup | dtb:td/dtb:linegroup | dtb:prodnote/dtb:linegroup |
        dtb:sidebar/dtb:linegroup | dtb:th/dtb:linegroup | dtb:poem/dtb:linegroup">

        <block>
            <xsl:call-template name="attrs"/>
            <xsl:for-each-group group-adjacent="boolean(self::dtb:line)" select="*">
                <xsl:choose>
                    <xsl:when test="current-grouping-key()">
                        <p>
                            <xsl:apply-templates select="current-group()"/>
                        </p>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="current-group()"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each-group>
        </block>

    </xsl:template>


    <xsl:template match="dtb:line">
        <ln>
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </ln>
    </xsl:template>

    <!-- any samp elements left at this point will be block-level, since nested samps were made into spans in earlier steps -->
    <xsl:template match="dtb:samp">

        <block role="example">
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </block>
    </xsl:template>

    <xsl:template match="dtb:dfn">
        <term>
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </term>
    </xsl:template>

    <xsl:template match="dtb:a">
        <xsl:element name="{if (empty(@href) and @smilref) then 'span' else 'ref'}">
            <xsl:call-template name="attrs"/>
            <xsl:if test="@href">
                <xsl:choose>
                    <xsl:when test="@external='true'">
                        <xsl:attribute name="xlink:href" select="@href"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="ref" select="replace(@href, '#', '')"/>
                    </xsl:otherwise>
                </xsl:choose>

            </xsl:if>
            <xsl:copy-of select="@rev"/>
            <xsl:copy-of select="@rel"/>
            <xsl:apply-templates/>

        </xsl:element>
    </xsl:template>

    <xsl:template match="dtb:dl">
        <!-- assumption: definition lists are unordered -->
        <list type="unordered">
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </list>
    </xsl:template>

    <xsl:template match="tmp:item">
        <item>
            <xsl:apply-templates/>
        </item>
    </xsl:template>

    <xsl:variable name="definition-list-block-elems"
        select="tokenize('list,dl,div,poem,linegroup,table,sidebar,note,epigraph', ',')"/>

    <xsl:template match="dtb:dd">
        <xsl:choose>
            <!-- when it has a block-level sibling, wrap in a p element -->
            <xsl:when
                test="preceding-sibling::*/local-name() = $definition-list-block-elems or
                following-sibling::*/local-name() = $definition-list-block-elems">
                <p>
                    <definition>
                        <xsl:call-template name="attrs"/>
                        <xsl:apply-templates/>
                    </definition>
                </p>
            </xsl:when>
            <xsl:otherwise>
                <definition>
                    <xsl:call-template name="attrs"/>
                    <xsl:apply-templates/>
                </definition>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="dtb:dt">
        <xsl:choose>
            <!-- when it has a block-level sibling, wrap in a p element -->
            <xsl:when
                test="preceding-sibling::*/local-name() = $definition-list-block-elems or
                following-sibling::*/local-name() = $definition-list-block-elems">
                <p>
                    <term>
                        <xsl:call-template name="attrs"/>
                        <xsl:apply-templates/>
                    </term>
                </p>
            </xsl:when>
            <xsl:otherwise>
                <term>
                    <xsl:call-template name="attrs"/>
                    <xsl:apply-templates/>
                </term>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="dtb:linenum">
        <lnum>
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </lnum>
    </xsl:template>

    <xsl:template match="dtb:poem">
        <xsl:variable name="id" as="xs:string">
            <xsl:call-template name="generate-id"/>
        </xsl:variable>
        <!--if standalone author, wrap it in a citation.-->
        <block role="poem" xml:id="{$id}">
            <xsl:call-template name="attrs"/>
            <!-- if no ID, then give a new ID -->

            <xsl:for-each-group group-adjacent="boolean(self::dtb:line)" select="*">
                <xsl:choose>
                    <xsl:when test="current-grouping-key()">
                        <p>
                            <xsl:apply-templates select="current-group()"/>
                        </p>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="current-group()"/>
                    </xsl:otherwise>
                </xsl:choose>

            </xsl:for-each-group>
        </block>
    </xsl:template>
    <xsl:template match="dtb:poem/dtb:title">
        <xsl:variable name="poem-id" as="xs:string">
            <xsl:for-each select="..">
                <xsl:call-template name="generate-id"/>
            </xsl:for-each>
        </xsl:variable>
        <!--TODO add @about-->
        <p property="title" about="#{$poem-id}">
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </p>
    </xsl:template>
    <xsl:template match="dtb:poem/dtb:author">
        <xsl:variable name="poem-id" as="xs:string">
            <xsl:for-each select="..">
                <xsl:call-template name="generate-id"/>
            </xsl:for-each>
        </xsl:variable>
        <p>
            <name property="author" about="#{$poem-id}">
                <xsl:call-template name="attrs"/>
                <xsl:apply-templates/>
            </name>
        </p>
    </xsl:template>


    <xsl:template match="tmp:code-block">
        <code>
            <xsl:call-template name="attrs"/>
            <xsl:for-each-group group-adjacent="boolean(self::dtb:em|self::dtb:strong|self::dtb:dfn|self::dtb:cite|self::dtb:abbr|self::dtb:acronym|self::dtb:a|self::dtb:sub|self::dtb:sup|self::dtb:span|self::dtb:bdo|self::dtb:w|self::dtb:annoref|self::dtb:noteref|self::dtb:sent|self::dtb:code-phrase|self::text())"
                select="node() except text()[normalize-space() = '']">
                <xsl:choose>
                    <xsl:when test="current-grouping-key()">
                        <p>
                           <xsl:apply-templates select="current-group()"/>
                        </p>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="current-group()" mode="code-block"/>
                    </xsl:otherwise>

                </xsl:choose>
            </xsl:for-each-group>
        </code>
    </xsl:template>
    <!-- explicitly ignore linebreaks when treating code as a group of block-level items -->
    <xsl:template match="tmp:code-block/dtb:br" mode="code-block"/>
    <xsl:template match="tmp:code-block/*[self::dtb:code-block|self::dtb:q|self::dtb:prodnote]" mode="code-block">
        <block>
            <xsl:apply-templates select="." />
        </block>
    </xsl:template>
    <xsl:template match="*" mode="code-block">
        <xsl:apply-templates select="."/>
    </xsl:template>



    <xsl:template match="tmp:code-phrase">
        <code>
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </code>
    </xsl:template>
    <xsl:template match="tmp:code-phrase/dtb:abbr">
        <span role="truncation">
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </span>
    </xsl:template>
    <xsl:template match="tmp:code-phrase/dtb:acronym">
        <span role="{if (@pronounce='yes') then 'acronym' else 'initialism'}">
            <xsl:call-template name="attrs"/>
            <xsl:apply-templates/>
        </span>
    </xsl:template>
    <xsl:template match="tmp:code-phrase/dtb:br"/>

    <!--TODO move to external common utils implementation if/when UTFX support catalogs-->
    <xsl:function name="f:roman-to-decimal" as="xs:integer">
        <xsl:param name="roman" as="xs:string"/>
        <!-- TODO: throw error for strings containing characters other than MDCLXVI (case insensitive), the seven characters still in use. -->
        <xsl:variable name="hindu-sequence"
            select="for $char in string-to-codepoints($roman) return
            number(replace(replace(replace(replace(replace(replace(replace(upper-case(codepoints-to-string($char)),'I','1'),'V','5'),'X','10'),'L','50'),'C','100'),'D','500'),'M','1000'))"/>
        <xsl:variable name="hindu-sequence-signed"
            select="for $i in 1 to count($hindu-sequence) return if (subsequence($hindu-sequence,$i+1) &gt; $hindu-sequence[$i]) then -$hindu-sequence[$i] else $hindu-sequence[$i]"/>
        <xsl:value-of select="sum($hindu-sequence-signed)"/>
    </xsl:function>
    <xsl:function name="f:alpha-to-decimal" as="xs:integer">
        <xsl:param name="alpha" as="xs:string"/>
        <xsl:message select="$alpha"/>
        <xsl:sequence select="string-to-codepoints(lower-case($alpha))-96"/>
    </xsl:function>

</xsl:stylesheet>
