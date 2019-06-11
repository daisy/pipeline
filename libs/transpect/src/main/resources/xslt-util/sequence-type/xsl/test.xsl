<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tr="http://transpect.io"
  exclude-result-prefixes="xs tr"
  version="2.0">
  
  <xsl:import href="sequence-type.xsl"/>
  
  <xsl:output method="text"/>
  
  <xsl:template name="test">
    <xsl:variable name="i" select="1" as="xs:integer"/>
    <xsl:message select="$i, tr:sequenceType($i)"/>
    <xsl:variable name="j" select="1"/>
    <xsl:message select="$j, tr:sequenceType($j)"/>
    <xsl:variable name="k" select="1" as="xs:anyAtomicType"/>
    <xsl:message select="$k, tr:sequenceType($k)"/>
    <xsl:variable name="l" select="1" as="xs:double"/>
    <xsl:message select="$l, tr:sequenceType($l)"/>
    <xsl:variable name="m" select="true()" as="xs:boolean"/>
    <xsl:message select="$m, tr:sequenceType($m)"/>
    <xsl:variable name="n" select="'1'" as="xs:string"/>
    <xsl:message select="$n, tr:sequenceType($n)"/>
    <xsl:variable name="u" select="document-uri(doc(''))" as="xs:anyURI"/>
    <xsl:message select="$u, tr:sequenceType($u)"/>
    <xsl:variable name="foo" as="document-node(element(foo))">
      <xsl:document>
        <foo bar="baz"/>
      </xsl:document>
    </xsl:variable>
    <xsl:message select="$foo, tr:sequenceType($foo)"/>
    <xsl:variable name="foo2">
      <foo bar="baz"/>
    </xsl:variable>
    <xsl:message select="$foo2, tr:sequenceType($foo2)"/>
    <xsl:variable name="foo3" as="element(foo)">
      <foo bar="baz"/>
    </xsl:variable>
    <xsl:message select="$foo3, tr:sequenceType($foo3)"/>
    <xsl:variable name="att" as="attribute()">
      <xsl:attribute name="foo" select="'bar'"/>
    </xsl:variable>
    <xsl:message select="$att, tr:sequenceType($att)"/>
    <xsl:variable name="pi" as="processing-instruction()">
      <xsl:processing-instruction name="foo" select="'bar'"/>
    </xsl:variable>
    <xsl:message select="$pi, tr:sequenceType($pi)"/>
    <xsl:variable name="comment" as="comment()">
      <xsl:comment select="'foo=bar'"/>
    </xsl:variable>
    <xsl:message select="$comment, tr:sequenceType($comment)"/>
  </xsl:template>
  
</xsl:stylesheet>