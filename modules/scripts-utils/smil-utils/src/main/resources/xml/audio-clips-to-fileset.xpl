<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:audio-clips-to-fileset" name="main">

  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>Convert a d:audio-clips document to a d:fileset listing the referenced audio files.</p>
  </p:documentation>

  <p:input port="source">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The d:audio-clips document</p>
    </p:documentation>
  </p:input>
  <p:output port="result">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The d:fileset document with the list of distinct audio files.</p>
    </p:documentation>
  </p:output>

  <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
    <p:documentation>
      px:fileset-join
    </p:documentation>
  </p:import>

  <p:xslt>
    <p:input port="stylesheet">
      <p:document href="audio-clips-to-fileset.xsl"/>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
  </p:xslt>

  <p:documentation>Normalize the hrefs</p:documentation>
  <px:fileset-join/>

</p:declare-step>
