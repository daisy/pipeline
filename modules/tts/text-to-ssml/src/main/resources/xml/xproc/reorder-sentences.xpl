<p:declare-step type="pxi:reorder-sentences" version="1.0" name="main"
		xmlns:p="http://www.w3.org/ns/xproc"
		xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
		exclude-inline-prefixes="#all">

  <p:input port="source" sequence="true" primary="true"/>
  <p:input port="ids-in-order"/>

  <p:output port="result" sequence="true" primary="true"/>

  <p:xslt>
    <p:input port="stylesheet">
      <p:document href="../xslt/reorder-sentences.xsl"/>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
    <p:input port="source">
      <p:pipe port="ids-in-order" step="main"/>
      <p:pipe port="source" step="main"/>
    </p:input>
  </p:xslt>

</p:declare-step>
