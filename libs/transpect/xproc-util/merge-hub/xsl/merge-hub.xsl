<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:cx="http://xmlcalabash.com/ns/extensions" 
  xmlns:dbk="http://docbook.org/ns/docbook"
  xmlns:css="http://www.w3.org/1996/css"
  xmlns:csstmp="http://transpect.io/csstmp"  
  xmlns:functx="http://www.functx.com"
  xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
  xmlns:hub="http://transpect.io/hub"
  xmlns:s="http://purl.oclc.org/dsdl/schematron"
  exclude-result-prefixes="xs" 
  xpath-default-namespace="http://docbook.org/ns/docbook" 
  version="2.0">


  <!-- used to insert sourcepaths -->
  <xsl:import href="http://www.functx.com/XML_Elements_and_Attributes/XML_Document_Structure/path-to-node-with-pos.xsl"/>
  

  <xsl:param name="move-dir-components-to-srcpath" as="xs:string" />
  <xsl:param name="space-separated-docVar-merge" as="xs:string" />
  <xsl:variable name="movecount" select="xs:integer($move-dir-components-to-srcpath)" as="xs:integer"/>
  <xsl:variable name="docVar-merge" select="tokenize($space-separated-docVar-merge,',\s*')" as="xs:string*"/>

  
  <xsl:param name="value-separator" select="'~~~'"/>

  <!-- identity template -->
  <xsl:template match="* | processing-instruction() | comment() | @*" mode="merge-hub">
    <xsl:copy copy-namespaces="no">
      <xsl:apply-templates select="@*, node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/cx:document" mode="merge-hub">
    <xsl:apply-templates select="*[1]" mode="#current"/>
  </xsl:template>

  <xsl:function name="hub:get-srcpath-prefix" as="xs:string">
    <xsl:param name="info-node" as="element(info)"/>
    <xsl:variable name="local-tokenized-path" as="xs:string+"
      select="tokenize(
                    $info-node/keywordset[@role eq 'hub']/keyword[@role eq 'source-dir-uri'],
                    '/'
                  )[not(. = '' or empty(.))]"/>
    <xsl:value-of select="concat(string-join($local-tokenized-path[position() gt last() - $movecount], '/'), '/')"/>
  </xsl:function>

  <xsl:template match="/cx:document/*" mode="merge-hub">
    <xsl:apply-templates select="/cx:document/*[1]/(@* except @xml:base)"/>
    <xsl:apply-templates mode="#current">
      <xsl:with-param name="srcpath-prefix" select="hub:get-srcpath-prefix(info)" tunnel="yes"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="/cx:document/*[1]" mode="merge-hub" priority="2">
    <xsl:variable name="tokenized-path" 
      select="tokenize(
                info/keywordset[@role eq 'hub']/keyword[@role eq 'source-dir-uri'],
                '/'
              )[not(. = '' or empty(.))]" as="xs:string*"/>
    <xsl:variable name="new-source-dir-uri" as="xs:string"
      select="concat(string-join($tokenized-path[position() le last() - $movecount], '/'), '/')" />
    <xsl:copy>
      <xsl:copy-of select="@* except @xml:base"/>
      <xsl:apply-templates select="info" mode="#current">
        <xsl:with-param name="other-css-rules" tunnel="yes" as="element(css:rules)*"
          select="/cx:document/*[position() gt 1][not(@role = 'placeholder')]/info/css:rules"/>
        <xsl:with-param name="other-keywords" tunnel="yes" as="element(keywordset)*" 
          select="/cx:document/*[position() gt 1][not(@role = 'placeholder')]/info/keywordset"/>
        <xsl:with-param name="new-source-dir-uri" select="$new-source-dir-uri" as="xs:string" tunnel="yes"/>
        <xsl:with-param name="multiple-value-docVars" tunnel="yes" as="element(keyword)*" 
          select="/cx:document/*[position() gt 1][not(@role = 'placeholder')]/info/keywordset/keyword[@role = $docVar-merge]"/>
      </xsl:apply-templates>
      <xsl:for-each select="/cx:document/*[not(@role = 'placeholder')]">
        <xsl:apply-templates select="node() except info" mode="#current">
          <xsl:with-param name="srcpath-prefix" select="hub:get-srcpath-prefix(info)" tunnel="yes"/>
        </xsl:apply-templates>  
      </xsl:for-each>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/cx:document/*[position() ne 1]/info" mode="merge-hub"/>

  <!-- match first css:rules element -->
  <xsl:template match="/cx:document/*[1]/info/css:rules" mode="merge-hub">
    <xsl:param name="other-css-rules" tunnel="yes" as="element(css:rules)*"/>
    <xsl:copy>
      <!-- group CSS rules by style name -->
      <xsl:for-each-group select="(. union $other-css-rules)/css:rule" group-by="@name">
        <xsl:variable name="style-name" select="current-grouping-key()" as="xs:string"/>

        <!-- group CSS properties by property name, e.g. css:font-size -->
        <xsl:copy>
          <xsl:apply-templates select="@*[namespace-uri() ne 'http://www.w3.org/1996/css']" mode="#current"/>
          
          <xsl:for-each-group select="current-group()/@css:*" group-by="name()">
            <xsl:variable name="css-distinct-values" select="distinct-values(current-group())"/>
            <!-- if a CSS property differs among one style, use the first value and create a Schematron Message -->

            <xsl:choose>
              <xsl:when test="count($css-distinct-values) gt 1">

                <!-- message for terminal output and SVRL -->
                <xsl:variable name="message" select="concat('[WARNING] merge-hub.xsl: Style ', $style-name, ' differs in ', name(), ': ', string-join($css-distinct-values, ', '), ' (using first value)')"/>
                <xsl:message select="$message"/>

                <!-- use only first attribute value -->
                <xsl:attribute name="{name()}" select="$css-distinct-values[1]"/>

                <!-- write other values separated in a single attribute to process them later -->
                <xsl:attribute name="{concat('csstmp:', local-name())}" select="string-join($css-distinct-values, $value-separator)"/>

              </xsl:when>
              <xsl:otherwise>
                <xsl:attribute name="{name()}" select="$css-distinct-values"/>
              </xsl:otherwise>
            </xsl:choose>

          </xsl:for-each-group>

          <xsl:apply-templates select="node()" mode="#current"/>

        </xsl:copy>

      </xsl:for-each-group>
    </xsl:copy>
  </xsl:template>

  <!-- match other css:rules elements. this template has got lower priority -->
  <xsl:template match="/cx:document/*[position() ne 1]/info/css:rules" mode="merge-hub"/>

  <xsl:template match="/" mode="generate-report">
    <svrl:schematron-output>
      
      <svrl:active-pattern id="ambiguous-css-property" name="ambiguous-css-property"/>
      
      <xsl:apply-templates select="//css:rule[@csstmp:*]" mode="generate-report"/>
    </svrl:schematron-output> 
  </xsl:template>

  <xsl:template match="*[@csstmp:*]" mode="generate-report">
    <xsl:variable name="srcpath" select="functx:path-to-node-with-pos(.)" as="xs:string"/>
    <xsl:variable name="style-name" select="@name" as="xs:string"/>
    <xsl:for-each select="@csstmp:*">
      <svrl:fired-rule context="{$srcpath}"/>
      <svrl:failed-assert 
        test="css-property" id="merge-hub-style-ambiguosity"
        location="{$srcpath}">
        <svrl:text>
          <s:span class="srcpath"><xsl:value-of select="$srcpath"/></s:span>
          <s:span class="css"><xsl:value-of select="concat('Style ''', $style-name ,''' with ambiguous CSS property ''', local-name(), '''. Multiple values (', string-join(tokenize(., $value-separator), ', '), ') defined. Using first value.')"/></s:span>
        </svrl:text>
      </svrl:failed-assert>  
    </xsl:for-each>
    
  </xsl:template>

  <xsl:template mode="merge-hub" 
    match="/cx:document/*/info/keywordset[@role eq 'hub']/keyword[@role eq 'source-dir-uri']">
    <xsl:param name="new-source-dir-uri" as="xs:string" tunnel="yes"/>
    <xsl:copy copy-namespaces="no">
      <xsl:apply-templates select="@*" mode="#current"/>
      <xsl:value-of select="$new-source-dir-uri"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template mode="merge-hub" match="/cx:document/*/info/keywordset/keyword[@role = $docVar-merge]" priority="2">
    <xsl:param name="multiple-value-docVars" tunnel="yes"/>
    <xsl:copy copy-namespaces="no">
      <xsl:apply-templates select="@*" mode="#current"/>
      <xsl:value-of select="string-join((./text(),$multiple-value-docVars[@role=current()/@role]//text()),';')"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="/cx:document/*/info/keywordset" mode="merge-hub">
    <xsl:param name="other-keywords" as="element(keywordset)*" tunnel="yes"/>
    <xsl:copy copy-namespaces="no">
      <xsl:apply-templates mode="#current" 
        select="@*, keyword, $other-keywords[@role = current()/@role]/keyword[not(@role = current()/keyword/@role)]"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="@srcpath" mode="merge-hub">
    <xsl:param name="srcpath-prefix" as="xs:string" tunnel="yes"/>
    <xsl:attribute name="{name()}" select="for $i in tokenize(.,' ') return concat($srcpath-prefix, $i)"/>
  </xsl:template>
  
</xsl:stylesheet>
