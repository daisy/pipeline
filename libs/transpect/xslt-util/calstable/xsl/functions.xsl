<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:calstable="http://docs.oasis-open.org/ns/oasis-exchange/table"
  exclude-result-prefixes="xs"
  version="2.0">
  
  <!-- return true if entry is in first col -->
  <xsl:function name="calstable:in-first-col" as="xs:boolean">
    <xsl:param name="entry" as="element()"/><!-- entry in any namespace -->
    <xsl:param name="colspecs" as="element()+"/>
    <xsl:sequence select="($entry/@colname, $entry/@namest) = $colspecs[@colnum = 1]/@colname"/>
  </xsl:function>
  
  <!-- return true if entry is in last col -->
  <xsl:function name="calstable:in-last-col" as="xs:boolean">
    <xsl:param name="entry" as="element()"/><!-- entry in any namespace -->
    <xsl:param name="colspecs" as="element()+"/>
    <xsl:sequence select="$colspecs[($entry/@colname, $entry/@nameend)[1] = @colname]/@colnum = max($colspecs/@colnum)"/>
  </xsl:function>
  
  <!-- return true if for some $e in entries: the colname/namest of entry is spanned by $e-->
  <xsl:function name="calstable:entry-overlaps" as="xs:boolean">
    <xsl:param name="entry" as="element()"/><!-- entry in any namespace -->
    <xsl:param name="entries" as="element()*"/><!-- entry+ in any namespace -->
    <xsl:param name="colspecs" as="element()+"/>
    <xsl:sequence select="
      exists($entries[ 
        calstable:is-col-in-span(
          xs:decimal($colspecs[($entry/@colname, $entry/@namest)[1] = @colname]/@colnum),
          (@colname, @namest)[1],
          (@colname, @nameend)[1],
          $colspecs
        )
      ])
      "/>
  </xsl:function>
  
  <!-- return true if entry span includes colnum -->
  <xsl:function name="calstable:is-entry-in-col" as="xs:boolean">
    <xsl:param name="entry" as="element()"/><!-- entry in any namespace -->
    <xsl:param name="colnum" as="xs:decimal"/>
    <xsl:param name="colspecs" as="element()+"/>
    <xsl:sequence
      select="calstable:is-col-in-span($colnum, ($entry/@colname, $entry/@namest)[1], ($entry/@colname, $entry/@nameend)[1], $colspecs)"
    />
  </xsl:function>
  
  <!-- return true if col for colnum is beetween namest and nameend (inclusive), assuming ascending numeration -->
  <xsl:function name="calstable:is-col-in-span" as="xs:boolean">
    <xsl:param name="colnum" as="xs:decimal"/>
    <xsl:param name="namest" as="xs:string"/>
    <xsl:param name="nameend" as="xs:string"/>
    <xsl:variable name="colspecs" as="element()+"><!-- colspec in any namespace -->
      <xsl:for-each select="(1 to xs:integer(replace($nameend, '^col', '')))">
        <xsl:element name="colspec" namespace="">
          <xsl:attribute name="colnum" select="."/>
          <xsl:attribute name="colname" select="concat('col', .)"/>
        </xsl:element>
      </xsl:for-each>
    </xsl:variable>
    <xsl:sequence select="calstable:is-col-in-span($colnum, $namest, $nameend, $colspecs)"/>
  </xsl:function>
  
  <!-- return true if col for colnum is beetween namest and nameend (inclusive) -->
  <xsl:function name="calstable:is-col-in-span" as="xs:boolean">
    <xsl:param name="colnum" as="xs:decimal"/>
    <xsl:param name="namest" as="xs:string"/>
    <xsl:param name="nameend" as="xs:string"/>
    <xsl:param name="colspecs" as="element()+"/><!-- colspec in any namespace -->
    <xsl:choose>
      <xsl:when test="$colspecs[@colname = $namest]/@colnum and $colspecs[@colname = $nameend]/@colnum">
        <xsl:variable name="start" select="$colspecs[@colname = $namest]/@colnum" as="xs:integer"/>
        <xsl:variable name="end" select="$colspecs[@colname = $nameend]/@colnum" as="xs:integer"/>
        <xsl:sequence select="$colnum = ($start to $end)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="false()"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
  <!-- return true if width1 is greater or equal to width2. Unit string 'pt' may be appended. -->
  <xsl:function name="calstable:first-width-ge" as="xs:boolean">
    <xsl:param name="width1" as="xs:string"/>
    <xsl:param name="width2" as="xs:string"/>
    <xsl:sequence select="replace($width1, 'pt', '') ge replace($width2, 'pt', '')"/>
  </xsl:function>
</xsl:stylesheet>