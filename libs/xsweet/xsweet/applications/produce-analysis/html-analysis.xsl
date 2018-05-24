<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:xsw="http://coko.foundation/xsweet"
  
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="#all">
  
  <!-- XSweet: Digests into a single-file "report" format an HTML file, to show its output (result structures) for diagnostics. -->
  <!-- Input: An HTML file probably "HTML Typescript" in spirit (i.e. the fairly cleanish output of an XSweet extraction pipeline).-->
  <!-- Output: An HTML file (open in a plain-ordinary browser) documenting a 'structural synopsis' of the HTML as given. -->
  <!-- Note: For diagnostic purposes, this is rude but useful. Also could be extended and polished for better results. -->
  
<!-- Produces an HTML report describing (HTML) inputs. -->
  
  <xsl:template match="/">
    <xsl:call-template name="page">
      <xsl:with-param name="contents">
        <xsl:call-template name="analysis"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template name="analysis">
    <div>
      <h2>Elements by signature <tt style="font-size: smaller">(name.class ::: @style)</tt></h2>
      <ul>
      <xsl:for-each-group select="//*" group-by="xsw:signature(.)">
        <xsl:sort select="count(current-group())" order="descending"/>
          <li>
            <span class="e"><xsl:value-of select="current-grouping-key()"/></span>
            <span class="count"><xsl:value-of select="count(current-group())"/></span>
          </li>
      </xsl:for-each-group>
      </ul>
    </div>
    <div>
      <h2>Abstract tree hierarchy (by signature)</h2>
      <xsl:call-template name="element-hierarchy"/>
    </div>
    
  </xsl:template>
  
  <xsl:template name="element-hierarchy">
    <xsl:param name="n" select="/*/*"/>
    <xsl:if test="exists($n)">
      <ul>
        <xsl:for-each-group select="$n" group-by="xsw:signature(.)">
          <li>
            <span class="e">
              <xsl:value-of select="current-grouping-key()"/>
            </span>
            <span class="count">
              <xsl:value-of select="count(current-group())"/>
            </span>
            <xsl:call-template name="element-hierarchy">
              <xsl:with-param name="n" select="current-group()/*"/>
            </xsl:call-template>
          </li>
        </xsl:for-each-group>
      </ul>
    </xsl:if>
  </xsl:template>
  
  <xsl:function name="xsw:signature" as="xs:string">
    <xsl:param name="n" as="element()"/>
    <xsl:value-of>
      <xsl:value-of select="name($n)"/>
      <xsl:value-of select="$n/@class/ (for $t in (tokenize(.,'\s+')) return concat('.',$t))"/>
      <xsl:for-each select="$n/@style">
        <xsl:text> ::: </xsl:text>
        <xsl:value-of select="."/>
      </xsl:for-each>
    </xsl:value-of>
  </xsl:function>
  
  <xsl:template name="page">
    <xsl:param name="contents">
      <xsl:apply-templates/>
    </xsl:param>
    <html>
      <head>
        <meta charset="UTF-8"><!-- comment me --></meta>
        <style type="text/css">
.e { color: darkgreen; font-weight: bold }
.count { color: darkred; font-style: italic }
          
        </style>
      </head>
      <body>
        <xsl:copy-of select="$contents"/>
      </body>
    </html>
  </xsl:template>
  
</xsl:stylesheet>