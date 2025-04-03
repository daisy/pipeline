<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:z="http://www.daisy.org/ns/z3998/authoring/"
                xmlns:rdfa="rdfa-functions"
                xmlns="http://www.idpf.org/2007/opf"
                xpath-default-namespace="http://www.idpf.org/2007/opf"
                exclude-result-prefixes="xs z f pf rdfa">

  <xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/generate-id.xsl"/>

  <!--TODO resolve metadata prefixes from @profile and @prefix
     * default prefixes declared in @profile
        e.g. @profile="http://www.daisy.org/z3998/2012/vocab/profiles/default/"
             declares "dc", "dcterms", "z3998" and "diagram"
     * other prefixes are explicitly declared in @prefix
        e.g. prefix="foaf: http://xmlns.com/foaf/0.1/"
  -->

  <xsl:param name="source-of-pagination" as="xs:string?" select="()"/>

  <xsl:template match="/z:document" priority="1">
    <xsl:call-template name="pf:next-match-with-generated-ids">
      <xsl:with-param name="prefix" select="'id_'"/>
      <xsl:with-param name="for-elements" select="f:get-title-from-content(/)|
                                                  //z:*[f:hasProp(.,('dc:title',
                                                                     'dcterms:title'))]|
                                                  //z:*[f:hasPropOrRole(.,('fulltitle',
                                                                           'title',
                                                                           'covertitle',
                                                                           'halftitle',
                                                                           'subtitle'))]"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="/z:document">
    <!-- TODO dynamically generate @prefix -->
    <metadata>
      <xsl:if test=".//z:pagebreak">
          <xsl:attribute name="prefix" select="'a11y: http://www.idpf.org/epub/vocab/package/a11y/#'"/>
      </xsl:if>
      <xsl:namespace name="dc" select="'http://purl.org/dc/elements/1.1/'"/>
      <!--== Identifier ==-->
      <xsl:for-each select="f:get-identifiers(/)">
        <dc:identifier id="{if(position()=1) then 'pub-id' else concat('pub-id-',position())}">
          <xsl:value-of select="."/>
        </dc:identifier>
        <!--TODO add dc:identifier's @property="identifier-type"-->
        <!--TODO add dc:identifier's @scheme-->
      </xsl:for-each>

      <!--== Title ==-->
      <!-- pre-construct title candidates -->
      <xsl:variable name="titles-and-metas" as="node()*">
        <xsl:apply-templates mode="title"/>
      </xsl:variable>
      <!--isolate dc:title elements-->
      <xsl:variable name="titles" as="node()*" select="$titles-and-metas[self::dc:title]"/>
      <!--group meta elements by refining ID, for easier lookup-->
      <xsl:variable name="metas" as="node()*">
        <xsl:for-each-group select="$titles-and-metas[self::meta]"
          group-by="substring-after(@refines,'#')">
          <metas refid="{current-grouping-key()}">
            <xsl:copy-of select="current-group()"/>
          </metas>
        </xsl:for-each-group>
      </xsl:variable>
      <!--acces the 'main' title: 
            * the first dc:title having a refining meta with title-type='main'
            * or else the first dc:title with no title type-->
      <xsl:variable name="main-title"
        select="(
        $titles[$metas[@refid=current()/@id]/*[@property='title-type' and string()='main']],
        $titles[empty($metas[@refid=current()/@id]/*[@property='title-type'])])[1]"/>
      <xsl:choose>
        <xsl:when test="$titles">
          <!--group by content to remove duplicates-->
          <xsl:for-each-group select="$titles" group-by=".">
            <xsl:copy-of select="current-group()[1]"/>
            <xsl:if test="current-group()=$main-title">
              <meta refines="{concat('#',current-group()[1]/@id)}" property="title-type">main</meta>
            </xsl:if>
            <xsl:copy-of
              select="$metas[@refid=current()/@id]/*[not(@property='display-seq') and not(@property='title-type' and string()='main')]"
            />
          </xsl:for-each-group>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="title" as="element()" select="f:get-title-from-content(/)"/>
          <xsl:for-each select="$title">
            <dc:title>
              <xsl:call-template name="pf:generate-id"/>
              <xsl:copy-of select="@dir"/>
              <xsl:copy-of select="@xml:lang"/>
              <xsl:value-of select="if (string(.)) then normalize-space(string(.)) else @content"/>
            </dc:title>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>

      <!--== Language ==-->
      <xsl:for-each select="f:get-languages(/)">
        <dc:language>
          <xsl:value-of select="."/>
        </dc:language>
      </xsl:for-each>

      <!--== Source of pagination
          - http://kb.daisy.org/publishing/docs/navigation/pagesrc.html
          - https://www.w3.org/publishing/a11y/page-source-id/
          ==-->
      <xsl:if test=".//z:pagebreak
                    and exists($source-of-pagination)
                    and not(//z:*[@property='a11y:pageBreakSource'
                                  and not(@about)
                                  and normalize-space(if (@content) then @content else .)])">
        <meta property="a11y:pageBreakSource">
          <xsl:value-of select="$source-of-pagination"/>
        </meta>
      </xsl:if>

      <!--== Other ==-->
      <xsl:apply-templates/>

    </metadata>
  </xsl:template>

  <xsl:template match="z:meta[@rel='z3998:meta-record' and @resource]">
    <xsl:variable name="this" select="."/>
    <xsl:variable name="record-type"
                  select="ancestor::z:head//z:meta[@property='z3998:meta-record-type']
                                                  [rdfa:context(.)=$this/@resource]
                                                  [1]/@content"/>
    <xsl:choose>
      <xsl:when test="$record-type='z3998:mods'">
        <link rel="record" href="{@resource}" media-type="application/mods+xml"/>
      </xsl:when>
      <xsl:when test="$record-type='z3998:onix-books'">
        <link rel="record" href="{@resource}" media-type="application/xml" properties="onix"/>
      </xsl:when>
      <xsl:when test="$record-type='z3998:marc21-xml'">
        <link rel="record" href="{@resource}" media-type="application/marcxml+xml"/>
      </xsl:when>
      <xsl:when test="$record-type=('z3998:dcterms-rdf','z3998:dctersm-rdfa')">
        <!--TODO translate external DCTERMS records ?-->
      </xsl:when>
    </xsl:choose>
  </xsl:template>


  <xsl:template match="z:meta[@rel and not(starts-with(@rel,'z3998:'))]">
    <!--TODO make sure prefix is declared-->
    <xsl:if test="@rel ne 'z3998:profile'">
      <link rel="{@rel}" href="{@resoure}"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="z:*[@property and not(@about)]">
    <!--TODO set meta[@property="alternate-script"] ?-->
    <!--TODO set meta[@property="file-as"] ?-->
    <!--TODO set meta[@property="role"] ?-->
    <!--TODO set meta[@property="scheme"] ?-->
    <!--
      e.g. dcterms:creator in EPUB:
      <meta property="dcterms:creator" id="creator">Haruki Murakami</meta>
      <meta about="#creator" property="alternate-script" xml:lang="ja">村上 春樹</meta>
      <meta about="#creator" property="file-as">Murakami, Haruki</meta>
      <meta about="#creator" property="role" id="role">aut</meta>
      <meta about="#role" property="scheme" datatype="xsd:anyURI">http://id.loc.gov/vocabulary/relators</meta>
    -->
    <xsl:choose>
      <xsl:when test="empty(normalize-space(if (@content) then @content else .))"/>
      <!-- DCMES optional elements -->
      <xsl:when
        test="@property=('dc:contributor', 'dcterms:contributor',
                         'dc:coverage',    'dcterms:coverage',
                         'dc:creator',     'dcterms:creator',
                         'dc:date',        'dcterms:date',
                         'dc:description', 'dcterms:description',
                         'dc:format',      'dcterms:format',
                         'dc:publisher',   'dcterms:publisher',
                         'dc:relation',    'dcterms:relation',
                         'dc:rights',      'dcterms:rights',
                         'dc:source',      'dcterms:source',
                         'dc:subject',     'dcterms:subject',
                         'dc:type',        'dcterms:type')">
        <xsl:element name="dc:{replace(@property,'^dc(terms)?:','')}">
          <xsl:value-of select="if (@content) then @content else ."/>
        </xsl:element>
      </xsl:when>
      <xsl:when
        test="not(
           starts-with(@property,'z3998:')
        or @property=('dc:identifier', 'dcterms:identifier',
                      'dc:title',      'dcterms:title',
                      'dc:language',   'dcterms:language'))">
        <!--TODO declare custom vocabularies-->
        <!--TODO refine RDFa attributes parsing-->
        <meta property="{@property}">
          <xsl:value-of select="if (@content) then @content else ."/>
        </meta>
      </xsl:when>
    </xsl:choose>
  </xsl:template>



  <!--==========================================================-->
  <!-- Mode: title                                              -->
  <!--==========================================================-->

  <xsl:template match="z:*[f:hasProp(.,('dc:title','dcterms:title'))]" mode="title">
    <xsl:call-template name="create-title"/>
    <xsl:variable name="id" as="xs:string">
      <xsl:call-template name="pf:generate-id"/>
    </xsl:variable>
    <meta refines="#{$id}" property="title-type">main</meta>
  </xsl:template>

  <xsl:template match="z:*[f:hasPropOrRole(.,'fulltitle')]" mode="title">
    <!--apply templates in case it is a compound fulltitle-->
    <xsl:apply-templates mode="title"/>
    <!--generate a dc:title anyway-->
    <xsl:call-template name="create-title"/>
    <!--it is an 'expanded' title if it has title/subtitle descendants-->
    <xsl:if test="descendant::z:*[f:hasPropOrRole(.,'title')]">
      <xsl:variable name="id" as="xs:string">
        <xsl:call-template name="pf:generate-id"/>
      </xsl:variable>
      <meta refines="#{$id}" property="title-type">expanded</meta>
    </xsl:if>
  </xsl:template>

  <!-- in case of DTBooks, the doctitle tag is converted here as a dc:title -->
  <xsl:template match="z:*[f:hasPropOrRole(.,('title','covertitle','halftitle'))]" mode="title">
    <xsl:call-template name="create-title"/>
  </xsl:template>
  <xsl:template match="z:*[f:hasPropOrRole(.,'subtitle')]" mode="title">
    <xsl:call-template name="create-title"/>
    <xsl:variable name="id" as="xs:string">
      <xsl:call-template name="pf:generate-id"/>
    </xsl:variable>
    <meta refines="#{$id}" property="title-type">subtitle</meta>
  </xsl:template>

  <xsl:template name="create-title">
    <!-- FIX - http://www.github.com/daisy/pipeline-tasks#125 : 
      empty doctitle could lead to empty tag with role title, 
        wich would lead to the creation of an empty dc:title tag
        thus making the epub invalid for epubcheck. -->
    <xsl:if test=".//text()[not(self::text()[not(normalize-space())])] != '' or @content">
      <dc:title>
        <xsl:call-template name="pf:generate-id"/>
        <xsl:copy-of select="@dir"/>
        <xsl:copy-of select="@xml:lang"/>
        <xsl:value-of select="if (string(.)) then normalize-space(string(.)) else @content"/>
      </dc:title>
    </xsl:if>
  </xsl:template>

  <xsl:template match="text()" mode="#all"/>

  <!--==========================================================-->
  <!-- Functions                                                -->
  <!--==========================================================-->

  <xsl:function name="f:get-identifiers" as="xs:string*">
    <xsl:param name="doc" as="document-node()"/>
    <xsl:sequence
      select="distinct-values((
      $doc/z:document/z:head/z:meta[@property='dc:identifier']/@content))"
    />
  </xsl:function>

  <xsl:function name="f:get-languages" as="xs:string*">
    <xsl:param name="doc" as="document-node()"/>
    <xsl:sequence
      select="distinct-values((
      $doc/z:document/z:head/z:meta[@property=('dc:language','dcterms:language')]/@content,
      $doc/z:document/@xml:lang,
      $doc/z:document/z:body/@xml:lang))"
    />
  </xsl:function>

  <xsl:function name="f:get-title-from-content" as="element()">
    <xsl:param name="doc" as="document-node()"/>
    <xsl:sequence select="($doc//z:h)[1]"/>
  </xsl:function>

  <!--TODO externalize functions in library ?-->
  <xsl:function name="f:hasProp" as="xs:boolean">
    <xsl:param name="node" as="node()"/>
    <xsl:param name="value" as="xs:string*"/>
    <xsl:sequence select="tokenize($node/@property,'\s+')=$value"/>
  </xsl:function>
  <xsl:function name="f:hasRole" as="xs:boolean">
    <xsl:param name="node" as="node()"/>
    <xsl:param name="value" as="xs:string*"/>
    <xsl:sequence select="tokenize($node/@role,'\s+')=$value"/>
  </xsl:function>
  <xsl:function name="f:hasPropOrRole" as="xs:boolean">
    <xsl:param name="node" as="node()"/>
    <xsl:param name="value" as="xs:string*"/>
    <xsl:sequence select="f:hasProp($node,$value) or f:hasRole($node,$value)"/>
  </xsl:function>

  <!-- FIXME: move to a rdfa-utils module? -->
  <xsl:function name="rdfa:context" as="xs:anyURI?">
    <xsl:param name="elem" as="element()"/>
    <xsl:sequence select="if (exists($elem/@about))
                          then $elem/@about
                          else ($elem/parent::*[@resource|@about])[last()]/(@resource,@about)[1]"/>
  </xsl:function>
  
</xsl:stylesheet>
