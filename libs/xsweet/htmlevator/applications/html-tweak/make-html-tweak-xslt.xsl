<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsw="http://coko.foundation/xsweet"
   exclude-result-prefixes="#all">

  <xsl:output indent="yes"/>


  <!-- XSweet: Dynamic XSLT production from configuration XML  -->
  <!-- Input: An HTML Tweak configuration file such as `html-tweak-map.xml` -->
  <!-- Output: An XSLT to be applied to HTML Typescript (to achieve the HTML Tweak) -->
  <!-- Dependency: the XSLTs produced by this also include file `html-tweak-lib.xsl` so that must be available. -->
  

<!-- Stylesheet transforms a rough transformation spec for HTML
    (affecting @style and @class only) into an XSLT executable.
  
  So you can write simple maps for your HTML and have the XSLT written for you behind the scenes.



  -->


  <!--
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
  -->
  <xsl:namespace-alias stylesheet-prefix="xsw" result-prefix="xsl"/>

  <xsl:param name="debug-mode" as="xs:string">silent</xsl:param>

  <xsl:template match="/*">

    <!--       xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
      xmlns:xs="http://www.w3.org/2001/XMLSchema"
      xmlns:xsw="http://coko.foundation/xsweet"
      xmlns="http://www.w3.org/1999/xhtml"
      
    -->
    <xsw:stylesheet version="2.0" exclude-result-prefixes="#all">


      <!--<xsw:template match="node() | @*">
        <xsw:copy>
          <xsw:apply-templates select="node() | @*"/>
        </xsw:copy>
      </xsw:template>

      <xsl:apply-templates select="div[@class='grouped']/div[@class='hX']/*" mode="xslt-produce"/>
      
      <xsl:if test="not($debug-mode='silent')">
      <xsw:variable name="in">
        <xsl:copy-of select="div"/>
      </xsw:variable>
      </xsl:if>
      -->

      <xsl:apply-templates  mode="xslt-produce"/>

      <xsw:include href="{resolve-uri('html-tweak-lib.xsl',document-uri(document('')) )}"/>

    </xsw:stylesheet>
  </xsl:template>


  <!-- Template writes XSLT templates  -->

  <!--
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
  -->

  <xsl:template match="where" mode="xslt-produce">
    <xsw:template priority="{count(.|following-sibling::where)}">
      <xsl:attribute name="match">
        <xsl:apply-templates mode="keyPattern" select="match/*[1]"/>
        <xsl:for-each select="match/*[1]/following-sibling::*">
          <xsl:text>[. intersect </xsl:text>
          <xsl:apply-templates mode="keyPattern" select="."/>
          <xsl:text>]</xsl:text>
        </xsl:for-each>
      </xsl:attribute>
      <!--match="key('elements-by-propertyValue','font-size: 18pt')
    [. intersect key('elements-by-class',        'Freeform')]"      >-->

      <xsw:call-template name="makeTweak">
        <!--<xsw:with-param name="removeStyleProperties"  >font-size</xsw:with-param>
      <xsw:with-param name="addStylePropertyValues" >color: red</xsw:with-param>
      <xsw:with-param name="removeClass"            >FreeForm</xsw:with-param>
      <xsw:with-param name="addClass"               >FreeFormNew</xsw:with-param>-->
        <xsl:apply-templates select="(add | remove)/(class | style)" mode="tweakParams"/>
      </xsw:call-template>
    </xsw:template>
  </xsl:template>

  <xsl:template match="match/style[matches(.,':')]" priority="2" mode="keyPattern">
    
    <xsl:text>key('elements-by-propertyValue','</xsl:text>
    <xsl:apply-templates/>
    <xsl:text>')</xsl:text>
  </xsl:template>
  
  <xsl:template match="match/style" priority="1" mode="keyPattern">
    <xsl:text>key('elements-by-property','</xsl:text>
    <xsl:apply-templates/>
    <xsl:text>')</xsl:text>
  </xsl:template>
  
  <xsl:template match="match/class" mode="keyPattern">
    <xsl:text>key('elements-by-class','</xsl:text>
    <xsl:apply-templates/>
    <xsl:text>')</xsl:text>
  </xsl:template>
  
  <xsl:template match="remove/class" mode="tweakParams">
    <xsw:with-param name="removeClass">
      <xsl:apply-templates/>
    </xsw:with-param>
  </xsl:template>
  
  <xsl:template match="add/class" mode="tweakParams">
    <xsw:with-param name="addClass">
      <xsl:apply-templates/>
    </xsw:with-param>
  </xsl:template>

  <xsl:template match="remove/style" mode="tweakParams">
    <xsw:with-param name="removeStyleProperties">
      <xsl:apply-templates/>
    </xsw:with-param>
  </xsl:template>

  <xsl:template match="add/style" mode="tweakParams">
    <xsw:with-param name="addStylePropertyValues">
      <xsl:apply-templates/>
    </xsw:with-param>
  </xsl:template>

</xsl:stylesheet>
