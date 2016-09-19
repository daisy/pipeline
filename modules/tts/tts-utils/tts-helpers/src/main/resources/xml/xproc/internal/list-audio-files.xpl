<p:declare-step version="1.0" type="pxi:list-audio-files"
		xmlns:p="http://www.w3.org/ns/xproc"
		xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal">

  <p:input port="source" primary="true" /> <!-- audio clips -->
  <p:output port="result" primary="true" />  <!-- list of distinct audio files -->

  <p:xslt>
    <p:input port="stylesheet">
      <p:document href="list-audio-files.xsl"/>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
  </p:xslt>

</p:declare-step>
