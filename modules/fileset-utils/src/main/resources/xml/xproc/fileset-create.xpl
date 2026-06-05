<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step  xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                 xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                 xmlns:d="http://www.daisy.org/ns/pipeline/data"
                 type="px:fileset-create"
                 exclude-inline-prefixes="px">

  <p:output port="result"/>
  <p:option name="base" required="false"/>
  
  <p:identity>
    <p:input port="source">
      <p:inline exclude-inline-prefixes="px"><d:fileset/></p:inline>
    </p:input>
  </p:identity>
  <p:choose>
    <p:when test="p:value-available('base')">
      <p:xslt>
        <p:with-param name="base" select="$base"/>
        <p:input port="stylesheet">
          <p:inline>
            <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pf="http://www.daisy.org/ns/pipeline/functions" version="2.0" exclude-result-prefixes="#all">
              <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl"/>
              <xsl:param name="base" required="yes"/>
              <xsl:template match="/*">
                <xsl:copy>
                  <xsl:attribute name="xml:base" select="pf:normalize-uri($base)"/>
                </xsl:copy>
              </xsl:template>
            </xsl:stylesheet>
          </p:inline>
        </p:input>
      </p:xslt>
    </p:when>
    <p:otherwise>
      <p:identity/>
    </p:otherwise>
  </p:choose>
  
</p:declare-step>
