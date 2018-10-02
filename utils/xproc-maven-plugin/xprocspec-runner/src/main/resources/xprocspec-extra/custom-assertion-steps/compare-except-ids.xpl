<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:x="http://www.daisy.org/ns/xprocspec"
                version="1.0"
                type="x:compare-except-ids"
                name="main">
  
  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>Compare two documents with f:deep-equal, but ignore the exact values of "id" and "xml:id"
    attributes. White space is normalized too.</p>
  </p:documentation>
  
  <p:input port="context" primary="false"/>
  <p:input port="expect" primary="false"/>
  <p:input port="parameters" kind="parameter" primary="true"/>
  <p:output port="result" primary="true"/>
  
  <p:declare-step type="pxi:normalize-ids">
    <p:input port="source"/>
    <p:output port="result"/>
    <p:xslt>
      <p:input port="stylesheet">
        <p:inline>
          <xsl:stylesheet version="2.0">
            <xsl:variable name="ids" as="xs:string*" select="//(@id|@xml:id)/string()"/>
            <xsl:template match="@*|node()">
              <xsl:copy>
                <xsl:apply-templates select="@*|node()"/>
              </xsl:copy>
            </xsl:template>
            <xsl:template match="@id|@xml:id">
              <xsl:attribute name="{name(.)}" select="concat('id',index-of($ids,string(.))[1])"/>
            </xsl:template>
          </xsl:stylesheet>
        </p:inline>
      </p:input>
      <p:input port="parameters">
        <p:empty/>
      </p:input>
    </p:xslt>
    <p:delete match="/*/@xml:space"/>
    <p:string-replace match="text()" replace="normalize-space(.)"/>
  </p:declare-step>
  
  <pxi:normalize-ids name="normalize-context">
    <p:input port="source">
      <p:pipe step="main" port="context"/>
    </p:input>
  </pxi:normalize-ids>
  
  <pxi:normalize-ids name="normalize-expect">
    <p:input port="source">
      <p:pipe step="main" port="expect"/>
    </p:input>
  </pxi:normalize-ids>
  
  <p:compare name="compare" fail-if-not-equal="false">
    <p:input port="source">
      <p:pipe step="normalize-context" port="result"/>
    </p:input>
    <p:input port="alternate">
      <p:pipe step="normalize-expect" port="result"/>
    </p:input>
  </p:compare>
  
  <p:rename match="/*" new-name="x:test-result">
    <p:input port="source">
      <p:pipe step="compare" port="result"/>
    </p:input>
  </p:rename>
  
  <p:add-attribute match="/*" attribute-name="result">
    <p:with-option name="attribute-value" select="if (string(/*)='true') then 'passed' else 'failed'">
      <p:pipe step="compare" port="result"/>
    </p:with-option>
  </p:add-attribute>
  
  <p:delete match="/*/node()" name="result"/>
  
  <p:choose>
    <p:when test="/*/@result='passed'">
      <p:identity/>
    </p:when>
    <p:otherwise>
      <p:wrap-sequence wrapper="x:expected" name="expected">
        <p:input port="source">
          <p:pipe step="main" port="expect"/>
          <!-- <p:pipe step="normalize-expect" port="result"/> -->
        </p:input>
      </p:wrap-sequence>
      <p:wrap-sequence wrapper="x:was" name="was">
        <p:input port="source">
          <p:pipe step="main" port="context"/>
          <!-- <p:pipe step="normalize-context" port="result"/> -->
        </p:input>
      </p:wrap-sequence>
      <p:insert match="/*" position="last-child">
        <p:input port="source">
          <p:pipe step="result" port="result"/>
        </p:input>
        <p:input port="insertion">
          <p:pipe step="expected" port="result"/>
          <p:pipe step="was" port="result"/>
        </p:input>
      </p:insert>
      <p:add-attribute match="/*/*" attribute-name="xml:space" attribute-value="preserve"/>
    </p:otherwise>
  </p:choose>
  
</p:declare-step>
