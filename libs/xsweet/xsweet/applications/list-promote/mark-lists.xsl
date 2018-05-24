<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsw="http://coko.foundation/xsweet"
  xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"

  exclude-result-prefixes="#all">

  <xsl:output method="xml" indent="no" omit-xml-declaration="yes"/>

  <xsl:template match="node() | @*">
    <xsl:copy>
      <xsl:apply-templates select="node() | @*"/>
    </xsl:copy>
  </xsl:template>

  <!-- This comes out of XSweet. -->
  <xsl:template match="div[@class='docx-body']">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="sequence-lists"/>
    </xsl:copy>
  </xsl:template>

  <!-- We can also subject sections to sectioning if they have anything more than a single
       header, or if their header is preceded by contents.
       Other sections should be considered already well-structured and passed through intact. -->
  <!-- (But a better approach to this may be to strip sections on the way in, then rebuild them.) -->
  <!--<xsl:template match="section[exists( (h1|h2|h3|h4|h5|h6)[1]/(preceding-sibling::* |
    following-sibling::*/(self::h1|self::h2|self::h3|self::h4|self::h5|self::h6)))]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="sequence-by-headers"/>
    </xsl:copy>
  </xsl:template>-->

  <xsl:template name="sequence-lists">
    <!-- The template emits a sequence of groups, each containing a cluster of elements belonging to a header
         (or not, if elements precede any headers). While it is flat, the level of each
         group is represented directly as @level - making induction easier over the groups. -->
      <xsl:for-each-group select="*" group-adjacent="exists(xsw:list-level(.))">
        <!-- grouping key is true() for nominal list items, false if not. -->
        <xsl:choose>
          <xsl:when test="current-grouping-key()">
            <!-- build a nested stack of lists for the sequence -->
            <xsl:call-template name="nest-lists"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="current-group()"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each-group>
  </xsl:template>

  <xsl:template name="nest-lists">
    <xsl:param name="level" select="0"/>
    <xsl:param name="who" select="current-group()"/>
    <xsw:list level="{$level}">
        <xsl:for-each-group select="$who" group-adjacent="xsw:list-level(.) gt $level">
          <!-- grouping key is true() for nominal list items, false if not. -->
          <xsl:choose>
            <xsl:when test="current-grouping-key()">
              <xsl:call-template name="nest-lists">
                <xsl:with-param name="level" select="$level + 1"/>
                <xsl:with-param name="who"   select="current-group()"/>
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates select="current-group()"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each-group>
    </xsw:list>

  </xsl:template>

  <xsl:function name="xsw:list-level" as="xs:integer?">
    <xsl:param name="whose" as="element()"/>
    <xsl:variable name="list-assignment" select="tokenize($whose/@style,'\s*;\s*')[matches(.,'xsweet-list-level:')]"/>
    <!-- level is only the value, ordinarily a whole number (zero or positive integer) -->
    <xsl:sequence select="if (exists($list-assignment)) then xs:integer(replace($list-assignment,'^.*list-level:\s*','')) else ()"/>
  </xsl:function>

</xsl:stylesheet>
