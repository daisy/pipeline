<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsw="http://coko.foundation/xsweet"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="#all">
  
  <!-- XSweet: Library for HTML Tweak XSLTs (must be available at runtime) -->
  <!-- Note: this XSLT isn't run on its own: it is, however, included as a module. HTML Tweak depends on its being available. -->
  
<!-- See html-tweak-demo.xsl for an example of how to use this stylesheet fragment. -->
  
  <xsl:template match="node() | @*">
    <xsl:copy>
      <xsl:apply-templates select="node() | @*"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="makeTweak">
    <!-- for class, a string with , delimiters; for style, a string with ; delimiters -->
    <xsl:param name="addClass"               select="()" as="xs:string?"/>
    <xsl:param name="removeClass"            select="()" as="xs:string?"/>
    <xsl:param name="addStylePropertyValues" select="()" as="xs:string?"/>
    <xsl:param name="removeStyleProperties"  select="()" as="xs:string?"/>
    
    <xsl:variable name="alreadyTweaked">
      <xsl:next-match/>
    </xsl:variable>
    <xsl:for-each select="$alreadyTweaked/*"><!-- change context to results of applying templates
      which we expect to be a single (html) element -->
      <xsl:copy>
        <xsl:copy-of select="@* except (@class | @style)"/>
        <xsl:call-template name="tweakStyle">
          <xsl:with-param name="removeProperties"  select="tokenize($removeStyleProperties, '\s*;\s*')"/>
          <xsl:with-param name="addPropertyValues" select="tokenize($addStylePropertyValues,'\s*;\s*')"/>
        </xsl:call-template>
        <xsl:call-template name="tweakClass">
          <xsl:with-param name="remove" select="tokenize($removeClass,'\s*,\s*')"/>
          <xsl:with-param name="add"    select="tokenize($addClass,   '\s*,\s*')"/>
        </xsl:call-template>
        <xsl:copy-of select="child::node()"/>
      </xsl:copy>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="tweakClass">
    <xsl:param name="remove" select="()" as="xs:string*"/>
    <xsl:param name="add"    select="()" as="xs:string*"/>
    <xsl:if test="exists( ($add, xsw:classes(.)[not(.= $remove)] (: either $add or classes other than $remove :)))">
      <xsl:attribute name="class" select="xsw:classes(.)[not(.=$remove)], $add" separator="&#32;"/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="tweakStyle">
    <!-- $removeProperties are expected as 'font-size', 'text-indent'  -->
    <!-- $addPropertyValues are expected as 'font-size: 12pt', 'text-indent: 36pt' -->
    <xsl:param name="removeProperties"     select="()" as="xs:string*"/>
    <xsl:param name="addPropertyValues"    select="()" as="xs:string*"/>
    <xsl:variable name="oldPropertyValues" select="xsw:style-propertyValues(.)"/>
    <xsl:variable name="newPropertyValues"
      select="$oldPropertyValues[not(replace(.,':.*$','') = $removeProperties)],
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
  
  <xsl:function name="xsw:classes" as="xs:string*">
    <xsl:param name="e" as="element()"/>
    <xsl:sequence select="tokenize($e/@class/normalize-space(.),'\s')"/>
  </xsl:function>
  
  <xsl:function name="xsw:style-properties" as="xs:string*">
    <!-- Returns 'font-family','font-size','color','text-indent' whatever
         properties are defined on @style -->
    <xsl:param name="e" as="element()"/>
    <xsl:sequence select="for $propVal in xsw:style-propertyValues($e)
      return substring-before($propVal,':')"/>
  </xsl:function>
  
  <xsl:function name="xsw:style-propertyValues" as="xs:string*">
    <!-- Returns 'font-family: Helvetica','font-size: 10pt' whatever
         properties are defined on @style -->
    <xsl:param name="e" as="element()"/>
    <xsl:sequence select="tokenize($e/@style/normalize-space(.),'\s*;\s*')"/>
  </xsl:function>
  
  <!--<xsl:function name="xsw:hasClass" as="xs:boolean">
    <xsl:param name="e" as="element()"/>
    <xsl:param name="c" as="xs:string"/>
    <xsl:sequence select="xsw:classes($e) = $c"/>
  </xsl:function>-->
  
  <xsl:key name="elements-by-class"         match="*[matches(@class,'\S')]" use="xsw:classes(.)"/>
  
  <xsl:key name="elements-by-property"      match="*[matches(@style,'\S')]" use="xsw:style-properties(.)"/>
  
  <xsl:key name="elements-by-propertyValue" match="*[matches(@style,'\S')]" use="xsw:style-propertyValues(.)"/>
  
</xsl:stylesheet>