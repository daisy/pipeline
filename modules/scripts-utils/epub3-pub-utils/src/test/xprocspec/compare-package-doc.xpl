<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:x="http://www.daisy.org/ns/xprocspec"
                xmlns:opf="http://www.idpf.org/2007/opf"
                type="x:compare-package-doc" name="main">
  
  <p:input port="context" primary="false"/>
  <p:input port="expect" primary="false"/>
  <p:input port="parameters" kind="parameter" primary="true"/>
  <p:output port="result" primary="true"/>
  
  <p:string-replace match="/opf:package/opf:metadata/opf:meta[@property='dcterms:modified']/text()"
                    replace="'3000-01-01T00:00:00Z'">
    <p:input port="source">
      <p:pipe step="main" port="context"/>
    </p:input>
  </p:string-replace>
  <p:string-replace name="normalize-context" match="text()" replace="normalize-space(.)"/>
  
  <p:string-replace match="/opf:package/opf:metadata/opf:meta[@property='dcterms:modified']/text()"
                    replace="'3000-01-01T00:00:00Z'">
    <p:input port="source">
      <p:pipe step="main" port="expect"/>
    </p:input>
  </p:string-replace>
  <p:string-replace name="normalize-expect" match="text()" replace="normalize-space(.)"/>
  
  <!--
      FIXME: everything below is the same for most custom compare steps, so move it to a shared step?
  -->
  
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
