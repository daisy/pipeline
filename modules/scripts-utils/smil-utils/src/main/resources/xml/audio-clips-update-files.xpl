<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:audio-clips-update-files" name="main">

  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>Update file and id references in a d:audio-clips document according to a mapping provided
    through a d:fileset document.</p>
  </p:documentation>

  <p:input port="source" primary="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The input d:audio-clips document</p>
      <p>It is expected to have no two clips with the same idref.</p>
    </p:documentation>
  </p:input>

  <p:input port="mapping">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The d:fileset document</p>
    </p:documentation>
  </p:input>

  <p:output port="result">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The output d:audio-clips document</p>
      <p>References to audio files (<code>src</code> attributes) are updated according to the
      mapping from <code>@original-href</code> to <code>@href</code> in the "mapping" document. All
      references are relative to the base URI of the output document, which gets inherited from the
      mapping document. References to content documents (<code>idref</code> attributes) are updated
      according to the mapping from <code>@original-id</code> to <code>@id</code> in the mapping
      document. It is assumed that ID attributes are unique across all content documents.</p>
    </p:documentation>
  </p:output>

  <p:xslt>
    <p:input port="source">
      <p:pipe step="main" port="source"/>
      <p:pipe step="main" port="mapping"/>
    </p:input>
    <p:input port="stylesheet">
      <p:document href="audio-clips-update-files.xsl"/>
    </p:input>
    <p:with-param name="output-base-uri" select="base-uri(/*)">
      <p:pipe step="main" port="mapping"/>
    </p:with-param>
    <p:with-option name="output-base-uri" select="base-uri(/*)">
      <p:pipe step="main" port="mapping"/>
    </p:with-option>
  </p:xslt>

</p:declare-step>
