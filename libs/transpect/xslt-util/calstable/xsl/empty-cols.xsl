<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:calstable="http://docs.oasis-open.org/ns/oasis-exchange/table"
  exclude-result-prefixes="xs"
  version="2.0">
  
  <xsl:import href="functions.xsl"/>
  
  <!-- return all @colnum values, where there is no entry containing either:
    - text
    - or at least one text-like-element (string matching elements local-name())
    The second option is useful images or footnote-refs, containing no text() themselves
  -->
  <xsl:function name="calstable:empty-cols" as="xs:integer*">
    <xsl:param name="tgroup" as="element(tgroup)"/>
    <xsl:param name="text-like-elements" as="xs:string+"/>
    <xsl:sequence select="$tgroup/colspec/@colnum[calstable:is-empty-col(., $tgroup, $text-like-elements)]"/>
  </xsl:function>
  
  <!-- return true if there is no entry using the col specified by colnum directly or indirectly in a namest/nameend span.
  Not using means the same as in function calstable:empty-cols#2
  -->
  <xsl:function name="calstable:is-empty-col" as="xs:boolean">
    <xsl:param name="colnum" as="xs:decimal"/>
    <xsl:param name="tgroup" as="element(tgroup)"/>
    <xsl:param name="text-like-elements" as="xs:string+"/>
    <xsl:variable name="colnames" select="$tgroup/colspec[@colnum=$colnum]/@colname"/>
    <xsl:variable name="entries" select="$tgroup/*/row/(entry[@colname = $colnames], entry[@namest][calstable:is-col-in-span($colnum, @namest, @nameend, $tgroup/colspec)])"/>
    <xsl:sequence select="empty(($entries//text(), $entries//*[local-name() = $text-like-elements]))"/>
  </xsl:function>
  
  <!-- templates where only text should be matched, no element to -->
  <!-- return all @colnum values, where there is no entry containing text -->
  <xsl:function name="calstable:empty-cols" as="xs:integer*">
    <xsl:param name="tgroup" as="element(tgroup)"/>
    <xsl:sequence select="calstable:empty-cols($tgroup, '')"/>
  </xsl:function>
  
  <!-- return true if there is no entry containing text, using the col specified by colnum directly or indirectly in a namest/nameend span -->
  <xsl:function name="calstable:is-empty-col" as="xs:boolean">
    <xsl:param name="colnum" as="xs:decimal"/>
    <xsl:param name="tgroup" as="element(tgroup)"/>
    <xsl:sequence select="calstable:is-empty-col($colnum, $tgroup, '')"/>
  </xsl:function>

</xsl:stylesheet>
