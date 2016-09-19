<p:declare-step type="pxi:css-to-ssml" version="1.0"
		xmlns:p="http://www.w3.org/ns/xproc"
		xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
		exclude-inline-prefixes="#all">

  <p:input port="source" sequence="true" primary="true"/>
  <p:output port="result" sequence="true" primary="true"/>

  <p:for-each>
    <p:xslt>
      <p:input port="parameters">
	<p:empty/>
      </p:input>
      <p:input port="stylesheet">
	<p:document href="../xslt/css-to-ssml.xsl"/>
      </p:input>
    </p:xslt>
  </p:for-each>

</p:declare-step>
