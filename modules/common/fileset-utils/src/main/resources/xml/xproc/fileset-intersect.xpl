<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="px"
                type="px:fileset-intersect">

  <p:input port="source" sequence="true"/>
  <p:output port="result">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>A fileset that contains only the files that are in all input filesets. Only the attributes
      from the <code>d:file</code> elements of the first input fileset are retained. Only the
      xml:base attribute of the first <code>d:fileset</code> is retained.</p>
      <p>The fileset is normalized.</p>
    </p:documentation>
  </p:output>

  <p:import href="fileset-join.xpl">
    <p:documentation>
      px:fileset-join
    </p:documentation>
  </p:import>

  <!-- Normalize URIs -->
  <p:for-each>
    <px:fileset-join/>
  </p:for-each>

  <p:xslt>
    <p:input port="stylesheet">
      <p:document href="../xslt/fileset-intersect.xsl"/>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
  </p:xslt>

</p:declare-step>
