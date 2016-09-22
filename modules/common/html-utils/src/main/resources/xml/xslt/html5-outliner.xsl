<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.w3.org/1999/xhtml" xpath-default-namespace="http://www.w3.org/1999/xhtml"
    xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:tts="http://www.daisy.org/ns/pipeline/tts"
    exclude-result-prefixes="#all">

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" omit-xml-declaration="no"/>

    <xsl:template match="html">
        <xsl:variable name="filtered">
            <xsl:apply-templates select="body" mode="filtering"/>
        </xsl:variable>
        <ol>
            <xsl:apply-templates select="$filtered/*"/>
        </ol>
    </xsl:template>

    <xsl:template match="body|article|aside|nav|section">
        <xsl:variable name="id" select="@id"/>
        <xsl:variable name="heading" select="(h1|h2|h3|h4|h5|h6|hgroup)[1]" as="element()?"/>
        <xsl:variable name="heading-content" select="f:heading-content($heading,.)" as="item()*"/>
        <xsl:variable name="children-doc">
            <xsl:copy-of select="* except $heading"/>
        </xsl:variable>
        <xsl:variable name="children" select="$children-doc/*" as="element()*"/>
        <!--        <xsl:message select="concat('section: ',name())"/>-->
        <xsl:choose>
            <xsl:when
                test="empty($children) or $children[1][f:is-heading(.) and f:rank(.) >= f:rank($heading)]">
                <!--                <xsl:message select="concat('heading only: ',$heading)"/>-->
                <li>
                    <xsl:if test="empty($heading)">
                        <xsl:attribute name="data-generated" select="'true'"/>
                    </xsl:if>
                    <a href="#{$id}">
                        <!-- TODO: try to not "depend" on the TTS namespace here -->
                        <xsl:copy-of select="$heading/ancestor-or-self::*/@tts:*"/>
                        <xsl:copy-of select="$heading-content"/>
                    </a>
                </li>
            </xsl:when>
        </xsl:choose>
        <xsl:for-each-group select="$children"
            group-starting-with="*[f:is-heading(.) and f:rank(.) >= f:rank($heading) and not(f:rank(preceding-sibling::*[1]) > f:rank(.))]">
            <xsl:choose>
                <xsl:when
                    test="position()=1 and not(f:is-heading(.) and f:rank(.) ge f:rank($heading))">
                    <!--                    <xsl:message select="concat('heading and subsections: ',$heading)"/>-->
                    <li>
                        <xsl:if test="empty($heading)">
                            <xsl:attribute name="data-generated" select="'true'"/>
                        </xsl:if>
                        <a href="#{$id}">
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
        <xsl:variable name="children-doc">
            <xsl:copy-of select="$elems[position()>1]"/>
        </xsl:variable>
        <xsl:variable name="children" select="$children-doc/*" as="element()*"/>
        <!--        <xsl:message select="concat('implicit section ',$elems[1])"/>-->
        <li>
            <xsl:if test="$elems[1]/empty(self::h1|self::h2|self::h3|self::h4|self::h5|self::h6|self::hgroup)">
                <xsl:attribute name="data-generated" select="'true'"/>
            </xsl:if>
            <a href="#{$elems[1]/@id}">
                <xsl:copy-of select="f:heading-content($elems[1],())"/>
            </a>
            <xsl:if test="$children">
                <ol>
                    <xsl:call-template name="subsections">
                        <xsl:with-param name="elems" select="$children"/>
                    </xsl:call-template>
                </ol>
            </xsl:if>
        </li>
    </xsl:template>
    <xsl:template name="subsections">
        <xsl:param name="elems" as="element()*"/>
        <!--        <xsl:message select="concat('subsections: ',string-join($elems/name(),' '))"/>-->

        <xsl:for-each-group select="$elems" group-adjacent="f:is-heading(.)">
            <!--            <xsl:message select="concat('group: ',string-join(current-group()/name(),' '))"/>-->
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

    <xsl:template match="body|article|aside|nav|section" mode="filtering">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="*" mode="filtering"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="h1|h2|h3|h4|h5|h6|hgroup" mode="filtering">
        <xsl:copy>
            <!-- TODO: try to not "depend" on the TTS namespace here -->
            <xsl:copy-of select="@*|ancestor-or-self::*/@tts:*|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="blockquote|details|fieldset|figure|td" mode="filtering"/>
    <xsl:template match="*" mode="filtering">
        <xsl:apply-templates select="*" mode="filtering"/>
    </xsl:template>

    <xsl:function name="f:is-heading" as="xs:boolean">
        <xsl:param name="node" as="element()"/>
        <xsl:sequence
            select="boolean($node[self::h1 or self::h2 or self::h3 or self::h4 or self::h5 or self::h6 or self::hgroup])"
        />
    </xsl:function>
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
    <xsl:function name="f:heading-content" as="item()*">
        <xsl:param name="node" as="element()?"/>
        <xsl:param name="parent" as="element()?"/>
        <xsl:choose>
            <xsl:when
                test="$node[self::h1 or self::h2 or self::h3 or self::h4 or self::h5 or self::h6]">
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

    <xsl:template match="a" mode="heading-content">
        <span>
            <xsl:copy-of select="@class|@dir|@lang|@title"/>
            <xsl:apply-templates mode="heading-content"/>
        </span>
    </xsl:template>
    <xsl:template match="node() | @*" mode="heading-content">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*" mode="heading-content"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
