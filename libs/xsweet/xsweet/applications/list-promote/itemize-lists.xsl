<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsw="http://coko.foundation/xsweet"
  xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="#all">

  <xsl:output method="xml" indent="no" omit-xml-declaration="yes"/>

<!-- Redundant match silences noisy XSLT engines -->
  <xsl:template match="node() | @* | html:html"  xmlns:html="http://www.w3.org/1999/xhtml">
    <xsl:copy copy-namespaces="no">
      <xsl:apply-templates select="node() | @*"/>
    </xsl:copy>
  </xsl:template>

  <!-- Groups can be unwrapped since the induced list structure takes care of everything. -->
  <xsl:template match="xsw:list">
    <ul>
      <xsl:for-each-group select="*" group-starting-with="p">
        <li>
          <xsl:apply-templates select="current-group()"/>
        </li>
      </xsl:for-each-group>
    </ul>
  </xsl:template>

</xsl:stylesheet>
