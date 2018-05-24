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
    <xsl:copy copy-namespaces="no">
      <xsl:apply-templates select="node() | @*"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="div">
    <xsl:for-each-group select="*" group-adjacent="exists(self::xsw:list)">
      <xsl:choose>
        <xsl:when test="not(current-grouping-key())">
          <xsl:apply-templates select="current-group()"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="list-assembly">
            <xsl:with-param name="who" select="current-group()"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
      
    </xsl:for-each-group>
  </xsl:template>
  
  <!-- Groups can be unwrapped since the induced list structure takes care of everything. -->
  <xsl:template match="xsw:list">
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template name="list-assembly">
    <!-- first time in, by default, who consists of runs of xsw:list children of the calling div -->
    <xsl:param name="who" required="yes" as="element(xsw:list)*"/>
    <!-- $who is a run of one or more contiguous siblings.
         When $who is empty we fall through so recursion is safe as long as $who is reduced. -->
    <xsl:param name="nominal-level"       select="0"     as="xs:integer"/>
    
    <xsl:for-each-group select="$who" group-starting-with="xsw:list[@level &lt;= $nominal-level]">
      <!-- Within $subsequence the elements are (sub)sequenced again at boundaries wherever @level <= $level.
        However, only subgroups where @level=$level are 'here' (at this level). -->
      <xsl:variable name="me" select="current-group()[@level = $nominal-level]"/>
      
      <!-- When empty($me), we want no section here, because we have no ("correct") header.
           But the group is passed in again at the next deeper level, so subgrouping occurs.
           Note that this template call is a no-op when $me exists (i.e. we have a group with a header). -->
      <xsl:call-template name="list-assembly">
        <xsl:with-param name="who" select="current-group()[empty($me)]"/>
        <xsl:with-param name="nominal-level" select="$nominal-level + 1"/>
      </xsl:call-template>
      
      <!-- When $me exists, the group is on the right level. -->
      <xsl:if test="exists($me)">
        <ul>
          <!-- Now we emit the group (to be unwrapped by a receiving template). -->
          <xsl:apply-templates select="$me"/>
          <!-- Next we subgroup the remaining groups within the section created for $me.
               We do this by assembling again, at $level+1, any remaining elements in the (sub)sequence. -->
          <!-- Note the new $who is empty when $me is the only group left. -->
          <xsl:call-template name="list-assembly">
            <xsl:with-param name="who" select="current-group() except $me"/><!-- Everything still in doc order. -->
            <xsl:with-param name="nominal-level"  select="$nominal-level + 1"/>
          </xsl:call-template>
        </ul>
      </xsl:if>
    </xsl:for-each-group>
  </xsl:template>
  
</xsl:stylesheet>
