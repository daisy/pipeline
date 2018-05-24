<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsw="http://coko.foundation/xsweet"
  xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml">


  <!-- XSweet: produces header promotion XSLT from analyzed (crunched) inputs, for the 'property-based' header promotion pathway [3b] -->
  <!-- Input: results of running `digest-paragraphs.xsl` on (wf) HTML input -->
  <!-- Output: an XSLT suitable for running on the same (original) input to produce a copy with headers promoted -->
  
  <xsl:output indent="yes"/>
  
  <!-- Example input - all these have been identified as headers -
    <div class="hX">
      <div class="level-group">
        <p style="font-weight: bold"/>
      </div>
      <div class="hX">
        <p style="font-size: 10pt; font-weight: bold"/>
      </div>
      <div class="hX">
        <p style="font-size: 10pt; font-style: italic; font-weight: bold"/>
      </div>
    </div>
-->

  <xsl:namespace-alias stylesheet-prefix="xsw" result-prefix="xsl"/>

  <xsl:param name="debug-mode" as="xs:string">silent</xsl:param>

  <!-- Note that generated stylesheet will error if $extra-match-criteria is anything but an XPath filter expression
       i.e. '[ booleanExp ]' (with square brackets).
       Exposing it as a parameter isn't recommended unless we can defend against arbitrary XPath injection. -->
  <xsl:variable name="extra-match-criteria">[empty(ancestor::table|ancestor::li)][ancestor::*/@class='docx-body'][string-length(.) &lt;= 200][matches(.,'\S')]</xsl:variable>

  <xsl:template match="body">

    <!--       xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
      xmlns:xs="http://www.w3.org/2001/XMLSchema"
      xmlns:xsw="http://coko.foundation/xsweet"
      xmlns="http://www.w3.org/1999/xhtml"

    -->
    <xsw:stylesheet version="3.0"
      xpath-default-namespace="http://www.w3.org/1999/xhtml"
      exclude-result-prefixes="#all">

      <xsw:output method="xml"  omit-xml-declaration="yes"/>

      <xsw:template match="node() | @* | /html">
        <xsw:copy>
          <xsw:apply-templates select="node() | @*"/>
        </xsw:copy>
      </xsw:template>

      <xsl:apply-templates select="div[@class='grouped']/div[@class='hX']/*" mode="xslt-produce"/>
      <!--<xsl:apply-templates mode="xslt-produce" select="$p-proxies-grouped/*"/>-->
      
      <xsl:if test="not($debug-mode='silent')">
      <xsw:variable name="in">
        <xsl:copy-of select="div"/>
      </xsw:variable>
      </xsl:if>

    </xsw:stylesheet>
  </xsl:template>

  <!--<xsl:include href="digest-paragraphs.xsl"/>-->
  
  <!-- Template writes XSLT templates  -->

  <xsl:template match="div[@class='hX']/*" mode="xslt-produce">
    <xsl:variable name="match">
      <xsl:value-of select="local-name()"/>
      <xsl:for-each select="@class">
        <xsl:text>[@class/tokenize(.,'\s+') = '</xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>']</xsl:text>
      </xsl:for-each>
      <xsl:for-each select="@style/tokenize(.,'\s*;\s*')">
        <xsl:text>[@style/tokenize(.,'\s*;\s*') = '</xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>']</xsl:text>
      </xsl:for-each>
      <!-- If font-style was not given explicity it must be missing to match. -->
      <xsl:if test="empty(@style/tokenize(.,'\s*;\s*')[starts-with(.,'font-size:')])">
        <xsl:text>[empty(@style/tokenize(.,'\s*;\s*')[starts-with(.,'font-size:')])]</xsl:text>
      </xsl:if>
      <xsl:if test="@data-always-caps='true'">
        <xsl:text>[.=upper-case(.)]</xsl:text>
      </xsl:if>
      <xsl:copy-of select="$extra-match-criteria"/>
    </xsl:variable>
    <xsw:template match="{$match}">
      <xsl:variable name="h-level" select="count(..|../following-sibling::div[@class='hX'])"/>
      <xsl:attribute name="priority" select="count(. | preceding-sibling::* | ../preceding-sibling::div[@class='hX']/*)"/>
      <xsw:element name="h{$h-level}">
        <xsw:copy-of select="@*"/>
        <!--<xsw:comment> was <xsl:value-of select="$match"/> </xsw:comment>-->
        <xsw:apply-templates/>
      </xsw:element>
    </xsw:template>
  </xsl:template>

</xsl:stylesheet>
