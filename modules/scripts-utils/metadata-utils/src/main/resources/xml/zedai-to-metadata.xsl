<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns="http://www.idpf.org/2007/opf" xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:z="http://www.daisy.org/ns/z3998/authoring/" version="2.0" exclude-result-prefixes="xs z"
  xpath-default-namespace="http://www.idpf.org/2007/opf">

  <xsl:output method="xml" indent="yes"/>

  <!--TODO resolve metadata prefixes from @profile and @prefix
     * default prefixes declared in @profile
        e.g. @profile="http://www.daisy.org/z3998/2011/vocab/profiles/default/"
             declares "dcterms" and "z3998"
     * other prefixes are explicitly declared in @prefix
        e.g. prefix="foaf: http://xmlns.com/foaf/0.1/"
  -->
  <xsl:template match="/z:document">
    <!-- TODO dynamically generate @prefix -->
    <metadata prefix="dc: http://purl.org/dc/elements/1.1/">
      <!--== Identifier ==-->
      <xsl:for-each select="f:get-identifiers(/)">
        <dc:identifier id="{if(position()=1) then 'pub-id' else concat('pub-id-',position())}">
          <xsl:value-of select="."/>
        </dc:identifier>
        <!--TODO add dcterms:identifier's @property="identifier-type"-->
        <!--TODO add dcterms:identifier's @scheme-->
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
          <xsl:variable name="title" select="f:get-title-from-content(/)"/>
          <dc:title id="{generate-id($title)}">
            <xsl:copy-of select="$title/@dir"/>
            <xsl:copy-of select="$title/@xml:lang"/>
            <xsl:value-of
              select="if (string($title)) then normalize-space(string($title)) else $title/@content"
            />
          </dc:title>
        </xsl:otherwise>
      </xsl:choose>

      <!--== Language ==-->
      <xsl:for-each select="f:get-languages(/)">
        <dc:language>
          <xsl:value-of select="."/>
        </dc:language>
      </xsl:for-each>

      <!--== Other ==-->
      <xsl:apply-templates/>

    </metadata>
  </xsl:template>

  <xsl:template match="z:meta[@rel='z3998:meta-record']">
    <xsl:variable name="this" select="."/>
    <xsl:variable name="record-type"
      select="ancestor::z:head//z:meta[@property='z3998:meta-record-type' and @about=$this/@resource][1]/@content"/>
    <xsl:choose>
      <xsl:when test="$record-type='z3998:mods'">
        <link rel="mods-record" href="{@resource}"/>
      </xsl:when>
      <xsl:when test="$record-type='z3998:onix-books'">
        <link rel="onix-record" href="{@resource}"/>
      </xsl:when>
      <xsl:when test="$record-type='z3998:marc21-xml'">
        <link rel="marc21xml-record" href="{@resource}"/>
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
      <!--<xsl:when test="@property=('dcterms:contributor','dcterms:coverage','...')">
        <!-\- DCMES optional elements: 
          contributor | coverage | creator | date | description | format 
          | publisher | relation | rights | source | subject | type
        -\->
        <!-\-TODO translate dcterms:* into dc:*-\->
        <!-\-TODO pick content from meta/@content or string-value(.)-\->
        <xsl:element name="{@property}">
          <xsl:value-of select="."/>
        </xsl:element>
      </xsl:when>-->
      <xsl:when
        test="not(
           starts-with(@property,'z3998:')
        or @property=('dc:identifier','dc:title','dc:language'))
        and normalize-space(if (@content) then @content else .) ">
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

  <xsl:template match="z:*[f:hasProp(.,'dcterms:title')]" mode="title">
    <xsl:call-template name="create-title"/>
    <meta refines="#{generate-id()}" property="title-type">main</meta>
  </xsl:template>

  <xsl:template match="z:*[f:hasPropOrRole(.,'fulltitle')]" mode="title">
    <!--apply templates in case it is a compound fulltitle-->
    <xsl:apply-templates mode="title"/>
    <!--generate a dc:title anyway-->
    <xsl:call-template name="create-title"/>
    <!--it is an 'expanded' title if it has title/subtitle descendants-->
    <xsl:if test="descendant::z:*[f:hasPropOrRole(.,'title')]">
      <meta refines="#{generate-id()}" property="title-type">expanded</meta>
    </xsl:if>
  </xsl:template>

  <xsl:template match="z:*[f:hasPropOrRole(.,('title','covertitle','halftitle'))]" mode="title">
    <xsl:call-template name="create-title"/>
  </xsl:template>
  <xsl:template match="z:*[f:hasPropOrRole(.,'subtitle')]" mode="title">
    <xsl:call-template name="create-title"/>
    <meta refines="#{generate-id()}" property="title-type">subtitle</meta>
  </xsl:template>

  <xsl:template name="create-title">
    <dc:title id="{generate-id()}">
      <xsl:copy-of select="@dir"/>
      <xsl:copy-of select="@xml:lang"/>
      <xsl:value-of select="if (string(.)) then normalize-space(string(.)) else @content"/>
    </dc:title>
  </xsl:template>

  <xsl:template match="text()" mode="#all"/>

  <!--==========================================================-->
  <!-- Functions                                                -->
  <!--==========================================================-->

  <xsl:function name="f:get-identifiers" as="xs:string*">
    <xsl:param name="doc" as="document-node()"/>
    <xsl:sequence
      select="distinct-values((
      $doc/z:document/z:head/z:meta[@property='dcterms:identifier']/@content))"
    />
  </xsl:function>

  <xsl:function name="f:get-languages" as="xs:string*">
    <xsl:param name="doc" as="document-node()"/>
    <xsl:sequence
      select="distinct-values((
      $doc/z:document/z:head/z:meta[@property='dcterms:language']/@content,
      $doc/z:document/@xml:lang,
      $doc/z:document/z:body/@xml:lang))"
    />
  </xsl:function>

  <xsl:function name="f:get-title-from-content" as="node()">
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

</xsl:stylesheet>
