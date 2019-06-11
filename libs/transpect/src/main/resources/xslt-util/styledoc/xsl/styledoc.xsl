<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:css="http://www.w3.org/1996/css" 
  xmlns:dbk="http://docbook.org/ns/docbook"
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:html="http://www.w3.org/1999/xhtml"
  xmlns:hub2htm="http://transpect.io/hub2htm" 
  exclude-result-prefixes="xs css dbk"
  version="2.0">
  
  <!-- remember to check out https://subversion.le-tex.de/common/hub2html_simple/trunk/ and add something like
    <rewriteURI uriStartString="http://transpect.io/hub2html/" rewritePrefix="/your/checkout/dir"/>
    to your catalog. -->
  <xsl:import href="http://transpect.io/hub2html/xsl/css-rules.xsl"/>

  <xsl:output method="xhtml"
    doctype-public="-//W3C//DTD XHTML 1.0//EN"
    doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"/>

  <!-- sample invocation:
    saxon -xsl:converter/xsl/styledoc.xsl -it:main uri=file:/$(cygpath -ma a9s) -o:doc/XML-Satzrichtlinien/styles.xhtml
    On Unix systems, it should work with realpath instead of cygpath. Or use a URL relative to xsl (uri=../../a9s).
  -->

  <xsl:param name="uri" as="xs:string"/>
  <xsl:param name="use-local-js" as="xs:string" select="'yes'"/>
  
  <xsl:variable name="all-style-docs" as="document-node(element(all-style-docs))">
    <xsl:document>
      <all-style-docs xmlns="">
        <xsl:apply-templates select="collection(concat($uri, '?select=cssa.xml;recurse=yes'))" mode="add-base-uri"/>
      </all-style-docs>
    </xsl:document>
  </xsl:variable>
  
  <xsl:template match="/*" mode="add-base-uri">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:attribute name="xml:base" select="base-uri()"/>
      <xsl:sequence select="node()"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template name="main">
    <html>
      <head>
        <title>
          <xsl:text>Styles</xsl:text>
        </title>
        <style type="text/css">
          h1.no-frills {
              cursor: default;
          }
          h1.heading {
              background-color:#00539f;
              color:white;
              font-size:150%;
          }
          h1, h2, h3, h4, h5{ 
              cursor:pointer;
              padding:0.25em;
              margin:0em;
          }
       
          h1{ 
             font-size:1.3em;
             color:#00539f;
          }
          h2{ 
              font-size:1.15em;
              color:#00539f;
          }
         
          h3{
             color:#444;
             font-size:1.05;
          }
          h4{
              font-size:0.9em;
            }
          h5{
              font-size:0.85em;
              }
          
          body{
              font-family:sans-serif;
              margin:1% 5%;
              background-color:#fff;
              padding:1em;
              color:#333;
          }
          p{
              margin:0em;
              padding:0.1em;
              color:#111;
              clear:both;
              font-size:0.8em;
          }
          
          p.general{
              margin:1em 0em 2em 0em;
              padding:0.1em;
              color:#111;
              clear:both;
              text-indent:0em;
              font-size:0.8em;
          }          
          p.template-names {
              background-color: #ddf;
              margin-left: 5em;
          }

          p:nth-child(odd){
              background-color:#e5e5e5;
          }
          div.nest{
              margin:0em 0em 1em 1em;
              border-left:0.5px solid #e5e5e5;
              border-top:0.5px solid #e5e5e5;
              border-bottom:0.5px solid #e5e5e5;
          }
          span.unfold-all,
          span.fold-all{
              cursor:pointer;
              display:inline-block;
              width:1.5em;
              background-color:#00539f;
              color:#fff;
              font-size:1.5em;
              padding:0.1em;
              text-align:center;
              margin-right:1em;
          }
          span.status{
            display:inline;
            width:5%;
            float:right;
            background-color:#eee8aa;
            color:#000;
            font-size:0.7em;
            padding:0.1em 0.5em;
            text-align:left;
            margin-left:1em;
          } 
          span.right{
          display:inline;
          width:40%;
          float:right;
          font-size:0.7em;
          padding:0.1em 0.5em;
          text-align:left;
          margin-left:1em;
          }
          span.comment{
          background-color:#ffc0cb;
          color:#000;
          }
          li{
            font-size:0.8em;
          }
          a{
            color:#00539f;
            border-bottom: 1px solid #ccc;
            text-decoration:none;
          }
            a:visited {
            color:#a0522d;
            border-bottom: 1px solid #ccc;
          } 
          
            a:hover {
            color:#fff;
            background-color:#00539f;
          }          

          <xsl:apply-templates select="$all-style-docs/all-style-docs/css:rules/css:rule" mode="serialize-css"/>
        </style>
        <script type="text/javascript" src="{if ($use-local-js = 'yes')
                                            then '../js/jquery-1.10.0.js'
                                            else 'https://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js'}"></script>
        

      </head>
      <body>
        <div class="folder">
          <span class="unfold-all" title="unfold all">&#8634;
          </span>
          <span class="fold-all" title="fold all">&#8635;
          </span>
        </div>
        <p class="general">This document contains lists of styles that are permitted for each series.
          It will contain all styles that are available in the sample document, not just the styles that are actually used.<br/>
          You may use style variants with the same base name, but with an additional name part after the character '~'. 
          These variants will usually be converted in the same way as the basic style.<br/>
          <b>Example:</b><br/>
          p_list and p_list~margin will both be treated as a p_list when it comes to identifying list items.<br/>
          Any styling that deviates from the basic p_list will be converted to local overrides.<br/>
          Local overrides will be converted to css:* attributes (e.g., 2&#xa0;mm extra space below will
          become css:margin-bottom="2mm"), and these overrides will typically be carried along throughout
          the conversion steps. (A notable exception being list items, where spacing above the first or below 
          the last list item will be interpreted as an indicator that the list isn’t included in the adjacent 
          paragraphs, but rather is on the same level as the paragraphs, causing a larger vertical space
          in further rendering stages.<br/>
          If you don’t feel like creating derivative, but semantically equivalent styles in the way described 
          above, but rather would like to use the basic style together with local overrides, you might as well 
          do so. Since tilde style deviations are converted to local overrides, they are equivalent.</p>
          
        
        <h1 class="no-frills heading">ToC</h1>
        <ol>
          <li><a href="#by-series">Styles by Series</a> (individual style hierarchy for each template)</li>
          <li><a href="#by-styles">Styles by Consolidated Style Hierarchy</a></li>
        </ol>
        <hr/>
        <div id="by-series">
          <h1 class="no-frills heading">Styles by Series (individual style hierarchy for each template)</h1>
          <xsl:apply-templates select="$all-style-docs/all-style-docs//css:rules" >
            <xsl:with-param name="type" select="'by-templates'" tunnel="yes"/>
          </xsl:apply-templates>  
        </div>
        <hr/>
        <div id="by-style">
          <h1 class="heading">&#8681;&#160;
            Styles by Consolidated Style Hierarchy</h1>
          <xsl:apply-templates select="$all-style-docs/all-style-docs">
            <xsl:with-param name="type" select="'by-style'" tunnel="yes"/>
          </xsl:apply-templates>
        </div>
        <script type="text/javascript">
          
          $(".nest").hide();
          
          $("h1").click(function () {
          $(this).next("div.nest").slideToggle();
          });
          
          $("h2").click(function () {
          $(this).next("div.nest").slideToggle().children("div.nest").slideToggle().children("div.nest").slideToggle().children("div.nest").slideToggle();
          }); 
          
          $("h3").click(function () {
          $(this).next("div.nest").slideToggle();
          }); 
          
          $("h4").click(function () {
          $(this).next("div.nest").slideToggle();
          }); 
          
          $("h5").click(function () {
          $(this).next("div.nest").slideToggle();
          });           
          
          $("span.unfold-all").click(function () {
          $("div.nest").slideDown();
          });
          
          $("span.fold-all").click(function () {
          $("div.nest").slideUp();
          });
          
        </script>
      </body>
    </html>
  </xsl:template>
  
  <xsl:function name="css:template-name" as="xs:string">
    <xsl:param name="uri" as="xs:string"/>
    <xsl:sequence select="if (matches($uri, '/(a9s|content)/(.+?)/styles/cssa.xml$'))
                          then replace($uri, '^.+/(a9s|content)/(.+?)/styles/cssa.xml$', '$2')
                          else replace($uri, '^.+/(.+?)/cssa.xml$', '$1')"/>
  </xsl:function>
  
  <xsl:template match="all-style-docs | css:rules" >
    <xsl:param name="type" as="xs:string" tunnel="yes"/>
    <xsl:if test="$type eq 'by-templates'">
      <h1>&#8681;&#160;
        <xsl:value-of select="css:template-name(base-uri())"/>
      </h1>  
    </xsl:if>
    <div class="nest">
      <!-- css:rule is for the 'by-series' case, css:rules/css:rule is for 'by-style' -->
      <xsl:for-each-group select="css:rule | css:rules/css:rule" group-by="@layout-type">
        <h2>&#8681;&#160; <xsl:value-of select="current-grouping-key()"/>
        </h2>
        <xsl:sequence select="css:group-hierarchic-styles(current-group(), 1, $type)"/>
      </xsl:for-each-group>
    </div>  
  </xsl:template>
  
  <xsl:function name="css:group-hierarchic-styles">
    <xsl:param name="rules" as="element(css:rule)+"/>
    <xsl:param name="level" as="xs:integer"/>
    <xsl:param name="type" as="xs:string"/>
    <div class="nest">
      <xsl:for-each-group select="$rules" group-by="tokenize(@native-name, ':')[$level]">
        <xsl:sort select="current-grouping-key()"/>
        <xsl:variable name="deeper-nested" select="current-group()[tokenize(@native-name, ':')[$level + 1]]"
          as="element(css:rule)*"/>
        <xsl:for-each-group select="current-group() except $deeper-nested" group-by="@native-name">
          <xsl:sort select="current-grouping-key()"/>
          <!-- render the first item (or the only item, in the 'by-series' case): -->
          <xsl:apply-templates select=".">
            <xsl:with-param name="type" select="$type" tunnel="yes"/>
          </xsl:apply-templates>
          <xsl:if test="$type eq 'by-style'">
            <p class="template-names">
              <!-- another group here because there were duplicates (don't know why!): -->
              <xsl:for-each-group select="current-group()" group-by="css:template-name(base-uri())">
                <xsl:sort/>
                <xsl:apply-templates select="." mode="render-template-name"/>
              </xsl:for-each-group>  
            </p>
          </xsl:if>
        </xsl:for-each-group>
        <xsl:if test="exists($deeper-nested)">
          <xsl:element name="h{$level + 2}">&#8681;&#160;
            <xsl:value-of select="current-grouping-key()"/>
          </xsl:element>
          <xsl:sequence select="css:group-hierarchic-styles(current-group(), $level + 1, $type)"/>
        </xsl:if>
      </xsl:for-each-group>
    </div>
  </xsl:function>
  
  <xsl:template match="css:rule">
    <xsl:param name="type" as="xs:string" tunnel="yes"/>
    <p class="{generate-id()}">
      <xsl:value-of select="tokenize(@native-name, ':')[last()]"/>
      <xsl:if test="$type eq 'by-templates'">
        <xsl:if test="exists(@status)">
          <span class="status">
            <xsl:value-of select="@status"/>
          </span>
        </xsl:if>
        <xsl:if test="exists(@comment)">
          <span class="comment right">
            <xsl:value-of select="@comment"/>
          </span>
        </xsl:if>
      </xsl:if>
    </p>
  </xsl:template>

  <xsl:template match="css:rule" mode="render-template-name">
    <span class="{generate-id()}">
      <xsl:choose>
        <xsl:when test="@comment">
          <span title="{@comment}" class="comment">
            <xsl:sequence select="css:template-name(base-uri())"/>
          </span>
        </xsl:when>
        <xsl:otherwise>
          <xsl:sequence select="css:template-name(base-uri())"/>
        </xsl:otherwise>
      </xsl:choose>
    </span>
    <xsl:text>&#x2002; </xsl:text>
  </xsl:template>
  
  <xsl:template match="css:rule" mode="serialize-css">
    <xsl:variable name="transformed" as="attribute(*)*">
      <xsl:apply-templates select="@css:*" mode="hub2htm:css-style-defs"/>
      <!--<xsl:attribute name="css:display" select="css:display-type(.)"/>-->
    </xsl:variable>
    <xsl:text>&#xa;.</xsl:text>
    <xsl:value-of select="generate-id()"/>
    <xsl:text>:hover {</xsl:text>
    <xsl:for-each select="$transformed">
      <xsl:text>&#xa;    </xsl:text>
      <xsl:value-of select="local-name()"/>
      <xsl:text>: </xsl:text>
      <xsl:value-of select="."/>
      <xsl:text>;</xsl:text>
    </xsl:for-each>
    <xsl:text>&#xa;  }</xsl:text>
  </xsl:template>

</xsl:stylesheet>