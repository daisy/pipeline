<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="2.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" 
  xmlns:saxon="http://saxon.sf.net/"
  xmlns:idml2xml="http://transpect.io/idml2xml" 
  xmlns:html="http://www.w3.org/1999/xhtml"
  xmlns:epub="http://www.idpf.org/2007/ops"
  xmlns:css="http://www.w3.org/1996/css" 
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  xmlns:dbk="http://docbook.org/ns/docbook"
  xmlns:hub2htm="http://transpect.io/hub2htm" 
  xmlns:docx2hub="http://transpect.io/docx2hub"
  xmlns:tr="http://transpect.io" 
  xmlns:mml="http://www.w3.org/1998/Math/MathML"
  xmlns:svg="http://www.w3.org/2000/svg"
  xmlns="http://www.w3.org/1999/xhtml">

  <!--  * This stylesheet is used to transform hub format in XHTML 1.0
        * 
        * Input is expected to be Hub 1.1:
        * http://www.le-tex.de/resource/schema/hub/1.1/hub.rng
        * 
        * Invoke either with inital template or consecutive with inital modes   
        *
        * -->

  <xsl:import href="../xsl/css-rules.xsl"/>
  <xsl:import href="../xsl/css-atts2wrap.xsl"/>
  
  <xsl:param name="debug" select="'no'"/>
  <xsl:param name="debug-dir-uri" select="'debug'"/>
  
  <xsl:param name="overwrite-image-paths" select="'no'"/> <!-- overwrites path with $image-path and image file extension with $suffix -->
  <xsl:param name="image-suffix" select="'png'"/>         <!-- $overwrite-image-paths must have the value 'yes'  -->
  <xsl:param name="image-path" select="'.'"/>             <!-- $overwrite-image-paths must have the value 'yes'  -->
  
  <xsl:param name="css-location" select="''"/>            <!-- location of CSS stylesheet -->
  <xsl:param name="use-css-rules" select="'yes'"/>        <!-- use existing CSS rules and convert them to CSS style element -->
  
  <xsl:param name="generate-toc" select="'no'"/>          <!-- generate html toc yes|no -->
  <xsl:param name="generate-index" select="'yes'"/>        <!-- generate index yes|no -->

  <xsl:param name="target" select="''"/>                  <!-- Supported: EPUB3, HTML5 (l.c. variants also accepted for compatibility reasons -->
  <xsl:variable name="TARGET" select="upper-case($target)" as="xs:string"/>

  <xsl:param name="s9y1-path" as="xs:string?"/>
  <xsl:param name="s9y2-path" as="xs:string?"/>
  <xsl:param name="s9y3-path" as="xs:string?"/>
  <xsl:param name="s9y4-path" as="xs:string?"/>
  <xsl:param name="s9y5-path" as="xs:string?"/>
  <xsl:param name="s9y6-path" as="xs:string?"/>
  <xsl:param name="s9y7-path" as="xs:string?"/>
  <xsl:param name="s9y8-path" as="xs:string?"/>
  <xsl:param name="s9y9-path" as="xs:string?"/>
  <xsl:param name="s9y1-role" as="xs:string?"/>
  <xsl:param name="s9y2-role" as="xs:string?"/>
  <xsl:param name="s9y3-role" as="xs:string?"/>
  <xsl:param name="s9y4-role" as="xs:string?"/>
  <xsl:param name="s9y5-role" as="xs:string?"/>
  <xsl:param name="s9y6-role" as="xs:string?"/>
  <xsl:param name="s9y7-role" as="xs:string?"/>
  <xsl:param name="s9y8-role" as="xs:string?"/>
  <xsl:param name="s9y9-role" as="xs:string?"/>

  <xsl:variable name="paths" as="xs:string*" 
    select="($s9y1-path, $s9y2-path, $s9y3-path, $s9y4-path, $s9y5-path, $s9y6-path, $s9y7-path, $s9y8-path, $s9y9-path)"/>
  <xsl:variable name="roles" as="xs:string*" 
    select="($s9y1-role, $s9y2-role, $s9y3-role, $s9y4-role, $s9y5-role, $s9y6-role, $s9y7-role, $s9y8-role, $s9y9-role)"/>
  <xsl:variable name="common-path" as="xs:string?" select="$paths[position() = index-of($roles, 'common')]"/>

  <xsl:variable name="css:rule-selection-attribute-names" select="'role'" as="xs:string*"/>
  
  <xsl:variable name="css-heading-rules" select="//css:rule[@layout-type='para' and matches(@remap,'h[1-6]')]" />
  <xsl:variable name="extracted-title" select="//dbk:para[@role=//css:rule[@native-name='Title']/@name]/text()"/>

  <xsl:output 
    method="xhtml" 
    indent="yes" 
    doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
    doctype-public="-//W3C//DTD XHTML 1.0//EN" 
    exclude-result-prefixes="dbk xs hub2htm idml2xml"
    saxon:suppress-indentation="p li h1 h2 h3 h4 h5 h6 th td"/>

  <xsl:output method="xml" indent="yes" name="debug" exclude-result-prefixes="#all"/>

  <xsl:template name="css:other-atts">
    <!-- In the context of an element with CSSa attributes -->
    <xsl:apply-templates select="." mode="class-att"/>
    <xsl:call-template name="css:remaining-atts">
      <xsl:with-param name="remaining-atts" 
        select="@*[not(namespace-uri() = 'http://www.w3.org/1996/css' or self::attribute(xml:lang))]
                  [not(css:map-att-to-elt(., ..))]"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="*" mode="class-att"/>
  
  <xsl:template match="*[@role]" mode="class-att">
    <xsl:apply-templates select="@role" mode="#current"/>
  </xsl:template>
  
  <xsl:variable name="hub2htm:ignore-style-name-regex-x"
    select="'^(NormalParagraphStyle|Hyperlink)$'"
    as="xs:string"/>
  
  <xsl:template match="@role" mode="class-att">
    <xsl:if test="not(matches(., $hub2htm:ignore-style-name-regex-x, 'x'))">
      <xsl:attribute name="class" select="replace(., ':', '_')"/>  
    </xsl:if>
  </xsl:template>
  

  <!--<xsl:key name="by-id" match="*[@xml:id]" use="@xml:id"/>-->
  
  <!--<xsl:variable name="ie-indexes" select="for $i in //indexterm return generate-id($i)" as="xs:string*"/>-->
  
  <!--  * 
        * global variables, may overwrite from importing stylesheet 
        * -->
  

  <xsl:param name="html-title" select="'Titel'"/>
  <xsl:variable name="headline-toc" select="'Inhaltsverzeichnis'" as="xs:string"/>
  <xsl:variable name="toc-level" select="3" as="xs:integer"/>
  
  <xsl:variable name="headline-index" select="'Stichwortverzeichnis'" as="xs:string"/>
  <xsl:param name="headline-endnotes" select="'Endnoten'" as="xs:string"/>
  <xsl:param name="section-class-level1" select="/*/dbk:*[name() = ('chapter', 'section')][1]/dbk:title/@role" as="xs:string?"/>
  <xsl:param name="section-class-level2" select="/*/dbk:*[name() = ('chapter', 'section')][1]/dbk:section[1]/dbk:title/@role" as="xs:string?"/>

  <!--  * 
        * initial template, invoke with it:main
        * -->
  
  <xsl:template name="main">
    <xsl:sequence select="$hub2htm-remove-ns"/>
    <!--  *
          * debugging for invocation via saxon
          * -->
    <xsl:if test="$debug eq 'yes'">
      <xsl:result-document href="{concat($debug-dir-uri, '/hub2htm/01.hub2htm-default.xml')}" indent="yes">
        <xsl:sequence select="$hub2htm-default"/>
      </xsl:result-document>
      <xsl:result-document href="{concat($debug-dir-uri, '/hub2htm/02.hub2htm:css.xml')}" indent="yes">
        <xsl:sequence select="$hub2htm:css"/>
      </xsl:result-document>
      <xsl:result-document href="{concat($debug-dir-uri, '/hub2htm/03.hub2htm-lists.xml')}" indent="yes">
        <xsl:sequence select="$hub2htm-lists"/>
      </xsl:result-document>
      <xsl:result-document href="{concat($debug-dir-uri, '/hub2htm/04.hub2htm-cals2html.xml')}" indent="yes">
        <xsl:sequence select="$hub2htm-cals2html"/>
      </xsl:result-document>
      <xsl:result-document href="{concat($debug-dir-uri, '/hub2htm/05.hub2htm-references.xml')}" indent="yes">
        <xsl:sequence select="$hub2htm-references"/>
      </xsl:result-document>
      <xsl:result-document href="{concat($debug-dir-uri, '/hub2htm/06.hub2htm-remove-ns.xml')}" indent="yes">
        <xsl:sequence select="$hub2htm-remove-ns"/>
      </xsl:result-document>
    </xsl:if>
  </xsl:template>
  
  <!--  * 
        * identity template, replicate all nodes that are not matched by a template
        * -->
  
  <xsl:template match="node() | @*" mode="hub2htm-default" 
    priority="-10">
    <xsl:copy copy-namespaces="no">
      <xsl:call-template name="css:content"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="node() | @*" 
    mode="hub2htm-lists hub2htm-cals2html hub2htm-references hub2htm-remove-ns" 
    priority="-10">
    <xsl:copy copy-namespaces="no">
      <xsl:apply-templates select="@* | node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>
  
  <!-- remove whitespace before the root node, caused by xml-model pis -->
  <xsl:template match="text()[not(ancestor::*)]" mode="hub2htm-default"/>
  
  <!--  * 
        * mode vars
        * -->
  
  <xsl:variable name="hub2htm-default">
    <xsl:apply-templates select="/" mode="hub2htm-default"/>
  </xsl:variable>
  
  <xsl:variable name="hub2htm:css">
    <xsl:apply-templates select="$hub2htm-default" mode="hub2htm:css"/>
  </xsl:variable>
  
  <xsl:variable name="hub2htm-lists">
    <xsl:apply-templates select="$hub2htm:css" mode="hub2htm-lists"/>
  </xsl:variable>
  
  <xsl:variable name="hub2htm-cals2html">
    <xsl:apply-templates select="$hub2htm-lists" mode="hub2htm-cals2html"/>
  </xsl:variable>
  
  <xsl:variable name="hub2htm-references">
    <xsl:apply-templates select="$hub2htm-cals2html" mode="hub2htm-references"/>
  </xsl:variable>
  
  <xsl:variable name="hub2htm-remove-ns">
    <xsl:apply-templates select="$hub2htm-references" mode="hub2htm-remove-ns"/>
  </xsl:variable>
  
  <!--  * 
        * MODE hub2htm-default
        * -->
  
  <!-- some data clean-up -->
  
  <xsl:template match="processing-instruction()[name() eq 'xml-model']" mode="hub2htm-default"/>

  <xsl:template match="/*[local-name() = ('hub', 'set', 'book', 'article', 'chapter', 'section', 'glossary',
                                          'part', 'partintro')]" 
    name="build-html-root" mode="hub2htm-default">
    <html>
      <head>
        <xsl:if test="tokenize($TARGET, '\s+') = ('EPUB3', 'HTML5')">
          <meta charset="UTF-8"/>  
        </xsl:if>
        <xsl:if test="not(dbk:info/dbk:title[normalize-space(.)])">
          <title>
            <xsl:choose>
              <xsl:when test="$extracted-title">
                <xsl:value-of select="$extracted-title"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$html-title"/>
              </xsl:otherwise>
            </xsl:choose>
            
            <!-- <xsl:value-of select="$html-title"/> -->
          </title>
        </xsl:if>
        <xsl:apply-templates select="dbk:info" mode="#current"/>
        <!--  *
              * If parameter use-css-rules is set to 'yes', the css:rules is converterd to a CSS style element.
              * Please note that the generated style can interfere with existing CSS stylesheets.
              * -->
        <xsl:if test="$use-css-rules eq 'yes'">
          <xsl:apply-templates select="dbk:info/css:rules" mode="hub2htm:css"/>
        </xsl:if>
        <xsl:call-template name="hub2htm:common-styles"/>
        <xsl:call-template name="hub2htm:style-links"/>
      </head>
      <xsl:call-template name="hub2htm:body"/>
    </html>
  </xsl:template>

  <xsl:template name="hub2htm:body">
    <body>
      <xsl:if test="dbk:info/node()[not(local-name()=('keywordset','title'))]">
        <div class="info">
          <xsl:apply-templates select="dbk:info/node()[not(local-name()=('keywordset','title'))]" mode="#current"/>
        </div>
      </xsl:if>
      <xsl:apply-templates select="node() except dbk:info" mode="#current"/>
    </body>
  </xsl:template>

  <xsl:template name="hub2htm:common-styles">
    <style type="text/css">
      td.marker { vertical-align:top; padding-right: .75em; }
    </style>
  </xsl:template>

  <xsl:template name="hub2htm:style-links">
    <link href="{if ($css-location = '') then concat($common-path, '/css/stylesheet.css') else $css-location}"
      type="text/css" rel="stylesheet"/>
    <xsl:for-each select="reverse($paths)">
      <xsl:variable name="url" select="concat(., 'css/overrides.css')" as="xs:string"/>
      <xsl:if test="unparsed-text-available($url)">
        <link href="{$url}" type="text/css" rel="stylesheet"/>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <!-- map keywords to meta elements -->
  
  <xsl:template match="dbk:keywordset[@role eq 'hub']" mode="hub2htm-default">
    <xsl:apply-templates select="dbk:keyword[@role eq 'source-dir-uri']" mode="#current"/>
  </xsl:template>

  <xsl:template match="dbk:keywordset" mode="hub2htm-default">
    <xsl:apply-templates  mode="#current"/>
  </xsl:template>
  
  <xsl:template match="dbk:keyword" mode="hub2htm-default">
    <meta name="{@role}" content="{.}"/>
  </xsl:template>

  <xsl:template match="dbk:keyword[not(parent::dbk:keywordset[@role = 'hub'])]
  																[ancestor::*/local-name()=('acknowledgements','preface','chapter')]" mode="hub2htm-default">
    <para class="meta_{@role}">
      <xsl:apply-templates select="@* except @role | node()" mode="#current"/>
    </para>
  </xsl:template>

  <xsl:template match="dbk:info" mode="hub2htm-default">
    <xsl:apply-templates select="dbk:keywordset | dbk:title" mode="#current"/>
  </xsl:template>
  
  <!--  *
        * the element css:rules is processed in mode hub2htm:css. Search in this stylesheet and css-rules.xsl 
        * for the string 'css:rules' to find the corresponding locations.    
        * -->
  <xsl:template match="css:rules" mode="hub2htm-default"/>

  <xsl:template match="dbk:info[parent::*[not(self::dbk:part | dbk:chapter | dbk:section | dbk:preface | dbk:colophon)]]/dbk:title" mode="hub2htm-default">
    <xsl:if test="normalize-space(.)">
      <title>
        <xsl:apply-templates mode="#current"/>
      </title>
    </xsl:if>
  </xsl:template>
  
  <!-- map attributes -->
  
  <xsl:template match="@role" mode="hub2htm-default">
    <xsl:attribute name="class" select="replace(., ':|\.', '_')"/>
  </xsl:template>
  
  <xsl:template match="@docx2hub:*|@remap" mode="hub2htm-default"/>

  <xsl:template match="@renderas|@condition" mode="hub2htm-default"/>
  

  <!-- map section hierarchy to html -->
  
  <xsl:template match="dbk:para[@role eq 'List_Bullet'][matches(descendant::dbk:phrase[1][@role eq 'dbk:identifier'], '')]" mode="hub2htm-default" priority="5">
    <ul class="List_Bullet">
      <li>
        <xsl:apply-templates mode="#current"/>
      </li>
    </ul>
  </xsl:template>
  
  <!-- specific stuff, should be moved to an importing stylesheet -->
  <xsl:template match="*[count(html:ul[@class eq 'List_Bullet']) gt 1 ]" mode="hub2htm:css">
    <xsl:copy>
      <xsl:apply-templates select="@*" mode="#current"/>
      <xsl:for-each-group select="node()" group-adjacent="concat(name(), '-', @class)">
        <xsl:choose>
          <xsl:when test="current-grouping-key() = 'ul-List_Bullet'">
            <ul>
              <xsl:attribute name="class" select="'List_Bullet'"/>
              <xsl:apply-templates select="current-group()/node()" mode="#current"/>
            </ul>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="current-group()" mode="#current"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each-group>
    </xsl:copy>
  </xsl:template>

  <xsl:variable name="hub2htm:remove-empty-title-element" as="xs:boolean"
    select="true()"/>

  <xsl:template match="dbk:title[not(normalize-space(.))][$hub2htm:remove-empty-title-element]" 
    mode="hub2htm-default" priority="3"/>
    
  <xsl:template match="dbk:title[parent::dbk:section] | dbk:title[parent::dbk:info[not(dbk:keywordset)][parent::dbk:section]]" mode="hub2htm-default">
    <xsl:variable name="hierarchy-level" select="count(ancestor::dbk:section)"/>
    <xsl:variable name="html-hierarchy-level" select="if($hierarchy-level gt 6) then 6 else $hierarchy-level"/>
    <xsl:element name="{concat('h', $html-hierarchy-level)}">
      <xsl:attribute name="title" select="string-join(.//text()[not(ancestor::dbk:indexterm)],' ')"/>
      <xsl:attribute name="id" select="concat('hd-', generate-id(.))"/>
      <xsl:apply-templates select="@*|node()" mode="#current"/>  
    </xsl:element>
  </xsl:template>
  
  <xsl:template name="create-section">
    <xsl:choose>
      <xsl:when test="$target = 'HTML5'">
        <section class="{string-join((local-name()[not(. = 'section')], @role), ' ')}">
          <xsl:apply-templates select="node() except dbk:info" mode="#current"/>      
        </section>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="node() except dbk:info" mode="#current"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template match="dbk:acknowledgements | dbk:preface | dbk:chapter | dbk:part | dbk:partintro | dbk:colophon | dbk:appendix | dbk:section" mode="hub2htm-default">
    <xsl:if test="dbk:info">
      <xsl:for-each-group select="dbk:info/*" group-adjacent="local-name(.)">
        <xsl:choose>
          <xsl:when test="current-grouping-key() eq 'keywordset'">
            <div class="metainfo">
              <xsl:apply-templates select="current-group()" mode="#current"/>
            </div>
          </xsl:when>
          <xsl:otherwise>
            <div class="info">
              <xsl:apply-templates select="current-group()" mode="#current"/>
            </div>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each-group>
    </xsl:if>
    <xsl:call-template name="create-section"/>
  </xsl:template>

  <xsl:template match="  dbk:toc/dbk:title | dbk:toc/dbk:info/dbk:title 
                       | dbk:acknowledgements/dbk:title | dbk:acknowledgements/dbk:info/dbk:title 
                       | dbk:preface/dbk:title | dbk:preface/dbk:info/dbk:title  
                       | dbk:chapter/dbk:title | dbk:chapter/dbk:info/dbk:title 
                       | dbk:colophon/dbk:title | dbk:colophon/dbk:info/dbk:title 
                       | dbk:appendix/dbk:title | dbk:appendix/dbk:info/dbk:title 
                       | dbk:part/dbk:title | dbk:part/dbk:info/dbk:title
                       | dbk:partintro/dbk:title | dbk:partintro/dbk:info/dbk:title  
                       | dbk:book/dbk:title | dbk:book/dbk:info/dbk:title" mode="hub2htm-default">
    <xsl:element name="h1">
      <xsl:apply-templates select="." mode="class-att"/>
      <xsl:attribute name="title" select="string-join(.//text()[not(ancestor::dbk:indexterm)],' ')"/>
      <xsl:attribute name="id" select="concat('hd-', generate-id(.))"/>
      <xsl:apply-templates select="@*|node()" mode="#current"/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="dbk:bridgehead[@renderas]" mode="hub2htm-default">
    <xsl:variable name="html-hierarchy-level">
      <xsl:choose>
        <xsl:when test="@renderas='sect1'">2</xsl:when>
        <xsl:when test="@renderas='sect2'">3</xsl:when>
        <xsl:when test="@renderas='sect3'">4</xsl:when>
        <xsl:when test="@renderas='sect4'">5</xsl:when>
        <xsl:when test="@renderas='sect5'">6</xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:element name="{concat('h', $html-hierarchy-level)}">
      <xsl:attribute name="title" select="string-join(.//text()[not(ancestor::dbk:indexterm)],' ')"/>
      <xsl:attribute name="id" select="concat('hd-', generate-id(.))"/>
      <xsl:apply-templates select="@*|node()" mode="#current"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="dbk:div" mode="hub2htm-default">
    <div>
      <xsl:if test="@role or @remap">
        <xsl:attribute name="class" select="if (@role) then @role else @remap"/>
        <xsl:apply-templates select="@*|node()" mode="#current"/>
      </xsl:if>
    </div>
  </xsl:template>

  <xsl:template match="dbk:poetry" mode="hub2htm-default">
    <div class="poetry">
        <xsl:apply-templates select="@*, node()" mode="#current"/>
    </div>
  </xsl:template>
  
  <xsl:template match="dbk:para" mode="hub2htm-default">
    <!-- FIX 10/07/2019 (for the DAISY pipeline 2 process): 
          when a css rule notify a possible header level remapping
          for the currant para, apply the remapping-->
    <xsl:variable name="current-role" select="./@role"/>
    <xsl:choose>
      <xsl:when test="$css-heading-rules[@name = $current-role]">
        <xsl:element name="{$css-heading-rules[@name = $current-role]/@remap}">
          <xsl:call-template name="css:content"/>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <p>
          <xsl:call-template name="css:content"/>
          <!--<xsl:apply-templates select="@*|node()" mode="#current"/>-->
        </p>
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>
  
  <xsl:template match="dbk:phrase[not(@*)] | dbk:emphasis[not(@*)]" mode="hub2htm-default">
    <xsl:apply-templates mode="#current"/>
  </xsl:template>
  
  <xsl:template match="dbk:phrase[@docx2hub:generated-vertAlign eq 'w:val:subscript']" mode="hub2htm-default" priority="5">
    <sub>
      <xsl:apply-templates mode="#current"/>
    </sub>
  </xsl:template>
  
  <xsl:template match="dbk:phrase[@docx2hub:generated-vertAlign eq 'w:val:superscript']" mode="hub2htm-default" priority="5">
    <sup>
      <xsl:apply-templates mode="#current"/>
    </sup>
  </xsl:template>
  
  <xsl:template match="dbk:phrase" mode="hub2htm-default">
    <xsl:choose>
      <xsl:when test="matches(., '^&#x20;$')">
        <xsl:apply-templates mode="#current"/>
      </xsl:when>
      <xsl:otherwise>
        <span>
          <xsl:call-template name="css:content"/>
         <!--<xsl:apply-templates select="node()|@*" mode="#current"/>-->
        </span>        
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="dbk:emphasis[not(@role)]" mode="hub2htm-default">
    <em>
      <xsl:call-template name="css:content"/>
    </em>
  </xsl:template>
  
  <xsl:template match="dbk:emphasis[@role]" mode="hub2htm-default">
    <span>
      <xsl:call-template name="css:content"/>
    </span>
  </xsl:template>
  
  <xsl:template match="dbk:tab" mode="hub2htm-default">
    <span class="{if(@role ne '') then concat('tab ', @role) else 'tab'}"><xsl:text>&#x9;</xsl:text></span>
  </xsl:template>
  
  <xsl:template match="dbk:tabs" mode="hub2htm-default"/>
  
  <xsl:template match="dbk:br" mode="hub2htm-default">
    <br>
      <xsl:apply-templates select="@*" mode="#current"/>
    </br>
  </xsl:template>
  
  <xsl:template match="*:symbol" mode="hub2htm-default">
    <span class="hub_symbol">
      <xsl:apply-templates select="@* except @role, node()" mode="#current"/>
    </span>
  </xsl:template>

  <xsl:template match="tr:comment" mode="hub2htm-default">
    <xsl:comment>
      <xsl:apply-templates mode="#current"/>
    </xsl:comment>
  </xsl:template>

  <xsl:template match="dbk:annotation" mode="hub2htm-default">
    <span class="annotation">
      <xsl:call-template name="css:content"/>
    </span>
  </xsl:template>
  
  <xsl:template match="dbk:literallayout" mode="hub2htm-default">
    <p class="literal">
      <xsl:call-template name="css:content"/>
    </p>
  </xsl:template>
  
  <xsl:template match="dbk:*[local-name()=('literallayout','literal')]//text()" mode="hub2htm-default">
    <xsl:analyze-string select="." regex="&#xa;">
      <xsl:matching-substring>
        <br/>
      </xsl:matching-substring>
      <xsl:non-matching-substring>
        <xsl:value-of select="."/>
      </xsl:non-matching-substring>
    </xsl:analyze-string>
  </xsl:template>

  <xsl:template match="dbk:literal" mode="hub2htm-default">
    <span class="literal">
      <xsl:call-template name="css:content"/>
    </span>
  </xsl:template>
  
  <xsl:template match="dbk:blockquote" mode="hub2htm-default">
    <div class="blockquote">
      <xsl:call-template name="css:content"/>
    </div>
  </xsl:template>
  
  <xsl:template match="dbk:sidebar" mode="hub2htm-default">
    <div class="{hub2htm:elementname-and-role-attr-as-class-attrval(.)}">
      <xsl:apply-templates select="@srcpath" mode="#current"/>
      <xsl:comment>Watch out! This might be remains of a Word inline graphic</xsl:comment>
      <xsl:apply-templates select="node()" mode="#current"/>
    </div>
  </xsl:template>
  
  <xsl:template match="dbk:textobject" mode="hub2htm-default">
    <div class="{hub2htm:elementname-and-role-attr-as-class-attrval(.)}">
      <xsl:comment>Text object remain from Word</xsl:comment>
      <xsl:apply-templates mode="#current"/>
    </div>
  </xsl:template>

  <xsl:function name="hub2htm:elementname-and-role-attr-as-class-attrval" as="xs:string">
    <xsl:param name="el" as="element()"/>
    <xsl:variable name="role-value" as="xs:string?">
      <xsl:apply-templates select="$el/@role" mode="hub2html-default"/>
    </xsl:variable>
    <xsl:sequence select="string-join((local-name($el),$role-value),'&#x20;')"/>
  </xsl:function>

  <xsl:template match="dbk:title[parent::dbk:sidebar]" mode="hub2htm-default">
    <p class="{string-join(('sidebar-title', @role), '&#x20;')}">
      <xsl:apply-templates select="@srcpath" mode="#current"/>
      <xsl:call-template name="css:content">
        <xsl:with-param name="css:apply-other-atts" select="false()"/>
      </xsl:call-template>
    </p>
  </xsl:template>
  
  <xsl:template match="dbk:subscript" mode="hub2htm-default">
    <sub>
      <xsl:call-template name="css:content"/>
    </sub>
  </xsl:template>
  
  <xsl:template match="dbk:superscript" mode="hub2htm-default">
    <sup>
      <xsl:call-template name="css:content"/>
    </sup>
  </xsl:template>
  
  <xsl:template match="dbk:authorgroup" mode="hub2htm-default">
    <div class="authors">
      <xsl:apply-templates select="node()" mode="#current"/>
    </div>
  </xsl:template>

  <xsl:template match="dbk:author" mode="hub2htm-default">
    <p>
      <xsl:call-template name="css:content"/>
    </p>
  </xsl:template>

  <xsl:template match="dbk:personname" mode="hub2htm-default">
    <xsl:apply-templates mode="#current"/>
    <xsl:value-of select="if (following-sibling::*) then ', ' else ''"/>
  </xsl:template>

  <xsl:template match="dbk:affiliation" mode="hub2htm-default">
    <span class="{local-name()}">
      <xsl:apply-templates mode="#current"/>
    </span>
  </xsl:template>

  <xsl:template match="dbk:firstname | dbk:givenname | dbk:surname | dbk:honorific" mode="hub2htm-default">
      <xsl:apply-templates mode="#current"/>
    <xsl:value-of select="if (following-sibling::*) then ' ' else ''"/>
  </xsl:template>

  <xsl:template match="dbk:orgname | dbk:street" mode="hub2htm-default">
    <xsl:call-template name="css:content"/>
    <xsl:value-of select="if (following-sibling::*) then ', ' else ''"/>
  </xsl:template>

  <xsl:template match="dbk:city | dbk:postcode" mode="hub2htm-default">
    <xsl:call-template name="css:content"/>
    <xsl:value-of select="if (following-sibling::*[not(local-name()=('city'))]) then ', ' else if (following-sibling::*) then ' ' else ''"/>
  </xsl:template>

  <xsl:template match="dbk:address" mode="hub2htm-default">
    <xsl:apply-templates mode="#current"/>
  </xsl:template>

  <xsl:template match="dbk:legalnotice" mode="hub2htm-default">
    <xsl:apply-templates mode="#current"/>
  </xsl:template>

  <xsl:template match="dbk:legalnotice/dbk:para" mode="hub2htm-default">
    <p>
      <xsl:call-template name="css:content"/>
    </p>
  </xsl:template>

  <xsl:template match="dbk:biblioid" mode="hub2htm-default">
    <p class="{if (@role) then @role else local-name()}">
      <xsl:apply-templates mode="#current"/>
    </p>
  </xsl:template>

  <xsl:template match="dbk:copyright | dbk:publisher" mode="hub2htm-default">
    <p>
      <xsl:call-template name="css:content"/>
    </p>
  </xsl:template>

  <xsl:template match="dbk:copyright | dbk:publisher | dbk:legalnotice/dbk:para | dbk:author" mode="class-att">
    <xsl:attribute name="class" select="local-name()"/>
  </xsl:template>

  <xsl:template match="dbk:copyright/*" mode="hub2htm-default">
    <xsl:call-template name="css:content"/>
    <xsl:value-of select="if (following-sibling::*) then ' ' else ''"/>
  </xsl:template>

  <xsl:template match="dbk:publisher/*" mode="hub2htm-default">
    <xsl:call-template name="css:content"/>
    <xsl:value-of select="if (following-sibling::*) then ', ' else ''"/>
  </xsl:template>

  <xsl:template match="dbk:othercredit" mode="hub2htm-default">
    <p class="{if (@otherclass) then @otherclass else if (@class) then @class else local-name()}">
      <xsl:apply-templates mode="#current"/>
    </p>
  </xsl:template>
    
  <!-- toc -->
  
  <xsl:template match="dbk:toc | dbk:tocpart" mode="hub2htm-default">
    <div>
      <xsl:call-template name="css:content"/>
    </div>  
  </xsl:template>
  
  <xsl:template match="dbk:tocentry" mode="hub2htm-default">
    <p>
      <xsl:call-template name="css:content"/>
    </p>  
  </xsl:template>
  
  <xsl:template match="dbk:toc | dbk:tocpart | dbk:tocentry" mode="class-att">
    <xsl:attribute name="class" select="local-name()"/>
  </xsl:template>

  <!-- anchors and links -->
  
  <xsl:template match="dbk:link" mode="hub2htm-default">
    <a>
      <xsl:call-template name="css:content"/>
    </a>
  </xsl:template>
  
  <!-- external links -->
  <xsl:template match="@xlink:href" mode="hub2htm-default">
    <xsl:attribute name="href" select="."/>
  </xsl:template>
  
  <!-- cross references -->
  <xsl:template match="@linkend" mode="hub2htm-default">
    <xsl:attribute name="href" select="concat('#', .)"/>
  </xsl:template>
  
  <xsl:key name="linking-element-by-linkend" match="*[@linkend]" use="tokenize(@linkend, '\s+')"/>
  
  <xsl:template match="dbk:anchor[@xml:id and not(node())][exists(key('linking-element-by-linkend', @xml:id))]" mode="hub2htm-default">
    <a id="{@xml:id}"/>
  </xsl:template>
  
  <xsl:template match="dbk:anchor[@xml:id and not(node())][not(exists(key('linking-element-by-linkend', @xml:id)))]"
    mode="hub2htm-default" />
  
  <xsl:template match="dbk:figure/dbk:title | dbk:figure/dbk:info/dbk:title" mode="hub2htm-default">
    <p class="{'figure-title',  @role}">
      <xsl:call-template name="css:content"/>
    </p>
  </xsl:template>
  
  <xsl:template match="dbk:figure | dbk:informalfigure" mode="hub2htm-default">
    <xsl:element name="{if(parent::*[local-name() = ('para', 'title', 'phrase', 'sup', 'sub')]) then 'span' else 'div'}">
      <xsl:if test="@xml:id">
        <xsl:attribute name="id" select="@xml:id"/>
      </xsl:if>
      <xsl:attribute name="class" select="self::node()/name()"/>
      <xsl:apply-templates select="@srcpath" mode="#current"/>
      <xsl:apply-templates select="node() except dbk:title" mode="#current"/>
      <xsl:apply-templates select="dbk:title" mode="#current"/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="dbk:inlinemediaobject[dbk:imageobject/dbk:imagedata/svg:svg] 
                      |dbk:mediaobject[dbk:imageobject/dbk:imagedata/svg:svg]" mode="hub2htm-default">
    <xsl:choose>
      <xsl:when test="dbk:caption">
        <div class="img">
          <xsl:copy-of select="dbk:imageobject/dbk:imagedata/svg:svg"/>
          <xsl:apply-templates select="dbk:caption" mode="#current"/>
        </div>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="dbk:imageobject/dbk:imagedata/svg:svg"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="dbk:inlinemediaobject | dbk:mediaobject" mode="hub2htm-default"> 
    <!-- overwrite 'container:' prefix generated by docx2hub -->
    <xsl:variable name="src-dir-uri" select="/dbk:hub/dbk:info/dbk:keywordset[@role eq 'hub']/dbk:keyword[@role eq 'source-dir-uri']" as="xs:string"/>
    <xsl:variable name="fileref" select="(dbk:imageobject/dbk:imagedata/@fileref, .//@fileref, '')[1]" as="xs:string"/>
    <xsl:variable name="container-prefix-regex" select="'^container:(.+)$'" as="xs:string"/>
    <xsl:variable name="patched-fileref" select="
      if(matches($fileref, $container-prefix-regex )) 
      then concat( $src-dir-uri, replace( $fileref, $container-prefix-regex, '$1' )) 
      else $fileref"/>
    <!-- replace path and file extension if $overwrite-image-paths has the value 'yes' -->
    <xsl:variable name="src" select="
      if ($overwrite-image-paths eq 'yes') 
      then
        replace(
          tokenize(
            tokenize(
              $patched-fileref,
              '\\+'             (: windows paths :) 
            )[last()],
            '/'                 (: unix paths :)
          )[last()],
          '\.(eps|tiff?|wmf)$', 
          concat( '.', $image-suffix )
        )
      else $patched-fileref"/>
    <!-- construct img container -->
    <xsl:choose>
      <xsl:when test="dbk:caption">
        <div class="img">
          <img src="{ if($overwrite-image-paths eq 'yes') then concat( $image-path, '/', $src) else $src}" alt="{$src}">
            <xsl:apply-templates select="(@srcpath, dbk:imageobject/dbk:imagedata/@srcpath)[1], @css:height" mode="#current"/>
          </img>
          <xsl:apply-templates select="dbk:caption" mode="#current"/>
        </div>
      </xsl:when>
      <xsl:otherwise>
        <img src="{ if($overwrite-image-paths eq 'yes') then concat( $image-path, '/', $src) else $src}" alt="{$src}">
          <xsl:apply-templates select="(@srcpath, dbk:imageobject/dbk:imagedata/@srcpath)[1], @css:height" mode="#current"/>
        </img>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="dbk:mediaobject/@css:height" mode="hub2htm-default">
    <xsl:attribute name="style" select="concat('height:', .)"/>
  </xsl:template>
  
  <xsl:template match="dbk:caption" mode="hub2htm-default">
   <xsl:apply-templates select="node()" mode="#current"/> 
  </xsl:template>

  <xsl:template match="dbk:equation" mode="hub2htm-default">
    <div class="{name()}"><xsl:apply-templates select="@*, node()" mode="#current"/></div>
  </xsl:template>
  
  <xsl:template match="dbk:inlineequation" mode="hub2htm-default">
    <span class="{name()}"><xsl:apply-templates select="@*, node()" mode="#current"/></span>
  </xsl:template>
  
  <!-- remove namespace prefix in order to support mathml rendering via MathJax in most browsers -->
  <xsl:template match="mml:*" mode="hub2htm-default">
    <xsl:element name="{local-name()}" namespace="http://www.w3.org/1998/Math/MathML">
      <xsl:apply-templates select="@*, node()" mode="#current"/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="dbk:note" mode="hub2htm-default">
    <span class="{local-name()}"><xsl:apply-templates select="@*, node()" mode="#current"/></span>
  </xsl:template>

  <xsl:template match="dbk:bibliography | dbk:bibliodiv" mode="hub2htm-default">
    <xsl:element name="div">
      <xsl:attribute name="class" select="local-name()"/>
      <xsl:apply-templates select="@*, node()" mode="#current"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="dbk:bibliomixed" mode="hub2htm-default">
    <xsl:element name="p">
      <xsl:apply-templates select="@*, node()" mode="#current"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="dbk:bibliomisc" mode="hub2htm-default">
    <xsl:apply-templates select="node()" mode="#current"/>
  </xsl:template>

  
  <!--  * 
        * MODE hub2htm:css
        * -->
  
  <xsl:template match="html:head" mode="hub2htm:css">
    <xsl:copy>
      <xsl:apply-templates mode="#current"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="dbk:info" mode="hub2htm:css">
    <xsl:apply-templates mode="#current"/>
  </xsl:template>  
  
  <!--  *
        * MODE hub2htm-lists 
        * -->
  
  <xsl:template match="dbk:itemizedlist" mode="hub2htm-lists">
    <ul>
      <xsl:choose>
        <xsl:when test="@mark = ('&#8226;', 'bullet', 'disc')">
          <xsl:attribute name="class" select="'disc'"/>
        </xsl:when>
        <xsl:when test="@mark = 'o'">
          <xsl:attribute name="class" select="'circle'"/>
        </xsl:when>
        <xsl:when test="@mark = '&#9633;'">
          <xsl:attribute name="class" select="'square'"/>
        </xsl:when>
        <xsl:when test="@mark = ('&#8212;', '–')">
          <xsl:attribute name="class" select="'emdash'"/>
        </xsl:when>
        <xsl:when test="@mark = ('&#8211;', 'nomark', 'none', '')">
          <xsl:attribute name="class" select="'nomark'"/>
        </xsl:when>
        <xsl:otherwise/>
      </xsl:choose>
      <xsl:apply-templates mode="#current"/>
    </ul>
  </xsl:template>
  
  <xsl:template match="dbk:listitem" mode="hub2htm-lists">
    <li>
      <xsl:apply-templates select="@*, node()" mode="#current"/>
    </li>
  </xsl:template>

  <!-- CSS3 list-style-type:inline doesn’t work yet. Create a table if there are overrides -->
  <xsl:template match="@override" mode="hub2htm-lists"/>
  
  <xsl:template match="dbk:orderedlist" mode="hub2htm-lists">
    <xsl:variable name="numeration">
      <xsl:choose>
        <xsl:when test="@numeration = 'arabic'">
          <xsl:sequence select="'1'"/>
        </xsl:when>
        <xsl:when test="@numeration = 'lowerroman'">
          <xsl:sequence select="'i'"/>
        </xsl:when>
        <xsl:when test="@numeration = 'upperroman'">
          <xsl:sequence select="'I'"/>
        </xsl:when>
        <xsl:when test="@numeration = 'loweralpha'">
          <xsl:sequence select="'a'"/>
        </xsl:when>
        <xsl:when test="@numeration = 'upperalpha'">
          <xsl:sequence select="'A'"/>
        </xsl:when>
        <xsl:otherwise/>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="last-listitem-number">
      <xsl:number format="{$numeration}" value="count(dbk:listitem)"/>
    </xsl:variable>
    <xsl:variable name="first-listitem-number">
      <xsl:number value="1" format="{$numeration}"/>
    </xsl:variable>
    <xsl:variable name="redundant-overrides" as="xs:boolean"
      select="dbk:listitem[1]/@override = concat($first-listitem-number,'.')
              and
              dbk:listitem[last()]/@override = concat($last-listitem-number,'.')"/>
    <xsl:choose>
      <xsl:when test="$redundant-overrides 
                      or
                     (every $item in dbk:listitem satisfies $item[not(@override)])">
        <ol>
          <xsl:if test="$numeration ne ''">
            <xsl:attribute name="class" select="@numeration"/>
          </xsl:if>
          <xsl:for-each select="dbk:listitem">
            <li>
              <xsl:call-template name="css:content"/>
            </li>
          </xsl:for-each>
        </ol>
      </xsl:when>
      <xsl:otherwise>
        <table class="BC_orderedlist">
          <tbody>
            <xsl:for-each select="dbk:listitem">
              <tr>
                <td class="{string-join(('marker', @role), '-')}">
                  <p>
                    <xsl:copy-of select="*[1]/@class"/>
                    <xsl:value-of select="@override"/>
                  </p>
                </td>
                <td>
                  <xsl:apply-templates select="node()" mode="#current"/>
                </td>
              </tr>
            </xsl:for-each>
          </tbody>
        </table>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- variable lists -->
  
  <xsl:template match="dbk:variablelist" mode="hub2htm-lists">
    <dl>
      <xsl:apply-templates select="@srcpath, node()" mode="#current"/>
    </dl>
  </xsl:template>
  
  <xsl:template match="dbk:varlistentry" mode="hub2htm-lists">
    <xsl:apply-templates mode="#current"/>
  </xsl:template>
  
  <xsl:template match="dbk:varlistentry/dbk:term" mode="hub2htm-lists">
    <dt class="{hub2htm:elementname-and-role-attr-as-class-attrval(.)}">
      <xsl:apply-templates select="@srcpath, node()" mode="#current"/>
    </dt>
  </xsl:template>
  
  <xsl:template match="dbk:varlistentry/dbk:listitem" mode="hub2htm-lists">
    <dd class="{hub2htm:elementname-and-role-attr-as-class-attrval(.)}">
      <xsl:apply-templates select="@srcpath, node()" mode="#current"/>
    </dd>
  </xsl:template>
  
  <!--  * 
        * MODE hub2htm-cals2html
        * -->
  
  <xsl:template match="dbk:title[parent::*:table]" mode="hub2htm-cals2html">
    <caption>
      <xsl:apply-templates select="@srcpath, node()" mode="#current"/>
    </caption>
  </xsl:template>
  
  <xsl:template match="dbk:row" mode="hub2htm-cals2html">
    <tr>
      <xsl:apply-templates mode="#current"/>
    </tr>
  </xsl:template>
  
  <xsl:template match="dbk:tgroup" mode="hub2htm-cals2html">
    <xsl:apply-templates mode="#current"/>
  </xsl:template>
  
  <xsl:template match="dbk:tbody" mode="hub2htm-cals2html">
    <tbody>
      <xsl:apply-templates mode="#current"/>
    </tbody>
  </xsl:template>
  
  <xsl:template match="dbk:thead" mode="hub2htm-cals2html">
    <thead>
      <xsl:apply-templates mode="#current"/>
    </thead>
  </xsl:template>
  
  <xsl:template match="dbk:entry" mode="hub2htm-cals2html">
    <xsl:element name="{if (ancestor::dbk:thead) then 'th' else 'td'}">
      <xsl:if test="@namest">
        <!-- should be more robust than just relying on certain column name literals -->  
        <xsl:attribute name="colspan" select="number(replace(@nameend, '^c(ol)?', '')) - number(replace(@namest, 'c(ol)?', '')) + 1"
        />
      </xsl:if>
      <xsl:if test="@morerows &gt; 0">
        <xsl:attribute name="rowspan" select="@morerows + 1"/>
      </xsl:if>
      <xsl:apply-templates select="@srcpath, @class, @style, @align, @valign" mode="#current"/>
      <xsl:apply-templates mode="#current"/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="dbk:informaltable | dbk:table" mode="hub2htm-cals2html">
    <table>
      <xsl:apply-templates select="@*" mode="#current"/>
      <xsl:if test="@xml:id">
        <xsl:attribute name="id" select="@xml:id"/>
      </xsl:if>
      
      <xsl:choose>
        <xsl:when test="exists(dbk:tgroup/*/dbk:row)">
          <xsl:apply-templates mode="#current"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates mode="#current"/>
        </xsl:otherwise>
      </xsl:choose>
    </table>
  </xsl:template>
  
  <xsl:template match="dbk:tr | dbk:td | dbk:th" mode="hub2htm-cals2html">
    <xsl:element name="{local-name()}">
      <xsl:apply-templates select="@* | node()" mode="#current"/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="dbk:colspec" mode="hub2htm-cals2html"/>
  
  <xsl:template match="@frame" mode="hub2htm-cals2html">
    <xsl:attribute name="frame">
      <xsl:choose>
        <xsl:when test=".='all'">border</xsl:when>
        <xsl:when test=".='bottom'">below</xsl:when>
        <xsl:when test=".='none'">void</xsl:when>
        <xsl:when test=".='sides'">hsides</xsl:when>
        <xsl:when test=".='top'">above</xsl:when>
        <xsl:when test=".='topbot'">vsides</xsl:when>
      </xsl:choose>
    </xsl:attribute>
  </xsl:template>  

  <xsl:template match="@orient" mode="hub2htm-cals2html"/>
  
  <!--  *
        * MODE hub2htm-references
        * -->
  
  <xsl:template match="dbk:footnote" mode="hub2htm-references">
      <span class="footnote" id="fn-{generate-id()}">
        <sup class="footnote">
          <a href="#en-{generate-id()}">
            <!--<xsl:number level="any"/>-->
            <xsl:choose>
              <xsl:when test="matches(., '^\s*$')">
                <xsl:apply-templates select="(.//@srcpath)[1]" mode="#current"/>
                <xsl:text>&#xa0;</xsl:text>
              </xsl:when>
              <xsl:otherwise>
              	<xsl:copy-of select=".//html:span[descendant::text()][@class eq 'hub_identifier']"/>
              </xsl:otherwise>
            </xsl:choose>
          </a>
        </sup>
      </span>
  </xsl:template>
  
  <xsl:template match="dbk:footnote//html:span[@class eq 'hub_identifier']" mode="hub2htm-references"/>
  
  <xsl:template match="dbk:indexterm" mode="hub2htm-default">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:attribute name="xml:id" select="generate-id()"/>
      <xsl:apply-templates mode="#current"/>
    </xsl:copy>
    
  </xsl:template>
  
  <xsl:template match="dbk:indexterm" mode="hub2htm-references">
    <a class="indexterm" id="in-{@xml:id}"/><xsl:comment select="concat('indexterm: ', .)"/>
  </xsl:template>
  
  <xsl:template match="html:body" mode="hub2htm-references">
    <xsl:copy>
      <!-- toc -->
      <xsl:if test="$generate-toc eq 'yes'">
        <xsl:call-template name="generate-toc"/>
      </xsl:if>
      <!-- process content -->
      <xsl:apply-templates mode="#current"/>
      <!-- endnotes -->
      <xsl:if test="//dbk:footnote">
        <xsl:call-template name="generate-endnotes"/>
      </xsl:if>
      <!-- index -->
      <xsl:if test="$generate-index eq 'yes' and //dbk:indexterm[not(@role = 'hub:not-placed-on-page')]">
        <xsl:call-template name="generate-index"/>
      </xsl:if>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template name="generate-toc">
    <!-- toc wrapper -->
    <xsl:element name="{if($TARGET = ('EPUB3', 'HTML5')) then 'section' else 'div'}">
      <xsl:attribute name="class" select="'BC_toc'"/>
      <!-- toc headline -->
      <h1 title="{$headline-toc}" class="{$section-class-level1} toc-headline">
        <xsl:value-of select="$headline-toc"/>
      </h1>
      <!-- nav or div element -->
      <xsl:element name="{if($TARGET = ('EPUB3', 'HTML5')) then 'nav' else 'div'}">
        <xsl:if test="$TARGET = ('EPUB3', 'HTML5')">
          <xsl:attribute name="epub:type" select="'toc'"/>
        </xsl:if>
        <!-- iterate over headlines -->
        <ol class="toc-list">
          <xsl:for-each select="*[matches(name(), concat('[0-',$toc-level,']'))]">
            <li class="{concat('toc-entry toc-level-', local-name())}">
              <a href="#{@id}">
                <xsl:value-of select="string-join(.//text(), ' ')"/>
              </a>
            </li>
          </xsl:for-each>
        </ol>
      </xsl:element>
    </xsl:element>
  </xsl:template>
  
  <xsl:template name="generate-index">
    <div class="BC_index">
      <h1 title="{$headline-index}" class="{$section-class-level1} index_head"><xsl:value-of select="$headline-index"/></h1>
      <div class="index_list">
        <xsl:for-each-group select="//dbk:indexterm[not(@role = 'hub:not-placed-on-page')]" group-by="substring(upper-case(string-join(dbk:primary//text(),'')), 1, 1)">
          <xsl:sort select="upper-case(string-join(dbk:primary//text(),''))"/>
          <xsl:variable name="current-grouping-key" select="substring(upper-case(string-join(./dbk:primary//text(),'')), 1, 1)"/>
          <h2 title="{$current-grouping-key}" class="{$section-class-level2} index_subhead">
            <xsl:value-of select="$current-grouping-key"/>
          </h2>
          <xsl:for-each-group select="//dbk:indexterm[dbk:primary[node()] and substring(upper-case(string-join(dbk:primary//text(),'')), 1, 1) eq $current-grouping-key]" group-by="dbk:primary">
            <xsl:sort select="upper-case(string-join(dbk:primary//text(),''))"/>
            <xsl:variable name="primary-value" select="./dbk:primary"/>
            <xsl:variable name="primary-index-entries" select="//dbk:indexterm[dbk:primary = $primary-value]" as="node()*"/>
            <p class="index_primary">
              <xsl:choose>
                <xsl:when test="count($primary-index-entries) eq 1">
                  <a href="#in-{@xml:id}">
                    <xsl:apply-templates select="./dbk:primary/node()" mode="#current"/>
                  </a>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:apply-templates select="./dbk:primary[1]/node()" mode="#current"/>
                  <xsl:value-of select="'&#x20;'"/>
                  <xsl:for-each select="$primary-index-entries">
                    <a href="#in-{@xml:id}">
                      <xsl:value-of select="position()"/>
                    </a>
                    <xsl:if test="position() != last()">
                      <xsl:value-of select="', '"/>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:otherwise>
              </xsl:choose>
            </p>
            <xsl:for-each-group select="//dbk:indexterm[dbk:primary = $primary-value][dbk:secondary[node()]]" group-by="dbk:secondary">
              <xsl:sort select="upper-case(dbk:secondary)"/>
              <xsl:variable name="secondary-value" select="./dbk:secondary" as="xs:string"/>
              <xsl:variable name="secondaries" select="./dbk:secondary[. = $secondary-value]"/>
              <p class="index_secondary">
                <xsl:choose>
                  <xsl:when test="count($secondaries) eq 1">
                    <a href="#in-{@xml:id}">
                      <xsl:apply-templates select="./dbk:secondary/node()" mode="#current"/>
                    </a>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:apply-templates select="./dbk:secondary[1]/node()" mode="#current"/>
                    <xsl:value-of select="'&#x20;'"/>
                    <xsl:for-each select="$secondaries">
                      <a href="#in-{@xml:id}">
                        <xsl:value-of select="position()"/>
                      </a>
                      <xsl:if test="position() != last()">
                        <xsl:value-of select="', '"/>
                      </xsl:if>
                    </xsl:for-each>
                  </xsl:otherwise>
                </xsl:choose>
              </p>
              <xsl:for-each-group select="//dbk:indexterm[dbk:primary = $primary-value][dbk:secondary = $secondary-value][dbk:tertiary[node()]]" group-by="dbk:tertiary">
                <xsl:variable name="tertiary-value" select="./dbk:tertiary" as="xs:string"/>
                <xsl:variable name="tertiaries" select="//dbk:indexterm[dbk:primary = $primary-value][dbk:secondary = $secondary-value][dbk:tertiary = $tertiary-value]"/>
                <p class="index_tertiary">
                  <xsl:choose>
                    <xsl:when test="count($tertiaries) eq 1">
                      <a href="#in-{@xml:id}">
                        <xsl:apply-templates select="./dbk:tertiary/node()" mode="#current"/>
                      </a>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:apply-templates select="./dbk:tertiary[1]/node()" mode="#current"/>
                      <xsl:value-of select="'&#x20;'"/>
                      <xsl:for-each select="$tertiaries">
                        <a href="#in-{@xml:id}">
                          <xsl:value-of select="position()"/>
                        </a>
                        <xsl:if test="position() != last()">
                          <xsl:value-of select="', '"/>
                        </xsl:if>
                      </xsl:for-each>
                    </xsl:otherwise>
                  </xsl:choose>
                </p>
              </xsl:for-each-group>
            </xsl:for-each-group>
          </xsl:for-each-group>
        </xsl:for-each-group>
      </div>
    </div>
  </xsl:template>
  
  <xsl:template name="generate-endnotes">
    <div class="BC_endnotes">
      <xsl:if test="$headline-endnotes ne ''">
        <h1 title="{$headline-endnotes}" class="{$section-class-level1} endnotes-head">
          <xsl:value-of select="$headline-endnotes"/>
        </h1>
      </xsl:if>
      <!-- use html definition list because dd allows multiple paras in dd and inline content in dt -->
      <dl class="endnote-list">
        <xsl:for-each select="//dbk:footnote">
          <dt id="en-{generate-id()}" class="endnote">
            <a href="#fn-{generate-id()}">
              <xsl:copy-of select=".//*[descendant::text() and @class eq 'hub_identifier']"/>
            </a>
          </dt>
          <dd class="endnote-text">
            <xsl:apply-templates select="node()[not(@class eq 'hub_identifier')]|@*" mode="#current"/>
          </dd>
        </xsl:for-each>  
      </dl>
    </div>
  </xsl:template>
  
  <xsl:template match="dbk:title[dbk:title]" mode="hub2htm-default" priority="5">
    <xsl:apply-templates select="node()" mode="#current"/>  
  </xsl:template>
  
  <!-- Following elements need to be handled because a special client wants his HTML report made of the evolved-hub -->
  
  <xsl:template match="dbk:subtitle" mode="hub2htm-default">
    <p>
      <xsl:apply-templates select="@*, node() except dbk:info" mode="#current"/>  
    </p>
  </xsl:template>
  
  <xsl:template match="*[local-name() = ('epigraph', 'dedication')]" mode="hub2htm-default">
    <div class="{concat(name(.), ' ', @role)}">
      <xsl:apply-templates select="@* except @role, node()" mode="#current"/>  
    </div>
  </xsl:template>
  
  <!--  *
        * MODE remove namespaces
        * -->

    <xsl:template match="html:span[not(@*)]" mode="hub2htm-remove-ns">
      <xsl:apply-templates select="@* | node()" mode="#current"/>
    </xsl:template>

    <xsl:template match="*" mode="hub2htm-remove-ns">
      <xsl:copy copy-namespaces="no">
        <xsl:apply-templates select="@* | node()" mode="#current"/>
      </xsl:copy>
    </xsl:template>
  
    <xsl:template match="@xml:id" mode="hub2htm-remove-ns"/> 
	
	  <xsl:template match="@idml2xml:layer" mode="hub2htm-remove-ns"/> 
  
<!-- just a stub (don’t know what the mode should be doing, yet it has to be used in the stylesheet because
      it is invoked in the xpl -->
    <xsl:template match="* | @*" mode="hub2htm-figures-equations">
      <xsl:copy>
        <xsl:apply-templates select="@* | node()" mode="#current"/>
      </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
