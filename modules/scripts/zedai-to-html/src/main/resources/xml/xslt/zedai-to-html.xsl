<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:f="http://www.daisy.org/ns/functions-internal"
                xmlns:pf="http://www.daisy.org/ns/functions"
                xmlns:diagram="http://www.daisy.org/ns/z3998/authoring/features/description/"
                xmlns:m="http://www.w3.org/1998/Math/MathML"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns:its="http://www.w3.org/2005/11/its"
                xmlns:tts="http://www.daisy.org/ns/pipeline/tts"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns="http://www.w3.org/1999/xhtml"
                xpath-default-namespace="http://www.daisy.org/ns/z3998/authoring/"
                exclude-result-prefixes="#all">

  <xsl:import href="zedai-vocab-utils.xsl"/>

  <xsl:output method="xhtml" doctype-system="about:legacy-compat" encoding="UTF-8" indent="yes"/>

  <xsl:param name="base" select="base-uri(/)"/>

  <xsl:key name="refs" match="*[@ref]" use="tokenize(@ref,'\s+')"/>
  <xsl:key name="described" match="*[@desc]" use="@desc"/>

  <xsl:template match="/">
    <xsl:call-template name="html">
      <xsl:with-param name="nodes" select="document/body/*"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="html">
    <xsl:param name="nodes" as="node()*"/>
    <!--TODO config: externalize the profile definition-->
    <xsl:variable name="lang" select="$nodes/ancestor::*/@xml:lang[1]"/>
    <html xml:lang="{if ($lang) then $lang else 'en'}">
      <xsl:apply-templates select="$nodes/ancestor::*/@its:dir[1]"/>
      <head>
        <meta charset="UTF-8"/>
        <title><xsl:value-of
          select="$nodes[1]/ancestor::document/head/meta[@property='dcterms:title']/@content"/></title>
        <!--<meta name="dcterms:identifier" content="com.googlecode.zednext.alice"/>-->
        <!--<meta name="dcterms:publisher" content="CSU"/>-->
        <!--<meta name="dcterms:date" content="2010-03-27T13:50:05-02:00"/>-->
      </head>
      <body>
	<xsl:copy-of select="$nodes/ancestor-or-self::*/@tts:*"/>
        <xsl:apply-templates select="$nodes"/>
      </body>
    </html>
  </xsl:template>

  <!--===========================================================-->
  <!-- Translation: Header                                       -->
  <!--===========================================================-->

  <!--TODO normalize: flatten meta -->
  <!--TODO translate: meta -->
  <!--<xsl:template match="meta">
    <meta>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </meta>
  </xsl:template>-->

  <!--===========================================================-->
  <!-- Translation: Section layer                                -->
  <!--===========================================================-->

  <!--====== Section module =====================================-->
  <xsl:template match="section">
    <section>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </section>
  </xsl:template>

  <!--====== Bibliography module ================================-->
  <xsl:template match="bibliography">
    <section>
      <xsl:call-template name="role">
        <xsl:with-param name="roles" select="('bibliography',@role)"/>
      </xsl:call-template>
      <xsl:apply-templates select="@* except @role"/>
      <!--TODO translate: bibliography/section[@role='custom']-->
      <xsl:apply-templates mode="bibliography"/>
    </section>
  </xsl:template>

  <xsl:template match="section" mode="bibliography">
    <section>
      <xsl:apply-templates select="@*"/>
      <!--TODO translate: bibliography/section[@role='custom']-->
      <xsl:apply-templates mode="bibliography"/>
    </section>
  </xsl:template>

  <xsl:template match="entry" mode="bibliography">
    <div>
      <xsl:call-template name="role">
        <xsl:with-param name="roles" select="('biblioentry',@role)"/>
      </xsl:call-template>
      <xsl:apply-templates select="@* except @role"/>
      <xsl:apply-templates/>
    </div>
  </xsl:template>

  <!--====== Cover module =======================================-->

  <xsl:template match="cover">
    <section>
      <xsl:call-template name="role">
        <xsl:with-param name="roles" select="('cover',@role)"/>
      </xsl:call-template>
      <xsl:apply-templates select="@* except @role"/>
      <xsl:apply-templates/>
    </section>
  </xsl:template>
  <xsl:template match="spine">
    <section>
      <xsl:apply-templates select="@*"/>
      <!--TODO translate: @role-->
      <xsl:apply-templates/>
    </section>
  </xsl:template>
  <xsl:template match="frontcover">
    <section>
      <!--TODO translate: @role-->
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </section>
  </xsl:template>
  <xsl:template match="backcover">
    <section>
      <!--TODO translate: @role-->
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </section>
  </xsl:template>
  <xsl:template match="flaps">
    <section>
      <!--TODO translate: @role-->
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </section>
  </xsl:template>

  <!--====== Glossary module ====================================-->
  <!--TODO normalize: simple glossary => dl-->
  <!--TODO variants: handle the block variant-->
  <xsl:template match="glossary">
    <section>
      <xsl:call-template name="role">
        <xsl:with-param name="roles" select="('glossary',@role)"/>
      </xsl:call-template>
      <xsl:apply-templates select="@* except @role"/>
      <xsl:apply-templates mode="glossary"/>
    </section>
  </xsl:template>
  <xsl:template match="section" mode="glossary">
    <section>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="glossary"/>
    </section>
  </xsl:template>
  <xsl:template match="entry" mode="glossary">
    <dt>
      <xsl:call-template name="role">
        <xsl:with-param name="roles" select="('glossterm',@role)"/>
      </xsl:call-template>
      <xsl:apply-templates select="@* except @role"/>
      <xsl:apply-templates/>
    </dt>
  </xsl:template>

  <!--====== Index module =======================================-->

  <xsl:template match="index">
    <nav>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="index"/>
    </nav>
  </xsl:template>
  <xsl:template match="section" mode="index">
    <section>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="glossary"/>
    </section>
  </xsl:template>
  <xsl:template match="entry" mode="index">
    <!--FIXME normalize adjacent entries into ul -->
    <xsl:apply-templates/>
  </xsl:template>


  <!--====== Document partitions module =========================-->

  <xsl:template match="frontmatter">
    <section>
      <xsl:call-template name="role">
        <xsl:with-param name="roles" select="('frontmatter',@role)"/>
      </xsl:call-template>
      <xsl:apply-templates select="@* except @role"/>
      <xsl:apply-templates/>
    </section>
  </xsl:template>
  <xsl:template match="bodymatter">
    <section>
      <xsl:call-template name="role">
        <xsl:with-param name="roles" select="('bodymatter',@role)"/>
      </xsl:call-template>
      <xsl:apply-templates select="@* except @role"/>
      <xsl:apply-templates/>
    </section>
  </xsl:template>
  <xsl:template match="backmatter">
    <section>
      <xsl:call-template name="role">
        <xsl:with-param name="roles" select="('backmatter',@role)"/>
      </xsl:call-template>
      <xsl:apply-templates select="@* except @role"/>
      <xsl:apply-templates/>
    </section>
  </xsl:template>


  <!--====== ToC module =========================================-->

  <xsl:template match="toc">
    <nav>
      <xsl:call-template name="role">
        <xsl:with-param name="roles" select="('toc',@role)"/>
      </xsl:call-template>
      <xsl:apply-templates select="@* except @role"/>
      <!--normalize adjacent entry elements into html:ul -->
      <xsl:for-each-group select="*" group-adjacent="empty(self::entry)">
        <xsl:choose>
          <xsl:when test="not(current-grouping-key())">
            <ul>
              <xsl:apply-templates mode="toc" select="current-group()"/>
            </ul>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates mode="toc" select="current-group()"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each-group>
    </nav>
  </xsl:template>

  <xsl:template match="entry" mode="toc">
    <li>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </li>
  </xsl:template>
  <xsl:template match="block" mode="toc">
    <div>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="toc"/>
    </div>
  </xsl:template>
  <xsl:template match="section" mode="toc">
    <section>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="toc"/>
    </section>
  </xsl:template>
  <xsl:template match="aside" mode="toc">
    <aside>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="toc"/>
    </aside>
  </xsl:template>

  <!--====== Verse module =======================================-->

  <xsl:template match="verse">
    <div>
      <!--TODO translate: @role-->
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </div>
  </xsl:template>
  <xsl:template match="section" mode="verse">
    <section>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="verse"/>
    </section>
  </xsl:template>

  <!--===========================================================-->
  <!-- Translation: Block layer                                  -->
  <!--===========================================================-->

  <!--====== Block module =======================================-->

  <xsl:template match="block" mode="#all">
    <div>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </div>
  </xsl:template>
  <xsl:template match="block[f:has-role(.,'figure')]" mode="#all">
    <figure>
      <xsl:apply-templates select="@*"/>
      <!--
        figure's objects:
        - tables or objects (images)
        the figure's top-level caption contains:
        - all captioning elements without @ref if there is an @associate on the parent
        - all captioning elements with @ref matching all the IDs of the figure's objects
      -->
      <xsl:variable name="objects" select="table|object"/>
      <xsl:variable name="captions"
        select=".[@associate]/(hd|caption|citation)[not(@ref)]
        | (hd|caption|citation)[f:references-all(.,$objects)]"
      />
      <!--we respect the document order: the caption is created either before or after
          the object depending on whether the first captioning element is found before
          or after.-->
      <xsl:if test="$captions[1] &lt;&lt; $objects[1]">
        <figcaption>
          <xsl:apply-templates select="f:simplify-captions($captions)" mode="caption"/>
        </figcaption>
      </xsl:if>
      <xsl:apply-templates select="*"/>
      <xsl:if test="$captions[1] >> $objects[1]">
        <figcaption>
          <xsl:apply-templates select="f:simplify-captions($captions)" mode="caption"/>
        </figcaption>
      </xsl:if>
    </figure>
  </xsl:template>

  <!--====== Annotation module ==================================-->

  <xsl:template match="annotation" mode="#all">
    <aside epub:type="annotation">
      <!--TODO better @role translation-->
      <!--<xsl:call-template name="role">
        <xsl:with-param name="roles" select="('annotation',@role)"/>
      </xsl:call-template>-->
      <xsl:apply-templates select="@* except @role"/>
      <xsl:apply-templates/>
    </aside>
  </xsl:template>
  <!--TODO variants: handle block annotations-->
  <xsl:template match="annoref" mode="#all">
    <a href="{@ref}">
      <xsl:call-template name="role">
        <xsl:with-param name="roles" select="('annoref',@role)"/>
      </xsl:call-template>
      <xsl:apply-templates select="@* except @role"/>
      <xsl:apply-templates select="if (@value) then @value else *"/>
    </a>
  </xsl:template>
  <xsl:template match="annoref/@value">
    <xsl:value-of select="."/>
  </xsl:template>

  <!--====== Aside module =======================================-->

  <xsl:template match="aside" mode="#all">
    <aside>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </aside>
  </xsl:template>

  <!--====== Byline module ======================================-->

  <xsl:template match="byline" mode="#all">
    <p>
      <xsl:apply-templates select="@*"/>
      <!--TODO translate: @role-->
      <xsl:apply-templates/>
    </p>
  </xsl:template>

  <!--====== Caption module =====================================-->

  <!--Captions are handled in the templates of the captioned element-->
  <xsl:template match="caption" mode="#all"/>
  <xsl:template match="caption" mode="caption" priority="10">
    <xsl:choose>
      <xsl:when test="some $child in node() satisfies f:is-phrase($child)">
        <p>
          <xsl:apply-templates select="@*" mode="#default"/>
          <xsl:apply-templates mode="#default"/>
        </p>
      </xsl:when>
      <xsl:when test="@* except @ref">
        <div>
          <xsl:apply-templates select="@*" mode="#default"/>
          <xsl:apply-templates mode="#default"/>
        </div>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="#default"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--====== Cite module ========================================-->

  <xsl:template match="citation" mode="#all">
    <xsl:if test="not(f:is-captioning(.))">
      <xsl:call-template name="citation"/>
    </xsl:if>
  </xsl:template>
  <xsl:template match="citation" mode="caption" priority="10">
      <xsl:call-template name="citation"/>
  </xsl:template>
  <xsl:template name="citation">
    <!--TODO normalize: citation (e.g. within a quote) -->
    <cite>
      <xsl:apply-templates select="@*"/>
      <!--TODO translate: @role-->
      <xsl:apply-templates/>
    </cite>
  </xsl:template>

  <!--====== Code module ==================================-->

  <!--TODO variants: refine characterization -->
  <xsl:template match="code" mode="#all">
    <pre>
      <code>
        <xsl:apply-templates select="@*"/>
        <xsl:apply-templates mode="code"/>
      </code>
    </pre>
  </xsl:template>
  <xsl:template match="code[f:is-phrase(.)]">
    <code>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="code"/>
    </code>
  </xsl:template>
  <xsl:template match="lngroup" mode="code">
    <div>
      <xsl:call-template name="role">
        <xsl:with-param name="roles" select="('dsy:lngroup',@role)"/>
      </xsl:call-template>
      <xsl:apply-templates select="@* except @role"/>
      <xsl:apply-templates mode="code"/>
    </div>
  </xsl:template>

  <!--====== Dateline module ==================================-->

  <xsl:template match="dateline">
    <p>
      <!--TODO translate: => time/@pubdate child ? -->
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </p>
  </xsl:template>

  <!--====== Definition module ==================================-->

  <xsl:template match="definition">
    <p>
      <!--TODO translate: @role -->
      <!--TODO normalize: definition => dl/dd ? -->
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </p>
  </xsl:template>

  <!--====== Description module =================================-->

  <!--TODO Support @epub:describedat when standardized -->

  <xsl:template match="description[@xlink:href]" mode="#all">
    <xsl:message>[WARNING] Unsupported external description to '<xsl:value-of select="@xlink:href"
      />'.</xsl:message>
  </xsl:template>
  <xsl:template match="description" mode="#all"/>
  <xsl:template match="description[key('described',@xml:id)[f:is-desc-unused(.)]]">
    <div>
      <xsl:apply-templates select="@*|node()"/>
    </div>
  </xsl:template>
  <xsl:template match="description" mode="details" priority="10">
    <xsl:choose>
      <xsl:when test="some $child in node() satisfies f:is-phrase($child)">
        <p>
          <xsl:apply-templates select="@*" mode="#default"/>
          <xsl:apply-templates mode="#default"/>
        </p>
      </xsl:when>
      <xsl:when test="@* except @ref">
        <div>
          <xsl:apply-templates select="@*" mode="#default"/>
          <xsl:apply-templates mode="#default"/>
        </div>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="#default"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:function name="f:description-string" as="xs:string">
    <xsl:param name="desc" as="element()?"/>
    <xsl:choose>
      <xsl:when test="some $child in $desc/node() satisfies f:is-phrase($child)">
        <xsl:sequence select="normalize-space(string($desc))"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="string-join($desc/*/normalize-space(),' ')"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <!--====== Headings module ====================================-->
  <xsl:template match="h" mode="#all">
    <xsl:element name="{if (hpart) then 'hgroup' else 'h1'}">
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>
  <xsl:template match="hpart" mode="#all">
    <h1>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </h1>
  </xsl:template>
  <xsl:template match="hd" mode="#all">
    <!--
    Skip headings referencing tables and objects.
    They will be treated as captions under the 'caption' mode.
  -->
    <xsl:if test="not(f:is-captioning(.))">
      <xsl:call-template name="hd"/>
    </xsl:if>
  </xsl:template>
  <xsl:template match="hd" mode="caption" priority="10">
    <xsl:call-template name="hd"/>
  </xsl:template>
  <xsl:template name="hd">
    <xsl:choose>
      <!--figure are sectioning roots, it's safe to translate hd into h1-->
      <xsl:when test="parent::block[f:has-role(.,'figure')]">
        <h1>
          <xsl:apply-templates select="@*"/>
          <xsl:apply-templates/>
        </h1>
      </xsl:when>
      <xsl:otherwise>
        <p>
          <xsl:call-template name="role">
            <xsl:with-param name="roles" select="('bridgehead',@role)"/>
          </xsl:call-template>
          <xsl:apply-templates select="@* except @role" mode="#default"/>
          <xsl:apply-templates mode="#default"/>
        </p>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--====== List module ========================================-->

  <xsl:template match="list" mode="#all">
    <xsl:apply-templates select="pagebreak[empty(preceding-sibling::item)]" mode="block"/>
    <xsl:element name="{if (@type='ordered') then 'ol' else 'ul'}">
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="* except pagebreak"/>
    </xsl:element>
    <xsl:apply-templates select="pagebreak[empty(following-sibling::item)]" mode="block"/>
  </xsl:template>
  <xsl:template match="list[@type='ordered']/@start">
    <xsl:copy/>
  </xsl:template>
  <xsl:template match="item" mode="#all">
    <li>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="preceding-sibling::pagebreak[exists(preceding-sibling::item) and current() is following-sibling::item[1]]"/>
      <xsl:apply-templates/>
    </li>
  </xsl:template>

  <!--====== Note module ========================================-->

  <!--TODO normalize: group adjacent nodes in a parent aside -->
  <xsl:template match="note">
    <aside>
      <!--TODO translate: @ref-->
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </aside>
  </xsl:template>
  <xsl:template match="noteref">
    <a rel="note" href="{concat('#',@ref)}">
      <xsl:call-template name="role">
        <xsl:with-param name="roles" select="('noteref',@role)"/>
      </xsl:call-template>
      <xsl:apply-templates select="@* except @role"/>
      <xsl:choose>
        <xsl:when test="@value">
          <sup>
            <xsl:value-of select="@value"/>
          </sup>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates/>
        </xsl:otherwise>
      </xsl:choose>
    </a>
  </xsl:template>

  <!--====== Object module ======================================-->
  <xsl:template match="object[f:is-image(.)]" mode="#all">
    <!--TODO  add support for DIAGRAM descriptions -->
    <!--Description is used for @alt; it comes from either:
     - external desription
     - description child
     - direct content (implicit description[@by='author'])
     -->
    <xsl:variable name="alt" as="xs:string?" select="f:description-string(
      if (description) then description
      else if (normalize-space(.)) then .
      else if (@desc) then id(tokenize(@desc,'\s+'))[not(@xlink:href)][1]
      else ()
      )"/>

    <xsl:variable name="captions" select="../(hd|caption|citation)[f:references(.,current())]"/>
    <xsl:variable name="shared-captions"
      select="..[f:has-role(.,'figure')]/(hd|caption|citation)[f:references-all(.,../(object|table))]"/>
    <xsl:variable name="dedicated-captions" select="$captions except $shared-captions"/>
    <xsl:variable name="republisher-anno"
      select="key('refs',@xml:id)[self::annotation][@by='republisher'][1]"/>
    <xsl:choose>
      <xsl:when test="$dedicated-captions">
        <figure>
          <xsl:if test="$dedicated-captions[1] &lt;&lt; .">
            <figcaption>
              <xsl:apply-templates select="f:simplify-captions($dedicated-captions)" mode="caption"/>
            </figcaption>
          </xsl:if>
          <img src="{@src}" alt="{$alt}">
            <xsl:apply-templates select="@*"/>

            <xsl:if test="f:is-desc-unused(.) or $republisher-anno">
              <xsl:attribute name="aria-describedby" select="if (f:is-desc-unused(.)) then @desc
                                                             else $republisher-anno/@xml:id"/>
            </xsl:if>
          </img>
          <xsl:if test="$dedicated-captions[1] >> .">
            <figcaption>
              <xsl:apply-templates select="f:simplify-captions($dedicated-captions)" mode="caption"/>
            </figcaption>
          </xsl:if>
        </figure>
      </xsl:when>
      <xsl:otherwise>
        <img src="{@src}" alt="{$alt}">
          <xsl:apply-templates select="@*"/>
          <xsl:if test="f:is-desc-unused(.) or $republisher-anno">
            <xsl:attribute name="aria-describedby" select="if (f:is-desc-unused(.)) then @desc
              else $republisher-anno/@xml:id"/>
          </xsl:if>
        </img>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="object" mode="#all">
    <xsl:message select="'object: unsuported media type'"/>
  </xsl:template>

  <!--====== Paragraph module ===================================-->

  <xsl:template match="p">
    <p>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </p>
  </xsl:template>

  <!--====== Pagebreak module ===================================-->

  <!--TODO normalize: page breaks-->
  <!--TODO variants: refine characterization-->
  <xsl:template match="pagebreak" mode="block">
    <div>
      <xsl:call-template name="role">
        <xsl:with-param name="roles" select="('pagebreak',@role)"/>
      </xsl:call-template>
      <xsl:apply-templates select="@* except @role"/>
    </div>
  </xsl:template>
  <xsl:template match="pagebreak">
    <span>
      <xsl:call-template name="role">
        <xsl:with-param name="roles" select="('pagebreak',@role)"/>
      </xsl:call-template>
      <xsl:apply-templates select="@* except @role"/>
    </span>
  </xsl:template>
  <xsl:template match="pagebreak/@value" mode="#all">
    <xsl:attribute name="title" select="."/>
  </xsl:template>

  <!--====== Quote module =======================================-->

  <!--TODO variants: refine characterization -->
  <!--TODO translate: citation child => @cite -->
  <xsl:template match="quote">
    <blockquote>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </blockquote>
  </xsl:template>
  <xsl:template match="quote[f:is-phrase(.)]">
    <q>
      <xsl:apply-templates select="@*"/>
      <!--TODO normalize: quotation marks-->
      <xsl:apply-templates/>
    </q>
  </xsl:template>

  <!--====== Transition module ==================================-->

  <xsl:template match="transition" mode="#all">
    <hr>
      <xsl:apply-templates select="@*"/>
    </hr>
  </xsl:template>

  <!--====== Table module =======================================-->
  <xsl:template match="table" mode="#all">
    <table>
      <xsl:apply-templates select="@*"/>
      <!--If the table is within a figure block, the caption has already been translated to a figcaption-->
      <xsl:if test="not(parent::block[@role='figure'])">
        <xsl:variable name="captions" select="key('refs',@xml:id)[self::hd|self::caption]"/>
        <xsl:variable name="descs" select="id(tokenize(@desc,'\s+'))[not(@xlink:href)]"/>
        <xsl:if test="$captions or $descs">
          <caption>
            <xsl:apply-templates
              select="if (count($captions)=1 and $captions[self::caption]) then $captions/node() else $captions"
              mode="caption"/>
            <xsl:if test="$descs">
              <!--TODO add CSS style to move out of the screen ?-->
              <details>
                <summary>Description</summary>
                <xsl:apply-templates select="$descs" mode="details"/>
              </details>
            </xsl:if>
          </caption>
        </xsl:if>
      </xsl:if>
      <xsl:apply-templates select="* except pagebreak"/>
      <!-- @colspan, @rowspan, @headers -->
    </table>
    <xsl:call-template name="table-final-pagebreaks"/>
  </xsl:template>
  <xsl:template match="colgroup" mode="#all">
    <colgroup>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="if(@span) then node() except col else node()"/>
    </colgroup>
  </xsl:template>
  <xsl:template match="col" mode="#all">
    <col>
      <xsl:apply-templates select="@*"/>
    </col>
  </xsl:template>
  <xsl:template match="colgroup/@span | col/@span" mode="#all">
    <xsl:copy/>
  </xsl:template>
  <xsl:template match="thead" mode="#all">
    <thead>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="* except pagebreak"/>
    </thead>
  </xsl:template>
  <xsl:template match="tbody" mode="#all">
    <tbody>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="* except pagebreak"/>
    </tbody>
  </xsl:template>
  <xsl:template match="tfoot" mode="#all">
    <tfoot>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="* except pagebreak"/>
    </tfoot>
  </xsl:template>
  <xsl:template match="tr" mode="#all">
    <tr>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="* except pagebreak"/>
    </tr>
  </xsl:template>
  <xsl:template name="th" match="th" mode="#all">
    <th>
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="table-incell-pagebreaks"/>
      <xsl:apply-templates/>
    </th>
  </xsl:template>
  <xsl:template match="td[@scope]" mode="#all">
    <!-- Note: As td/@scope is not defined in HTML, td/@scope becomes th/@scope -->
    <xsl:call-template name="th"/>
  </xsl:template>
  <xsl:template match="td" mode="#all">
    <td>
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="table-incell-pagebreaks"/>
      <xsl:apply-templates/>
    </td>
  </xsl:template>
  <xsl:template match="@colspan|@rowspan|@scope|@headers" mode="#all">
    <xsl:copy/>
  </xsl:template>
  <xsl:template name="table-incell-pagebreaks">
    <xsl:if test="position()=1">
      <!--pagebreak just before the current row-->
      <xsl:apply-templates select="../preceding-sibling::*[1][self::pagebreak]"/>
      <!--pagebreak at the end of the previous row-->
      <xsl:apply-templates
        select="../preceding-sibling::*[1][self::tr]/*[last()][self::pagebreak]"/>
    </xsl:if>
    <xsl:if test="position()=1 and not(../preceding-sibling::tr)">
      <!--pagebreak at the end of the previous header or body-->
      <xsl:apply-templates
        select="../parent::tbody/preceding-sibling::*[1][self::thead or self::tbody]/(*[last()]|*[last()]/*[last()])[self::pagebreak]"/>
      <xsl:apply-templates
        select="../parent::tfoot/preceding-sibling::*[1][self::tbody]/(*[last()]|*[last()]/*[last()])[self::pagebreak]"
      />
    </xsl:if>
    <!--pagebreak before the cell -->
    <xsl:apply-templates select="preceding-sibling::*[1][self::pagebreak]"/>
  </xsl:template>
  <xsl:template name="table-final-pagebreaks">
    <!--pagebreak after the last row-->
    <xsl:apply-templates select="*[last()][self::pagebreak]" mode="block"/>
    <!--pagebreak as the last child of the last row-->
    <xsl:apply-templates select="*[last()][self::tr]/*[last()][self::pagebreak]" mode="block"/>
    <!--pagebreak at the end of the last header or body-->
    <xsl:apply-templates
      select="*[last()][self::tbody|self::tfoot]/(*[last()]|*[last()]/*[last()])[self::pagebreak]"
      mode="block"/>
  </xsl:template>

  <!--===========================================================-->
  <!-- Translation: Phrase layer                                 -->
  <!--===========================================================-->

  <!--====== Span module ========================================-->

  <xsl:template match="span" mode="#all">
    <span>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="#current"/>
    </span>
  </xsl:template>

  <!--====== Abbreviations module ===============================-->

  <xsl:template match="abbr" mode="#all">
    <abbr title="{normalize-space(id(@ref))}">
      <!--TODO translate: @role-->
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="#current"/>
    </abbr>
  </xsl:template>
  <xsl:template match="expansion[not(ancestor::head)]" mode="#all">
    <!--
      expansions in the header are ignored (used for abbreviation's title attribute)
      expansions in the body are translated as spans
    -->
    <span>
      <!--TODO translate: @role-->
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="#current"/>
    </span>
  </xsl:template>

  <!--====== Dialogue module ====================================-->

  <xsl:template match="d" mode="#all">
    <span>
      <!--TODO translate: @role-->
      <!--TODO normalize: quotation marks-->
      <!--TODO translate: conceptual link to speaker -->
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="#current"/>
    </span>
  </xsl:template>

  <!--====== Emphasis module ====================================-->

  <xsl:template match="emph" mode="#all">
    <em>
      <!-- TODO translate: => em vs strong ? -->
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="#current"/>
    </em>
  </xsl:template>

  <!--====== Line module ========================================-->

  <xsl:template match="ln" mode="#all">
    <span>
      <!--TODO translate: @role-->
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="#current"/>
    </span>
    <br/>
  </xsl:template>

  <xsl:template match="lnum" mode="#all">
    <span>
      <!--TODO translate: @role-->
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="#current"/>
    </span>
  </xsl:template>

  <xsl:template match="lngroup" mode="#all">
    <div>
      <!--TODO translate: @role-->
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="#current"/>
    </div>
  </xsl:template>

  <!--====== Linking module =====================================-->

  <xsl:template match="ref" mode="#all">
    <a>
      <!--TODO translate: conceptual link ?-->
      <!--TODO translate: multiple @ref destinations ?-->
      <xsl:choose>
        <xsl:when test="@ref">
          <xsl:attribute name="href" select="concat('#',@ref)"/>
        </xsl:when>
        <xsl:when test="@xlink:href">
          <xsl:attribute name="href" select="@xlink:href"/>
          <xsl:attribute name="rel" select="'external'"/>
        </xsl:when>
      </xsl:choose>
      <xsl:apply-templates select="@* except (@ref,@xlink:href)"/>
      <xsl:apply-templates mode="#current"/>
    </a>
  </xsl:template>

  <!--TODO translate: @continuation-->

  <!--====== Name module ========================================-->

  <xsl:template match="name" mode="#all">
    <!--TODO translate: => i, span, b ?-->
    <i>
      <!--TODO translate: @role-->
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="#current"/>
    </i>
  </xsl:template>

  <!--====== Num module =========================================-->

  <xsl:template match="num" mode="#all">
    <span>
      <!--TODO translate: @role-->
      <xsl:apply-templates select="@*"/>
      <xsl:if test="@value">
        <xsl:attribute name="title" select="@value"/>
      </xsl:if>
      <xsl:apply-templates mode="#current"/>
    </span>
  </xsl:template>

  <!--====== Sentence module ====================================-->

  <xsl:template match="s" mode="#all">
    <span>
      <!--TODO translate: @role-->
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="#current"/>
    </span>
  </xsl:template>

  <!--====== Term module ========================================-->

  <!-- TODO translate: => i, dfn ? -->
  <xsl:template match="term" mode="#all">
    <xsl:element
      name="{if (id(@ref)=(parent::*,preceding-sibling::*,following-sibling::*))
               then 'dfn'
               else 'i'}">
      <!--TODO translate: @role-->
      <!--TODO translate: @ref-->
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="#current"/>
    </xsl:element>
  </xsl:template>

  <!--====== Time module ========================================-->

  <xsl:template match="time" mode="#all">
    <time>
      <xsl:apply-templates select="@*"/>
      <xsl:if test="@time">
        <xsl:attribute name="datetime" select="@time"/>
      </xsl:if>
      <xsl:apply-templates mode="#current"/>
    </time>
  </xsl:template>

  <!--====== Word module ========================================-->

  <xsl:template match="w" mode="#all">
    <span>
      <!--TODO translate: @role-->
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="#current"/>
    </span>
  </xsl:template>
  <xsl:template match="wpart" mode="#all">
    <span>
      <!--TODO translate: @role-->
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="#current"/>
    </span>
  </xsl:template>

  <!--===========================================================-->
  <!-- Translation: Text layer                                   -->
  <!--===========================================================-->
  <!--====== Sup/sub module =====================================-->

  <xsl:template match="sub" mode="#all">
    <sub>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="#current"/>
    </sub>
  </xsl:template>

  <xsl:template match="sup" mode="#all">
    <sup>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="#current"/>
    </sup>
  </xsl:template>

  <!--====== Char module ========================================-->

  <!-- TODO differentiate from phrase level-->
  <!--<xsl:template match="span" mode="#all">
    <!-\- TODO translate: => CSS ? -\->
    <span>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="#current"/>
      <!-\- TODO translate: @role -\->
    </span>
  </xsl:template>-->


  <!--===========================================================-->
  <!-- Translation: MathML Feature                               -->
  <!--===========================================================-->

  <xsl:template match="m:*|m:*/@*">
    <xsl:copy copy-namespaces="no">
      <xsl:apply-templates select="node() | @*"/>
    </xsl:copy>
  </xsl:template>

  <!--===========================================================-->
  <!-- Feature :: DIAGRAM Descriptions                           -->
  <!--===========================================================-->

  <xsl:template match="diagram:description">
    <aside>
      <xsl:apply-templates select="@*|node()"/>
    </aside>
  </xsl:template>

  <xsl:template match="diagram:body">
    <details>
      <xsl:apply-templates select="@*|node()"/>
    </details>
  </xsl:template>

  <xsl:template match="diagram:summary">
    <summary>
      <xsl:apply-templates select="@*|node()"/>
    </summary>
  </xsl:template>

  <xsl:template match="diagram:longdesc[not(*)]">
    <p>
      <xsl:value-of select="."/>
    </p>
  </xsl:template>

  <!--===========================================================-->
  <!-- Identity templates                                        -->
  <!--===========================================================-->

  <xsl:template match="comment()|processing-instruction()|text()">
    <xsl:copy/>
  </xsl:template>

  <!--===========================================================-->
  <!-- Global attributes                                         -->
  <!--===========================================================-->

  <xsl:template match="@xml:id">
    <xsl:attribute name="id" select="."/>
  </xsl:template>
  <xsl:template match="@base|@class|@xml:space|@xml:base|@xml:lang|@its:dir">
    <xsl:copy/>
    <!--TODO translate: @its:translate-->
  </xsl:template>
  <xsl:template match="@its:dir">
    <xsl:attribute name="dir" select="string(.)"/>
    <!--TODO translate: @its:dir lro and rlo values -->
  </xsl:template>
  <xsl:template match="@role">
    <xsl:variable name="epub-type" select="pf:to-epub(.)"/>
    <xsl:if test="$epub-type">
      <xsl:attribute name="epub:type" select="$epub-type"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="@tts:*" mode="#all">
    <xsl:copy/>
  </xsl:template>

  <!--Do not copy attributes by default-->
  <xsl:template match="@*" mode="#all"/>

  <xsl:template name="role">
    <xsl:param name="roles" as="xs:string*"/>
    <xsl:variable name="role-string" select="normalize-space(string-join($roles,' '))"/>
    <xsl:attribute name="epub:type"
      select="string-join(distinct-values(tokenize($role-string,'\s+')),' ')"/>
  </xsl:template>
  <!--
    Use translation function for roles ? e.g.
      <xsl:function name="f:role">
        tbd
      </xsl:function>
    It can be called in:
      <span role="{f:role(@role,'explicit role values'}">
        <xsl:apply-templates select="@* except @role"/>
      </span>
  -->


  <!--===========================================================-->
  <!-- Keys                                                      -->
  <!--===========================================================-->

  <!--===========================================================-->
  <!-- Util functions                                            -->
  <!--===========================================================-->

  <xsl:function name="f:has-role" as="xs:boolean">
    <xsl:param name="elem" as="node()"/>
    <xsl:param name="role" as="xs:string*"/>
    <xsl:sequence select="tokenize($elem/@role,'\s+')=$role"/>
  </xsl:function>
  <xsl:function name="f:is-phrase" as="xs:boolean">
    <!--FIXME improve heuristics-->
    <xsl:param name="node" as="node()"/>
    <xsl:sequence
      select="$node/self::text()[normalize-space()] or $node/preceding-sibling::text()[normalize-space()]
      or $node/following-sibling::text()[normalize-space()]
      or $node/parent::p"
    />
  </xsl:function>
  <xsl:function name="f:is-block" as="xs:boolean">
    <!--FIXME improve heuristics-->
    <xsl:param name="node" as="node()"/>
    <xsl:sequence select="$node/(self::p or self::block)"/>
  </xsl:function>
  <xsl:function name="f:is-image" as="xs:boolean">
    <xsl:param name="node" as="node()"/>
    <xsl:sequence
      select="starts-with($node/@srctype,'image/') or matches($node/@src,'\.(jpg|png|gif|svg)$')"/>
  </xsl:function>
  <xsl:function name="f:is-captioning" as="xs:boolean">
    <xsl:param name="elem" as="element()"/>
    <xsl:sequence
      select="$elem/../@associate or $elem/id(tokenize($elem/@ref,'\s+'))[self::table|self::object]"/>
  </xsl:function>
  <xsl:function name="f:references" as="xs:boolean">
    <xsl:param name="ref" as="element()"/>
    <xsl:param name="elems" as="element()*"/>
    <xsl:sequence select="$ref/@ref and tokenize($ref/@ref,'\s+')=$elems/@xml:id"/>
  </xsl:function>
  <xsl:function name="f:references-all" as="xs:boolean">
    <xsl:param name="ref" as="element()"/>
    <xsl:param name="elems" as="element()*"/>
    <xsl:sequence select="$ref/@ref and (every $id in $elems/@xml:id satisfies tokenize($ref/@ref,'\s+')=$id)"/>
  </xsl:function>
  <xsl:function name="f:simplify-captions" as="node()*">
    <xsl:param name="captions" as="element()*"/>
    <xsl:sequence select="if (count($captions)=1 and $captions[self::caption]) then $captions/node() else $captions"/>
  </xsl:function>
  <xsl:function name="f:is-desc-unused" as="xs:boolean">
    <xsl:param name="elem" as="element()"/>
    <xsl:sequence select="$elem/@desc and ($elem/description or normalize-space($elem))"/>
  </xsl:function>
</xsl:stylesheet>
