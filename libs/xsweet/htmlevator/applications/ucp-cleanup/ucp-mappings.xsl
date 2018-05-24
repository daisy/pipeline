<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsw="http://coko.foundation/xsweet"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="#all">
  
  <xsl:output method="xml" indent="no" omit-xml-declaration="yes"/>
  
<!-- XSweet: Some small adjustments to tagging as required by a local process. -->
<!-- Input: HTML Typescript -->
<!-- Output: A copy, with modifications. -->
  
  <xsl:mode on-no-match="shallow-copy"/>
  
  <xsl:template match="b | u">
    <i>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </i>
  </xsl:template>
  
  <xsl:template match="key('elements-by-propertyValue','font-weight: bold')[self::p] |
                       key('elements-by-propertyValue','text-decoration: underline')[self::p]">
    <xsl:copy>
      <xsl:copy-of select="@* except @style"/>
      <xsl:call-template name="tweakStyle">
        <xsl:with-param name="removePropertyValues" select="'font-weight: bold','text-decoration: underline'"/>
        <xsl:with-param name="addPropertyValues" select="'font-style: italic'"/>
      </xsl:call-template>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:key name="elements-by-propertyValue" match="*[matches(@style,'\S')]" use="xsw:style-propertyValues(.)"/>
  
  <!-- template copied from ../html-tweak/html-tweak-lib.xsl -->
  
  <xsl:function name="xsw:style-propertyValues" as="xs:string*">
    <!-- Returns 'font-family: Helvetica','font-size: 10pt' whatever
         properties are defined on @style -->
    <xsl:param name="e" as="element()"/>
    <xsl:sequence select="tokenize($e/@style/normalize-space(.),'\s*;\s*')"/>
  </xsl:function>
  
  <xsl:template name="tweakStyle">
    <!-- $removeProperties are expected as 'font-size', 'text-indent'  -->
    <!-- $addPropertyValues are expected as 'font-size: 12pt', 'text-indent: 36pt' -->
    <xsl:param name="removePropertyValues" select="()" as="xs:string*"/>
    <xsl:param name="addPropertyValues"    select="()" as="xs:string*"/>
    <xsl:variable name="oldPropertyValues" select="xsw:style-propertyValues(.)"/>
    <xsl:variable name="newPropertyValues"
      select="$oldPropertyValues[not(. = $removePropertyValues)],
      $addPropertyValues"/>
    <xsl:if test="exists($newPropertyValues)">
      <xsl:attribute name="style">
        <xsl:for-each select="$newPropertyValues">
          <xsl:sort data-type="text"/>
          <xsl:if test="position() gt 1">; </xsl:if>
          <xsl:value-of select="."/>
        </xsl:for-each>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>
  
</xsl:stylesheet>