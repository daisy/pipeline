<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:tts="http://www.daisy.org/ns/pipeline/tts"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns="http://www.w3.org/1999/xhtml"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all">

    <!--
        The HTML5 outline algorithm: https://html.spec.whatwg.org/multipage/sections.html#headings-and-sections

        Terminology:
        * outline: https://html.spec.whatwg.org/multipage/sections.html#outline
        * section: https://html.spec.whatwg.org/multipage/sections.html#concept-section
        * heading content elements: https://html.spec.whatwg.org/multipage/dom.html#heading-content-2
        * sectioning content elements: https://html.spec.whatwg.org/multipage/dom.html#sectioning-content-2
        * sectioning root elements: https://html.spec.whatwg.org/multipage/sections.html#sectioning-root
        * rank: https://html.spec.whatwg.org/multipage/sections.html#rank
    -->

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
    <xsl:include href="untitled-section-titles.xsl"/>

    <xsl:param name="output-base-uri" required="yes"/>
    <xsl:param name="heading-links-only" required="yes"/>
    <xsl:param name="fix-untitled-sections-in-outline" required="yes"/> <!-- imply-heading | unwrap -->

    <xsl:key name="id" match="*" use="@id"/>

    <xsl:template match="/">
        <!-- Create the outline -->
        <xsl:variable name="outline" as="element(d:outline)">
            <xsl:call-template name="get-outline"/>
        </xsl:variable>
        <!-- Create the list -->
        <xsl:call-template name="format-outline">
            <xsl:with-param name="outline" select="$outline"/>
        </xsl:call-template>
        <!-- Return outline on secondary port -->
        <xsl:result-document method="xml" href="outline">
            <xsl:sequence select="$outline"/>
        </xsl:result-document>
    </xsl:template>

    <xsl:template name="get-outline" as="element(d:outline)">
        <!-- Create a stripped down version of the body with only the sectioning and heading content
             elements -->
        <xsl:variable name="filtered" as="element(body)">
            <xsl:apply-templates mode="filter" select="/html/body"/>
        </xsl:variable>
        <!-- Create the outline -->
        <xsl:apply-templates mode="outline" select="$filtered"/>
    </xsl:template>

    <xsl:template name="format-outline">
        <xsl:param name="outline" as="element(d:outline)" required="yes"/>
        <xsl:variable name="input-base-uri" select="base-uri(/*)"/>
        <xsl:variable name="relative-path" select="pf:relativize-uri($input-base-uri, $output-base-uri)"/>
        <xsl:variable name="ol" as="element(ol)">
            <ol>
                <xsl:apply-templates select="$outline">
                    <xsl:with-param name="html-doc" tunnel="yes" select="root()"/>
                    <xsl:with-param name="relative-path" tunnel="yes" select="$relative-path"/>
                </xsl:apply-templates>
            </ol>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$fix-untitled-sections-in-outline='unwrap'">
                <!-- Because we might be left with empty ol elements -->
                <xsl:apply-templates mode="clean" select="$ol"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="$ol"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- for XSpec tests -->
    <xsl:template match="html">
        <!-- Create the outline -->
        <xsl:variable name="outline" as="element(d:outline)">
            <xsl:call-template name="get-outline"/>
        </xsl:variable>
        <!-- Create the list -->
        <xsl:call-template name="format-outline">
            <xsl:with-param name="outline" select="$outline"/>
        </xsl:call-template>
    </xsl:template>

    <!-- ========== -->
    <!-- FIRST PASS -->
    <!-- ========== -->

    <!-- Copy the body element including its attributes. -->
    <xsl:template mode="filter" match="body">
        <xsl:copy>
            <xsl:sequence select="@*"/>
            <xsl:apply-templates mode="#current" select="*"/>
        </xsl:copy>
    </xsl:template>

    <!-- Ignore other sectioning root elements. They would start their own outlines that don't
         contribute to the main outline. -->
    <xsl:template mode="filter" match="blockquote|details|fieldset|figure|td"/>

    <!-- Copy sectioning content elements including their attributes. -->
    <xsl:template mode="filter" match="article|aside|nav|section">
        <xsl:copy>
            <xsl:sequence select="@*"/>
            <xsl:apply-templates mode="#current" select="*"/>
        </xsl:copy>
    </xsl:template>

    <!-- Copy heading content elements including their content and attributes (and ancestors' "TTS"
         attributes). -->
    <xsl:template mode="filter" match="h1|h2|h3|h4|h5|h6|hgroup">
        <xsl:copy>
            <xsl:sequence select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template mode="filter" match="*">
        <xsl:apply-templates mode="#current" select="*"/>
    </xsl:template>

    <!-- =========== -->
    <!-- SECOND PASS -->
    <!-- =========== -->

    <!-- A sectioning root or sectioning content element creates a new outline with one or more
         sections. -->
    <xsl:template mode="outline" match="body|
                                        article|aside|nav|section">
        <d:outline owner="{@id}">
            <xsl:call-template name="sections">
                <xsl:with-param name="outline-owner" select="."/>
            </xsl:call-template>
        </d:outline>
    </xsl:template>

    <!-- Create one or more sections from a sequence of sectioning and heading content elements. -->
    <xsl:template name="sections" as="element(d:section)*">
        <!-- If the resulting sections are to be added to an outline, this is that outline's
             owner. It will be associated with the first section. -->
        <xsl:param name="outline-owner" as="element()?" select="()"/>
        <!-- The sectioning or heading content elements currently in scope and from which the
             sections will be created. -->
        <xsl:param name="content" as="element()*" select="$outline-owner/*"/>
        <xsl:choose>
            <xsl:when test="not(exists($outline-owner)) and not(exists($content) and f:is-heading($content[1]))">
                <xsl:message terminate="yes">coding error</xsl:message>
            </xsl:when>
            <xsl:when test="not(exists($content))">
                <!-- Section has no heading associated -->
                <d:section owner="{$outline-owner/@id}"/>
            </xsl:when>
            <xsl:otherwise>
                <!-- The first heading content element before the first sectioning content element
                     is the heading content element associated with the first section. -->
                <xsl:variable name="first-heading" as="element()?"
                              select="$content[f:is-heading(.)][1]
                                              [not(preceding-sibling::*[f:is-section(.)] intersect $content)]"/>
                <!-- Every heading content element that is not preceded by a heading with higher
                     rank creates a new implied section. -->
                <xsl:for-each-group select="$content"
                                    group-starting-with="*[f:is-heading(.)]
                                                          [f:rank(.) >= max((0,(preceding-sibling::* intersect $content)/f:rank(.)))]">
                    <xsl:choose>
                        <!-- The first group is the section that the outline's owner will be
                             associated with. -->
                        <xsl:when test="position()=1">
                            <d:section>
                                <xsl:if test="exists($outline-owner)">
                                    <xsl:attribute name="owner" select="$outline-owner/@id"/>
                                </xsl:if>
                                <xsl:if test="exists($first-heading)">
                                    <xsl:attribute name="heading"
                                                   select="($first-heading/@id,
                                                            $first-heading[not(preceding-sibling::*)]/parent::*/@id)[1]"/>
                                </xsl:if>
                                <!-- The section has subsections if the heading is followed by
                                     sectioning content elements or heading content elements (with a
                                     lower rank). -->
                                <xsl:for-each-group select="current-group() except $first-heading"
                                                    group-adjacent="f:is-section(.)">
                                    <xsl:choose>
                                        <xsl:when test="f:is-section(.)">
                                            <!-- Sectioning content elements create direct subsections. -->
                                            <xsl:apply-templates mode="#current" select="current-group()"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <!-- Heading content elements create implied sections -->
                                            <xsl:call-template name="sections">
                                                <xsl:with-param name="content" select="current-group()"/>
                                            </xsl:call-template>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:for-each-group>
                            </d:section>
                        </xsl:when>
                        <xsl:otherwise>
                            <!-- The other groups are implied sections -->
                            <xsl:call-template name="sections">
                                <xsl:with-param name="content" select="current-group()"/>
                            </xsl:call-template>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each-group>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ========== -->
    <!-- THIRD PASS -->
    <!-- ========== -->

    <xsl:template match="d:outline">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <!-- When a section has no associated heading content elements, it gets an implied heading. -->
    <xsl:template match="d:section">
        <xsl:param name="html-doc" tunnel="yes" as="document-node()" required="yes"/>
        <xsl:param name="relative-path" tunnel="yes" as="xs:string" required="yes"/>
        <xsl:if test="not(@owner) and not(@heading)">
            <xsl:message terminate="yes">coding error</xsl:message>
        </xsl:if>
        <xsl:variable name="heading" as="element()?" select="key('id',@heading,$html-doc)"/>
        <xsl:variable name="heading" as="element()?" select="for $h in $heading return
                                                             if (f:is-heading($h))
                                                               then $h
                                                               else $h/*[f:is-heading(.)][1]"/>
        <xsl:choose>
            <xsl:when test="not($heading/descendant-or-self::text())
                            and $fix-untitled-sections-in-outline='unwrap'">
                <xsl:apply-templates select="*"/>
            </xsl:when>
            <xsl:otherwise>
                <li>
                    <xsl:element namespace="http://www.w3.org/1999/xhtml"
                                 name="{if ($heading-links-only='true' and not(@heading))
                                        then 'span'
                                        else 'a'}">
                        <xsl:choose>
                            <xsl:when test="$heading-links-only='true'">
                                <xsl:if test="@heading">
                                    <xsl:attribute name="href" select="concat($relative-path,'#',@heading)"/>
                                </xsl:if>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:attribute name="href" select="concat($relative-path,'#',(@owner,@heading)[1])"/>
                            </xsl:otherwise>
                        </xsl:choose>
                        <!-- FIXME: try to not "depend" on the TTS namespace here -->
                        <xsl:sequence select="$heading/ancestor-or-self::*/@tts:*"/>
                        <xsl:choose>
                            <xsl:when test="$heading/descendant-or-self::text()">
                                <!-- Get the content of the associated heading content element. -->
                                <xsl:choose>
                                    <xsl:when test="$heading[self::h1 or
                                                             self::h2 or
                                                             self::h3 or
                                                             self::h4 or
                                                             self::h5 or
                                                             self::h6]">
                                        <xsl:apply-templates select="$heading/(*|text())"/>
                                    </xsl:when>
                                    <xsl:when test="$heading[self::hgroup]">
                                        <!-- Get the content of the child that gives this hgroup its rank. -->
                                        <xsl:variable name="rank" select="f:rank($heading)"/>
                                        <xsl:variable name="heading" select="$heading/*[f:rank(.)=$rank][1]"/>
                                        <xsl:apply-templates select="$heading/(*|text())"/>
                                    </xsl:when>
                                </xsl:choose>
                            </xsl:when>
                            <xsl:when test="@owner">
                                <!-- If the section has no associated heading, create implied heading -->
                                <!-- An empty entry leads to an invalid EPUB according to epubcheck, so
                                     treat an empty heading as an absent heading. -->
                                <xsl:variable name="owner" as="element()" select="key('id',@owner,$html-doc)"/>
                                <xsl:choose>
                                    <xsl:when test="$owner/@aria-label">
                                        <xsl:value-of select="$owner/@aria-label"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:call-template name="get-untitled-section-title">
                                            <xsl:with-param name="sectioning-element" select="$owner"/>
                                        </xsl:call-template>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:sequence select="'Untitled section'"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:element>
                    <xsl:if test="exists(*)">
                        <ol>
                            <xsl:apply-templates select="*"/>
                        </ol>
                    </xsl:if>
                </li>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- For the outline document convert anchor elements to span and take only the
         @class|@dir|@lang|@title attributes. -->
    <xsl:template match="a">
        <xsl:variable name="content" as="node()*">
            <xsl:apply-templates/>
        </xsl:variable>
        <xsl:if test="exists($content)">
            <span>
                <xsl:copy-of select="@class|@dir|@lang|@title"/>
                <xsl:sequence select="$content"/>
            </span>
        </xsl:if>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- =========== -->
    <!-- FOURTH PASS -->
    <!-- =========== -->

    <xsl:template mode="clean" match="ol[.. and not(li)]"/>

    <xsl:template mode="clean" match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- ========= -->
    <!-- FUNCTIONS -->
    <!-- ========= -->

    <!-- Check if an element is a heading content element. -->
    <xsl:function name="f:is-heading" as="xs:boolean">
        <xsl:param name="node" as="element()"/>
        <xsl:sequence select="boolean($node[self::h1 or
                                            self::h2 or
                                            self::h3 or
                                            self::h4 or
                                            self::h5 or
                                            self::h6 or
                                            self::hgroup])"/>
    </xsl:function>

    <!-- Check if an element is a sectioning content element. -->
    <xsl:function name="f:is-section" as="xs:boolean">
        <xsl:param name="node" as="element()"/>
        <xsl:sequence select="boolean($node[self::article or
                                            self::aside or
                                            self::nav or
                                            self::section])"/>
    </xsl:function>

    <!-- Get a number representing the rank of a heading content element. The number is highest for
         h1 (rank 1) and lowest for h6 (rank 6). The rank of an hgroup element is the rank of the
         highest-ranked h1–h6 element descendant of the hgroup element, if there are any such
         elements, or otherwise the same as for an h1 element. -->
    <xsl:function name="f:rank" as="xs:integer">
        <xsl:param name="node" as="element()?"/>
        <xsl:choose>
            <xsl:when test="$node[self::h1 or
                                  self::h2 or
                                  self::h3 or
                                  self::h4 or
                                  self::h5 or
                                  self::h6]">
                <xsl:sequence select="7-xs:integer(substring-after(name($node),'h'))"/>
            </xsl:when>
            <xsl:when test="$node[self::hgroup]">
                <xsl:sequence select="max($node/*/f:rank(.))"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="0"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

</xsl:stylesheet>
