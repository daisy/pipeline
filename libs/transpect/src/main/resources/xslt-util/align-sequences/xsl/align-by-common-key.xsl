<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tr="http://transpect.io"
  xmlns:sequence-align="http://transpect.io/sequence-align"
  xmlns:math="http://www.w3.org/2005/xpath-functions/math"
  xmlns:dbk="http://docbook.org/ns/docbook"
  exclude-result-prefixes="xs math tr sequence-align"
  version="3.0">

  <xsl:param name="terminate-on-error" as="xs:boolean" select="true()"/>
  
  <xsl:function name="sequence-align:wrap" as="element(sequence-align:wrap)">
    <xsl:param name="seq" as="node()*"/>
    <xsl:param name="n" as="xs:integer"/>
    <xsl:param name="key-expr" as="xs:string"/>
    <xsl:choose>
      <xsl:when test="exists ($seq/../@n) 
                      and 
                      (every $s in $seq satisfies exists($s/self::sequence-align:item))">
        <xsl:sequence select="$seq/.."/>
      </xsl:when>
      <xsl:otherwise>
        <sequence-align:wrap n="{$n}">
          <xsl:for-each select="$seq">
            <sequence-align:item n="{$n}">
              <xsl:attribute name="key">
                <xsl:evaluate xpath="$key-expr" context-item="."/>
              </xsl:attribute>
              <xsl:sequence select="."/>
            </sequence-align:item>
          </xsl:for-each>
        </sequence-align:wrap>    
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
  <xsl:function name="sequence-align:align-two" as="element(sequence-align:wrap)">
    <xsl:param name="n1" as="xs:integer"/>
    <xsl:param name="_doc1" as="element(*)"/>
    <xsl:param name="n2" as="xs:integer"/>
    <xsl:param name="_doc2" as="element(*)"/>
    <xsl:param name="key-expr" as="xs:string"/>
    <xsl:variable name="is1" as="element(sequence-align:item)*" select="sequence-align:wrap($_doc1/*, $n1, $key-expr)/*"/>
    <xsl:variable name="is2" as="element(sequence-align:item)*" select="sequence-align:wrap($_doc2/*, $n2, $key-expr)/*"/>
    <xsl:variable name="matching1" as="node()*" select="$is1[@key = $is2/@key]"/>
    <xsl:variable name="matching2" as="node()*" select="$is2[@key = $is1/@key]"/>
    <sequence-align:wrap n="{$matching1[1]/@n} {$matching2[1]/@n}">
      <xsl:variable name="seq1-before" as="node()*" select="$is1[. &lt;&lt; $matching1[1]]"/>
      <xsl:if test="exists($seq1-before)">
        <xsl:sequence select="$seq1-before"/>
      </xsl:if>
      <xsl:variable name="seq2-before" as="node()*" select="$is2[. &lt;&lt; $matching2[1]]"/>
      <xsl:if test="exists($seq2-before)">
        <xsl:sequence select="$seq2-before"/>
      </xsl:if>
      <xsl:for-each select="$matching1">
        <xsl:variable name="m1pos" as="xs:integer" select="sequence-align:index-of($matching1, .)"/>
        <xsl:variable name="m1next" as="node()?" select="$matching1[position() = $m1pos + 1]"/>
        <xsl:variable name="m2" select="$is2[@key = current()/@key]" as="node()*"/>
        <xsl:if test="count($m2) gt 1">
          <xsl:message terminate="{$terminate-on-error}">Duplicates: <xsl:sequence select="$m2/@key"/></xsl:message>
        </xsl:if>
        <xsl:variable name="m2pos" as="xs:integer+" select="sequence-align:index-of($matching2, $m2)"/>
        <xsl:if test="count($m2pos) gt 1">
          <xsl:message select="'MATCHING2 ', $matching2/@key, '&#xa;M2 ',$m2/@key"/>
        </xsl:if>
        <xsl:variable name="m2next" as="node()?" select="$matching2[position() = $m2pos + 1]"/>
        <!--<xsl:if test="position() = 1">
          <xsl:variable name="seq2-before-m2" as="node()*" select="$is2[not(. &gt;&gt; $m2)] except $m2"/>
          <xsl:if test="exists($seq2-before-m2)">
            <xsl:sequence select="$seq2-before-m2"/>
          </xsl:if>
        </xsl:if>-->
        <sequence-align:item key="{@key}" n="{@n} {$m2/@n}">
          <xsl:sequence select="., $m2"/>
        </sequence-align:item>
        <xsl:sequence select="$is1[. >> current()][. &lt;&lt; $m1next]"/>
        <xsl:sequence select="$is2[. >> $m2][. &lt;&lt; $m2next]"/>
      </xsl:for-each>
      <xsl:variable name="seq1-after" as="node()*" select="$is1[. >> $matching1[last()]]"/>
      <xsl:if test="exists($seq1-after)">
        <xsl:sequence select="$seq1-after"/>
      </xsl:if>
      <xsl:variable name="seq2-after" as="node()*" select="$is2[. >> $matching2[last()]]"/>
      <xsl:if test="exists($seq2-after)">
        <xsl:sequence select="$seq2-after"/>
      </xsl:if>
    </sequence-align:wrap>
  </xsl:function>
  
  <xsl:function name="sequence-align:index-of" as="xs:integer*">
    <xsl:param name="set" as="node()*"/>
    <xsl:param name="srch" as="node()*"/>
    <xsl:sequence select="for $gi in $srch/generate-id() return index-of($set/generate-id(), $gi)"/>
  </xsl:function>
  
  <xsl:function name="sequence-align:align" as="element(sequence-align:wrap)">
    <xsl:param name="_docs" as="element(*)+"/>
    <xsl:param name="key-expr" as="xs:string"/>
    <xsl:choose>
      <xsl:when test="count($_docs) gt 2">
        <xsl:apply-templates 
          select="sequence-align:align-two(count($_docs) - 1, 
                                           sequence-align:align($_docs[position() lt last()], $key-expr),
                                           count($_docs),
                                           sequence-align:wrap($_docs[position() = last()]/*, count($_docs), $key-expr),
                                           $key-expr
                                          )" mode="sequence-align:flatten"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="sequence-align:align-two(1, 
                                                       sequence-align:wrap($_docs[1]/*, 1, $key-expr),
                                                       2,
                                                       sequence-align:wrap($_docs[2]/*, 2, $key-expr),
                                                       $key-expr
                                                      )"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
  <xsl:mode name="sequence-align:flatten" on-no-match="shallow-copy"/>
  
  <xsl:template match="sequence-align:item[parent::sequence-align:item][sequence-align:item]" mode="sequence-align:flatten">
    <xsl:apply-templates mode="#current"/>
  </xsl:template>
  
  <xsl:mode name="sequence-align:ignore-indexterms-and-footnotes" on-no-match="shallow-copy"/>
  
  <xsl:function name="sequence-align:normalize-for-key" as="xs:string" visibility="public">
    <xsl:param name="context" as="element(*)"/>
    <xsl:variable name="prelim" as="element(*)">
      <xsl:apply-templates select="$context" mode="sequence-align:ignore-indexterms-and-footnotes"/>
    </xsl:variable>
    <xsl:sequence select="normalize-space($prelim)"/>
  </xsl:function>
  
  <xsl:template match="dbk:indexterm | dbk:footnote" mode="sequence-align:ignore-indexterms-and-footnotes"/>
  
</xsl:stylesheet>