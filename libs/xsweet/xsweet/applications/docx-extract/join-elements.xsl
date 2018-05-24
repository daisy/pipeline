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


  <!-- XSweet: Further reduces haphazard redundancy in markup by joining adjacent elements with similar properties .... [3d] -->
  <!-- Input: A messy noisy HTML document needing (yet more) streamlining and cleanup. -->
  <!-- Output: A copy, with improvements. -->
  
  <!-- Copy everything by default. -->
  <xsl:template match="node() | @*">
    <xsl:copy>
      <xsl:apply-templates select="node() | @*"/>
    </xsl:copy>
  </xsl:template>

  <!-- Copy 'p', and merge what's inside it. -->
  <xsl:template match="p">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:call-template name="collapse-ilk"/>
    </xsl:copy>
  </xsl:template>

  <!-- Merge logic accepts a sequence of nodes (by default,
       children of the context node where called) and returns
       sequences where 'like' nodes in sequence are merged.
       So <u>Moby </u><u>Dick</u> comes back <u>Moby Dick</u>.

       'Likeness' is established by xsw:node-signature, and permits elements
       of the same type and attribute values to be merged accorrding
       to logic given in the hashing templates.
  -->

  <xsl:template name="collapse-ilk">
    <xsl:param name="among" select="node()"/>
    <xsl:for-each-group select="$among" group-adjacent="xsw:node-signature(.)">
      <xsl:choose>
        <xsl:when test="exists(current-group()/self::*)">
          <xsl:for-each select="current-group()[self::*][1]">
            <!-- In the element case, splice in an element. -->
            <xsl:copy>
              <xsl:copy-of select="@*"/>
              <xsl:call-template name="collapse-ilk">
                <xsl:with-param name="among" select="current-group()/(node(),self::text())"/>
              </xsl:call-template>
            </xsl:copy>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="current-group()"/>
        </xsl:otherwise>
      </xsl:choose>
      <!-- Splice in anything not an element. -->
    </xsl:for-each-group>
  </xsl:template>

  <xsl:function name="xsw:node-signature" as="xs:string">
    <xsl:param name="n" as="node()"/>
    <xsl:value-of separator="|">
      <xsl:apply-templates mode="signature" select="$n"/>
    </xsl:value-of>
  </xsl:function>

  <!-- Note we're going to collapse things with the same (local) name
       though in different namespaces - this ain't lookin to be namespace safe. -->
  <xsl:template mode="signature" match="*">
    <xsl:value-of select="local-name()"/>
    <xsl:for-each select="@*">
      <xsl:sort select="local-name()"/>
      <xsl:if test="position() ne 1"> ::: </xsl:if>
      <xsl:apply-templates mode="#current" select="."/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template mode="signature" match="@*">
    <xsl:value-of select="local-name(),." separator="="/>
  </xsl:template>

  <!-- These guys should never collapse so their hash is always unique to them.-->
  <xsl:template mode="signature" match="div | p | tab">
    <xsl:value-of select="local-name(.)"/>
    <xsl:value-of select="generate-id(.)"/>
  </xsl:template>

  <!-- ws-only text nodes, PIs and comments should be merged with adjacent elements
       iff those nodes are being merged together. -->
  <xsl:template mode="signature" match="text() | comment() | processing-instruction()">
    <xsl:variable name="fore" select="preceding-sibling::*[1]/xsw:node-signature(.)"/>
    <xsl:variable name="aft"  select="following-sibling::*[1]/xsw:node-signature(.)"/>
    <xsl:value-of select="if ($fore = $aft) then $fore else generate-id(.)"/>
  </xsl:template>

  <!-- However, text nodes that are not ws-only should not merge with adjacent elements
       even when they are alike e.g. <i>there</i> it goes <i>again</i> ... -->
  <!-- This template has a supervening priority over the preceding one, i.e. 0.5 to -0.5 -->
  <xsl:template mode="signature" match="text()[matches(.,'\S')]">
    <xsl:value-of select="generate-id(.)"/>
  </xsl:template>

</xsl:stylesheet>
