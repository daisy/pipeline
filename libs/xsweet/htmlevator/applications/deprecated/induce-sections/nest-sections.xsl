<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsw="http://coko.foundation/xsweet"
  xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  
  exclude-result-prefixes="#all">

  <!-- XSweet: the second half of the section induction two-step. [3] -->
  <xsl:output method="xml" indent="no" omit-xml-declaration="yes"/>
  
  <xsl:template match="node() | @*">
    <xsl:copy copy-namespaces="no">
      <xsl:apply-templates select="node() | @*"/>
    </xsl:copy>
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
    <xsl:param name="level"       select="1"     as="xs:integer"/>
    
    <xsl:for-each-group select="$subsequence" group-starting-with="xsw:group[@level &lt;= $level]">
      <!-- Within $subsequence the elements are (sub)sequenced again at boundaries wherever @level <= $level.
        However, only subgroups where @level=$level are 'here' (at this level). Subgroups may appear at
        deeper levels also, resulting in empty($me). This occurs if group levels are skipped in the inputs -
        the first group in the subsequence doesn't have the appropriate level.
           (e.g. an h4 appears without an h3 giving us a group[@level=4] 'inside' a group[@level=2].) -->
      <xsl:variable name="me" select="current-group()[@level = $level]"/>
      
      <!-- When empty($me), we want no section here, because we have no ("correct") header.
           But the group is passed in again at the next deeper level, so subgrouping occurs.
           Note that this template call is a no-op when $me exists (i.e. we have a group with a header). -->
      <xsl:call-template name="section-assembly">
        <xsl:with-param name="subsequence" select="current-group()[empty($me)]"/>
        <xsl:with-param name="level" select="$level + 1"/>
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
            <xsl:with-param name="level"        select="$level + 1"/>
          </xsl:call-template>
        </section>
      </xsl:if>
    </xsl:for-each-group>
  </xsl:template>
  
</xsl:stylesheet>