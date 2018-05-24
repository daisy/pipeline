<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsw="http://coko.foundation/xsweet"
  xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="#all">

  <!-- Indent should really be no, but for testing. -->
  <xsl:output method="xml" indent="no" omit-xml-declaration="yes"/>

  <!-- XSweet: notes cleanup, step 2 of regular docx extraction .... [3b] -->
  <!-- Input: A messy noisy HTML document straight out of docx-extract.xsl -->
  <!-- Output: A copy, with some regularization with respect specifically to footnotes and endnotes ... -->
  
  <xsl:param as="xs:string" name="footnote-format">a</xsl:param>
  <xsl:param as="xs:string" name="endnote-format" >1</xsl:param>
  
  <!-- Copy everything by default. -->
  <xsl:template match="node() | @*">
    <xsl:copy>
      <xsl:apply-templates select="node() | @*"/>
    </xsl:copy>
  </xsl:template>


<!-- This stylesheet repairs endnotes and their references:
     discarding unreferenced endnotes and re-ordering notes by order of first reference. -->

  <xsl:key name="endnote-by-id"  match="p[@class='docx-endnote']"  use="@id"/>
  <xsl:key name="footnote-by-id" match="p[@class='docx-footnote']" use="@id"/>

  <!--<a class="endnoteReference" href="#en{@w:id}">-->

  <!-- Retrieveing endnotes, keep only those that are referenced, in their order of reference. -->
  <xsl:template match="div[@class='docx-endnotes']">
    <xsl:variable name="notes" select="div[@class='docx-endnote']"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <!-- We only capture end notes that have actually been referenced, in their order of referencing. -->
      <xsl:for-each
        select="../div[@class = 'docx-body']//a[@class = 'endnoteReference'][xsw:is-first-enref(.)]">
        <xsl:variable name="href" select="@href"/>
        <xsl:apply-templates select="$notes[concat('#', @id) = $href]"/>
      </xsl:for-each>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="div[@class='docx-footnotes']">
    <xsl:variable name="notes" select="div[@class='docx-footnote']"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:for-each
        select="../div[@class = 'docx-body']//a[@class = 'footnoteReference'][xsw:is-first-fnref(.)]">
        <xsl:variable name="href" select="@href"/>
        <xsl:apply-templates select="$notes[concat('#', @id) = $href]"/>
      </xsl:for-each>
    </xsl:copy>
  </xsl:template>

  <!-- Expand placeholders for links with generated numbers if they have no values yet. -->
  <xsl:template match="a[@class=('endnoteReference','footnoteReference')][not(normalize-space(.))]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="." mode="get-number"/>
    </xsl:copy>
  </xsl:template>

  <!--<span class="endnoteRef"/> inside end note text was produced from w:endnoteRef, and also requires expansion. -->
  <xsl:template match="span[@class='endnoteRef'][not(normalize-space(.))]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates mode="get-number"
        select="key('endnoteRef-by-href',concat('#',ancestor::div[@class='docx-endnote']/@id))[1]"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="span[@class='footnoteRef'][not(normalize-space(.))]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates mode="get-number"
        select="key('footnoteRef-by-href',concat('#',ancestor::div[@class='docx-footnote']/@id))[1]"/>
    </xsl:copy>
  </xsl:template>

  <!-- Since notes need to be counted, only first references to notes can
       be counted, and the order of notes depends on the order of (first)
       references to them, so that's the set of elements we need for counting. -->

  <xsl:key name="endnoteRef-by-href"  match="a[@class='endnoteReference']"   use="@href"/>
  <xsl:key name="footnoteRef-by-href" match="a[@class='footnoteReference']"  use="@href"/>

  <xsl:function name="xsw:is-first-enref" as="xs:boolean">
    <xsl:param name="enref" as="element(a)"/>
    <!-- Boolean returns true() iff this is the first endnoteRef with its @id (and hence target). -->
    <xsl:sequence select="($enref/@class='endnoteReference') and ($enref is key('endnoteRef-by-href',$enref/@href,root($enref))[1])"/>
  </xsl:function>

  <xsl:function name="xsw:is-first-fnref" as="xs:boolean">
    <xsl:param name="fnref" as="element(a)"/>
    <!-- Boolean returns true() iff this is the first footnoteRef with its @id (and hence target). -->
    <xsl:sequence select="($fnref/@class='footnoteReference') and ($fnref is key('footnoteRef-by-href',$fnref/@href,root($fnref))[1])"/>
  </xsl:function>

  <xsl:template match="a[@class='endnoteReference']" mode="get-number">
    <xsl:for-each select="key('endnoteRef-by-href',@href)[1]">
      <xsl:number level="any" format="{$endnote-format}" count="a[@class='endnoteReference'][xsw:is-first-enref(.)]"/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="a[@class='footnoteReference']" mode="get-number">
    <xsl:for-each select="key('footnoteRef-by-href',@href)[1]">
      <xsl:number level="any" format="{$footnote-format}" count="a[@class='footnoteReference'][xsw:is-first-fnref(.)]"/>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
