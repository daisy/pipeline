<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:css="http://www.w3.org/1996/css"
  xmlns:dbk="http://docbook.org/ns/docbook"
  xmlns:saxon="http://saxon.sf.net/"
  xmlns:tr="http://www.transpect.io"
  xmlns:calstable="http://docs.oasis-open.org/ns/oasis-exchange/table"
  version="2.0">
  
  <!-- Documentation: in normalize.xsl -->
  <xsl:import href="normalize-colnames.xsl"/>
  
  <xsl:param name="colname-prefix" select="'col'" as="xs:string"/>

  <xsl:output indent="yes"/>

  <xsl:template match="node() | @*" mode="#default check-normalized normalize-colnames">
    <xsl:copy copy-namespaces="no">
      <xsl:apply-templates select="@*, node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[*:row]" mode="check-normalized">
    <xsl:sequence select="calstable:check-normalized(
                            calstable:normalize(.), 
                            'no'
                          )"/>
  </xsl:template>

  <xsl:template match="*:tgroup" mode="normalize-colnames" >
    <xsl:sequence select="calstable:normalize-colnames(., $colname-prefix)"/>
  </xsl:template>

  <xsl:template match="*:tgroup" mode="#default" >
<!--    <xsl:sequence select="calstable:normalize(.)"/>-->
    <xsl:sequence select="calstable:check-normalized(calstable:normalize(.),'no')"/>
  </xsl:template>

  

</xsl:stylesheet>