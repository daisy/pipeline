<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsw="http://coko.foundation/xsweet"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="#all">
  
  <xsl:output method="xml" indent="no" omit-xml-declaration="yes"/>


  <!-- XSweet: An *example* of an XSLT produced for the generalized HTML Tweak operation. -->
  <!-- Input: (presumably) HTML Typescript -->
  <!-- Output: a copy, with (demo) tweaks -->
  <!-- Note: This XSLT was produced from (a version of) html-tweak-map.xml as an example of HTML Tweak logic.
    It is saved here as a demonstration. -->
  

<!-- Stylesheet to rewrite style and class on HTML
     via a template cascade - i.e., more than a single transformation
     can be performed over a single element.
    
Ultimately this could support a little language kinda like:

where { font-size: 18pt } .FreeForm
  remove { font-size } .FreeForm
  add    { color: red } .FreeFormNew

<where>
  <match>
    <style>font-size: 18pt</style>
    <class>FreeForm</class>
  </match>
  <remove>
    <style>font-size</style>
    <class>FreeForm</class>
  </remove>
  <add>
    <class>FreeFormNew</class>
    <style>color: red</style>
  </add>
</where>

providing mappings across @class (considered as NMTOKENS) and @style (considered as CSS property sets)

  -->

<!-- How to use (until we implement a next-layer-up):
       match using appropriate key for class (name), style (property or property-value)
       call execute-tweak with replacement values.
       
       Note it is possible to pass in multiple values for parameters using , and ; within style and class settings.
       
       i.e. 
            <with-param name="removeStyleProperties">font-size; font-weight</with-param>
       removes both the properties named.

       Note also that control of style values is by property for removal (i.e., remove any/all 'font-size' property),
       but by property-value pair for addition (i.e., add 'font-size: larger").

       Not that you should be adding @style!

  -->

  <!-- Implementation of rule given above. -->
  
  <xsl:template  priority="12" match="key('elements-by-propertyValue','font-size: 18pt')
                         [. intersect key('elements-by-class',        'Freeform')]"      >

    <xsl:call-template name="makeTweak">
      <xsl:with-param name="removeStyleProperties"  >font-size</xsl:with-param>
      <xsl:with-param name="addStylePropertyValues" >color: red</xsl:with-param>
      <xsl:with-param name="removeClass"            >FreeForm</xsl:with-param>
      <xsl:with-param name="addClass"               >FreeFormNew</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template priority="11" match="key('elements-by-propertyValue','font-family: Arial Unicode MS')">
    <xsl:call-template name="makeTweak">
      <xsl:with-param name="removeStyleProperties">font-family</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template priority="10" match="key('elements-by-propertyValue','text-indent: 36pt')">
    <xsl:call-template name="makeTweak">
      <xsl:with-param name="addClass" select="'indented'"/>
    </xsl:call-template>
  </xsl:template>
  
  <!-- Infrastructure should not require modification. -->

  <xsl:include href="html-tweak-lib.xsl"/>
  
</xsl:stylesheet>