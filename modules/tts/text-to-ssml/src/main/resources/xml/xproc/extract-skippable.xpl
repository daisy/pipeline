<p:declare-step type="pxi:extract-skippable" version="1.0" name="main"
		xmlns:p="http://www.w3.org/ns/xproc"
		xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
		xmlns:ssml="http://www.w3.org/2001/10/synthesis"
		exclude-inline-prefixes="#all">

  <p:input port="source" primary="true"/>
  <p:input port="skippable-ids"/>
  <p:output port="skippable-free" primary="true"/>
  <p:output port="skippable-only">
    <p:pipe port="secondary" step="separate"/>
  </p:output>

  <p:xslt name="separate">
    <p:input port="source">
      <p:pipe port="source" step="main"/>
      <p:pipe port="skippable-ids" step="main"/>
    </p:input>
    <p:input port="stylesheet">
      <p:document href="../xslt/extract-skippable.xsl"/>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
  </p:xslt>

</p:declare-step>
