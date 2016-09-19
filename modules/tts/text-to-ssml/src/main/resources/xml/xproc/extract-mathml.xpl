<p:declare-step type="pxi:extract-mathml" version="1.0" name="main"
		xmlns:p="http://www.w3.org/ns/xproc"
		xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
		exclude-inline-prefixes="#all">

  <p:input port="source" primary="true"/>
  <p:input port="math-ids"/>

  <p:output port="mathml-free" primary="true"/>
  <p:output port="mathml-only">
    <p:pipe port="secondary" step="separate"/>
  </p:output>

  <p:xslt name="separate">
    <p:input port="source">
      <p:pipe port="source" step="main"/>
      <p:pipe port="math-ids" step="main"/>
    </p:input>
    <p:input port="stylesheet">
      <p:document href="../xslt/extract-mathml.xsl"/>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
  </p:xslt>

</p:declare-step>
