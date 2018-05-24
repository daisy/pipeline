<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  version="3.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsw="http://coko.foundation/xsweet"
  xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="#all">

  <!-- Indent should really be no, but for testing. -->
  <xsl:output method="xml" indent="no" omit-xml-declaration="yes"/>

  <!-- XSweet: Further removal of redundant expression of formatting properties, especially in service of subsequent
    heuristics (where we need to see properties on paragraphs, not only their contents objects) .... [3e] -->
  <!-- Input: A messy noisy HTML document needing (yet more and even more) streamlining and cleanup. -->
  <!-- Output: A copy, with improvements. -->
  
  <!-- Copy everything by default. -->
  <xsl:template match="node() | @*">
    <xsl:copy>
      <xsl:apply-templates select="node() | @*"/>
    </xsl:copy>
  </xsl:template>

<!-- Rewrites CSS where p has all its contents in a single branch; display semantics
     of that branch are expressed in CSS and overwrite the p element's given @style.
     [examples]

  <p style="color: #000020; font-size: 13.5pt; margin-left: 72pt">
    <span style="color: #000020; font-size: 13.5pt">
      <i>All wholesome food is caught without a net or a trap.</i>
    </span>
  </p>

  should be rewritten

  <p style="color: #000020; font-size: 13.5pt; font-style: italic; margin-left: 72pt">All wholesome food is caught without a net or a trap.</p>

Note the properties overwritten on descendants are removed, and the 'i' element is rewritten as
font-style='italic'.

Note the following mappings:
  i - font-style='italic'.
  b - font-weight='bold'.
  u - text-decoration='underline'.

  -->

  <xsl:template match="p">
    <xsl:variable name="css-proxy" as="element()">
      <style>
        <xsl:apply-templates select="@style" mode="as-attributes"/>
        <xsl:call-template name="override-styles"/>
      </style>
    </xsl:variable>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <!-- Now overwriting @style ... -->
      <xsl:for-each select="$css-proxy[exists(@*)]">
        <!-- ... only when there are properties as attributes on $css-proxy ... -->
        <xsl:attribute name="style">
            <xsl:for-each select="@*">
              <xsl:sort data-type="text" select="name()"/>
              <xsl:if test="position() gt 1">; </xsl:if>
              <xsl:value-of select="name()"/>
              <xsl:text>: </xsl:text>
              <xsl:value-of select="."/>
            </xsl:for-each>
        </xsl:attribute>
      </xsl:for-each>
      <!--<xsl:copy-of select="$css-proxy"/>-->
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <!-- We can strip 'span' elements when they are coextensive with their wrapping p and
       have nothing but @style to offer, as the latter is being promoted. -->
<!-- NB suppressing this until it can test whether a wrapping @class doesn't warrant its retention
     see https://gitlab.coko.foundation/XSweet/XSweet/issues/140 -->
  <!--<xsl:template match="p//span[empty(@class)]
    [normalize-space(.) = normalize-space(ancestor::p[1]) and count(../*) eq 1]">
    <xsl:apply-templates/>
  </xsl:template>-->

  <!-- Note we leave 'u', 'i' and 'b' in place despite also promoting them to CSS. -->

  <xsl:template name="override-styles">
    <!-- Under certain conditions, descends tree to collect CSS style property assignments
      returning them as attributes (captured on a proxy). -->
    <xsl:if test="count(* except tab) eq 1 and normalize-space(.) = normalize-space((* except tab)[1])">
      <xsl:for-each select="* except tab">
        <xsl:apply-templates select=". | @style" mode="as-attributes"/>
        <!-- descend recursively -->
        <xsl:call-template name="override-styles"/>
      </xsl:for-each>

    </xsl:if>
  </xsl:template>


<!-- 'as-attributes mode' loads up a proxy element with CSS properties. We exploit
     the fact that attributes overwrite other attributes of the same name, added
     earlier (since attributes must be uniquely named) to de-duplicate our CSS ...
  i.e. font-size will come out only once, with whatever value was declared deepest.

  Note that elements as well as @style values will prompt CSS properties being added in this way. -->
  <xsl:template match="*" mode="as-attributes"/>

  <xsl:template match="u" mode="as-attributes">
    <xsl:attribute name="text-decoration">underline</xsl:attribute>
  </xsl:template>

  <xsl:template match="i" mode="as-attributes">
    <xsl:attribute name="font-style">italic</xsl:attribute>
  </xsl:template>

  <xsl:template match="b" mode="as-attributes">
    <xsl:attribute name="font-weight">bold</xsl:attribute>
  </xsl:template>

  <xsl:template match="@style" mode="as-attributes">
    <xsl:for-each select="tokenize(.,'\s*;\s*')">
      <xsl:attribute name="{replace(.,':.*$','')}" select="replace(.,'^.*:\s*','')"/>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
