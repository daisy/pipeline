<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:tts="http://www.daisy.org/ns/pipeline/tts"
                xmlns="http://www.w3.org/1999/xhtml"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

    <xsl:param name="output-base-uri" required="yes"/>

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" omit-xml-declaration="no"/>

    <!-- <xsl:strip-space elements="*"/> -->

    <xsl:template match="/">
        <xsl:message>Creating outline from html5</xsl:message>
        <!-- For debugging
        <xsl:variable name="document">
            <xsl:copy-of select="*"/>
        </xsl:variable>
        <xsl:message select='$document'/> -->

        <xsl:apply-templates/>
    </xsl:template>

    <!-- On the html tag found -->
    <xsl:template match="html">
        <xsl:variable name="input-base-uri" select="base-uri(.)"/>
        <xsl:variable name="relative-path" select="pf:relativize-uri($input-base-uri, $output-base-uri)"/>
        <!-- Store a filtered version of the body in "filtered" 
        This versions only contains the headers sections-->
        <xsl:variable name="filtered">
            <xsl:apply-templates select="body" mode="filtering"/>
        </xsl:variable>
        <!-- Reconstruct an ordered list of filtered content -->
        <ol>
            <xsl:apply-templates select="$filtered/*">
                <xsl:with-param name="relative-path" tunnel="yes" select="$relative-path"/>
            </xsl:apply-templates>
        </ol>
    </xsl:template>

    <!--  -->
    <xsl:template match="body|article|aside|nav|section">
        <xsl:param name="relative-path" tunnel="yes" as="xs:string" required="yes"/>
        <xsl:variable name="id" select="@id"/>
        <xsl:variable name="heading" select="(h1|h2|h3|h4|h5|h6|hgroup)[1]" as="element()?"/>
        <xsl:variable name="heading-content" select="f:heading-content($heading,.)" as="item()*"/>
        <xsl:variable name="children-doc">
            <xsl:copy-of select="* except $heading"/>
        </xsl:variable>
        <xsl:variable name="children" select="$children-doc/*" as="element()*"/>
        <!-- <xsl:message select="concat('section: ',name())"/> -->
        <xsl:choose>
            <!-- When there are no children or the first children -->
            <xsl:when
                test="empty($children) or $children[1][f:is-heading(.) and f:rank(.) >= f:rank($heading)]">
                <!-- <xsl:message select="concat('heading only: ',$heading)"/> -->
                <li>
                    <xsl:if test="empty($heading) or not($heading/descendant-or-self::text())">
                        <xsl:attribute name="data-generated" select="'true'"/>
                    </xsl:if>
                    <a href="{$relative-path}#{$id}">
                        <!-- TODO: try to not "depend" on the TTS namespace here -->
                        <xsl:copy-of select="$heading/ancestor-or-self::*/@tts:*"/>
                        <xsl:copy-of select="$heading-content"/>
                    </a>
                </li>
            </xsl:when>
        </xsl:choose>
        <xsl:for-each-group select="$children"
            group-starting-with="*[f:is-heading(.) and f:rank(.) >= f:rank($heading) and not(f:rank(preceding-sibling::*[1]) > f:rank(.))]">
            <!-- <xsl:message select="$children" /> -->
            <xsl:choose>
                <xsl:when
                    test="position()=1 and not(f:is-heading(.) and f:rank(.) ge f:rank($heading))">
                    <!-- <xsl:message select="concat('heading and subsections: ',$heading, ' ', normalize-space($heading-content))"/> -->
                    <!-- https://github.com/daisy/pipeline-tasks/issues/125 :
                        an empty heading leads to an empty entry after the nav-fixer process, which leads to an invalid epub according to epubcheck.
                        The corresponding entries are now also marked as "data-generated" to be identified by the nav-fixer as removable -->
                    <li>
                        <xsl:if test="empty($heading) or not($heading/descendant-or-self::text())">
                            <xsl:attribute name="data-generated" select="'true'"/>
                        </xsl:if>
                        <a href="{$relative-path}#{$id}">
                            <!-- TODO: try to not "depend" on the TTS namespace here -->
                            <xsl:copy-of select="$heading/ancestor-or-self::*/@tts:*"/>
                            <xsl:copy-of select="$heading-content"/>
                        </a>
                        <ol>
                            <xsl:call-template name="subsections">
                                <xsl:with-param name="elems" select="current-group()"/>
                            </xsl:call-template>
                        </ol>
                    </li>
                </xsl:when>
                <xsl:otherwise>
                    <!--implicit section-->
                    <xsl:call-template name="implicit-section">
                        <xsl:with-param name="elems" select="current-group()"/>
                    </xsl:call-template>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each-group>
    </xsl:template>

    <xsl:template name="implicit-section">
        <xsl:param name="elems" as="element()*"/>
        <xsl:param name="relative-path" tunnel="yes" as="xs:string" required="yes"/>
        <xsl:variable name="children-doc">
            <xsl:copy-of select="$elems[position()>1]"/>
        </xsl:variable>
        <xsl:variable name="children" select="$children-doc/*" as="element()*"/>
        <!-- <xsl:message select="concat('implicit section ',$elems[1])"/> -->
        <li>
            <xsl:if test="$elems[1]/empty(self::h1|self::h2|self::h3|self::h4|self::h5|self::h6|self::hgroup)">
                <xsl:attribute name="data-generated" select="'true'"/>
            </xsl:if>
            <a href="{$relative-path}#{$elems[1]/@id}">
                <xsl:copy-of select="f:heading-content($elems[1],())"/>
            </a>
            <!-- Check if a real children exists (children with text node) -->
            <xsl:if test="$children">
                <ol>
                    <xsl:call-template name="subsections">
                        <xsl:with-param name="elems" select="$children"/>
                    </xsl:call-template>
                </ol>
            </xsl:if>
        </li>
    </xsl:template>

    <!-- -->
    <xsl:template name="subsections">
        <xsl:param name="elems" as="element()*"/>
        <!-- <xsl:message select="concat('subsections: ',string-join($elems/name(),' '))"/> -->

        <xsl:for-each-group select="$elems" group-adjacent="f:is-heading(.)">
            <!-- <xsl:message select="concat('group: ',string-join(current-group()/name(),' '))"/> -->
            <xsl:choose>
                <xsl:when test="f:is-heading(.)">
                    <xsl:for-each-group select="current-group()"
                        group-starting-with="*[f:rank(.) >= max(preceding-sibling::*/f:rank(.))]">
                        <xsl:call-template name="implicit-section">
                            <xsl:with-param name="elems" select="current-group()"/>
                        </xsl:call-template>
                    </xsl:for-each-group>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="current-group()"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each-group>

    </xsl:template>

    <!-- On body|article|aside|nav|section matched tags in filtering context, 
        construct a "filtered copy" of the tag and its attributes and content -->
    <xsl:template match="body|article|aside|nav|section" mode="filtering">
        <xsl:copy>
            <!-- Copy the attributes -->
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="*" mode="filtering"/>
        </xsl:copy>
    </xsl:template>


    <!-- On h1|h2|h3|h4|h5|h6|hgroup matched tags in filtering context, 
        construct a copy of node with attributes (and including previous TTS attributes) and content -->
    <xsl:template match="h1|h2|h3|h4|h5|h6|hgroup" mode="filtering">
        <xsl:copy>
            <!-- TODO: try to not "depend" on the TTS namespace here -->
            <xsl:copy-of select="@*|ancestor-or-self::*/@tts:*|node()"/>
        </xsl:copy>
    </xsl:template>
    <!-- on blockquote|details|fieldset|figure|td in filtering mode, 
        do nothing -->
    <xsl:template match="blockquote|details|fieldset|figure|td" mode="filtering"/>

    <!-- on every not previously matched elements in filtering mode, 
        Search and apply next template in filtering mode -->
    <xsl:template match="*" mode="filtering">
        <xsl:apply-templates select="*" mode="filtering"/>
    </xsl:template>

    <!-- Check if node is a header -->
    <xsl:function name="f:is-heading" as="xs:boolean">
        <xsl:param name="node" as="element()"/>
        <xsl:sequence
            select="boolean($node[self::h1 or self::h2 or self::h3 or self::h4 or self::h5 or self::h6 or self::hgroup])"
        />
    </xsl:function>
    <!-- -->
    <xsl:function name="f:rank" as="xs:integer">
        <xsl:param name="node" as="element()?"/>
        <xsl:choose>
            <xsl:when
                test="$node[self::h1 or self::h2 or self::h3 or self::h4 or self::h5 or self::h6]">
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
    
    <!-- -->
    <xsl:function name="f:heading-content" as="item()*">
        <xsl:param name="node" as="element()?"/>
        <xsl:param name="parent" as="element()?"/>
        <xsl:choose>
            <xsl:when
                test="$node[self::h1 or self::h2 or self::h3 or self::h4 or self::h5 or self::h6]/descendant-or-self::text()">
                <xsl:apply-templates select="$node/(*|text())" mode="heading-content"/>
            </xsl:when>
            <xsl:when test="$node[self::hgroup]">
                <xsl:variable name="rank" select="f:rank($node)"/>
                <xsl:sequence select="f:heading-content($node/*[f:rank(.)=$rank][1],$parent)"/>
            </xsl:when>
            <xsl:when test="$parent[self::body]">
                <xsl:sequence select="'Untitled document'"/>
            </xsl:when>
            <xsl:when test="$parent[self::article]">
                <xsl:sequence select="'Article'"/>
            </xsl:when>
            <xsl:when test="$parent[self::aside]">
                <xsl:sequence select="'Sidebar'"/>
            </xsl:when>
            <xsl:when test="$parent[self::nav]">
                <xsl:sequence select="'Navigation'"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="'Untitled section'"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!-- On anchor matched in heading content context, 
        Create a span with the copy of @class|@dir|@lang|@title attributes and with its content -->
    <xsl:template match="a" mode="heading-content">
        <span>
            <xsl:copy-of select="@class|@dir|@lang|@title"/>
            <xsl:apply-templates mode="heading-content"/>
        </span>
    </xsl:template>
    
    <!-- -->
    <xsl:template match="node() | @*" mode="heading-content">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*" mode="heading-content"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
