<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://www.w3.org/1999/xhtml"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all">

    <!--
        When semantic elements are converted to generic ones; add a class mirroring the original name of the element.
        For instance, <aside/> becomes <div class="aside"/>.
    -->
    <xsl:param name="add-semantic-classes" select="'true'"/>

    <!--
        Replace elements like audio and video with links to the media.
        For instance <audio><source src="a.ogg"/><source src="b.mp3"/></audio> becomes <a href="a.ogg">a.ogg</a>.
        If false; the media will be completely removed.
    -->
    <xsl:param name="link-to-media" select="'true'"/>

    <xsl:output indent="yes" exclude-result-prefixes="#all"/>

    <xsl:variable name="all-ids" select="//@id"/>

    <xsl:template match="text()|comment()">
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template match="*">
        <xsl:element name="{if (f:is-phrasing(.)) then 'span' else 'div'}">
            <xsl:call-template name="attrs">
                <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'unknown-element' else ()" tunnel="yes"/>
            </xsl:call-template>
            <xsl:attribute name="style" select="string-join((@style,'display:none;'),' ')"/>
            <xsl:comment select="concat(' No template for element: ',name(),' ')"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="html:style"/>
    <xsl:template name="coreattrs">
        <xsl:param name="except" tunnel="yes"/>

        <xsl:copy-of select="(@id|@title|@xml:space)[not(name()=$except)]"/>
        <xsl:call-template name="classes"/>
    </xsl:template>

    <xsl:template name="i18n">
        <xsl:param name="except" tunnel="yes"/>

        <xsl:copy-of select="(@dir|@lang)[not(name()=$except)]"/>
    </xsl:template>

    <xsl:template name="classes">
        <xsl:param name="classes" select="()" tunnel="yes"/>
        <xsl:param name="except" tunnel="yes" select="()"/>
        <xsl:param name="except-classes" tunnel="yes" select="()"/>

        <xsl:if test="not($except-classes='*')">

            <xsl:variable name="old-classes" select="f:classes(.)"/>

            <xsl:if test="not('_class'=$except)">
                <xsl:variable name="class-string" select="string-join(distinct-values(($classes, $old-classes)[not(.='') and not(.=$except-classes)]),' ')"/>
                <xsl:if test="not($class-string='')">
                    <xsl:attribute name="class" select="$class-string"/>
                </xsl:if>
            </xsl:if>

        </xsl:if>
    </xsl:template>

    <xsl:template name="attrs">
        <xsl:call-template name="coreattrs"/>
        <xsl:call-template name="i18n"/>

        <!-- might as well invoke events here instead of on a per-element basis; if these are allowed in HTML4, they should be allowed in HTML5 and vice versa. -->
        <xsl:call-template name="events"/>
    </xsl:template>

    <xsl:template name="events">
        <xsl:copy-of
            select="@onload|@onunload|@onblur|@onchange|@onfocus|@onselect|@onsubmit|@onkeydown|@onkeypress|@onkeyup|@onclick|@ondblclick|@onmousedown|@onmousemove|@onmouseout|@onmouseover|@onmouseup|@onabort"
        />
    </xsl:template>

    <xsl:template name="attrsrqd">
        <xsl:param name="except" tunnel="yes"/>

        <xsl:copy-of select="(@id|@title|@xml:space)[not(name()=$except)]"/>
        <xsl:call-template name="classes"/>
        <xsl:call-template name="i18n"/>
    </xsl:template>

    <xsl:template match="html:html">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.html"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.html">
        <xsl:call-template name="i18n"/>
    </xsl:template>

    <xsl:template match="html:head">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.head"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.head">
        <xsl:call-template name="i18n"/>
        <xsl:if test="html:link[@rel='profile' and @href]">
            <xsl:attribute name="profile" select="(html:link[@rel='profile'][1])/@href"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="html:title">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.title"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.title">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:link[@rel='profile' and @href]"/>
    <xsl:template match="html:link">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.link"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.link">
        <xsl:call-template name="attrs"/>
        <xsl:copy-of select="@href|@hreflang|@type|@rel|@media"/>
        <!-- @sizes are dropped -->
    </xsl:template>

    <xsl:template match="html:meta">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.meta"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.meta">
        <xsl:call-template name="i18n"/>
        <xsl:copy-of select="@http-equiv|@name|@content"/>
        <xsl:if test="@charset">
            <xsl:attribute name="http-equiv" select="'Content-Type'"/>
            <xsl:attribute name="content" select="concat('text/html; charset=',@charset)"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="html:body">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.body"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.body">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:section">
        <div>
            <xsl:call-template name="attlist.section"/>
            <xsl:apply-templates select="node()"/>
        </div>
    </xsl:template>

    <xsl:template name="attlist.section">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'section' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:article">
        <div>
            <xsl:call-template name="attlist.article"/>
            <xsl:apply-templates select="node()"/>
        </div>
    </xsl:template>

    <xsl:template name="attlist.article">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'section' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:br">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.br"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.br">
        <xsl:call-template name="coreattrs"/>
    </xsl:template>

    <xsl:template match="html:address">
        <p>
            <xsl:call-template name="attlist.address"/>
            <xsl:apply-templates select="node()"/>
        </p>
    </xsl:template>

    <xsl:template name="attlist.address">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'address' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:div">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.div"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.div">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:a">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.a"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.a">
        <xsl:call-template name="attrs"/>
        <xsl:copy-of select="@href|@hreflang|@rel|@target"/>
        <!-- @download, @media and @type is dropped - they don't have a good equivalent in HTML4 -->
    </xsl:template>

    <xsl:template match="html:em">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.em"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.em">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:strong">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.strong"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.strong">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:dfn">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.dfn"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.dfn">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:kbd">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.kbd"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.kbd">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:code">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.code"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.code">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:samp">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.samp"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.samp">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:cite">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.cite"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.cite">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:abbr">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.abbr"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.abbr">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:sub">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.sub"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.sub">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:sup">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.sup"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.sup">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:span">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.span"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.span">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:bdo">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.bdo"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.bdo">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:q">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.q"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.q">
        <xsl:call-template name="attrs"/>
        <xsl:copy-of select="@cite"/>
    </xsl:template>

    <xsl:template match="html:img">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.img"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.img">
        <xsl:call-template name="attrs"/>
        <xsl:copy-of select="@src|@alt|@longdesc|@height|@width"/>
    </xsl:template>

    <xsl:template match="html:figure">
        <div>
            <xsl:call-template name="attlist.figure"/>
            <xsl:apply-templates select="node()"/>
        </div>
    </xsl:template>

    <xsl:template name="attlist.figure">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'figure' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:p">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.p"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.p">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:hr">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.hr"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.hr">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:h1 | html:h2 | html:h3 | html:h4 | html:h5 | html:h6">
        <!--
            the ranks have been previously normalized (in html-downgrade.xpl)
        -->
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.h"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.h">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:blockquote">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.blockquote"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.blockquote">
        <xsl:call-template name="attrs"/>
        <xsl:copy-of select="@cite"/>
    </xsl:template>

    <xsl:template match="html:dl">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.dl"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.dl">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:dt">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.dt"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.dt">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:dd">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.dd"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.dd">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:ol">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.ol"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.ol">
        <xsl:call-template name="attrs"/>
        <xsl:copy-of select="@start|@type"/>
    </xsl:template>

    <xsl:template match="html:ul">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.ul"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.ul">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:li">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.li"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.li">
        <xsl:call-template name="attrs"/>
        <xsl:copy-of select="@value"/>
    </xsl:template>

    <xsl:template match="html:table">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.table"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.table">
        <xsl:call-template name="attrs"/>
        <xsl:copy-of select="@sortable"/>
    </xsl:template>

    <xsl:template match="html:caption">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.caption"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.caption">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:figcaption">
        <div>
            <xsl:call-template name="attlist.figcaption"/>
            <xsl:apply-templates select="node()"/>
        </div>
    </xsl:template>

    <xsl:template name="attlist.figcaption">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'figcaption' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:thead">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.thead"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.thead">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:tfoot">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.tfoot"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.tfoot">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:tbody">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.tbody"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.tbody">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:colgroup">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.colgroup"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.colgroup">
        <xsl:call-template name="attrs"/>
        <xsl:copy-of select="@span"/>
    </xsl:template>

    <xsl:template match="html:col">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.col"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.col">
        <xsl:call-template name="attrs"/>
        <xsl:copy-of select="@span"/>
    </xsl:template>

    <xsl:template match="html:tr">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.tr"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.tr">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:th">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.th"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.th">
        <xsl:call-template name="attrs"/>
        <xsl:copy-of select="@headers|@scope|@rowspan|@colspan"/>
    </xsl:template>

    <xsl:template match="html:td">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.td"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.td">
        <xsl:call-template name="attrs"/>
        <xsl:copy-of select="@headers|@scope|@rowspan|@colspan"/>
    </xsl:template>

    <xsl:template match="html:area">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.area"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.area">
        <xsl:call-template name="attrs"/>
        <xsl:copy-of select="@alt|@coords|@href|@shape|@target"/>
        <!-- dropped: @download, @hreflang, @media, @rel, @type -->
    </xsl:template>

    <xsl:template match="html:aside">
        <div>
            <xsl:call-template name="attlist.aside"/>
            <xsl:apply-templates select="node()"/>
        </div>
    </xsl:template>

    <xsl:template name="attlist.aside">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'aside' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:audio">
        <xsl:choose>
            <xsl:when test="$link-to-media = 'true'">
                <xsl:variable name="src" select="(@src,html:source/@src)[1]"/>
                <a href="{$src}">
                    <xsl:call-template name="attrs">
                        <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'audio' else ()" tunnel="yes"/>
                    </xsl:call-template>
                    <xsl:value-of select="$src"/>
                </a>
            </xsl:when>
            <xsl:otherwise>
                <span>
                    <xsl:call-template name="attrs">
                        <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'audio' else ()" tunnel="yes"/>
                    </xsl:call-template>
                    <xsl:attribute name="style" select="string-join((@style,'display:none;'),' ')"/>
                    <xsl:comment select="concat(' link removed: ',(@src,html:source/@src)[1],' ')"/>
                </span>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="html:b">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.b"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.b">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:base">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.base"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.base">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:bdi">
        <span>
            <xsl:call-template name="attlist.bdi"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <xsl:template name="attlist.bdi">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'bdi' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:button">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.button"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.button">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'button' else ()" tunnel="yes"/>
        </xsl:call-template>
        <xsl:copy-of select="@disabled|@name|@type|@value"/>
        <!-- dropped: @autofocus, @form, @formaction, @formenctype, @formmethod, @formnovalidate, @formtarget -->
    </xsl:template>

    <xsl:template match="html:canvas">
        <xsl:comment select="' canvas from HTML5-version of this document has been removed. An empty div remains to make sure no internal links break. '"/>
        <div>
            <xsl:call-template name="attlist.canvas"/>
        </div>
    </xsl:template>

    <xsl:template name="attlist.canvas">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'canvas' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:data">
        <span>
            <xsl:call-template name="attlist.data"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <xsl:template name="attlist.data">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'data' else ()" tunnel="yes"/>
        </xsl:call-template>
        <xsl:if test="@value and not(@title)">
            <xsl:attribute name="title" select="@value"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="html:datalist">
        <xsl:message select="' the datalist element is not allowed in HTML4 and has no good equivalent; replacing with an invisible div... '"/>
        <div>
            <xsl:call-template name="attlist.datalist"/>
            <xsl:apply-templates select="node()"/>
        </div>
    </xsl:template>

    <xsl:template name="attlist.datalist">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'datalist' else ()" tunnel="yes"/>
        </xsl:call-template>
        <xsl:attribute name="style" select="string-join((@style,'display:none;'),' ')"/>
    </xsl:template>

    <xsl:template match="html:del">
        <s>
            <xsl:call-template name="attlist.del"/>
            <xsl:apply-templates select="node()"/>
        </s>
    </xsl:template>

    <xsl:template name="attlist.del">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'del' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:details">
        <div>
            <xsl:call-template name="attlist.details"/>
            <xsl:apply-templates select="node()"/>
        </div>
    </xsl:template>

    <xsl:template name="attlist.details">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then ('details',if (@open) then 'details-open' else ()) else ()" tunnel="yes"/>
        </xsl:call-template>
        <!-- @open is dropped and converted to a class instead -->
    </xsl:template>

    <xsl:template match="html:dialog">
        <div>
            <xsl:call-template name="attlist.dialog"/>
            <xsl:apply-templates select="node()"/>
        </div>
    </xsl:template>

    <xsl:template name="attlist.dialog">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then ('dialog',if (@open) then 'dialog-open' else ()) else ()" tunnel="yes"/>
        </xsl:call-template>
        <!-- @open is dropped and converted to a class instead -->
    </xsl:template>

    <xsl:template match="html:embed">
        <xsl:choose>
            <xsl:when test="ancestor::html:object">
                <span>
                    <xsl:call-template name="attrs">
                        <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'embed' else ()" tunnel="yes"/>
                    </xsl:call-template>
                    <xsl:attribute name="style" select="string-join((@style,'display:none;'),' ')"/>
                    <xsl:comment select="concat(' embedded media removed: ',@src,' ')"/>
                </span>
            </xsl:when>
            <xsl:otherwise>
                <object>
                    <xsl:call-template name="attlist.embed"/>
                    <xsl:apply-templates select="node()"/>
                </object>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="attlist.embed">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'embed' else ()" tunnel="yes"/>
        </xsl:call-template>
        <xsl:copy-of select="@height|@type|@width"/>
        <xsl:if test="@src">
            <xsl:attribute name="data" select="@src"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="html:fieldset">
        <div>
            <xsl:call-template name="attlist.fieldset"/>
            <xsl:apply-templates select="node()"/>
        </div>
    </xsl:template>

    <xsl:template name="attlist.fieldset">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'fieldset' else ()" tunnel="yes"/>
        </xsl:call-template>
        <!-- @disabled, @form and @name is dropped -->
        <xsl:if test="@form">
            <xsl:message select="' WARNING: the *explicit* linkage between the fieldset and the referenced form is lost since @form is dropped '"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="html:footer">
        <div>
            <xsl:call-template name="attlist.footer"/>
            <xsl:apply-templates select="node()"/>
        </div>
    </xsl:template>

    <xsl:template name="attlist.footer">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'footer' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:form">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.form"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.form">
        <xsl:call-template name="attrs"/>
        <xsl:copy-of select="@accept|@accept-charset|@action|@enctype|@method|@name|@target"/>
        <!-- @autocomplete and @novalidate are dropped -->
    </xsl:template>

    <xsl:template match="html:header">
        <div>
            <xsl:call-template name="attlist.header"/>
            <xsl:apply-templates select="node()"/>
        </div>
    </xsl:template>

    <xsl:template name="attlist.header">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'header' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:i">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.i"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.i">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:iframe">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.iframe"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.iframe">
        <xsl:call-template name="attrs"/>
        <xsl:copy-of select="@height|@name|@src|@width"/>
        <!-- @sandbox, @seamless and @srcdoc are dropped -->
    </xsl:template>

    <xsl:template match="html:input">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.input"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.input">
        <xsl:call-template name="attrs"/>
        <xsl:copy-of select="@accept|@alt|@checked|@disabled|@maxlength|@name|@readonly|@size|@src|@type|@value"/>
        <xsl:attribute name="style" select="string-join((@style,if (@height) then concat('height:',@height,';') else (), if (@width) then concat('width:',@width,';') else ()),' ')"/>
        <!-- @autocomplete, @autofocus, @form, @formaction, @formenctype, @formmethod, @formnovalidate, @formtarget, @list, @max, @min, @multiple, @pattern, @placeholder, @required and @step are dropped -->
        <xsl:if test="@form">
            <xsl:message select="' WARNING: the *explicit* linkage between the input and the referenced form is lost since @form is dropped '"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="html:ins">
        <u>
            <xsl:call-template name="attlist.ins"/>
            <xsl:apply-templates select="node()"/>
        </u>
    </xsl:template>

    <xsl:template name="attlist.ins">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'ins' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:keygen">
        <span>
            <xsl:call-template name="attlist.keygen"/>
            <xsl:apply-templates select="node()"/>
        </span>
        <xsl:message select="' WARNING: the keygen element was replaced with a span; the form is probably broken '"/>
    </xsl:template>

    <xsl:template name="attlist.keygen">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'keygen' else ()" tunnel="yes"/>
        </xsl:call-template>
        <!-- @autofocus, @challenge, @disabled, @form, @keytype and @name is dropped -->
    </xsl:template>

    <xsl:template match="html:label">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.label"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.label">
        <xsl:call-template name="attrs"/>
        <xsl:copy-of select="@for"/>
        <!-- @form is dropped -->
        <xsl:if test="@form">
            <xsl:message select="' WARNING: the *explicit* linkage between the input and the referenced form is lost since @form is dropped '"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="html:legend">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.legend"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.legend">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:main">
        <div>
            <xsl:call-template name="attlist.main"/>
            <xsl:apply-templates select="node()"/>
        </div>
    </xsl:template>

    <xsl:template name="attlist.main">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'main' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:map">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.map"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.map">
        <xsl:call-template name="attrs"/>
        <xsl:copy-of select="@name"/>
    </xsl:template>

    <xsl:template match="html:mark">
        <em>
            <xsl:call-template name="attlist.mark"/>
            <xsl:apply-templates select="node()"/>
        </em>
    </xsl:template>

    <xsl:template name="attlist.mark">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'mark' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:menu">
        <div>
            <xsl:call-template name="attlist.menu"/>
            <xsl:apply-templates select="node()"/>
        </div>
    </xsl:template>

    <xsl:template name="attlist.menu">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'menu' else ()" tunnel="yes"/>
        </xsl:call-template>
        <!-- @label and @type is dropped -->
    </xsl:template>

    <xsl:template match="html:menuitem">
        <div>
            <xsl:call-template name="attlist.menuitem"/>
            <xsl:apply-templates select="node()"/>
        </div>
    </xsl:template>

    <xsl:template name="attlist.menuitem">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'menuitem' else ()" tunnel="yes"/>
        </xsl:call-template>
        <!-- @type, @label, @icon, @disabled, @checked, @radiogroup, @default and @command is dropped -->
    </xsl:template>

    <xsl:template match="html:meter">
        <span>
            <xsl:call-template name="attlist.meter"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <xsl:template name="attlist.meter">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'meter' else ()" tunnel="yes"/>
        </xsl:call-template>
        <!-- @form, @high, @low, @max, @min, @optimum and @value is dropped -->
    </xsl:template>

    <xsl:template match="html:nav">
        <div>
            <xsl:call-template name="attlist.nav"/>
            <xsl:apply-templates select="node()"/>
        </div>
    </xsl:template>

    <xsl:template name="attlist.nav">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'nav' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:noscript">
        <xsl:choose>
            <xsl:when test="ancestor::html:body">
                <xsl:copy copy-namespaces="no">
                    <xsl:call-template name="attlist.noscript"/>
                    <xsl:apply-templates select="node()"/>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message select="' noscript element removed since it can only be used inside the body element in HTML4 '"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="attlist.noscript">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'noscript' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:object">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.object"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.object">
        <xsl:call-template name="attrs"/>
        <xsl:copy-of select="@data|@height|@name|@type|@usemap|@width"/>
        <!-- @form is dropped -->
    </xsl:template>

    <xsl:template match="html:optgroup">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.optgroup"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.optgroup">
        <xsl:call-template name="attrs"/>
        <xsl:copy-of select="@disabled|@label"/>
    </xsl:template>

    <xsl:template match="html:option">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.option"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.option">
        <xsl:call-template name="attrs"/>
        <xsl:copy-of select="@disabled|@label|@selected|@value"/>
    </xsl:template>

    <xsl:template match="html:output">
        <xsl:element name="{if (f:is-phrasing(.)) then 'span' else 'div'}">
            <xsl:call-template name="attlist.output"/>
            <xsl:apply-templates select="node()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template name="attlist.output">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'output' else ()" tunnel="yes"/>
        </xsl:call-template>
        <!-- @for, @form and @name is dropped -->
        <xsl:if test="@form">
            <xsl:message select="' WARNING: the *explicit* linkage between the output and the referenced form is lost since @form is dropped '"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="html:param">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.param"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.param">
        <xsl:call-template name="attrs"/>
        <xsl:copy-of select="@name|@value"/>
    </xsl:template>

    <xsl:template match="html:pre">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.pre"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.pre">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:progress">
        <xsl:element name="{if (f:is-phrasing(.)) then 'span' else 'div'}">
            <xsl:call-template name="attlist.progress"/>
            <xsl:apply-templates select="node()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template name="attlist.progress">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'progress' else ()" tunnel="yes"/>
        </xsl:call-template>
        <!-- @max and @value is dropped -->
    </xsl:template>

    <xsl:template match="html:rb">
        <span>
            <xsl:call-template name="attlist.rb"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <xsl:template name="attlist.rb">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'rb' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:rp">
        <span>
            <xsl:call-template name="attlist.rp"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <xsl:template name="attlist.rp">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'rp' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:rt">
        <span>
            <xsl:call-template name="attlist.rt"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <xsl:template name="attlist.rt">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'rt' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:rtc">
        <span>
            <xsl:call-template name="attlist.rtc"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <xsl:template name="attlist.rtc">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'rtc' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:ruby">
        <span>
            <xsl:call-template name="attlist.ruby"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <xsl:template name="attlist.ruby">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'ruby' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:s">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.s"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.s">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:script">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.script"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.script">
        <xsl:call-template name="attrs"/>
        <xsl:copy-of select="@charset|@defer|@src|@type"/>
        <!-- @async is dropped -->
    </xsl:template>

    <xsl:template match="html:select">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.select"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.select">
        <xsl:call-template name="attrs"/>
        <xsl:copy-of select="@disabled|@multiple|@name|@size"/>
        <!-- @autofocus, @form and @required are dropped -->
        <xsl:if test="@form">
            <xsl:message select="' WARNING: the *explicit* linkage between the select and the referenced form is lost since @form is dropped '"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="html:small">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.small"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.small">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:source">
        <span>
            <xsl:call-template name="attlist.source"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <xsl:template name="attlist.source">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'source' else ()" tunnel="yes"/>
        </xsl:call-template>
        <!-- @media, @src and @type is dropped -->
    </xsl:template>

    <xsl:template match="html:summary">
        <div>
            <xsl:call-template name="attlist.summary"/>
            <xsl:apply-templates select="node()"/>
        </div>
    </xsl:template>

    <xsl:template name="attlist.summary">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'summary' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:template">
        <div>
            <xsl:call-template name="attlist.template"/>
            <xsl:apply-templates select="node()"/>
        </div>
    </xsl:template>

    <xsl:template name="attlist.template">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'template' else ()" tunnel="yes"/>
        </xsl:call-template>
        <xsl:attribute name="style" select="string-join((@style,'display:none;'),' ')"/>
    </xsl:template>

    <xsl:template match="html:textarea">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.textarea"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.textarea">
        <xsl:call-template name="attrs"/>
        <xsl:copy-of select="@cols|@disabled|@name|@readonly|@rows"/>
        <!-- @autofocus, @form, @maxlength, @placeholder, @required and @wrap is dropped -->
        <xsl:if test="@form">
            <xsl:message select="' WARNING: the *explicit* linkage between the textarea and the referenced form is lost since @form is dropped '"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="html:time">
        <span>
            <xsl:call-template name="attlist.time"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <xsl:template name="attlist.time">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'time' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:track">
        <span>
            <xsl:call-template name="attlist.track"/>
            <xsl:comment select="string-join((@kind,@srclang,@label,@src),' - ')"/>
        </span>
    </xsl:template>

    <xsl:template name="attlist.track">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'track' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="html:u">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.u"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.u">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:var">
        <xsl:copy copy-namespaces="no">
            <xsl:call-template name="attlist.var"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="attlist.var">
        <xsl:call-template name="attrs"/>
    </xsl:template>

    <xsl:template match="html:video">
        <xsl:choose>
            <xsl:when test="$link-to-media = 'true'">
                <xsl:variable name="src" select="(@src,html:source/@src)[1]"/>
                <a href="{$src}">
                    <xsl:call-template name="attrs">
                        <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'audio' else ()" tunnel="yes"/>
                    </xsl:call-template>
                    <xsl:value-of select="$src"/>
                </a>
            </xsl:when>
            <xsl:otherwise>
                <span>
                    <xsl:call-template name="attrs">
                        <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'audio' else ()" tunnel="yes"/>
                    </xsl:call-template>
                    <xsl:attribute name="style" select="string-join((@style,'display:none;'),' ')"/>
                    <xsl:comment select="concat(' link removed: ',(@src,html:source/@src)[1],' ')"/>
                </span>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="html:wbr">
        <span>
            <xsl:call-template name="attlist.wbr"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <xsl:template name="attlist.wbr">
        <xsl:call-template name="attrs">
            <xsl:with-param name="classes" select="if ($add-semantic-classes='true') then 'wbr' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:function name="f:classes" as="xs:string*">
        <xsl:param name="element" as="element()"/>
        <xsl:sequence select="tokenize($element/@class,'\s+')"/>
    </xsl:function>

    <xsl:function name="f:is-phrasing" as="xs:boolean">
        <xsl:param name="context" as="node()*"/>
        <xsl:sequence
            select="if ((for $e in ($context) return if ($e[self::html:a[f:is-phrasing(html:*)] or self::html:abbr or self::html:area[ancestor::html:map] or self::html:audio or self::html:b or self::html:bdi or self::html:bdo or self::html:br or self::html:button or self::html:canvas or self::html:cite or self::html:code or self::html:command or self::html:datalist or self::html:del[f:is-phrasing(html:*)] or self::html:dfn or self::html:em or self::html:embed or self::html:i or self::html:iframe or self::html:img or self::html:input or self::html:ins[f:is-phrasing(html:*)] or self::html:kbd or self::html:keygen or self::html:label or self::html:map[f:is-phrasing(html:*)] or self::html:mark or self::html:math or self::html:meter or self::html:noscript or self::html:object or self::html:output or self::html:progress or self::html:q or self::html:ruby or self::html:s or self::html:samp or self::html:script or self::html:select or self::html:small or self::html:span or self::html:strong or self::html:sub or self::html:sup or self::html:svg or self::html:textarea or self::html:time or self::html:u or self::html:var or self::html:video or self::html:wbr or self::html:text]) then 'true' else 'false')='false') then true() else false()"
        />
    </xsl:function>

</xsl:stylesheet>
