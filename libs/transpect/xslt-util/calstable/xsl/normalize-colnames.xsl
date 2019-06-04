<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:calstable="http://docs.oasis-open.org/ns/oasis-exchange/table" 
  version="2.0"
  exclude-result-prefixes="xs calstable">

  <xsl:import href="normalize.xsl"/>

  <xsl:template match="node() | @*" mode="calstable:colnames calstable:colnames-entry">
    <xsl:copy copy-namespaces="no">
      <xsl:apply-templates select="@*, node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*:colspec/@colname" mode="calstable:colnames">
    <xsl:param name="calstable:colprefix" as="xs:string" select="'c'" tunnel="yes"/>
    <xsl:attribute name="{name()}" select="string-join(($calstable:colprefix, ../@colnum), '')"/>
  </xsl:template>
  
  <xsl:key name="by-colname" match="*:colspec" use="@colname"/>
  
  <xsl:template match="@*:namest | @*:nameend | *:entry/@*:colname" mode="calstable:colnames">
    <xsl:variable name="colspec" as="element(*)?" 
      select="key('by-colname', .)[ancestor::*:tgroup[1] is current()/ancestor::*:tgroup[1]]"/>
    <xsl:variable name="new-val" as="attribute(*)">
      <xsl:choose>
        <xsl:when test="exists($colspec/@colname)">
          <xsl:apply-templates select="$colspec/@colname" mode="#current"/>    
        </xsl:when>
        <xsl:when test="exists($colspec)">
          <xsl:attribute name="error" select="'colname missing'"/>    
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="error" select="'colspec missing'"/>    
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:attribute name="{local-name(($new-val[name() = 'error'], .)[1])}" select="string($new-val)"/>
  </xsl:template>
  
  <xsl:template match="@calstable:id | @calstable:colspan | *:entry[@calstable:rid] 
                       | *:entry[@linkend[starts-with(., 'calstable_')]]
                       | *:entry/@xml:id[starts-with(., 'calstable_')]" mode="calstable:colnames"/>
  
  <xsl:template match="@calstable:morerows" mode="calstable:colnames">
    <xsl:attribute name="{local-name()}" select="string(.)"/>
  </xsl:template>
  
  <xsl:function name="calstable:normalize-colnames" as="element(*)">
    <!-- expects a tgroup element -->
    <xsl:param name="tgroup" as="element(*)"/>
    <xsl:param name="prefix" as="xs:string"/>
    <xsl:variable name="prelim" as="document-node(element(*))">
      <xsl:document>
        <xsl:apply-templates select="$tgroup" mode="calstable:colnames-entry"/>
      </xsl:document>  
    </xsl:variable>
    <xsl:apply-templates select="$prelim" mode="calstable:colnames">
      <xsl:with-param name="calstable:colprefix" select="$prefix" tunnel="yes"/>
    </xsl:apply-templates>
  </xsl:function>
  
  <xsl:template match="*[*:row]" mode="calstable:colnames-entry">
    <xsl:sequence select="calstable:normalize(.)"/>
  </xsl:template>
  
  <xsl:template match="*:tgroup" mode="calstable:colnames-entry">
    <xsl:copy copy-namespaces="no">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="*:colspec[last()]" mode="calstable:colspec"/>
      <xsl:apply-templates select="node()[empty(following-sibling::*:colspec)] except *:colspec" mode="#current"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>