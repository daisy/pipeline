<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:x="http://www.daisy.org/ns/xprocspec"
                version="1.0"
                type="x:compare-exact"
                name="main">
  
  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>Compare two documents with f:deep-equal, but also taking into account differences in
    prefixes and namespace nodes.</p>
  </p:documentation>
  
  <p:input port="context" primary="false"/>
  <p:input port="expect" primary="false"/>
  <p:input port="parameters" kind="parameter" primary="true"/>
  <p:output port="result" primary="true"/>
  
  <p:declare-step type="pxi:make-prefixes-and-namespaces-explicit-and-normalize-space">
    <p:input port="source"/>
    <p:output port="result"/>
    <p:xslt>
      <p:input port="stylesheet">
        <p:document href="make-prefixes-and-namespaces-explicit.xsl"/>
      </p:input>
      <p:input port="parameters">
        <p:empty/>
      </p:input>
    </p:xslt>
    <p:delete match="/*/@xml:space"/>
    <p:string-replace match="text()" replace="normalize-space(.)"/>
  </p:declare-step>
  
  <pxi:make-prefixes-and-namespaces-explicit-and-normalize-space name="normalize-context">
    <p:input port="source">
      <p:pipe step="main" port="context"/>
    </p:input>
  </pxi:make-prefixes-and-namespaces-explicit-and-normalize-space>
  
  <pxi:make-prefixes-and-namespaces-explicit-and-normalize-space name="normalize-expect">
    <p:input port="source">
      <p:pipe step="main" port="expect"/>
    </p:input>
  </pxi:make-prefixes-and-namespaces-explicit-and-normalize-space>
  
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
