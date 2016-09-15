<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="pxi:info-document" name="main" version="1.0"
		xmlns:p="http://www.w3.org/ns/xproc"
		xmlns:x="http://www.daisy.org/ns/xprocspec"
		xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal">

  <p:input port="context" primary="false"/>
  <p:input port="expect" primary="false"/>
  <p:input port="parameters" kind="parameter" primary="true"/>
  <p:output port="result" primary="true"/>

  <p:xslt name="get-info">
    <p:input port="source">
      <p:pipe port="context" step="main"/>
    </p:input>
    <p:input port="stylesheet">
      <p:document href="xslt/info-document.xsl"/>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
  </p:xslt>

  <p:choose>
    <p:when test="//*[@status and @status != 'ok']">
      <p:wrap-sequence wrapper="x:was" name="was">
	<p:input port="source">
	  <p:pipe step="get-info" port="result"/>
	</p:input>
      </p:wrap-sequence>
      <p:insert match="/*" position="last-child">
	<p:input port="source">
	  <p:inline>
	    <x:test-result result="failed"/>
	  </p:inline>
	</p:input>
	<p:input port="insertion">
	  <p:pipe port="result" step="was"/>
	</p:input>
      </p:insert>
    </p:when>
    <p:otherwise>
      <p:identity>
	<p:input port="source">
	  <p:inline>
	    <x:test-result result="passed"/>
	  </p:inline>
	</p:input>
      </p:identity>
    </p:otherwise>
  </p:choose>
</p:declare-step>
