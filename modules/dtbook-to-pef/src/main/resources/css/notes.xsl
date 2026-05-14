<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="org.daisy.pipeline.css.StyleAccessor"
                xmlns="http://www.daisy.org/z3986/2005/dtbook/"
                xpath-default-namespace="http://www.daisy.org/z3986/2005/dtbook/"
                exclude-result-prefixes="#all">

  <!--
      - Move note elements if notes-placement is "end-of-block",
      - otherwise generate a div element to contain all the notes and to attach a title.
  -->

  <xsl:param name="notes-placement" as="xs:string" select="'end-of-book'"/>
  <xsl:param name="endnotes-section-class" as="xs:string" select="''"/>
  <xsl:param name="chapter-selector" as="xs:string" select="''"/>

  <xsl:param name="style"/>

  <xsl:key name="idref" match="*[@id]" use="concat('#',@id)"/>

  <xsl:template match="/*">
    <xsl:choose>
      <xsl:when test="$notes-placement=('bottom-of-page','end-of-volume','end-of-book')
                      and $endnotes-section-class!=''
                      and //noteref">
        <xsl:copy>
          <xsl:sequence select="@*|node()"/>
          <div class="{$endnotes-section-class}"/>
        </xsl:copy>
      </xsl:when>
      <xsl:when test="$notes-placement='end-of-chapter'
                      and $endnotes-section-class!=''
                      and $chapter-selector!=''
                      and //noteref">
        <xsl:apply-templates mode="insert-chapter-notes-sections" select="."/>
      </xsl:when>
      <xsl:when test="$notes-placement='end-of-block'">
        <xsl:apply-templates mode="move-notes" select="."/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="move-notes" match="noteref" as="element()*">
    <xsl:next-match/>
    <xsl:if test="@idref">
      <xsl:for-each select="key('idref',@idref)[1]">
        <xsl:copy>
          <xsl:apply-templates mode="#current" select="@*|node()"/>
        </xsl:copy>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <xsl:template mode="move-notes" match="note"/>

  <xsl:template mode="move-notes" match="*" as="element()*">
    <xsl:variable name="nodes" as="node()*">
      <xsl:apply-templates mode="#current" select="node()"/>
    </xsl:variable>
    <xsl:variable name="note-nodes" as="element()*" select="$nodes/self::note"/>
    <xsl:choose>
      <xsl:when test="exists($note-nodes) and css:get($style,.,'display')=('inline','none')">
        <xsl:copy>
          <xsl:apply-templates mode="#current" select="@*"/>
          <xsl:sequence select="$nodes[not(self::note)]"/>
        </xsl:copy>
        <xsl:sequence select="$note-nodes"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates mode="#current" select="@*"/>
          <xsl:sequence select="$nodes[not(self::note)]"/>
        </xsl:copy>
        <xsl:if test="exists($note-nodes)">
          <div>
            <xsl:sequence select="$note-nodes"/>
          </div>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="insert-chapter-notes-sections" match="*">
    <xsl:copy>
      <xsl:apply-templates mode="#current" select="@*|node()"/>
      <xsl:if test="css:matches($style,.,$chapter-selector)">
        <div class="{$endnotes-section-class}"/>
      </xsl:if>
    </xsl:copy>
  </xsl:template>

  <xsl:template mode="move-notes
                      insert-chapter-notes-sections"
                match="@*|node()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates mode="#current" select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
