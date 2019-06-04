<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tr="http://transpect.io"
  xmlns:functx="http://www.functx.com"
  exclude-result-prefixes="xs"  
  version="2.0">
  
  <!--  *
        * functions to analyze file paths.
        * -->
  
  <xsl:function name="tr:notdir" as="xs:string+">
    <xsl:param name="paths" as="xs:string+"/>
    <xsl:for-each select="$paths">
      <xsl:sequence select="tokenize(., '/')[normalize-space(.)][last()]"/>
    </xsl:for-each>
  </xsl:function>
  
  <xsl:function name="tr:basename" as="xs:string+">
    <xsl:param name="paths" as="xs:string+"/>
    <xsl:for-each select="$paths">
      <xsl:sequence select="tokenize(tr:notdir(.), '\.')[1]"/>
    </xsl:for-each>
  </xsl:function>
  
  <xsl:function name="tr:ext" as="xs:string+">
    <xsl:param name="paths" as="xs:string+"/>
    <xsl:for-each select="$paths">
      <xsl:variable name="temp" as="xs:string?" 
        select="tokenize(replace(., '[?#].*$', ''), '\.')[normalize-space(.)][last()]"/>
      <xsl:if test="empty($temp)">
        <xsl:message select="'xslt-util/paths/xsl/paths.xsl, tr:ext(): Empty extension for ', ."/>
      </xsl:if>
      <xsl:sequence select="$temp"/>
    </xsl:for-each>
  </xsl:function>
  
  <xsl:function name="tr:path" as="xs:string+">
    <xsl:param name="paths" as="xs:string+"/>
    <xsl:for-each select="$paths">
      <xsl:sequence select="string-join(tokenize(., '/')[normalize-space(.)][position() ne last()], '/')"/>  
    </xsl:for-each>
  </xsl:function>
  
</xsl:stylesheet>