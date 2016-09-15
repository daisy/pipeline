<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="pxi:dtbook-compare" name="main" version="1.0"
    xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:x="http://www.daisy.org/ns/xprocspec"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal">

    <p:input port="context" primary="false"/>
    <p:input port="expect" primary="false"/>
    <p:input port="parameters" kind="parameter" primary="true"/>
    <p:output port="result" primary="true"/>

    <p:xslt name="normalized-context">
      <p:input port="source">
	<p:pipe port="context" step="main"/>
      </p:input>
      <p:input port="stylesheet">
	<p:document href="xslt/normalize-dtbook.xsl"/>
      </p:input>
      <p:input port="parameters">
	<p:empty/>
      </p:input>
    </p:xslt>

    <p:xslt name="normalized-expect">
      <p:input port="source">
	<p:pipe port="expect" step="main"/>
      </p:input>
      <p:input port="stylesheet">
	<p:document href="xslt/normalize-dtbook.xsl"/>
      </p:input>
      <p:input port="parameters">
	<p:empty/>
      </p:input>
    </p:xslt>

    <p:compare fail-if-not-equal="false" name="compare">
      <p:input port="source">
	<p:pipe port="result" step="normalized-context"/>
      </p:input>
      <p:input port="alternate">
	<p:pipe port="result" step="normalized-expect"/>
      </p:input>
    </p:compare>

    <p:choose>
      <p:xpath-context>
	<p:pipe port="result" step="compare"/>
      </p:xpath-context>
      <p:when test="string(/*)='true'">
	<p:identity>
	  <p:input port="source">
	    <p:inline>
	      <x:test-result result="passed"/>
	    </p:inline>
	  </p:input>
	</p:identity>
      </p:when>
      <p:otherwise>
	<p:wrap-sequence wrapper="x:expected" name="expected">
	  <p:input port="source">
	    <p:pipe step="normalized-expect" port="result"/>
	  </p:input>
	</p:wrap-sequence>
	<p:wrap-sequence wrapper="x:was" name="was">
	  <p:input port="source">
	    <p:pipe step="normalized-context" port="result"/>
	  </p:input>
	</p:wrap-sequence>
	<p:insert match="/*" position="last-child">
	  <p:input port="source">
	    <p:inline>
	      <x:test-result result="failed"/>
	    </p:inline>
	  </p:input>
	  <p:input port="insertion">
	    <p:pipe port="result" step="expected"/>
	    <p:pipe port="result" step="was"/>
	  </p:input>
	</p:insert>
      </p:otherwise>
    </p:choose>
</p:declare-step>