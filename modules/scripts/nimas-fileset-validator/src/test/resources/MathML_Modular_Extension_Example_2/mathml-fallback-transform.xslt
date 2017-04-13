<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:m="http://www.w3.org/1998/Math/MathML" xmlns="http://www.daisy.org/z3986/2005/dtbook/" >

    <!-- MathML-in-DAISY fallback transform 2007-09-28
    	 created by gh, LLC and ViewPlus Technologies for DAISY Consortium  -->


  <xsl:output method="xml" indent="no" encoding="UTF-8" doctype-public="-//NISO//DTD dtbook 2005-3//EN" doctype-system="http://www.daisy.org/z3986/2005/dtbook-2005-3.dtd"/>

  <xsl:template match="*">
    <!-- Create a new element instead of copying source.  Prevents the processor from copying unnecessary namespace nodes from old to new. -->
    <xsl:element name="{name()}" namespace="{namespace-uri()}" >
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates />
    </xsl:element>
  </xsl:template>
	
  <xsl:template match="processing-instruction() | comment()">
    <xsl:copy-of select="."/>
  </xsl:template>
	
  <xsl:template match="m:math">
    <xsl:variable name="img_id" select="concat('img_',generate-id())"/>
    <xsl:variable name="group_id" select="concat('imggroup_',generate-id())"/>
    <!-- Create a new element instead of copying source.  Prevents the processor from copying unnecessary namespace nodes from old to new. -->
    <xsl:element name="imggroup">
      <xsl:attribute name="id"><xsl:value-of select=" $group_id "/></xsl:attribute>
      <xsl:attribute name="smilref"><xsl:value-of xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/" select="@dtbook:smilref"/></xsl:attribute>
      <!-- Create a new element instead of copying source.  Prevents the processor from copying unnecessary namespace nodes from old to new. -->
      <xsl:element name="img">
        <xsl:attribute name="id"><xsl:value-of select=" $img_id "/></xsl:attribute>
        <xsl:attribute name="src"><xsl:value-of select="@altimg"/></xsl:attribute>
        <xsl:attribute name="alt"><xsl:value-of select="@alttext"/></xsl:attribute>
      </xsl:element>
      <!-- Create a new element instead of copying source.  Prevents the processor from copying unnecessary namespace nodes from old to new. -->
      <xsl:element name="prodnote">
        <xsl:attribute name="render">required</xsl:attribute>
        <xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
        <xsl:attribute name="imgref"><xsl:value-of select=" $img_id "/></xsl:attribute>
        <xsl:attribute name="smilref"><xsl:value-of xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/" select="@dtbook:smilref"/></xsl:attribute>
        <xsl:value-of select="@alttext"/>
      </xsl:element>
    </xsl:element>    
  </xsl:template>
	
</xsl:stylesheet>
