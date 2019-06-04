<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"  
  xmlns:cx="http://xmlcalabash.com/ns/extensions" 
  xmlns:xslo="xslotto"
  exclude-result-prefixes="xs"
  version="2.0">
  
  <xsl:namespace-alias stylesheet-prefix="xslo" result-prefix="xsl"/>
  
  <xsl:template match="@* | *">
    <xsl:copy>
      <xsl:apply-templates select="@*, node()"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="/p:declare-step/p:output[@port = 'result']">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:attribute name="sequence" select="'true'"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/p:declare-step/p:output[@port = 'report']"/>

  <xsl:template match="/p:declare-step[p:output[@port = 'result']]">
    <xsl:copy>
      <xsl:apply-templates select="@*, node()"/>
      <xsl:variable name="report-connection" as="element(*)*" 
        select="p:output[@port = 'report']/(p:pipe | p:inline)"/>
      <xsl:variable name="result-connection" as="element(*)*" 
        select="p:output[@port = 'result']/(p:pipe | p:inline)"/>
      <xsl:if test="not($result-connection)"><!-- last step’s output was primary output -->
        <p:identity name="__I_D_E_N_T_I_T_Y__"/>
        <p:sink/>
      </xsl:if>
      <p:identity>
        <p:input port="source">
          <xsl:if test="not($result-connection)">
            <p:pipe port="result" step="__I_D_E_N_T_I_T_Y__"/>
          </xsl:if>
          <xsl:sequence select="$result-connection"/>
          <xsl:comment>report connections go here:</xsl:comment>
          <xsl:sequence select="$report-connection"/>
        </p:input>
      </p:identity>
      <p:wrap-sequence wrapper="c:wrapper"/>
      <!-- use p:xslt to add the xml:base attribute, since p:add-attribute can reorder existing attributes -->
      <p:xslt name="add_xml-base_attribute">
        <p:input port="parameters"><p:empty/></p:input>
        <p:with-param name="base-uri" select="base-uri()">
          <xsl:choose>
            <xsl:when test="$result-connection">
              <xsl:sequence select="$result-connection"/>
            </xsl:when>
            <xsl:otherwise>
              <p:pipe port="result" step="__I_D_E_N_T_I_T_Y__"/>
              <!--
              <xsl:variable name="primary-input" select="(p:input[@primary = 'true'], p:input[1])[1]" as="element(p:input)"/>
              <p:pipe port="{$primary-input/@port}" step="{@name}"/>-->
            </xsl:otherwise>
          </xsl:choose>
        </p:with-param>
        <p:input port="stylesheet">
          <p:inline>
            <xslo:stylesheet version="2.0">
              <xslo:param name="base-uri"/>
              <xslo:template match="/* | /*/*[1]">
                <xslo:copy>
                  <xslo:attribute name="xml:base" select="$base-uri"/>
                  <xslo:apply-templates select="@*, node()" mode="#current"/>
                </xslo:copy>
              </xslo:template>
              <xslo:template match="* | node() | @* | processing-instruction() | comment()" priority="-1">
                <xslo:sequence select="."/>
              </xslo:template>
            </xslo:stylesheet>
          </p:inline>
        </p:input>
      </p:xslt>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>