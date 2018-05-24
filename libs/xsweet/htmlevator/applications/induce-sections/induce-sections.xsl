<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:math="http://www.w3.org/2005/xpath-functions/math"
  xmlns:xsw="http://coko.foundation/xsweet"
  xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  
  exclude-result-prefixes="#all"
  version="3.0">
  
  <!-- XSweet: Flat HTML becomes well structured HTML. This XSLT, using micropipelines, combines the of `mark-sections.xsl` followed by `nest-sections.xsl`, except here in a single XSLT. (Use this one!) [1] -->
  <!-- Input: HTML Typescript, with header elements indicating section demarcations -->
  <!-- Output: the same, except with the sections marked. -->
  <!-- Note: also marks the result with a comment if headers are found out of expected sequence. -->
  
  <xsl:output method="xml" indent="no" omit-xml-declaration="yes"/>
  
  <xsl:template match="node() | @*">
    <xsl:copy copy-namespaces="no">
      <xsl:apply-templates select="node() | @*"/>
    </xsl:copy>
  </xsl:template>
  
  <!--<xsl:template match="body/div[xsw:has-regular-order(h1|h2|h3|h4|h5|h6) or true()]">-->
  <xsl:template match="body/div[@class='docx-body']">
    <xsl:variable name="sequenced">
      <xsl:call-template name="sequence-by-headers"/>
    </xsl:variable>
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:if test="not(xsw:has-regular-order(h1|h2|h3|h4|h5|h6))">
        <xsl:comment expand-text="true"> Headers out of regular order: { string-join((h1|h2|h3|h4|h5|h6)/name(), ', ') }</xsl:comment>
      </xsl:if>
      <xsl:apply-templates select="$sequenced/xsw:sequence"/>
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
  
  <xsl:template name="sequence-by-headers" as="element(xsw:sequence)">
    <!-- The template emits a sequence of groups, each containing a cluster of elements belonging to a header
         (or not, if elements precede any headers). While it is flat, the level of each
         group is represented directly as @level - making induction easier over the groups. -->
    <xsw:sequence>
      <xsl:for-each-group select="*" group-starting-with="h1 | h2 | h3 | h4 | h5 | h6">
        <!-- Remember . is current-group()[1], so $leader is the header (when found) -->
        <xsl:variable name="leader"
          select="self::h1 | self::h2 | self::h3 | self::h4 | self::h5 | self::h6"/>
        <!-- group/@level is X for hX, 0 if there is no leader.  -->
        <xsw:group level="{($leader/replace(local-name(),'\D',''),0)[1]}">
          <xsl:apply-templates select="current-group()"/>
        </xsw:group>
      </xsl:for-each-group>
    </xsw:sequence>
  </xsl:template>
  
  <xsl:template match="xsw:sequence">
    <!-- $zero is the zero-level group, when present.
         (Some sequences begin with elements before a header; these are grouped in xsw:group[@level=0]-->
    <xsl:variable name="zero" select="xsw:group[@level = 0]"/>
    <!-- We want the content of zero but w/o a section wrapper, so we exclude it from section assembly.
         Of course this is a no-op if empty($zero). -->
    <xsl:apply-templates select="$zero"/>
    <!-- We want section wrappers for anything remaining (again a no-op when there is nothing). -->
    <xsl:call-template name="section-assembly">
      <xsl:with-param name="subsequence" select="xsw:group except $zero"/>
    </xsl:call-template>
  </xsl:template>
  
  <!-- Groups can be unwrapped since the induced section structure takes care of everything. -->
  <xsl:template match="xsw:group">
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template name="section-assembly">
    <!-- $who is a run of one or more contiguous siblings.
         When $who is empty we fall through so recursion is safe as long as $who is reduced. -->
    <!-- When we enter we operate on all xsw:group child elements of the context except level 0.
         All children should be xsw:group as provided in the previous step. --> 
    <xsl:param name="subsequence" required="yes" as="element(xsw:group)*"/>
    <xsl:param name="this-level"       select="1"     as="xs:integer"/>
    
    <xsl:for-each-group select="$subsequence" group-starting-with="xsw:group[@level &lt;= $this-level]">
      <!-- Within $subsequence the elements are (sub)sequenced again at boundaries wherever @level <= $level.
        However, only subgroups where @level=$level are 'here' (at this level). Subgroups may appear at
        deeper levels also, resulting in empty($me). This occurs if group levels are skipped in the inputs -
        the first group in the subsequence doesn't have the appropriate level.
           (e.g. an h4 appears without an h3 giving us a group[@level=4] 'inside' a group[@level=2].) -->
      <xsl:variable name="me" select="current-group()[@level = $this-level]"/>
      
      <!-- When empty($me), we want no section here, because we have no ("correct") header.
           But the group is passed in again at the next deeper level, so subgrouping occurs.
           Note that this template call is a no-op when $me exists (i.e. we have a group with a header). -->
      <xsl:call-template name="section-assembly">
        <xsl:with-param name="subsequence" select="current-group()[empty($me)]"/>
        <xsl:with-param name="this-level" select="$this-level + 1"/>
      </xsl:call-template>
      
      <!-- When $me exists, the group is on the right level. -->
      <xsl:if test="exists($me)">
        <section>
          <!-- Now we emit the group (to be unwrapped by a receiving template). -->
          <xsl:apply-templates select="$me"/>
          <!-- Next we subgroup the remaining groups within the section created for $me.
               We do this by assembling again, at $level+1, any remaining elements in the (sub)sequence. -->
          <!-- Note the new $who is empty when $me is the only group left. -->
          <xsl:call-template name="section-assembly">
            <xsl:with-param name="subsequence" select="current-group() except $me"/><!-- Everything still in doc order. -->
            <xsl:with-param name="this-level"  select="$this-level + 1"/>
          </xsl:call-template>
        </section>
      </xsl:if>
    </xsl:for-each-group>
  </xsl:template>
  
  <!-- NB not being used at present... -->
  <!-- Function returns true if a sequence of headers has "regular order"
       defined as "never skip a level going down" -->
  <xsl:function name="xsw:has-regular-order" as="xs:boolean">
    <xsl:param name="header-sequence" as="element()*"/>
    <!-- header numbering sequence ignores elements whose names don't contain arabic numerals e.g. 'p' or 'li'
         only the likes of 'h1' or 'h5' are kept (1,5).-->
    <xsl:variable name="header-num-sequence"
      select="$header-sequence / (local-name(.) => replace('\D','') => xs:integer() )"/>
    <xsl:variable name="okay-sequence" as="xs:boolean*">
      <xsl:for-each select="$header-num-sequence">
        <xsl:variable name="p" select="position()"/>
        <xsl:variable name="predecessor-num" select="if ($p = 1) then 0 else $header-num-sequence[position() eq ($p - 1)]"/>
        <xsl:sequence select=". le ($predecessor-num + 1)"/>
      </xsl:for-each>
    </xsl:variable>
    <!--<xsl:message>
      <xsl:value-of select="$okay-sequence" separator=", "/>
    </xsl:message>-->
    <xsl:sequence select="every $okay in $okay-sequence satisfies $okay"/>
  </xsl:function>
  
</xsl:stylesheet>